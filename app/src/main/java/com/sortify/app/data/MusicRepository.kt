package com.sortify.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import com.sortify.app.BuildConfig

class MusicRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("sortify", Context.MODE_PRIVATE)
    val initialSongs: List<Song> = runCatching {
        context.assets.open("catalog.json").bufferedReader().use { JSONArray(it.readText()).songs() }
    }.getOrDefault(emptyList())

    var onboarded: Boolean
        get() = prefs.getBoolean("onboarded", false)
        set(value) { prefs.edit().putBoolean("onboarded", value).apply() }
    var languages: Set<String>
        get() = prefs.getStringSet("languages", setOf("English", "Hindi"))!!.toSet()
        set(value) { prefs.edit().putStringSet("languages", value).apply() }
    var quality: String
        get() = prefs.getString("quality", "160kbps")!!
        set(value) { prefs.edit().putString("quality", value).apply() }
    var theme: String
        get() = prefs.getString("theme", "Dark")!!
        set(value) { prefs.edit().putString("theme", value).apply() }

    fun loadSongs(key: String): List<Song> = runCatching { JSONArray(prefs.getString(key, "[]")).songs() }.getOrDefault(emptyList())
    fun saveSongs(key: String, songs: List<Song>) { prefs.edit().putString(key, songs.json().toString()).apply() }
    fun loadPlaylists(): List<Playlist> = runCatching {
        val a = JSONArray(prefs.getString("playlists", "[]"))
        (0 until a.length()).map { a.getJSONObject(it).let { p -> Playlist(p.getString("id"), p.getString("name"), p.getJSONArray("songs").songs()) } }
    }.getOrDefault(emptyList())
    fun savePlaylists(playlists: List<Playlist>) { prefs.edit().putString("playlists", JSONArray(playlists.map(Playlist::json)).toString()).apply() }

    private suspend fun request(path: String): JSONObject = withContext(Dispatchers.IO) {
        val connection = URI(BuildConfig.MUSIC_API_BASE.trimEnd('/') + path).toURL().openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000; connection.readTimeout = 20_000
        connection.setRequestProperty("Accept", "application/json")
        try {
            check(connection.responseCode in 200..299) { "Music service is unavailable (${connection.responseCode}). Try again." }
            val text = connection.inputStream.bufferedReader().use { it.readText() }
            check(text.trimStart().startsWith("{")) { "The music service returned an unexpected response. Try again later." }
            JSONObject(text).also { check(it.optBoolean("success", true)) { "The music service could not complete this request." } }
        } finally { connection.disconnect() }
    }

    suspend fun searchSongs(query: String, limit: Int = 30): List<Song> {
        val data = request("/search/songs?query=${encode(query)}&limit=$limit").optJSONObject("data")
        return data?.optJSONArray("results")?.songs().orEmpty().distinctBy { it.title.lowercase() to it.artist.lowercase() }
    }

    suspend fun searchCatalog(query: String, kind: String): List<CatalogItem> {
        val a = request("/search/$kind?query=${encode(query)}&limit=20").optJSONObject("data")?.optJSONArray("results") ?: JSONArray()
        return (0 until a.length()).map { a.getJSONObject(it) }.map { j ->
            val images = j.optJSONArray("image")
            CatalogItem(j.optString("id"), decode(j.optString("name", j.optString("title"))),
                decode(j.optString("description", j.optString("year", kind.removeSuffix("s").replaceFirstChar(Char::uppercase)))),
                images?.optJSONObject((images.length() - 1).coerceAtLeast(0))?.optString("url") ?: "", kind)
        }
    }

    suspend fun catalogSongs(item: CatalogItem): List<Song> {
        val path = if (item.kind == "artists") "/artists/${encode(item.id)}/songs?limit=30" else "/${item.kind}?id=${encode(item.id)}"
        val data = request(path).optJSONObject("data")
        return (data?.optJSONArray("songs") ?: data?.optJSONArray("results"))?.songs().orEmpty()
    }

    suspend fun download(song: Song, quality: String): Song = withContext(Dispatchers.IO) {
        val url = song.copy(localPath = "").audioUrl(quality) ?: error("This song has no downloadable audio.")
        val dir = File(context.filesDir, "downloads").apply { mkdirs() }
        val target = File(dir, song.id.replace(Regex("[^a-zA-Z0-9_-]"), "_") + ".m4a")
        val temp = File(dir, target.name + ".part")
        val conn = URI(url).toURL().openConnection() as HttpURLConnection
        conn.connectTimeout = 15_000; conn.readTimeout = 30_000
        try {
            check(conn.responseCode in 200..299) { "Download failed. Please try again." }
            val contentType = conn.contentType.orEmpty()
            check(!contentType.contains("text/") && !contentType.contains("json")) { "The service did not return an audio file." }
            conn.inputStream.use { input -> temp.outputStream().use { output -> input.copyTo(output) } }
            check(temp.length() > 0 && temp.renameTo(target)) { "Could not save this download. Check free storage." }
            song.copy(localPath = target.toURI().toString())
        } finally { conn.disconnect(); temp.delete() }
    }

    fun removeDownload(song: Song) {
        if (song.localPath.isBlank()) return
        val file = File(URI(song.localPath))
        val root = File(context.filesDir, "downloads").canonicalFile
        if (file.canonicalFile.parentFile == root) file.delete()
    }

    private fun encode(value: String) = URLEncoder.encode(value, "UTF-8")
}
