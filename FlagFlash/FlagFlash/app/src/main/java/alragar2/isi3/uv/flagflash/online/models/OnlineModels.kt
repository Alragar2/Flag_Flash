package alragar2.isi3.uv.flagflash.online.models

data class OnlinePlayer(
    val uid: String = "",
    val name: String = "",
    val score: Int = 0,
    val answeredCurrentQuestion: Boolean = false,
    val avatar: String = "default",
    val frame: String = "none"
)

data class OnlineRoom(
    val roomCode: String = "",
    val hostUid: String = "",
    val gameMode: String = "BANDERA",
    val status: String = "WAITING",   // WAITING | IN_PROGRESS | FINISHED
    val questionIndex: Int = 0,
    val totalQuestions: Int = 15,
    val players: Map<String, OnlinePlayer> = emptyMap()
)

data class OnlineQuestion(
    val promptImageUrl: String? = null,
    val promptText: String? = null,
    val options: List<String> = emptyList(),
    val correctOption: String = ""
)
