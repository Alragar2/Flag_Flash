package alragar2.isi3.uv.flagflash.juego.compose

import alragar2.isi3.uv.flagflash.R
import alragar2.isi3.uv.flagflash.UserPreferences
import alragar2.isi3.uv.flagflash.UserScoreManager
import alragar2.isi3.uv.flagflash.musica.SoundEffectsManager
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import org.json.JSONArray
import java.text.Normalizer

data class EscribirPaisesUiState(
    val isLoading: Boolean = true,
    val continent: String = "Todos",
    val allCountries: List<Map<String, Any>> = emptyList(),
    val guessedCountries: Set<String> = emptySet(), // Almacena el campo "nombre" de los países adivinados
    val revealedRemaining: Boolean = false,
    val inputText: String = "",
    val timeLeft: Int = 0,
    val totalTime: Int = 0,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val score: Int = 0,
    val activePet: String? = null,
    val isPetFed: Boolean = false
)

class EscribirPaisesViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(EscribirPaisesUiState())
    val uiState: StateFlow<EscribirPaisesUiState> = _uiState.asStateFlow()

    private val userPreferences = UserPreferences(application)
    private val userScoreManager = UserScoreManager()
    private val db = FirebaseDatabase.getInstance("https://flag-flash-tfg-default-rtdb.europe-west1.firebasedatabase.app/").reference

    private var timerJob: Job? = null
    private var startTimeMillis: Long = 0

    // Tabla de alias comunes para países
    private val aliases = mapOf(
        "Estados Unidos" to listOf("eeuu", "ee.uu.", "usa", "estados unidos de america", "estados unidos de américa", "estados unidos de america", "united states"),
        "Reino Unido" to listOf("uk", "gran bretaña", "gran bretana", "inglaterra", "great britain"),
        "Países Bajos" to listOf("holanda", "paises bajos", "netherlands"),
        "Chequia" to listOf("republica checa", "república checa"),
        "República del Congo" to listOf("congo", "republica del congo"),
        "República Democrática del Congo" to listOf("rd congo", "rdc", "republica democratica del congo", "república democrática del congo"),
        "Bosnia" to listOf("bosnia y herzegovina", "bosnia-herzegovina"),
        "Macedonia" to listOf("macedonia del norte", "macedonia norte"),
        "Vaticano" to listOf("ciudad del vaticano"),
        "República Dominicana" to listOf("dominicana", "republica dominicana"),
        "San Cristóbal y Nieves" to listOf("saint kitts", "saint kitts y nevis", "san cristobal y nieves", "san cristobal"),
        "San Vicente y las Granadinas" to listOf("san vicente"),
        "Trinidad y Tobago" to listOf("trinidad"),
        "Emiratos Árabes Unidos" to listOf("emiratos arabes unidos", "emiratos arabes", "eau", "emiratos"),
        "Corea del Norte" to listOf("corea norte"),
        "Corea del Sur" to listOf("corea sur"),
        "Papúa Nueva Guinea" to listOf("papua", "papua nueva guinea"),
        "Costa de Marfil" to listOf("costa de marfil", "ivory coast"),
        "Suazilandia" to listOf("eswatini", "swazilandia")
    )

    fun initGame(continent: String) {
        timerJob?.cancel()
        _uiState.value = EscribirPaisesUiState(
            isLoading = true,
            continent = continent
        )
        startTimeMillis = System.currentTimeMillis()

        viewModelScope.launch {
            loadUserStats()
            loadCountries(continent)
            startTimer()
        }
    }

    private suspend fun loadUserStats() {
        val score = suspendCancellableCoroutine<Int> { continuation ->
            userPreferences.getScore { scoreVal ->
                continuation.resume(scoreVal)
            }
        }
        val activePet = suspendCancellableCoroutine<String?> { continuation ->
            userPreferences.getSelectedPet { petVal ->
                continuation.resume(petVal)
            }
        }
        var isPetFed = false
        if (activePet != null) {
            isPetFed = suspendCancellableCoroutine<Boolean> { continuation ->
                userPreferences.isPetFed(activePet) { fedVal ->
                    continuation.resume(fedVal)
                }
            }
        }

        _uiState.value = _uiState.value.copy(
            score = score,
            activePet = activePet,
            isPetFed = isPetFed
        )
    }

    private suspend fun loadCountries(continent: String) {
        var countriesList: List<Map<String, Any>> = emptyList()
        try {
            // Intenta cargar desde Firebase
            val snapshot = db.child("paises").get().await()
            if (snapshot.exists()) {
                countriesList = snapshot.children.mapNotNull { it.value as? Map<String, Any> }
            }
        } catch (e: Exception) {
            Log.e("EscribirPaisesViewModel", "Error loading from Firebase, falling back to local JSON", e)
        }

        // Si Firebase falló o está vacío, usa el JSON local
        if (countriesList.isEmpty()) {
            countriesList = loadCountriesFromLocalJson()
        }

        // Filtrar por continente
        val filtered = if (continent == "Todos") {
            countriesList
        } else {
            countriesList.filter { (it["continente"] as? String) == continent }
        }

        // Determinar el tiempo según la cantidad de países
        val duration = when (continent) {
            "Oceanía" -> 120 // 2 min
            "América" -> 300 // 5 min
            "Europa" -> 360  // 6 min
            "Asia" -> 360    // 6 min
            "África" -> 420  // 7 min
            else -> 900      // 15 min para todos
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            allCountries = filtered,
            timeLeft = duration,
            totalTime = duration
        )
    }

    private fun loadCountriesFromLocalJson(): List<Map<String, Any>> {
        val list = mutableListOf<Map<String, Any>>()
        try {
            val inputStream = getApplication<Application>().resources.openRawResource(R.raw.paises)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val map = mutableMapOf<String, Any>()
                map["id"] = obj.optInt("id")
                map["nombre"] = obj.optString("nombre")
                map["capital"] = obj.optString("capital")
                map["bandera"] = obj.optString("bandera")
                map["continente"] = obj.optString("continente")
                list.add(map)
            }
        } catch (e: Exception) {
            Log.e("EscribirPaisesViewModel", "Failed to parse local JSON", e)
        }
        return list
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0 && !_uiState.value.isGameOver) {
                delay(1000)
                if (!_uiState.value.isGameOver) {
                    val current = _uiState.value.timeLeft
                    if (current <= 1) {
                        _uiState.value = _uiState.value.copy(timeLeft = 0)
                        onTimeOut()
                    } else {
                        _uiState.value = _uiState.value.copy(timeLeft = current - 1)
                    }
                }
            }
        }
    }

    fun onInputChanged(text: String) {
        if (_uiState.value.isGameOver) return
        _uiState.value = _uiState.value.copy(inputText = text)

        val targetCountry = findMatchingCountry(text)
        if (targetCountry != null) {
            val countryName = targetCountry["nombre"] as String
            val alreadyGuessed = _uiState.value.guessedCountries.contains(countryName)
            if (!alreadyGuessed) {
                // Acierto!
                val newGuessed = _uiState.value.guessedCountries + countryName
                val newScore = _uiState.value.score + 10

                // Registrar país descubierto en preferencias
                userPreferences.addDiscoveredCountry(countryName)

                // Reproducir sonido de acierto
                SoundEffectsManager.playSound(getApplication(), R.raw.correct_answer)

                _uiState.value = _uiState.value.copy(
                    guessedCountries = newGuessed,
                    score = newScore,
                    inputText = ""
                )

                // Comprobar victoria
                if (newGuessed.size == _uiState.value.allCountries.size) {
                    finishGame(victory = true)
                }
            }
        }
    }

    fun giveUp() {
        if (_uiState.value.isGameOver) return
        finishGame(victory = false)
    }

    private fun onTimeOut() {
        finishGame(victory = false)
    }

    private fun finishGame(victory: Boolean) {
        timerJob?.cancel()
        val elapsed = (System.currentTimeMillis() - startTimeMillis) / 1000
        
        _uiState.value = _uiState.value.copy(
            isGameOver = true,
            isVictory = victory,
            revealedRemaining = true
        )

        // Registrar estadísticas y guardar datos del usuario
        viewModelScope.launch {
            saveGameResults(victory, elapsed)
        }
    }

    private suspend fun saveGameResults(victory: Boolean, timeElapsed: Long) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val guessedCount = _uiState.value.guessedCountries.size
        
        // Guardar puntuación del usuario en Firebase Realtime DB
        userScoreManager.saveUserScore(userId, _uiState.value.score, {}, {})

        // Actualizar estadísticas en UserPreferences
        userPreferences.incrementTotalGames()
        if (victory) {
            userPreferences.incrementPerfectGames()
            userPreferences.unlockAchievement("first_win") {}
        }
        
        userPreferences.getStats { total, perfect ->
            if (total >= 10) userPreferences.unlockAchievement("veteran_10") {}
            if (total >= 50) userPreferences.unlockAchievement("veteran_50") {}
            if (perfect >= 1) userPreferences.unlockAchievement("perfect_1") {}
            if (perfect >= 5) userPreferences.unlockAchievement("perfect_5") {}
        }

        userPreferences.addCorrectAnswers(guessedCount)
        userPreferences.getAdvancedStats { correct, _, _ ->
            if (correct >= 100) userPreferences.unlockAchievement("erudito_100") {}
        }

        // Monedas: 1 moneda por país acertado
        val baseCoins = guessedCount
        val mult1 = 1.0 + (Math.random() * 0.3) // Entre x1 y x1.3
        
        // Si adivinó rápido (promedio de menos de 4 segundos por país acertado)
        val isFast = guessedCount > 0 && timeElapsed <= 4 * guessedCount
        val finalMultiplier = if (isFast) {
            val mult2 = 1.7 + (Math.random() * 0.3) // Entre x1.7 y x2
            mult1 + mult2
        } else {
            mult1
        }

        var totalCoinsGained = Math.round(baseCoins * finalMultiplier).toInt()
        
        // Bonus extra por completar el continente completo
        if (victory) {
            val bonus = if (_uiState.value.continent == "Todos") 200 else 50
            totalCoinsGained += bonus
        }

        // Guardar monedas
        userPreferences.getCoins { current ->
            userPreferences.setCoins(current + totalCoinsGained)
        }
    }

    private fun findMatchingCountry(input: String): Map<String, Any>? {
        val normInput = input.normalize()
        if (normInput.isEmpty()) return null

        for (country in _uiState.value.allCountries) {
            val name = country["nombre"] as? String ?: continue
            val normName = name.normalize()
            if (normName == normInput) {
                return country
            }

            // Comprobar alias
            val countryAliases = aliases[name]
            if (countryAliases != null) {
                for (alias in countryAliases) {
                    if (alias.normalize() == normInput) {
                        return country
                    }
                }
            }
        }
        return null
    }

    private fun String.normalize(): String {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        return temp.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
            .trim()
    }
}
