package ke.co.smartroundclinic.patient.core.presence

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

fun setupPresenceService() {
    val component = object : KoinComponent {
        val presenceService: PresenceService by inject()
    }
    component.presenceService.start()
}
