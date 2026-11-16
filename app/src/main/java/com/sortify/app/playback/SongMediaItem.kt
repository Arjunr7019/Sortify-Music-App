package com.sortify.app.playback

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.sortify.app.data.AudioSource
import com.sortify.app.data.MusicProvider
import com.sortify.app.data.Song
import org.json.JSONObject
import java.io.File
import java.net.URI

internal fun Song.playbackUrl(quality: String, downloads: List<Song>): String? {
    val local = (downloads.filter { it.id == id } + this).firstOrNull { song ->
        runCatching { song.localPath.isNotBlank() && File(URI(song.localPath)).isFile }.getOrDefault(false)
    }
    return local?.localPath ?: if (provider == MusicProvider.YouTubeMusic)
        "https://music.youtube.com/watch?v=${id.removePrefix("yt:")}&sortify_quality=$quality"
    else copy(localPath = "").audioUrl(quality)
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
internal fun Song.toMediaItem(quality: String, downloads: List<Song>, mediaId: String = id): MediaItem? {
    val url = playbackUrl(quality, downloads) ?: return null
    return MediaItem.Builder().setMediaId(mediaId).setUri(url).setMediaMetadata(
        MediaMetadata.Builder().setTitle(title).setArtist(artist).setAlbumTitle(album)
            .setArtworkUri(image.takeIf { it.startsWith("https://") }?.let(Uri::parse))
            .setDurationMs(duration.takeIf { it > 0 }?.times(1000))
            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
            .setIsBrowsable(false).setIsPlayable(true)
            .setExtras(Bundle().apply { putString(SONG_EXTRA, json().toString()) }).build()
    ).build()
}

internal fun MediaItem.toSong(quality: String): Song = runCatching {
    mediaMetadata.extras?.getString(SONG_EXTRA)?.let { Song.fromJson(JSONObject(it)) }
}.getOrNull() ?: Song(
    id = mediaId,
    title = mediaMetadata.title?.toString().orEmpty(),
    artist = mediaMetadata.artist?.toString().orEmpty(),
    album = mediaMetadata.albumTitle?.toString().orEmpty(),
    image = mediaMetadata.artworkUri?.toString().orEmpty(),
    sources = localConfiguration?.uri?.toString()?.let { listOf(AudioSource(quality, it)) }.orEmpty(),
    provider = if (mediaId.startsWith("yt:")) MusicProvider.YouTubeMusic else MusicProvider.JioSaavn,
)

private const val SONG_EXTRA = "com.sortify.app.SONG"
