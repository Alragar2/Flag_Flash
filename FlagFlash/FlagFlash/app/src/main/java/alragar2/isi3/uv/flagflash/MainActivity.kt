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
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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
    private var modalListener: ListenerRegistration? = null

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
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // USAMOS SNAPSHOT LISTENER: Escucha cambios en tiempo real
            modalListener = db.collection("users").document(user.uid)
                .addSnapshotListener { document, error ->
                    if (error != null || document == null || !document.exists()) return@addSnapshotListener
                    updateModalUI(dialog, document)
                }
        }

        dialog.findViewById<Button>(R.id.modalCerrarSesion).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, AuthenticationLoginActivity::class.java))
            dialog.dismiss()
            finish()
        }

        dialog.setOnDismissListener {
            modalListener?.remove()
            modalListener = null
        }

        dialog.show()
    }

    private fun updateModalUI(dialog: Dialog, document: com.google.firebase.firestore.DocumentSnapshot) {
        val nombre = dialog.findViewById<TextView>(R.id.modalNombreUsuario)
        val score = dialog.findViewById<TextView>(R.id.modalPuntuacion)
        val modalMonedas = dialog.findViewById<TextView>(R.id.modalMonedas)
        val modalFoodCount = dialog.findViewById<TextView>(R.id.modalFoodCount)
        val modalBtnFeed = dialog.findViewById<Button>(R.id.modalBtnFeed)
        val modalPetStatus = dialog.findViewById<TextView>(R.id.modalPetStatus)
        
        val containerBuho = dialog.findViewById<View>(R.id.containerBuho)
        val containerGato = dialog.findViewById<View>(R.id.containerGato)
        val containerTortuga = dialog.findViewById<View>(R.id.containerTortuga)

        val petIconBuho = dialog.findViewById<ImageView>(R.id.petIconBuho)
        val petIconGato = dialog.findViewById<ImageView>(R.id.petIconGato)
        val petIconTortuga = dialog.findViewById<ImageView>(R.id.petIconTortuga)

        val indicatorBuho = dialog.findViewById<ImageView>(R.id.petFedIndicatorBuho)
        val indicatorGato = dialog.findViewById<ImageView>(R.id.petFedIndicatorGato)
        val indicatorTortuga = dialog.findViewById<ImageView>(R.id.petFedIndicatorTortuga)

        nombre.text = document.getString("name") ?: "Jugador"
        score.text = (document.getLong("score")?.toString() ?: "0")
        val coins = document.getLong("coins")?.toInt() ?: 100
        modalMonedas.text = coins.toString()
        tvMainCoins.text = coins.toString()

        val food = document.getLong("foodCount")?.toInt() ?: 0
        modalFoodCount.text = "x$food"

        val selectedPet = document.getString("selectedPet")
        val ownedPets = document.get("ownedPets") as? List<String> ?: emptyList()
        val fedStates = document.get("petFedStates") as? Map<String, Boolean> ?: emptyMap()

        modalPetStatus.text = if (selectedPet != null) {
            val isFed = fedStates[selectedPet] ?: false
            "${selectedPet.replaceFirstChar { it.uppercase() }} " + (if (isFed) "(Listo)" else "(Hambriento)")
        } else {
            "Ninguna seleccionada"
        }

        // Configurar iconos y visibilidad de mascotas compradas
        setupPetInModal(petIconBuho, indicatorBuho, containerBuho, "buho", selectedPet, ownedPets, fedStates)
        setupPetInModal(petIconGato, indicatorGato, containerGato, "gato", selectedPet, ownedPets, fedStates)
        setupPetInModal(petIconTortuga, indicatorTortuga, containerTortuga, "tortuga", selectedPet, ownedPets, fedStates)

        modalBtnFeed.setOnClickListener {
            if (selectedPet != null && food > 0 && !(fedStates[selectedPet] ?: false)) {
                userPreferences.setFoodCount(food - 1)
                userPreferences.setPetFed(selectedPet, true)
                // El SnapshotListener se encargará de refrescar la UI automáticamente
            }
        }
    }

    private fun setupPetInModal(
        icon: ImageView, 
        indicator: ImageView, 
        container: View, 
        petId: String, 
        selectedPet: String?, 
        ownedPets: List<String>, 
        fedStates: Map<String, Boolean>
    ) {
        if (ownedPets.contains(petId)) {
            container.visibility = View.VISIBLE
            
            // Resaltar si está seleccionada
            if (selectedPet == petId) {
                icon.alpha = 1.0f
                container.setBackgroundResource(R.drawable.search_view_background)
            } else {
                icon.alpha = 0.4f
                container.setBackgroundResource(0)
            }

            // Indicador de "Alimentado" (Tick verde)
            val isFed = fedStates[petId] ?: false
            indicator.visibility = if (isFed) View.VISIBLE else View.GONE

            container.setOnClickListener {
                if (selectedPet != petId) {
                    userPreferences.setSelectedPet(petId)
                    // No hace falta Toast ni recargar manualmente, el SnapshotListener lo hará
                }
            }
        } else {
            // NO COMPRADA: Desaparece del modal
            container.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        userPreferences.getCoins { coins ->
            runOnUiThread { tvMainCoins.text = coins.toString() }
        }
    }
}