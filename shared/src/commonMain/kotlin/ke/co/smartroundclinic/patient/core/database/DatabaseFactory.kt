package ke.co.smartroundclinic.patient.core.database

import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

fun createDatabase(): AppDatabase =
    getDatabaseBuilder()
        .fallbackToDestructiveMigration(true)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
