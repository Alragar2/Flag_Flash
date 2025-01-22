package alragar2.isi3.uv.flagflash

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

        val gameModes = listOf("Bandera", "País", "Escudos/Emblemas nacionales", "Capitales") // Añade aquí tus modos de juego
        val gameModesRecyclerView = findViewById<RecyclerView>(R.id.gameModesRecyclerView)
        gameModesRecyclerView.layoutManager = LinearLayoutManager(this)
        gameModesRecyclerView.adapter = GameModeAdapter(gameModes) { gameMode ->
            // Aquí manejas el clic en un modo de juego
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
            // Aquí manejas el clic en un filtro de continente
            // Puedes usar el filtro para mostrar solo los modos de juego de ese continente
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