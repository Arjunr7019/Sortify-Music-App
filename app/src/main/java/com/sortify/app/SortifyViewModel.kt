package com.sortify.app

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.sortify.app.data.*
import com.sortify.app.playback.PlaybackService
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.util.UUID

enum class Screen { Home, Search, Library, Create, Playlist, Favorites, Downloads, Settings, Languages, Player, Queue }

class SortifyViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = MusicRepository(app)
    var stage by mutableStateOf(if (repo.onboarded) 2 else 0); private set
    var screen by mutableStateOf(Screen.Home); private set
    var playerOrigin by mutableStateOf(Screen.Home); private set
    var tab by mutableStateOf(Screen.Home); private set
    private val backStack = mutableStateListOf<Screen>()
    var theme by mutableStateOf(repo.theme); private set
    var quality by mutableStateOf(repo.quality); private set
    var languages by mutableStateOf(repo.languages); private set
    var favorites by mutableStateOf(repo.loadSongs("favorites")); private set
    var downloads by mutableStateOf(repo.loadSongs("downloads")); private set
    var recent by mutableStateOf(repo.loadSongs("recent")); private set
    var playlists by mutableStateOf(repo.loadPlaylists()); private set
    val seed = repo.initialSongs
    var trending by mutableStateOf<Map<String, List<Song>>>(emptyMap()); private set
    var refreshing by mutableStateOf(false); private set
    var homeError by mutableStateOf<String?>(null); private set
    var selectedPlaylist by mutableStateOf<Playlist?>(null); private set
    var detailLoading by mutableStateOf(false); private set
    var detailError by mutableStateOf<String?>(null); private set
    private var detailItem: CatalogItem? = null

    var query by mutableStateOf(""); private set
    var searchKind by mutableStateOf("songs"); private set
    var searchResults by mutableStateOf<List<Song>>(emptyList()); private set
    var catalogResults by mutableStateOf<List<CatalogItem>>(emptyList()); private set
    var searching by mutableStateOf(false); private set
    var searchError by mutableStateOf<String?>(null); private set
    private var searchJob: Job? = null
    private var searchGeneration = 0

    var current by mutableStateOf<Song?>(null); private set
    var queue by mutableStateOf<List<Song>>(emptyList()); private set
    var playing by mutableStateOf(false); private set
    var buffering by mutableStateOf(false); private set
    var position by mutableStateOf(0L); private set
    var duration by mutableStateOf(0L); private set
    var shuffle by mutableStateOf(false); private set
    var repeat by mutableStateOf(Player.REPEAT_MODE_OFF); private set
    var downloading by mutableStateOf<Set<String>>(emptySet()); private set
    var playbackError by mutableStateOf<String?>(null); private set
    var addToPlaylist by mutableStateOf<Song?>(null)
    var songMenu by mutableStateOf<Song?>(null)
    var sleepMinutes by mutableStateOf(0); private set
    private var sleepJob: Job? = null
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val messages = _messages.asSharedFlow()
    private var controller: MediaController? = null
    private val controllerFuture = MediaController.Builder(app, SessionToken(app, ComponentName(app, PlaybackService::class.java))).buildAsync()

    init {
        controllerFuture.addListener({
            runCatching {
                controller = controllerFuture.get().apply {
                    addListener(object : Player.Listener {
                        override fun onEvents(player: Player, events: Player.Events) { syncPlayer() }
                        override fun onPlayerError(error: PlaybackException) {
                            playbackError = "This track could not play. Check your connection and try again."
                            message(playbackError!!)
                        }
                    })
                }
                syncPlayer()
            }.onFailure { message("The player could not start. Reopen Sortify to retry.") }
        }, ContextCompat.getMainExecutor(app))
        viewModelScope.launch {
            while (true) {
                syncPlayer()
                delay(500)
            }
        }
        if (stage == 2) refreshHome()
    }

    private fun syncPlayer() {
        val player = controller ?: return
        if (queue.isEmpty() && player.mediaItemCount > 0) {
            val known = (downloads + recent + favorites + seed).associateBy { it.id }
            queue = (0 until player.mediaItemCount).map { index ->
                val item = player.getMediaItemAt(index)
                known[item.mediaId] ?: Song(item.mediaId, item.mediaMetadata.title?.toString().orEmpty(), item.mediaMetadata.artist?.toString().orEmpty(), image = item.mediaMetadata.artworkUri?.toString().orEmpty(), sources = item.localConfiguration?.uri?.toString()?.let { listOf(AudioSource(quality, it)) }.orEmpty())
            }
        }
        playing = player.isPlaying
        buffering = player.playbackState == Player.STATE_BUFFERING
        position = player.currentPosition.coerceAtLeast(0)
        duration = player.duration.takeIf { it > 0 } ?: ((current?.duration ?: 0) * 1000)
        shuffle = player.shuffleModeEnabled
        repeat = player.repeatMode
        val id = player.currentMediaItem?.mediaId
        val song = queue.find { it.id == id }
        if (song != null && current?.id != song.id) {
            current = song
            recent = (listOf(song) + recent.filterNot { it.id == song.id }).take(12)
            repo.saveSongs("recent", recent)
        }
    }

    fun startWelcome() { stage = 1 }
    fun finishOnboarding(selected: Set<String>) {
        updateLanguages(selected)
        repo.onboarded = true; stage = 2; refreshHome()
    }
    fun open(destination: Screen) {
        if (destination == screen) return
        if (destination == Screen.Player && screen != Screen.Queue) playerOrigin = screen
        backStack += screen
        screen = destination
    }
    fun switchTab(destination: Screen) { backStack.clear(); screen = destination; tab = destination }
    fun back(): Boolean {
        if (stage == 1) { stage = 0; return true }
        if (backStack.isNotEmpty()) { screen = backStack.removeAt(backStack.lastIndex); return true }
        if (screen != Screen.Home) { switchTab(Screen.Home); return true }
        return false
    }
    fun hasBack() = stage == 1 || backStack.isNotEmpty() || screen != Screen.Home
    fun message(text: String) { _messages.tryEmit(text) }
    fun updateTheme(value: String) { theme = value; repo.theme = value }
    fun updateQuality(value: String) { quality = value; repo.quality = value; message("Streaming quality set to $value. Applies to the next track.") }
    fun updateLanguages(value: Set<String>) { if (value.isNotEmpty()) { languages = value; repo.languages = value } }

    fun refreshHome() {
        if (refreshing) return
        viewModelScope.launch {
            refreshing = true; homeError = null
            val result = supervisorScope {
                languages.map { language -> async {
                    language to runCatching { repo.searchSongs("$language hit songs", 15) }
                } }.awaitAll()
            }
            trending = result.mapNotNull { (language, r) -> r.getOrNull()?.takeIf { it.isNotEmpty() }?.let { language to it } }.toMap()
            if (trending.isEmpty()) homeError = "Couldn’t refresh your music. Showing the saved catalog."
            refreshing = false
        }
    }

    fun search(text: String = query, kind: String = searchKind, immediate: Boolean = false) {
        query = text; searchKind = kind; searchJob?.cancel()
        val generation = ++searchGeneration
        searchError = null
        if (text.isBlank()) { searchResults = emptyList(); catalogResults = emptyList(); searching = false; return }
        searching = true
        searchJob = viewModelScope.launch {
            if (!immediate) delay(400)
            try {
                if (kind == "songs") { searchResults = repo.searchSongs(text); catalogResults = emptyList() }
                else { catalogResults = repo.searchCatalog(text, kind); searchResults = emptyList() }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (_: Exception) { searchError = "Search couldn’t connect. Check your internet connection and try again." }
            finally { if (generation == searchGeneration) searching = false }
        }
    }

    fun openPlaylist(playlist: Playlist) {
        selectedPlaylist = playlist; detailItem = null; detailError = null; detailLoading = false; open(Screen.Playlist)
    }
    fun openCatalog(item: CatalogItem) {
        selectedPlaylist = Playlist(item.id, item.title, description = item.subtitle, personal = false)
        detailItem = item; open(Screen.Playlist); loadDetail()
    }
    fun loadDetail() {
        val item = detailItem ?: return
        viewModelScope.launch {
            detailLoading = true; detailError = null
            runCatching { repo.catalogSongs(item) }
                .onSuccess { selectedPlaylist = selectedPlaylist?.copy(songs = it) }
                .onFailure { detailError = "Couldn’t load these songs. Please try again." }
            detailLoading = false
        }
    }

    fun createPlaylist(name: String, songs: List<Song> = emptyList()): Playlist? {
        val clean = name.trim()
        if (clean.isEmpty()) return null
        val playlist = Playlist(UUID.randomUUID().toString(), clean.take(80), songs)
        playlists = playlists + playlist; repo.savePlaylists(playlists)
        message("${playlist.name} created")
        return playlist
    }
    fun renamePlaylist(id: String, name: String) {
        if (name.isBlank()) return
        playlists = playlists.map { if (it.id == id) it.copy(name = name.trim().take(80)) else it }
        selectedPlaylist = playlists.find { it.id == id }; repo.savePlaylists(playlists)
    }
    fun deletePlaylist(id: String) {
        playlists = playlists.filterNot { it.id == id }; repo.savePlaylists(playlists)
        back(); message("Playlist deleted")
    }
    fun savePlaylist(playlist: Playlist) {
        createPlaylist(playlist.name, playlist.songs)?.let { selectedPlaylist = it }
    }
    fun addSong(playlist: Playlist, song: Song) {
        if (playlist.songs.any { it.id == song.id }) { message("Already in ${playlist.name}"); return }
        playlists = playlists.map { if (it.id == playlist.id) it.copy(songs = it.songs + song) else it }
        if (selectedPlaylist?.id == playlist.id) selectedPlaylist = playlists.find { it.id == playlist.id }
        repo.savePlaylists(playlists); addToPlaylist = null; message("Added to ${playlist.name}")
    }
    fun removeSong(playlist: Playlist, song: Song) {
        playlists = playlists.map { if (it.id == playlist.id) it.copy(songs = it.songs.filterNot { s -> s.id == song.id }) else it }
        selectedPlaylist = playlists.find { it.id == playlist.id }; repo.savePlaylists(playlists)
        message("Removed from playlist")
    }
    fun toggleFavorite(song: Song) {
        val exists = isFavorite(song)
        favorites = if (exists) favorites.filterNot { it.id == song.id } else listOf(song) + favorites
        repo.saveSongs("favorites", favorites)
        message(if (exists) "Removed from liked songs" else "Added to liked songs")
    }
    fun isFavorite(song: Song) = favorites.any { it.id == song.id }
    fun isDownloaded(song: Song) = downloads.any { it.id == song.id }

    fun download(song: Song) {
        if (isDownloaded(song)) { message("Already available offline"); return }
        if (song.id in downloading) return
        viewModelScope.launch {
            downloading = downloading + song.id
            message("Downloading ${song.title}…")
            runCatching { repo.download(song, quality) }
                .onSuccess { saved -> downloads = listOf(saved) + downloads; repo.saveSongs("downloads", downloads); message("${song.title} is ready offline") }
                .onFailure { message(it.message ?: "Download failed. Please try again.") }
            downloading = downloading - song.id
        }
    }
    fun removeDownload(song: Song) {
        repo.removeDownload(song); downloads = downloads.filterNot { it.id == song.id }; repo.saveSongs("downloads", downloads)
        message("Download removed")
    }
    fun clearDownloads() { downloads.forEach(repo::removeDownload); downloads = emptyList(); repo.saveSongs("downloads", downloads); message("Downloads removed") }

    private fun Song.mediaItem(): MediaItem? {
        val local = downloads.find { it.id == id }?.takeIf { java.io.File(java.net.URI(it.localPath)).exists() }
        val url = (local ?: copy(localPath = "")).audioUrl(quality) ?: return null
        return MediaItem.Builder().setMediaId(id).setUri(url).setMediaMetadata(
            MediaMetadata.Builder().setTitle(title).setArtist(artist).setAlbumTitle(album)
                .setArtworkUri(image.takeIf { it.startsWith("https://") }?.let(Uri::parse)).build()
        ).build()
    }

    fun play(song: Song, songs: List<Song> = listOf(song), expand: Boolean = false) {
        val player = controller ?: run { message("The player is starting. Try again in a moment."); return }
        val valid = songs.mapNotNull { s -> s.mediaItem()?.let { s to it } }
        val index = valid.indexOfFirst { it.first.id == song.id }
        if (index < 0) { message("This track is not available for playback."); return }
        playbackError = null
        queue = valid.map { it.first }
        player.setMediaItems(valid.map { it.second }, index, 0)
        player.prepare(); player.play(); syncPlayer()
        if (expand && screen != Screen.Player) open(Screen.Player)
    }
    fun togglePlay() {
        controller?.let {
            if (it.isPlaying) it.pause() else {
                if (it.playbackState == Player.STATE_ENDED) it.seekToDefaultPosition()
                if (it.playbackState == Player.STATE_IDLE) it.prepare()
                it.play()
            }
        }
    }
    fun retryPlayback() { playbackError = null; controller?.prepare(); controller?.play() }
    fun next() { controller?.let { if (it.hasNextMediaItem()) it.seekToNextMediaItem() else message("You’re at the end of the queue") } }
    fun previous() { controller?.let { if (it.currentPosition > 3000) it.seekTo(0) else if (it.hasPreviousMediaItem()) it.seekToPreviousMediaItem() else it.seekTo(0) } }
    fun seek(value: Long) { controller?.seekTo(value.coerceIn(0, duration.coerceAtLeast(0))); position = value }
    fun toggleShuffle() { controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled }; syncPlayer() }
    fun toggleRepeat() { controller?.let { it.repeatMode = when (it.repeatMode) { Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL; Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE; else -> Player.REPEAT_MODE_OFF } }; syncPlayer() }
    fun addToQueue(song: Song) {
        val item = song.mediaItem() ?: run { message("This track is not available for playback."); return }
        if (queue.isEmpty()) { play(song); return }
        queue = queue + song; controller?.addMediaItem(item); message("Added to queue")
    }
    fun removeFromQueue(index: Int) {
        if (index !in queue.indices || queue[index].id == current?.id) return
        controller?.removeMediaItem(index); queue = queue.filterIndexed { i, _ -> i != index }
    }
    fun moveUp(index: Int) {
        if (index <= 0 || index >= queue.size) return
        val updated = queue.toMutableList(); val song = updated.removeAt(index); updated.add(index - 1, song)
        controller?.moveMediaItem(index, index - 1); queue = updated
    }
    fun playQueueIndex(index: Int) { controller?.seekToDefaultPosition(index); controller?.play() }
    fun setSleepTimer(minutes: Int) {
        sleepJob?.cancel(); sleepMinutes = minutes
        if (minutes > 0) {
            sleepJob = viewModelScope.launch { delay(minutes * 60_000L); controller?.pause(); sleepMinutes = 0; message("Sleep timer paused your music") }
            message("Music will pause in $minutes minutes")
        } else message("Sleep timer is off")
    }
    override fun onCleared() { MediaController.releaseFuture(controllerFuture); super.onCleared() }
}
