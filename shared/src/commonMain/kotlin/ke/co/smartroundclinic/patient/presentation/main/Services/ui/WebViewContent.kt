package ke.co.smartroundclinic.patient.presentation.main.Services.ui

import androidx.compose.runtime.Composable

/**
 * Opens [url] in the platform's in-app browser as a side-effect of entering composition.
 * Android uses Chrome Custom Tabs; iOS uses SFSafariViewController.
 * Re-fires if [url] changes.
 */
@Composable
expect fun LaunchBrowserEffect(url: String)
