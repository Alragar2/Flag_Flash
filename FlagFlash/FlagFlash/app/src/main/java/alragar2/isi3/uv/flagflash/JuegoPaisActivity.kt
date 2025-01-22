package alragar2.isi3.uv.flagflash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth

class JuegoPaisActivity: AppCompatActivity() {
    private lateinit var bandera1: ImageView
    private lateinit var bandera2: ImageView
    private lateinit var bandera3: ImageView
    private lateinit var bandera4: ImageView
    private lateinit var paisTextView: TextView
    private var correctCountry = ""
    private var correctGesses = 0
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private var lives = 3
    private lateinit var hearts: List<ImageView>
    private val selectedCountries = mutableSetOf<String>()
    private lateinit var tickImageView: ImageView
    private lateinit var banderas: Array<ImageView>
    private lateinit var databaseReference: DatabaseReference
    private lateinit var scoreTextView: TextView
    private lateinit var userScoreManager: UserScoreManager
    private var scoreReal = 0
    private var selectedContinent: String? = null


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.juego_pais)

        selectedContinent = intent.getStringExtra("selectedContinent")

        tickImageView = findViewById(R.id.tick)

        bandera1 = findViewById(R.id.bandera)
        bandera2 = findViewById(R.id.bandera2)
        bandera3 = findViewById(R.id.bandera3)
        bandera4 = findViewById(R.id.bandera4)
        paisTextView = findViewById(R.id.pais)

        // Initialize Firestore
        val userPreferences = UserPreferences(this)
        userPreferences.getScore { score ->
            userPreferences.setInitialScore(score)
            scoreTextView = findViewById(R.id.puntuacion)
            scoreTextView.text = score.toString()
        }

        databaseReference = FirebaseDatabase.getInstance("https://flag-flash-tfg-default-rtdb.europe-west1.firebasedatabase.app/").reference

        preloadImages()
        banderas = arrayOf(bandera1, bandera2, bandera3, bandera4)

        progressBar = findViewById(R.id.progressBar)
        progressBar.progress = 0
        progressText = findViewById(R.id.tvProgress)
        progressText.text = "0/10"

        hearts = listOf(
            findViewById(R.id.corazon1),
            findViewById(R.id.corazon2),
            findViewById(R.id.corazon3)
        )

        fun playCorrectAnswerSound() {
            val mediaPlayer = MediaPlayer
                .create(this, R.raw.correct_answer)
            mediaPlayer.start()
        }

        fun playWrongAnswerSound() {
            val mediaPlayer = MediaPlayer
                .create(this, R.raw.wrong_answer)
            mediaPlayer.start()
        }

        updateImages()

        userScoreManager = UserScoreManager()

        @RequiresApi(Build.VERSION_CODES.O)
        fun vibrateDevice(context: Context) {
            val vibrator = context
                .getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= 26) {
                    val vibrationEffect = VibrationEffect
                        .createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(vibrationEffect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(500)
                }
            }
        }


        for (bandera in banderas) {
            bandera.setOnClickListener {
                setImageViewsEnabled(false)
                if (bandera.tag == correctCountry) {
                    playCorrectAnswerSound()
                    tickImageView.setImageResource(R.drawable.tick_verde)
                    progressBar.progress = ++correctGesses
                    progressText.text = "$correctGesses/10"
                    scoreReal += 10
                    updateScore(userPreferences, 10) {
                        if (correctGesses == 10) {
                            // Guardar el puntaje del usuario y navegar a la actividad de victoria
                            userPreferences.getScore { score ->
                                saveUserAndFinish(score)
                            }
                        }
                        else {
                            Handler(Looper.getMainLooper()).postDelayed({
                                updateImages()
                            }, 1000)
                        }
                    }
                } else {
                    lives--
                    scoreReal -= 5
                    updateScore(userPreferences, -5) {
                        if (lives == 0) {
                            userPreferences.getScore { score ->
                                saveUserAndFinish(score)
                            }
                        }
                    }
                    playWrongAnswerSound()
                    userPreferences.getScore { score ->
                        val newScore = score - 5
                        userPreferences.setScore(newScore)
                        scoreTextView.text = newScore.toString()
                    }
                    val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
                    hearts[lives].startAnimation(fadeOut)
                    hearts[lives].visibility = View.GONE
                    tickImageView.setImageResource(R.drawable.tick_rojo)
                    vibrateDevice(this)
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    tickImageView.setImageDrawable(null)
                    setImageViewsEnabled(true)
                }, 1000)
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
            intent.putExtra("originActivity", "JuegoPaisActivity")
            intent.putExtra("selectedContinent", selectedContinent)
            startActivity(intent)
            finish()
        }, {
            // Manejar el error al guardar los puntos
            Log.e("FirestoreError", "Error al guardar los puntos", it)
        })
    }

    private fun setImageViewsEnabled(enabled: Boolean) {
        for (imageView in banderas) {
            imageView.isEnabled = enabled
        }
    }

    private fun preloadImages() {
        val countryFlagsURL = mutableListOf<String>()
        databaseReference.child("paises").get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                for (countrySnapshot in dataSnapshot.children) {
                    val banderaUrl = countrySnapshot.child("bandera").getValue(String::class.java)
                    banderaUrl?.let { countryFlagsURL.add(it) }
                }

                for (url in countryFlagsURL) {
                    Glide.with(this@JuegoPaisActivity)
                        .load(url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .preload()
                }
            }
        }.addOnFailureListener { e ->
            Log.e("FirebaseError", "Error al obtener datos", e)
        }
    }

    private fun updateImages() {
        databaseReference.child("paises").get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val countryFlags = mutableListOf<String>()
                for (countrySnapshot in dataSnapshot.children) {
                    val banderasURL = countrySnapshot.child("bandera").getValue(String::class.java)
                    val continent = countrySnapshot.child("continente").getValue(String::class.java)
                    if (banderasURL != null && (continent == selectedContinent || selectedContinent == "Todos")) {
                        countryFlags.add(banderasURL)
                    }
                }

                val playCountryFlags = mutableListOf<String>()

                while (playCountryFlags.size < 4) {
                    val randomFlag = countryFlags.random()
                    if (randomFlag !in playCountryFlags && randomFlag !in selectedCountries) {
                        playCountryFlags.add(randomFlag)
                    }
                }

                if (playCountryFlags.size >= 4){
                    correctCountry = playCountryFlags.random()
                    val paisSnapshot = dataSnapshot.children.first { it.child("bandera").getValue(String::class.java) == correctCountry }
                    val banderaName = paisSnapshot.child("nombre").getValue(String::class.java)

                    paisTextView.text = banderaName

                    val shufledFlags = playCountryFlags.shuffled()
                    for (i in banderas.indices) {
                        if (!isDestroyed) {
                            Glide.with(this)
                                .load(shufledFlags[i])
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(banderas[i])
                            banderas[i].tag = shufledFlags[i]
                            Log.d("JuegoPaisActivity", "Tag Bandera: ${banderas[i].tag}")
                        }
                    }
                }
                setImageViewsEnabled(true)
                selectedCountries.add(correctCountry)
            }
        }.addOnFailureListener { e ->
            Log.e("FirebaseError", "Error al obtener datos", e)
        }
    }
}