package alragar2.isi3.uv.flagflash

import alragar2.isi3.uv.flagflash.BaseDatos.PaisDatabase
import alragar2.isi3.uv.flagflash.BaseDatos.PaisDao
import android.annotation.SuppressLint
import android.content.Intent
import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MultijugadorCapitalActivity : AppCompatActivity() {
    private var correctCountry = ""
    private var player1Score = 0
    private var player2Score = 0
    private var correctGuesses = 0
    private lateinit var progressBar: ProgressBar
    private lateinit var progressBar2: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var progressText2: TextView
    private lateinit var buttons: List<Button>
    private lateinit var paisDao: PaisDao
    private lateinit var vibrator: Vibrator
    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.multijugador_capital)

        databaseReference = FirebaseDatabase.getInstance("https://flag-flash-tfg-default-rtdb.europe-west1.firebasedatabase.app/").reference
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            buttons = listOf(
                findViewById(R.id.Pais1),
                findViewById(R.id.Pais2),
                findViewById(R.id.Pais3),
                findViewById(R.id.Pais4),
                findViewById(R.id.Pais11),
                findViewById(R.id.Pais22),
                findViewById(R.id.Pais33),
                findViewById(R.id.Pais44)
            )

            progressBar = findViewById(R.id.progressBar)
            progressBar2 = findViewById(R.id.progressBar2)
            progressBar.progress = 0
            progressBar2.progress = 0
            progressText = findViewById(R.id.tvProgress)
            progressText2 = findViewById(R.id.tvProgress2)
            progressText.text = "0/15"
            progressText2.text = "0/15"
            val player1ScoreView = findViewById<TextView>(R.id.puntuacion1)
            val player2ScoreView = findViewById<TextView>(R.id.puntuacion2)

            updateButtons()

            for (button in buttons) {
                button.setOnClickListener { view ->
                    val button = view as Button
                    CoroutineScope(Dispatchers.IO).launch {
                        if (button.text == correctCountry) {
                            playCorrectAnswerSound()
                            withContext(Dispatchers.Main) {
                                setButtonsEnabled(false)
                            }
                            // Incrementar la puntuación del jugador correspondiente
                            if (button.id in setOf(R.id.Pais1, R.id.Pais2, R.id.Pais3, R.id.Pais4)) {
                                player1Score++
                                updateUI {
                                    player1ScoreView.text = player1Score.toString()
                                }
                            } else {
                                player2Score++
                                updateUI {
                                    player2ScoreView.text = player2Score.toString()
                                }
                            }

                            // Actualizar ambas barras de progreso
                            updateUI {
                                button.setBackgroundColor(Color.GREEN)
                                correctGuesses++
                                progressBar.progress = correctGuesses
                                progressBar2.progress = correctGuesses
                                progressText.text = "$correctGuesses/15"
                                progressText2.text = "$correctGuesses/15"
                            }

                            if (correctGuesses == 14) {
                                // Pasar las puntuaciones de los jugadores a VictoriaMJActivity
                                updateUI {
                                    val intent = Intent(this@MultijugadorCapitalActivity, VictoriaMJActivity::class.java)
                                    intent.putExtra("player1Score", player1Score)
                                    intent.putExtra("player2Score", player2Score)
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                // Generate new buttons after a delay
                                Handler(Looper.getMainLooper()).postDelayed({
                                    updateButtons()
                                    resetButtonColors()
                                    runOnUiThread {
                                        setButtonsEnabled(true)
                                    }
                                }, 1200)
                            }
                        } else {
                            updateUI {
                                button.setBackgroundColor(Color.RED)
                                vibrateDevice()
                                resetButtonColors()
                            }
                        }
                    }
                }
            }
    }

    private fun playCorrectAnswerSound() {
        // Música de victoria cuando se acierta la respuesta
        val mediaPlayer = MediaPlayer.create(this, R.raw.correct_answer)
        mediaPlayer.start()
    }

    private val selectedCountries = mutableListOf<String>()

    private fun updateButtons() {

        databaseReference.child("paises").get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val countryCapitalNames = mutableListOf<String>()
                for (countrySnapshot in dataSnapshot.children) {
                    val capital = countrySnapshot.child("capital").value.toString()
                    countryCapitalNames.add(capital)
                }

                if (countryCapitalNames.size < 4) {
                    return@addOnSuccessListener
                }

                val playCapitalNames = mutableListOf<String>()

                while (playCapitalNames.size < 4) {
                    val randomCapital = countryCapitalNames.random()
                    if (randomCapital !in playCapitalNames && randomCapital != correctCountry) {
                        playCapitalNames.add(randomCapital)
                    }
                }

                correctCountry = playCapitalNames.random()
                val capitalSnapshot = dataSnapshot.children.find { it.child("capital").value == correctCountry }
                val countryName = capitalSnapshot?.child("nombre")?.value.toString()
                val banderaURL = capitalSnapshot?.child("bandera")?.value.toString()

                updateUI {
                    val imageView = findViewById<ImageView>(R.id.bandera)
                    val imageView2 = findViewById<ImageView>(R.id.bandera2)
                    Glide.with(this)
                        .load(banderaURL)
                        .into(imageView)
                    Glide.with(this)
                        .load(banderaURL)
                        .into(imageView2)
                    val textView = findViewById<TextView>(R.id.nombre_pais1)
                    val textView2 = findViewById<TextView>(R.id.nombre_pais2)
                    textView.text = countryName
                    textView2.text = countryName

                    val shuffledCapitalNames = playCapitalNames.shuffled()
                    for (i in buttons.indices) {
                        buttons[i].text = shuffledCapitalNames[i % shuffledCapitalNames.size]
                    }

                    selectedCountries.add(correctCountry)
                }
            }
        }
    }


    private fun vibrateDevice() {
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= 26) {
                val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        }
    }

    private fun resetButtonColors() {
        for (button in buttons) {
            button.setBackgroundColor(Color.parseColor("#2E8DFF"))
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        for (button in buttons) {
            button.isEnabled = enabled
        }
    }

    private inline fun updateUI(crossinline block: () -> Unit) {
        runOnUiThread {
            block()
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
