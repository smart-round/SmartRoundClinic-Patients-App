package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetServiceCategoriesRes

interface ServiceCategoryRepository {
    suspend fun getServiceCategories(page: Int, size: Int): Resource<GetServiceCategoriesRes>
}
