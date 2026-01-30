package com.maksaline.musicplayer

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var musicPlayerManager: MusicPlayerManager
    private lateinit var tvTrackName: TextView
    private lateinit var tvTrackIndex: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnStop: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton

    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        musicPlayerManager = MusicPlayerManager(this)

        initViews()

        setupListeners()
        setupMusicPlayerCallbacks()

        updateTrackInfo()
    }

    private fun initViews() {
        tvTrackName = findViewById(R.id.tvTrackName)
        tvTrackIndex = findViewById(R.id.tvTrackIndex)
        tvStatus = findViewById(R.id.tvStatus)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnStop = findViewById(R.id.btnStop)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
    }

    private fun setupListeners() {
        btnPlayPause.setOnClickListener {
            if (isPlaying) {
                musicPlayerManager.pause()
            } else {
                musicPlayerManager.play()
            }
        }

        btnStop.setOnClickListener {
            musicPlayerManager.stop()
        }

        btnPrevious.setOnClickListener {
            musicPlayerManager.playPrevious()
        }

        btnNext.setOnClickListener {
            musicPlayerManager.playNext()
        }
    }

    private fun setupMusicPlayerCallbacks() {
        musicPlayerManager.onTrackChanged = { trackName, index, total ->
            runOnUiThread {
                tvTrackName.text = trackName
                tvTrackIndex.text = getString(R.string.track_format, index, total)
            }
        }

        musicPlayerManager.onPlaybackStateChanged = { state ->
            runOnUiThread {
                when (state) {
                    MusicPlayerManager.PlaybackState.PLAYING -> {
                        isPlaying = true
                        btnPlayPause.setImageResource(R.drawable.ic_pause)
                        tvStatus.text = "Playing"
                    }
                    MusicPlayerManager.PlaybackState.PAUSED -> {
                        isPlaying = false
                        btnPlayPause.setImageResource(R.drawable.ic_play)
                        tvStatus.text = "Paused"
                    }
                    MusicPlayerManager.PlaybackState.STOPPED -> {
                        isPlaying = false
                        btnPlayPause.setImageResource(R.drawable.ic_play)
                        tvStatus.text = "Stopped"
                    }
                }
            }
        }
    }

    private fun updateTrackInfo() {
        tvTrackName.text = musicPlayerManager.getCurrentTrackName()
        val total = musicPlayerManager.getTotalTracks()
        if (total > 0) {
            tvTrackIndex.text = getString(
                R.string.track_format,
                musicPlayerManager.getCurrentTrackNumber(),
                total
            )
        } else {
            tvTrackIndex.text = "No tracks found"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayerManager.release()
    }
}