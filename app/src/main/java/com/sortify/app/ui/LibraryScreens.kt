package com.sortify.app.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sortify.app.Screen
import com.sortify.app.SortifyViewModel
import com.sortify.app.data.Playlist
import com.sortify.app.data.Song

@Composable
fun LibraryScreen(vm: SortifyViewModel) {
    var filter by rememberSaveable { mutableStateOf("All") }
    var alphabetical by rememberSaveable { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandMark(Modifier.padding(end = 12.dp))
                Text("Your library", Modifier.weight(1f), style = MaterialTheme.typography.headlineLarge)
                ActionIcon(Icons.Rounded.Add, "Create playlist") { vm.switchTab(Screen.Create) }
                ActionIcon(Icons.Rounded.Settings, "Settings") { vm.open(Screen.Settings) }
            }
            Text("All the music you keep coming back to.", Modifier.padding(top = 14.dp, bottom = 14.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("All", "Playlists", "Liked", "Downloads")) { label -> CategoryChip(filter == label, { filter = label }, label = { Text(label) }) }
            }
            Spacer(Modifier.height(18.dp))
        }
        if (filter == "All" || filter == "Liked") item {
            LibraryShortcut(Icons.Rounded.Favorite, "Liked songs", "${vm.favorites.size} songs · All your favourites", MaterialTheme.colorScheme.primaryContainer) { vm.open(Screen.Favorites) }
        }
        if (filter == "All" || filter == "Downloads") item {
            LibraryShortcut(Icons.Rounded.DownloadDone, "Downloads", "${vm.downloads.size} songs · Ready when you’re offline", MaterialTheme.colorScheme.secondaryContainer) { vm.open(Screen.Downloads) }
        }
        if (filter == "All" || filter == "Playlists") {
            item {
                Row(Modifier.padding(top = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Your playlists", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                    TextButton({ alphabetical = !alphabetical }) { Icon(Icons.AutoMirrored.Rounded.Sort, null, Modifier.size(18.dp)); Text(if (alphabetical) " A–Z" else " Recent") }
                }
            }
            if (vm.playlists.isEmpty()) item { EmptyState(Icons.Rounded.LibraryMusic, "A home for your music", "Start a playlist for a mood, a moment, or the everyday.", "Create a playlist") { vm.switchTab(Screen.Create) } }
            else items(if (alphabetical) vm.playlists.sortedBy { it.name.lowercase() } else vm.playlists.reversed(), key = { it.id }) { playlist -> PlaylistRow(playlist) { vm.openPlaylist(playlist) } }
        }
        if (filter == "All" && vm.recent.isNotEmpty()) {
            item { SectionTitle("Recently played") }
            items(vm.recent.take(5)) { SongRow(it, vm, vm.recent, Modifier.padding(bottom = 10.dp)) }
        }
    }
}

