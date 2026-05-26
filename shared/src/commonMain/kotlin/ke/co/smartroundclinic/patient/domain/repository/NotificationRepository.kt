package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource

interface NotificationRepository {
    suspend fun registerDeviceToken(token: String, platform: String): Resource<Unit>
}
