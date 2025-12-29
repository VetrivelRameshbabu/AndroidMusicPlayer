package com.example.musicplayer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicplayer.ui.theme.MusicPlayerTheme

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MusicApp(
    viewModel: MainViewModel
) {
    var showPlayer by remember { mutableStateOf(false) }

    MusicPlayerTheme {
        Scaffold(
            bottomBar = {
                // No standard bottom bar, just the MiniPlayer
            }
        ) { padding ->
             // Calculate padding for the list so it scrolls behind the mini player but last item is visible
            val bottomPadding = if (viewModel.currentSong != null && !showPlayer) 90.dp else 0.dp

            Box(modifier = Modifier.fillMaxSize()) {
                SongListScreen(
                    audioFiles = viewModel.audioFiles,
                    onSongClick = {
                        viewModel.playAudio(it)
                        showPlayer = true
                    },
                    searchQuery = viewModel.searchQuery,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    contentPadding = PaddingValues(bottom = bottomPadding)
                )

                // Floating Mini Player
                val miniPlayerVisible = viewModel.currentSong != null && !showPlayer
                AnimatedVisibility(
                    visible = miniPlayerVisible,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    MiniPlayer(
                        currentSong = viewModel.currentSong!!,
                        isPlaying = viewModel.isPlaying,
                        onPlayPause = viewModel::togglePlayPause,
                        onNext = viewModel::skipNext,
                        onPrevious = viewModel::skipPrevious,
                        onExpand = { showPlayer = true }
                    )
                }

                // Full Screen Player
                AnimatedVisibility(
                    visible = showPlayer,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    PlayerScreen(
                        currentSong = viewModel.currentSong!!,
                        isPlaying = viewModel.isPlaying,
                        currentPosition = viewModel.currentPosition,
                        onPlayPause = viewModel::togglePlayPause,
                        onBack = { showPlayer = false },
                        onNext = viewModel::skipNext,
                        onPrevious = viewModel::skipPrevious,
                        isShuffleEnabled = viewModel.isShuffleEnabled,
                        repeatMode = viewModel.repeatMode,
                        onShuffleClick = viewModel::toggleShuffle,
                        onRepeatClick = viewModel::cycleRepeatMode,
                        onSeek = viewModel::seekTo
                    )
                }
            }
        }
    }
}
