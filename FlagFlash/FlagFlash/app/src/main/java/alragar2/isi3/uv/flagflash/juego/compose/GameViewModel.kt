package alragar2.isi3.uv.flagflash.juego.compose

import alragar2.isi3.uv.flagflash.UserPreferences
import alragar2.isi3.uv.flagflash.UserScoreManager
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private val userPreferences = UserPreferences(application)
    private val userScoreManager = UserScoreManager()
    private val db = FirebaseDatabase.getInstance("https://flag-flash-tfg-default-rtdb.europe-west1.firebasedatabase.app/").reference
    
    private var allCountries: List<Map<String, Any>> = emptyList()
    private var gameMode: GameMode = GameMode.BANDERA
    private var selectedContinent: String = "Todos"
    private val selectedCountries = mutableListOf<String>()
    private var startTime: Long = 0

    fun initGame(mode: GameMode, continent: String, type: GameType = GameType.NORMAL, questions: String = "15") {
        gameMode = mode
        selectedContinent = continent
        startTime = System.currentTimeMillis()
        
        val totalQ = if (questions == "infinity" || type == GameType.SURVIVAL) Int.MAX_VALUE else questions.toIntOrNull() ?: 15

        _uiState.value = _uiState.value.copy(
            gameType = type,
            totalQuestions = totalQ,
            timeLeft = if (type == GameType.TIME_ATTACK) 60 else null,
            lives = if (type == GameType.SURVIVAL) 1 else 3
        )
        
        viewModelScope.launch {
            loadUserStats()
            loadCountries()
            generateQuestion()
        }

        if (type == GameType.TIME_ATTACK) {
            startTimer()
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (_uiState.value.timeLeft != null && _uiState.value.timeLeft!! > 0 && !_uiState.value.isGameOver) {
                kotlinx.coroutines.delay(1000)
                if (!_uiState.value.isGameOver && _uiState.value.showResultTick == null) {
                    val currentTimer = _uiState.value.timeLeft ?: 0
                    if (currentTimer <= 1) {
                        _uiState.value = _uiState.value.copy(timeLeft = 0)
                        finishGame(false)
                    } else {
                        _uiState.value = _uiState.value.copy(timeLeft = currentTimer - 1)
                    }
                }
            }
        }
    }

    private suspend fun loadUserStats() {
        // Wrap callbacks in suspend if possible or just update state
        userPreferences.getScore { score ->
            _uiState.value = _uiState.value.copy(score = score)
            userPreferences.setInitialScore(score)
        }
        userPreferences.getSelectedPet { pet ->
            _uiState.value = _uiState.value.copy(activePet = pet)
            pet?.let {
                userPreferences.isPetFed(it) { fed ->
                    _uiState.value = _uiState.value.copy(isPetFed = fed)
                }
            }
        }
    }

    private suspend fun loadCountries() {
        try {
            val snapshot = db.child("paises").get().await()
            if (snapshot.exists()) {
                allCountries = snapshot.children.mapNotNull { it.value as? Map<String, Any> }
                    .filter {
                        val continent = it["continente"] as? String
                        selectedContinent == "Todos" || continent == selectedContinent
                    }
            }
        } catch (e: Exception) {
            Log.e("GameViewModel", "Error loading countries", e)
        }
    }

    fun generateQuestion() {
        if (_uiState.value.currentQuestionIndex >= _uiState.value.totalQuestions) {
            finishGame(true)
            return
        }

        if (allCountries.isEmpty()) return

        val pool = allCountries.filter { 
            val id = when(gameMode) {
                GameMode.BANDERA, GameMode.ESCUDO, GameMode.PAIS -> it["nombre"] as? String
                GameMode.CAPITAL -> it["capital"] as? String
                else -> it["nombre"] as? String
            }
            id != null && id !in selectedCountries 
        }

        if (pool.size < 4) {
            if (_uiState.value.totalQuestions == Int.MAX_VALUE) {
                // If it's infinite mode, reset the selected countries and regenerate
                selectedCountries.clear()
                generateQuestion()
                return
            } else {
                // Not enough countries left, reuse or finish
                finishGame(true)
                return
            }
        }

        val questionCountries = pool.shuffled().take(4)
        val correctCountry = questionCountries.random()
        
        val question = when (gameMode) {
            GameMode.BANDERA -> Question(
                promptImageUrl = correctCountry["bandera"] as? String,
                options = questionCountries.map { Option(text = it["nombre"] as? String) },
                correctOption = Option(text = correctCountry["nombre"] as? String)
            )
            GameMode.PAIS -> Question(
                promptText = correctCountry["nombre"] as? String,
                options = questionCountries.map { Option(imageUrl = it["bandera"] as? String) },
                correctOption = Option(imageUrl = correctCountry["bandera"] as? String)
            )
            GameMode.CAPITAL -> Question(
                promptText = correctCountry["nombre"] as? String,
                promptImageUrl = correctCountry["bandera"] as? String,
                options = questionCountries.map { Option(text = it["capital"] as? String) },
                correctOption = Option(text = correctCountry["capital"] as? String)
            )
            GameMode.ESCUDO -> Question(
                promptImageUrl = correctCountry["escudo"] as? String,
                options = questionCountries.map { Option(text = it["nombre"] as? String) },
                correctOption = Option(text = correctCountry["nombre"] as? String)
            )
        }

        val countryId = when(gameMode) {
            GameMode.CAPITAL -> correctCountry["capital"] as String
            else -> correctCountry["nombre"] as String
        }
        selectedCountries.add(countryId)

        _uiState.value = _uiState.value.copy(
            currentQuestion = question,
            isLoading = false,
            selectedOption = null,
            showResultTick = null
        )
    }

    fun onOptionSelected(option: Option) {
        if (_uiState.value.selectedOption != null) return

        val isCorrect = option == _uiState.value.currentQuestion?.correctOption
        _uiState.value = _uiState.value.copy(
            selectedOption = option,
            showResultTick = isCorrect
        )

        viewModelScope.launch {
            if (isCorrect) {
                handleCorrectAnswer()
            } else {
                handleWrongAnswer()
            }
        }
    }

    private fun handleCorrectAnswer() {
        val newScore = _uiState.value.score + 10
        val nextIndex = _uiState.value.currentQuestionIndex + 1
        
        updateScore(10)

        // Record discovered country
        val currentQuestion = _uiState.value.currentQuestion
        if (currentQuestion != null) {
            val discoveredCountry = allCountries.find { 
                when(gameMode) {
                    GameMode.BANDERA, GameMode.ESCUDO -> it["nombre"] == currentQuestion.correctOption.text
                    GameMode.PAIS -> it["nombre"] == currentQuestion.promptText
                    GameMode.CAPITAL -> it["capital"] == currentQuestion.correctOption.text
                    else -> false
                }
            }
            discoveredCountry?.get("nombre")?.let { name ->
                userPreferences.addDiscoveredCountry(name as String)
            }
        }

        val newTimeLeft = if (_uiState.value.gameType == GameType.TIME_ATTACK) {
            (_uiState.value.timeLeft ?: 0) + 2
        } else {
            _uiState.value.timeLeft
        }

        _uiState.value = _uiState.value.copy(
            score = newScore,
            currentQuestionIndex = nextIndex,
            timeLeft = newTimeLeft
        )

        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            generateQuestion()
        }
    }

    private fun handleWrongAnswer() {
        var penalty = -5
        var currentLives = _uiState.value.lives
        var isPetFed = _uiState.value.isPetFed
        val activePet = _uiState.value.activePet

        // Tortuga Ability
        if (activePet == "tortuga" && isPetFed) {
            penalty = 0
            isPetFed = false
            userPreferences.setPetFed("tortuga", false)
            _uiState.value = _uiState.value.copy(isPetFed = false)
        }

        if (_uiState.value.gameType != GameType.TIME_ATTACK) {
            currentLives--
        }

        // Gato Ability
        if (currentLives <= 0 && activePet == "gato" && isPetFed && _uiState.value.gameType != GameType.TIME_ATTACK) {
            currentLives = 1
            isPetFed = false
            userPreferences.setPetFed("gato", false)
            _uiState.value = _uiState.value.copy(isPetFed = false)
        }

        val newTimeLeft = if (_uiState.value.gameType == GameType.TIME_ATTACK) {
            val t = (_uiState.value.timeLeft ?: 0) - 3
            if (t < 0) 0 else t
        } else {
            _uiState.value.timeLeft
        }

        updateScore(penalty)
        _uiState.value = _uiState.value.copy(
            score = _uiState.value.score + penalty,
            lives = currentLives,
            mistakes = _uiState.value.mistakes + 1,
            timeLeft = newTimeLeft
        )

        if (currentLives <= 0 || newTimeLeft == 0) {
            finishGame(false)
        } else {
            viewModelScope.launch {
                kotlinx.coroutines.delay(1200)
                generateQuestion()
            }
        }
    }

    fun useOwlAbility() {
        val state = _uiState.value
        if (state.activePet == "buho" && state.isPetFed && state.currentQuestion != null) {
            val incorrectOptions = state.currentQuestion.options.filter { it != state.currentQuestion.correctOption }.shuffled().take(2)
            // In Compose we might need to update the options list or add a "hidden" flag to options.
            // For simplicity, let's filter them out in the current question state.
            val newOptions = state.currentQuestion.options.filter { it !in incorrectOptions }
            _uiState.value = _uiState.value.copy(
                currentQuestion = state.currentQuestion.copy(options = newOptions),
                isPetFed = false
            )
            userPreferences.setPetFed("buho", false)
        }
    }

    private fun updateScore(increment: Int) {
        userPreferences.getScore { currentScore ->
            userPreferences.setScore(currentScore + increment)
        }
    }

    private fun finishGame(victory: Boolean) {
        val endTime = System.currentTimeMillis()
        val elapsed = (endTime - startTime) / 1000
        _uiState.value = _uiState.value.copy(
            isGameOver = true,
            isVictory = victory,
            timeElapsed = elapsed
        )
        
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        userScoreManager.saveUserScore(userId, _uiState.value.score, {}, {})

        // Estadísticas y Logros
        userPreferences.incrementTotalGames()
        if (victory && _uiState.value.mistakes == 0) {
            userPreferences.incrementPerfectGames()
        }

        userPreferences.getStats { total, perfect ->
            if (victory) userPreferences.unlockAchievement("first_win") {}
            if (total >= 10) userPreferences.unlockAchievement("veteran_10") {}
            if (total >= 50) userPreferences.unlockAchievement("veteran_50") {}
            if (perfect >= 1) userPreferences.unlockAchievement("perfect_1") {}
            if (perfect >= 5) userPreferences.unlockAchievement("perfect_5") {}
        }

        val correctAnswers = _uiState.value.currentQuestionIndex
        userPreferences.addCorrectAnswers(correctAnswers)
        if (_uiState.value.gameType == GameType.SURVIVAL) {
            userPreferences.updateSurvivalMaxScore(correctAnswers)
        } else if (_uiState.value.gameType == GameType.TIME_ATTACK) {
            userPreferences.updateTimeAttackMaxScore(correctAnswers)
        }

        userPreferences.getAdvancedStats { correct, sMax, tMax ->
            if (correct >= 100) userPreferences.unlockAchievement("erudito_100") {}
            if (sMax >= 20) userPreferences.unlockAchievement("survival_20") {}
            if (tMax >= 15) userPreferences.unlockAchievement("time_attack_15") {}
        }
    }
}
