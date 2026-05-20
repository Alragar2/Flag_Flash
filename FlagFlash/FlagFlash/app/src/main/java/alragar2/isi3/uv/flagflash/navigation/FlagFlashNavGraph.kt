package alragar2.isi3.uv.flagflash.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import alragar2.isi3.uv.flagflash.UserPreferences
import alragar2.isi3.uv.flagflash.TiendaScreen
import alragar2.isi3.uv.flagflash.authentication.LoginScreen
import alragar2.isi3.uv.flagflash.authentication.RegisterScreen
import alragar2.isi3.uv.flagflash.composables.MainScreen
import alragar2.isi3.uv.flagflash.galeria.GaleriaScreen
import alragar2.isi3.uv.flagflash.galeria.GaleriaViewModel
import alragar2.isi3.uv.flagflash.juego.MultiGameMode
import alragar2.isi3.uv.flagflash.juego.MultijugadorLocalScreen
import alragar2.isi3.uv.flagflash.juego.MultijugadorLocalViewModel
import alragar2.isi3.uv.flagflash.juego.compose.GameMode
import alragar2.isi3.uv.flagflash.juego.compose.GameScreen
import alragar2.isi3.uv.flagflash.juego.compose.GameViewModel
import alragar2.isi3.uv.flagflash.online.*
import alragar2.isi3.uv.flagflash.ranking.RankingScreen
import alragar2.isi3.uv.flagflash.resultado.*

