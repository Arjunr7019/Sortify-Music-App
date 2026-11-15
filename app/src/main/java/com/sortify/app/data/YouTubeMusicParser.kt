package com.sortify.app.data

import org.json.JSONArray
import org.json.JSONObject

/** Parses public music renderers, ignoring menu/overlay copies of the same entries. */
object YouTubeMusicParser {
    fun renderers(root: Any?): List<JSONObject> = buildList {
        fun visit(value: Any?) {
            when (value) {
                is JSONObject -> {
                    val item = value.optJSONObject("musicResponsiveListItemRenderer")
                        ?: value.optJSONObject("musicTwoRowItemRenderer")
                    if (item != null) add(item)
                    else value.keys().forEach { visit(value.opt(it)) }
                }
                is JSONArray -> for (i in 0 until value.length()) visit(value.opt(i))
            }
        }
        visit(root)
    }

    fun songs(root: JSONObject): List<Song> = renderers(root).mapNotNull { item ->
        val title = title(item)
        val videoId = item.optJSONObject("playlistItemData")?.optString("videoId").orEmpty()
            .ifBlank { endpoint(item).optJSONObject("watchEndpoint")?.optString("videoId").orEmpty() }
            .ifBlank { findObject(item.optJSONObject("overlay"), "watchEndpoint")?.optString("videoId").orEmpty() }
        if (!videoId.matches(Regex("[A-Za-z0-9_-]{11}")) || title.isBlank()) return@mapNotNull null
        val runs = subtitleRuns(item)
        val artists = runs.filter { browseId(it).startsWith("UC") }.map { it.optString("text") }
        val album = runs.firstOrNull { browseId(it).startsWith("MPRE") }?.optString("text").orEmpty()
        val durationText = columns(item, "fixedColumns").firstOrNull().orEmpty()
            .ifBlank { runs.map { it.optString("text") }.firstOrNull { it.matches(Regex("\\d{1,2}:\\d{2}(:\\d{2})?")) }.orEmpty() }
        Song("yt:$videoId", title,
            artists.joinToString(", ").ifBlank { runs.firstOrNull { it.optString("text").isNotBlank() && it.optString("text") !in listOf(" • ", " · ", "Song", "Video") }?.optString("text") ?: "YouTube Music" },
            album = album, image = thumbnail(item), duration = durationSeconds(durationText), provider = MusicProvider.YouTubeMusic)
    }.distinctBy { it.id }

    fun catalog(root: JSONObject, kind: String? = null): List<CatalogItem> = renderers(root).mapNotNull { item ->
        val browse = endpoint(item).optJSONObject("browseEndpoint") ?: return@mapNotNull null
        val id = browse.optString("browseId")
        val page = browse.optJSONObject("browseEndpointContextSupportedConfigs")
            ?.optJSONObject("browseEndpointContextMusicConfig")?.optString("pageType").orEmpty()
        val actualKind = when {
            id.startsWith("VL") || page.contains("PLAYLIST") -> "playlists"
            id.startsWith("MPRE") || page.contains("ALBUM") -> "albums"
            id.startsWith("UC") || page.contains("ARTIST") -> "artists"
            else -> return@mapNotNull null
        }
        if (kind != null && kind != actualKind) return@mapNotNull null
        val title = title(item).takeIf { it.isNotBlank() } ?: return@mapNotNull null
        CatalogItem(id, title, text(item.optJSONObject("subtitle")).ifBlank { columns(item, "flexColumns").drop(1).joinToString(" · ") }
            .ifBlank { "YouTube Music" }, thumbnail(item), actualKind, MusicProvider.YouTubeMusic)
    }.distinctBy { it.id }

    fun findObject(root: Any?, key: String): JSONObject? {
        when (root) {
            is JSONObject -> {
                root.optJSONObject(key)?.let { return it }
                root.keys().forEach { findObject(root.opt(it), key)?.let { found -> return found } }
            }
            is JSONArray -> for (i in 0 until root.length()) findObject(root.opt(i), key)?.let { return it }
        }
        return null
    }

    private fun endpoint(item: JSONObject): JSONObject {
        item.optJSONObject("navigationEndpoint")?.let { return it }
        val flex = item.optJSONArray("flexColumns")?.optJSONObject(0)
            ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")?.optJSONObject("text")?.optJSONArray("runs")
        return flex?.optJSONObject(0)?.optJSONObject("navigationEndpoint") ?: JSONObject()
    }
    private fun title(item: JSONObject) = text(item.optJSONObject("title")).ifBlank { columns(item, "flexColumns").firstOrNull().orEmpty() }
    private fun columns(item: JSONObject, name: String): List<String> {
        val columns = item.optJSONArray(name) ?: return emptyList()
        val renderer = if (name == "fixedColumns") "musicResponsiveListItemFixedColumnRenderer" else "musicResponsiveListItemFlexColumnRenderer"
        return (0 until columns.length()).map { text(columns.optJSONObject(it)?.optJSONObject(renderer)?.optJSONObject("text")) }
    }
    private fun subtitleRuns(item: JSONObject): List<JSONObject> {
        val runs = item.optJSONObject("subtitle")?.optJSONArray("runs")
            ?: item.optJSONArray("flexColumns")?.optJSONObject(1)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")?.optJSONObject("text")?.optJSONArray("runs")
            ?: return emptyList()
        return (0 until runs.length()).mapNotNull { runs.optJSONObject(it) }
    }
    private fun browseId(run: JSONObject) = run.optJSONObject("navigationEndpoint")?.optJSONObject("browseEndpoint")?.optString("browseId").orEmpty()
    private fun text(value: JSONObject?): String {
        value ?: return ""
        val runs = value.optJSONArray("runs") ?: return value.optString("simpleText")
        return (0 until runs.length()).joinToString("") { runs.optJSONObject(it)?.optString("text").orEmpty() }
    }
    private fun thumbnail(item: JSONObject): String {
        val array = findObject(item.opt("thumbnailRenderer") ?: item.opt("thumbnail"), "thumbnail")?.optJSONArray("thumbnails")
            ?: item.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
        return array?.optJSONObject((array.length() - 1).coerceAtLeast(0))?.optString("url").orEmpty().let { if (it.startsWith("//")) "https:$it" else it }
    }
    fun durationSeconds(value: String): Long = value.split(":").takeIf { it.size in 2..3 && it.all { p -> p.toLongOrNull() != null } }
        ?.fold(0L) { acc, part -> acc * 60 + part.toLong() } ?: 0
}
