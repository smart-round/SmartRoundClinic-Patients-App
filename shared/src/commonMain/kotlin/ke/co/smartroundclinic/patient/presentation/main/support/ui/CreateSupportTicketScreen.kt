package ke.co.smartroundclinic.patient.presentation.main.support.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ke.co.smartroundclinic.patient.domain.model.IssueCategory
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import ke.co.smartroundclinic.patient.presentation.theme.Primary40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateSupportTicketScreen(
    categories: List<IssueCategory>,
    isCreating: Boolean,
    onBack: () -> Unit,
    onSubmit: (categoryId: String, title: String, description: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCategory by remember { mutableStateOf<IssueCategory?>(null) }
    var categoryDropdownOpen by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val canSubmit = selectedCategory != null && title.isNotBlank() && description.isNotBlank() && !isCreating

    Scaffold(modifier = modifier, contentWindowInsets = WindowInsets(0)) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
                    .statusBarsPadding()
                    .padding(vertical = 4.dp),
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("New Ticket", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White, modifier = Modifier.align(Alignment.Center).padding(vertical = 16.dp))
            }

            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).navigationBarsPadding()) {
                Spacer(Modifier.height(20.dp))
                Text("Issue Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Box {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select a category") },
                        trailingIcon = { Icon(Icons.Outlined.ExpandMore, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { categoryDropdownOpen = true },
                        enabled = false,
                    )
                    DropdownMenu(
                        expanded = categoryDropdownOpen,
                        onDismissRequest = { categoryDropdownOpen = false },
                        containerColor = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth(0.9f),
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.onBackground),
                                text = { Text(cat.name, style = MaterialTheme.typography.bodyMedium) },
                                onClick = { selectedCategory = cat; categoryDropdownOpen = false },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, placeholder = { Text("Brief summary of your issue") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, placeholder = { Text("Describe your issue in detail") }, minLines = 5, maxLines = 10, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(28.dp))
                Button(
                    onClick = { selectedCategory?.let { cat -> onSubmit(cat.id, title, description) } },
                    enabled = canSubmit,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary40),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isCreating) CircularProgressIndicator(modifier = Modifier.padding(4.dp).height(18.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Submit Ticket", modifier = Modifier.padding(vertical = 6.dp))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
