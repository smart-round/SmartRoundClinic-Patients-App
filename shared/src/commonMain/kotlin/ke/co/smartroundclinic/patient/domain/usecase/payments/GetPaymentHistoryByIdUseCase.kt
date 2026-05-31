package ke.co.smartroundclinic.patient.domain.usecase.payments

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetPaymentHistoryByIdRes
import ke.co.smartroundclinic.patient.domain.repository.PaymentsRepository

class GetPaymentHistoryByIdUseCase(private val repository: PaymentsRepository) {
    suspend operator fun invoke(id: String): Resource<GetPaymentHistoryByIdRes> =
        repository.getPaymentHistoryById(id)
}
