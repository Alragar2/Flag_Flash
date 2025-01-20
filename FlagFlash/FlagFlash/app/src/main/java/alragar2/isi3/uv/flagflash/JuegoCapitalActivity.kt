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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class JuegoCapitalActivity : AppCompatActivity(){
    private var correctCountry = ""
    private var lives = 3
    private var correctGuesses = 0
    private lateinit var hearts: List<ImageView>
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var vibrator: Vibrator
    private lateinit var buttons: List<Button>
    private lateinit var databaseReference: DatabaseReference
    private lateinit var scoreTextView: TextView
    private lateinit var userScoreManager: UserScoreManager
    private var scoreReal = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.juego_capital)

        // Initialize Firestore
        val userPreferences = UserPreferences(this)
        userPreferences.getScore { score ->
            userPreferences.setInitialScore(score)
            scoreTextView = findViewById(R.id.puntuacion)
            scoreTextView.text = score.toString()
        }

        databaseReference = FirebaseDatabase.getInstance("https://flag-flash-tfg-default-rtdb.europe-west1.firebasedatabase.app/").reference

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        buttons = listOf(
            findViewById<Button>(R.id.Capital1),
            findViewById<Button>(R.id.Capital2),
            findViewById<Button>(R.id.Capital3),
            findViewById<Button>(R.id.Capital4)
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

        updateButtons()

        userScoreManager = UserScoreManager()

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
        userScoreManager.saveUserScore(userId, score, {
            // Navegar a la actividad de victoria o derrota
            val intent = if (lives > 0) {
                Intent(this, VictoriaIndividualActivity::class.java)
            } else {
                Intent(this, DerrotaIndividualActivity::class.java)
            }
            intent.putExtra("originActivity", "JuegoCapitalActivity")
            startActivity(intent)
            finish()
        }, {
            // Manejar el error al guardar los puntos
            Log.e("FirestoreError", "Error al guardar los puntos", it)
        })
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

    private val selectedCountries = mutableListOf<String>()

    private fun updateButtons() {
        databaseReference.child("paises").get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val capitalNames = mutableListOf<String>()
                val countryNames = mutableListOf<String>()
                for (countrySnapshot in dataSnapshot.children) {
                    val capitalName = countrySnapshot.child("capital").getValue(String::class.java)
                    val countryName = countrySnapshot.child("nombre").getValue(String::class.java)
                    capitalName?.let { capitalNames.add(it) }
                    countryName?.let { countryNames.add(it) }
                }

                val playCapitalNames = mutableListOf<String>()

                while (playCapitalNames.size < 4) {
                    val randomCapital = capitalNames.random()
                    if (randomCapital !in playCapitalNames && randomCapital !in selectedCountries) {
                        playCapitalNames.add(randomCapital)
                    }
                }

                if (playCapitalNames.size >= 4) {
                    correctCountry = playCapitalNames.random()
                    val paisSnapshot = dataSnapshot.children.first { it.child("capital").getValue(String::class.java) == correctCountry }
                    val banderaUrl = paisSnapshot.child("bandera").getValue(String::class.java)
                    val countryName = paisSnapshot.child("nombre").getValue(String::class.java)

                    updateUI {
                        val imageView = findViewById<ImageView>(R.id.bandera_capital)
                        Glide.with(this@JuegoCapitalActivity)
                            .load(banderaUrl)
                            .into(imageView)

                        val paisTextView = findViewById<TextView>(R.id.pais_capital)
                        paisTextView.text = countryName

                        val shuffledCapitalNames = playCapitalNames.shuffled()
                        for (i in buttons.indices) {
                            buttons[i].text = shuffledCapitalNames[i]
                            buttons[i].setBackgroundColor(Color.parseColor("#2E8DFF"))
                        }

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
