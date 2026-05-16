package com.tetraverse.app.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log

class AudioManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private var moveId = 0
    private var rotateId = 0
    private var clearId = 0
    private var overId = 0
    private var currentBgm: MediaPlayer? = null
    private var currentBgmResId: Int = 0

    init {
        try {
            val attr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            soundPool = SoundPool.Builder().setMaxStreams(5).setAudioAttributes(attr).build()
            
            moveId = load("move")
            rotateId = load("rotate")
            clearId = load("clear")
            overId = load("gameover")
        } catch (e: Exception) {
            Log.e("Audio", "SoundPool init failed")
        }
    }

    private fun load(name: String): Int {
        return try {
            val id = context.resources.getIdentifier(name, "raw", context.packageName)
            if (id != 0) soundPool?.load(context, id, 1) ?: 0 else 0
        } catch (e: Exception) { 0 }
    }

    fun playMove() { try { if (moveId != 0) soundPool?.play(moveId, 0.3f, 0.3f, 0, 0, 1f) } catch(e: Exception) {} }
    fun playRotate() { try { if (rotateId != 0) soundPool?.play(rotateId, 0.4f, 0.4f, 0, 0, 1f) } catch(e: Exception) {} }
    fun playClear() { try { if (clearId != 0) soundPool?.play(clearId, 0.6f, 0.6f, 0, 0, 1f) } catch(e: Exception) {} }
    fun playGameOverSfx() { try { if (overId != 0) soundPool?.play(overId, 0.8f, 0.8f, 0, 0, 1f) } catch(e: Exception) {} }

    fun playLobbyMusic() { switchBGM("lobby_music") }
    fun playInGameMusic() { switchBGM("game_music") }
    fun playGameOverMusic() { switchBGM("gameover_music") }

    private fun switchBGM(resName: String) {
        try {
            val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
            if (resId == 0 || resId == currentBgmResId) return

            stopBGM()
            
            // Extremely safe creation
            MediaPlayer.create(context, resId)?.let { player ->
                currentBgm = player
                player.isLooping = true
                player.setVolume(0.2f, 0.2f)
                player.setOnErrorListener { mp, _, _ ->
                    mp.release()
                    if (currentBgm == mp) currentBgm = null
                    true
                }
                player.start()
                currentBgmResId = resId
            }
        } catch (e: Throwable) {
            Log.e("Audio", "BGM switch failed: $resName")
        }
    }

    fun stopBGM() {
        try {
            currentBgm?.let { 
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (e: Exception) {}
        currentBgm = null
        currentBgmResId = 0
    }
    
    fun resumeBGM() {
        try {
            currentBgm?.let { if (!it.isPlaying) it.start() }
        } catch (e: Exception) {}
    }
    
    fun pauseBGM() {
        try {
            currentBgm?.let { if (it.isPlaying) it.pause() }
        } catch (e: Exception) {}
    }
}
