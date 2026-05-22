package alragar2.isi3.uv.flagflash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import alragar2.isi3.uv.flagflash.ui.components.FlagFlashButton
import alragar2.isi3.uv.flagflash.ui.theme.*

@Composable
fun TiendaScreen(
    userPreferences: UserPreferences,
    onBack: () -> Unit
) {
    var currentCoins by remember { mutableStateOf(0) }
    var foodCount by remember { mutableStateOf(0) }
    var selectedPet by remember { mutableStateOf<String?>(null) }
    var ownedPets by remember { mutableStateOf<List<String>>(emptyList()) }
    var fedStates by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    var selectedTab by remember { mutableStateOf("Mascotas") }
    var currentAvatar by remember { mutableStateOf("default") }
    var currentFrame by remember { mutableStateOf("none") }
    var ownedCosmetics by remember { mutableStateOf<List<String>>(emptyList()) }

    fun reload() {
        userPreferences.getCoins { currentCoins = it }
        userPreferences.getFoodCount { foodCount = it }
        userPreferences.getSelectedPet { selectedPet = it }
        userPreferences.getOwnedPets { ownedPets = it }
        userPreferences.getPetFedStates { fedStates = it }
        userPreferences.getAvatar { currentAvatar = it }
        userPreferences.getFrame { currentFrame = it }
        userPreferences.getOwnedCosmetics { ownedCosmetics = it }
    }

    LaunchedEffect(Unit) { reload() }

    val bgGradient = Brush.verticalGradient(listOf(Color(0xFFFB923C), BgLight))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                }
                Text("🛒 Tienda", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
            }
            Row(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🪙", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text("$currentCoins", fontWeight = FontWeight.ExtraBold, color = TextPrimary, fontSize = 16.sp)
            }
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tabs
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(
                    selected = selectedTab == "Mascotas",
                    onClick = { selectedTab = "Mascotas" },
                    label = { Text("🐾 Mascotas", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = DeepSkyBlue, selectedLabelColor = Color.White)
                )
                FilterChip(
                    selected = selectedTab == "Cosméticos",
                    onClick = { selectedTab = "Cosméticos" },
                    label = { Text("🖼️ Cosméticos", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF8B5CF6), selectedLabelColor = Color.White)
                )
            }

            if (selectedTab == "Mascotas") {
                // Food section
                Column(
                    modifier = Modifier
                        .shadow(6.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.alimento),
                            contentDescription = "Comida",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Comida para mascotas", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Stock: x$foodCount", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                        FlagFlashButton(
                            text = "Comprar (50 🪙)",
                            onClick = {
                                if (currentCoins >= 50) {
                                    currentCoins -= 50; foodCount += 1
                                    userPreferences.setCoins(currentCoins); userPreferences.setFoodCount(foodCount)
                                }
                            },
                            enabled = currentCoins >= 50,
                            modifier = Modifier.width(180.dp),
                            height = 44.dp,
                            gradientStart = Color(0xFFFBBF24),
                            gradientEnd = Color(0xFFF59E0B)
                        )
                    }
                }

                // Pets section
                Text("Mascotas", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)

                ShopRegistry.pets.forEach { pet ->
                    PetCard(
                        petId = pet.id,
                        petName = pet.name,
                        description = pet.description,
                        price = pet.price,
                        isOwned = ownedPets.contains(pet.id),
                        isSelected = selectedPet == pet.id,
                        isFed = fedStates[pet.id] == true,
                        coins = currentCoins,
                        onBuy = {
                            if (currentCoins >= pet.price) {
                                currentCoins -= pet.price
                                userPreferences.setCoins(currentCoins)
                                userPreferences.addOwnedPet(pet.id)
                                userPreferences.setSelectedPet(pet.id)
                                userPreferences.setPetFed(pet.id, false)
                                ownedPets = ownedPets + pet.id
                                selectedPet = pet.id
                                fedStates = fedStates.toMutableMap().also { it[pet.id] = false }
                            }
                        },
                        onSelect = {
                            userPreferences.setSelectedPet(pet.id)
                            selectedPet = pet.id
                        }
                    )
                }
            } else {
                // Cosmetics Section
                Text("Avatares", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)

                ShopRegistry.avatars.forEach { avatar ->
                    CosmeticCard(
                        id = avatar.id, name = avatar.name, type = "Avatar", price = avatar.price,
                        isOwned = avatar.id == "default" || ownedCosmetics.contains(avatar.id),
                        isSelected = currentAvatar == avatar.id,
                        coins = currentCoins,
                        onBuy = {
                            if (currentCoins >= avatar.price) {
                                currentCoins -= avatar.price
                                userPreferences.setCoins(currentCoins)
                                userPreferences.addOwnedCosmetic(avatar.id)
                                userPreferences.setAvatar(avatar.id)
                                ownedCosmetics = ownedCosmetics + avatar.id
                                currentAvatar = avatar.id
                            }
                        },
                        onSelect = {
                            userPreferences.setAvatar(avatar.id)
                            currentAvatar = avatar.id
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Marcos de Perfil", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)

                ShopRegistry.frames.forEach { frame ->
                    CosmeticCard(
                        id = frame.id, name = frame.name, type = "Marco", price = frame.price,
                        isOwned = frame.id == "none" || ownedCosmetics.contains(frame.id),
                        isSelected = currentFrame == frame.id,
                        coins = currentCoins,
                        onBuy = {
                            if (currentCoins >= frame.price) {
                                currentCoins -= frame.price
                                userPreferences.setCoins(currentCoins)
                                userPreferences.addOwnedCosmetic(frame.id)
                                userPreferences.setFrame(frame.id)
                                ownedCosmetics = ownedCosmetics + frame.id
                                currentFrame = frame.id
                            }
                        },
                        onSelect = {
                            userPreferences.setFrame(frame.id)
                            currentFrame = frame.id
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PetCard(
    petId: String, petName: String, description: String, price: Int,
    isOwned: Boolean, isSelected: Boolean, isFed: Boolean,
    coins: Int, onBuy: () -> Unit, onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFFE0F2FE) else Color.White)
            .clickable(enabled = isOwned && !isSelected) { onSelect() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val imageRes = ShopRegistry.getPetImage(petId) ?: R.drawable.buho
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = petName,
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(petName, fontWeight = FontWeight.ExtraBold, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                if (isSelected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    val badgeColor = if (isFed) GreenCorrect else Color(0xFFF97316)
                    val badgeText = if (isFed) "ACTIVA" else "HAMBRIENTA"
                    Badge(containerColor = badgeColor) {
                        Text(badgeText, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                if (isFed && isOwned) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("✅", fontSize = 14.sp)
                }
            }
            Text(description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        if (!isOwned) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$price 🪙", fontWeight = FontWeight.ExtraBold, color = Gold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onBuy,
                    enabled = coins >= price,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) { Text("Comprar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            }
        } else if (!isSelected) {
            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(containerColor = DeepSkyBlue),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) { Text("Elegir", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
        }
    }
}

@Composable
private fun CosmeticCard(
    id: String, name: String, type: String, price: Int,
    isOwned: Boolean, isSelected: Boolean,
    coins: Int, onBuy: () -> Unit, onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFFF3E8FF) else Color.White)
            .clickable(enabled = isOwned && !isSelected) { onSelect() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val emoji = when (type) {
            "Avatar" -> ShopRegistry.getAvatarEmoji(id)
            else -> ShopRegistry.frames.find { it.id == id }?.emoji ?: "⬛"
        }
        Text(emoji, fontSize = 40.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, fontWeight = FontWeight.ExtraBold, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                if (isSelected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(containerColor = GreenCorrect) { Text("ACTIVO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold) }
                }
            }
            Text(type, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        if (!isOwned) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$price 🪙", fontWeight = FontWeight.ExtraBold, color = Gold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onBuy,
                    enabled = coins >= price,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) { Text("Comprar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            }
        } else if (!isSelected) {
            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) { Text("Elegir", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
        }
    }
}
