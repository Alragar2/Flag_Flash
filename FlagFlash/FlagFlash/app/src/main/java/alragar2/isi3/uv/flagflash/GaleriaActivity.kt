package alragar2.isi3.uv.flagflash

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.database.*

class GaleriaActivity : AppCompatActivity() {
    private val activityScope = CoroutineScope(Dispatchers.Main)

    private lateinit var databaseReference: DatabaseReference
    private lateinit var paisAdapter: PaisAdapter
    private var allPaises: List<Map<String, String>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_galeria)

        databaseReference = FirebaseDatabase.getInstance("https://flag-flash-tfg-default-rtdb.europe-west1.firebasedatabase.app/").reference

        paisAdapter = PaisAdapter(allPaises, this::showCountryDetails)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        recyclerView.adapter = paisAdapter

        loadPaises()

        searchPaises()
    }

    private fun searchPaises(){
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterPaises(newText)
                return true
            }
        })

        searchView.setOnClickListener{
            searchView.isIconified = false
            searchView.requestFocusFromTouch()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)
        }

        searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text).setTextColor(
            ContextCompat.getColor(this, R.color.black))
        searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text).setHintTextColor(
            ContextCompat.getColor(this, R.color.black))

        // Cambiar el color de la X (icono de cancelación)
        val closeButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeButton.setColorFilter(ContextCompat.getColor(this, R.color.black), PorterDuff.Mode.SRC_IN)

        // Cambiar el color del icono de búsqueda
        val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        searchIcon.setColorFilter(ContextCompat.getColor(this, R.color.black), PorterDuff.Mode.SRC_IN)
    }

    private fun loadPaises() {
        databaseReference.child("paises").get().addOnSuccessListener { dataSnapshot ->
            val paises = mutableListOf<Map<String, String>>()
            for (paisSnapshot in dataSnapshot.children) {
                val pais = paisSnapshot.value as? Map<String, String>
                pais?.let { paises.add(it) }
            }
            allPaises = paises
            Log.d("FirebaseData", "Paises cargados: $allPaises")
            paisAdapter.updatePaises(allPaises)
        }.addOnFailureListener { error ->
            Log.e("FirebaseError", "Error al obtener datos", error)
        }
    }

    private fun filterPaises(query: String?) {
        val filteredPaises = if (query.isNullOrEmpty()) {
            allPaises
        } else {
            allPaises.filter { it["nombre"]?.contains(query, ignoreCase = true) == true }
        }
        paisAdapter.updatePaises(filteredPaises)
    }

    private fun showCountryDetails(pais: Map<String, String>) {
        MaterialDialog(this@GaleriaActivity).show {
            val customView = customView(R.layout.activity_galeria_detalles)
            val countryFlagImageView = customView.findViewById<ImageView>(R.id.country_flag)
            val countryNameTextView = customView.findViewById<TextView>(R.id.country_name)
            val countryCapitalTextView = customView.findViewById<TextView>(R.id.country_capital)
            val countryContinentTextView = customView.findViewById<TextView>(R.id.country_continent)

            val banderaUrl = pais["bandera"]
            if (banderaUrl != null) {
                Glide.with(this@GaleriaActivity)
                    .load(banderaUrl)
                    .into(countryFlagImageView)
            }
            countryNameTextView.text = pais["nombre"] ?: "Nombre no disponible"
            countryCapitalTextView.text = "Capital: ${pais["capital"] ?: "No disponible"}"
            countryContinentTextView.text = "Continente: ${pais["continente"] ?: "No disponible"}"

            positiveButton(text = "OK") { dialog ->
                dialog.dismiss()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
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
