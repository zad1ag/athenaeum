package dev.reggie.tarot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun StavesList(
    staves: List<Stave>,
    onStaveClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(staves, key = { it.id }) { stave ->
            StaveListItem(stave, onStaveClick)
        }
    }
}

private fun staveCategoryColor(category: String): Color = when (category.lowercase()) {
    "protection" -> Color(0xFF1E3A3A)
    "travel" -> Color(0xFF2A2A3D)
    "dreams" -> Color(0xFF2E2A3D)
    "stealth" -> Color(0xFF2A2A2A)
    "luck" -> Color(0xFF2D3A1E)
    "prosperity" -> Color(0xFF3D2E1A)
    "victory" -> Color(0xFF3A1E1E)
    "spirit work" -> Color(0xFF1E1E3A)
    "love" -> Color(0xFF3A1E3A)
    "home" -> Color(0xFF2E2A1E)
    "craft" -> Color(0xFF2A2A2A)
    "fertility" -> Color(0xFF1E3A1E)
    "curses" -> Color(0xFF2A0D0D)
    else -> Color(0xFF2A2A2A)
}

private fun staveCategoryAccent(category: String): Color = when (category.lowercase()) {
    "protection" -> Color(0xFF9AE6B4)
    "travel" -> Color(0xFFB8D4F0)
    "dreams" -> Color(0xFFC9B8F0)
    "stealth" -> Color(0xFFB7B7B7)
    "luck" -> Color(0xFFB4E69A)
    "prosperity" -> Color(0xFFF0C89A)
    "victory" -> Color(0xFFFF8F8F)
    "spirit work" -> Color(0xFFA8A8F0)
    "love" -> Color(0xFFF0A8E0)
    "home" -> Color(0xFFE0D0A0)
    "craft" -> Color(0xFFB0B0B0)
    "fertility" -> Color(0xFF9AE6B4)
    "curses" -> Color(0xFFFF6B6B)
    else -> Color(0xFFC9A959)
}

