package ke.co.smartroundclinic.patient.domain.usecase.datastore

import ke.co.smartroundclinic.patient.domain.repository.DatastoreRepository

class GetKeyUseCase(private val repository: DatastoreRepository) {
    suspend operator fun invoke(key: String): String? = repository.getKey(key)
}
