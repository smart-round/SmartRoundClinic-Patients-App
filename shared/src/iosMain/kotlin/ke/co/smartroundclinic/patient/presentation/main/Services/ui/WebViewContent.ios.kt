@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package ke.co.smartroundclinic.patient.presentation.main.Services.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIApplication

@Composable
actual fun LaunchBrowserEffect(url: String) {
    LaunchedEffect(url) {
        val nsUrl = NSURL.URLWithString(url) ?: return@LaunchedEffect
        val safari = SFSafariViewController(uRL = nsUrl)
        var topVC = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (topVC?.presentedViewController != null) {
            topVC = topVC?.presentedViewController
        }
        topVC?.presentViewController(safari, animated = true, completion = null)
    }
}
