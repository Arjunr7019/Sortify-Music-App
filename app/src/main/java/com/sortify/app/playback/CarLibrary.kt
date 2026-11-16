package com.sortify.app.playback

import com.sortify.app.data.Playlist
import com.sortify.app.data.Song
import java.util.Base64

/** A fresh snapshot is loaded for every browse so phone edits are immediately visible. */
internal data class CarLibrary(
    val recent: List<Song>,
    val favorites: List<Song>,
    val downloads: List<Song>,
    val playlists: List<Playlist>,
    val discovery: List<Song>,
) {
    fun folders(parentId: String): List<Pair<String, String>>? = when (parentId) {
        ROOT -> listOf(HOME to "Home", FAVORITES to "Liked songs", DOWNLOADS to "Downloads", PLAYLISTS to "Playlists")
        PLAYLISTS -> playlists.map { playlistId(it.id) to it.name }
        else -> null
    }

    fun songs(parentId: String): List<Song>? = when (parentId) {
        HOME -> (recent + discovery).distinctBy { it.id }
        FAVORITES -> favorites
        DOWNLOADS -> downloads
        else -> playlists.firstOrNull { playlistId(it.id) == parentId }?.songs
    }

    fun find(id: String): Song? = (downloads + recent + favorites + playlists.flatMap { it.songs } + discovery)
        .firstOrNull { it.id == id }

    companion object {
        const val ROOT = "sortify:root"
        const val HOME = "sortify:home"
        const val FAVORITES = "sortify:favorites"
        const val DOWNLOADS = "sortify:downloads"
        const val PLAYLISTS = "sortify:playlists"
        fun playlistId(id: String) = "sortify:playlist/$id"
    }
}

/** Keep browse context in IDs so selecting a song can populate its whole queue. */
internal object CarMediaId {
    private const val PREFIX = "sortify:track/"
    fun encode(parent: String, songId: String): String = PREFIX + encodePart(parent) + "/" + encodePart(songId)
    fun decode(id: String): Pair<String, String>? = runCatching {
        if (!id.startsWith(PREFIX)) return null
        val parts = id.removePrefix(PREFIX).split('/')
        if (parts.size != 2) return null
        decodePart(parts[0]) to decodePart(parts[1])
    }.getOrNull()

    private fun encodePart(value: String) = Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(Charsets.UTF_8))
    private fun decodePart(value: String) = String(Base64.getUrlDecoder().decode(value), Charsets.UTF_8)
}

internal fun <T> List<T>.libraryPage(page: Int, pageSize: Int): List<T> {
    if (page < 0 || pageSize <= 0) return emptyList()
    val start = page.toLong() * pageSize
    if (start >= size) return emptyList()
    return subList(start.toInt(), (start + pageSize).coerceAtMost(size.toLong()).toInt())
}
