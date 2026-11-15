package com.sortify.app

import com.sortify.app.data.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class YouTubeMusicParserTest {
    private fun fixture(name: String) = JSONObject(javaClass.getResource("/youtube/$name.json")!!.readText())

    @Test fun `live song search preserves video identity artist album duration and artwork`() {
        val songs = YouTubeMusicParser.songs(fixture("songs"))
        assertEquals(2, songs.size)
        val song = songs.first()
        assertTrue(song.id.matches(Regex("yt:[A-Za-z0-9_-]{11}")))
        assertEquals(MusicProvider.YouTubeMusic, song.provider)
        assertTrue(song.title.isNotBlank())
        assertTrue(song.artist.contains("Arijit Singh"))
        assertTrue(song.album.isNotBlank())
        assertTrue(song.duration > 0)
        assertTrue(song.image.startsWith("https://"))
        assertTrue(song.sources.isEmpty()) // Metadata responses are not playable audio URLs.
    }

    @Test fun `playlist search retains its browse id and source without leaking menu entries`() {
        val root = fixture("playlists")
        val lists = YouTubeMusicParser.catalog(root, "playlists")
        assertEquals(2, lists.size)
        assertTrue(lists.all { it.id.startsWith("VL") && it.provider == MusicProvider.YouTubeMusic && it.image.startsWith("https://") })
        assertTrue(YouTubeMusicParser.catalog(root, "artists").isEmpty())
        assertTrue(YouTubeMusicParser.songs(root).isEmpty())
    }

    @Test fun `chart playlist renderers can be opened for trending tracks`() {
        val lists = YouTubeMusicParser.catalog(fixture("charts"), "playlists")
        assertEquals(2, lists.size)
        assertTrue(lists.first().title.contains("Trending", true))
        assertTrue(lists.all { it.id.startsWith("VL") })
    }

    @Test fun `duplicate entries and missing video ids cannot create bogus playable tracks`() {
        val root = fixture("songs")
        val contents = root.getJSONArray("contents")
        contents.put(contents.getJSONObject(0))
        contents.put(JSONObject("""{"musicResponsiveListItemRenderer":{"flexColumns":[]}}"""))
        assertEquals(2, YouTubeMusicParser.songs(root).size)
        assertTrue(YouTubeMusicParser.songs(JSONObject()).isEmpty())
    }

    @Test fun `old saved tracks and unknown provider values default to JioSaavn`() {
        assertEquals(MusicProvider.JioSaavn, MusicProvider.fromValue(""))
        assertEquals(MusicProvider.JioSaavn, MusicProvider.fromValue("future-source"))
        val song = Song("yt:abcdefghijk", "Song", "Artist", provider = MusicProvider.YouTubeMusic)
        assertEquals(MusicProvider.YouTubeMusic, MusicProvider.fromValue(song.json().getString("provider")))
        assertNotEquals(song.id, Song("abcdefghijk", "Song", "Artist").id)
    }

    @Test fun `durations support hours and reject missing or unavailable values`() {
        assertEquals(3633, YouTubeMusicParser.durationSeconds("1:00:33"))
        assertEquals(245, YouTubeMusicParser.durationSeconds("4:05"))
        assertEquals(0, YouTubeMusicParser.durationSeconds("LIVE"))
        assertEquals(0, YouTubeMusicParser.durationSeconds(""))
    }
}
