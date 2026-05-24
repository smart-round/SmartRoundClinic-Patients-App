package ke.co.smartroundclinic.patient.domain.usecase.datastore

import ke.co.smartroundclinic.patient.domain.repository.DatastoreRepository

class SetKeyUseCase(private val repository: DatastoreRepository) {
    suspend operator fun invoke(key: String, value: String?): Boolean = repository.setKey(key, value)
}
