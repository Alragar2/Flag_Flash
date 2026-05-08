package alragar2.isi3.uv.flagflash.BaseDatos
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Pais(
    @PrimaryKey
    val id: Int,
    val nombre: String,
    val capital: String,
    val bandera: String,
    val continente: String
)
