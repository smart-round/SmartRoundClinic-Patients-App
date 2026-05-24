package ke.co.smartroundclinic.patient.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.core.context.GlobalContext

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val context = GlobalContext.get().get<Context>()
    val dbPath = context.getDatabasePath("patient_smartround.db").absolutePath
    return Room.databaseBuilder<AppDatabase>(context = context, name = dbPath)
        .setDriver(BundledSQLiteDriver())
}
