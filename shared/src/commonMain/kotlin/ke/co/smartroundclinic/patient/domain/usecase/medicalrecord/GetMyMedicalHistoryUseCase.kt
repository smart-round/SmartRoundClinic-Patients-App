package ke.co.smartroundclinic.patient.domain.usecase.medicalrecord

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.MedicalRecord
import ke.co.smartroundclinic.patient.domain.repository.MedicalRecordRepository

class GetMyMedicalHistoryUseCase(private val repository: MedicalRecordRepository) {
    suspend operator fun invoke(): Resource<List<MedicalRecord>> =
        when (val result = repository.getMyHistory()) {
            is Resource.Success -> Resource.Success(
                data = result.data?.data?.map { it.toDomain() } ?: emptyList(),
                message = result.message ?: "Success",
            )
            is Resource.Error -> Resource.Error(result.message ?: "An unknown error occurred")
            is Resource.Loading -> Resource.Loading()
        }
}
