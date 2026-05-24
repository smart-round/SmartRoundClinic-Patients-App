package ke.co.smartroundclinic.patient.domain.usecase.doctor

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.Article
import ke.co.smartroundclinic.patient.domain.repository.DoctorRepository

class GetDoctorArticlesUseCase(private val repository: DoctorRepository) {
    suspend operator fun invoke(doctorId: String, page: Int = 1, size: Int = 20): Resource<List<Article>> =
        when (val result = repository.getDoctorArticles(doctorId, page, size)) {
            is Resource.Success -> Resource.Success(result.data?.data?.items?.map { it.toDomain() } ?: emptyList())
            is Resource.Error -> Resource.Error(result.message ?: "Failed to fetch articles")
            is Resource.Loading -> Resource.Loading()
        }
}
