package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetSpecialitiesRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetSpecialityPricingRes

interface SpecialityRepository {
    suspend fun getAllSpecialities(): Resource<GetSpecialitiesRes>
    suspend fun getSpecialityPricing(id: String): Resource<GetSpecialityPricingRes>
}
