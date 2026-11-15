package com.sortify.app

import com.sortify.app.data.DataBackup
import com.sortify.app.data.Song
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class DataBackupTest {
    @get:Rule val temp = TemporaryFolder()

    private fun snapshot() = JSONObject().apply {
        put("settings", JSONObject().put("onboarded", true).put("quality", "320kbps")
            .put("theme", "Light").put("searchProvider", "YouTubeMusic")
            .put("languages", JSONArray(listOf("English", "Hindi"))))
        listOf("favorites", "recent", "downloads", "playlists").forEach { put(it, JSONArray()) }
    }

    private fun export(data: JSONObject, root: File = temp.newFolder()): ByteArray =
        ByteArrayOutputStream().also { DataBackup.write(it, data, root) }.toByteArray()

    private fun manifest(bytes: ByteArray): JSONObject = ZipInputStream(bytes.inputStream()).use { zip ->
        assertEquals("sortify.json", zip.nextEntry.name)
        JSONObject(zip.readBytes().toString(Charsets.UTF_8))
    }

    private fun archive(data: JSONObject, entries: Map<String, ByteArray> = emptyMap()): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry("sortify.json"))
            zip.write(data.toString().toByteArray())
            zip.closeEntry()
            entries.forEach { (path, bytes) ->
                zip.putNextEntry(ZipEntry(path)); zip.write(bytes); zip.closeEntry()
            }
        }
        return output.toByteArray()
    }

    @Test fun `empty library and all preferences round trip`() {
        val original = snapshot()
        val imported = DataBackup.read(export(original).inputStream(), temp.newFolder())
        val settings = original.getJSONObject("settings")
        settings.keys().forEach { key ->
            assertEquals(settings.get(key).toString(), imported.getJSONObject("settings").get(key).toString())
        }
        assertEquals(0, imported.getJSONArray("downloads").length())
        assertEquals(0, imported.getJSONArray("playlists").length())
    }

    @Test fun `audio and every library reference restore to this device`() {
        val root = temp.newFolder()
        val audio = File(root, "track.m4a").apply { writeBytes(ByteArray(150_000) { (it % 251).toByte() }) }
        val song = Song("track", "A song", "Artist", localPath = audio.toURI().toString()).json()
        val data = snapshot().put("downloads", JSONArray().put(song))
            .put("favorites", JSONArray().put(song)).put("recent", JSONArray().put(song))
            .put("playlists", JSONArray().put(JSONObject().put("id", "playlist")
                .put("name", "My mix").put("songs", JSONArray().put(song))))
        val bytes = export(data, root)
        assertFalse(manifest(bytes).toString().contains(root.absolutePath.replace("\\", "/")))
        val staging = temp.newFolder()
        val imported = DataBackup.read(bytes.inputStream(), staging)
        assertArrayEquals(audio.readBytes(), File(staging, "0.m4a").readBytes())
        DataBackup.relocateAudio(imported, staging)
        val restored = File(staging, "0.m4a").toURI().toString()
        listOf("favorites", "recent", "downloads").forEach {
            assertEquals(restored, imported.getJSONArray(it).getJSONObject(0).getString("localPath"))
        }
        assertEquals(restored, imported.getJSONArray("playlists").getJSONObject(0)
            .getJSONArray("songs").getJSONObject(0).getString("localPath"))
        // Export must not rewrite the live library.
        assertEquals(audio.toURI().toString(), song.getString("localPath"))
    }

    @Test fun `stale local paths on streaming songs are removed`() {
        val data = snapshot().put("favorites", JSONArray().put(
            Song("stream", "Title", "Artist", localPath = "file:/old-device/missing.m4a").json()))
        val imported = DataBackup.read(export(data).inputStream(), temp.newFolder())
        assertEquals("", imported.getJSONArray("favorites").getJSONObject(0).getString("localPath"))
    }

    @Test fun `export refuses files outside private downloads`() {
        val outside = temp.newFile().apply { writeText("private data") }
        val data = snapshot().put("downloads", JSONArray().put(
            Song("bad", "Title", "Artist", localPath = outside.toURI().toString()).json()))
        assertThrows(IllegalArgumentException::class.java) { export(data) }
    }

    @Test fun `unsupported versions and invalid preferences are rejected`() {
        val valid = manifest(export(snapshot()))
        assertThrows(IllegalArgumentException::class.java) {
            DataBackup.read(archive(JSONObject(valid.toString()).put("version", 99)).inputStream(), temp.newFolder())
        }
        valid.getJSONObject("settings").put("quality", "invalid")
        assertThrows(IllegalArgumentException::class.java) {
            DataBackup.read(archive(valid).inputStream(), temp.newFolder())
        }
    }

    @Test fun `path traversal and undeclared entries are rejected`() {
        val valid = manifest(export(snapshot()))
        assertThrows(IllegalArgumentException::class.java) {
            DataBackup.read(archive(valid, mapOf("../escape" to byteArrayOf(1))).inputStream(), temp.newFolder())
        }
        valid.put("audioFiles", JSONArray().put(JSONObject().put("path", "audio/../escape").put("size", 1)))
        assertThrows(IllegalArgumentException::class.java) {
            DataBackup.read(archive(valid).inputStream(), temp.newFolder())
        }
    }

    @Test fun `missing incomplete and oversized audio are rejected`() {
        val root = temp.newFolder()
        val file = File(root, "song.m4a").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val data = snapshot().put("downloads", JSONArray().put(
            Song("track", "Title", "Artist", localPath = file.toURI().toString()).json()))
        val valid = manifest(export(data, root))
        listOf(emptyMap(), mapOf("audio/0.m4a" to byteArrayOf(1)),
            mapOf("audio/0.m4a" to byteArrayOf(1, 2, 3, 4))).forEach { entries ->
            assertThrows(IllegalArgumentException::class.java) {
                DataBackup.read(archive(valid, entries).inputStream(), temp.newFolder())
            }
        }
    }

    @Test fun `invalid files cannot replace an existing library`() {
        assertThrows(IllegalArgumentException::class.java) {
            DataBackup.read("not a backup".byteInputStream(), temp.newFolder())
        }
        val data = manifest(export(snapshot())).put("favorites", JSONArray().put(JSONObject().put("id", "incomplete")))
        assertThrows(Exception::class.java) { DataBackup.read(archive(data).inputStream(), temp.newFolder()) }
    }
}
