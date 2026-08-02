package ke.co.smartroundclinic.patient.domain.usecase.doctor

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.Doctor
import ke.co.smartroundclinic.patient.domain.repository.DoctorRepository

/** Single-doctor lookup, e.g. resolving a full [Doctor] (with specialization/rating/etc.) for a
 * doctor named on a referral, who may never have appeared in the patient's browsed/cached list. */
class GetDoctorByIdUseCase(private val repository: DoctorRepository) {
    suspend operator fun invoke(doctorId: String): Resource<Doctor?> {
        val result = repository.getDoctorById(doctorId)
        val response = result.data ?: return Resource.Error(result.message ?: "Failed to fetch doctor")
        return Resource.Success(response.data?.toDomain())
    }
}
