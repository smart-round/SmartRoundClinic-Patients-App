package ke.co.smartroundclinic.patient.domain.usecase.speciality

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.SpecialityPricing
import ke.co.smartroundclinic.patient.domain.repository.SpecialityRepository

class GetSpecialityPricingUseCase(private val repository: SpecialityRepository) {
    suspend operator fun invoke(specializationId: String): Resource<SpecialityPricing> =
        when (val result = repository.getSpecialityPricing(specializationId)) {
            is Resource.Success -> result.data?.data?.toDomain()
                ?.let { Resource.Success(it) }
                ?: Resource.Error("No pricing data returned")
            is Resource.Error -> Resource.Error(result.message ?: "Failed to fetch pricing")
            is Resource.Loading -> Resource.Loading()
        }
}
