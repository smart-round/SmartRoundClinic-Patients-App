package ke.co.smartroundclinic.patient.domain.usecase.support

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.IssueCategory
import ke.co.smartroundclinic.patient.domain.repository.SupportRepository

class GetIssueCategoriesUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(): Resource<List<IssueCategory>> = repository.getCategories()
}
