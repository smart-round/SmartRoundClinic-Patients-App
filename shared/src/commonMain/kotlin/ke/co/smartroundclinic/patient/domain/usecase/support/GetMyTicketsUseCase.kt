package ke.co.smartroundclinic.patient.domain.usecase.support

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetMyTicketsRes
import ke.co.smartroundclinic.patient.domain.repository.SupportRepository

class GetMyTicketsUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(page: Int = 1, size: Int = 20): Resource<GetMyTicketsRes> =
        repository.getMyTickets(page, size)
}
