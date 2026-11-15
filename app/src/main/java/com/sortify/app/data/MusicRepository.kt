package com.sortify.app.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.util.UUID
import com.sortify.app.BuildConfig

class MusicRepository(private val context: Context) {
    private val youtube = YouTubeMusicClient()
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
    var searchProvider: MusicProvider
        get() = MusicProvider.fromValue(prefs.getString("searchProvider", "").orEmpty())
        set(value) { prefs.edit().putString("searchProvider", value.name).apply() }

    fun loadSongs(key: String): List<Song> = runCatching { JSONArray(prefs.getString(key, "[]")).songs() }.getOrDefault(emptyList())
    fun saveSongs(key: String, songs: List<Song>) { prefs.edit().putString(key, songs.json().toString()).apply() }
    fun loadPlaylists(): List<Playlist> = runCatching {
        val a = JSONArray(prefs.getString("playlists", "[]"))
        (0 until a.length()).map { a.getJSONObject(it).let { p -> Playlist(p.getString("id"), p.getString("name"), p.getJSONArray("songs").songs()) } }
    }.getOrDefault(emptyList())
    fun savePlaylists(playlists: List<Playlist>) { prefs.edit().putString("playlists", JSONArray(playlists.map(Playlist::json)).toString()).apply() }

    suspend fun exportData(uri: Uri) = withContext(Dispatchers.IO) {
        val snapshot = backupSnapshot()
        val output = context.contentResolver.openOutputStream(uri, "wt")
            ?: error("Could not open this file. Choose another location and retry.")
        output.use { DataBackup.write(it, snapshot, File(context.filesDir, "downloads")) }
    }

    class PreparedImport internal constructor(internal val data: JSONObject, internal val directory: File) {
        fun discard() { directory.deleteRecursively() }
    }

    fun prepareImport(uri: Uri): PreparedImport {
        val root = File(context.filesDir, "downloads").apply { mkdirs() }
        val staging = File(root, "restore-${UUID.randomUUID()}")
        check(staging.mkdir()) { "Could not prepare backup storage. Check free space and retry." }
        try {
            val data = context.contentResolver.openInputStream(uri)?.use { DataBackup.read(it, staging) }
                ?: error("Could not open this backup. Choose the file again.")
            DataBackup.relocateAudio(data, staging)
            return PreparedImport(data, staging)
        } catch (error: Exception) {
            staging.deleteRecursively()
            throw error
        }
    }

    // One preferences transaction publishes the fully validated library and audio paths together.
    fun applyImport(prepared: PreparedImport) {
        val previous = backupSnapshot()
        if (!writeSnapshot(prepared.data)) {
            writeSnapshot(previous)
            error("Could not save the imported data. Check free storage and retry.")
        }
    }

    suspend fun removeReplacedAudio(previous: List<Song>) = withContext(Dispatchers.IO) {
        previous.forEach { runCatching { removeDownload(it) } }
    }

    private fun backupSnapshot() = JSONObject().apply {
        put("settings", JSONObject().put("onboarded", onboarded).put("languages", JSONArray(languages.toList()))
            .put("quality", quality).put("theme", theme).put("searchProvider", searchProvider.name))
        fun normalizedSongs(songs: JSONArray) = JSONArray((0 until songs.length()).map {
            Song.fromJson(songs.getJSONObject(it)).json()
        })
        listOf("favorites", "recent", "downloads").forEach { key ->
            put(key, normalizedSongs(JSONArray(prefs.getString(key, "[]"))))
        }
        val playlists = JSONArray(prefs.getString("playlists", "[]"))
        for (index in 0 until playlists.length()) {
            val playlist = playlists.getJSONObject(index)
            playlist.put("songs", normalizedSongs(playlist.getJSONArray("songs")))
        }
        put("playlists", playlists)
    }

