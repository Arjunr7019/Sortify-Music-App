package com.sortify.app

import com.sortify.app.data.AudioSource
import com.sortify.app.data.MusicProvider
import com.sortify.app.data.Playlist
import com.sortify.app.data.Song
import com.sortify.app.playback.CarLibrary
import com.sortify.app.playback.CarMediaId
import com.sortify.app.playback.libraryPage
import com.sortify.app.playback.playbackUrl
import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files

class CarLibraryTest {
    private val first = Song("first", "First", "Artist", sources = listOf(AudioSource("160kbps", "https://audio.example/first")))
    private val second = first.copy(id = "second", title = "Second")
    private val library = CarLibrary(listOf(second), listOf(first), emptyList(),
        listOf(Playlist("mix/1", "Road trip", listOf(second, first))), listOf(first, second))

    @Test fun `home prioritizes recent listening without duplicate songs`() {
        assertEquals(listOf("second", "first"), library.songs(CarLibrary.HOME)?.map { it.id })
        assertEquals(4, library.folders(CarLibrary.ROOT)?.size)
    }

    @Test fun `playlist folders retain saved order and unknown folders are distinct from empty folders`() {
        val folder = library.folders(CarLibrary.PLAYLISTS)!!.single()
        assertEquals("Road trip", folder.second)
        assertEquals(listOf(second, first), library.songs(folder.first))
        assertEquals(emptyList<Song>(), library.songs(CarLibrary.DOWNLOADS))
        assertNull(library.songs("missing"))
    }

    @Test fun `browse ids preserve provider ids unicode and playlist context`() {
        listOf("yt:a_b-c", "song/with/slashes", "हिन्दी + / ?").forEach { id ->
            val parent = "sortify:playlist/mix/1"
            assertEquals(parent to id, CarMediaId.decode(CarMediaId.encode(parent, id)))
        }
        assertNull(CarMediaId.decode("yt:normal-id"))
        assertNull(CarMediaId.decode("sortify:track/bad%/value"))
        assertNull(CarMediaId.decode("sortify:track/extra/path/segment"))
    }

    @Test fun `paging tolerates legacy unbounded requests and avoids integer overflow`() {
        val values = listOf(first, second)
        assertEquals(values, values.libraryPage(0, Int.MAX_VALUE))
        assertEquals(listOf(second), values.libraryPage(1, 1))
        assertTrue(values.libraryPage(Int.MAX_VALUE, Int.MAX_VALUE).isEmpty())
        assertTrue(values.libraryPage(-1, 1).isEmpty())
        assertTrue(values.libraryPage(0, 0).isEmpty())
    }

    @Test fun `playback uses a real download then falls back when a saved file has disappeared`() {
        val file = Files.createTempFile("sortify-car-audio", ".m4a").toFile()
        try {
            val downloaded = first.copy(localPath = file.toURI().toString())
            assertEquals(downloaded.localPath, first.playbackUrl("160kbps", listOf(downloaded)))
            assertTrue(file.delete())
            assertEquals("https://audio.example/first", downloaded.playbackUrl("160kbps", listOf(downloaded)))
            assertEquals("https://audio.example/first", first.playbackUrl("160kbps", listOf(first.copy(localPath = "broken path"))))
        } finally { file.delete() }
    }

    @Test fun `youtube tracks use deferred resolution at the selected quality`() {
        val youtube = Song("yt:video-id", "Title", "Artist", provider = MusicProvider.YouTubeMusic)
        assertEquals("https://music.youtube.com/watch?v=video-id&sortify_quality=320kbps", youtube.playbackUrl("320kbps", emptyList()))
        assertNull(first.copy(sources = emptyList()).playbackUrl("160kbps", emptyList()))
    }
}
