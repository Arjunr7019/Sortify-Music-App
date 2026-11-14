package com.sortify.app.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.sortify.app.Screen
import com.sortify.app.SortifyViewModel
import com.sortify.app.data.timeLabel

@Composable
fun MiniPlayer(vm: SortifyViewModel) {
    val song = vm.current ?: return
    Surface(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.secondaryContainer, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.clickable { vm.open(Screen.Player) }) {
            Row(Modifier.padding(start = 8.dp, top = 7.dp, bottom = 5.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Artwork(song.art(), Modifier.size(48.dp))
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(song.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                ActionIcon(if (vm.isFavorite(song)) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, if (vm.isFavorite(song)) "Unlike song" else "Like song", tint = if (vm.isFavorite(song)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) { vm.toggleFavorite(song) }
                if (vm.buffering) CircularProgressIndicator(Modifier.padding(12.dp).size(24.dp), strokeWidth = 2.dp)
                else ActionIcon(if (vm.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (vm.playing) "Pause" else "Play", onClick = vm::togglePlay)
            }
            LinearProgressIndicator(progress = { if (vm.duration > 0) vm.position.toFloat() / vm.duration else 0f }, Modifier.fillMaxWidth().height(2.dp).padding(horizontal = 12.dp), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
fun PlayerScreen(vm: SortifyViewModel) {
    val song = vm.current ?: return
    var timerDialog by remember { mutableStateOf(false) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val coverSize = minOf(maxWidth - 56.dp, maxHeight * .39f, 390.dp)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 26.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().padding(bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                ActionIcon(Icons.Rounded.KeyboardArrowDown, "Close player") { vm.back() }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Now playing", style = MaterialTheme.typography.titleSmall)
                    Text(song.album.ifBlank { "Your music" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                ActionIcon(Icons.Rounded.MoreHoriz, "Song options") { vm.songMenu = song }
            }
            Artwork(song.art(), Modifier.size(coverSize), description = "Cover for ${song.title}")
            Row(Modifier.fillMaxWidth().padding(top = 28.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(song.title, style = MaterialTheme.typography.headlineMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, Modifier.padding(top = 6.dp), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                ActionIcon(if (vm.isFavorite(song)) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, if (vm.isFavorite(song)) "Unlike song" else "Like song", tint = if (vm.isFavorite(song)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) { vm.toggleFavorite(song) }
            }
            if (vm.playbackError != null) Box(Modifier.padding(top = 16.dp)) { ErrorNotice(vm.playbackError!!, vm::retryPlayback) }
            var seekPosition by remember(song.id) { mutableStateOf<Float?>(null) }
            Slider(value = seekPosition ?: vm.position.toFloat().coerceIn(0f, vm.duration.toFloat().coerceAtLeast(1f)), onValueChange = { seekPosition = it }, onValueChangeFinished = { seekPosition?.let { vm.seek(it.toLong()) }; seekPosition = null }, valueRange = 0f..vm.duration.toFloat().coerceAtLeast(1f), modifier = Modifier.fillMaxWidth().padding(top = 16.dp), enabled = vm.duration > 0)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(timeLabel((seekPosition?.toLong() ?: vm.position) / 1000), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(timeLabel(vm.duration / 1000), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                ActionIcon(Icons.Rounded.Shuffle, if (vm.shuffle) "Turn shuffle off" else "Turn shuffle on", tint = if (vm.shuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, onClick = vm::toggleShuffle)
                ActionIcon(Icons.Rounded.SkipPrevious, "Previous track", onClick = vm::previous)
                PlayButton(vm.playing, size = 74.dp, loading = vm.buffering, onClick = vm::togglePlay)
                ActionIcon(Icons.Rounded.SkipNext, "Next track", onClick = vm::next)
                ActionIcon(if (vm.repeat == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat, "Repeat: ${when(vm.repeat) { Player.REPEAT_MODE_ONE -> "one song"; Player.REPEAT_MODE_ALL -> "all songs"; else -> "off" }}", tint = if (vm.repeat != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, onClick = vm::toggleRepeat)
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                ActionIcon(if (vm.isDownloaded(song)) Icons.Rounded.DownloadDone else Icons.Rounded.Download, if (vm.isDownloaded(song)) "Downloaded" else "Download song", tint = if (vm.isDownloaded(song)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) { vm.download(song) }
                TextButton({ timerDialog = true }) { Icon(Icons.Rounded.Bedtime, null, Modifier.size(17.dp)); Text(if (vm.sleepMinutes > 0) " ${vm.sleepMinutes} min" else " Sleep timer", style = MaterialTheme.typography.labelMedium) }
                ActionIcon(Icons.AutoMirrored.Rounded.QueueMusic, "Open queue") { vm.open(Screen.Queue) }
            }
        }
    }
    if (timerDialog) ChoiceDialog("Sleep timer", listOf("Off", "15 minutes", "30 minutes", "60 minutes"), if (vm.sleepMinutes == 0) "Off" else "${vm.sleepMinutes} minutes", { vm.setSleepTimer(it.substringBefore(" ").toIntOrNull() ?: 0); timerDialog = false }, { timerDialog = false })
}

@Composable
fun QueueScreen(vm: SortifyViewModel) {
    Column(Modifier.fillMaxSize()) {
        BackHeader("Play queue", { vm.back() })
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("Keep the music going.", style = MaterialTheme.typography.headlineMedium)
                Text("${vm.queue.size} songs · ${if (vm.shuffle) "Shuffle on" else "Playing in order"}", Modifier.padding(top = 10.dp, bottom = 22.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            itemsIndexed(vm.queue) { index, song ->
                SongRow(song, vm, vm.queue, onPlay = { vm.playQueueIndex(index) }, trailing = {
                    if (song.id == vm.current?.id) ActionIcon(if (vm.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Toggle current song", tint = Color(0xFF00210D), onClick = vm::togglePlay)
                    else {
                        ActionIcon(Icons.Rounded.KeyboardArrowUp, "Move ${song.title} earlier") { vm.moveUp(index) }
                        ActionIcon(Icons.Rounded.Close, "Remove ${song.title} from queue") { vm.removeFromQueue(index) }
                    }
                })
            }
            item { TextButton({ vm.switchTab(Screen.Search) }) { Icon(Icons.Rounded.Add, null); Text("Add more music") } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSheets(vm: SortifyViewModel) {
    val context = LocalContext.current
    vm.songMenu?.let { song ->
        ModalBottomSheet(onDismissRequest = { vm.songMenu = null }, containerColor = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(horizontal = 22.dp).navigationBarsPadding()) {
                Row(Modifier.padding(bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Artwork(song.art(), Modifier.size(64.dp))
                    Column(Modifier.weight(1f).padding(start = 14.dp)) { Text(song.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(song.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                }
                SheetAction(if (vm.isFavorite(song)) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, if (vm.isFavorite(song)) "Remove from liked songs" else "Add to liked songs") { vm.toggleFavorite(song); vm.songMenu = null }
                SheetAction(Icons.AutoMirrored.Rounded.PlaylistAdd, "Add to playlist") { vm.songMenu = null; vm.addToPlaylist = song }
                SheetAction(Icons.AutoMirrored.Rounded.QueueMusic, "Add to queue") { vm.addToQueue(song); vm.songMenu = null }
                SheetAction(if (vm.isDownloaded(song)) Icons.Rounded.DeleteOutline else Icons.Rounded.Download, if (vm.isDownloaded(song)) "Remove download" else if (song.id in vm.downloading) "Downloading…" else "Download song") {
                    if (vm.isDownloaded(song)) vm.downloads.find { it.id == song.id }?.let(vm::removeDownload) else vm.download(song)
                    vm.songMenu = null
                }
                SheetAction(Icons.Rounded.Share, "Share song") {
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, "${song.title} — ${song.artist}"), "Share song")); vm.songMenu = null
                }
                val playlist = vm.selectedPlaylist
                if (vm.screen == Screen.Playlist && playlist?.personal == true && playlist.songs.any { it.id == song.id }) SheetAction(Icons.Rounded.RemoveCircleOutline, "Remove from this playlist") { vm.removeSong(playlist, song); vm.songMenu = null }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
    vm.addToPlaylist?.let { song ->
        var create by remember { mutableStateOf(false) }
        var name by remember { mutableStateOf("") }
        ModalBottomSheet(onDismissRequest = { vm.addToPlaylist = null }, containerColor = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).navigationBarsPadding().imePadding()) {
                Text("Add to playlist", style = MaterialTheme.typography.headlineSmall)
                Text(song.title, Modifier.padding(top = 8.dp, bottom = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (create) {
                    OutlinedTextField(name, { name = it.take(80) }, Modifier.fillMaxWidth(), label = { Text("Playlist name") }, singleLine = true)
                    Button({ vm.createPlaylist(name, listOf(song)); vm.addToPlaylist = null }, Modifier.fillMaxWidth().padding(vertical = 14.dp), enabled = name.isNotBlank()) { Text("Create and add song") }
                } else {
                    SheetAction(Icons.Rounded.Add, "New playlist") { create = true }
                    LazyColumn(Modifier.heightIn(max = 350.dp), contentPadding = PaddingValues(bottom = 22.dp)) {
                        itemsIndexed(vm.playlists) { _, playlist -> PlaylistRow(playlist) { vm.addSong(playlist, song) } }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetAction(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 17.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.padding(end = 18.dp)); Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
