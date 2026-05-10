package alragar2.isi3.uv.flagflash.galeria

import alragar2.isi3.uv.flagflash.musica.MusicService
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels

class GaleriaActivity : ComponentActivity() {
    private val viewModel: GaleriaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GaleriaScreen(
                viewModel = viewModel,
                onNavigateBack = { finish() }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Iniciar el servicio de música
        val musicIntent = Intent(this, MusicService::class.java)
        startService(musicIntent)
    }
}
