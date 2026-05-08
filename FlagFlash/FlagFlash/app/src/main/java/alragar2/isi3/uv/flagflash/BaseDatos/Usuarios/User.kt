package alragar2.isi3.uv.flagflash.BaseDatos.Usuarios

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) var id: Int,
    var username: String,
    var password: String
)