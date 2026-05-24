package ke.co.smartroundclinic.patient.presentation.main.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import ke.co.smartroundclinic.patient.presentation.common.composables.PrimaryButton
import ke.co.smartroundclinic.patient.presentation.main.profile.ProfileViewModel
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import ke.co.smartroundclinic.patient.presentation.theme.Primary40
import ke.co.smartroundclinic.patient.presentation.theme.ShapeInput
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private val genderOptions = listOf("Male", "Female", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PersonalInfoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val user by viewModel.user.collectAsState()
    val isSaving = viewModel.isUpdating
    val isSavingPicture = viewModel.isSavingPicture
    val scope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(user?.email) {
        user?.let {
            fullName = it.fullName
            email = it.email
            phone = it.phoneNumber ?: ""
            dateOfBirth = it.dateOfBirth ?: ""
            selectedGender = it.gender.replaceFirstChar { c -> c.uppercase() }
        }
    }

    val galleryLauncher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        scope.launch {
            file?.readBytes()?.let { bytes -> viewModel.uploadProfilePicture(bytes) }
        }
    }

    Scaffold(modifier = modifier, contentWindowInsets = WindowInsets(0)) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // ── Gradient header ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(brush = Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
                    .statusBarsPadding(),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = Color.White)
                        }
                        Text(
                            text = "Personal Information",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center).padding(vertical = 12.dp),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f))
                                .clickable { galleryLauncher.launch() },
                            contentAlignment = Alignment.Center,
                        ) {
                            when {
                                isSavingPicture -> CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
                                user?.profilePicture != null -> AsyncImage(
                                    model = user!!.profilePicture,
                                    contentDescription = "Profile picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                )
                                else -> Icon(Icons.Outlined.Person, null, tint = Color.White, modifier = Modifier.size(44.dp))
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Primary40)
                                .clickable { galleryLauncher.launch() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.CameraAlt, "Change photo", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Form ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .navigationBarsPadding(),
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name", style = MaterialTheme.typography.bodySmall) },
                    shape = ShapeInput,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address", style = MaterialTheme.typography.bodySmall) },
                    shape = ShapeInput,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", style = MaterialTheme.typography.bodySmall) },
                    shape = ShapeInput,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    label = { Text("Date of Birth (DD/MM/YYYY)", style = MaterialTheme.typography.bodySmall) },
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
                        value = selectedGender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Gender", style = MaterialTheme.typography.bodySmall) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                        shape = ShapeInput,
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                        genderOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { selectedGender = option; genderExpanded = false },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                PrimaryButton(
                    onClick = {
                        viewModel.updateProfile(
                            fullName = fullName,
                            email = email,
                            phoneNumber = phone.takeIf { it.isNotBlank() },
                            gender = selectedGender.lowercase().takeIf { it.isNotBlank() },
                            dateOfBirth = dateOfBirth.takeIf { it.isNotBlank() },
                        )
                    },
                    enabled = fullName.isNotBlank() && !isSaving,
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save Changes", style = MaterialTheme.typography.labelLarge, color = Color.White, modifier = Modifier.padding(vertical = 14.dp))
                    }
                }
            }
        }
    }
}