    private fun writeSnapshot(data: JSONObject): Boolean {
        val settings = data.getJSONObject("settings")
        val languages = settings.getJSONArray("languages")
        return prefs.edit().putBoolean("onboarded", settings.getBoolean("onboarded"))
            .putStringSet("languages", (0 until languages.length()).map { languages.getString(it) }.toSet())
            .putString("quality", settings.getString("quality"))
            .putString("theme", settings.getString("theme"))
            .putString("searchProvider", settings.getString("searchProvider"))
            .putString("favorites", data.getJSONArray("favorites").toString())
            .putString("recent", data.getJSONArray("recent").toString())
            .putString("downloads", data.getJSONArray("downloads").toString())
            .putString("playlists", data.getJSONArray("playlists").toString()).commit()
    }

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

    suspend fun searchSongs(query: String, limit: Int = 30, provider: MusicProvider = MusicProvider.JioSaavn): List<Song> {
        if (provider == MusicProvider.YouTubeMusic) return youtube.searchSongs(query, limit)
        val data = request("/search/songs?query=${encode(query)}&limit=$limit").optJSONObject("data")
        return data?.optJSONArray("results")?.songs().orEmpty().distinctBy { it.title.lowercase() to it.artist.lowercase() }
    }

    suspend fun searchCatalog(query: String, kind: String, provider: MusicProvider = MusicProvider.JioSaavn): List<CatalogItem> {
        if (provider == MusicProvider.YouTubeMusic) return youtube.searchCatalog(query, kind)
        val a = request("/search/$kind?query=${encode(query)}&limit=20").optJSONObject("data")?.optJSONArray("results") ?: JSONArray()
        return (0 until a.length()).map { a.getJSONObject(it) }.map { j ->
            val images = j.optJSONArray("image")
            CatalogItem(j.optString("id"), decode(j.optString("name", j.optString("title"))),
                decode(j.optString("description", j.optString("year", kind.removeSuffix("s").replaceFirstChar(Char::uppercase)))),
                images?.optJSONObject((images.length() - 1).coerceAtLeast(0))?.optString("url") ?: "", kind)
        }
    }

    suspend fun catalogSongs(item: CatalogItem): List<Song> {
        if (item.provider == MusicProvider.YouTubeMusic) return youtube.catalogSongs(item)
        val path = if (item.kind == "artists") "/artists/${encode(item.id)}/songs?limit=30" else "/${item.kind}?id=${encode(item.id)}"
        val data = request(path).optJSONObject("data")
        return (data?.optJSONArray("songs") ?: data?.optJSONArray("results"))?.songs().orEmpty()
    }

    suspend fun download(song: Song, quality: String): Song = withContext(Dispatchers.IO) {
        val url = if (song.provider == MusicProvider.YouTubeMusic) youtube.resolveAudio(song.id.removePrefix("yt:"), quality)
            else song.copy(localPath = "").audioUrl(quality) ?: error("This song has no downloadable audio.")
        val dir = File(context.filesDir, "downloads").apply { mkdirs() }
        val target = File(dir, song.id.replace(Regex("[^a-zA-Z0-9_-]"), "_") + ".m4a")
        val temp = File(dir, target.name + ".part")
        val conn = URI(url).toURL().openConnection() as HttpURLConnection
        conn.connectTimeout = 15_000; conn.readTimeout = 30_000
        if (song.provider == MusicProvider.YouTubeMusic) conn.setRequestProperty("User-Agent", YouTubeMusicClient.USER_AGENT)
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
        if (file.canonicalFile.startsWith(root) && file.canonicalFile != root) {
            file.delete()
            file.parentFile?.takeIf { it != root && it.list()?.isEmpty() == true }?.delete()
        }
    }

    private fun encode(value: String) = URLEncoder.encode(value, "UTF-8")

    suspend fun youtubeTrending() = youtube.trending()
    suspend fun youtubePlaylists(language: String) = youtube.languagePlaylists(language)
}
