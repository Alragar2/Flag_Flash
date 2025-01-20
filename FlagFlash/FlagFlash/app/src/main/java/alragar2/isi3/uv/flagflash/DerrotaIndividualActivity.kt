package alragar2.isi3.uv.flagflash

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DerrotaIndividualActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_derrota_ind)

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
            intent?.let {
                startActivity(it)
                finish()
            }
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

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }
}