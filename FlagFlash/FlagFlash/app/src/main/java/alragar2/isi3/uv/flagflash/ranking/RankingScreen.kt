package alragar2.isi3.uv.flagflash.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import alragar2.isi3.uv.flagflash.UserScoreManager
import alragar2.isi3.uv.flagflash.ui.theme.*
import kotlinx.coroutines.launch

data class Player(val name: String, val score: Int, val avatar: String = "default", val frame: String = "none")

@Composable
fun PlayerAvatar(avatar: String, frame: String, size: androidx.compose.ui.unit.Dp = 40.dp) {
    val avatarEmoji = when (avatar) {
        "ninja" -> "🥷"
        "robot" -> "🤖"
        "king" -> "👑"
        else -> "👽"
    }
    val frameBorder = when (frame) {
        "bronze" -> androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFCD7F32))
        "gold" -> androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD700))
        "fire" -> androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF4500))
        else -> null
    }

    var modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(Color.White)
    if (frameBorder != null) {
        modifier = modifier.border(frameBorder, CircleShape)
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(avatarEmoji, fontSize = (size.value * 0.6).sp)
    }
}

@Composable
fun RankingScreen(onBack: () -> Unit) {
    var players by remember { mutableStateOf<List<Player>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            players = UserScoreManager().getTopPlayers(20)
            isLoading = false
        }
    }

    val bgGradient = Brush.verticalGradient(listOf(Color(0xFFFBBF24), BgLight))

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
                text = "🏆 Ranking",
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
            // Podium for top 3
            if (players.size >= 3) {
                PodiumSection(players = players.take(3))
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Rest of players
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(players.drop(3)) { index, player ->
                    PlayerRow(position = index + 4, player = player)
                }
            }
        }
    }
}

@Composable
private fun PodiumSection(players: List<Player>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Top 3", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            PodiumItem(player = players[1], medal = "🥈", height = 90.dp, bgColor = Silver)
            PodiumItem(player = players[0], medal = "🥇", height = 120.dp, bgColor = Gold)
            PodiumItem(player = players[2], medal = "🥉", height = 70.dp, bgColor = Bronze)
        }
    }
}

@Composable
private fun PodiumItem(player: Player, medal: String, height: androidx.compose.ui.unit.Dp, bgColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(medal, fontSize = 28.sp)
        PlayerAvatar(avatar = player.avatar, frame = player.frame, size = 36.dp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(player.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, textAlign = TextAlign.Center, maxLines = 1)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(player.score.toString(), fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
private fun PlayerRow(position: Int, player: Player) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("#$position", fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.width(36.dp))
            PlayerAvatar(avatar = player.avatar, frame = player.frame, size = 32.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(player.name, fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
        }
        Text("⭐ ${player.score}", fontWeight = FontWeight.ExtraBold, color = DeepSkyBlue)
    }
}
