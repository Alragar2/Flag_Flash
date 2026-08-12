package alragar2.isi3.uv.flagflash.juego.compose

import alragar2.isi3.uv.flagflash.UserPreferences
import alragar2.isi3.uv.flagflash.UserScoreManager
import alragar2.isi3.uv.flagflash.musica.SoundEffectsManager
import alragar2.isi3.uv.flagflash.R
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
import kotlin.math.max

enum class MasMenosMetric {
    POBLACION, AREA
}

data class MasMenosUiState(
    val isLoading: Boolean = true,
    val continent: String = "Todos",
    val metric: MasMenosMetric = MasMenosMetric.POBLACION,
    val currentCountry: Map<String, Any>? = null,
    val nextCountry: Map<String, Any>? = null,
    val score: Int = 0,
    val gameScore: Int = 0, // Aciertos * 10
    val lives: Int = 3,
    val maxLives: Int = 3,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val activePet: String? = null,
    val isPetFed: Boolean = false,
    val timeLeft: Int? = null,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 10,
    val revealedValueB: Boolean = false,
    val isCorrectGuess: Boolean? = null, // true = acierto, false = fallo, null = sin responder
    val isButtonEnabled: Boolean = true,
    val allCountriesCount: Int = 0,
    val timeElapsed: Long = 0L
)

class MasMenosViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MasMenosUiState())
    val uiState: StateFlow<MasMenosUiState> = _uiState.asStateFlow()

    private val userPreferences = UserPreferences(application)
    private val userScoreManager = UserScoreManager()
    private val db = FirebaseDatabase.getInstance("https://flag-flash-tfg-default-rtdb.europe-west1.firebasedatabase.app/").reference

    private var allCountries: List<Map<String, Any>> = emptyList()
    private val unusedCountries = mutableListOf<Map<String, Any>>()
    
    private var timerJob: Job? = null
    private var startTimeMillis: Long = 0
    private var gameType: GameType = GameType.NORMAL

    fun initGame(continent: String, metric: MasMenosMetric, type: GameType, questions: String) {
        timerJob?.cancel()
        gameType = type
        
        val totalQ = if (questions == "infinity" || type == GameType.SURVIVAL) Int.MAX_VALUE else questions.toIntOrNull() ?: 10
        val initialLives = if (type == GameType.SURVIVAL) 1 else 3
        val initialTimeLeft = if (type == GameType.TIME_ATTACK) 60 else null

        _uiState.value = MasMenosUiState(
            isLoading = true,
            continent = continent,
            metric = metric,
            lives = initialLives,
            maxLives = initialLives,
            timeLeft = initialTimeLeft,
            totalQuestions = totalQ
        )
        startTimeMillis = System.currentTimeMillis()

        viewModelScope.launch {
            loadUserStats()
            loadCountries(continent, metric)
            setupInitialPair()
            if (type == GameType.TIME_ATTACK) {
                startTimer()
            }
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

    private suspend fun loadCountries(continent: String, metric: MasMenosMetric) {
        var countriesList: List<Map<String, Any>> = emptyList()
        try {
            val snapshot = db.child("paises").get().await()
            if (snapshot.exists()) {
                countriesList = snapshot.children.mapNotNull { it.value as? Map<String, Any> }
            }
        } catch (e: Exception) {
            Log.e("MasMenosViewModel", "Error loading from Firebase, falling back to local JSON", e)
        }

        if (countriesList.isEmpty()) {
            countriesList = loadCountriesFromLocalJson()
        }

        // Filtrar por continente
        val continentFiltered = if (continent == "Todos") {
            countriesList
        } else {
            countriesList.filter { (it["continente"] as? String) == continent }
        }

        // Filtrar países que contengan datos válidos para la métrica elegida
        allCountries = continentFiltered.filter { country ->
            val name = country["nombre"] as? String
            val flag = country["bandera"] as? String
            val valMetric = getMetricValue(country, metric)
            !name.isNullOrBlank() && !flag.isNullOrBlank() && valMetric > 0
        }
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
                
                // Mapear campos de población y área si existen
                if (obj.has("poblacion")) map["poblacion"] = obj.optLong("poblacion")
                if (obj.has("area_km2")) map["area_km2"] = obj.optDouble("area_km2")
                
                list.add(map)
            }
        } catch (e: Exception) {
            Log.e("MasMenosViewModel", "Failed to parse local JSON", e)
        }
        return list
    }

    private fun setupInitialPair() {
        if (allCountries.size < 2) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isGameOver = true,
                isVictory = false
            )
            return
        }

        val shuffled = allCountries.shuffled()
        val first = shuffled[0]
        val second = shuffled[1]
        
        unusedCountries.clear()
        unusedCountries.addAll(shuffled.drop(2))

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            currentCountry = first,
            nextCountry = second,
            allCountriesCount = allCountries.size
        )
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft != null && _uiState.value.timeLeft!! > 0 && !_uiState.value.isGameOver) {
                delay(1000)
                if (!_uiState.value.isGameOver) {
                    val current = _uiState.value.timeLeft ?: 0
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

    fun makeGuess(guessIsMore: Boolean) {
        val state = _uiState.value
        if (!state.isButtonEnabled || state.isGameOver) return

        _uiState.value = _uiState.value.copy(isButtonEnabled = false)

        val valA = getMetricValue(state.currentCountry, state.metric)
        val valB = getMetricValue(state.nextCountry, state.metric)

        val correct = if (guessIsMore) {
            valB >= valA
        } else {
            valB <= valA
        }

        if (correct) {
            SoundEffectsManager.playSound(getApplication(), R.raw.correct_answer)
        } else {
            SoundEffectsManager.playSound(getApplication(), R.raw.wrong_answer)
        }

        viewModelScope.launch {
            // Revelar el valor
            _uiState.value = _uiState.value.copy(
                revealedValueB = true,
                isCorrectGuess = correct
            )

            // Esperar 1.5 segundos para que el usuario vea la respuesta
            delay(1500)

            handleAnswerResult(correct)
        }
    }

    private fun handleAnswerResult(correct: Boolean) {
        val state = _uiState.value
        if (state.isGameOver) return

        var newScore = state.score
        var newGameScore = state.gameScore
        var newLives = state.lives
        var newTimeLeft = state.timeLeft
        var isPetFed = state.isPetFed
        val activePet = state.activePet

        if (correct) {
            newScore += 10
            newGameScore += 10
            if (gameType == GameType.TIME_ATTACK && newTimeLeft != null) {
                newTimeLeft += 2
            }
        } else {
            // Habilidad de la tortuga: Evita penalización del primer error
            if (activePet == "tortuga" && isPetFed) {
                isPetFed = false
                userPreferences.setPetFed("tortuga", false)
            } else {
                if (gameType != GameType.TIME_ATTACK) {
                    newLives--
                } else if (newTimeLeft != null) {
                    newTimeLeft = max(0, newTimeLeft - 3)
                }
            }

            // Habilidad del gato: Resucita con 1 vida al llegar a 0
            if (newLives <= 0 && activePet == "gato" && isPetFed && gameType != GameType.TIME_ATTACK) {
                newLives = 1
                isPetFed = false
                userPreferences.setPetFed("gato", false)
            }
        }

        val nextIndex = state.currentQuestionIndex + 1

        val isOver = newLives <= 0 || (newTimeLeft != null && newTimeLeft == 0) || nextIndex >= state.totalQuestions
        val isVictory = nextIndex >= state.totalQuestions && newLives > 0

        _uiState.value = state.copy(
            score = newScore,
            gameScore = newGameScore,
            lives = newLives,
            timeLeft = newTimeLeft,
            isPetFed = isPetFed,
            currentQuestionIndex = nextIndex
        )

        if (isOver) {
            finishGame(victory = isVictory)
        } else {
            // Avanzar al siguiente par de países
            val nextBase = state.nextCountry
            val nextCompare = getNextCompareCountry()

            _uiState.value = _uiState.value.copy(
                currentCountry = nextBase,
                nextCountry = nextCompare,
                revealedValueB = false,
                isCorrectGuess = null,
                isButtonEnabled = true
            )
        }
    }

    private fun getNextCompareCountry(): Map<String, Any>? {
        if (unusedCountries.isEmpty()) {
            // Si nos quedamos sin países, rellenamos barajando toda la lista de nuevo
            val currentNextName = _uiState.value.nextCountry?.get("nombre") as? String
            val refilled = allCountries.filter { (it["nombre"] as? String) != currentNextName }.shuffled()
            unusedCountries.addAll(refilled)
        }
        return if (unusedCountries.isNotEmpty()) unusedCountries.removeAt(0) else null
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
            revealedValueB = true,
            timeElapsed = elapsed
        )

        viewModelScope.launch {
            saveGameResults(victory, elapsed)
        }
    }

    private suspend fun saveGameResults(victory: Boolean, timeElapsed: Long) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val correctCount = _uiState.value.gameScore / 10

        // Guardar la puntuación final acumulada del usuario en Firebase Firestore
        userScoreManager.saveUserScore(userId, _uiState.value.score, {}, {})

        // Guardar estadísticas generales
        userPreferences.incrementTotalGames()
        if (victory && _uiState.value.lives > 0) {
            userPreferences.incrementPerfectGames()
        }

        userPreferences.addCorrectAnswers(correctCount)

        // Monedas: 1 moneda por acierto + multiplicador de tiempo
        val baseCoins = correctCount
        val mult1 = 1.0 + (Math.random() * 0.3) // Entre x1 y x1.3

        val isFast = correctCount > 0 && timeElapsed <= 4 * correctCount
        val finalMultiplier = if (isFast) {
            val mult2 = 1.7 + (Math.random() * 0.3) // Entre x1.7 y x2
            mult1 + mult2
        } else {
            mult1
        }

        var totalCoinsGained = Math.round(baseCoins * finalMultiplier).toInt()

        if (victory) {
            val bonus = if (_uiState.value.continent == "Todos") 100 else 30
            totalCoinsGained += bonus
        }

        // Guardar monedas
        userPreferences.getCoins { current ->
            userPreferences.setCoins(current + totalCoinsGained)
        }
    }

    fun getMetricValue(country: Map<String, Any>?, metric: MasMenosMetric): Double {
        if (country == null) return 0.0
        return when (metric) {
            MasMenosMetric.POBLACION -> {
                val value = country["poblacion"]
                (value as? Number)?.toDouble() ?: 0.0
            }
            MasMenosMetric.AREA -> {
                val value = country["area_km2"]
                (value as? Number)?.toDouble() ?: 0.0
            }
        }
    }
}
