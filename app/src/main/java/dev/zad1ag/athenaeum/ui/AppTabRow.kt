package dev.zad1ag.athenaeum.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * The app's signature tab treatment: transparent by default, active tabs get
 * a soft gold wash with bold text. Single row of pill buttons.
 */
@Composable
fun AppTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
    compact: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalArrangement = if (centered) Arrangement.Center else Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            TextButton(
                onClick = { onSelected(index) },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = if (compact) 12.dp else 16.dp,
                    vertical = if (compact) 0.dp else 8.dp
                ),
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else Color.Transparent,
                    contentColor = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    label,
                    style = if (compact) MaterialTheme.typography.labelMedium
                            else MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Free-floating pill tabs — transparent background, pills carry a thin
 * outline. Selected: gold outline + soft gold fill + bold gold text.
 */
@Composable
fun FloatingPillTabs(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val outline = Modifier.border(
                width = 1.dp,
                color = if (selected) Color.Transparent
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = CircleShape
            )
            TextButton(
                onClick = { onSelected(index) },
                shape = CircleShape,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 14.dp, vertical = 4.dp
                ),
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else Color.Transparent,
                    contentColor = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.then(outline)
            ) {
                Text(
                    label,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}