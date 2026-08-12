package alragar2.isi3.uv.flagflash.composables

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import alragar2.isi3.uv.flagflash.R
import alragar2.isi3.uv.flagflash.UserPreferences
import alragar2.isi3.uv.flagflash.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class MainUserState(
    val name: String = "",
    val score: Int = 0,
    val coins: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userPreferences: UserPreferences,
    onJugar: () -> Unit,
    onMultijugador: () -> Unit,
    onGaleria: () -> Unit,
    onRanking: () -> Unit,
    onTienda: () -> Unit,
    onLogros: () -> Unit,
    onLogout: () -> Unit
) {
    var userState by remember { mutableStateOf(MainUserState()) }
    var showOptionsSheet by remember { mutableStateOf(false) }

    // Load user data
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        val doc = FirebaseFirestore.getInstance().collection("users").document(uid).get().await()
        userState = MainUserState(
            name   = doc.getString("name") ?: "Jugador",
            score  = doc.getLong("score")?.toInt() ?: 0,
            coins  = doc.getLong("coins")?.toInt() ?: 0
        )
    }

    val bgGradient = Brush.verticalGradient(listOf(SkyBlue, BgLight, Color(0xFFD1EEFF)))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "¡Hola, ${userState.name}!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "¿Lista para el reto? 🌍",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                IconButton(
                    onClick = { showOptionsSheet = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceOverlay, RoundedCornerShape(12.dp))
                        .size(48.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Perfil", tint = DeepSkyBlue)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(label = "Puntuación", value = userState.score.toString(), iconRes = R.drawable.points, modifier = Modifier.weight(1f))
                StatCard(label = "Monedas", value = userState.coins.toString(), iconRes = R.drawable.moneda, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Globe pulsing logo
            PulsingGlobe()

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Modos de juego",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main action buttons
            MenuButton(
                label = "Jugar",
                subtitle = "Solo contra el mundo",
                emoji = "🎮",
                gradientStart = Color(0xFF38BDF8),
                gradientEnd = Color(0xFF0284C7),
                onClick = onJugar
            )
            Spacer(modifier = Modifier.height(10.dp))
            MenuButton(
                label = "Multijugador",
                subtitle = "Reta a tus amigos",
                emoji = "👥",
                gradientStart = Color(0xFF818CF8),
                gradientEnd = Color(0xFF4F46E5),
                onClick = onMultijugador
            )
            Spacer(modifier = Modifier.height(10.dp))
            MenuButton(
                label = "Galería",
                subtitle = "Explora tus banderas",
                emoji = "🗂️",
                gradientStart = Color(0xFF34D399),
                gradientEnd = Color(0xFF059669),
                onClick = onGaleria
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallMenuButton(
                    label = "Ranking",
                    emoji = "🏆",
                    onClick = onRanking,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFFBBF24)
                )
                SmallMenuButton(
                    label = "Tienda",
                    emoji = "🛒",
                    onClick = onTienda,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFF97316)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            SmallMenuButton(
                label = "Logros",
                emoji = "🎖️",
                onClick = onLogros,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF8B5CF6)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Options bottom sheet
        if (showOptionsSheet) {
            OptionsBottomSheet(
                userState = userState,
                userPreferences = userPreferences,
                onDismiss = { showOptionsSheet = false },
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    iconRes: Int? = null,
    emoji: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (iconRes != null) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        } else if (emoji != null) {
            Text(text = emoji, fontSize = 28.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
private fun MenuButton(
    label: String,
    subtitle: String,
    emoji: String,
    gradientStart: Color,
    gradientEnd: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .scale(scale)
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(gradientStart, gradientEnd)))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, fontFamily = NunitoFamily)
                Text(text = subtitle, fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f), fontFamily = NunitoFamily)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun SmallMenuButton(
    label: String,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color
) {
    Box(
        modifier = modifier
            .height(76.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 28.sp)
            Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, fontFamily = NunitoFamily)
        }
    }
}

@Composable
private fun PulsingGlobe() {
    val infiniteTransition = rememberInfiniteTransition(label = "globe")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "globeScale"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.icono),
            contentDescription = "Icono App",
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(24.dp))
        )
    }
}
