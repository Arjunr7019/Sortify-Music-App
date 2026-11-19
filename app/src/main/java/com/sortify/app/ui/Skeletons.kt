package com.sortify.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

private val LocalSkeletonAlpha = staticCompositionLocalOf<State<Float>?> { null }

@Composable
private fun rememberSkeletonAlpha(): State<Float> {
    // Compose honors Android's animator duration scale, including Remove animations.
    val transition = rememberInfiniteTransition(label = "Skeleton loading")
    return transition.animateFloat(
        initialValue = .6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Reverse),
        label = "Skeleton pulse",
    )
}

/** One accessible loading announcement and one animation clock per content group. */
@Composable
fun SkeletonLoading(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val alpha = rememberSkeletonAlpha()
    Box(modifier.clearAndSetSemantics {
        contentDescription = label
        stateDescription = "Loading"
        liveRegion = LiveRegionMode.Polite
    }) {
        CompositionLocalProvider(LocalSkeletonAlpha provides alpha, content = content)
    }
}

@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    val alpha = LocalSkeletonAlpha.current ?: rememberSkeletonAlpha()
    // Read animation state while drawing so pulses do not recompose or remeasure rows.
    Spacer(modifier.clip(shape).drawBehind { drawRect(color, alpha = alpha.value) })
}

@Composable
fun SkeletonTextLine(
    modifier: Modifier = Modifier,
    fraction: Float = 1f,
    style: TextStyle = MaterialTheme.typography.titleSmall,
) {
    val lineHeight = with(LocalDensity.current) { style.lineHeight.toDp() }
    Box(modifier.height(lineHeight), contentAlignment = Alignment.CenterStart) {
        SkeletonBlock(Modifier.fillMaxWidth(fraction).height(lineHeight * .5f), shape = CircleShape)
    }
}

@Composable
fun SkeletonCarousel(label: String, modifier: Modifier = Modifier, playlist: Boolean = false) {
    SkeletonLoading(label, modifier.fillMaxWidth()) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), userScrollEnabled = false) {
            items(6) { index ->
                Column(Modifier.width(148.dp)) {
                    SkeletonBlock(Modifier.size(148.dp))
                    SkeletonTextLine(Modifier.fillMaxWidth().padding(top = 9.dp), fraction = if (index % 2 == 0) .85f else .7f)
                    if (playlist) SkeletonTextLine(Modifier.fillMaxWidth(), fraction = .55f)
                    SkeletonTextLine(Modifier.fillMaxWidth().padding(top = 3.dp), fraction = .6f, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SkeletonResultRow(kind: String, index: Int) {
    val song = kind == "songs"
    Row(
        Modifier.fillMaxWidth().padding(start = if (song) 10.dp else 0.dp, top = if (song) 9.dp else 8.dp, bottom = if (song) 9.dp else 8.dp, end = if (song) 2.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonBlock(Modifier.size(if (song) 54.dp else 64.dp), shape = if (kind == "artists") CircleShape else RoundedCornerShape(14.dp))
        Column(Modifier.weight(1f).padding(horizontal = if (song) 12.dp else 14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            SkeletonTextLine(Modifier.fillMaxWidth(), fraction = if (index % 2 == 0) .85f else .65f, style = MaterialTheme.typography.titleMedium)
            SkeletonTextLine(Modifier.fillMaxWidth(), fraction = .5f, style = if (song) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.width(if (song) 48.dp else 24.dp))
    }
}

@Composable
fun SkeletonSearchResults(kind: String, modifier: Modifier = Modifier) {
    SkeletonLoading("Search results", modifier.fillMaxWidth()) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp), userScrollEnabled = false) {
            items(8) { SkeletonResultRow(kind, it) }
        }
    }
}

@Composable
fun SkeletonSongList(modifier: Modifier = Modifier) {
    SkeletonLoading("Playlist songs", modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(6) { SkeletonResultRow("songs", it) }
        }
    }
}
