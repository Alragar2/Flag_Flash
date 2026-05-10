package alragar2.isi3.uv.flagflash.juego

import alragar2.isi3.uv.flagflash.InterstitialAdManager
import alragar2.isi3.uv.flagflash.resultado.DerrotaIndividualActivity
import alragar2.isi3.uv.flagflash.musica.MusicService
import alragar2.isi3.uv.flagflash.R
import alragar2.isi3.uv.flagflash.UserPreferences
import alragar2.isi3.uv.flagflash.UserScoreManager
import alragar2.isi3.uv.flagflash.resultado.VictoriaIndividualActivity
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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
    private var selectedContinent: String? = null

    // Mascotas
    private lateinit var userPreferences: UserPreferences
    private var activePet: String? = null
    private var isPetFed = false
    private lateinit var ivPetActive: ImageView
    private lateinit var btnOwlAbility: ImageButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.juego_capital)

        InterstitialAdManager.showAdWithProbability(this, 0.4f)

        selectedContinent = intent.getStringExtra("selectedContinent")
        userPreferences = UserPreferences(this)
        ivPetActive = findViewById(R.id.ivPetActive)
        btnOwlAbility = findViewById(R.id.btnOwlAbility)

        // Cargar Mascota
        userPreferences.getSelectedPet { pet ->
            activePet = pet
            if (pet != null) {
                userPreferences.isPetFed(pet) { fed ->
                    isPetFed = fed
                    updatePetUI()
                }
            } else {
                updatePetUI()
            }
        }

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
                val btn = view as Button
                setButtonsEnabled(false)
                if (btn.text == correctCountry) {
                    playCorrectAnswerSound()
                    updateUI {
                        btn.setBackgroundColor(Color.GREEN)
                        progressBar.progress = ++correctGuesses
                        progressText.text = "$correctGuesses/10"
                        scoreReal += 10
                        updateScore(userPreferences, 10) {
                            if (correctGuesses == 10) {
                                userPreferences.getScore { score ->
                                    saveUserAndFinish(score)
                                }
                            } else {
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
                    
                    // Habilidad Tortuga (Escudo)
                    var penalty = -5
                    if (activePet == "tortuga" && isPetFed) {
                        penalty = 0
                        isPetFed = false
                        userPreferences.setPetFed("tortuga", false)
                        updatePetUI()
                        Toast.makeText(this, "¡Escudo de Tortuga activado!", Toast.LENGTH_SHORT).show()
                    }

                    lives--
                    
                    // Habilidad Gato (Vida Extra)
                    if (lives == 0 && activePet == "gato" && isPetFed) {
                        lives = 1
                        isPetFed = false
                        userPreferences.setPetFed("gato", false)
                        updatePetUI()
                        Toast.makeText(this, "¡Gato te dio una vida extra!", Toast.LENGTH_SHORT).show()
                    }

                    scoreReal += penalty
                    updateScore(userPreferences, penalty) {
                        if (lives == 0) {
                            userPreferences.getScore { score ->
                                saveUserAndFinish(score)
                            }
                        }
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (lives > 0) setButtonsEnabled(true)
                    }, 1200)
                    updateUI {
                        btn.setBackgroundColor(Color.RED)
                        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
                        if (lives >= 0 && hearts.size > lives) {
                            hearts[lives].startAnimation(fadeOut)
                            hearts[lives].visibility = View.GONE
                        }
                        vibrateDevice()
                    }
                }
            }
        }

        // Habilidad Búho (Pista)
        btnOwlAbility.setOnClickListener {
            if (activePet == "buho" && isPetFed) {
                useOwlAbility()
            }
        }
    }

    private fun updatePetUI() {
        runOnUiThread {
            if (activePet != null) {
                ivPetActive.visibility = View.VISIBLE
                val iconRes = when(activePet) {
                    "buho" -> R.drawable.buho
                    "gato" -> R.drawable.gatito
                    "tortuga" -> R.drawable.tortuguita
                    else -> 0
                }
                if (iconRes != 0) ivPetActive.setImageResource(iconRes)
                ivPetActive.alpha = if (isPetFed) 1.0f else 0.4f

                // Botón de habilidad búho
                if (activePet == "buho" && isPetFed) {
                    btnOwlAbility.visibility = View.VISIBLE
                } else {
                    btnOwlAbility.visibility = View.GONE
                }
            } else {
                ivPetActive.visibility = View.GONE
                btnOwlAbility.visibility = View.GONE
            }
        }
    }

    private fun useOwlAbility() {
        var removed = 0
        val shuffledButtons = buttons.shuffled()
        for (btn in shuffledButtons) {
            if (btn.text != correctCountry && removed < 2) {
                btn.visibility = View.INVISIBLE
                removed++
            }
        }
        isPetFed = false
        userPreferences.setPetFed("buho", false)
        updatePetUI()
        Toast.makeText(this, "¡Búho ha eliminado dos opciones!", Toast.LENGTH_SHORT).show()
    }

    private fun updateScore(userPreferences: UserPreferences, increment: Int, onComplete: () -> Unit) {
        userPreferences.getScore { score ->
            val newScore = score + increment
            userPreferences.setScore(newScore)
            scoreTextView.text = newScore.toString()
            onComplete()
        }
    }

    private fun saveUserAndFinish(score: Int){
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        userScoreManager.saveUserScore(userId, score, {
            val intent = if (lives > 0) {
                Intent(this, VictoriaIndividualActivity::class.java)
            } else {
                Intent(this, DerrotaIndividualActivity::class.java)
            }
            intent.putExtra("originActivity", "JuegoCapitalActivity")
            intent.putExtra("selectedContinent", selectedContinent)
            startActivity(intent)
            finish()
        }, {
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
                    val continent = countrySnapshot.child("continente").getValue(String::class.java)
                    if (countryName != null && (continent == selectedContinent || selectedContinent == "Todos")) {
                        capitalName?.let { capitalNames.add(it) }
                        countryName?.let { countryNames.add(it) }
                    }
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
                        if (!isDestroyed) {
                            Glide.with(this@JuegoCapitalActivity)
                                .load(banderaUrl)
                                .into(imageView)
                        }

                        val paisTextView = findViewById<TextView>(R.id.pais_capital)
                        paisTextView.text = countryName

                        val shuffledCapitalNames = playCapitalNames.shuffled()
                        for (i in buttons.indices) {
                            buttons[i].text = shuffledCapitalNames[i]
                            buttons[i].setBackgroundColor(Color.parseColor("#2E8DFF"))
                            buttons[i].visibility = View.VISIBLE
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
        val musicIntent = Intent(this, MusicService::class.java)
        startService(musicIntent)
    }

    override fun onPause() {
        super.onPause()
    }
}
