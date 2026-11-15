package com.sortify.app.data

import com.sortify.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

/** Native adapter for the public search/browse/player operations in the supplied ytmusicapi source. */
class YouTubeMusicClient {
    suspend fun searchSongs(query: String, limit: Int = 30) = withContext(Dispatchers.IO) {
        YouTubeMusicParser.songs(search(query, "songs")).take(limit)
    }
    suspend fun searchCatalog(query: String, kind: String) = withContext(Dispatchers.IO) {
        YouTubeMusicParser.catalog(search(query, kind), kind)
    }
    suspend fun catalogSongs(item: CatalogItem): List<Song> = withContext(Dispatchers.IO) {
        val response = request("browse", JSONObject().put("browseId", item.id))
        val initial = YouTubeMusicParser.songs(response)
        if (item.kind != "artists") initial else {
            // Artist pages may expose only a preview; follow the songs shelf's browse endpoint.
            val shelf = YouTubeMusicParser.findObject(response, "musicShelfRenderer")
            val endpoint = YouTubeMusicParser.findObject(shelf?.optJSONObject("bottomEndpoint"), "browseEndpoint")
            if (endpoint == null) initial else YouTubeMusicParser.songs(request("browse", endpoint)).ifEmpty { initial }
        }
    }
    suspend fun trending(): List<Song> = withContext(Dispatchers.IO) {
        val charts = request("browse", JSONObject().put("browseId", "FEmusic_charts")
            .put("formData", JSONObject().put("selectedValues", org.json.JSONArray().put("IN"))))
        val direct = YouTubeMusicParser.songs(charts)
        if (direct.isNotEmpty()) direct.take(20) else {
            val lists = YouTubeMusicParser.catalog(charts, "playlists")
            val chart = lists.firstOrNull { it.title.contains("trending", true) }
                ?: lists.firstOrNull { it.title.contains("top", true) }
                ?: lists.firstOrNull() ?: throw IOException("YouTube Music charts are unavailable. Try again.")
            catalogSongs(chart).take(20).also { if (it.isEmpty()) throw IOException("YouTube Music charts are empty. Try again later.") }
        }
    }
    suspend fun languagePlaylists(language: String) = withContext(Dispatchers.IO) {
        // Featured search can include unrelated supplemental shelves. Only keep language-named
        // playlists, and broaden to community playlists when there are few featured matches.
        val featured = YouTubeMusicParser.catalog(search("$language songs", "featured_playlists"), "playlists")
            .filter { it.title.contains(language, ignoreCase = true) }
        val additional = if (featured.size < 3) YouTubeMusicParser.catalog(search("$language songs playlist", "playlists"), "playlists")
            .filter { it.title.contains(language, ignoreCase = true) } else emptyList()
        (featured + additional).distinctBy { it.id }.take(8)
    }

    private fun search(query: String, kind: String): JSONObject {
        val params = when (kind) {
            "songs" -> "EgWKAQIIAWoMEA4QChADEAQQCRAF"
            "artists" -> "EgWKAQIgAWoMEA4QChADEAQQCRAF"
            "albums" -> "EgWKAQIYAWoMEA4QChADEAQQCRAF"
            "playlists" -> "Eg-KAQwIABAAGAAgACgBMABqChAEEAMQCRAFEAo%3D"
            "featured_playlists" -> "EgeKAQQoADgBagwQDhAKEAMQBBAJEAU%3D"
            else -> error("Unsupported YouTube Music search category")
        }
        return request("search", JSONObject().put("query", query).put("params", params))
    }

    /** Called by Media3's IO thread when opening a track, so expiring URLs are never saved in queues. */
    fun resolveAudio(videoId: String, quality: String): String {
        require(videoId.matches(Regex("[A-Za-z0-9_-]{11}"))) { "Invalid YouTube Music track" }
        val key = "$videoId:$quality"
        streams[key]?.takeIf { it.second > System.currentTimeMillis() }?.let { return it.first }
        val url = YouTubeAudioResolver.resolve(videoId, quality)
        streams[key] = url to (System.currentTimeMillis() + 5 * 60 * 1000)
        return url
    }

    private fun request(path: String, body: JSONObject): JSONObject {
        val version = "1.${LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.BASIC_ISO_DATE)}.01.00"
        body.put("context", JSONObject().put("client", JSONObject().put("clientName", "WEB_REMIX")
            .put("clientVersion", version).put("hl", "en").put("gl", "IN")))
        val connection = URI(BuildConfig.YOUTUBE_MUSIC_API_BASE.trimEnd('/') + "/$path?alt=json").toURL().openConnection() as HttpURLConnection
        connection.requestMethod = "POST"; connection.doOutput = true
        configure(connection)
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        connection.setRequestProperty("Origin", "https://music.youtube.com")
        connection.setRequestProperty("Referer", "https://music.youtube.com/")
        try {
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            if (connection.responseCode !in 200..299) throw IOException("YouTube Music is unavailable (${connection.responseCode}). Try again.")
            val result = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            if (result.has("error")) throw IOException("YouTube Music could not complete the request. Try again.")
            return result
        } finally { connection.disconnect() }
    }
    private fun configure(connection: HttpURLConnection) {
        connection.connectTimeout = 15_000; connection.readTimeout = 20_000
        connection.setRequestProperty("User-Agent", USER_AGENT)
        connection.setRequestProperty("Accept", "*/*")
    }
    companion object {
        const val USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
        private val streams = ConcurrentHashMap<String, Pair<String, Long>>()
    }
}
