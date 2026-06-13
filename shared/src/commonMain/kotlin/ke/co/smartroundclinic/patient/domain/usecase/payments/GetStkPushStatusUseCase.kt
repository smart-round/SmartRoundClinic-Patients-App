package ke.co.smartroundclinic.patient.domain.usecase.payments

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.StkPushStatusData
import ke.co.smartroundclinic.patient.domain.repository.PaymentsRepository

class GetStkPushStatusUseCase(private val repository: PaymentsRepository) {
    suspend operator fun invoke(invoiceId: String): Resource<StkPushStatusData> =
        repository.getStkPushStatus(invoiceId)
}
