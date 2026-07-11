package ke.co.smartroundclinic.patient.domain.usecase.notification

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.repository.NotificationRepository

class RegisterDeviceTokenUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(token: String, platform: String, tokenType: String = "STANDARD"): Resource<Unit> =
        repository.registerDeviceToken(token, platform, tokenType)
}
