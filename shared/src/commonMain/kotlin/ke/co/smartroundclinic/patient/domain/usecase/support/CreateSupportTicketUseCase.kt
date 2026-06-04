package ke.co.smartroundclinic.patient.domain.usecase.support

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.SupportTicket
import ke.co.smartroundclinic.patient.domain.repository.SupportRepository

class CreateSupportTicketUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(
        categoryId: String,
        title: String,
        description: String,
        complainantName: String,
        complainantEmail: String,
    ): Resource<SupportTicket> = repository.createTicket(categoryId, title, description, complainantName, complainantEmail)
}
