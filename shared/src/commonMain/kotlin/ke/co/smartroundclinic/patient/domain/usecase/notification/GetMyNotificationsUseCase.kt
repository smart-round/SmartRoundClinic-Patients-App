package ke.co.smartroundclinic.patient.domain.usecase.notification

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.Notification
import ke.co.smartroundclinic.patient.domain.repository.NotificationRepository

class GetMyNotificationsUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(): Resource<List<Notification>> =
        repository.getMyNotifications()
}
