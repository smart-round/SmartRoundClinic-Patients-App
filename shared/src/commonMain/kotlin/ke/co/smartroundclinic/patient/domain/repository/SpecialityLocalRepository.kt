package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.core.database.entity.SpecialityEntity

interface SpecialityLocalRepository {
    suspend fun getSpecialities(): List<SpecialityEntity>
    suspend fun saveSpecialities(entities: List<SpecialityEntity>)
    suspend fun hasSpecialities(): Boolean
}
