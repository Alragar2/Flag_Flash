package alragar2.isi3.uv.flagflash

import alragar2.isi3.uv.flagflash.BaseDatos.Pais
import alragar2.isi3.uv.flagflash.BaseDatos.PaisDatabase
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.room.Room
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.IOException

class ElegirMultijugarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.elegir_multijugar)



        val database = Room.databaseBuilder(
            applicationContext,
            PaisDatabase::class.java,
            "pais_database"
        )
            .fallbackToDestructiveMigration()
            .build()

        Log.d("ElegirJugarActivity", "Base de datos creada")

        val paisDao = database.paisDao()

        val json: String
        try {
            val inputStream = resources.openRawResource(R.raw.paises)
            json = inputStream.bufferedReader().use { it.readText() }
        } catch (ex: IOException) {
            ex.printStackTrace()
            return
        }

        Log.d("ElegirJugarActivity", "Base de datos creada2")
        // Convierte el JSON en una lista de objetos Pais
        val gson = Gson()
        val listType = object : TypeToken<List<Pais>>() {}.type
        val paises: List<Pais> = gson.fromJson(json, listType)

        // Inserta los objetos Pais en la base de datos
        Thread {

            paisDao.deleteAll()

            paisDao.insertAll(*paises.toTypedArray())

        }.start()

        val jugarButton = findViewById<Button>(R.id.multiBandera)
        jugarButton.setOnClickListener {
            val intent = Intent(this, MultijugadorBanderaActivity::class.java)
            startActivity(intent)
        }

        val jugarButton2 = findViewById<Button>(R.id.multiPais)
        jugarButton2.setOnClickListener {
            val intent = Intent(this, MultijugadorPaisActivity::class.java)
            startActivity(intent)
        }

        val jugarButton3 = findViewById<Button>(R.id.multiCapital)
        jugarButton3.setOnClickListener {
            val intent = Intent(this, MultijugadorCapitalActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Iniciar el servicio de música
        val musicIntent = Intent(this, MusicService::class.java)
        startService(musicIntent)
    }

    // No detener el servicio de música en onPause
    override fun onPause() {
        super.onPause()
    }

}