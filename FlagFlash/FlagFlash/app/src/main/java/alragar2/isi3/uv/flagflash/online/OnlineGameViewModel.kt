package alragar2.isi3.uv.flagflash.online

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import alragar2.isi3.uv.flagflash.online.models.OnlinePlayer
import alragar2.isi3.uv.flagflash.online.models.OnlineQuestion
import alragar2.isi3.uv.flagflash.online.models.OnlineRoom
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class OnlineGameUiState(
    val isLoading: Boolean = false,
    val roomCode: String = "",
    val room: OnlineRoom? = null,
    val currentQuestion: OnlineQuestion? = null,
    val myUid: String = "",
    val selectedOption: String? = null,
    val showResult: Boolean = false,
    val isCorrect: Boolean? = null,
    val errorMsg: String = "",
    val isGameOver: Boolean = false,
    val allCountries: List<Map<String, Any>> = emptyList()
)

class OnlineGameViewModel(application: Application) : AndroidViewModel(application) {

    val repo = RoomRepository()

    private val _state = MutableStateFlow(OnlineGameUiState(myUid = repo.myUid))
    val state: StateFlow<OnlineGameUiState> = _state.asStateFlow()

    // ── Room creation/joining ─────────────────────────────────────────────

    fun createRoom(gameMode: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMsg = "")
            runCatching {
                val code = repo.createRoom(gameMode)
                _state.value = _state.value.copy(isLoading = false, roomCode = code)
                observeRoom(code)
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false, errorMsg = it.message ?: "Error")
            }
        }
    }

    fun joinRoom(code: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMsg = "")
            runCatching {
                val ok = repo.joinRoom(code.uppercase().trim())
                if (ok) {
                    _state.value = _state.value.copy(isLoading = false, roomCode = code.uppercase().trim())
                    observeRoom(code.uppercase().trim())
                } else {
                    _state.value = _state.value.copy(isLoading = false, errorMsg = "Sala llena, no encontrada o ya iniciada")
                }
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false, errorMsg = it.message ?: "Error")
            }
        }
    }

    private fun observeRoom(code: String) {
        viewModelScope.launch {
            repo.observeRoom(code).collect { room ->
                _state.value = _state.value.copy(room = room)
                if (room?.status == "IN_PROGRESS") {
                    observeQuestion(code)
                }
                if (room?.status == "FINISHED") {
                    _state.value = _state.value.copy(isGameOver = true)
                }
            }
        }
    }

    private fun observeQuestion(code: String) {
        viewModelScope.launch {
            repo.observeQuestion(code)
                .distinctUntilChanged()
                .collect { q ->
                    _state.value = _state.value.copy(
                        currentQuestion = q,
                        selectedOption = null,
                        showResult = false,
                        isCorrect = null
                    )
                }
        }
    }

    // ── Host actions ──────────────────────────────────────────────────────

    fun startGame() {
        val code = _state.value.roomCode
        val room = _state.value.room ?: return
        viewModelScope.launch {
            val countries = repo.loadCountries()
            _state.value = _state.value.copy(allCountries = countries)
            repo.startGame(code, countries, room.gameMode)
        }
    }

    // ── Answer submission ─────────────────────────────────────────────────

    fun submitAnswer(option: String) {
        if (_state.value.selectedOption != null) return
        val q = _state.value.currentQuestion ?: return
        val isCorrect = option == q.correctOption
        val code = _state.value.roomCode
        val room = _state.value.room ?: return

        _state.value = _state.value.copy(
            selectedOption = option,
            showResult = true,
            isCorrect = isCorrect
        )

        viewModelScope.launch {
            repo.submitAnswer(code, room.questionIndex, option, isCorrect)

            // If host, check if all answered and advance
            if (repo.myUid == room.hostUid) {
                delay(1800)
                checkAndAdvance()
            }
        }
    }

    private suspend fun checkAndAdvance() {
        val code = _state.value.roomCode
        val room = _state.value.room ?: return
        val allAnswered = room.players.values.all { it.answeredCurrentQuestion }
        if (allAnswered) {
            repo.advanceQuestion(
                code,
                _state.value.allCountries,
                room.gameMode,
                room.questionIndex + 1,
                room.totalQuestions
            )
        } else {
            // Timeout fallback: wait max 15s then advance anyway
            delay(13_000)
            repo.advanceQuestion(
                code,
                _state.value.allCountries,
                room.gameMode,
                room.questionIndex + 1,
                room.totalQuestions
            )
        }
    }

    fun leaveRoom() {
        viewModelScope.launch {
            repo.leaveRoom(_state.value.roomCode)
        }
    }

    override fun onCleared() {
        super.onCleared()
        leaveRoom()
    }
}
