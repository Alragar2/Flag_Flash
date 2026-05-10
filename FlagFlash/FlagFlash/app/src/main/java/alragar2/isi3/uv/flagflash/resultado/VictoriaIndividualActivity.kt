package alragar2.isi3.uv.flagflash.resultado

import alragar2.isi3.uv.flagflash.MainActivity
import alragar2.isi3.uv.flagflash.musica.MusicService
import alragar2.isi3.uv.flagflash.R
import alragar2.isi3.uv.flagflash.UserPreferences
import alragar2.isi3.uv.flagflash.juego.JuegoBanderaActivity
import alragar2.isi3.uv.flagflash.juego.JuegoCapitalActivity
import alragar2.isi3.uv.flagflash.juego.JuegoEscudoActivity
import alragar2.isi3.uv.flagflash.juego.JuegoPaisActivity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class VictoriaIndividualActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private var selectedContinent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_victoria_ind)

        selectedContinent = intent.getStringExtra("selectedContinent")
        val timeElapsed = intent.getLongExtra("timeElapsed", 0)
        val mistakes = intent.getIntExtra("mistakes", 0)
        val originActivity = intent.getStringExtra("originActivity") ?: "MainActivity"

        mediaPlayer = MediaPlayer.create(this, R.raw.win)
        mediaPlayer.start()

        val userPreferences = UserPreferences(this)
        
        // 1. Mostrar Puntuación Total Ganada
        userPreferences.getScore { finalScore ->
            userPreferences.getInitialScore().let { initialScore ->
                val score = finalScore - initialScore
                findViewById<TextView>(R.id.puntos).text = score.toString()
            }
        }

        // 2. Formatear y Mostrar Tiempo
        val minutes = timeElapsed / 60
        val seconds = timeElapsed % 60
        findViewById<TextView>(R.id.tvTiempo).text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        // 3. Calcular y Mostrar Precisión
        val precision = if (10 + mistakes > 0) {
            (10.0 / (10.0 + mistakes) * 100).toInt()
        } else 100
        findViewById<TextView>(R.id.tvPrecision).text = "$precision%"

        // 4. Gestionar Monedas
        val coinsGained = 10
        val tvXP = findViewById<TextView>(R.id.experiencia)
        tvXP.text = "Monedas Ganadas: +$coinsGained"
        
        userPreferences.getCoins { currentCoins ->
            userPreferences.setCoins(currentCoins + coinsGained)
        }

        // 5. Barra de "Progreso/XP"
        val progressBar = findViewById<ProgressBar>(R.id.xpProgressBar)
        progressBar.progress = precision 

        val backToMainButton = findViewById<Button>(R.id.backToMainButton)
        val playAgainButton = findViewById<Button>(R.id.playAgainButton)

        backToMainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        playAgainButton.setOnClickListener {
            val intent = when (originActivity) {
                "JuegoBanderaActivity" -> Intent(this, JuegoBanderaActivity::class.java)
                "JuegoPaisActivity" -> Intent(this, JuegoPaisActivity::class.java)
                "JuegoCapitalActivity" -> Intent(this, JuegoCapitalActivity::class.java)
                "JuegoEscudoActivity" -> Intent(this, JuegoEscudoActivity::class.java)
                else -> Intent (this, MainActivity::class.java)
            }
            intent.putExtra("selectedContinent", selectedContinent)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val musicIntent = Intent(this, MusicService::class.java)
        startService(musicIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }
}