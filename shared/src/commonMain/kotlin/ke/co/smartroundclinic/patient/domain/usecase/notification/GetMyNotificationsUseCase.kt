package ke.co.smartroundclinic.patient.domain.usecase.notification

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetNotificationsRes
import ke.co.smartroundclinic.patient.domain.repository.NotificationRepository

class GetMyNotificationsUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(page: Int = 1, size: Int = 20): Resource<GetNotificationsRes> =
        repository.getMyNotifications(page, size)
}
