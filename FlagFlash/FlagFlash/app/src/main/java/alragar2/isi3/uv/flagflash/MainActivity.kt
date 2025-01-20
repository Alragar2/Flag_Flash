package alragar2.isi3.uv.flagflash

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var mAdView : AdView
    private lateinit var logoutButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.i("Ads", "onAdLoaded")
            }

            fun onAdFailedToLoad(errorCode : Int) {
                // Code to be executed when an ad request fails.
                Log.i("Ads", "onAdFailedToLoad")
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.i("Ads", "onAdOpened")
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                Log.i("Ads", "onAdClicked")
            }

            fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.i("Ads", "onAdLeftApplication")
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                Log.i("Ads", "onAdClosed")
            }
        }

        val jugarButton = findViewById<Button>(R.id.Jugar)
        jugarButton.setOnClickListener {
            val intent = Intent(this, ElegirJugarActivity::class.java)
            startActivity(intent)
        }

        val multijugadorButton = findViewById<Button>(R.id.Multijugador)
        multijugadorButton.setOnClickListener {
            val intent = Intent(this, ElegirMultijugarActivity::class.java)
            startActivity(intent)
        }

        val logrosButton = findViewById<Button>(R.id.Galeria)
        logrosButton.setOnClickListener {
            val intent = Intent(this, GaleriaActivity::class.java)
            startActivity(intent)
        }

        val rankingButton = findViewById<Button>(R.id.Ranking)
        rankingButton.setOnClickListener {
            val intent = Intent(this, RankingActivity::class.java)
            startActivity(intent)
        }

        val registroButton = findViewById<Button>(R.id.IniciarSesion)
        registroButton.setOnClickListener {
            val intent = Intent(this, AuthenticationLoginActivity::class.java)
            startActivity(intent)

            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                registroButton.visibility = View.GONE  // Ocultar el botón de registro
            } else {
                registroButton.visibility = View.VISIBLE  // Mostrar el botón de registro
            }
        }

        logoutButton = findViewById(R.id.logoutButton)
        auth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                logoutButton.visibility = View.VISIBLE  // Mostrar el botón de logout
            } else {
                logoutButton.visibility = View.GONE  // Ocultar el botón de logout
            }
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            logoutButton.visibility = View.GONE  // Ocultar el botón de logout
        }


        // Iniciar el servicio de música
        val musicIntent = Intent(this, MusicService::class.java)
        startService(musicIntent)
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authStateListener)
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

        // Detener el servicio de música
        val musicIntent = Intent(this, MusicService::class.java)
        stopService(musicIntent)
    }
}