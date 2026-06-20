package ke.co.smartroundclinic.patient.presentation.main.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ke.co.smartroundclinic.patient.presentation.common.composables.PrimaryButton
import ke.co.smartroundclinic.patient.presentation.main.profile.MedicalBioViewModel
import ke.co.smartroundclinic.patient.presentation.theme.CardBackground
import ke.co.smartroundclinic.patient.presentation.theme.Primary40
import ke.co.smartroundclinic.patient.presentation.theme.Primary90
import ke.co.smartroundclinic.patient.presentation.theme.ShapeInput

private val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
private val maritalStatuses = listOf("SINGLE", "MARRIED", "DIVORCED", "WIDOWED")
private val weightUnits = listOf("KG", "LBS")
private val heightUnits = listOf("CM", "INCHES")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MedicalBioScreen(
    viewModel: MedicalBioViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = "Medical Information",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                },
                modifier = Modifier.statusBarsPadding(),
            )
        },
    ) { paddingValues ->
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Primary40)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionCard(title = "Personal Stats") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = viewModel.weight,
                        onValueChange = { viewModel.weight = it },
                        label = { Text("Weight", style = MaterialTheme.typography.bodySmall) },
                        shape = ShapeInput,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                    UnitToggle(
                        options = weightUnits,
                        selected = viewModel.weightUnit,
                        onSelect = { viewModel.weightUnit = it },
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = viewModel.height,
                        onValueChange = { viewModel.height = it },
                        label = { Text("Height", style = MaterialTheme.typography.bodySmall) },
                        shape = ShapeInput,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                    UnitToggle(
                        options = heightUnits,
                        selected = viewModel.heightUnit,
                        onSelect = { viewModel.heightUnit = it },
                    )
                }
                Spacer(Modifier.height(12.dp))
                var bloodGroupExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = bloodGroupExpanded,
                    onExpandedChange = { bloodGroupExpanded = it },
                ) {
                    OutlinedTextField(
                        value = viewModel.bloodGroup,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Blood Group", style = MaterialTheme.typography.bodySmall) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodGroupExpanded) },
                        shape = ShapeInput,
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = bloodGroupExpanded,
                        onDismissRequest = { bloodGroupExpanded = false },
                        containerColor = MaterialTheme.colorScheme.background,
                    ) {
                        bloodGroups.forEach { bg ->
                            DropdownMenuItem(
                                colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.onBackground),
                                text = { Text(bg) },
                                onClick = { viewModel.bloodGroup = bg; bloodGroupExpanded = false },
                            )
                        }
                    }
                }
            }

            SectionCard(title = "Marital Status") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    maritalStatuses.forEach { status ->
                        FilterChip(
                            selected = viewModel.maritalStatus == status,
                            onClick = { viewModel.maritalStatus = status },
                            label = {
                                Text(
                                    text = status.replaceFirstChar { it.uppercase() }.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary40,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }
            }

            ChipListCard(
                title = "Allergies",
                items = viewModel.allergies,
                onAdd = { viewModel.addAllergy(it) },
                onRemove = { viewModel.removeAllergy(it) },
            )

            ChipListCard(
                title = "Chronic Conditions",
                items = viewModel.chronicConditions,
                onAdd = { viewModel.addChronicCondition(it) },
                onRemove = { viewModel.removeChronicCondition(it) },
            )

            ChipListCard(
                title = "Current Medications",
                items = viewModel.currentMedications,
                onAdd = { viewModel.addMedication(it) },
                onRemove = { viewModel.removeMedication(it) },
            )

            Spacer(Modifier.height(8.dp))

            PrimaryButton(
                onClick = { viewModel.save() },
                enabled = !viewModel.isSaving,
            ) {
                if (viewModel.isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        "Save Changes",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 14.dp),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipListCard(
    title: String,
    items: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    var showInput by remember { mutableStateOf(false) }
    var inputValue by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .background(Primary90, RoundedCornerShape(8.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { showInput = !showInput }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = "Add", tint = Primary40, modifier = Modifier.size(14.dp))
                        Text("Add", style = MaterialTheme.typography.labelSmall, color = Primary40)
                    }
                }
            }

            if (items.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .background(Primary90, RoundedCornerShape(20.dp))
                                .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) { onRemove(item) },
                            )
                        }
                    }
                }
            }

            if (showInput) {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    placeholder = { Text("Type and press Done", style = MaterialTheme.typography.bodySmall) },
                    singleLine = true,
                    shape = ShapeInput,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (inputValue.isNotBlank()) {
                                onAdd(inputValue)
                                inputValue = ""
                                showInput = false
                            }
                        },
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (inputValue.isNotBlank()) {
                                onAdd(inputValue)
                                inputValue = ""
                                showInput = false
                            }
                        }) {
                            Icon(Icons.Filled.Add, contentDescription = "Confirm", tint = Primary40)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun UnitToggle(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .background(
                        if (isSelected) Primary40 else Color.Transparent,
                        RoundedCornerShape(6.dp),
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onSelect(option) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
