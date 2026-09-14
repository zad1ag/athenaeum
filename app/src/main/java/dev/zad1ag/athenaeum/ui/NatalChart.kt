package dev.zad1ag.athenaeum.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import dev.zad1ag.athenaeum.BirthProfile
import dev.zad1ag.athenaeum.NatalAspect
import dev.zad1ag.athenaeum.NatalChart
import dev.zad1ag.athenaeum.Placement
import dev.zad1ag.athenaeum.TarotApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.cos
import kotlin.math.sin

private val bodySymbol = mapOf(
    "sun" to "\u2609", "moon" to "\u263D", "mercury" to "\u263F",
    "venus" to "\u2640", "mars" to "\u2642", "jupiter" to "\u2643", "saturn" to "\u2644"
)

private fun signIndex(name: String): Int = when (name.lowercase()) {
    "aries" -> 0; "taurus" -> 1; "gemini" -> 2; "cancer" -> 3
    "leo" -> 4; "virgo" -> 5; "libra" -> 6; "scorpio" -> 7
    "sagittarius" -> 8; "capricorn" -> 9; "aquarius" -> 10; else -> 11
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NatalChartScreen(onBack: () -> Unit) {
    var profiles by remember { mutableStateOf(emptyList<BirthProfile>()) }
    var selected by remember { mutableStateOf<BirthProfile?>(null) }
    var chart by remember { mutableStateOf<NatalChart?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<BirthProfile?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var profileToDelete by remember { mutableStateOf<BirthProfile?>(null) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        profiles = TarotApp.core.getAllBirthProfiles()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Natal Charts", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (profiles.isEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { editingProfile = null; showEditor = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Birth Profile") }
                )
            }
        }
    ) { padding ->
        when {
            selected != null && chart != null -> ChartDetail(
                profile = selected!!,
                chart = chart!!,
                contentPadding = padding,
                onBack = { selected = null; chart = null },
                onEdit = { editingProfile = selected; showEditor = true },
                onDelete = { showDeleteConfirm = true }
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = padding.calculateTopPadding() + 12.dp,
                    bottom = padding.calculateBottomPadding() + 12.dp,
                    start = 12.dp,
                    end = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (profiles.isEmpty()) {
                    item {
                        Text(
                            "No birth profiles yet.\nAdd one to compute a natal chart.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 48.dp)
                        )
                    }
                }
                items(profiles, key = { it.id }) { profile ->
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = profile
                                chart = TarotApp.core.natalChart(profile)
                            },
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    profile.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                val local = LocalDateTime.of(2000, 1, 1, 0, 0)
                                    .plusDays(((profile.jd - 2440587.5) / 1.0).toInt().toLong())
                                Text(
                                    "JD ${"%.2f".format(profile.jd)} · ${profile.timezoneOffsetHours}h offset",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { profileToDelete = profile }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete profile",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (profileToDelete != null) {
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            title = { Text("Delete profile?") },
            text = { Text("${profileToDelete!!.name} will be removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        TarotApp.core.deleteBirthProfile(profileToDelete!!.id)
                        profiles = TarotApp.core.getAllBirthProfiles()
                        if (selected?.id == profileToDelete!!.id) {
                            selected = null
                            chart = null
                        }
                        profileToDelete = null
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (showEditor) {
        BirthProfileEditorDialog(
            profile = editingProfile,
            onDismiss = { showEditor = false },
            onSave = { saved ->
                val newId = TarotApp.core.saveBirthProfile(saved.id, saved)
                showEditor = false
                profiles = TarotApp.core.getAllBirthProfiles()
                if (newId > 0) {
                    val savedProfile = saved.copy(id = newId.toInt())
                    selected = savedProfile
                    chart = TarotApp.core.natalChart(savedProfile)
                }
            }
        )
    }

    if (showDeleteConfirm && selected != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete profile?") },
            text = { Text("${selected!!.name} will be removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        TarotApp.core.deleteBirthProfile(selected!!.id)
                        showDeleteConfirm = false
                        selected = null
                        chart = null
                        profiles = TarotApp.core.getAllBirthProfiles()
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ChartDetail(profile: BirthProfile, chart: NatalChart, onBack: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit, contentPadding: androidx.compose.foundation.layout.PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            profile.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))

        ChartWheel(chart = chart, modifier = Modifier.size(300.dp))

        Spacer(Modifier.height(16.dp))

        if (chart.ascendantSign != null) {
            Text(
                text = "Ascendant: ${chart.ascendantSign.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.height(16.dp))

        chart.placements.forEach { p ->
            OutlinedCard(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = bodySymbol[p.body.lowercase()] ?: "•",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(
                            text = p.body.replaceFirstChar { it.uppercase() } +
                                    if (p.retrograde) " ℞" else "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${p.sign.replaceFirstChar { it.uppercase() }} ${"%.1f".format(p.degreeInSign)}°" +
                                    if (p.house > 0) " · house ${p.house}" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (chart.aspects.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "ASPECTS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            chart.aspects.forEach { aspect ->
                Text(
                    text = "${aspect.a.replaceFirstChar { it.uppercase() }} ${aspect.kind.replaceFirstChar { it.uppercase() }} ${aspect.b.replaceFirstChar { it.uppercase() }} (orb ${"%.1f".format(aspect.orb)}°)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onEdit) { Text("Edit") }
            OutlinedButton(onClick = onDelete) { Text("Delete", color = MaterialTheme.colorScheme.error) }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ChartWheel(chart: NatalChart, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.82f
        val ringRadius = radius * 0.78f

        // Outer wheel
        drawCircle(color = outline, radius = radius, style = Stroke(1.5f))
        drawCircle(color = outline.copy(alpha = 0.5f), radius = ringRadius, style = Stroke(1f))

        // Sign divisions (12 spokes) + sign glyphs as tick labels
        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30.0) - 90.0)
            val start = Offset(
                center.x + ringRadius * cos(angle).toFloat(),
                center.y + ringRadius * sin(angle).toFloat()
            )
            val end = Offset(
                center.x + radius * cos(angle).toFloat(),
                center.y + radius * sin(angle).toFloat()
            )
            drawLine(outline.copy(alpha = 0.4f), start, end, strokeWidth = 1f)
        }

        // Placements: symbol positioned at its longitude
        chart.placements.forEach { p ->
            val angle = Math.toRadians((p.longitude - 90.0))
            val symbolRadius = ringRadius * 0.86f
            val pos = Offset(
                center.x + symbolRadius * cos(angle).toFloat(),
                center.y + symbolRadius * sin(angle).toFloat()
            )
            // dot on the ring
            val dotPos = Offset(
                center.x + ringRadius * cos(angle).toFloat(),
                center.y + ringRadius * sin(angle).toFloat()
            )
            drawCircle(
                color = primary,
                radius = 3.5f,
                center = dotPos
            )
            // aspect lines handled below via pairs
        }

        // Aspect lines between bodies on the ring
        chart.aspects.forEach { aspect ->
            val pa = chart.placements.firstOrNull { it.body == aspect.a } ?: return@forEach
            val pb = chart.placements.firstOrNull { it.body == aspect.b } ?: return@forEach
            val angA = Math.toRadians(pa.longitude - 90.0)
            val angB = Math.toRadians(pb.longitude - 90.0)
            drawLine(
                color = primary.copy(alpha = 0.55f),
                start = Offset(center.x + ringRadius * cos(angA).toFloat(), center.y + ringRadius * sin(angA).toFloat()),
                end = Offset(center.x + ringRadius * cos(angB).toFloat(), center.y + ringRadius * sin(angB).toFloat()),
                strokeWidth = 1.5f
            )
        }
    }

    // Text labels drawn in normal Compose overlay (Canvas text is clunky)
    Box(modifier = Modifier.height(0.dp)) { }
}

@Composable
private fun BirthProfileEditorDialog(
    profile: BirthProfile?,
    onDismiss: () -> Unit,
    onSave: (BirthProfile) -> Unit
) {
    val isEdit = profile != null
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var dateText by remember {
        mutableStateOf(
            if (profile != null) jdToDateString(profile.jd) else ""
        )
    }
    var timeText by remember { mutableStateOf(if (profile != null) jdToTimeString(profile.jd, profile.timezoneOffsetHours) else "") }
    var latText by remember { mutableStateOf(profile?.latitude?.toString() ?: "") }
    var lonText by remember { mutableStateOf(profile?.longitude?.toString() ?: "") }
    var tzText by remember { mutableStateOf(profile?.timezoneOffsetHours?.toString() ?: "0") }
    var errorText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Birth Profile" else "Add Birth Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dateText, onValueChange = { dateText = it },
                    label = { Text("Birth date (YYYY-MM-DD)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = timeText, onValueChange = { timeText = it },
                    label = { Text("Birth time (HH:MM, 24h, local)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = latText, onValueChange = { latText = it },
                    label = { Text("Latitude (optional, e.g. 64.14)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lonText, onValueChange = { lonText = it },
                    label = { Text("Longitude (optional, e.g. -21.9)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tzText, onValueChange = { tzText = it },
                    label = { Text("Timezone offset at birth (hours, e.g. 0 or 1)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorText.isNotBlank()) {
                    Text(errorText, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val date = java.time.LocalDate.parse(dateText.trim())
                        val (h, m) = if (timeText.contains(":")) {
                            val parts = timeText.split(":")
                            parts[0].trim().toInt() to parts[1].trim().toInt()
                        } else 12 to 0
                        val local = LocalDateTime.of(dateText.trim().split("-")[0].toInt(),
                            dateText.trim().split("-")[1].toInt(), dateText.trim().split("-")[2].toInt(), h, m)
                        val tz = tzText.trim().toDoubleOrNull() ?: 0.0
                        val jd = localDateTimeToJd(local, tz)
                        val p = BirthProfile(
                            id = profile?.id ?: 0,
                            name = name.trim(),
                            jd = jd,
                            latitude = latText.trim().toDoubleOrNull(),
                            longitude = lonText.trim().toDoubleOrNull(),
                            timezoneOffsetHours = tz
                        )
                        onSave(p)
                    } catch (e: Exception) {
                        errorText = "Couldn't parse date/time: ${e.message}"
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// --- JD helpers -----------------------------------------------------------

fun localDateTimeToJd(dt: LocalDateTime, tzOffsetHours: Double): Double {
    // Local civil time -> UT -> JD. java.time does the calendar math.
    val ut = dt.minusSeconds((tzOffsetHours * 3600).toLong())
    val epochSecs = ut.toEpochSecond(ZoneOffset.UTC)
    return 2440587.5 + epochSecs / 86400.0
}

fun jdToDateString(jd: Double): String {
    val unixDays = (jd - 2440587.5).toInt()
    val dt = LocalDateTime.of(1970, 1, 1, 0, 0).plusDays(unixDays.toLong())
    return "%04d-%02d-%02d".format(dt.year, dt.monthValue, dt.dayOfMonth)
}

fun jdToTimeString(jd: Double, tz: Double): String {
    val utHours = ((jd - 2440587.5) % 1.0) * 24.0
    val local = ((utHours + tz) % 24.0 + 24.0) % 24.0
    val h = local.toInt()
    val m = ((local - h) * 60).toInt()
    return "%02d:%02d".format(h, m)
}