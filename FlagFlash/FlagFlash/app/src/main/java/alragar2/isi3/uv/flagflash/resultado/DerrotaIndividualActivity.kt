package alragar2.isi3.uv.flagflash.resultado

import alragar2.isi3.uv.flagflash.MainActivity
import alragar2.isi3.uv.flagflash.musica.MusicService
import alragar2.isi3.uv.flagflash.R
import alragar2.isi3.uv.flagflash.UserPreferences
import alragar2.isi3.uv.flagflash.juego.compose.GameActivity
import alragar2.isi3.uv.flagflash.juego.compose.GameMode
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DerrotaIndividualActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private var selectedContinent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_derrota_ind)

        selectedContinent = intent.getStringExtra("selectedContinent")

        mediaPlayer = MediaPlayer.create(this, R.raw.game_over)
        mediaPlayer.start()

        val userPreferences = UserPreferences(this)
        userPreferences.getScore { finalScore ->
            userPreferences.getInitialScore().let { initialScore ->
                val score = finalScore - initialScore
                val scoreTextView: TextView = findViewById(R.id.puntos)
                scoreTextView.text = score.toString()
            }
        }

        val backToMainButton = findViewById<Button>(R.id.backToMainButton)
        val playAgainButton = findViewById<Button>(R.id.playAgainButton)

        val originActivity = intent.getStringExtra("originActivity") ?: "MainActivity"

        backToMainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        playAgainButton.setOnClickListener {
            val mode = when (originActivity) {
                "JuegoBanderaActivity" -> GameMode.BANDERA
                "JuegoPaisActivity" -> GameMode.PAIS
                "JuegoCapitalActivity" -> GameMode.CAPITAL
                "JuegoEscudoActivity" -> GameMode.ESCUDO
                else -> null
            }

            if (mode != null) {
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("gameMode", mode.name)
                intent.putExtra("selectedContinent", selectedContinent)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
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
