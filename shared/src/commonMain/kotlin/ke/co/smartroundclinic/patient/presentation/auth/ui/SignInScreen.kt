package ke.co.smartroundclinic.patient.presentation.auth.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ke.co.smartroundclinic.patient.presentation.auth.SignInViewModel
import ke.co.smartroundclinic.patient.presentation.common.composables.PrimaryButton
import ke.co.smartroundclinic.patient.presentation.theme.ShapeInput
import ke.co.smartroundclinic.patient.presentation.theme.smartRoundColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SignInScreen(
    onForgotPassword: () -> Unit,
    onSignUp: () -> Unit,
    onSignIn: () -> Unit = {},
    onUnverified: (email: String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = koinViewModel(),
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val isSigningIn by viewModel.isSigningIn.collectAsStateWithLifecycle()
    val isWrongApp by viewModel.isWrongApp.collectAsStateWithLifecycle()
    val isFormValid = email.isNotBlank() && password.isNotBlank()

    if (isWrongApp) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissWrongApp() },
            title = { Text("Wrong App") },
            text = {
                Text(
                    "This account is registered as a doctor. Please use the SmartRound Clinic Doctor app to sign in.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissWrongApp() }) {
                    Text("OK")
                }
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
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
                    text = "Welcome Back",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Sign in to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Column(modifier = modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", style = MaterialTheme.typography.bodySmall) },
                placeholder = { Text("Enter your email", style = MaterialTheme.typography.bodySmall) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = ShapeInput,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", style = MaterialTheme.typography.bodySmall) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = ShapeInput,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            TextButton(
                onClick = onForgotPassword,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.height(8.dp))

            PrimaryButton(
                onClick = {
                    viewModel.signIn(
                        email = email,
                        password = password,
                        onSuccess = onSignIn,
                        onUnverified = onUnverified,
                    )
                },
                enabled = isFormValid && !isSigningIn,
            ) {
                if (isSigningIn) {
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
                        text = "Sign In",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(vertical = 14.dp),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                TextButton(
                    onClick = onSignUp,
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(
                        text = "Sign up",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
