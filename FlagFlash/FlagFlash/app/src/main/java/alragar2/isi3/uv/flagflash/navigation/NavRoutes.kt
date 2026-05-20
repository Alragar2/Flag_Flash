package alragar2.isi3.uv.flagflash.navigation

object NavRoutes {
    const val LOGIN         = "login"
    const val REGISTER      = "register"
    const val MAIN          = "main"
    const val ELEGIR_JUGAR  = "elegir_jugar"
    const val ELEGIR_MULTI  = "elegir_multijugador"
    const val GALERIA       = "galeria"
    const val RANKING       = "ranking"
    const val TIENDA        = "tienda"
    const val LOGROS        = "logros"

    // Juego individual
    const val GAME          = "game/{gameMode}/{continent}/{gameType}/{questions}"
    fun game(mode: String, continent: String, type: String, questions: String) = "game/$mode/$continent/$type/$questions"

    // Resultados individuales
    const val VICTORIA_IND  = "victoria_ind/{score}/{timeElapsed}/{mistakes}/{originMode}/{continent}/{gameType}/{questions}"
    fun victoriaInd(score: Int, timeElapsed: Long, mistakes: Int, originMode: String, continent: String, gameType: String, questions: String) =
        "victoria_ind/$score/$timeElapsed/$mistakes/$originMode/$continent/$gameType/$questions"

    const val DERROTA_IND   = "derrota_ind/{score}/{originMode}/{continent}/{gameType}/{questions}"
    fun derrotaInd(score: Int, originMode: String, continent: String, gameType: String, questions: String) =
        "derrota_ind/$score/$originMode/$continent/$gameType/$questions"

    // Multijugador local
    const val MULTI_LOCAL   = "multi_local/{gameMode}"
    fun multiLocal(mode: String) = "multi_local/$mode"

    const val VICTORIA_MJ   = "victoria_mj/{player1Score}/{player2Score}/{gameMode}"
    fun victoriaMJ(p1: Int, p2: Int, mode: String) = "victoria_mj/$p1/$p2/$mode"

    // Multijugador online
    const val ONLINE_MENU   = "online_menu"
    const val CREATE_ROOM   = "create_room"
    const val JOIN_ROOM     = "join_room"
    const val WAITING_ROOM  = "waiting_room/{roomCode}"
    fun waitingRoom(code: String) = "waiting_room/$code"

    const val ONLINE_GAME   = "online_game/{roomCode}"
    fun onlineGame(code: String) = "online_game/$code"

    const val ONLINE_RESULT = "online_result/{roomCode}"
    fun onlineResult(code: String) = "online_result/$code"
}
