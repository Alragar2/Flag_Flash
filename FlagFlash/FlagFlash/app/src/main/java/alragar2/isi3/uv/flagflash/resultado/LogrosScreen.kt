package alragar2.isi3.uv.flagflash.resultado

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import alragar2.isi3.uv.flagflash.UserPreferences
import alragar2.isi3.uv.flagflash.ui.theme.*

data class AchievementDef(val id: String, val emoji: String, val name: String, val desc: String)

val ALL_ACHIEVEMENTS = listOf(
    AchievementDef("first_win", "🥉", "Primera Victoria", "Gana tu primera partida."),
    AchievementDef("veteran_10", "🎖️", "Veterano", "Juega 10 partidas."),
    AchievementDef("veteran_50", "🌎", "Trotamundos", "Juega 50 partidas."),
    AchievementDef("perfect_1", "⭐", "Impecable", "Gana una partida sin fallos."),
    AchievementDef("perfect_5", "🌟", "Maestro", "Gana 5 partidas sin fallos."),
    AchievementDef("erudito_100", "📚", "Erudito", "Acierta 100 respuestas en total."),
    AchievementDef("survival_20", "🛡️", "Superviviente Nato", "Llega a 20 aciertos en Supervivencia."),
    AchievementDef("time_attack_15", "⚡", "Rayo", "Llega a 15 aciertos en Contrarreloj.")
)

@Composable
fun LogrosScreen(userPreferences: UserPreferences, onBack: () -> Unit) {
    var unlocked by remember { mutableStateOf<List<String>>(emptyList()) }
    var totalGames by remember { mutableStateOf(0L) }
    var perfectGames by remember { mutableStateOf(0L) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        userPreferences.getStats { t, p ->
            totalGames = t
            perfectGames = p
            userPreferences.getUnlockedAchievements { ach ->
                unlocked = ach
                isLoading = false
            }
        }
    }

    val bgGradient = Brush.verticalGradient(listOf(Color(0xFF8B5CF6), BgLight))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
            }
            Text(
                text = "Logros",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DeepSkyBlue)
            }
        } else {
            // Stats header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatColumn(label = "Jugadas", value = totalGames.toString())
                StatColumn(label = "Perfectas", value = perfectGames.toString())
                StatColumn(label = "Desbloqueados", value = "${unlocked.size}/${ALL_ACHIEVEMENTS.size}")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Achievement List
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ALL_ACHIEVEMENTS) { ach ->
                    val isUnlocked = unlocked.contains(ach.id)
                    AchievementCard(ach = ach, isUnlocked = isUnlocked)
                }
            }
        }
    }
}

@Composable
fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = DeepSkyBlue)
        Text(label, fontSize = 12.sp, color = TextSecondary)
    }
}

@Composable
fun AchievementCard(ach: AchievementDef, isUnlocked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isUnlocked) 6.dp else 1.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(if (isUnlocked) Color.White else Color(0xFFE2E8F0))
            .alpha(if (isUnlocked) 1f else 0.6f)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) Color(0xFFFBBF24).copy(alpha = 0.3f) else Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(if (isUnlocked) ach.emoji else "🔒", fontSize = 28.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(ach.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
            Text(ach.desc, color = TextSecondary, fontSize = 14.sp)
        }
    }
}
