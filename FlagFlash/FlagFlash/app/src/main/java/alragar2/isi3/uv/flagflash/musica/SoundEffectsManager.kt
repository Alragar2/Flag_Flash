package alragar2.isi3.uv.flagflash.musica

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import alragar2.isi3.uv.flagflash.UserPreferences

object SoundEffectsManager {
    fun playSound(context: Context, resId: Int) {
        val userPrefs = UserPreferences(context)
        if (!userPrefs.isSoundEffectsEnabled()) return

        try {
            val mp = MediaPlayer.create(context.applicationContext, resId)
            if (mp != null) {
                mp.setOnCompletionListener { mediaPlayer ->
                    try {
                        mediaPlayer.release()
                    } catch (e: Exception) {
                        Log.e("SoundEffectsManager", "Error releasing MediaPlayer", e)
                    }
                }
                mp.start()
            } else {
                Log.e("SoundEffectsManager", "MediaPlayer.create returned null for resource ID $resId")
            }
        } catch (e: Exception) {
            Log.e("SoundEffectsManager", "Error playing sound effect", e)
        }
    }
}
