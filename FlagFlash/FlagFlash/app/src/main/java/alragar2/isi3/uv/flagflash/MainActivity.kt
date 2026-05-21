package alragar2.isi3.uv.flagflash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.google.firebase.auth.FirebaseAuth
import alragar2.isi3.uv.flagflash.musica.MusicService
import alragar2.isi3.uv.flagflash.navigation.FlagFlashNavGraph
import alragar2.isi3.uv.flagflash.navigation.NavRoutes
import alragar2.isi3.uv.flagflash.ui.theme.FlagFlashTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Allow drawing behind system bars for full-bleed backgrounds
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Determine start destination based on auth state
        val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
        val startDest = if (isLoggedIn) NavRoutes.MAIN else NavRoutes.LOGIN

        InterstitialAdManager.loadAd(this)

        setContent {
            FlagFlashTheme {
                FlagFlashNavGraph(startDestination = startDest)
            }
        }

        // Start background music
        startService(Intent(this, MusicService::class.java))
    }

    override fun onResume() {
        super.onResume()
        startService(Intent(this, MusicService::class.java))
    }
}