package alragar2.isi3.uv.flagflash.BaseDatos

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Pais::class], version = 2)
abstract class PaisDatabase : RoomDatabase() {
    abstract fun paisDao(): PaisDao
}