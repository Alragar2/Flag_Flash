package alragar2.isi3.uv.flagflash.galeria

import alragar2.isi3.uv.flagflash.UserPreferences
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class GaleriaUiState(
    val allCountries: List<Map<String, Any>> = emptyList(),
    val discoveredCountries: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = ""
)

class GaleriaViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(GaleriaUiState())
    val uiState: StateFlow<GaleriaUiState> = _uiState.asStateFlow()

    private val userPreferences = UserPreferences(application)
    private val db = FirebaseDatabase.getInstance("https://flag-flash-tfg-default-rtdb.europe-west1.firebasedatabase.app/").reference

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Load discovered countries
            userPreferences.getDiscoveredCountries { discovered ->
                _uiState.value = _uiState.value.copy(discoveredCountries = discovered)
            }

            // Load all countries from Firebase
            try {
                val snapshot = db.child("paises").get().await()
                if (snapshot.exists()) {
                    val countries = snapshot.children.mapNotNull { it.value as? Map<String, Any> }
                    _uiState.value = _uiState.value.copy(allCountries = countries, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}
