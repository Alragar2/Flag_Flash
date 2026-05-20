package alragar2.isi3.uv.flagflash.composables

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import alragar2.isi3.uv.flagflash.UserPreferences
import alragar2.isi3.uv.flagflash.ui.theme.*

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

    LaunchedEffect(Unit) {
        userPreferences.getSelectedPet { selectedPet = it }
        userPreferences.getOwnedPets { ownedPets = it }
        userPreferences.getFoodCount { foodCount = it }
        userPreferences.getSelectedPet { pet ->
            pet?.let {
                userPreferences.isPetFed(it) { fed ->
                    fedStates = fedStates.toMutableMap().also { m -> m[it] = fed }
                }
            }
        }
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
                    .background(SurfaceOverlay)
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
                        val emoji = when (petId) {
                            "buho" -> "🦉"; "gato" -> "🐱"; "tortuga" -> "🐢"; else -> "🐾"
                        }
                        val isSelected = selectedPet == petId
                        val isFed = fedStates[petId] == true
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .shadow(if (isSelected) 6.dp else 2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(if (isSelected) DeepSkyBlue else SurfaceOverlay)
                                .padding(12.dp)
                                .clickable {
                                    userPreferences.setSelectedPet(petId)
                                    selectedPet = petId
                                }
                        ) {
                            Text(emoji, fontSize = 28.sp)
                            if (isFed) Text("✅", fontSize = 12.sp)
                        }
                    }
                }

                // Feed button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Comida: 🍖 x$foodCount", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
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