@Composable
private fun StaveListItem(stave: Stave, onStaveClick: (Int) -> Unit) {
    val container = remember(stave.category) { staveCategoryColor(stave.category) }
    val accent = remember(stave.category) { staveCategoryAccent(stave.category) }
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStaveClick(stave.id) },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(containerColor = container),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StaveGlyph(stave.id, accent)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stave.category,
                    style = MaterialTheme.typography.labelMedium,
                    color = accent.copy(alpha = 0.9f)
                )
                Text(
                    text = stave.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = stave.icelandicName,
                    style = MaterialTheme.typography.titleMedium,
                    color = accent.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stave.meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun StaveGlyph(id: Int, color: Color) {
    val dark = isSystemInDarkTheme()
    val bg = if (dark) Color(0xFF141414) else Color(0xFFFAFAFA)
    androidx.compose.foundation.Canvas(
        modifier = Modifier.size(72.dp)
    ) {
        val strokeWidth = size.width * 0.04f
        val lineColor = color
        drawCircle(lineColor, radius = size.minDimension * 0.48f, style = Stroke(width = strokeWidth))
        when (id) {
            1 -> { // Ægishjálmur: eight tridents
                val armCount = 8
                val rOuter = size.minDimension * 0.35f
                val rInner = size.minDimension * 0.12f
                val center = Offset(size.width / 2, size.height / 2)
                repeat(armCount) { i ->
                    val angle = Math.PI * 2 * i / armCount
                    val dxOuter = kotlin.math.cos(angle).toFloat() * rOuter
                    val dyOuter = kotlin.math.sin(angle).toFloat() * rOuter
                    val dxMid = kotlin.math.cos(angle).toFloat() * rInner
                    val dyMid = kotlin.math.sin(angle).toFloat() * rInner
                    drawLine(
                        lineColor,
                        start = center + Offset(dxMid, dyMid),
                        end = center + Offset(dxOuter, dyOuter),
                        strokeWidth = strokeWidth * 1.2f,
                        cap = StrokeCap.Round
                    )
                    val prong = rOuter * 0.22f
                    val left = Offset(dxOuter, dyOuter).rotate(35f, Offset.Zero) * (prong / rOuter)
                    val right = Offset(dxOuter, dyOuter).rotate(-35f, Offset.Zero) * (prong / rOuter)
                    drawLine(lineColor, start = center + Offset(dxOuter, dyOuter), end = center + Offset(dxOuter, dyOuter) + left, strokeWidth, cap = StrokeCap.Round)
                    drawLine(lineColor, start = center + Offset(dxOuter, dyOuter), end = center + Offset(dxOuter, dyOuter) + right, strokeWidth, cap = StrokeCap.Round)
                }
                drawCircle(bg, radius = size.minDimension * 0.08f)
            }
            2 -> { // Vegvísir
                val center = Offset(size.width / 2, size.height / 2)
                val r = size.minDimension * 0.32f
                drawCircle(lineColor, radius = r, style = Stroke(width = strokeWidth))
                drawLine(lineColor, start = center - Offset(0f, r * 1.25f), end = center + Offset(0f, r * 1.25f), strokeWidth = strokeWidth * 1.5f, cap = StrokeCap.Round)
                drawLine(lineColor, start = center - Offset(r * 1.0f, 0f), end = center + Offset(r * 1.0f, 0f), strokeWidth = strokeWidth * 1.5f, cap = StrokeCap.Round)
                drawLine(lineColor, start = center - Offset(r * 0.7f, r * 0.7f), end = center + Offset(r * 0.7f, r * 0.7f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                drawLine(lineColor, start = center + Offset(r * 0.7f, -r * 0.7f), end = center - Offset(r * 0.7f, r * 0.7f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                drawCircle(lineColor, radius = size.minDimension * 0.06f)
            }
            else -> { // generic radiating stave
                val center = Offset(size.width / 2, size.height / 2)
                val armCount = 6
                val r = size.minDimension * 0.32f
                repeat(armCount) { i ->
                    val angle = Math.PI * 2 * i / armCount
                    val dx = kotlin.math.cos(angle).toFloat() * r
                    val dy = kotlin.math.sin(angle).toFloat() * r
                    drawLine(lineColor, center, center + Offset(dx, dy), strokeWidth = strokeWidth * 1.3f, cap = StrokeCap.Round)
                    val hook = r * 0.2f
                    val perpX = -kotlin.math.sin(angle).toFloat() * hook
                    val perpY = kotlin.math.cos(angle).toFloat() * hook
                    drawLine(lineColor, center + Offset(dx, dy), center + Offset(dx + perpX, dy + perpY), strokeWidth, cap = StrokeCap.Round)
                }
                drawCircle(lineColor, radius = size.minDimension * 0.05f)
            }
        }
    }
}

private operator fun Offset.times(scale: Float): Offset = Offset(x * scale, y * scale)
private operator fun Offset.plus(other: Offset): Offset = Offset(x + other.x, y + other.y)
private fun Offset.rotate(degrees: Float, pivot: Offset): Offset {
    val rad = Math.toRadians(degrees.toDouble())
    val cos = kotlin.math.cos(rad).toFloat()
    val sin = kotlin.math.sin(rad).toFloat()
    val dx = x - pivot.x
    val dy = y - pivot.y
    return Offset(pivot.x + dx * cos - dy * sin, pivot.y + dx * sin + dy * cos)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaveDetailScreen(
    id: Int,
    onBack: () -> Unit
) {
    var stave by remember { mutableStateOf<Stave?>(null) }

    LaunchedEffect(id) {
        stave = TarotApp.core.getStave(id)
    }

    BackHandler { onBack() }

    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stave?.name ?: "Stave", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    stave?.let { current ->
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit stave")
                        }
                        if (showEditDialog) {
                            EditStaveDialog(
                                stave = current,
                                onDismiss = { showEditDialog = false },
                                onSave = {
                                    stave = TarotApp.core.getStave(id)
                                    showEditDialog = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        stave?.let { s ->
            val container = remember(s.category) { staveCategoryColor(s.category) }
            val accent = remember(s.category) { staveCategoryAccent(s.category) }
            val scroll = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scroll),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.outlinedCardColors(containerColor = container)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StaveGlyph(s.id, accent)
                        Text(
                            text = s.category,
                            style = MaterialTheme.typography.labelLarge,
                            color = accent,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = s.name,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = s.icelandicName,
                            style = MaterialTheme.typography.titleLarge,
                            color = accent.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                TextBlock(label = "Meaning", text = s.meaning, accent = accent)
                TextBlock(label = "Purpose", text = s.purpose, accent = accent)
                TextBlock(label = "Visual Notes", text = s.visualNotes, accent = accent)

                val context = LocalContext.current
                val resId = remember(s.imageRef) {
                    if (s.imageRef.isBlank()) 0
                    else context.resources.getIdentifier(s.imageRef, "drawable", context.packageName)
                }
                if (resId != 0) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "IMAGE".uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = accent,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(resId),
                                contentDescription = s.name,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.size(24.dp))
            }
        } ?: Text(
            "Stave not found",
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun TextBlock(label: String, text: String, accent: Color) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditStaveDialog(
    stave: Stave,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var name by remember(stave.id) { mutableStateOf(stave.name) }
    var icelandicName by remember(stave.id) { mutableStateOf(stave.icelandicName) }
    var meaning by remember(stave.id) { mutableStateOf(stave.meaning) }
    var purpose by remember(stave.id) { mutableStateOf(stave.purpose) }
    var category by remember(stave.id) { mutableStateOf(stave.category) }
    var visualNotes by remember(stave.id) { mutableStateOf(stave.visualNotes) }
    var imageRef by remember(stave.id) { mutableStateOf(stave.imageRef) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Stave") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (error.isNotBlank()) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = icelandicName,
                    onValueChange = { icelandicName = it },
                    label = { Text("Icelandic name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = meaning,
                    onValueChange = { meaning = it },
                    label = { Text("Meaning") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Purpose") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = visualNotes,
                    onValueChange = { visualNotes = it },
                    label = { Text("Visual notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = imageRef,
                    onValueChange = { imageRef = it },
                    label = { Text("Image ref") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank() || category.isBlank()) {
                        error = "Name and category are required."
                        return@TextButton
                    }
                    TarotApp.core.updateStave(
                        stave.copy(
                            name = name.trim(),
                            icelandicName = icelandicName.trim(),
                            meaning = meaning.trim(),
                            purpose = purpose.trim(),
                            category = category.trim(),
                            visualNotes = visualNotes.trim(),
                            imageRef = imageRef.trim()
                        )
                    )
                    onSave()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
