package com.example.musicplayer.data

import android.net.Uri

data class AudioFile(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val uri: Uri,
    val duration: Long,
    val albumId: Long,
    val albumArtUri: Uri?
)
