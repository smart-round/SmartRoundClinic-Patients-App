package ke.co.smartroundclinic.patient.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import ke.co.smartroundclinic.patient.App

// Extends FragmentActivity (not ComponentActivity) so the Cloudflare RealtimeKit
// UI Kit can attach its meeting fragment.
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}
