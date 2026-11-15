package com.sortify.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.view.WindowCompat
import com.sortify.app.ui.*

class MainActivity : ComponentActivity() {
    private val notifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: SortifyViewModel = viewModel()
            SortifyTheme(vm.theme) {
                val lightBars = MaterialTheme.colorScheme.background.luminance() > 0.5f
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = lightBars
                        isAppearanceLightNavigationBars = lightBars
                    }
                }
                var notificationAsked by remember { mutableStateOf(false) }
                LaunchedEffect(vm.current?.id) {
                    if (vm.current != null && !notificationAsked && Build.VERSION.SDK_INT >= 33) {
                        notificationAsked = true
                        notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                SortifyApp(vm)
            }
        }
    }
}

private val destinations = listOf(
    Triple(Screen.Home, "Home", Icons.Rounded.Home),
    Triple(Screen.Search, "Search", Icons.Rounded.Search),
    Triple(Screen.Library, "Your library", Icons.Rounded.LibraryMusic),
    Triple(Screen.Create, "Create", Icons.Rounded.Add),
)

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun SortifyApp(vm: SortifyViewModel) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(vm) { vm.messages.collect { snackbar.showSnackbar(it, duration = SnackbarDuration.Short) } }
    BackHandler(vm.hasBack()) { vm.back() }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        ScreenBackground(Modifier.fillMaxSize()) {
            BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                val expanded = maxWidth >= 700.dp
                val playerFull = vm.screen == Screen.Player || vm.screen == Screen.Queue
                val browseScreen = if (playerFull) vm.playerOrigin else vm.screen
                val searchKeyboardOpen = vm.screen == Screen.Search && WindowInsets.isImeVisible
                val heightPx = with(LocalDensity.current) { maxHeight.toPx() }
                val motion = remember { PlayerMotionState(playerFull) }
                val miniGesture = Modifier.playerDrag(motion, false, heightPx) { open -> if (open) vm.open(Screen.Player) }
                Row(Modifier.fillMaxSize().then(if (playerFull) Modifier.clearAndSetSemantics { } else Modifier)) {
                    if (expanded && vm.stage == 2) NavigationRail(containerColor = Color.Transparent, modifier = Modifier.fillMaxHeight(), header = { BrandMark(Modifier.padding(vertical = 18.dp)) }) {
                        destinations.forEach { (screen, label, icon) -> NavigationRailItem(vm.tab == screen, { vm.switchTab(screen) }, { Icon(icon, label) }, label = { Text(label) }, modifier = Modifier.padding(vertical = 7.dp)) }
                    }
                    Scaffold(
                        modifier = Modifier.weight(1f),
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets(0),
                        bottomBar = {
                            if (vm.stage == 2 && !searchKeyboardOpen) Column {
                                MiniPlayer(vm, miniGesture)
                                if (!expanded) NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp, windowInsets = WindowInsets(0), modifier = Modifier.heightIn(min = 72.dp)) {
                                    destinations.forEach { (screen, label, icon) -> NavigationBarItem(vm.tab == screen, { vm.switchTab(screen) }, { Icon(icon, label) }, label = { Text(label, style = MaterialTheme.typography.labelMedium) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.onBackground, selectedTextColor = MaterialTheme.colorScheme.onBackground, indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .4f))) }
                                }
                            }
                        },
                    ) { padding ->
                        Box(Modifier.fillMaxSize().padding(padding).imePadding(), contentAlignment = Alignment.TopCenter) {
                            Box(Modifier.fillMaxHeight().widthIn(max = if (expanded) 900.dp else 700.dp)) {
                                when (vm.stage) {
                                    0 -> WelcomeScreen(vm)
                                    1 -> LanguageScreen(vm, onboarding = true)
                                    else -> when (browseScreen) {
                                        Screen.Home -> HomeScreen(vm)
                                        Screen.Search -> SearchScreen(vm)
                                        Screen.Library -> LibraryScreen(vm)
                                        Screen.Create -> CreateScreen(vm)
                                        Screen.Playlist -> PlaylistScreen(vm)
                                        Screen.Favorites -> CollectionScreen(vm, downloads = false)
                                        Screen.Downloads -> CollectionScreen(vm, downloads = true)
                                        Screen.Settings -> SettingsScreen(vm)
                                        Screen.Languages -> LanguageScreen(vm)
                                        Screen.Player, Screen.Queue -> Unit
                                    }
                                }
                            }
                        }
                    }
                }
                PlayerOverlay(vm, motion, heightPx)
                SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(
                    start = 12.dp, end = 12.dp,
                    bottom = if (playerFull || searchKeyboardOpen) 16.dp else if (expanded) 84.dp else if (vm.current != null) 164.dp else 88.dp,
                ))
            }
        }
        PlayerSheets(vm)
    }
}
