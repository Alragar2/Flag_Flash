package alragar2.isi3.uv.flagflash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class JuegoBanderaActivity : AppCompatActivity() {
    private var correctCountry = ""
    private var lives = 3
    private var correctGuesses = 0
    private lateinit var hearts: List<ImageView>
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var vibrator: Vibrator
    private lateinit var buttons: List<Button>
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userScoreManager: UserScoreManager
    private lateinit var scoreTextView: TextView
    private var scoreReal = 0
    private val selectedCountries = mutableListOf<String>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.juego_bandera)

        val userPreferences = UserPreferences(this)
        userPreferences.getScore { score ->
            userPreferences.setInitialScore(score)
            scoreTextView = findViewById(R.id.puntuacion)
            scoreTextView.text = score.toString()
        }

        databaseReference = FirebaseDatabase.getInstance("https://flag-flash-tfg-default-rtdb.europe-west1.firebasedatabase.app/").reference

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        buttons = listOf(
            findViewById<Button>(R.id.Pais1),
            findViewById<Button>(R.id.Pais2),
            findViewById<Button>(R.id.Pais3),
            findViewById<Button>(R.id.Pais4)
        )

        hearts = listOf(
            findViewById(R.id.corazon1),
            findViewById(R.id.corazon2),
            findViewById(R.id.corazon3)
        )

        progressBar = findViewById(R.id.progressBar)
        progressBar.progress = 0
        progressText = findViewById(R.id.tvProgress)
        progressText.text = "0/10"

        preloadImages()

        updateButtons()

        userScoreManager = UserScoreManager()

        updateInterface(userPreferences)

    }

    private fun updateInterface(userPreferences: UserPreferences) {
        for (button in buttons) {
            button.setOnClickListener { view ->
                val button = view as Button
                setButtonsEnabled(false)
                if (button.text == correctCountry) {
                    playCorrectAnswerSound()
                    updateUI {
                        button.setBackgroundColor(Color.GREEN)
                        progressBar.progress = ++correctGuesses
                        progressText.text = "$correctGuesses/10"
                        scoreReal += 10
                        updateScore(userPreferences, 10) {
                            if (correctGuesses == 10) {
                                // Finalizar el juego y navegar a la actividad de victoria y guardar los puntos
                                userPreferences.getScore { score ->
                                    saveUserAndFinish(score)
                                }
                            } else {
                                // Generar nuevos botones después de un retraso
                                Handler(Looper.getMainLooper()).postDelayed({
                                    updateButtons()
                                    resetButtonColors(buttons)
                                    setButtonsEnabled(true)
                                }, 1200)
                            }
                        }
                    }
                } else {
                    playwrongAnswer()
                    lives--
                    scoreReal -= 5
                    updateScore(userPreferences, -5) {
                        if (lives == 0) {
                            // Finalizar el juego y navegar a la actividad de derrota y guardar los puntos
                            userPreferences.getScore { score ->
                                saveUserAndFinish(score)
                            }
                        }
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        setButtonsEnabled(true)
                    }, 1200)
                    updateUI {
                        button.setBackgroundColor(Color.RED)
                        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
                        hearts[lives].startAnimation(fadeOut)
                        hearts[lives].visibility = View.GONE
                        vibrateDevice()
                    }
                }
            }
        }
    }

    // Actualiza el puntaje en la interfaz de usuario y en la base de datos
    private fun updateScore(userPreferences: UserPreferences, increment: Int, onComplete: () -> Unit) {
        userPreferences.getScore { score ->
            val newScore = score + increment
            userPreferences.setScore(newScore)
            scoreTextView.text = newScore.toString()
            Log.d("Score", "Puntuación: $newScore")
            onComplete()
        }
    }

    private fun saveUserAndFinish(score: Int){
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d("ScoreR", "Puntuación real: $scoreReal")
        Log.d("Score", "Puntuación: $score")
        userScoreManager.saveUserScore(userId, score, {
            // Navegar a la actividad de victoria o derrota
            val intent = if (lives > 0) {
                Intent(this, VictoriaIndividualActivity::class.java)
            } else {
                Intent(this, DerrotaIndividualActivity::class.java)
            }
            intent.putExtra("originActivity", "JuegoBanderaActivity")
            startActivity(intent)
            finish()
        }, {
            // Manejar el error al guardar los puntos
            Log.e("FirestoreError", "Error al guardar los puntos", it)
        })
    }

    private fun preloadImages() {
        val countryFlagsURL = mutableListOf<String>()
        databaseReference.child("paises").get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                for (countrySnapshot in dataSnapshot.children) {
                    val banderaUrl = countrySnapshot.child("bandera").getValue(String::class.java)
                    banderaUrl?.let { countryFlagsURL.add(it) }
                }

                CoroutineScope(Dispatchers.IO).launch {
                    for (url in countryFlagsURL) {
                        try {
                            val futureTarget = Glide.with(this@JuegoBanderaActivity)
                                .load(url)
                                .submit()
                            futureTarget.get() // Bloquea hasta que la imagen se cargue
                        } catch (e: Exception) {
                            Log.e("GlideError", "Error al cargar la imagen desde la URL: $url", e)
                        }
                    }
                }
            }
        }.addOnFailureListener { e ->
            Log.e("FirebaseError", "Error al obtener datos", e)
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        for (button in buttons) {
            button.isEnabled = enabled
        }
    }

    private fun playCorrectAnswerSound() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.correct_answer)
        mediaPlayer.start()
    }

    private fun playwrongAnswer() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.wrong_answer)
        mediaPlayer.start()
    }

    // Actualiza los botones con nuevos países
    private fun updateButtons() {
        databaseReference.child("paises").get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val countryNames = mutableListOf<String>()
                for (countrySnapshot in dataSnapshot.children) {
                    val countryName = countrySnapshot.child("nombre").getValue(String::class.java)
                    countryName?.let { countryNames.add(it) }
                }

                val playCountryNames = mutableListOf<String>()

                while (playCountryNames.size < 4) {
                    val randomCountry = countryNames.random()
                    if (randomCountry !in playCountryNames && randomCountry !in selectedCountries) {
                        playCountryNames.add(randomCountry)
                    }
                }

                Log.d("FirebaseData", "Países seleccionados: $playCountryNames")

                if (playCountryNames.size >= 4) {
                    correctCountry = playCountryNames.random()
                    val paisSnapshot = dataSnapshot.children.first { it.child("nombre").getValue(String::class.java) == correctCountry }
                    val banderaUrl = paisSnapshot.child("bandera").getValue(String::class.java)

                    Log.d("FirebaseData", "País correcto: $correctCountry")
                    Log.d("FirebaseData", "URL Bandera: $banderaUrl")

                    // Actualizar la interfaz de usuario en el subproceso principal
                    updateUI {
                        val imageView = findViewById<ImageView>(R.id.bandera)
                        Glide.with(this)
                            .load(banderaUrl)
                            .into(imageView)

                        val shuffledCountryNames = playCountryNames.shuffled()
                        for (i in buttons.indices) {
                            buttons[i].text = shuffledCountryNames[i]
                            buttons[i].setBackgroundColor(Color.parseColor("#2E8DFF"))
                        }

                        // Agregar el país seleccionado a la lista de países seleccionados
                        selectedCountries.add(correctCountry)
                    }
                }
            }
        }.addOnFailureListener { e ->
            Log.e("FirebaseError", "Error al obtener datos", e)
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

    private fun resetButtonColors(buttons: List<Button>) {
        for (button in buttons) {
            button.setBackgroundColor(Color.parseColor("#2E8DFF"))
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
