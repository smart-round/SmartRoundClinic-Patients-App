package ke.co.smartroundclinic.patient.domain.usecase.notification

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.repository.NotificationRepository

class MarkNotificationReadUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(id: String): Resource<Unit> = repository.markAsRead(id)
}
