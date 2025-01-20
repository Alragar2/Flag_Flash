package alragar2.isi3.uv.flagflash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ElegirJugarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.elegir_jugar)

        val gameModes = listOf("Bandera", "País", "Escudos/Emblemas nacionales", "Capitales") // Añade aquí tus modos de juego
        val gameModesRecyclerView = findViewById<RecyclerView>(R.id.gameModesRecyclerView)
        gameModesRecyclerView.layoutManager = LinearLayoutManager(this)
        gameModesRecyclerView.adapter = GameModeAdapter(gameModes) { gameMode ->
            // Aquí manejas el clic en un modo de juego
            when (gameMode) {
                "Bandera" -> {
                    val intent = Intent(this, JuegoBanderaActivity::class.java)
                    startActivity(intent)
                }
                "País" -> {
                    val intent = Intent(this, JuegoPaisActivity::class.java)
                    startActivity(intent)
                }
                "Escudos/Emblemas nacionales" -> {
                    val intent = Intent(this, JuegoEscudoActivity::class.java)
                    startActivity(intent)
                }
                "Capitales" -> {
                    val intent = Intent(this, JuegoCapitalActivity::class.java)
                    startActivity(intent)
                }
            }
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