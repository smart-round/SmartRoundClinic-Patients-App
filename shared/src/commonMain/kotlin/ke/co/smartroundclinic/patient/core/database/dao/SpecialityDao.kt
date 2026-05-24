package ke.co.smartroundclinic.patient.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ke.co.smartroundclinic.patient.core.database.entity.SpecialityEntity

@Dao
interface SpecialityDao {
    @Query("SELECT * FROM specialities")
    suspend fun getAll(): List<SpecialityEntity>

    @Upsert
    suspend fun upsertAll(specialities: List<SpecialityEntity>)

    @Query("DELETE FROM specialities")
    suspend fun deleteAll()
}
