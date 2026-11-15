package com.sortify.app.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** A portable library snapshot. Audio is streamed rather than held in memory. */
object DataBackup {
    private const val FORMAT = "sortify-backup"
    private const val VERSION = 1
    private const val MANIFEST = "sortify.json"
    private const val MAX_MANIFEST_BYTES = 16 * 1024 * 1024
    private val audioName = Regex("audio/[0-9]+\\.m4a")

    fun write(output: OutputStream, snapshot: JSONObject, downloadsRoot: File) {
        val data = JSONObject(snapshot.toString())
        validate(data)
        val root = downloadsRoot.canonicalFile
        val files = linkedMapOf<String, File>()
        val paths = mutableMapOf<String, String>()
        val downloads = data.getJSONArray("downloads")
        for (index in 0 until downloads.length()) {
            val song = downloads.getJSONObject(index)
            val file = runCatching { File(java.net.URI(song.getString("localPath"))).canonicalFile }.getOrNull()
            require(file != null && file.startsWith(root) && file != root && file.isFile && file.length() > 0) {
                "A downloaded song is missing. Remove it from Downloads or download it again, then retry."
            }
            val path = "audio/$index.m4a"
            files[path] = file
            paths[song.getString("id")] = path
        }
        rewritePaths(data, paths)
        data.put("format", FORMAT).put("version", VERSION)
        data.put("audioFiles", JSONArray(files.map { (path, file) ->
            JSONObject().put("path", path).put("size", file.length())
        }))
        val manifest = data.toString().toByteArray(Charsets.UTF_8)
        require(manifest.size <= MAX_MANIFEST_BYTES) { "This library is too large for this backup format." }
        ZipOutputStream(output.buffered()).use { zip ->
            zip.setLevel(1)
            zip.putNextEntry(ZipEntry(MANIFEST))
            zip.write(manifest)
            zip.closeEntry()
            files.forEach { (path, file) ->
                zip.putNextEntry(ZipEntry(path))
                file.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    /** Extract only declared audio into an isolated directory; never touch live data here. */
    fun read(input: InputStream, staging: File): JSONObject {
        require(staging.isDirectory && staging.list().orEmpty().isEmpty()) { "Could not prepare backup storage." }
        ZipInputStream(input.buffered()).use { zip ->
            require(zip.nextEntry?.name == MANIFEST) { "Choose a Sortify ZIP backup exported from Settings." }
            val manifest = ByteArrayOutputStream()
            val manifestBuffer = ByteArray(8192)
            while (true) {
                val count = zip.read(manifestBuffer)
                if (count < 0) break
                require(manifest.size() + count <= MAX_MANIFEST_BYTES) { "The backup's library data is too large." }
                manifest.write(manifestBuffer, 0, count)
            }
            val bytes = manifest.toByteArray()
            val data = JSONObject(String(bytes, Charsets.UTF_8))
            require(data.getString("format") == FORMAT) { "This is not a Sortify backup." }
            require(data.getInt("version") == VERSION) { "This backup needs a newer version of Sortify." }
            validate(data)
            val files = data.getJSONArray("audioFiles")
            val expected = linkedMapOf<String, Long>()
            for (index in 0 until files.length()) {
                val file = files.getJSONObject(index)
                val path = file.getString("path")
                val size = file.getLong("size")
                require(audioName.matches(path) && size > 0 && expected.put(path, size) == null) { "Invalid audio files in backup." }
            }
            var total = 0L
            expected.values.forEach { size ->
                require(size <= Long.MAX_VALUE - total) { "Backup is too large." }
                total += size
            }
            require(total <= staging.usableSpace - 10 * 1024 * 1024) { "Not enough free storage. Free some space and retry." }
            val downloads = data.getJSONArray("downloads")
            val downloadPaths = (0 until downloads.length()).map { downloads.getJSONObject(it).getString("localPath") }
            require(downloadPaths.toSet() == expected.keys && downloadPaths.distinct().size == downloadPaths.size) { "Downloaded audio is missing from this backup." }
            forEachSong(data) { song ->
                val path = song.optString("localPath")
                require(path.isEmpty() || path in expected) { "Invalid song path in backup." }
            }
            val remaining = expected.toMutableMap()
            while (true) {
                val entry = zip.nextEntry ?: break
                val size = remaining.remove(entry.name)
                require(!entry.isDirectory && size != null) { "Unexpected or duplicate file in backup." }
                val target = File(staging, entry.name.substringAfter('/'))
                target.outputStream().buffered().use { out ->
                    val buffer = ByteArray(64 * 1024)
                    var written = 0L
                    while (true) {
                        val count = zip.read(buffer)
                        if (count < 0) break
                        require(count.toLong() <= size - written) { "Audio file exceeds its declared size." }
                        out.write(buffer, 0, count)
                        written += count
                    }
                    require(written == size) { "The backup contains incomplete audio." }
                }
                zip.closeEntry()
            }
            require(remaining.isEmpty()) { "The backup is incomplete. Export it again and retry." }
            return data
        }
    }

    fun relocateAudio(data: JSONObject, directory: File) {
        val downloads = data.getJSONArray("downloads")
        val paths = (0 until downloads.length()).associate { index ->
            val song = downloads.getJSONObject(index)
            song.getString("id") to File(directory, song.getString("localPath").substringAfter('/')).toURI().toString()
        }
        rewritePaths(data, paths)
    }

    private fun rewritePaths(data: JSONObject, paths: Map<String, String>) {
        forEachSong(data) { song -> song.put("localPath", paths[song.getString("id")].orEmpty()) }
    }

    private fun validate(data: JSONObject) {
        val settings = data.getJSONObject("settings")
        require(settings.get("onboarded") is Boolean) { "Invalid onboarding preference." }
        require(settings.getString("quality") in listOf("96kbps", "160kbps", "320kbps")) { "Invalid audio quality." }
        require(settings.getString("theme") in listOf("Dark", "Light", "System")) { "Invalid appearance." }
        require(settings.getString("searchProvider") in MusicProvider.entries.map { it.name }) { "Invalid music provider." }
        val languages = settings.getJSONArray("languages")
        require(languages.length() > 0 && (0 until languages.length()).all { languages.get(it) is String && languages.getString(it).isNotBlank() }) { "Invalid music languages." }
        val playlists = data.getJSONArray("playlists")
        val playlistIds = mutableSetOf<String>()
        for (index in 0 until playlists.length()) {
            val playlist = playlists.getJSONObject(index)
            require(playlist.getString("id").isNotBlank() && playlistIds.add(playlist.getString("id")) && playlist.getString("name").isNotBlank()) { "Invalid playlist in backup." }
        }
        forEachSong(data) { song ->
            require(song.getString("id").isNotBlank() && song.get("name") is String && song.get("primaryArtists") is String) { "Invalid song in backup." }
            require(song.getString("provider") in MusicProvider.entries.map { it.name }) { "Invalid song provider." }
            require(song.get("localPath") is String && song.get("duration") is Number && song.getLong("duration") >= 0) { "Invalid song data." }
            song.getJSONObject("album")
            song.getJSONArray("image")
            val sources = song.getJSONArray("downloadUrl")
            for (index in 0 until sources.length()) {
                val source = sources.getJSONObject(index)
                require(source.get("quality") is String && source.get("url") is String) { "Invalid song source." }
            }
        }
        for (key in listOf("favorites", "recent", "downloads")) {
            val songs = data.getJSONArray(key)
            val ids = (0 until songs.length()).map { songs.getJSONObject(it).getString("id") }
            require(ids.distinct().size == ids.size) { "Duplicate songs in backup." }
        }
    }

    private fun forEachSong(data: JSONObject, action: (JSONObject) -> Unit) {
        fun visit(songs: JSONArray) { for (index in 0 until songs.length()) action(songs.getJSONObject(index)) }
        listOf("favorites", "recent", "downloads").forEach { visit(data.getJSONArray(it)) }
        val playlists = data.getJSONArray("playlists")
        for (index in 0 until playlists.length()) visit(playlists.getJSONObject(index).getJSONArray("songs"))
    }
}
