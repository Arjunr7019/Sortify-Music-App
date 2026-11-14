package com.sortify.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.sortify.app.Screen
import com.sortify.app.SortifyViewModel
import com.sortify.app.data.Playlist
import com.sortify.app.data.Song
import java.time.LocalTime

fun dailyMixes(songs: List<Song>): List<Playlist> = listOf(
    Playlist("daily", "Your daily mix", songs.take(8), "A little familiar. A little unexpected.", false),
    Playlist("late", "After hours", songs.filterIndexed { i, _ -> i % 2 == 0 }.reversed(), "For the nights that need a soundtrack.", false),
    Playlist("feel", "Feel good favourites", songs.filterIndexed { i, _ -> i % 2 == 1 }, "Good songs. Better moods.", false),
    Playlist("hindi", "Desi favourites", songs.filter { it.language.equals("hindi", true) }, "Find your next favourite, closer to home.", false),
)

@Composable
fun WelcomeScreen(vm: SortifyViewModel) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxHeight < 650.dp
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            BrandMark(wordmark = true)
            Box(Modifier.fillMaxWidth().height(if (compact) 240.dp else 340.dp).padding(vertical = 28.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(if (compact) 210.dp else 300.dp).clip(CircleShape).background(Brush.radialGradient(listOf(SortifyGreen.copy(alpha = .18f), Color.Transparent))))
                Icon(androidx.compose.ui.res.painterResource(com.sortify.app.R.drawable.sortify_mark), null, Modifier.size(if (compact) 145.dp else 195.dp, if (compact) 215.dp else 290.dp), tint = SortifyGreen)
            }
            Text(buildAnnotatedString {
                append("Your music.\n")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("Your world.") }
            }, Modifier.fillMaxWidth(), style = MaterialTheme.typography.displayLarge)
            Text("Discover something you love.\nKeep it with you, wherever you go.", Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 30.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
            Button(vm::startWelcome, Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text("Get started", Modifier.weight(1f)); Icon(Icons.AutoMirrored.Rounded.ArrowForward, null)
            }
            Text("No account needed. Just press play.", Modifier.padding(top = 16.dp, bottom = 16.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

val availableLanguages = listOf("English", "Hindi", "Kannada", "Tamil", "Telugu", "Malayalam", "Marathi", "Punjabi", "Bengali")
private val languageNative = listOf("English", "हिन्दी", "ಕನ್ನಡ", "தமிழ்", "తెలుగు", "മലയാളം", "मराठी", "ਪੰਜਾਬੀ", "বাংলা")

@Composable
fun LanguageScreen(vm: SortifyViewModel, onboarding: Boolean = false) {
    var selected by remember { mutableStateOf(vm.languages) }
    Column(Modifier.fillMaxSize()) {
        BackHeader(if (onboarding) "Make it yours" else "Music languages", { vm.back() })
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("What do you\nlove listening to?", style = MaterialTheme.typography.headlineLarge)
                Text("Pick your languages. We’ll find the music.", Modifier.padding(top = 12.dp, bottom = 22.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(availableLanguages.chunked(2)) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { language ->
                        val checked = language in selected
                        Surface(onClick = { selected = if (checked) selected - language else selected + language }, Modifier.weight(1f), shape = RoundedCornerShape(16.dp), color = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(languageNative[availableLanguages.indexOf(language)], style = MaterialTheme.typography.titleLarge)
                                    if (checked) Icon(Icons.Rounded.CheckCircle, "Selected", tint = MaterialTheme.colorScheme.primary)
                                }
                                Text(language, Modifier.padding(top = 10.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        Column(Modifier.padding(24.dp)) {
            Text(if (selected.isEmpty()) "Choose at least one language" else "${selected.size} languages selected · Change anytime in Settings", Modifier.padding(bottom = 12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button({ if (onboarding) vm.finishOnboarding(selected) else { vm.updateLanguages(selected); vm.refreshHome(); vm.back() } }, Modifier.fillMaxWidth().heightIn(min = 54.dp), enabled = selected.isNotEmpty()) { Text(if (onboarding) "Find my music" else "Save languages") }
        }
    }
}

@Composable
fun HomeScreen(vm: SortifyViewModel) {
    var filter by remember { mutableStateOf("All") }
    val mixes = remember(vm.seed) { dailyMixes(vm.seed) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandMark(Modifier.padding(end = 12.dp))
                Column(Modifier.weight(1f)) {
                    Text(when (LocalTime.now().hour) { in 5..11 -> "Good morning"; in 12..16 -> "Good afternoon"; else -> "Good evening" }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Sortify", style = MaterialTheme.typography.titleMedium)
                }
                OutlinedIconButton({ vm.open(Screen.Settings) }, Modifier.size(48.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) { Icon(Icons.Rounded.Settings, "Settings") }
            }
            Text(buildAnnotatedString { append("Welcome to "); withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("Sortify") }; append("\nWorld of Music") }, Modifier.padding(top = 25.dp, bottom = 18.dp), style = MaterialTheme.typography.headlineLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(listOf("All", "Music", "Playlists")) { label ->
                    CategoryChip(filter == label, { filter = label }, label = { Text(label, Modifier.padding(horizontal = 8.dp)) })
                }
            }
            if (vm.homeError != null) Box(Modifier.padding(top = 12.dp)) { ErrorNotice(vm.homeError!!, vm::refreshHome) }
            if (vm.refreshing) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 12.dp), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
        }
        if (filter != "Music") item {
            SectionTitle("Made for you", "See all", { vm.openPlaylist(Playlist("discover", "Made for you", vm.seed, "A selection to start exploring", false)) })
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(mixes) { mix ->
                    Column(Modifier.width(148.dp).clickable { vm.openPlaylist(mix) }) {
                        Box(Modifier.size(148.dp)) {
                            CoverCollage(mix.songs, Modifier.fillMaxSize())
                            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .65f)))))
                            Text(mix.name, Modifier.align(Alignment.BottomStart).padding(12.dp), style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                        Text(mix.description, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (filter == "Music") item {
            SectionTitle("Picked for you")
            SongCarousel(vm.seed, vm)
        }
        if (filter != "Playlists") item {
            SectionTitle("Artists to explore", "See all", { vm.switchTab(Screen.Search); vm.search("popular artists", "artists", true) })
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(vm.seed.distinctBy { it.artist }.take(8)) { song ->
                    Column(Modifier.width(90.dp).clickable { vm.switchTab(Screen.Search); vm.search(song.artist.substringBefore(","), "artists", true) }, horizontalAlignment = Alignment.CenterHorizontally) {
                        Artwork(song.art(), Modifier.size(90.dp), circle = true)
                        Text(song.artist.substringBefore(","), Modifier.padding(top = 9.dp), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        if (filter != "Playlists" && vm.recent.isNotEmpty()) item {
            SectionTitle("Recently played", "See all", { vm.openPlaylist(Playlist("recent", "Recently played", vm.recent, "Your last listening session", false)) })
            SongCarousel(vm.recent, vm)
        }
        if (filter != "Playlists") {
            if (vm.trending.isEmpty()) item {
                SectionTitle("Start exploring")
                SongCarousel(vm.seed, vm)
            }
            vm.trending.forEach { (language, songs) -> item {
                SectionTitle("Trending in $language", "See all", { vm.openPlaylist(Playlist("trending-$language", "Trending in $language", songs, "Discover what’s playing in $language", false)) })
                SongCarousel(songs, vm)
            } }
        }
        if (filter != "Music" && vm.playlists.isNotEmpty()) item {
            SectionTitle("Your playlists", "See all", { vm.switchTab(Screen.Library) })
            vm.playlists.take(3).forEach { playlist -> PlaylistRow(playlist) { vm.openPlaylist(playlist) } }
        }
        if (filter == "Playlists" && vm.playlists.isEmpty()) item {
            EmptyState(Icons.Rounded.LibraryMusic, "Make your own mix", "Bring together the songs you love in a playlist.", "Create a playlist") { vm.switchTab(Screen.Create) }
        }
    }
}

@Composable
fun SongCarousel(songs: List<Song>, vm: SortifyViewModel) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(songs) { song ->
            Column(Modifier.width(148.dp).clickable { vm.play(song, songs, true) }) {
                Artwork(song.art(), Modifier.size(148.dp))
                Text(song.title, Modifier.padding(top = 9.dp), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, Modifier.padding(top = 3.dp), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SearchScreen(vm: SortifyViewModel) {
    val focus = LocalFocusManager.current
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Find your next\nfavourite.", Modifier.weight(1f), style = MaterialTheme.typography.headlineLarge)
            ActionIcon(Icons.Rounded.Settings, "Settings") { vm.open(Screen.Settings) }
        }
        OutlinedTextField(vm.query, { vm.search(it) }, Modifier.fillMaxWidth(), placeholder = { Text("Songs, artists, albums…") }, leadingIcon = { Icon(Icons.Rounded.Search, null) }, trailingIcon = { if (vm.query.isNotEmpty()) ActionIcon(Icons.Rounded.Close, "Clear search") { vm.search("") } }, singleLine = true, shape = RoundedCornerShape(14.dp), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { focus.clearFocus(); vm.search(immediate = true) }))
        if (vm.query.isNotBlank()) {
            LazyRow(Modifier.padding(top = 12.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("songs", "artists", "albums", "playlists")) { kind -> CategoryChip(vm.searchKind == kind, { vm.search(kind = kind, immediate = true); focus.clearFocus() }, label = { Text(kind.replaceFirstChar(Char::uppercase)) }) }
            }
            if (vm.searching) Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            else if (vm.searchError != null) ErrorNotice(vm.searchError!!) { vm.search(immediate = true) }
            else if (vm.searchResults.isEmpty() && vm.catalogResults.isEmpty()) EmptyState(Icons.Rounded.SearchOff, "No matches yet", "Try another title, artist, or spelling.")
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                if (vm.searchKind == "songs") items(vm.searchResults, key = { it.id }) { song -> SongRow(song, vm, vm.searchResults) }
                else items(vm.catalogResults, key = { it.id }) { item ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { focus.clearFocus(); vm.openCatalog(item) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Artwork(item.image, Modifier.size(64.dp), circle = item.kind == "artists")
                        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Icon(Icons.Rounded.ChevronRight, null)
                    }
                }
            }
        } else LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item { SectionTitle("Browse your mood") }
            items(listOf("Made for you" to "Your daily mix", "After hours" to "Night music", "Feel good" to "Happy hits", "Desi favourites" to "Hindi hits", "Focus" to "Instrumental", "Indie finds" to "Indie" ).chunked(2)) { pair ->
                Row(Modifier.padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEachIndexed { index, (title, search) ->
                        val song = vm.seed.getOrNull((title.length + index) % vm.seed.size.coerceAtLeast(1))
                        Box(Modifier.weight(1f).height(120.dp).clip(RoundedCornerShape(16.dp)).clickable { vm.search(search, "songs", true) }) {
                            Artwork(song?.art().orEmpty(), Modifier.fillMaxSize())
                            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = .8f), Color.Black.copy(alpha = .25f)))))
                            Text(title, Modifier.align(Alignment.BottomStart).padding(14.dp), style = MaterialTheme.typography.titleLarge, color = Color.White)
                        }
                    }
                }
            }
            if (vm.recent.isNotEmpty()) {
                item { SectionTitle("From your last session") }
                items(vm.recent.take(4)) { SongRow(it, vm, vm.recent, Modifier.padding(bottom = 10.dp)) }
            }
        }
    }
}
