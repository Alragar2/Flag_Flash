package alragar2.isi3.uv.flagflash

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class VictoriaMJActivity2 : AppCompatActivity() {
    private lateinit var winnerImage: ImageView
    private lateinit var playAgainButton: Button
    private lateinit var backToMainButton: Button
    private var player1Score: Int = 0
    private var player2Score: Int = 0
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_victoria_mj)

        mediaPlayer = MediaPlayer.create(this, R.raw.win)
        mediaPlayer.start()

        winnerImage = findViewById(R.id.winnerImage)
        playAgainButton = findViewById(R.id.playAgainButton)
        backToMainButton = findViewById(R.id.backToMainButton)

        // Obtener las puntuaciones de los jugadores desde la actividad anterior
        val bundle = intent.extras
        player1Score = bundle?.getInt("player1Score") ?: 0
        player2Score = bundle?.getInt("player2Score") ?: 0

        // Determinar al jugador ganador
        val winnerPlayer = getWinnerPlayer()
        val winnerImageResource = when (winnerPlayer) {
            1 -> R.drawable.jugador1
            2 -> R.drawable.jugador2
            else -> R.drawable.jugador1 // Valor predeterminado si no se puede determinar el ganador
        }
        winnerImage.setImageResource(winnerImageResource)

        playAgainButton.setOnClickListener {
            // Volver a iniciar la actividad del juego multijugador
            finish()
            startActivity(Intent(this, MultijugadorPaisActivity::class.java))
        }

        backToMainButton.setOnClickListener {
            // Volver a iniciar la actividad del juego multijugador
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }


    private fun getWinnerPlayer(): Int {
        return when {
            player1Score > player2Score -> 1
            player2Score > player1Score -> 2
            else -> 0 // Empate
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
