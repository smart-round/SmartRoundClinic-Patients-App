package ke.co.smartroundclinic.patient.domain.usecase.doctor

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetDoctorBySpecializationRes
import ke.co.smartroundclinic.patient.domain.repository.DoctorRepository

class GetDoctorsBySpecializationUseCase(private val repository: DoctorRepository) {
    suspend operator fun invoke(
        specializationId: String,
        page: Int = 1,
        size: Int = 20,
    ): Resource<GetDoctorBySpecializationRes> =
        repository.getDoctorBySpecialization(specializationId, page, size)
}
