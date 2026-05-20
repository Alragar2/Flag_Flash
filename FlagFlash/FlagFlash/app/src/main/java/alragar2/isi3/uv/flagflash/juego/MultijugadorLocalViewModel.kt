package alragar2.isi3.uv.flagflash.juego

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class MultiGameMode { BANDERA, PAIS, CAPITAL }

data class MultiQuestion(
    val promptImageUrl: String? = null,
    val promptText: String? = null,
    val options: List<String> = emptyList(),
    val correctOption: String = ""
)

data class MultiLocalState(
    val isLoading: Boolean = true,
    val question: MultiQuestion? = null,
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val correctGuesses: Int = 0,
    val totalQuestions: Int = 15,
    val selectedByP1: String? = null,
    val selectedByP2: String? = null,
    val showResult: Boolean = false,
    val isCorrectP1: Boolean? = null,
    val isCorrectP2: Boolean? = null,
    val isGameOver: Boolean = false
)

class MultijugadorLocalViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(MultiLocalState())
    val state: StateFlow<MultiLocalState> = _state.asStateFlow()

    private val db = FirebaseDatabase.getInstance(
        "https://flag-flash-tfg-default-rtdb.europe-west1.firebasedatabase.app/"
    ).reference

    private var allCountries: List<Map<String, Any>> = emptyList()
    private val usedCountries = mutableSetOf<String>()
    private lateinit var mode: MultiGameMode

    fun initGame(gameMode: MultiGameMode) {
        mode = gameMode
        viewModelScope.launch {
            loadCountries()
            nextQuestion()
        }
    }

    private suspend fun loadCountries() {
        val snapshot = db.child("paises").get().await()
        allCountries = snapshot.children.mapNotNull { it.value as? Map<String, Any> }
    }

    fun nextQuestion() {
        if (_state.value.correctGuesses >= _state.value.totalQuestions) {
            _state.value = _state.value.copy(isGameOver = true)
            return
        }
        val pool = allCountries.filter { c ->
            val key = c["nombre"] as? String ?: return@filter false
            key !in usedCountries
        }
        if (pool.size < 4) {
            _state.value = _state.value.copy(isGameOver = true)
            return
        }
        val chosen = pool.shuffled().take(4)
        val correct = chosen.random()
        val correctName = correct["nombre"] as? String ?: ""
        usedCountries.add(correctName)

        val q = when (mode) {
            MultiGameMode.BANDERA -> MultiQuestion(
                promptImageUrl = correct["bandera"] as? String,
                options = chosen.map { it["nombre"] as? String ?: "" }.shuffled(),
                correctOption = correctName
            )
            MultiGameMode.PAIS -> MultiQuestion(
                promptText = correctName,
                options = chosen.map { it["bandera"] as? String ?: "" }.shuffled(),
                correctOption = correct["bandera"] as? String ?: ""
            )
            MultiGameMode.CAPITAL -> MultiQuestion(
                promptImageUrl = correct["bandera"] as? String,
                promptText = correctName,
                options = chosen.map { it["capital"] as? String ?: "" }.shuffled(),
                correctOption = correct["capital"] as? String ?: ""
            )
        }
        _state.value = _state.value.copy(
            question = q,
            isLoading = false,
            selectedByP1 = null,
            selectedByP2 = null,
            showResult = false,
            isCorrectP1 = null,
            isCorrectP2 = null
        )
    }

    fun player1Answer(option: String) {
        if (_state.value.selectedByP1 != null) return
        val correct = option == _state.value.question?.correctOption
        val newScore = if (correct) _state.value.player1Score + 1 else _state.value.player1Score
        _state.value = _state.value.copy(
            selectedByP1 = option,
            player1Score = newScore,
            isCorrectP1 = correct
        )
        checkBothAnswered()
    }

    fun player2Answer(option: String) {
        if (_state.value.selectedByP2 != null) return
        val correct = option == _state.value.question?.correctOption
        val newScore = if (correct) _state.value.player2Score + 1 else _state.value.player2Score
        _state.value = _state.value.copy(
            selectedByP2 = option,
            player2Score = newScore,
            isCorrectP2 = correct
        )
        checkBothAnswered()
    }

    private fun checkBothAnswered() {
        val s = _state.value
        if (s.selectedByP1 != null && s.selectedByP2 != null) {
            val newCorrect = s.correctGuesses + 1
            _state.value = _state.value.copy(showResult = true, correctGuesses = newCorrect)
            viewModelScope.launch {
                delay(1500)
                if (newCorrect >= s.totalQuestions) {
                    _state.value = _state.value.copy(isGameOver = true)
                } else {
                    nextQuestion()
                }
            }
        }
    }
}
