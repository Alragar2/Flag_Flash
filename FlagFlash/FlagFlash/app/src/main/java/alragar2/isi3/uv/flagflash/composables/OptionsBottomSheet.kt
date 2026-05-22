package alragar2.isi3.uv.flagflash.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import alragar2.isi3.uv.flagflash.R
import alragar2.isi3.uv.flagflash.ShopRegistry
import alragar2.isi3.uv.flagflash.UserPreferences
import alragar2.isi3.uv.flagflash.ui.theme.*
import android.content.Intent
import alragar2.isi3.uv.flagflash.musica.MusicService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsBottomSheet(
    userState: MainUserState,
    userPreferences: UserPreferences,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedPet by remember { mutableStateOf<String?>(null) }
    var ownedPets by remember { mutableStateOf<List<String>>(emptyList()) }
    var fedStates by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var foodCount by remember { mutableStateOf(0) }
    var isMusicEnabled by remember { mutableStateOf(userPreferences.isMusicEnabled()) }
    var isSoundEnabled by remember { mutableStateOf(userPreferences.isSoundEffectsEnabled()) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        userPreferences.getSelectedPet { selectedPet = it }
        userPreferences.getOwnedPets { ownedPets = it }
        userPreferences.getFoodCount { foodCount = it }
        userPreferences.getPetFedStates { fedStates = it }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BgLight,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Mi perfil",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            HorizontalDivider(color = SkyBlue.copy(alpha = 0.4f))

            // User info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(userState.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Text("Puntuación: ${userState.score}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(userState.coins.toString(), fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
                }
            }

            // Pets section
            if (ownedPets.isNotEmpty()) {
                Text("Mascotas", fontWeight = FontWeight.Bold, color = TextSecondary, style = MaterialTheme.typography.titleLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ownedPets.forEach { petId ->
                        val imageRes = ShopRegistry.getPetImage(petId) ?: R.drawable.buho
                        val isSelected = selectedPet == petId
                        val isFed = fedStates[petId] == true
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .shadow(if (isSelected) 6.dp else 2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(if (isSelected) DeepSkyBlue else Color.White)
                                .clickable {
                                    userPreferences.setSelectedPet(petId)
                                    selectedPet = petId
                                }
                                .padding(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = petId,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (isFed) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(2.dp),
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    Text("✅", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                // Feed section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.alimento),
                            contentDescription = "Comida",
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Comida: x$foodCount", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                            selectedPet?.let { pet ->
                                val name = ShopRegistry.getPetName(pet)
                                val fed = fedStates[pet] == true
                                Text(
                                    text = if (fed) "$name: ACTIVA" else "$name: HAMBRIENTA",
                                    color = if (fed) GreenCorrect else Color(0xFFF97316),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Button(
                        onClick = {
                            val pet = selectedPet
                            if (pet != null && foodCount > 0 && fedStates[pet] != true) {
                                userPreferences.setFoodCount(foodCount - 1)
                                userPreferences.setPetFed(pet, true)
                                foodCount -= 1
                                fedStates = fedStates.toMutableMap().also { it[pet] = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenCorrect),
                        enabled = selectedPet != null && foodCount > 0 && fedStates[selectedPet] != true
                    ) { Text("Alimentar", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }

            HorizontalDivider(color = SkyBlue.copy(alpha = 0.4f))

            // Sound settings section
            Text(
                text = "Ajustes de Sonido",
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                style = MaterialTheme.typography.titleLarge
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Background Music Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isMusicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                            contentDescription = "Música de fondo",
                            tint = if (isMusicEnabled) DeepSkyBlue else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Música de fondo",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    }
                    Switch(
                        checked = isMusicEnabled,
                        onCheckedChange = { checked ->
                            isMusicEnabled = checked
                            userPreferences.setMusicEnabled(checked)
                            val intent = Intent(context, MusicService::class.java).apply {
                                action = if (checked) "PLAY" else "PAUSE"
                            }
                            context.startService(intent)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DeepSkyBlue,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }

                // Sound Effects Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Efectos de sonido",
                            tint = if (isSoundEnabled) DeepSkyBlue else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Efectos de sonido",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    }
                    Switch(
                        checked = isSoundEnabled,
                        onCheckedChange = { checked ->
                            isSoundEnabled = checked
                            userPreferences.setSoundEffectsEnabled(checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DeepSkyBlue,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }
            }

            HorizontalDivider(color = SkyBlue.copy(alpha = 0.4f))

            // Logout
            Button(
                onClick = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = RedWrong),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
