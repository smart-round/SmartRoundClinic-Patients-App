package ke.co.smartroundclinic.patient.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import ke.co.smartroundclinic.patient.core.database.dao.ArticleCategoryDao
import ke.co.smartroundclinic.patient.core.database.dao.ArticleDao
import ke.co.smartroundclinic.patient.core.database.dao.DoctorDao
import ke.co.smartroundclinic.patient.core.database.dao.ServiceCategoryDao
import ke.co.smartroundclinic.patient.core.database.dao.SpecialityDao
import ke.co.smartroundclinic.patient.core.database.dao.UserDao
import ke.co.smartroundclinic.patient.core.database.entity.ArticleCategoryEntity
import ke.co.smartroundclinic.patient.core.database.entity.ArticleEntity
import ke.co.smartroundclinic.patient.core.database.entity.DoctorEntity
import ke.co.smartroundclinic.patient.core.database.entity.ServiceCategoryEntity
import ke.co.smartroundclinic.patient.core.database.entity.SpecialityEntity
import ke.co.smartroundclinic.patient.core.database.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        SpecialityEntity::class,
        ArticleEntity::class,
        ArticleCategoryEntity::class,
        DoctorEntity::class,
        ServiceCategoryEntity::class,
    ],
    version = 8,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val specialityDao: SpecialityDao
    abstract val articleDao: ArticleDao
    abstract val articleCategoryDao: ArticleCategoryDao
    abstract val doctorDao: DoctorDao
    abstract val serviceCategoryDao: ServiceCategoryDao
}

// Room KSP generates the actual implementations per platform.
// -Xexpect-actual-classes + @Suppress allow this pattern to compile cleanly.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
