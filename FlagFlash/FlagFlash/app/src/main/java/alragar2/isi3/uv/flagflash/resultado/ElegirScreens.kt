package alragar2.isi3.uv.flagflash.resultado

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import alragar2.isi3.uv.flagflash.ui.components.FlagFlashButton
import alragar2.isi3.uv.flagflash.ui.theme.*
import alragar2.isi3.uv.flagflash.UserPreferences

@Composable
fun ElegirJugarScreen(
    userPreferences: UserPreferences,
    onGameModeSelected: (mode: String, continent: String, type: String, questions: String) -> Unit,
    onBack: () -> Unit
) {
    var unlocked by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(Unit) {
        userPreferences.getUnlockedAchievements { unlocked = it }
    }

    val hasFirstWin = unlocked.contains("first_win")
    val hasVeteran10 = unlocked.contains("veteran_10")
    val hasVeteran50 = unlocked.contains("veteran_50")
    val hasPerfect1 = unlocked.contains("perfect_1")
    val hasPerfect5 = unlocked.contains("perfect_5")

    var selectedContinent by remember { mutableStateOf("Europa") }
    var selectedType by remember { mutableStateOf("NORMAL") }
    var selectedQuestions by remember { mutableStateOf("10") }

    val continents = listOf(
        Triple("Europa", "🏰", true) to "",
        Triple("América", "🗽", true) to "",
        Triple("África", "🦁", hasFirstWin) to "1 victoria",
        Triple("Asia", "🏯", hasVeteran10) to "10 partidas",
        Triple("Oceanía", "🦘", hasVeteran50) to "50 partidas",
        Triple("Todos", "🌍", hasPerfect5) to "5 p. perfectas"
    )

    val gameTypes = listOf(
        Triple("NORMAL", "Clásico", true) to "",
        Triple("TIME_ATTACK", "Contrarreloj", hasVeteran10) to "10 partidas",
        Triple("SURVIVAL", "Supervivencia", hasPerfect1) to "1 p. perfecta"
    )

    val questionCounts = listOf(
        Triple("10", "10", true) to "",
        Triple("15", "15", hasFirstWin) to "1 victoria",
        Triple("20", "20", hasVeteran10) to "10 partidas",
        Triple("50", "50", hasVeteran50) to "50 partidas",
        Triple("infinity", "Infinito", hasPerfect5) to "5 p. perfectas"
    )

    val gameModes = listOf(
        Triple("🚩", "Bandera", "BANDERA"),
        Triple("🗺️", "País", "PAIS"),
        Triple("🏛️", "Escudos", "ESCUDO"),
        Triple("🏙️", "Capitales", "CAPITAL")
    )

    val bgGradient = Brush.verticalGradient(listOf(SkyBlue, BgLight))

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
                text = "Seleccionar modo",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Número de Preguntas
            Text(
                text = "Nº de Preguntas",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(questionCounts) { (item, req) ->
                    val (id, label, isUnlocked) = item
                    SelectableCard(
                        text = label,
                        isSelected = selectedQuestions == id,
                        isUnlocked = isUnlocked,
                        unlockReq = req,
                        onClick = { if (isUnlocked) selectedQuestions = id }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tipo de Juego
            Text(
                text = "Tipo de juego",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(gameTypes) { (item, req) ->
                    val (id, label, isUnlocked) = item
                    val icon = when(id) { "TIME_ATTACK" -> "⏱️"; "SURVIVAL" -> "❤️"; else -> "🧠" }
                    SelectableCard(
                        text = "$icon $label",
                        isSelected = selectedType == id,
                        isUnlocked = isUnlocked,
                        unlockReq = req,
                        onClick = { if (isUnlocked) selectedType = id }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Continentes
            Text(
                text = "Filtrar por continente",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(continents) { (item, req) ->
                    val (continent, emoji, isUnlocked) = item
                    SelectableCard(
                        text = "$emoji $continent",
                        isSelected = selectedContinent == continent,
                        isUnlocked = isUnlocked,
                        unlockReq = req,
                        onClick = { if (isUnlocked) selectedContinent = continent }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Modo de juego",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                gameModes.forEach { (emoji, label, mode) ->
                    GameModeCard(
                        emoji = emoji,
                        label = label,
                        onClick = { onGameModeSelected(mode, selectedContinent, selectedType, selectedQuestions) }
                    )
                }
            }
        }
    }
}

@Composable
fun SelectableCard(text: String, isSelected: Boolean, isUnlocked: Boolean, unlockReq: String? = null, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = 50.dp)
            .shadow(if (isSelected) 8.dp else 2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(if (!isUnlocked) Color.LightGray else if (isSelected) DeepSkyBlue else SurfaceOverlay)
            .clickable(enabled = isUnlocked, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isUnlocked) {
                Text("🔒", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = text,
                    color = if (!isUnlocked) Color.DarkGray else if (isSelected) Color.White else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (!isUnlocked && !unlockReq.isNullOrBlank()) {
                    Text(
                        text = "Req: $unlockReq",
                        color = Color.DarkGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ElegirMultijugarScreen(
    onLocalMode: (mode: String) -> Unit,
    onOnlineMultiplayer: () -> Unit,
    onBack: () -> Unit
) {
    val bgGradient = Brush.verticalGradient(listOf(SkyBlue, BgLight))

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
                text = "Multijugador",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Online section
            Text("🌐 Online", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF818CF8), Color(0xFF4F46E5))))
                    .clickable(onClick = onOnlineMultiplayer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🌐", fontSize = 36.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Multijugador Online", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, fontFamily = NunitoFamily)
                        Text("Crea o únete a una sala con amigos", fontSize = 13.sp, color = Color.White.copy(0.85f), fontFamily = NunitoFamily)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Badge(containerColor = Gold) { Text("NUEVO", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold) }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Local section
            Text("📱 Local (mismo dispositivo)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            val localModes = listOf(Triple("🚩", "Banderas", "BANDERA"), Triple("🗺️", "País", "PAIS"), Triple("🏙️", "Capitales", "CAPITAL"))
            localModes.forEach { (emoji, label, mode) ->
                GameModeCard(emoji = emoji, label = label, onClick = { onLocalMode(mode) })
            }
        }
    }
}

@Composable
private fun GameModeCard(emoji: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceOverlay)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 32.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = NunitoFamily)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = TextSecondary)
    }
}
