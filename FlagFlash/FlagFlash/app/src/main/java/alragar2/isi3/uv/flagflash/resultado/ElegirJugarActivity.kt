package alragar2.isi3.uv.flagflash.resultado

import alragar2.isi3.uv.flagflash.musica.MusicService
import alragar2.isi3.uv.flagflash.R
import alragar2.isi3.uv.flagflash.galeria.GameModeAdapter
import alragar2.isi3.uv.flagflash.juego.JuegoBanderaActivity
import alragar2.isi3.uv.flagflash.juego.JuegoCapitalActivity
import alragar2.isi3.uv.flagflash.juego.JuegoEscudoActivity
import alragar2.isi3.uv.flagflash.juego.JuegoPaisActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ElegirJugarActivity : AppCompatActivity() {

    private var selectedcontinent: String = "Todos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.elegir_jugar)

        val gameModes = listOf("Bandera", "País", "Escudos/Emblemas nacionales", "Capitales")
        val gameModesRecyclerView = findViewById<RecyclerView>(R.id.gameModesRecyclerView)
        gameModesRecyclerView.layoutManager = LinearLayoutManager(this)
        gameModesRecyclerView.adapter = GameModeAdapter(gameModes) { gameMode ->
            val activityMap = mapOf(
                "Bandera" to JuegoBanderaActivity::class.java,
                "País" to JuegoPaisActivity::class.java,
                "Escudos/Emblemas nacionales" to JuegoEscudoActivity::class.java,
                "Capitales" to JuegoCapitalActivity::class.java
            )

            activityMap[gameMode]?.let { activityClass ->
                val intent = Intent(this, activityClass)
                intent.putExtra("selectedContinent", selectedcontinent)
                startActivity(intent)
            }
        }

        val continentFilter = listOf("Todos", "África", "América", "Asia", "Europa", "Oceanía")
        val continentFilterRecyclerView = findViewById<RecyclerView>(R.id.continentFilterRecyclerView)
        continentFilterRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        continentFilterRecyclerView.adapter = ContinentFilterAdapter(continentFilter) { filter ->
            selectedcontinent = filter
        }
    }

    // Iniciar el servicio de música en onResume
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