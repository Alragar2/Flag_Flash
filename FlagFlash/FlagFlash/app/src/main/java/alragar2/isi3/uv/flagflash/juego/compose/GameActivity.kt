package alragar2.isi3.uv.flagflash.juego.compose

import alragar2.isi3.uv.flagflash.resultado.DerrotaIndividualActivity
import alragar2.isi3.uv.flagflash.resultado.VictoriaIndividualActivity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect

class GameActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val modeString = intent.getStringExtra("gameMode") ?: "BANDERA"
        val continent = intent.getStringExtra("selectedContinent") ?: "Todos"
        val mode = GameMode.valueOf(modeString)

        viewModel.initGame(mode, continent)

        setContent {
            GameScreen(
                viewModel = viewModel,
                mode = mode,
                onNavigateBack = { finish() },
                onGameFinished = { score, victory, timeElapsed, mistakes ->
                    val intent = if (victory) {
                        Intent(this, VictoriaIndividualActivity::class.java)
                    } else {
                        Intent(this, DerrotaIndividualActivity::class.java)
                    }
                    
                    val originActivity = when(mode) {
                        GameMode.BANDERA -> "JuegoBanderaActivity"
                        GameMode.PAIS -> "JuegoPaisActivity"
                        GameMode.CAPITAL -> "JuegoCapitalActivity"
                        GameMode.ESCUDO -> "JuegoEscudoActivity"
                    }
                    
                    intent.putExtra("originActivity", originActivity)
                    intent.putExtra("selectedContinent", continent)
                    intent.putExtra("timeElapsed", timeElapsed)
                    intent.putExtra("mistakes", mistakes)
                    intent.putExtra("score", score)
                    
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}
