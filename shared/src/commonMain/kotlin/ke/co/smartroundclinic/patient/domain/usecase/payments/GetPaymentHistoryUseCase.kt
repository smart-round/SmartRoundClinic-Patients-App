package ke.co.smartroundclinic.patient.domain.usecase.payments

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetPaymentHistoryRes
import ke.co.smartroundclinic.patient.domain.repository.PaymentsRepository

class GetPaymentHistoryUseCase(private val repository: PaymentsRepository) {
    suspend operator fun invoke(page: Int = 1, size: Int = 20): Resource<GetPaymentHistoryRes> =
        repository.getPaymentsHistory(page, size)
}
