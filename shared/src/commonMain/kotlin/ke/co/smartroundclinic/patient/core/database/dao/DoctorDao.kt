package ke.co.smartroundclinic.patient.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ke.co.smartroundclinic.patient.core.database.entity.DoctorEntity

@Dao
interface DoctorDao {
    @Query("SELECT * FROM doctors")
    suspend fun getAll(): List<DoctorEntity>

    @Upsert
    suspend fun upsertAll(doctors: List<DoctorEntity>)

    @Query("DELETE FROM doctors")
    suspend fun deleteAll()
}
