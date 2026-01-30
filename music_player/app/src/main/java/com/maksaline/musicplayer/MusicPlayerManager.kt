package com.maksaline.musicplayer

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer

class MusicPlayerManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var trackList: List<String> = emptyList()
    private var currentTrackIndex: Int = 0
    private var isPaused: Boolean = false

    var onTrackChanged: ((trackName: String, index: Int, total: Int) -> Unit)? = null
    var onPlaybackStateChanged: ((PlaybackState) -> Unit)? = null
    var onPlaybackCompleted: (() -> Unit)? = null

    enum class PlaybackState {
        PLAYING, PAUSED, STOPPED
    }

    init {
        loadTrackList()
    }

    private fun loadTrackList() {
        try {
            val assetManager = context.assets
            val files = assetManager.list("") ?: emptyArray()
            trackList = files.filter { it.endsWith(".mp3", ignoreCase = true) }.sorted()
        } catch (e: Exception) {
            e.printStackTrace()
            trackList = emptyList()
        }
    }

    fun getCurrentTrackName(): String {
        return if (trackList.isNotEmpty() && currentTrackIndex in trackList.indices) {
            trackList[currentTrackIndex].removeSuffix(".mp3")
        } else {
            "No track"
        }
    }


    fun getCurrentTrackNumber(): Int = currentTrackIndex + 1


    fun getTotalTracks(): Int = trackList.size

    fun play() {
        if (trackList.isEmpty()) return

        if (isPaused && mediaPlayer != null) {
            mediaPlayer?.start()
            isPaused = false
            onPlaybackStateChanged?.invoke(PlaybackState.PLAYING)
            return
        }

        prepareAndPlay()
    }

    private fun prepareAndPlay() {
        if (trackList.isEmpty() || currentTrackIndex !in trackList.indices) return

        try {
            releasePlayer()

            val trackFileName = trackList[currentTrackIndex]
            val afd: AssetFileDescriptor = context.assets.openFd(trackFileName)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                prepare()
                start()

                setOnCompletionListener {
                    onPlaybackCompleted?.invoke()
                    playNext()
                }
            }

            afd.close()
            isPaused = false
            onPlaybackStateChanged?.invoke(PlaybackState.PLAYING)
            onTrackChanged?.invoke(getCurrentTrackName(), getCurrentTrackNumber(), getTotalTracks())

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPaused = true
                onPlaybackStateChanged?.invoke(PlaybackState.PAUSED)
            }
        }
    }

    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying || isPaused) {
                it.stop()
            }
        }
        releasePlayer()
        isPaused = false
        onPlaybackStateChanged?.invoke(PlaybackState.STOPPED)
    }


    fun playNext() {
        if (trackList.isEmpty()) return
        currentTrackIndex = (currentTrackIndex + 1) % trackList.size
        prepareAndPlay()
    }


    fun playPrevious() {
        if (trackList.isEmpty()) return
        currentTrackIndex = if (currentTrackIndex - 1 < 0) {
            trackList.size - 1
        } else {
            currentTrackIndex - 1
        }
        prepareAndPlay()
    }


//    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true


    private fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }


    fun release() {
        releasePlayer()
    }
}
