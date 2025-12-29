package com.example.musicplayer.ui

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicplayer.data.AudioFile
import com.example.musicplayer.data.AudioRepository
import com.example.musicplayer.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MainViewModel : ViewModel() {

    private var allAudioFiles by mutableStateOf<List<AudioFile>>(emptyList())

    var searchQuery by mutableStateOf("")
        private set

    val audioFiles: List<AudioFile>
        get() = if (searchQuery.isBlank()) {
            allAudioFiles
        } else {
            allAudioFiles.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true)
            }
        }

    var currentSong by mutableStateOf<AudioFile?>(null)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var isShuffleEnabled by mutableStateOf(false)
        private set

    var repeatMode by mutableStateOf(Player.REPEAT_MODE_ALL)
        private set

    var currentPosition by mutableStateOf(0L)
        private set

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    suspend fun initializeController(context: Context) {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        suspendCancellableCoroutine<Unit> { continuation ->
            mediaControllerFuture?.addListener({
                mediaController = mediaControllerFuture?.get()
                mediaController?.repeatMode = repeatMode
                mediaController?.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlayingParam: Boolean) {
                        isPlaying = isPlayingParam
                        if(isPlayingParam) {
                            viewModelScope.launch {
                                while (isPlaying) {
                                    currentPosition = mediaController?.currentPosition ?: 0L
                                    delay(1000)
                                }
                            }
                        }
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        super.onMediaItemTransition(mediaItem, reason)
                        val id = mediaItem?.mediaId?.toLongOrNull()
                        if (id != null) {
                            currentSong = allAudioFiles.find { it.id == id }
                        }
                    }

                    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                        isShuffleEnabled = shuffleModeEnabled
                    }

                    override fun onRepeatModeChanged(newRepeatMode: Int) {
                        repeatMode = newRepeatMode
                    }
                })
                continuation.resume(Unit)
            }, MoreExecutors.directExecutor())
        }
    }

    suspend fun loadAudioFiles(context: Context) {
        val repository = AudioRepository(context)
        val files = repository.getAudioFiles()
        allAudioFiles = files
        val mediaItems = files.map(::createMediaItem)
        mediaController?.setMediaItems(mediaItems)
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
    }

    fun playAudio(audioFile: AudioFile) {
        val controller = mediaController ?: return
        
        val startingIndex = allAudioFiles.indexOf(audioFile)
        if (startingIndex != -1) {
            controller.seekTo(startingIndex, 0)
            controller.prepare()
            controller.play()
            currentSong = audioFile
        }
    }

    private fun createMediaItem(audioFile: AudioFile): MediaItem {
        return MediaItem.Builder()
            .setMediaId(audioFile.id.toString())
            .setUri(audioFile.uri)
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(audioFile.title)
                    .setArtist(audioFile.artist)
                    .setAlbumTitle(audioFile.album)
                    .setArtworkUri(audioFile.albumArtUri)
                    .build()
            )
            .build()
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun skipNext() {
        mediaController?.seekToNext()
    }

    fun skipPrevious() {
        mediaController?.seekToPrevious()
    }

    fun toggleShuffle() {
       mediaController?.shuffleModeEnabled = !isShuffleEnabled
    }

    fun cycleRepeatMode() {
        val newRepeatMode = when (repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
        mediaController?.repeatMode = newRepeatMode
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        currentPosition = position
    }

    override fun onCleared() {
        super.onCleared()
        mediaControllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
