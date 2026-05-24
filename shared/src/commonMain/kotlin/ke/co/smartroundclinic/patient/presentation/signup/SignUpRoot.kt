package ke.co.smartroundclinic.patient.presentation.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import ke.co.smartroundclinic.patient.generated.resources.Res
import ke.co.smartroundclinic.patient.generated.resources.top_appbar_logo
import ke.co.smartroundclinic.patient.presentation.signup.destinations.AccountVerification
import ke.co.smartroundclinic.patient.presentation.signup.destinations.SignUp
import ke.co.smartroundclinic.patient.presentation.signup.ui.AccountVerificationScreen
import ke.co.smartroundclinic.patient.presentation.signup.ui.SignUpScreen
import ke.co.smartroundclinic.patient.presentation.theme.smartRoundColors
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpRoot(
    onSignIn: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val filesViewModel: SignUpFilesViewModel = koinViewModel()
    val formViewModel: SignUpFormViewModel = koinViewModel()
    val backStack = retain { mutableStateListOf<NavKey>(SignUp) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier.wrapContentHeight(),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.top_appbar_logo),
                            contentScale = ContentScale.Crop,
                            contentDescription = "Smart Round Clinic",
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Get Started",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Create your patient account",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            NavDisplay(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background),
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<SignUp> {
                        SignUpScreen(
                            filesViewModel = filesViewModel,
                            formViewModel = formViewModel,
                            onNext = { email ->
                                backStack.add(AccountVerification(email))
                                backStack.remove(SignUp)
                            },
                            onSignIn = onSignIn,
                        )
                    }
                    entry<AccountVerification> { dest ->
                        AccountVerificationScreen(
                            email = dest.email,
                            onVerify = onSignIn,
                            onBack = { backStack.removeLastOrNull() },
                            onGoToLogin = onSignIn,
                        )
                    }
                }
            )
        }
    }
}
