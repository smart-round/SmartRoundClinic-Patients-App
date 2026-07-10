package ke.co.smartroundclinic.patient.domain.usecase.consultation

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.MergedHistoryPage
import ke.co.smartroundclinic.patient.domain.repository.ConsultationRepository

class GetMergedConsultationHistoryUseCase(private val repository: ConsultationRepository) {
    suspend operator fun invoke(
        doctorId: String,
        patientId: String,
        before: String? = null,
        size: Int = 50,
    ): Resource<MergedHistoryPage> = repository.getMergedMessages(doctorId, patientId, before, size)
}
