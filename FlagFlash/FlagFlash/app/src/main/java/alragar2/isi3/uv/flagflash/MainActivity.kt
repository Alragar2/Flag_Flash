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
import android.app.Dialog
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private lateinit var mAdView : AdView
    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar si el usuario está autenticado
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // Si no está autenticado, redirigir a AuthenticationLoginActivity
            val intent = Intent(this, AuthenticationLoginActivity::class.java)
            startActivity(intent)
            finish() // Finalizar MainActivity para que no pueda volver atrás
            return
        }

        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance() // Initialize Firebase Auth

        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.i("Session", "Sesión iniciada con email: ${user.email}")
            } else {
                Log.i("Session", "Sesión cerrada")
            }
        }

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

        val registroButton = findViewById<Button>(R.id.Options)
        registroButton.setOnClickListener {
            showOptionsModal()
        }

        // Iniciar el servicio de música
        val musicIntent = Intent(this, MusicService::class.java)
        startService(musicIntent)
    }

    private fun showOptionsModal() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.modal_options)

        val iniciarSesionButton = dialog.findViewById<Button>(R.id.modalIniciarSesion)
        val cerrarSesionButton = dialog.findViewById<Button>(R.id.modalCerrarSesion)
        val eliminarCuentaButton = dialog.findViewById<Button>(R.id.modalEliminarCuenta)
        val nombre = dialog.findViewById<TextView>(R.id.modalNombreUsuario)
        val score = dialog.findViewById<TextView>(R.id.modalPuntuacion)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            iniciarSesionButton.visibility = View.GONE
            cerrarSesionButton.visibility = View.VISIBLE
            eliminarCuentaButton.visibility = View.VISIBLE

            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        nombre.visibility = View.VISIBLE
                        score.visibility = View.VISIBLE
                        nombre.text = document.getString("name")
                        score.text = document.getLong("score")?.toString() ?: "0"
                    } else {
                        nombre.visibility = View.GONE
                        score.visibility = View.GONE
                    }
                }
                .addOnFailureListener { exception ->
                    nombre.visibility = View.GONE
                    score.visibility = View.GONE
                }
        } else {
            iniciarSesionButton.visibility = View.VISIBLE
            cerrarSesionButton.visibility = View.GONE
            eliminarCuentaButton.visibility = View.GONE
            nombre.visibility = View.GONE
            score.visibility = View.GONE
        }

        iniciarSesionButton.setOnClickListener {
            val intent = Intent(this, AuthenticationLoginActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        cerrarSesionButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, AuthenticationLoginActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        eliminarCuentaButton.setOnClickListener {
            val confirmDialog = Dialog(this)
            confirmDialog.setContentView(R.layout.dialog_confirm_delete)
            val confirmButton = confirmDialog.findViewById<Button>(R.id.confirmButton)
            val cancelButton = confirmDialog.findViewById<Button>(R.id.cancelButton)

            confirmButton.setOnClickListener {
                FirebaseAuth.getInstance().currentUser?.delete()
                confirmDialog.dismiss()
                val intent = Intent(this, AuthenticationLoginActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
            }

            cancelButton.setOnClickListener {
                confirmDialog.dismiss()
            }


            confirmDialog.show()
        }

        dialog.show()
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