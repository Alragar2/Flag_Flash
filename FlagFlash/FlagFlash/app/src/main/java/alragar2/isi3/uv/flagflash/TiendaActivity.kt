package alragar2.isi3.uv.flagflash

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class TiendaActivity : AppCompatActivity() {

    private lateinit var userPreferences: UserPreferences
    private lateinit var tvCoins: TextView
    private lateinit var tvFoodStock: TextView
    private lateinit var tvPetStatus: TextView
    
    private lateinit var btnActionBuho: LinearLayout
    private lateinit var btnActionGato: LinearLayout
    private lateinit var btnActionTortuga: LinearLayout
    
    private lateinit var tvPriceBuho: TextView
    private lateinit var tvPriceGato: TextView
    private lateinit var tvPriceTortuga: TextView

    private lateinit var ivCoinBuho: ImageView
    private lateinit var ivCoinGato: ImageView
    private lateinit var ivCoinTortuga: ImageView
    
    private var currentCoins = 0
    private var foodCount = 0
    private var selectedPet: String? = null
    private var ownedPets = listOf<String>()
    private var isFed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tienda)

        userPreferences = UserPreferences(this)
        
        tvCoins = findViewById(R.id.tvTiendaCoins)
        tvFoodStock = findViewById(R.id.tvFoodStock)
        tvPetStatus = findViewById(R.id.tvPetStatus)
        
        btnActionBuho = findViewById(R.id.btnBuyBuho)
        btnActionGato = findViewById(R.id.btnBuyGato)
        btnActionTortuga = findViewById(R.id.btnBuyTortuga)

        tvPriceBuho = findViewById(R.id.tvPriceBuho)
        tvPriceGato = findViewById(R.id.tvPriceGato)
        tvPriceTortuga = findViewById(R.id.tvPriceTortuga)

        ivCoinBuho = findViewById(R.id.ivCoinBuho)
        ivCoinGato = findViewById(R.id.ivCoinGato)
        ivCoinTortuga = findViewById(R.id.ivCoinTortuga)
        
        val btnBuyFood = findViewById<LinearLayout>(R.id.btnBuyFood)
        val btnFeedPet = findViewById<Button>(R.id.btnFeedPet)

        loadUserData()

        btnBuyFood.setOnClickListener {
            if (currentCoins >= 50) {
                currentCoins -= 50
                foodCount += 1
                userPreferences.setCoins(currentCoins)
                userPreferences.setFoodCount(foodCount)
                updateUI()
            }
        }

        btnFeedPet.setOnClickListener {
            if (selectedPet != null && !isFed && foodCount > 0) {
                foodCount -= 1
                isFed = true
                userPreferences.setFoodCount(foodCount)
                userPreferences.setPetFed(selectedPet!!, true) // Corregido
                updateUI()
            }
        }

        btnActionBuho.setOnClickListener { handlePetAction("buho") }
        btnActionGato.setOnClickListener { handlePetAction("gato") }
        btnActionTortuga.setOnClickListener { handlePetAction("tortuga") }
    }

    private fun loadUserData() {
        userPreferences.getCoins { coins ->
            currentCoins = coins
            userPreferences.getFoodCount { count ->
                foodCount = count
                userPreferences.getSelectedPet { pet ->
                    selectedPet = pet
                    userPreferences.getOwnedPets { pets ->
                        ownedPets = pets
                        if (selectedPet != null) {
                            userPreferences.isPetFed(selectedPet!!) { fed -> // Corregido
                                isFed = fed
                                updateUI()
                            }
                        } else {
                            updateUI()
                        }
                    }
                }
            }
        }
    }

    private fun handlePetAction(petId: String) {
        if (ownedPets.contains(petId)) {
            selectedPet = petId
            userPreferences.setSelectedPet(petId)
            userPreferences.isPetFed(petId) { fed -> // Corregido
                isFed = fed
                updateUI()
            }
        } else {
            if (currentCoins >= 2000) {
                currentCoins -= 2000
                userPreferences.setCoins(currentCoins)
                userPreferences.addOwnedPet(petId)
                ownedPets = ownedPets + petId
                selectedPet = petId
                userPreferences.setSelectedPet(petId)
                isFed = false
                userPreferences.setPetFed(petId, false) // Corregido
                updateUI()
            }
        }
    }

    private fun updateUI() {
        runOnUiThread {
            tvCoins.text = currentCoins.toString()
            tvFoodStock.text = "x$foodCount"
            
            if (selectedPet == null) {
                tvPetStatus.text = "Selecciona una mascota"
                tvPetStatus.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            } else if (isFed) {
                tvPetStatus.text = "¡Mascota lista para ayudar!"
                tvPetStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            } else {
                tvPetStatus.text = "Tu mascota tiene hambre"
                tvPetStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            }

            updatePetButtonState(btnActionBuho, tvPriceBuho, ivCoinBuho, "buho")
            updatePetButtonState(btnActionGato, tvPriceGato, ivCoinGato, "gato")
            updatePetButtonState(btnActionTortuga, tvPriceTortuga, ivCoinTortuga, "tortuga")
        }
    }

    private fun updatePetButtonState(layout: LinearLayout, textView: TextView, coinIcon: ImageView, petId: String) {
        if (ownedPets.contains(petId)) {
            if (selectedPet == petId) {
                textView.text = "Activa"
                coinIcon.visibility = View.GONE
                layout.isEnabled = false
                layout.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.darker_gray))
            } else {
                textView.text = "Elegir"
                coinIcon.visibility = View.GONE
                layout.isEnabled = true
                layout.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.backgroundButton))
            }
        } else {
            textView.text = "2000"
            coinIcon.visibility = View.VISIBLE
            layout.isEnabled = true
            layout.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
        }
    }
}