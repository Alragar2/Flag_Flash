package alragar2.isi3.uv.flagflash

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class PetDefinition(
    val id: String,
    val name: String,
    val description: String,
    val price: Int = 2000,
    val imageResId: Int
)

data class AvatarDefinition(
    val id: String,
    val name: String,
    val price: Int,
    val emoji: String
)

data class FrameDefinition(
    val id: String,
    val name: String,
    val price: Int,
    val emoji: String,
    val borderColor: Color?
)

object ShopRegistry {
    val pets = listOf(
        PetDefinition("buho", "Búho", "Elimina 2 opciones incorrectas", imageResId = R.drawable.buho),
        PetDefinition("gato", "Gato", "Segunda oportunidad al perder", imageResId = R.drawable.gatito),
        PetDefinition("tortuga", "Tortuga", "Anula la penalización de un fallo", imageResId = R.drawable.tortuguita)
    )

    val avatars = listOf(
        AvatarDefinition("default", "Alien", 0, "👽"),
        AvatarDefinition("ninja", "Ninja", 500, "🥷"),
        AvatarDefinition("robot", "Robot", 500, "🤖"),
        AvatarDefinition("pirate", "Pirata", 1000, "🏴‍☠️"),
        AvatarDefinition("king", "Rey", 2000, "👑")
    )

    val frames = listOf(
        FrameDefinition("none", "Ninguno", 0, "⬛", null),
        FrameDefinition("bronze", "Marco de Bronce", 1000, emoji = "🥉", Color(0xFFCD7F32)),
        FrameDefinition("silver", "Marco de Plata", 1500, emoji = "🥈", Color(0xFF808080)),
        FrameDefinition("gold", "Marco de Oro", 2500, emoji = "🥇", Color(0xFFFFD700)),
        FrameDefinition("diamond", "Marco de Diamante", 2000, emoji = "💎", Color(0xFF03A9F4)),
        FrameDefinition("fire", "Marco de Fuego", 5000, emoji = "🔥", Color(0xFFFF4500)),
        FrameDefinition("water", "Marco de Agua", 5000, emoji = "💧", Color(0xFF00BFFF)),
    )

    fun getPetImage(petId: String?): Int? {
        return pets.find { it.id == petId }?.imageResId
    }

    fun getPetName(petId: String?): String {
        return pets.find { it.id == petId }?.name ?: (petId?.replaceFirstChar { it.uppercase() } ?: "")
    }

    fun getAvatarEmoji(avatarId: String): String {
        return avatars.find { it.id == avatarId }?.emoji ?: "👽"
    }

    fun getFrameBorder(frameId: String): BorderStroke? {
        val color = frames.find { it.id == frameId }?.borderColor ?: return null
        return BorderStroke(2.dp, color)
    }
}
