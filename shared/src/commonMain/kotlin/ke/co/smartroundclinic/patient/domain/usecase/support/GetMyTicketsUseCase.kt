package ke.co.smartroundclinic.patient.domain.usecase.support

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.SupportTicket
import ke.co.smartroundclinic.patient.domain.repository.SupportRepository

class GetMyTicketsUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(): Resource<List<SupportTicket>> = repository.getMyTickets()
}
