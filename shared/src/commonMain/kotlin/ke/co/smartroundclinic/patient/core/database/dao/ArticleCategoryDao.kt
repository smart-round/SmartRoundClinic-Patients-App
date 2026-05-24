package ke.co.smartroundclinic.patient.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ke.co.smartroundclinic.patient.core.database.entity.ArticleCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleCategoryDao {
    @Query("SELECT * FROM article_categories WHERE isActive = 1 ORDER BY name ASC")
    fun observeActiveCategories(): Flow<List<ArticleCategoryEntity>>

    @Query("SELECT * FROM article_categories WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getActiveCategories(): List<ArticleCategoryEntity>

    @Upsert
    suspend fun upsertCategories(categories: List<ArticleCategoryEntity>)
}
