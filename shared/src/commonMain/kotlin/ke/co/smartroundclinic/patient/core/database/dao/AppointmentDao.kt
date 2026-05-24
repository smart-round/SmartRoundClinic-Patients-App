package ke.co.smartroundclinic.patient.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ke.co.smartroundclinic.patient.core.database.entity.AppointmentEntity

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments")
    suspend fun getAll(): List<AppointmentEntity>

    @Upsert
    suspend fun upsertAll(appointments: List<AppointmentEntity>)

    @Query("DELETE FROM appointments")
    suspend fun deleteAll()
}
