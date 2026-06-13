package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetNotificationsRes

interface NotificationRepository {
    suspend fun registerDeviceToken(token: String, platform: String): Resource<Unit>
    suspend fun getMyNotifications(page: Int = 1, size: Int = 20): Resource<GetNotificationsRes>
    suspend fun markAsRead(id: String): Resource<Unit>
}
