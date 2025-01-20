package alragar2.isi3.uv.flagflash.BaseDatos.Usuarios

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM User WHERE username = :username")
    suspend fun getUser(username: String): User?
}