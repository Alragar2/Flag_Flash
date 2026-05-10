package alragar2.isi3.uv.flagflash.resultado

import alragar2.isi3.uv.flagflash.musica.MusicService
import alragar2.isi3.uv.flagflash.R
import alragar2.isi3.uv.flagflash.galeria.GameModeAdapter
import alragar2.isi3.uv.flagflash.juego.compose.GameActivity
import alragar2.isi3.uv.flagflash.juego.compose.GameMode
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
            val modeEnum = when (gameMode) {
                "Bandera" -> GameMode.BANDERA
                "País" -> GameMode.PAIS
                "Escudos/Emblemas nacionales" -> GameMode.ESCUDO
                "Capitales" -> GameMode.CAPITAL
                else -> GameMode.BANDERA
            }

            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("gameMode", modeEnum.name)
            intent.putExtra("selectedContinent", selectedcontinent)
            startActivity(intent)
        }

        val continentFilter = listOf("Todos", "África", "América", "Asia", "Europa", "Oceanía")
        val continentFilterRecyclerView = findViewById<RecyclerView>(R.id.continentFilterRecyclerView)
        continentFilterRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        continentFilterRecyclerView.adapter = ContinentFilterAdapter(continentFilter) { filter ->
            selectedcontinent = filter
        }
    }

    override fun onResume() {
        super.onResume()
        val musicIntent = Intent(this, MusicService::class.java)
        startService(musicIntent)
    }

    override fun onPause() {
        super.onPause()
    }
}
