package com.sortify.app.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import com.sortify.app.data.YouTubeMusicClient
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.LibraryResult
import androidx.media3.session.SessionError
import com.sortify.app.MainActivity
import com.sortify.app.data.MusicRepository
import com.sortify.app.data.Song
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class PlaybackService : MediaLibraryService() {
    private var session: MediaLibrarySession? = null
    private lateinit var repository: MusicRepository
    private lateinit var preferences: SharedPreferences
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val searches = object : LinkedHashMap<String, List<Song>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<Song>>?) = size > 16
    }
    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key in setOf("favorites", "recent", "downloads", "playlists")) {
            scope.launch {
                val library = loadLibrary()
                val active = session ?: return@launch
                listOf(CarLibrary.ROOT, CarLibrary.HOME, CarLibrary.FAVORITES, CarLibrary.DOWNLOADS, CarLibrary.PLAYLISTS)
                    .plus(library.playlists.map { CarLibrary.playlistId(it.id) }).forEach { parent ->
                        val count = library.folders(parent)?.size ?: library.songs(parent)?.size ?: 0
                        active.notifyChildrenChanged(parent, count, null)
                    }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = MusicRepository(this)
        preferences = getSharedPreferences("sortify", Context.MODE_PRIVATE)
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
            addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    val song = mediaItem?.toSong(repository.quality) ?: return
                    repository.saveSongs("recent", (listOf(song) + repository.loadSongs("recent")
                        .filterNot { it.id == song.id }).take(12))
                }
            })
        }
        val activity = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        session = MediaLibrarySession.Builder(this, player, LibraryCallback()).setSessionActivity(activity).build()
        preferences.registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? = session

    private suspend fun loadLibrary() = withContext(Dispatchers.IO) {
        CarLibrary(repository.loadSongs("recent"), repository.loadSongs("favorites"),
            repository.loadSongs("downloads"), repository.loadPlaylists(), repository.initialSongs)
    }

    private fun folder(id: String, title: String) = MediaItem.Builder().setMediaId(id).setMediaMetadata(
        MediaMetadata.Builder().setTitle(title).setIsBrowsable(true).setIsPlayable(false)
            .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED).build()
    ).build()

    private fun playable(songs: List<Song>, library: CarLibrary, parentId: String? = null): List<MediaItem> =
        songs.mapNotNull { song -> song.toMediaItem(repository.quality, library.downloads,
            parentId?.let { CarMediaId.encode(it, song.id) } ?: song.id) }

    private suspend fun search(query: String): List<Song> {
        val normalized = query.trim()
        if (normalized.isEmpty()) return loadLibrary().songs(CarLibrary.HOME).orEmpty()
        val key = "${repository.searchProvider.name}:$normalized"
        searches[key]?.let { return it }
        val library = loadLibrary()
        val terms = normalized.split(Regex("\\s+"))
        val local = (library.downloads + library.favorites + library.recent + library.playlists.flatMap { it.songs } + library.discovery)
            .filter { song -> terms.all { term -> "${song.title} ${song.artist} ${song.album}".contains(term, ignoreCase = true) } }
        // Saved matches are immediate and work without connectivity.
        if (local.isNotEmpty()) return local.distinctBy { it.id }
        return repository.searchSongs(normalized, provider = repository.searchProvider).distinctBy { it.id }
            .also { searches[key] = it }
    }

    private suspend fun contextSongs(parentId: String, library: CarLibrary): List<Song>? =
        if (parentId.startsWith(SEARCH_PREFIX)) search(parentId.removePrefix(SEARCH_PREFIX)) else library.songs(parentId)

    private suspend fun resolve(item: MediaItem, controller: MediaSession.ControllerInfo, library: CarLibrary): MediaItem {
        // The phone already supplies its selected URI. External controllers resolve through our library.
        if (controller.packageName == packageName && item.localConfiguration != null) return item
        val context = CarMediaId.decode(item.mediaId)
        val song = if (context != null) contextSongs(context.first, library)?.firstOrNull { it.id == context.second }
            else library.find(item.mediaId)
        return song?.toMediaItem(repository.quality, library.downloads)
            ?: throw IllegalArgumentException("This track is no longer available. Choose another song.")
    }

    private inner class LibraryCallback : MediaLibrarySession.Callback {
        override fun onGetLibraryRoot(session: MediaLibrarySession, browser: MediaSession.ControllerInfo, params: LibraryParams?) =
            Futures.immediateFuture(LibraryResult.ofItem(folder(CarLibrary.ROOT, "Sortify"),
                LibraryParams.Builder().setExtras(Bundle().apply { putBoolean("android.media.browse.SEARCH_SUPPORTED", true) }).build()))

        override fun onGetChildren(session: MediaLibrarySession, browser: MediaSession.ControllerInfo, parentId: String,
            page: Int, pageSize: Int, params: LibraryParams?): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> = libraryResult {
            val library = loadLibrary()
            val children = library.folders(parentId)?.map { folder(it.first, it.second) }
                ?: library.songs(parentId)?.let { playable(it, library, parentId) }
                ?: return@libraryResult LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
            LibraryResult.ofItemList(children.libraryPage(page, pageSize), params)
        }

        override fun onGetItem(session: MediaLibrarySession, browser: MediaSession.ControllerInfo, mediaId: String): ListenableFuture<LibraryResult<MediaItem>> = libraryResult {
            if (mediaId == CarLibrary.ROOT) return@libraryResult LibraryResult.ofItem(folder(mediaId, "Sortify"), null)
            val library = loadLibrary()
            val entry = (library.folders(CarLibrary.ROOT).orEmpty() + library.folders(CarLibrary.PLAYLISTS).orEmpty()).firstOrNull { it.first == mediaId }
            if (entry != null) return@libraryResult LibraryResult.ofItem(folder(entry.first, entry.second), null)
            val resolved = resolve(MediaItem.Builder().setMediaId(mediaId).build(), browser, library)
            LibraryResult.ofItem(resolved.buildUpon().setMediaId(mediaId).build(), null)
        }

        override fun onSearch(session: MediaLibrarySession, browser: MediaSession.ControllerInfo, query: String, params: LibraryParams?): ListenableFuture<LibraryResult<Void>> = libraryResult {
            val results = playable(search(query), loadLibrary())
            session.notifySearchResultChanged(browser, query, results.size, params)
            LibraryResult.ofVoid(params)
        }

        override fun onGetSearchResult(session: MediaLibrarySession, browser: MediaSession.ControllerInfo, query: String,
            page: Int, pageSize: Int, params: LibraryParams?): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> = libraryResult {
            LibraryResult.ofItemList(playable(search(query), loadLibrary(), SEARCH_PREFIX + query).libraryPage(page, pageSize), params)
        }

        override fun onAddMediaItems(mediaSession: MediaSession, controller: MediaSession.ControllerInfo, mediaItems: MutableList<MediaItem>): ListenableFuture<List<MediaItem>> = future {
            val library = loadLibrary()
            mediaItems.map { resolve(it, controller, library) }
        }

        override fun onSetMediaItems(mediaSession: MediaSession, controller: MediaSession.ControllerInfo, mediaItems: MutableList<MediaItem>,
            startIndex: Int, startPositionMs: Long): ListenableFuture<MediaSession.MediaItemsWithStartPosition> = future {
            val library = loadLibrary()
            val single = mediaItems.singleOrNull()
            val context = single?.let { CarMediaId.decode(it.mediaId) }
            val query = single?.requestMetadata?.searchQuery
            if (context != null || query != null) {
                val songs = if (query != null) search(query) else contextSongs(context!!.first, library).orEmpty()
                val items = playable(songs, library)
                val index = if (context != null) items.indexOfFirst { it.mediaId == context.second } else 0
                require(items.isNotEmpty() && index >= 0) { "No playable songs were found. Try another song or search." }
                MediaSession.MediaItemsWithStartPosition(items, index, startPositionMs)
            } else MediaSession.MediaItemsWithStartPosition(mediaItems.map { resolve(it, controller, library) }, startIndex, startPositionMs)
        }

        override fun onPlaybackResumption(mediaSession: MediaSession, controller: MediaSession.ControllerInfo): ListenableFuture<MediaSession.MediaItemsWithStartPosition> = future {
            val library = loadLibrary()
            val items = playable(library.recent.ifEmpty { library.discovery }, library)
            require(items.isNotEmpty()) { "Choose a song to start listening." }
            MediaSession.MediaItemsWithStartPosition(items, 0, 0)
        }
    }

    private fun <T : Any> libraryResult(block: suspend () -> LibraryResult<T>): ListenableFuture<LibraryResult<T>> = future {
        try { return@future block() }
        catch (error: CancellationException) { throw error }
        catch (error: IllegalArgumentException) { return@future LibraryResult.ofError<T>(SessionError.ERROR_BAD_VALUE) }
        catch (error: Exception) { return@future LibraryResult.ofError<T>(SessionError.ERROR_IO) }
    }

    private fun <T> future(block: suspend () -> T): ListenableFuture<T> {
        val result = SettableFuture.create<T>()
        val job = scope.launch {
            try { result.set(block()) }
            catch (error: CancellationException) { result.cancel(false); throw error }
            catch (error: Exception) { result.setException(error) }
        }
        job.invokeOnCompletion { if (!result.isDone) result.cancel(false) }
        result.addListener({ if (result.isCancelled) job.cancel() }, MoreExecutors.directExecutor())
        return result
    }

    override fun onDestroy() {
        preferences.unregisterOnSharedPreferenceChangeListener(preferenceListener)
        scope.cancel()
        session?.run { player.release(); release() }
        session = null
        super.onDestroy()
    }

    companion object {
        private const val SEARCH_PREFIX = "sortify:search/"
    }
}
