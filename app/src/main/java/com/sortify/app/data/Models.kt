package com.sortify.app.data

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs

data class AudioSource(val quality: String, val url: String)

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val image: String = "",
    val duration: Long = 0,
    val language: String = "",
    val sources: List<AudioSource> = emptyList(),
    val localPath: String = "",
    val bundledArt: String = "",
) {
    fun audioUrl(quality: String): String? = if (localPath.isNotBlank()) localPath else
        sources.filter { it.url.startsWith("https://") }.minByOrNull {
            abs(bitrate(it.quality) - bitrate(quality))
        }?.url

    fun json() = JSONObject().apply {
        put("id", id); put("name", title); put("primaryArtists", artist)
        put("album", JSONObject().put("name", album)); put("duration", duration)
        put("language", language); put("localPath", localPath); put("bundledArt", bundledArt)
        put("image", JSONArray().put(JSONObject().put("url", image)))
        put("downloadUrl", JSONArray(sources.map { JSONObject().put("quality", it.quality).put("url", it.url) }))
    }

    companion object {
        fun fromJson(j: JSONObject): Song {
            val primary = j.optJSONObject("artists")?.optJSONArray("primary")
            val artist = primary?.let { a -> (0 until a.length()).mapNotNull { a.optJSONObject(it)?.optString("name") }.joinToString(", ") }
                ?.takeIf { it.isNotBlank() } ?: j.optString("primaryArtists", "Unknown artist")
            val images = j.optJSONArray("image")
            val urls = j.optJSONArray("downloadUrl") ?: JSONArray()
            return Song(
                id = j.optString("id"), title = decode(j.optString("name", j.optString("title"))),
                artist = decode(artist), album = decode(j.optJSONObject("album")?.optString("name") ?: ""),
                image = images?.optJSONObject((images.length() - 1).coerceAtLeast(0))?.optString("url") ?: "",
                duration = j.optLong("duration"), language = j.optString("language"),
                sources = (0 until urls.length()).mapNotNull { index -> urls.optJSONObject(index)?.let { AudioSource(it.optString("quality"), it.optString("url")) } },
                localPath = j.optString("localPath"), bundledArt = j.optString("bundledArt"),
            )
        }
    }
}

fun bitrate(value: String) = value.filter(Char::isDigit).toIntOrNull() ?: 0
fun decode(value: String) = android.text.Html.fromHtml(value, android.text.Html.FROM_HTML_MODE_LEGACY).toString()
fun JSONArray.songs(): List<Song> = (0 until length()).mapNotNull { optJSONObject(it)?.let(Song::fromJson) }.filter { it.id.isNotBlank() }
fun List<Song>.json() = JSONArray(map(Song::json))
fun timeLabel(seconds: Long): String = "%d:%02d".format(seconds.coerceAtLeast(0) / 60, seconds.coerceAtLeast(0) % 60)

data class Playlist(val id: String, val name: String, val songs: List<Song> = emptyList(), val description: String = "Made by you", val personal: Boolean = true) {
    fun json() = JSONObject().put("id", id).put("name", name).put("songs", songs.json())
}

data class CatalogItem(val id: String, val title: String, val subtitle: String, val image: String, val kind: String)
