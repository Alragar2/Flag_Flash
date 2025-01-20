package alragar2.isi3.uv.flagflash

import alragar2.isi3.uv.flagflash.R
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

class MusicService : Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private var pausePosition: Int = 0

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.flags_soundtrack)
        mediaPlayer.isLooping = true
        mediaPlayer.setVolume(0.5f, 0.5f)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.seekTo(pausePosition)
            mediaPlayer.start()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun pauseMusic() {
        if (mediaPlayer.isPlaying) {
            pausePosition = mediaPlayer.currentPosition
            mediaPlayer.pause()
        }
    }

    fun resumeMusic() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.seekTo(pausePosition)
            mediaPlayer.start()
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }
}