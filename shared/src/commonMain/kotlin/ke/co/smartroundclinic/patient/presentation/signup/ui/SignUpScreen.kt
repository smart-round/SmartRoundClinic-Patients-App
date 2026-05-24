package ke.co.smartroundclinic.patient.presentation.signup.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import ke.co.smartroundclinic.patient.common.isValidEmail
import ke.co.smartroundclinic.patient.core.platform.todayDay
import ke.co.smartroundclinic.patient.core.platform.todayMonth
import ke.co.smartroundclinic.patient.core.platform.todayYear
import ke.co.smartroundclinic.patient.generated.resources.Res
import ke.co.smartroundclinic.patient.presentation.common.composables.PrimaryButton
import ke.co.smartroundclinic.patient.presentation.signup.SignUpFilesViewModel
import ke.co.smartroundclinic.patient.presentation.signup.SignUpFormViewModel
import ke.co.smartroundclinic.patient.presentation.theme.ShapeInput
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

@Serializable
data class CountryCode(val name: String, val dialCode: String, val flag: String)

val defaultCountry = CountryCode("Kenya", "+254", "🇰🇪")

@OptIn(ExperimentalResourceApi::class)
@Composable
fun rememberCountryCodes(): List<CountryCode> {
    var countries by remember { mutableStateOf(listOf(defaultCountry)) }
    LaunchedEffect(Unit) {
        runCatching {
            val bytes = Res.readBytes("files/country_codes.json")
            countries = Json.decodeFromString(bytes.decodeToString())
        }
    }
    return countries
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    filesViewModel: SignUpFilesViewModel,
    formViewModel: SignUpFormViewModel,
    onNext: (email: String) -> Unit,
    onSignIn: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var countryQuery by remember { mutableStateOf("") }

    val countrySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val allCountries = rememberCountryCodes()
    val isLoading by formViewModel.isLoading.collectAsStateWithLifecycle()

    val galleryLauncher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        scope.launch { filesViewModel.profilePictureBytes = file?.readBytes() }
    }

    val genderOptions = listOf("Male", "Female", "Other")

    val filteredCountries = remember(countryQuery, allCountries) {
        if (countryQuery.isEmpty()) allCountries
        else allCountries.filter {
            it.name.contains(countryQuery, ignoreCase = true) || it.dialCode.contains(countryQuery)
        }
    }

    val dobError = when {
        formViewModel.dob.isEmpty() -> null
        formViewModel.dob.length < 8 -> "Enter a complete date (DD/MM/YYYY)"
        !formViewModel.dob.isValidDate() -> "Invalid date"
        !formViewModel.dob.isOldEnough(minAge = 0) -> "Invalid date of birth"
        else -> null
    }
    val emailError = if (formViewModel.email.isNotBlank() && !formViewModel.email.isValidEmail()) "Enter a valid email" else null

    val isFormValid = formViewModel.fullName.isNotBlank() &&
            formViewModel.gender.isNotBlank() &&
            formViewModel.dob.length == 8 && formViewModel.dob.isValidDate() &&
            formViewModel.email.isValidEmail() &&
            formViewModel.phoneNumber.isNotBlank() &&
            formViewModel.password.length >= 8

    if (showCountryPicker) {
        CountryCodeBottomSheet(
            sheetState = countrySheetState,
            query = countryQuery,
            onQueryChange = { countryQuery = it },
            countries = filteredCountries,
            onSelect = { country ->
                formViewModel.countryDialCode = country.dialCode
                formViewModel.countryFlag = country.flag
                formViewModel.countryName = country.name
                countryQuery = ""
                scope.launch {
                    countrySheetState.hide()
                    showCountryPicker = false
                }
            },
            onDismiss = {
                showCountryPicker = false
                countryQuery = ""
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .clickable { galleryLauncher.launch() },
        ) {
            if (filesViewModel.profilePictureBytes != null) {
                AsyncImage(
                    model = filesViewModel.profilePictureBytes,
                    contentDescription = "Profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Icon(
                    painter = rememberVectorPainter(Icons.Default.Person),
                    contentDescription = "Upload photo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp),
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text("Upload Photo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = formViewModel.fullName,
            onValueChange = { formViewModel.fullName = it },
            label = { Text("Full Name", style = MaterialTheme.typography.bodySmall) },
            placeholder = { Text("Enter your full name", style = MaterialTheme.typography.bodySmall) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            shape = ShapeInput,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = it },
        ) {
            OutlinedTextField(
                value = formViewModel.gender,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender", style = MaterialTheme.typography.bodySmall) },
                trailingIcon = {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                },
                shape = ShapeInput,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, style = MaterialTheme.typography.bodySmall) },
                        onClick = {
                            formViewModel.gender = option
                            genderExpanded = false
                        },
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = formViewModel.dob,
            onValueChange = { raw ->
                if (raw.length <= 8 && raw.all { it.isDigit() }) formViewModel.dob = raw
            },
            label = { Text("Date of Birth", style = MaterialTheme.typography.bodySmall) },
            placeholder = { Text("DD/MM/YYYY", style = MaterialTheme.typography.bodySmall) },
            isError = dobError != null,
            visualTransformation = DobVisualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
            shape = ShapeInput,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (dobError != null) {
            Text(dobError, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 4.dp, top = 2.dp).fillMaxWidth())
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = formViewModel.email,
            onValueChange = { formViewModel.email = it },
            label = { Text("Email", style = MaterialTheme.typography.bodySmall) },
            placeholder = { Text("Enter your email", style = MaterialTheme.typography.bodySmall) },
            isError = emailError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            shape = ShapeInput,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (emailError != null) {
            Text(emailError, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 4.dp, top = 2.dp).fillMaxWidth())
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = "${formViewModel.countryFlag} ${formViewModel.countryDialCode}",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                shape = ShapeInput,
                singleLine = true,
                modifier = Modifier
                    .width(120.dp)
                    .clickable { showCountryPicker = true },
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = formViewModel.phoneNumber,
                onValueChange = { if (it.all { c -> c.isDigit() }) formViewModel.phoneNumber = it },
                label = { Text("Phone", style = MaterialTheme.typography.bodySmall) },
                placeholder = { Text("712345678", style = MaterialTheme.typography.bodySmall) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                shape = ShapeInput,
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = formViewModel.password,
            onValueChange = { formViewModel.password = it },
            label = { Text("Password", style = MaterialTheme.typography.bodySmall) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "Hide" else "Show", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            shape = ShapeInput,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        PrimaryButton(
            onClick = {
                formViewModel.signUp(
                    onSuccess = { email -> onNext(email) },
                )
            },
            enabled = isFormValid && !isLoading,
        ) {
            if (isLoading) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 14.dp)) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            } else {
                Text("Sign Up", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(vertical = 14.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Text("Already have an account? ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
            TextButton(onClick = onSignIn, contentPadding = PaddingValues(0.dp)) {
                Text("Sign in", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryCodeBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    query: String,
    onQueryChange: (String) -> Unit,
    countries: List<CountryCode>,
    onSelect: (CountryCode) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(sheetState = sheetState, onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Search", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = ShapeInput,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn {
                items(countries) { country ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(country) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(country.flag, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.width(8.dp))
                        Text(country.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(country.dialCode, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private val DobVisualTransformation = VisualTransformation { text ->
    val digits = text.text
    val out = StringBuilder()
    for (i in digits.indices) {
        out.append(digits[i])
        if ((i == 1 || i == 3) && i < digits.lastIndex) out.append('/')
    }
    val offsetMapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            return when {
                offset <= 2 -> offset
                offset <= 4 -> offset + 1
                else -> offset + 2
            }.coerceAtMost(out.length)
        }
        override fun transformedToOriginal(offset: Int): Int {
            return when {
                offset <= 2 -> offset
                offset <= 5 -> offset - 1
                else -> offset - 2
            }.coerceAtMost(digits.length)
        }
    }
    TransformedText(AnnotatedString(out.toString()), offsetMapping)
}

private fun String.isValidDate(): Boolean {
    if (length != 8) return false
    val day = substring(0, 2).toIntOrNull() ?: return false
    val month = substring(2, 4).toIntOrNull() ?: return false
    val year = substring(4, 8).toIntOrNull() ?: return false
    return day in 1..31 && month in 1..12 && year in 1900..todayYear()
}

private fun String.isOldEnough(minAge: Int = 0): Boolean {
    if (length != 8) return false
    val day = substring(0, 2).toIntOrNull() ?: return false
    val month = substring(2, 4).toIntOrNull() ?: return false
    val year = substring(4, 8).toIntOrNull() ?: return false
    val currentYear = todayYear()
    val currentMonth = todayMonth()
    val currentDay = todayDay()
    val age = currentYear - year - if (month > currentMonth || (month == currentMonth && day > currentDay)) 1 else 0
    return age >= minAge
}
