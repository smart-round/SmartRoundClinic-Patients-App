package ke.co.smartroundclinic.patient.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ke.co.smartroundclinic.patient.core.database.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE state = 'LIVE' ORDER BY datePosted DESC")
    fun observeLiveArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ArticleEntity?

    @Upsert
    suspend fun upsertArticles(articles: List<ArticleEntity>)

    @Query("DELETE FROM articles")
    suspend fun clearAll()
}
