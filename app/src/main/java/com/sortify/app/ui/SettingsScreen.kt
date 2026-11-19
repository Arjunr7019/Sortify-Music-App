package com.sortify.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sortify.app.BuildConfig
import com.sortify.app.Screen
import com.sortify.app.SortifyViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(vm: SortifyViewModel) {
    var dialog by rememberSaveable { mutableStateOf("") }
    var importUri by rememberSaveable { mutableStateOf<String?>(null) }
    val exportFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        uri?.let(vm::exportData)
    }
    val importFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) { importUri = uri.toString(); dialog = "import" }
    }
    val transferAvailable = vm.dataTransfer == null && vm.downloading.isEmpty()
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
                SectionTitle("Backup & restore")
                Text("Back up playlists, liked songs, recent history, preferences, and downloaded audio.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                SettingsRow(Icons.Rounded.FileUpload, "Export all data", "Save a complete ZIP backup", enabled = transferAvailable) {
                    val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss"))
                    exportFile.launch("Sortify-backup-$date.zip")
                }
                SettingsRow(Icons.Rounded.FileDownload, "Import all data", "Restore a Sortify backup and replace current data", enabled = transferAvailable) {
                    importFile.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream"))
                }
                if (vm.downloading.isNotEmpty()) Text("Backup and restore will be available when downloads finish.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                SectionTitle("About")
                SettingsRow(Icons.Rounded.Info, "About Sortify", "Version ${BuildConfig.VERSION_NAME}") { dialog = "about" }
                Text("Your data stays on this device unless you export a backup. Keep a backup somewhere safe before uninstalling Sortify.", Modifier.padding(top = 24.dp, bottom = 24.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    when (dialog) {
        "import" -> AlertDialog(
            onDismissRequest = { dialog = ""; importUri = null },
            title = { Text("Replace all data?") },
            text = { Text("This replaces your playlists, liked songs, recent history, preferences, and downloads with the selected backup. Playback and the sleep timer will stop. Export your current data first if you want to keep it.") },
            confirmButton = { TextButton(enabled = transferAvailable, onClick = {
                importUri?.let { vm.importData(Uri.parse(it)) }
                dialog = ""; importUri = null
            }) { Text("Replace and import") } },
            dismissButton = { TextButton({ dialog = ""; importUri = null }) { Text("Cancel") } },
        )
        "quality" -> ChoiceDialog("Audio quality", listOf("96kbps", "160kbps", "320kbps"), vm.quality, { vm.updateQuality(it); dialog = "" }, { dialog = "" }, "Higher quality uses more data and storage. Sortify uses the closest available bitrate when needed.")
        "theme" -> ChoiceDialog("Appearance", listOf("Dark", "Light", "System"), vm.theme, { vm.updateTheme(it); dialog = "" }, { dialog = "" })
        "sleep" -> ChoiceDialog("Sleep timer", listOf("Off", "15 minutes", "30 minutes", "60 minutes"), if (vm.sleepMinutes == 0) "Off" else "${vm.sleepMinutes} minutes", { vm.setSleepTimer(it.substringBefore(" ").toIntOrNull() ?: 0); dialog = "" }, { dialog = "" })
        "clear" -> AlertDialog(onDismissRequest = { dialog = "" }, title = { Text("Remove all downloads?") }, text = { Text("You’ll need an internet connection to play these songs again. You can download them later.") }, confirmButton = { TextButton({ vm.clearDownloads(); dialog = "" }) { Text("Remove downloads") } }, dismissButton = { TextButton({ dialog = "" }) { Text("Cancel") } })
        "about" -> AlertDialog(onDismissRequest = { dialog = "" }, icon = { BrandMark() }, title = { Text("Sortify ${BuildConfig.VERSION_NAME}") }, text = { Text("A native Kotlin music player.\n\nMusic search and streaming use a compatible third-party Saavn service. Catalog and playback availability depend on that service and your region.\n\nNo account required. Your library is saved locally.") }, confirmButton = { TextButton({ dialog = "" }) { Text("Done") } })
    }
    vm.dataTransfer?.let { status ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text(status) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SkeletonLoading(status, Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SkeletonTextLine(Modifier.fillMaxWidth())
                            SkeletonTextLine(Modifier.fillMaxWidth(), fraction = .65f)
                        }
                    }
                    Text("Downloaded audio can make this take a while. Keep Sortify open until it finishes.")
                }
            },
            confirmButton = {},
        )
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, enabled: Boolean = true, onClick: () -> Unit) {
    val contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.38f)
    val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.38f)
    Row(Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick).padding(vertical = 17.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.padding(end = 17.dp), tint = secondaryColor)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = contentColor)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = secondaryColor)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = secondaryColor)
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
