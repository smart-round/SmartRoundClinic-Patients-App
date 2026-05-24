package ke.co.smartroundclinic.patient.domain.usecase.datastore

import ke.co.smartroundclinic.patient.domain.repository.DatastoreRepository
import kotlinx.coroutines.flow.Flow

class ObserveKeyUseCase(private val repository: DatastoreRepository) {
    operator fun invoke(key: String): Flow<String?> = repository.observe(key)
}
