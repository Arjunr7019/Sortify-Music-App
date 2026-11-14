package com.sortify.app.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sortify.app.R
import com.sortify.app.SortifyViewModel
import com.sortify.app.data.Playlist
import com.sortify.app.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.security.MessageDigest

private object ImageCache {
    val memory = object : LruCache<String, Bitmap>(20 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }
}

@Composable
fun Artwork(model: String, modifier: Modifier = Modifier, description: String? = null, circle: Boolean = false, cornerRadius: Dp = 14.dp) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = ImageCache.memory.get(model), model) {
        if (model.isBlank()) { value = null; return@produceState }
        value = withContext(Dispatchers.IO) {
            runCatching {
                ImageCache.memory.get(model) ?: if (model.startsWith("artwork/")) {
                    context.assets.open(model).use(BitmapFactory::decodeStream)
                } else {
                    require(model.startsWith("https://"))
                    val name = MessageDigest.getInstance("SHA-256").digest(model.toByteArray()).joinToString("") { "%02x".format(it) }
                    val dir = File(context.cacheDir, "artwork").apply { mkdirs() }
                    val cached = File(dir, name)
                    val existing = cached.takeIf(File::exists)?.let { BitmapFactory.decodeFile(it.path) }
                    existing ?: run {
                        val connection = URI(model).toURL().openConnection() as HttpURLConnection
                        connection.connectTimeout = 10_000; connection.readTimeout = 10_000
                        try {
                            check(connection.responseCode in 200..299)
                            val data = connection.inputStream.use { it.readBytes() }
                            BitmapFactory.decodeByteArray(data, 0, data.size)?.also { image ->
                                // Cache original bytes; images are never re-encoded or altered.
                                if (dir.listFiles().orEmpty().sumOf { it.length() } < 64L * 1024 * 1024) cached.writeBytes(data)
                            }
                        } finally { connection.disconnect() }
                    }
                }
            }.getOrNull()?.also { ImageCache.memory.put(model, it) }
        }
    }
    Box(modifier.clip(if (circle) CircleShape else RoundedCornerShape(cornerRadius)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        if (bitmap != null) Image(bitmap!!.asImageBitmap(), description, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Icon(Icons.Rounded.MusicNote, description, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

fun Song.art() = bundledArt.ifBlank { image }

@Composable
fun BrandMark(modifier: Modifier = Modifier, wordmark: Boolean = false) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        Icon(painterResource(R.drawable.sortify_mark), if (wordmark) null else "Sortify", Modifier.size(29.dp, 40.dp), tint = MaterialTheme.colorScheme.primary)
        if (wordmark) Text("Sortify", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ActionIcon(icon: ImageVector, label: String, modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.onSurface, onClick: () -> Unit) {
    IconButton(onClick, modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) { Icon(icon, label, tint = tint) }
}

@Composable
fun BackHeader(title: String, onBack: () -> Unit, action: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 12.dp, top = 4.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        ActionIcon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onClick = onBack)
        Text(title, Modifier.weight(1f).padding(start = 4.dp), style = MaterialTheme.typography.titleLarge)
        action()
    }
}

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
        if (action != null) TextButton(onAction, contentPadding = PaddingValues(start = 10.dp)) {
            Text(action); Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.padding(start = 3.dp).size(18.dp))
        }
    }
}

@Composable
fun PlayButton(playing: Boolean = false, size: Dp = 60.dp, loading: Boolean = false, onClick: () -> Unit) {
    FilledIconButton(onClick, Modifier.size(size), colors = IconButtonDefaults.filledIconButtonColors(containerColor = SortifyGreen, contentColor = Color(0xFF00210D))) {
        if (loading) CircularProgressIndicator(Modifier.size(25.dp), color = Color(0xFF00210D), strokeWidth = 2.dp)
        else Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (playing) "Pause" else "Play", Modifier.size(size * .53f))
    }
}

@Composable
fun SongRow(song: Song, vm: SortifyViewModel, songs: List<Song>, modifier: Modifier = Modifier, onPlay: (() -> Unit)? = null, trailing: @Composable (() -> Unit)? = null) {
    val active = vm.current?.id == song.id
    val background = if (active) SortifyGreen else MaterialTheme.colorScheme.surface
    val foreground = if (active) Color(0xFF00210D) else MaterialTheme.colorScheme.onSurface
    Row(modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(background)
        .clickable(role = Role.Button, onClickLabel = "Play ${song.title}") { if (onPlay != null) onPlay() else if (active) vm.togglePlay() else vm.play(song, songs) }
        .padding(start = 10.dp, top = 9.dp, bottom = 9.dp, end = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Artwork(song.art(), Modifier.size(54.dp))
        Column(Modifier.weight(1f).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(song.title, style = MaterialTheme.typography.titleMedium, color = foreground, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (vm.isDownloaded(song)) Icon(Icons.Rounded.CheckCircle, "Downloaded", Modifier.padding(end = 4.dp).size(13.dp), tint = if (active) foreground else MaterialTheme.colorScheme.primary)
                Text(song.artist, style = MaterialTheme.typography.bodyMedium, color = if (active) foreground else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (active && vm.playing) Equalizer(foreground)
        if (trailing != null) trailing() else ActionIcon(Icons.Rounded.MoreVert, "Options for ${song.title}", tint = foreground) { vm.songMenu = song }
    }
}

@Composable
fun Equalizer(color: Color = MaterialTheme.colorScheme.primary) {
    Row(Modifier.size(22.dp, 24.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf(10, 22, 15, 8).forEach { h -> Box(Modifier.width(3.dp).height(h.dp).clip(CircleShape).background(color)) }
    }
}

@Composable
fun CoverCollage(songs: List<Song>, modifier: Modifier = Modifier) {
    if (songs.size < 4) Artwork(songs.firstOrNull()?.art().orEmpty(), modifier)
    else Column(modifier.clip(RoundedCornerShape(14.dp))) {
        for (row in 0..1) Row(Modifier.weight(1f)) {
            for (column in 0..1) Artwork(songs[row * 2 + column].art(), Modifier.weight(1f).fillMaxHeight(), cornerRadius = 0.dp)
        }
    }
}

@Composable
fun PlaylistRow(playlist: Playlist, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        CoverCollage(playlist.songs, Modifier.size(68.dp))
        Column(Modifier.weight(1f).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(playlist.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Playlist · ${playlist.songs.size} songs", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, text: String, action: String? = null, onAction: () -> Unit = {}) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.size(82.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(34.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        if (action != null) Button(onAction) { Text(action) }
    }
}

@Composable
fun ErrorNotice(text: String, onRetry: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(start = 14.dp, top = 7.dp, bottom = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        TextButton(onRetry) { Text("Retry") }
    }
}

@Composable
fun ScreenBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = .38f), MaterialTheme.colorScheme.background), endY = 1000f)), content = content)
}

@Composable
fun CategoryChip(selected: Boolean, onClick: () -> Unit, label: @Composable () -> Unit, modifier: Modifier = Modifier) {
    FilterChip(selected, onClick, label, modifier.heightIn(min = 48.dp), shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary))
}
