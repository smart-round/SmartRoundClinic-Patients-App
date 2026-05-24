package ke.co.smartroundclinic.patient.presentation.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ke.co.smartroundclinic.patient.presentation.auth.ForgotPasswordViewModel
import ke.co.smartroundclinic.patient.presentation.common.composables.PrimaryButton
import ke.co.smartroundclinic.patient.presentation.theme.ShapeInput
import ke.co.smartroundclinic.patient.presentation.theme.smartRoundColors
import kotlinx.coroutines.delay

@Composable
fun VerifyEmailScreen(
    viewModel: ForgotPasswordViewModel,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var otp by remember { mutableStateOf("") }
    val otpLength = 4
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var resendCountdown by remember { mutableIntStateOf(60) }

    LaunchedEffect(resendCountdown) {
        if (resendCountdown > 0) {
            delay(1000)
            resendCountdown--
        }
    }

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
                    text = "Verify Email",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Enter the verification code sent to your email to proceed with resetting your password",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Column(modifier = modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            BasicTextField(
                value = otp,
                onValueChange = {
                    if (it.length <= otpLength && it.all { c -> c.isDigit() }) otp = it
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                decorationBox = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(otpLength) { index ->
                            val char = otp.getOrNull(index)?.toString() ?: ""
                            val isCurrent = index == otp.length
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .size(64.dp)
                                    .border(
                                        width = if (isCurrent) 2.dp else 1.dp,
                                        color = if (isCurrent || char.isNotEmpty())
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outline,
                                        shape = ShapeInput,
                                    ),
                            ) {
                                Text(
                                    text = char,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            )

            Spacer(Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Didn't receive a code? ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                when {
                    isLoading -> CircularProgressIndicator(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(start = 4.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    resendCountdown > 0 -> Text(
                        text = "Resend in ${resendCountdown}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    else -> TextButton(
                        onClick = {
                            viewModel.resendPasswordResetOtp()
                            resendCountdown = 60
                        },
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(
                            text = "Resend Code",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            PrimaryButton(
                onClick = {
                    viewModel.saveOtp(otp)
                    onContinue()
                },
                enabled = otp.length == otpLength,
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(vertical = 14.dp),
                )
            }
        }
    }
}
