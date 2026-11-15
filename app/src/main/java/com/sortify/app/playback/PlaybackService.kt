package com.sortify.app.playback

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import com.sortify.app.data.YouTubeMusicClient
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.sortify.app.MainActivity

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val youtube = YouTubeMusicClient()
        val upstream = DefaultDataSource.Factory(this, DefaultHttpDataSource.Factory().setUserAgent(YouTubeMusicClient.USER_AGENT))
        val resolving = ResolvingDataSource.Factory(upstream) { spec ->
            if (spec.uri.host == "music.youtube.com" && spec.uri.path == "/watch") {
                val id = spec.uri.getQueryParameter("v").orEmpty()
                val quality = spec.uri.getQueryParameter("sortify_quality") ?: "160kbps"
                spec.withUri(Uri.parse(youtube.resolveAudio(id, quality)))
            } else spec
        }
        val player = ExoPlayer.Builder(this).setMediaSourceFactory(DefaultMediaSourceFactory(resolving)).build().apply {
            setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(), true)
            setHandleAudioBecomingNoisy(true)
        }
        val activity = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        session = MediaSession.Builder(this, player).setSessionActivity(activity).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onDestroy() {
        session?.run { player.release(); release() }
        session = null
        super.onDestroy()
    }
}
