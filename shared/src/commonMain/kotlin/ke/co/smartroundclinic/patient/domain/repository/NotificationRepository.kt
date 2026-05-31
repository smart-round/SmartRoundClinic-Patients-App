package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.Notification

interface NotificationRepository {
    suspend fun registerDeviceToken(token: String, platform: String): Resource<Unit>
    suspend fun getMyNotifications(page: Int = 1, size: Int = 50): Resource<List<Notification>>
    suspend fun markAsRead(id: String): Resource<Unit>
}
