package alragar2.isi3.uv.flagflash.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val FlagFlashColorScheme = lightColorScheme(
    primary          = DeepSkyBlue,
    onPrimary        = TextOnBlue,
    primaryContainer = SkyBlue,
    onPrimaryContainer = TextPrimary,
    secondary        = DarkSkyBlue,
    onSecondary      = TextOnBlue,
    background       = BgLight,
    onBackground     = TextPrimary,
    surface          = Surface,
    onSurface        = TextPrimary,
    surfaceVariant   = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    error            = RedWrong,
    onError          = TextOnBlue
)

@Composable
fun FlagFlashTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FlagFlashColorScheme,
        typography  = FlagFlashTypography,
        content     = content
    )
}
