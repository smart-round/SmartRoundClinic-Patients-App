package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.core.database.entity.DoctorEntity

interface DoctorLocalRepository {
    suspend fun getDoctors(): List<DoctorEntity>
    suspend fun saveDoctors(entities: List<DoctorEntity>)
}
