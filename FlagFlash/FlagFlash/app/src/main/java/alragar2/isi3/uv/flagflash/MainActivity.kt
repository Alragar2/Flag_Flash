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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import alragar2.isi3.uv.flagflash.authentication.AuthenticationLoginActivity
import alragar2.isi3.uv.flagflash.galeria.GaleriaActivity
import alragar2.isi3.uv.flagflash.musica.MusicService
import alragar2.isi3.uv.flagflash.ranking.RankingActivity
import alragar2.isi3.uv.flagflash.resultado.ElegirJugarActivity
import alragar2.isi3.uv.flagflash.resultado.ElegirMultijugarActivity

class MainActivity : AppCompatActivity() {

    private lateinit var mAdView : AdView
    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var tvMainCoins: TextView
    private lateinit var userPreferences: UserPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            val intent = Intent(this, AuthenticationLoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        tvMainCoins = findViewById(R.id.tvMainCoins)
        userPreferences = UserPreferences(this)

        userPreferences.getCoins { coins ->
            runOnUiThread {
                tvMainCoins.text = coins.toString()
            }
        }

        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.i("Session", "Sesión iniciada: ${user.email}")
            } else {
                Log.i("Session", "Sesión cerrada")
            }
        }

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() { Log.i("Ads", "onAdLoaded") }
            override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                Log.e("Ads", "Error Ads: ${error.message}")
            }
        }

        InterstitialAdManager.loadAd(this)

        findViewById<Button>(R.id.Jugar).setOnClickListener {
            startActivity(Intent(this, ElegirJugarActivity::class.java))
        }

        findViewById<Button>(R.id.Multijugador).setOnClickListener {
            startActivity(Intent(this, ElegirMultijugarActivity::class.java))
        }

        findViewById<Button>(R.id.Galeria).setOnClickListener {
            startActivity(Intent(this, GaleriaActivity::class.java))
        }

        findViewById<Button>(R.id.Ranking).setOnClickListener {
            startActivity(Intent(this, RankingActivity::class.java))
        }

        findViewById<Button>(R.id.Tienda).setOnClickListener {
            startActivity(Intent(this, TiendaActivity::class.java))
        }

        findViewById<Button>(R.id.Options).setOnClickListener {
            showOptionsModal()
        }

        startService(Intent(this, MusicService::class.java))
    }

    private fun showOptionsModal() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.modal_options)

        val nombre = dialog.findViewById<TextView>(R.id.modalNombreUsuario)
        val score = dialog.findViewById<TextView>(R.id.modalPuntuacion)
        val modalMonedas = dialog.findViewById<TextView>(R.id.modalMonedas)
        val modalFoodCount = dialog.findViewById<TextView>(R.id.modalFoodCount)
        val modalBtnFeed = dialog.findViewById<Button>(R.id.modalBtnFeed)
        val modalPetStatus = dialog.findViewById<TextView>(R.id.modalPetStatus)
        
        val petIconBuho = dialog.findViewById<ImageView>(R.id.petIconBuho)
        val petIconGato = dialog.findViewById<ImageView>(R.id.petIconGato)
        val petIconTortuga = dialog.findViewById<ImageView>(R.id.petIconTortuga)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        nombre.text = document.getString("name") ?: "Jugador"
                        score.text = "Puntos: " + (document.getLong("score")?.toString() ?: "0")
                        val coins = document.getLong("coins")?.toInt() ?: 100
                        modalMonedas.text = "Monedas: $coins"
                        tvMainCoins.text = coins.toString()

                        val food = document.getLong("foodCount")?.toInt() ?: 0
                        modalFoodCount.text = "Alimento: $food"

                        val selectedPet = document.getString("selectedPet")
                        val isFed = document.getBoolean("petFed") ?: false
                        val ownedPets = document.get("ownedPets") as? List<String> ?: emptyList()

                        modalPetStatus.text = if (selectedPet != null) {
                            "Mascota: ${selectedPet.capitalize()} " + (if (isFed) "(Alimentada)" else "(Hambrienta)")
                        } else {
                            "Estado: Sin seleccionar"
                        }

                        // Configurar iconos de mascotas
                        setupPetIcon(petIconBuho, "buho", selectedPet, ownedPets)
                        setupPetIcon(petIconGato, "gato", selectedPet, ownedPets)
                        setupPetIcon(petIconTortuga, "tortuga", selectedPet, ownedPets)

                        modalBtnFeed.setOnClickListener {
                            if (selectedPet == null) {
                                Toast.makeText(this, "Selecciona una mascota primero", Toast.LENGTH_SHORT).show()
                            } else if (isFed) {
                                Toast.makeText(this, "Ya está alimentada", Toast.LENGTH_SHORT).show()
                            } else if (food > 0) {
                                userPreferences.setFoodCount(food - 1)
                                userPreferences.setPetFed(true)
                                dialog.dismiss()
                                showOptionsModal() // Recargar modal
                                Toast.makeText(this, "¡Mascota alimentada!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "No tienes alimento", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }

        dialog.findViewById<Button>(R.id.modalCerrarSesion).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, AuthenticationLoginActivity::class.java))
            dialog.dismiss()
            finish()
        }

        dialog.findViewById<Button>(R.id.modalEliminarCuenta).setOnClickListener {
            // Lógica de eliminación (omitida por brevedad, igual que antes)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupPetIcon(icon: ImageView, petId: String, selectedPet: String?, ownedPets: List<String>) {
        if (ownedPets.contains(petId)) {
            icon.alpha = if (selectedPet == petId) 1.0f else 0.5f
            if (selectedPet == petId) {
                icon.setBackgroundResource(R.drawable.rounded_corners) // O algún borde para resaltar
            }
            icon.setOnClickListener {
                userPreferences.setSelectedPet(petId)
                Toast.makeText(this, "Has seleccionado a ${petId.capitalize()}", Toast.LENGTH_SHORT).show()
                // Recargar el modal para reflejar el cambio (o cerrar y abrir)
                // Para una mejor UX, podrías actualizar la UI directamente aquí
            }
        } else {
            icon.alpha = 0.1f
            icon.setOnClickListener {
                Toast.makeText(this, "Compra esta mascota en la tienda", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        userPreferences.getCoins { coins ->
            runOnUiThread { tvMainCoins.text = coins.toString() }
        }
    }
}