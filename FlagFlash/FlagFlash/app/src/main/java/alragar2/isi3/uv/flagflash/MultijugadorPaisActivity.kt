package alragar2.isi3.uv.flagflash

import alragar2.isi3.uv.flagflash.BaseDatos.PaisDatabase
import alragar2.isi3.uv.flagflash.BaseDatos.PaisDao
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
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MultijugadorPaisActivity : AppCompatActivity() {
    private lateinit var bandera1: ImageView
    private lateinit var bandera2: ImageView
    private lateinit var bandera3: ImageView
    private lateinit var bandera4: ImageView
    private lateinit var bandera5: ImageView
    private lateinit var bandera6: ImageView
    private lateinit var bandera7: ImageView
    private lateinit var bandera8: ImageView
    private var correctCountry = ""
    private var player1Score = 0
    private var player2Score = 0
    private var correctGuesses = 0
    private lateinit var progressBar: ProgressBar
    private lateinit var progressBar2: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var progressText2: TextView
    private lateinit var paisDao: PaisDao
    private lateinit var vibrator: Vibrator
    private lateinit var paisTextView: TextView
    private lateinit var paisTextView2: TextView
    private val selectedCountries = mutableSetOf<String>()
    private lateinit var tickImageView: ImageView
    private lateinit var tickImageView2: ImageView
    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.multijugador_pais)

        tickImageView = findViewById(R.id.tick)
        tickImageView2 = findViewById(R.id.tick2)


        databaseReference = FirebaseDatabase.getInstance("https://flag-flash-tfg-default-rtdb.europe-west1.firebasedatabase.app/").reference
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            bandera1 = findViewById(R.id.bandera1)
            bandera2 = findViewById(R.id.bandera2)
            bandera3 = findViewById(R.id.bandera3)
            bandera4 = findViewById(R.id.bandera4)
            bandera5 = findViewById(R.id.bandera5)
            bandera6 = findViewById(R.id.bandera6)
            bandera7 = findViewById(R.id.bandera7)
            bandera8 = findViewById(R.id.bandera8)




            progressBar = findViewById(R.id.progressBar)
            progressBar2 = findViewById(R.id.progressBar2)
            progressBar.progress = 0
            progressBar2.progress = 0
            progressText = findViewById(R.id.tvProgress)
            progressText2 = findViewById(R.id.tvProgress2)
            progressText.text = "0/15"
            progressText2.text = "0/15"

            paisTextView = findViewById(R.id.pais)
            paisTextView2 = findViewById(R.id.pais2)

            val player1ScoreView = findViewById<TextView>(R.id.puntuacion1)
            val player2ScoreView = findViewById<TextView>(R.id.puntuacion2)

            CoroutineScope(Dispatchers.IO).launch {
                updateButtons()
            }

            for (bandera in listOf(bandera1, bandera2, bandera3, bandera4, bandera5, bandera6, bandera7, bandera8)) {
                bandera.setOnClickListener { view ->
                    CoroutineScope(Dispatchers.IO).launch {
                        if (bandera.tag == correctCountry) {
                            setImageViewsEnabled(false)
                            playCorrectAnswerSound()
                            // Incrementar la puntuación del jugador correspondiente
                            if (bandera.id in setOf(R.id.bandera1, R.id.bandera2, R.id.bandera3, R.id.bandera4)) {
                                player1Score++
                                updateUI {
                                    player1ScoreView.text = player1Score.toString()
                                    tickImageView.setImageResource(R.drawable.tick_verde) // Mostrar tick verde en tickImageView
                                }
                            } else {
                                player2Score++
                                updateUI {
                                    player2ScoreView.text = player2Score.toString()
                                    tickImageView2.setImageResource(R.drawable.tick_verde) // Mostrar tick verde solo en tickImageView2
                                }
                            }

                            // Actualizar ambas barras de progreso
                            updateUI {
                                progressBar.progress = ++correctGuesses
                                progressBar2.progress = correctGuesses
                                progressText.text = "$correctGuesses/15"
                                progressText2.text = "$correctGuesses/15"
                            }

                            if (correctGuesses == 14) {
                                // Pasar las puntuaciones de los jugadores a VictoriaMJActivity
                                updateUI {
                                    val intent = Intent(this@MultijugadorPaisActivity, VictoriaMJActivity2::class.java)
                                    intent.putExtra("player1Score", player1Score)
                                    intent.putExtra("player2Score", player2Score)
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                // Generar nuevos botones después de un retraso
                                Handler(Looper.getMainLooper()).postDelayed({
                                    CoroutineScope(Dispatchers.IO).launch {
                                        updateButtons()
                                    }
                                    resetImageViews()
                                    setImageViewsEnabled(true)
                                }, 1200)
                            }

                        } else {
                            if (bandera.id in setOf(R.id.bandera1, R.id.bandera2, R.id.bandera3, R.id.bandera4)) {
                                updateUI {
                                    tickImageView.setImageResource(R.drawable.tick_rojo) // Mostrar tick rojo en tickImageView
                                    vibrateDevice()
                                    resetImageViews()
                                }
                            } else {
                                updateUI {
                                    tickImageView2.setImageResource(R.drawable.tick_rojo) // Mostrar tick rojo solo en tickImageView2
                                    vibrateDevice()
                                    resetImageViews()
                                }
                            }
                        }
                        Handler(Looper.getMainLooper()).postDelayed({
                            tickImageView.setImageDrawable(null)
                            tickImageView2.setImageDrawable(null)
                        }, 1000)
                    }
                }
            }
    }

    fun playCorrectAnswerSound() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.correct_answer)
        mediaPlayer.start()
    }

    private fun updateButtons() {
        databaseReference.child("paises").get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val countryFlags = mutableListOf<String>()
                for (countrySnapshot in dataSnapshot.children) {
                    val countryFlag = countrySnapshot.child("bandera").getValue(String::class.java)
                    if (countryFlag != null) {
                        countryFlags.add(countryFlag)
                    }
                }

                if (countryFlags.size < 4) {
                    return@addOnSuccessListener
                }

                val playCountryFlags = mutableListOf<String>()

                while (playCountryFlags.size < 4) {
                    val randomCountryFlag = countryFlags.random()
                    if (randomCountryFlag !in playCountryFlags && randomCountryFlag != correctCountry) {
                        playCountryFlags.add(randomCountryFlag)
                    }
                }

                correctCountry = playCountryFlags.random()
                val paisSnapshot = dataSnapshot.children.first { it.child("bandera").getValue(String::class.java) == correctCountry }
                val nameCountry = paisSnapshot.child("nombre").getValue(String::class.java)

                updateUI {
                    val banderas = listOf(bandera1, bandera2, bandera3, bandera4, bandera5, bandera6, bandera7, bandera8)
                    for (i in banderas.indices) {
                        val imageView = banderas[i]
                        val pais = playCountryFlags[i % playCountryFlags.size]
                        Log.d("Pais", pais)
                        Glide.with(this@MultijugadorPaisActivity)
                            .load(pais)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imageView)
                        imageView.tag = pais
                    }

                    if (nameCountry != null) {
                        Log.d("Pais", nameCountry)
                    }
                    paisTextView.text = nameCountry
                    paisTextView2.text = nameCountry


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

    private fun resetImageViews() {
        for (imageView in listOf(bandera1, bandera2, bandera3, bandera4, bandera5, bandera6, bandera7, bandera8)) {
            imageView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun setImageViewsEnabled(enabled: Boolean) {
        for (imageView in listOf(bandera1, bandera2, bandera3, bandera4, bandera5, bandera6, bandera7, bandera8)) {
            imageView.isEnabled = enabled
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