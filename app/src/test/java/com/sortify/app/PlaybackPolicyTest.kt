package com.sortify.app

import com.sortify.app.data.AudioSource
import com.sortify.app.data.Song
import com.sortify.app.data.timeLabel
import org.junit.Assert.*
import org.junit.Test

class PlaybackPolicyTest {
    private val song = Song("track", "Title", "Artist", sources = listOf(
        AudioSource("320kbps", "https://audio.example/high.m4a"),
        AudioSource("96kbps", "https://audio.example/low.m4a"),
    ))

    @Test fun `lower quality preference selects nearest available bitrate`() {
        assertEquals("https://audio.example/low.m4a", song.audioUrl("160kbps"))
    }

    @Test fun `exact quality selects the matching stream`() {
        assertEquals("https://audio.example/high.m4a", song.audioUrl("320kbps"))
    }

    @Test fun `downloaded audio takes priority over network preference`() {
        assertEquals("file:/downloads/track.m4a", song.copy(localPath = "file:/downloads/track.m4a").audioUrl("320kbps"))
    }

    @Test fun `insecure or absent audio is unavailable`() {
        assertNull(song.copy(sources = listOf(AudioSource("160kbps", "http://audio.example/track"))).audioUrl("160kbps"))
        assertNull(song.copy(sources = emptyList()).audioUrl("320kbps"))
    }

    @Test fun `time labels handle unknown and hour-long media`() {
        assertEquals("0:00", timeLabel(-1))
        assertEquals("3:07", timeLabel(187))
        assertEquals("60:01", timeLabel(3601))
    }
}
