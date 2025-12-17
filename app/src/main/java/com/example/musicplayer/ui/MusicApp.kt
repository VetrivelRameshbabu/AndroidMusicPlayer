package com.example.musicplayer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
                AnimatedVisibility(
                    visible = viewModel.currentSong != null && !showPlayer,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
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
            }
        ) { padding ->
            val bottomPadding = if (viewModel.currentSong != null && !showPlayer) 64.dp else 0.dp

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

                AnimatedVisibility(
                    visible = showPlayer,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    PlayerScreen(
                        currentSong = viewModel.currentSong!!,
                        isPlaying = viewModel.isPlaying,
                        onPlayPause = viewModel::togglePlayPause,
                        onBack = { showPlayer = false },
                        onNext = viewModel::skipNext,
                        onPrevious = viewModel::skipPrevious,
                        isShuffleEnabled = viewModel.isShuffleEnabled,
                        repeatMode = viewModel.repeatMode,
                        onShuffleClick = viewModel::toggleShuffle,
                        onRepeatClick = viewModel::cycleRepeatMode
                    )
                }
            }
        }
    }
}