@Composable
private fun LibraryShortcut(icon: ImageVector, title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(70.dp).clip(RoundedCornerShape(14.dp)).background(color), contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(30.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer) }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CollectionScreen(vm: SortifyViewModel, downloads: Boolean) {
    val songs = if (downloads) vm.downloads else vm.favorites
    Column(Modifier.fillMaxSize()) {
        BackHeader(if (downloads) "Downloads" else "Liked songs", { vm.back() })
        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Box(Modifier.size(136.dp).clip(RoundedCornerShape(22.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(if (downloads) Icons.Rounded.DownloadDone else Icons.Rounded.Favorite, null, Modifier.size(54.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Row(Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(if (downloads) "Music to go." else "Your kind of music.", style = MaterialTheme.typography.headlineMedium)
                        Text("${songs.size} songs${if (downloads) " · Available offline" else " · Made by you"}", Modifier.padding(top = 7.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (songs.isNotEmpty()) PlayButton { vm.play(songs.first(), songs) }
                }
            }
            if (songs.isEmpty()) item { EmptyState(if (downloads) Icons.Rounded.CloudDownload else Icons.Rounded.FavoriteBorder, if (downloads) "Take your favourites offline" else "Love a song? Keep it here.", if (downloads) "Open a song’s options and choose Download. Your music will be ready without a connection." else "Tap the heart on any song. You’ll find all your favourites right here.", "Explore music") { vm.switchTab(Screen.Search) } }
            items(songs, key = { it.id }) { SongRow(it, vm, songs) }
        }
    }
}

@Composable
fun PlaylistScreen(vm: SortifyViewModel) {
    val playlist = vm.selectedPlaylist ?: return
    val context = LocalContext.current
    var menu by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf("") }
    var name by remember(playlist.id) { mutableStateOf(playlist.name) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Box(Modifier.fillMaxWidth().height(300.dp)) {
                if (playlist.songs.size >= 3) Row(Modifier.fillMaxSize()) {
                    playlist.songs.take(3).forEach { Artwork(it.art(), Modifier.weight(1f).fillMaxHeight()) }
                } else CoverCollage(playlist.songs, Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .24f), Color.Transparent, MaterialTheme.colorScheme.background))))
                FilledIconButton({ vm.back() }, Modifier.padding(12.dp).align(Alignment.TopStart), colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.Black.copy(alpha = .45f), contentColor = Color.White)) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                Column(Modifier.align(Alignment.BottomStart).padding(start = 20.dp, end = 20.dp, bottom = 12.dp)) {
                    Text(playlist.name, style = MaterialTheme.typography.headlineLarge, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(playlist.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    BrandMark(Modifier.height(24.dp))
                    Text("${if (playlist.personal) "You" else "Sortify"} · ${playlist.songs.size} songs", Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    ActionIcon(Icons.Rounded.Shuffle, "Shuffle playlist", tint = if (vm.shuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) {
                        if (playlist.songs.isNotEmpty()) { vm.play(playlist.songs.random(), playlist.songs); if (!vm.shuffle) vm.toggleShuffle() }
                    }
                    if (!playlist.personal) ActionIcon(Icons.Rounded.AddCircleOutline, "Save playlist") { vm.savePlaylist(playlist) }
                    ActionIcon(Icons.Rounded.Download, "Download playlist") {
                        if (playlist.songs.isEmpty()) vm.message("Add songs to this playlist first") else playlist.songs.forEach(vm::download)
                    }
                    ActionIcon(Icons.Rounded.Share, "Share playlist") {
                        val body = playlist.name + "\n\n" + playlist.songs.joinToString("\n") { "${it.title} — ${it.artist}" }
                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, body), "Share playlist"))
                    }
                    Box {
                        ActionIcon(Icons.Rounded.MoreVert, "Playlist options") { menu = true }
                        DropdownMenu(menu, { menu = false }) {
                            if (playlist.personal) {
                                DropdownMenuItem({ Text("Rename playlist") }, { menu = false; name = playlist.name; dialog = "rename" })
                                DropdownMenuItem({ Text("Delete playlist") }, { menu = false; dialog = "delete" })
                            }
                            DropdownMenuItem({ Text("Add all to queue") }, { menu = false; playlist.songs.forEach(vm::addToQueue) })
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    if (playlist.songs.isNotEmpty()) PlayButton { vm.play(playlist.songs.first(), playlist.songs) }
                }
            }
        }
        if (vm.detailLoading) item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        if (vm.detailError != null) item { Box(Modifier.padding(horizontal = 20.dp)) { ErrorNotice(vm.detailError!!, vm::loadDetail) } }
        if (playlist.songs.isEmpty() && !vm.detailLoading && vm.detailError == null) item { EmptyState(Icons.AutoMirrored.Rounded.PlaylistAdd, "Every mix starts somewhere", "Find a song, open its options, and add it to this playlist.", "Find songs") { vm.switchTab(Screen.Search) } }
        items(playlist.songs, key = { it.id }) { song -> SongRow(song, vm, playlist.songs, Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) }
        if (playlist.songs.isNotEmpty()) item {
            TextButton({ vm.switchTab(Screen.Search) }, Modifier.padding(start = 12.dp, top = 12.dp)) { Text("Find more music"); Icon(Icons.Rounded.ArrowOutward, null, Modifier.padding(start = 8.dp).size(18.dp)) }
        }
    }
    if (dialog == "rename") AlertDialog(onDismissRequest = { dialog = "" }, title = { Text("Rename playlist") }, text = { OutlinedTextField(name, { name = it.take(80) }, label = { Text("Playlist name") }, singleLine = true) }, confirmButton = { TextButton({ vm.renamePlaylist(playlist.id, name); dialog = "" }, enabled = name.isNotBlank()) { Text("Save") } }, dismissButton = { TextButton({ dialog = "" }) { Text("Cancel") } })
    if (dialog == "delete") AlertDialog(onDismissRequest = { dialog = "" }, title = { Text("Delete ${playlist.name}?") }, text = { Text("This removes the playlist from your library. Your liked songs and downloads are kept.") }, confirmButton = { TextButton({ dialog = ""; vm.deletePlaylist(playlist.id) }) { Text("Delete") } }, dismissButton = { TextButton({ dialog = "" }) { Text("Cancel") } })
}

@Composable
fun CreateScreen(vm: SortifyViewModel) {
    var name by rememberSaveable { mutableStateOf("") }
    var selected by rememberSaveable { mutableStateOf(emptyList<String>()) }
    val songs = (vm.recent + vm.seed).distinctBy { it.id }
    Column(Modifier.fillMaxSize()) {
        BackHeader("Create a playlist", { vm.back() })
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("A soundtrack\nfor anything.", style = MaterialTheme.typography.headlineLarge)
                Text("Your commute. Your late nights. Your new obsession.", Modifier.padding(top = 12.dp, bottom = 24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    CoverCollage(songs.filter { it.id in selected }, Modifier.size(90.dp))
                    Text("The cover comes from\nthe music you add.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedTextField(name, { name = it.take(80) }, Modifier.fillMaxWidth().padding(top = 24.dp), label = { Text("Playlist name") }, placeholder = { Text("e.g. The long way home") }, singleLine = true, shape = RoundedCornerShape(14.dp), supportingText = { Text("${name.length}/80") })
                SectionTitle("Add a few favourites")
                Text("Optional · ${selected.size} songs selected", Modifier.padding(bottom = 12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(songs.take(12), key = { it.id }) { song ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { selected = if (song.id in selected) selected - song.id else selected + song.id }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Artwork(song.art(), Modifier.size(50.dp))
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(song.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(song.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    Checkbox(song.id in selected, { checked -> selected = if (checked) selected + song.id else selected - song.id })
                }
            }
        }
        Button({ vm.createPlaylist(name, songs.filter { it.id in selected })?.let { vm.switchTab(Screen.Library); vm.openPlaylist(it) } }, Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp).heightIn(min = 54.dp), enabled = name.isNotBlank()) { Text("Create playlist${if (selected.isNotEmpty()) " · ${selected.size} songs" else ""}") }
    }
}