@Composable
fun FlagFlashNavGraph(startDestination: String) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }

    NavHost(navController = navController, startDestination = startDestination) {

        // ── Auth ──────────────────────────────────────────────────────────
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavRoutes.MAIN) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(NavRoutes.REGISTER) }
            )
        }
        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.MAIN) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // ── Main ──────────────────────────────────────────────────────────
        composable(NavRoutes.MAIN) {
            MainScreen(
                userPreferences = userPreferences,
                onJugar           = { navController.navigate(NavRoutes.ELEGIR_JUGAR) },
                onMultijugador    = { navController.navigate(NavRoutes.ELEGIR_MULTI) },
                onGaleria         = { navController.navigate(NavRoutes.GALERIA) },
                onRanking         = { navController.navigate(NavRoutes.RANKING) },
                onTienda          = { navController.navigate(NavRoutes.TIENDA) },
                onLogros          = { navController.navigate(NavRoutes.LOGROS) },
                onLogout          = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.MAIN) { inclusive = true }
                    }
                }
            )
        }

        // ── Solo game selection ───────────────────────────────────────────
        composable(NavRoutes.ELEGIR_JUGAR) {
            ElegirJugarScreen(
                userPreferences = userPreferences,
                onGameModeSelected = { mode, continent, type, questions ->
                    navController.navigate(NavRoutes.game(mode, continent, type, questions))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.GAME,
            arguments = listOf(
                navArgument("gameMode") { type = NavType.StringType },
                navArgument("continent") { type = NavType.StringType },
                navArgument("gameType") { type = NavType.StringType },
                navArgument("questions") { type = NavType.StringType }
            )
        ) { back ->
            val modeStr = back.arguments?.getString("gameMode") ?: "BANDERA"
            val continent = back.arguments?.getString("continent") ?: "Todos"
            val typeStr = back.arguments?.getString("gameType") ?: "NORMAL"
            val questions = back.arguments?.getString("questions") ?: "15"
            val mode = GameMode.valueOf(modeStr)
            val gameType = alragar2.isi3.uv.flagflash.juego.compose.GameType.valueOf(typeStr)
            val vm: GameViewModel = viewModel()
            LaunchedEffect(mode, typeStr, questions) { vm.initGame(mode, continent, gameType, questions) }
            GameScreen(
                viewModel = vm,
                mode = mode,
                onNavigateBack = { navController.popBackStack() },
                onGameFinished = { score, victory, timeElapsed, mistakes ->
                    if (victory) {
                        navController.navigate(NavRoutes.victoriaInd(score, timeElapsed, mistakes, modeStr, continent, typeStr, questions)) {
                            popUpTo(NavRoutes.GAME) { inclusive = true }
                        }
                    } else {
                        navController.navigate(NavRoutes.derrotaInd(score, modeStr, continent, typeStr, questions)) {
                            popUpTo(NavRoutes.GAME) { inclusive = true }
                        }
                    }
                }
            )
        }

        // ── Solo results ──────────────────────────────────────────────────
        composable(
            route = NavRoutes.VICTORIA_IND,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("timeElapsed") { type = NavType.LongType },
                navArgument("mistakes") { type = NavType.IntType },
                navArgument("originMode") { type = NavType.StringType },
                navArgument("continent") { type = NavType.StringType },
                navArgument("gameType") { type = NavType.StringType },
                navArgument("questions") { type = NavType.StringType }
            )
        ) { back ->
            val score = back.arguments?.getInt("score") ?: 0
            val timeElapsed = back.arguments?.getLong("timeElapsed") ?: 0L
            val mistakes = back.arguments?.getInt("mistakes") ?: 0
            val originMode = back.arguments?.getString("originMode") ?: "BANDERA"
            val continent = back.arguments?.getString("continent") ?: "Todos"
            val gameType = back.arguments?.getString("gameType") ?: "NORMAL"
            val questions = back.arguments?.getString("questions") ?: "15"
            VictoriaIndividualScreen(
                score = score, timeElapsed = timeElapsed, mistakes = mistakes,
                originMode = originMode, continent = continent, userPreferences = userPreferences,
                onPlayAgain = {
                    navController.navigate(NavRoutes.game(originMode, continent, gameType, questions)) {
                        popUpTo(NavRoutes.MAIN)
                    }
                },
                onMainMenu = { navController.navigate(NavRoutes.MAIN) { popUpTo(NavRoutes.MAIN) { inclusive = true } } }
            )
        }

        composable(
            route = NavRoutes.DERROTA_IND,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("originMode") { type = NavType.StringType },
                navArgument("continent") { type = NavType.StringType },
                navArgument("gameType") { type = NavType.StringType },
                navArgument("questions") { type = NavType.StringType }
            )
        ) { back ->
            val score = back.arguments?.getInt("score") ?: 0
            val originMode = back.arguments?.getString("originMode") ?: "BANDERA"
            val continent = back.arguments?.getString("continent") ?: "Todos"
            val gameType = back.arguments?.getString("gameType") ?: "NORMAL"
            val questions = back.arguments?.getString("questions") ?: "15"
            DerrotaIndividualScreen(
                score = score, originMode = originMode, continent = continent,
                onPlayAgain = {
                    navController.navigate(NavRoutes.game(originMode, continent, gameType, questions)) {
                        popUpTo(NavRoutes.MAIN)
                    }
                },
                onMainMenu = { navController.navigate(NavRoutes.MAIN) { popUpTo(NavRoutes.MAIN) { inclusive = true } } }
            )
        }

        // ── Multiplayer selection ─────────────────────────────────────────
        composable(NavRoutes.ELEGIR_MULTI) {
            ElegirMultijugarScreen(
                onLocalMode = { mode -> navController.navigate(NavRoutes.multiLocal(mode)) },
                onOnlineMultiplayer = { navController.navigate(NavRoutes.ONLINE_MENU) },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Local multiplayer game ────────────────────────────────────────
        composable(
            route = NavRoutes.MULTI_LOCAL,
            arguments = listOf(navArgument("gameMode") { type = NavType.StringType })
        ) { back ->
            val modeStr = back.arguments?.getString("gameMode") ?: "BANDERA"
            val mode = MultiGameMode.valueOf(modeStr)
            val vm: MultijugadorLocalViewModel = viewModel()
            MultijugadorLocalScreen(
                viewModel = vm, mode = mode,
                onGameFinished = { p1, p2 ->
                    navController.navigate(NavRoutes.victoriaMJ(p1, p2, modeStr)) {
                        popUpTo(NavRoutes.ELEGIR_MULTI)
                    }
                }
            )
        }

        composable(
            route = NavRoutes.VICTORIA_MJ,
            arguments = listOf(
                navArgument("player1Score") { type = NavType.IntType },
                navArgument("player2Score") { type = NavType.IntType },
                navArgument("gameMode") { type = NavType.StringType }
            )
        ) { back ->
            val p1 = back.arguments?.getInt("player1Score") ?: 0
            val p2 = back.arguments?.getInt("player2Score") ?: 0
            val mode = back.arguments?.getString("gameMode") ?: "BANDERA"
            VictoriaMJScreen(
                player1Score = p1, player2Score = p2, gameMode = mode,
                onPlayAgain = { navController.navigate(NavRoutes.multiLocal(mode)) { popUpTo(NavRoutes.ELEGIR_MULTI) } },
                onMainMenu = { navController.navigate(NavRoutes.MAIN) { popUpTo(NavRoutes.MAIN) { inclusive = true } } }
            )
        }

        // ── Galería ───────────────────────────────────────────────────────
        composable(NavRoutes.GALERIA) {
            val vm: GaleriaViewModel = viewModel()
            GaleriaScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() })
        }

        // ── Ranking ───────────────────────────────────────────────────────
        composable(NavRoutes.RANKING) {
            RankingScreen(onBack = { navController.popBackStack() })
        }

        // ── Tienda ────────────────────────────────────────────────────────
        composable(NavRoutes.TIENDA) {
            TiendaScreen(userPreferences = userPreferences, onBack = { navController.popBackStack() })
        }

        // ── Logros ────────────────────────────────────────────────────────
        composable(NavRoutes.LOGROS) {
            alragar2.isi3.uv.flagflash.resultado.LogrosScreen(userPreferences = userPreferences, onBack = { navController.popBackStack() })
        }

        // ── Online multiplayer ────────────────────────────────────────────
        composable(NavRoutes.ONLINE_MENU) {
            OnlineMenuScreen(
                onCreateRoom = { navController.navigate(NavRoutes.CREATE_ROOM) },
                onJoinRoom = { navController.navigate(NavRoutes.JOIN_ROOM) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.CREATE_ROOM) {
            val vm: OnlineGameViewModel = viewModel()
            CreateRoomScreen(
                viewModel = vm,
                onRoomCreated = { code -> navController.navigate(NavRoutes.waitingRoom(code)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.JOIN_ROOM) {
            val vm: OnlineGameViewModel = viewModel()
            JoinRoomScreen(
                viewModel = vm,
                onJoined = { code -> navController.navigate(NavRoutes.waitingRoom(code)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.WAITING_ROOM,
            arguments = listOf(navArgument("roomCode") { type = NavType.StringType })
        ) { back ->
            val code = back.arguments?.getString("roomCode") ?: ""
            val vm: OnlineGameViewModel = viewModel()
            WaitingRoomScreen(
                viewModel = vm,
                onGameStarted = { navController.navigate(NavRoutes.onlineGame(code)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.ONLINE_GAME,
            arguments = listOf(navArgument("roomCode") { type = NavType.StringType })
        ) { back ->
            val code = back.arguments?.getString("roomCode") ?: ""
            val vm: OnlineGameViewModel = viewModel()
            OnlineGameScreen(
                viewModel = vm,
                onGameOver = {
                    navController.navigate(NavRoutes.onlineResult(code)) {
                        popUpTo(NavRoutes.WAITING_ROOM) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = NavRoutes.ONLINE_RESULT,
            arguments = listOf(navArgument("roomCode") { type = NavType.StringType })
        ) {
            val vm: OnlineGameViewModel = viewModel()
            OnlineResultScreen(
                viewModel = vm,
                onMainMenu = { navController.navigate(NavRoutes.MAIN) { popUpTo(NavRoutes.MAIN) { inclusive = true } } }
            )
        }
    }
}
