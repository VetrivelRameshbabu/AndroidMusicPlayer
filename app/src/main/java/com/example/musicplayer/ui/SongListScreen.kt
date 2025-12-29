package com.example.musicplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.musicplayer.data.AudioFile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListScreen(
    audioFiles: List<AudioFile>,
    onSongClick: (AudioFile) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val groupedSongs = remember(audioFiles) {
        audioFiles.groupBy { it.title.first().uppercaseChar() }.toSortedMap()
    }
    val sortedKeys = remember(groupedSongs) { groupedSongs.keys.toList() }

    fun scrollToLetter(letter: Char) {
        coroutineScope.launch {
            val index = sortedKeys.indexOf(letter)
            if (index != -1) {
                var position = 0
                for (i in 0 until index) {
                    position += groupedSongs.values.elementAt(i).size + 1
                }
                listState.scrollToItem(position)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header
        Text(
            text = "Your Library",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 16.dp)
        )

        // Search Bar
        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                placeholder = { Text("Search songs, artists...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding
            ) {
                if (searchQuery.isNotEmpty()) {
                    items(audioFiles, key = { it.id }) { song ->
                        SongListItem(song = song, onSongClick = onSongClick)
                    }
                } else {
                    groupedSongs.forEach { (letter, songs) ->
                        item {
                            Text(
                                text = letter.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                        items(songs, key = { it.id }) { song ->
                            SongListItem(song = song, onSongClick = onSongClick)
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${audioFiles.size} Songs • Developed By Vetrivel",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            if (searchQuery.isEmpty()) {
                AlphabeticalFastScroller(
                    sortedKeys = sortedKeys,
                    onLetterSelected = { letter -> scrollToLetter(letter) },
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}

@Composable
private fun AlphabeticalFastScroller(
    sortedKeys: List<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    var isDragging by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(12.dp)
                )
                .pointerInput(sortedKeys) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            val y = offset.y.coerceIn(0f, size.height.toFloat() - 1)
                            val index = (y / (size.height.toFloat() / sortedKeys.size))
                                .toInt().coerceIn(0, sortedKeys.lastIndex)
                            val letter = sortedKeys[index]
                            if (selectedLetter != letter) {
                                selectedLetter = letter
                                onLetterSelected(letter)
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            selectedLetter = null
                        },
                        onVerticalDrag = { change, _ ->
                            val y = change.position.y.coerceIn(0f, size.height.toFloat() - 1)
                            val index = (y / (size.height.toFloat() / sortedKeys.size))
                                .toInt().coerceIn(0, sortedKeys.lastIndex)
                            val letter = sortedKeys[index]
                            if (selectedLetter != letter) {
                                selectedLetter = letter
                                onLetterSelected(letter)
                            }
                        }
                    )
                }
        ) {
            sortedKeys.forEach { letter ->
                Text(
                    text = letter.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isDragging && selectedLetter != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(end = 40.dp) // Position it to the left of the scroller
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            ) {
                Text(
                    text = selectedLetter.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }
        }
    }
}


@Composable
fun SongListItem(
    song: AudioFile,
    onSongClick: (AudioFile) -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            // Album Art Placeholder / Image
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(56.dp)
            ) {
                SubcomposeAsyncImage(
                    model = song.albumArtUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    error = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .clickable { onSongClick(song) }
            .padding(vertical = 4.dp)
    )
}