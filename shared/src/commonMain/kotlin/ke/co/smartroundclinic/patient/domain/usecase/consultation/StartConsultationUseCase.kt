package ke.co.smartroundclinic.patient.domain.usecase.consultation

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.ConsultationSession
import ke.co.smartroundclinic.patient.domain.repository.ConsultationRepository

class StartConsultationUseCase(private val repository: ConsultationRepository) {
    suspend operator fun invoke(appointmentId: String): Resource<ConsultationSession> =
        repository.startOrGet(appointmentId)
}
