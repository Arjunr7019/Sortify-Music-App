package com.sortify.app

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.browse.MediaBrowser
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.os.Bundle
import androidx.lifecycle.ViewModelStore
import androidx.media3.common.MediaItem
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaBrowser as Media3Browser
import androidx.media3.session.SessionToken
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sortify.app.data.Playlist
import com.sortify.app.data.Song
import com.sortify.app.data.json
import com.sortify.app.data.songs
import com.sortify.app.playback.CarLibrary
import com.sortify.app.playback.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.collect.ImmutableList
import org.json.JSONArray
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/** Exercises Android Auto's legacy protocol without a car, UI, or network audio. */
@RunWith(AndroidJUnit4::class)
class AndroidAutoPlaybackTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val preferences = context.getSharedPreferences("sortify", Context.MODE_PRIVATE)
    private val component = ComponentName(context, PlaybackService::class.java)
    private val saved = mutableMapOf<String, Any?>()
    private lateinit var audio: File
    private lateinit var first: Song
    private lateinit var second: Song
    private var legacy: MediaBrowser? = null
    private var modern: Media3Browser? = null
    private val viewModels = ViewModelStore()

    @Before fun seedLibrary() {
        listOf("favorites", "downloads", "recent", "playlists", "onboarded").forEach { saved[it] = preferences.all[it] }
        audio = File.createTempFile("android-auto-test-", ".wav", context.cacheDir)
        val samples = ByteArray(8_000 * 2 * 30)
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            .put("RIFF".toByteArray()).putInt(samples.size + 36).put("WAVEfmt ".toByteArray())
            .putInt(16).putShort(1).putShort(1).putInt(8_000).putInt(16_000)
            .putShort(2).putShort(16).put("data".toByteArray()).putInt(samples.size).array()
        audio.outputStream().use { it.write(header); it.write(samples) }
        first = Song("car-test-first", "Car test first", "Test artist", duration = 30, localPath = audio.toURI().toString())
        second = first.copy(id = "car-test-second", title = "Car test second")
        assertTrue(preferences.edit().putString("favorites", listOf(first).json().toString())
            .putString("downloads", listOf(first, second).json().toString()).putString("recent", "[]")
            .putString("playlists", JSONArray().put(Playlist("car-test", "Car test mix", listOf(first, second)).json()).toString())
            .putBoolean("onboarded", false).commit())
    }

    @After fun restoreLibrary() {
        instrumentation.runOnMainSync {
            legacy?.takeIf { it.isConnected }?.let { MediaController(context, it.sessionToken).transportControls.stop(); it.disconnect() }
            modern?.release()
            viewModels.clear()
            context.stopService(Intent(context, PlaybackService::class.java))
        }
        instrumentation.waitForIdleSync()
        val editor = preferences.edit()
        saved.forEach { (key, value) ->
            when (value) {
                null -> editor.remove(key)
                is String -> editor.putString(key, value)
                is Boolean -> editor.putBoolean(key, value)
            }
        }
        assertTrue(editor.commit())
        audio.delete()
    }

    @Test fun legacyBrowserCanColdStartBrowsePlayQueueAndControlPlayback() {
        val browser = connectLegacy()
        assertEquals(listOf("Home", "Liked songs", "Downloads", "Playlists"), children(browser.root).map { it.description.title.toString() })
        val playlists = children(CarLibrary.PLAYLISTS)
        assertEquals("Car test mix", playlists.single().description.title.toString())
        val songs = children(playlists.single().mediaId!!)
        assertEquals(2, songs.size)
        assertTrue(songs.all { it.isPlayable && !it.isBrowsable })
        assertEquals(2, children(CarLibrary.DOWNLOADS).size)
        val controller = MediaController(context, browser.sessionToken)
        controller.transportControls.playFromMediaId(songs[1].mediaId, null)
        await("selected song should play from its download") {
            controller.playbackState?.state == PlaybackState.STATE_PLAYING && controller.metadata?.description?.mediaId == second.id
        }
        assertEquals(2, controller.queue?.size)
        controller.transportControls.pause()
        await("pause") { controller.playbackState?.state == PlaybackState.STATE_PAUSED }
        controller.transportControls.skipToPrevious()
        await("previous") { controller.metadata?.description?.mediaId == first.id }
        controller.transportControls.skipToNext()
        await("next") { controller.metadata?.description?.mediaId == second.id }
        assertTrue(JSONArray(preferences.getString("recent", "[]")).songs().any { it.id == second.id })
        assertTrue(preferences.edit().putString("favorites", listOf(first, second).json().toString()).commit())
        assertEquals(2, children(CarLibrary.FAVORITES).size)
    }

    @Test fun voiceSearchAndPhonePlayerFollowCarQueueReplacement() {
        var pending: ListenableFuture<Media3Browser>? = null
        instrumentation.runOnMainSync { pending = Media3Browser.Builder(context, SessionToken(context, component)).buildAsync() }
        val browser = pending!!.get(10, TimeUnit.SECONDS).also { modern = it }
        var result: ListenableFuture<LibraryResult<ImmutableList<MediaItem>>>? = null
        instrumentation.runOnMainSync { result = browser.getSearchResult("Car test", 0, 10, null) }
        val searchResult = result!!.get(30, TimeUnit.SECONDS)
        assertEquals(0, searchResult.resultCode)
        assertEquals(2, searchResult.value!!.size)
        lateinit var viewModel: SortifyViewModel
        instrumentation.runOnMainSync {
            viewModel = SortifyViewModel(context.applicationContext as Application)
            viewModels.put("car-test", viewModel)
        }
        val legacyBrowser = connectLegacy()
        val controller = MediaController(context, legacyBrowser.sessionToken)
        controller.transportControls.playFromSearch("Car test", Bundle())
        await("voice playback should populate the phone queue") {
            var matches = false
            instrumentation.runOnMainSync { matches = viewModel.queue.map { it.id } == listOf(first.id, second.id) && viewModel.current?.id == first.id }
            matches
        }
        controller.transportControls.playFromMediaId(children(CarLibrary.FAVORITES).single().mediaId, null)
        await("the phone should replace its existing queue after a car selection") {
            var matches = false
            instrumentation.runOnMainSync { matches = viewModel.queue.map { it.id } == listOf(first.id) }
            matches
        }
    }

    private fun connectLegacy(): MediaBrowser {
        val connected = CountDownLatch(1)
        instrumentation.runOnMainSync {
            legacy = MediaBrowser(context, component, object : MediaBrowser.ConnectionCallback() {
                override fun onConnected() { connected.countDown() }
                override fun onConnectionFailed() { connected.countDown() }
            }, null).also { it.connect() }
        }
        assertTrue("legacy browser connection timed out", connected.await(10, TimeUnit.SECONDS))
        return legacy!!.also { assertTrue("legacy browser connection rejected", it.isConnected) }
    }

    private fun children(parentId: String): List<MediaBrowser.MediaItem> {
        val completed = CountDownLatch(1)
        val items = AtomicReference<List<MediaBrowser.MediaItem>>()
        val callback = object : MediaBrowser.SubscriptionCallback() {
            override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowser.MediaItem>) { items.set(children); completed.countDown() }
            override fun onError(parentId: String) { completed.countDown() }
        }
        instrumentation.runOnMainSync { legacy!!.subscribe(parentId, callback) }
        assertTrue("browse timed out: $parentId", completed.await(10, TimeUnit.SECONDS))
        instrumentation.runOnMainSync { legacy!!.unsubscribe(parentId, callback) }
        return requireNotNull(items.get()) { "browse failed: $parentId" }
    }

    private fun await(message: String, condition: () -> Boolean) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15)
        while (System.nanoTime() < deadline) {
            if (condition()) return
            Thread.sleep(100)
        }
        fail(message)
    }
}
