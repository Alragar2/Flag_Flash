package alragar2.isi3.uv.flagflash.juego.compose

enum class GameMode {
    BANDERA, PAIS, CAPITAL, ESCUDO
}

data class Option(
    val text: String? = null,
    val imageUrl: String? = null
)

data class Question(
    val promptText: String? = null,
    val promptImageUrl: String? = null,
    val options: List<Option>,
    val correctOption: Option
)

data class GameState(
    val score: Int = 0,
    val lives: Int = 3,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 10,
    val currentQuestion: Question? = null,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val activePet: String? = null,
    val isPetFed: Boolean = false,
    val timeElapsed: Long = 0,
    val mistakes: Int = 0,
    val isLoading: Boolean = true,
    val selectedOption: Option? = null,
    val showResultTick: Boolean? = null // true = correct, false = wrong, null = none
)
