package alragar2.isi3.uv.flagflash.BaseDatos
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PaisDao {
    @Query("SELECT * FROM Pais")
    fun getAll(): List<Pais>

    @Insert
    fun insertAll(vararg paises: Pais)

    @Query("DELETE FROM Pais")
    fun deleteAll()

    @Query("SELECT id FROM Pais ORDER BY RANDOM() LIMIT 1")
    fun getRandomId(): Int

    @Query("SELECT * FROM Pais WHERE id = :id")
    fun getPais(id: Int): Pais

    @Query("SELECT nombre FROM Pais ORDER BY RANDOM() LIMIT 4")
    fun getFourRandomCountryNames(): List<String>

    @Query("SELECT * FROM Pais WHERE nombre = :name")
    fun getPaisByName(name: String): Pais

    @Query("SELECT id FROM Pais ORDER BY RANDOM() LIMIT 4")
    fun getFourRandomIds(): List<Int>

    @Query("SELECT * FROM Pais ORDER BY RANDOM() LIMIT 1")
    fun getRandomCountry(): Pais

    @Query("SELECT capital FROM Pais ORDER BY RANDOM() LIMIT 4")
    fun getFourRandomCapital(): List<String>

    @Query("SELECT * FROM Pais WHERE capital = :capital")
    fun getPaisByCapital(capital: String): Pais

}