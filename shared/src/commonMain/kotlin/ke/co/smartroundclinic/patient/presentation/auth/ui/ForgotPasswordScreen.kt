package ke.co.smartroundclinic.patient.presentation.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ke.co.smartroundclinic.patient.common.isValidEmail
import ke.co.smartroundclinic.patient.presentation.auth.ForgotPasswordViewModel
import ke.co.smartroundclinic.patient.presentation.common.composables.PrimaryButton
import ke.co.smartroundclinic.patient.presentation.theme.ShapeInput
import ke.co.smartroundclinic.patient.presentation.theme.smartRoundColors

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onProceed: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val emailError = if (email.isNotBlank() && !email.isValidEmail()) "Enter a valid email address" else null
    val canProceed = email.isNotBlank() && emailError == null && !isLoading

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.smartRoundColors.topAppBarGradientStart,
                            MaterialTheme.smartRoundColors.topAppBarGradientEnd,
                        )
                    )
                )
        ) {
            Column(modifier = modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                Text(
                    text = "Forgot Password",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Provide your registered email to reset your password securely",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", style = MaterialTheme.typography.bodySmall) },
                placeholder = { Text("Enter your email", style = MaterialTheme.typography.bodySmall) },
                isError = emailError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = ShapeInput,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (emailError != null) {
                Text(
                    text = emailError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                )
            }

            Spacer(Modifier.height(32.dp))

            PrimaryButton(
                onClick = { viewModel.requestPasswordReset(email, onProceed) },
                enabled = canProceed,
            ) {
                if (isLoading) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 14.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                } else {
                    Text(
                        text = "Proceed",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(vertical = 14.dp),
                    )
                }
            }
        }
    }
}
