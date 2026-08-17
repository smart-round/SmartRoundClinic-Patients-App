package ke.co.smartroundclinic.patient.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
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

    /**
     * A refresh must reflect exactly what the server just returned — upserting alone left
     * doctors who dropped out of the recommended list (or changed id) stuck in the cache forever.
     */
    @Transaction
    suspend fun replaceAll(doctors: List<DoctorEntity>) {
        deleteAll()
        upsertAll(doctors)
    }
}
