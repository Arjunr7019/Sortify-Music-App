package com.sortify.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sortify.app.Screen
import com.sortify.app.SortifyViewModel

@Composable
fun SettingsScreen(vm: SortifyViewModel) {
    var dialog by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        BackHeader("Settings", { vm.back() })
        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)) {
            item {
                BrandMark(Modifier.padding(vertical = 16.dp), wordmark = true)
                Text("Make yourself at home.", style = MaterialTheme.typography.headlineLarge)
                Text("Your music, your way.", Modifier.padding(top = 10.dp, bottom = 20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                SectionTitle("Listening")
                SettingsRow(Icons.AutoMirrored.Rounded.VolumeUp, "Audio quality", "${vm.quality} · New streams and downloads") { dialog = "quality" }
                SettingsRow(Icons.Rounded.Language, "Music languages", vm.languages.joinToString(", ")) { vm.open(Screen.Languages) }
                SettingsRow(Icons.Rounded.Bedtime, "Sleep timer", if (vm.sleepMinutes == 0) "Off" else "Pauses after ${vm.sleepMinutes} minutes") { dialog = "sleep" }
                SectionTitle("Personalise")
                SettingsRow(Icons.Rounded.Palette, "Appearance", vm.theme) { dialog = "theme" }
                SectionTitle("Storage")
                SettingsRow(Icons.Rounded.DownloadDone, "Offline music", "${vm.downloads.size} downloaded songs") { vm.open(Screen.Downloads) }
                SettingsRow(Icons.Rounded.DeleteOutline, "Remove all downloads", "Free up the audio storage on this device") { if (vm.downloads.isEmpty()) vm.message("There are no downloads to remove") else dialog = "clear" }
                SectionTitle("About")
                SettingsRow(Icons.Rounded.Info, "About Sortify", "Version 1.0.0") { dialog = "about" }
                Text("Your playlists, likes, downloads, and preferences stay on this device. Uninstalling Sortify removes them.", Modifier.padding(top = 24.dp, bottom = 24.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    when (dialog) {
        "quality" -> ChoiceDialog("Audio quality", listOf("96kbps", "160kbps", "320kbps"), vm.quality, { vm.updateQuality(it); dialog = "" }, { dialog = "" }, "Higher quality uses more data and storage. Sortify uses the closest available bitrate when needed.")
        "theme" -> ChoiceDialog("Appearance", listOf("Dark", "Light", "System"), vm.theme, { vm.updateTheme(it); dialog = "" }, { dialog = "" })
        "sleep" -> ChoiceDialog("Sleep timer", listOf("Off", "15 minutes", "30 minutes", "60 minutes"), if (vm.sleepMinutes == 0) "Off" else "${vm.sleepMinutes} minutes", { vm.setSleepTimer(it.substringBefore(" ").toIntOrNull() ?: 0); dialog = "" }, { dialog = "" })
        "clear" -> AlertDialog(onDismissRequest = { dialog = "" }, title = { Text("Remove all downloads?") }, text = { Text("You’ll need an internet connection to play these songs again. You can download them later.") }, confirmButton = { TextButton({ vm.clearDownloads(); dialog = "" }) { Text("Remove downloads") } }, dismissButton = { TextButton({ dialog = "" }) { Text("Cancel") } })
        "about" -> AlertDialog(onDismissRequest = { dialog = "" }, icon = { BrandMark() }, title = { Text("Sortify 1.0.0") }, text = { Text("A native Kotlin music player.\n\nMusic search and streaming use a compatible third-party Saavn service. Catalog and playback availability depend on that service and your region.\n\nNo account required. Your library is saved locally.") }, confirmButton = { TextButton({ dialog = "" }) { Text("Done") } })
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 17.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.padding(end = 17.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ChoiceDialog(title: String, choices: List<String>, selected: String, onSelect: (String) -> Unit, onDismiss: () -> Unit, description: String? = null) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
        Column {
            if (description != null) Text(description, Modifier.padding(bottom = 12.dp), style = MaterialTheme.typography.bodyMedium)
            choices.forEach { choice -> Row(Modifier.fillMaxWidth().clickable { onSelect(choice) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(choice == selected, { onSelect(choice) }); Text(choice, Modifier.padding(start = 8.dp)) } }
        }
    }, confirmButton = { TextButton(onDismiss) { Text("Close") } })
}
