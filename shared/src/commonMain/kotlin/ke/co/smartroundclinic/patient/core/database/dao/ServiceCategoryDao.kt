package ke.co.smartroundclinic.patient.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ke.co.smartroundclinic.patient.core.database.entity.ServiceCategoryEntity

@Dao
interface ServiceCategoryDao {
    @Query("SELECT * FROM service_categories")
    suspend fun getAll(): List<ServiceCategoryEntity>

    @Upsert
    suspend fun upsertAll(categories: List<ServiceCategoryEntity>)

    @Query("DELETE FROM service_categories")
    suspend fun deleteAll()
}
