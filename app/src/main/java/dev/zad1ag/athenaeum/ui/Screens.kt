package dev.zad1ag.athenaeum

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import dev.zad1ag.athenaeum.ui.AppTabRow


enum class MoonPhase {
    NewMoon, WaxingCrescent, FirstQuarter, WaxingGibbous,
    FullMoon, WaningGibbous, LastQuarter, WaningCrescent
}

private fun moonPhaseForDate(date: LocalDate = LocalDate.now()): MoonPhase {
    val knownNewMoon = LocalDate.of(2000, 1, 6)
    val days = ChronoUnit.DAYS.between(knownNewMoon, date).toDouble()
    val cycle = 29.53059
    val age = days % cycle
    val phase = age / cycle
    return when (phase) {
        in 0.0..0.025, in 0.975..1.0 -> MoonPhase.NewMoon
        in 0.025..0.225 -> MoonPhase.WaxingCrescent
        in 0.225..0.275 -> MoonPhase.FirstQuarter
        in 0.275..0.475 -> MoonPhase.WaxingGibbous
        in 0.475..0.525 -> MoonPhase.FullMoon
        in 0.525..0.725 -> MoonPhase.WaningGibbous
        in 0.725..0.775 -> MoonPhase.LastQuarter
        else -> MoonPhase.WaningCrescent
    }
}

private fun nextMajorMoonPhaseName(date: LocalDate = LocalDate.now()): String {
    val phase = moonPhaseForDate(date)
    val order = listOf(
        MoonPhase.NewMoon, MoonPhase.FirstQuarter, MoonPhase.FullMoon,
        MoonPhase.LastQuarter, MoonPhase.NewMoon
    )
    val currentIdx = order.indexOf(phase)
    val nextIdx = if (currentIdx == -1 || currentIdx == order.lastIndex) 0 else currentIdx + 1
    val daysAhead = when (order[nextIdx]) {
        MoonPhase.NewMoon -> daysToPhase(date, 0.0)
        MoonPhase.FirstQuarter -> daysToPhase(date, 0.25)
        MoonPhase.FullMoon -> daysToPhase(date, 0.5)
        MoonPhase.LastQuarter -> daysToPhase(date, 0.75)
        else -> 0
    }
    val target = date.plusDays(daysAhead.toLong())
    return "${order[nextIdx].toReadable()} on ${target.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))}"
}

private fun daysToPhase(date: LocalDate, targetPhase: Double): Int {
    var days = 0
    while (days < 30) {
        val check = date.plusDays(days.toLong())
        val knownNewMoon = LocalDate.of(2000, 1, 6)
        val d = ChronoUnit.DAYS.between(knownNewMoon, check).toDouble()
        val cycle = 29.53059
        val current = (d % cycle) / cycle
        val normalized = if (current < 0) current + 1 else current
        if (kotlin.math.abs(normalized - targetPhase) < 0.02) return days
        days++
    }
    return days
}

private fun MoonPhase.toReadable(): String = name.replace(Regex("([a-z])([A-Z])"), "$1 $2")

fun MoonPhaseRequirement.toReadable(): String = when (this) {
    MoonPhaseRequirement.any -> "Any moon phase"
    MoonPhaseRequirement.new_moon -> "New moon"
    MoonPhaseRequirement.waxing_crescent -> "Waxing crescent"
    MoonPhaseRequirement.first_quarter -> "First quarter"
    MoonPhaseRequirement.waxing_gibbous -> "Waxing gibbous"
    MoonPhaseRequirement.full_moon -> "Full moon"
    MoonPhaseRequirement.waning_gibbous -> "Waning gibbous"
    MoonPhaseRequirement.last_quarter -> "Last quarter"
    MoonPhaseRequirement.waning_crescent -> "Waning crescent"
}

private fun MoonPhase.iconRes(): Int = when (this) {
    MoonPhase.NewMoon -> R.drawable.ic_moon_new
    MoonPhase.WaxingCrescent -> R.drawable.ic_moon_waxing_crescent
    MoonPhase.FirstQuarter -> R.drawable.ic_moon_first_quarter
    MoonPhase.WaxingGibbous -> R.drawable.ic_moon_waxing_gibbous
    MoonPhase.FullMoon -> R.drawable.ic_moon_full
    MoonPhase.WaningGibbous -> R.drawable.ic_moon_waning_gibbous
    MoonPhase.LastQuarter -> R.drawable.ic_moon_last_quarter
    MoonPhase.WaningCrescent -> R.drawable.ic_moon_waning_crescent
}

fun MoonPhaseRequirement.iconRes(): Int = when (this) {
    MoonPhaseRequirement.any -> R.drawable.ic_moon_full
    MoonPhaseRequirement.new_moon -> R.drawable.ic_moon_new
    MoonPhaseRequirement.waxing_crescent -> R.drawable.ic_moon_waxing_crescent
    MoonPhaseRequirement.first_quarter -> R.drawable.ic_moon_first_quarter
    MoonPhaseRequirement.waxing_gibbous -> R.drawable.ic_moon_waxing_gibbous
    MoonPhaseRequirement.full_moon -> R.drawable.ic_moon_full
    MoonPhaseRequirement.waning_gibbous -> R.drawable.ic_moon_waning_gibbous
    MoonPhaseRequirement.last_quarter -> R.drawable.ic_moon_last_quarter
    MoonPhaseRequirement.waning_crescent -> R.drawable.ic_moon_waning_crescent
}

private fun moonPhaseCalendar(yearMonth: java.time.YearMonth): Map<LocalDate, MoonPhase> {
    val map = mutableMapOf<LocalDate, MoonPhase>()
    for (day in 1..yearMonth.lengthOfMonth()) {
        val date = yearMonth.atDay(day)
        map[date] = moonPhaseForDate(date)
    }
    return map
}

/**
 * Home-screen moon widget: the current phase's icon drawn large inside a
 * glowing ring with illumination percentage and next-major-phase countdown.
 * Tapping opens the full moon calendar dialog.
 */
@Composable
private fun MoonWidget(
    phase: MoonPhase,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val knownNewMoon = LocalDate.of(2000, 1, 6)
    val cycle = 29.53059
    val age = ChronoUnit.DAYS.between(knownNewMoon, LocalDate.now()).toDouble() % cycle
    val illumination = ((1 - kotlin.math.cos(2 * Math.PI * age / cycle)) / 2 * 100).toInt()

    val glow by rememberInfiniteTransition(label = "moonGlow").animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moonGlowPulse"
    )

    val ringColor = MaterialTheme.colorScheme.primary
    val glowColor = when (phase) {
        MoonPhase.FullMoon -> Color(0xFFE8E4D8)
        MoonPhase.NewMoon -> Color(0xFF3A3A4A)
        else -> Color(0xFFC9C2B0)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 24.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Soft bloom behind the disc
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.35f * glow),
                                Color.Transparent
                            )
                        ),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
            // Pulsing ring
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .border(
                        width = 2.dp,
                        color = ringColor.copy(alpha = 0.5f * glow),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
            Icon(
                painter = painterResource(phase.iconRes()),
                contentDescription = phase.toReadable(),
                modifier = Modifier.size(84.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = phase.toReadable(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "$illumination% illuminated",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Next: ${nextMajorMoonPhaseName()}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * Home-screen at-a-glance astro strip: today's sun sign and computed moon
 * sign with the planetary day, pulling from the Rust astro engine.
 */
@Composable
private fun AstroGlance(modifier: Modifier = Modifier) {
    var daily by remember { mutableStateOf<DailyAstro?>(null) }

    LaunchedEffect(Unit) {
        daily = TarotApp.core.dailyAstro()
    }

    val d = daily ?: return

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.ic_astro_sun),
                    contentDescription = "Sun",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Sun",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = d.sunSign.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "·",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.ic_astro_moon),
                    contentDescription = "Moon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Moon",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = d.moonSign.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Text(
            text = "${d.weekdayName} — ${d.planetaryDay.replaceFirstChar { it.uppercase() }} day",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    openMoonCalendar: Boolean = false,
    openMoonCalendarReset: () -> Unit = {},
    onTarotClick: () -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var showCalendar by remember { mutableStateOf(openMoonCalendar) }

    // Consume the one-shot "open calendar" flag so it doesn't re-trigger
    // every time Home re-enters composition (e.g. switching tabs and back).
    LaunchedEffect(Unit) {
        if (openMoonCalendar) {
            openMoonCalendarReset()
        }
    }

    LaunchedEffect(Unit) {
        userName = TarotApp.core.getUserName()
    }

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Good night"
    }

    val dateText = try {
        LocalDate.now().format(
            DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
        )
    } catch (_: Exception) {
        LocalDate.now().toString()
    }

    val phase = moonPhaseForDate()

    if (showCalendar) {
        MoonCalendarDialog(onDismiss = { showCalendar = false })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            MoonWidget(
                phase = phase,
                onClick = { showCalendar = true },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = if (userName.isBlank()) "$greeting" else "$greeting, $userName",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Today is $dateText",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            AstroGlance(modifier = Modifier.padding(top = 20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoonCalendarDialog(onDismiss: () -> Unit) {
    var yearMonth by remember { mutableStateOf(java.time.YearMonth.now()) }
    val phases = remember(yearMonth) { moonPhaseCalendar(yearMonth) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { yearMonth = yearMonth.minusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous month")
                    }
                    IconButton(onClick = { yearMonth = yearMonth.plusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next month")
                    }
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items((1..yearMonth.lengthOfMonth()).toList()) { day ->
                        val date = yearMonth.atDay(day)
                        val phase = phases[date] ?: moonPhaseForDate(date)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$day",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                painter = painterResource(phase.iconRes()),
                                contentDescription = phase.toReadable(),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

sealed class TarotFilter(val label: String) {
    object Major : TarotFilter("Major")
    object Wands : TarotFilter("Wands")
    object Cups : TarotFilter("Cups")
    object Swords : TarotFilter("Swords")
    object Pentacles : TarotFilter("Pentacles")
}

private val tarotFilters = listOf(
    TarotFilter.Major,
    TarotFilter.Wands,
    TarotFilter.Cups,
    TarotFilter.Swords,
    TarotFilter.Pentacles
)

private fun tarotFilterColor(filter: TarotFilter): Color = when (filter) {
    TarotFilter.Major -> Color(0xFF2E2840)
    TarotFilter.Wands -> Color(0xFF3D2E1A)
    TarotFilter.Cups -> Color(0xFF1E2E3A)
    TarotFilter.Swords -> Color(0xFF2A3A4A)
    TarotFilter.Pentacles -> Color(0xFF1A331A)
}

private fun tarotFilterAccent(filter: TarotFilter): Color = when (filter) {
    TarotFilter.Major -> Color(0xFFC9B8F0)
    TarotFilter.Wands -> Color(0xFFF0A890)
    TarotFilter.Cups -> Color(0xFFA8D0F0)
    TarotFilter.Swords -> Color(0xFFB8C8D8)
    TarotFilter.Pentacles -> Color(0xFF9AE6B4)
}

private fun tarotToSuit(filter: TarotFilter): Suit? = when (filter) {
    TarotFilter.Wands -> Suit.wands
    TarotFilter.Cups -> Suit.cups
    TarotFilter.Swords -> Suit.swords
    TarotFilter.Pentacles -> Suit.pentacles
    else -> null
}

private fun suitContainerColor(suit: Suit): Color = when (suit) {
    Suit.wands -> Color(0xFF3D2E1A)
    Suit.cups -> Color(0xFF1E2E3A)
    Suit.swords -> Color(0xFF2A3A4A)
    Suit.pentacles -> Color(0xFF1A331A)
    Suit.none -> Color(0xFF2E2840)
}

private fun suitAccentColor(suit: Suit): Color = when (suit) {
    Suit.wands -> Color(0xFFF0A890)
    Suit.cups -> Color(0xFFA8D0F0)
    Suit.swords -> Color(0xFFB8C8D8)
    Suit.pentacles -> Color(0xFF9AE6B4)
    Suit.none -> Color(0xFFC9B8F0)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TarotScreen(
    onCardClick: (Int) -> Unit
) {
    var selectedFilter by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(initialPage = selectedFilter, pageCount = { tarotFilters.size })
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val cardsCache = remember { mutableStateMapOf<Int, List<Card>>() }

    LaunchedEffect(selectedFilter) {
        if (pagerState.currentPage != selectedFilter) {
            pagerState.animateScrollToPage(selectedFilter)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        selectedFilter = pagerState.currentPage
    }

    LaunchedEffect(Unit) {
        tarotFilters.indices.forEach { idx ->
            if (cardsCache[idx] == null) {
                cardsCache[idx] = when (tarotFilters[idx]) {
                    TarotFilter.Major -> TarotApp.core.getMajorArcana()
                    TarotFilter.Wands -> TarotApp.core.getCardsBySuit(Suit.wands)
                    TarotFilter.Cups -> TarotApp.core.getCardsBySuit(Suit.cups)
                    TarotFilter.Swords -> TarotApp.core.getCardsBySuit(Suit.swords)
                    TarotFilter.Pentacles -> TarotApp.core.getCardsBySuit(Suit.pentacles)
                }
            }
        }
    }

    val allCards = remember(cardsCache.toMap(), TarotApp.overrides.revision) {
        cardsCache.values.flatten().map { it.withOverride(TarotApp.overrides.getCardOverride(it.id)) }
    }

    val displayedCards = remember(allCards, selectedFilter, searchQuery, isSearching) {
        if (isSearching && searchQuery.isNotBlank()) {
            allCards.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.keywords.any { k -> k.contains(searchQuery, ignoreCase = true) } ||
                        it.uprightMeaning.contains(searchQuery, ignoreCase = true) ||
                        it.reversedMeaning.contains(searchQuery, ignoreCase = true)
            }
        } else {
            allCards.filter { it.suit.let { s -> s == tarotToSuit(tarotFilters[selectedFilter]) } || (tarotFilters[selectedFilter] == TarotFilter.Major && it.arcana == Arcana.major) }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (!isSearching) {
                androidx.compose.material3.FloatingActionButton(
                    onClick = { isSearching = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = "Tarot",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            if (isSearching) {
                dev.zad1ag.athenaeum.SearchHeader(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClose = {
                        isSearching = false
                        searchQuery = ""
                    }
                )
            } else {
                AppTabRow(
                    tabs = tarotFilters.map { it.label },
                    selectedIndex = selectedFilter,
                    onSelected = { selectedFilter = it }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            }
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 4,
                userScrollEnabled = !isSearching,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val cards = if (isSearching) displayedCards else cardsCache[page] ?: emptyList()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cards, key = { it.id }) { card ->
                        TarotListItem(card, tarotFilters[page], onCardClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun TarotListItem(card: Card, filter: TarotFilter, onCardClick: (Int) -> Unit) {
    val tone = remember(card.tone) { toneColors(card.tone.toTone()) }
    val (containerColor, accent) = remember {
        if (card.arcana == Arcana.major) {
            tarotFilterColor(filter) to tarotFilterAccent(filter)
        } else {
            suitContainerColor(card.suit) to suitAccentColor(card.suit)
        }
    }
    val label = if (card.arcana == Arcana.major) "Major Arcana" else card.suit.name.replaceFirstChar { it.uppercase() }
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick(card.id) },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                MiniCardArtwork(card, tint = accent)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = accent.copy(alpha = 0.9f)
                    )
                    OutlinedCard(
                        shape = MaterialTheme.shapes.small,
                        colors = CardDefaults.outlinedCardColors(containerColor = tone.container)
                    ) {
                        Text(
                            text = when (card.tone.toTone()) {
                                Tone.positive -> "Good"
                                Tone.negative -> "Bad"
                                Tone.neutral -> "Neutral"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = line2For(card),
                    style = MaterialTheme.typography.titleMedium,
                    color = accent.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = card.keywords.take(3).joinToString(", ").ifBlank { card.uprightMeaning },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(cardId: Int, onBack: () -> Unit) {
    var card by remember { mutableStateOf<Card?>(null) }

    LaunchedEffect(cardId) {
        card = TarotApp.core.getCard(cardId)?.withOverride(TarotApp.overrides.getCardOverride(cardId))
    }

    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(card?.name ?: "Card", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    card?.let { current ->
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit card")
                        }
                        if (showEditDialog) {
                            EditCardDialog(
                                card = current,
                                onDismiss = { showEditDialog = false },
                                onSave = { updated ->
                                    card = updated
                                    showEditDialog = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        card?.let { c ->
            val scroll = rememberScrollState()
            val tone = c.tone.toTone()
            val toneColors = toneColors(tone)
            val label = if (c.arcana == Arcana.major) "Major Arcana" else c.suit.name.replaceFirstChar { it.uppercase() }
            val (container, accent) = remember {
                if (c.arcana == Arcana.major) {
                    Color(0xFF2E2840) to Color(0xFFC9B8F0)
                } else {
                    suitContainerColor(c.suit) to suitAccentColor(c.suit)
                }
            }
            var selectedContext by remember { mutableStateOf("general") }
            val contextOptions = listOf(
                "general" to "General",
                "love_dating" to "Dating",
                "love_single" to "Single",
                "love_relationship" to "Partner",
                "career" to "Career",
                "money" to "Money",
                "health" to "Health"
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scroll)
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(containerColor = container)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            MiniCardArtwork(c, tint = Color.White)
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = c.name,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        OutlinedCard(
                            shape = MaterialTheme.shapes.small,
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = toneColors.container,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = when (tone) {
                                    Tone.positive -> "Good"
                                    Tone.negative -> "Bad"
                                    Tone.neutral -> "Neutral"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                AppTabRow(
                    tabs = contextOptions.map { it.second },
                    selectedIndex = contextOptions.indexOfFirst { it.first == selectedContext }.coerceAtLeast(0),
                    onSelected = { selectedContext = contextOptions[it].first },
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                MeaningBlock("Meaning", c.contexts.get(selectedContext))
                MeaningBlock("Upright Meaning", c.uprightMeaning)
                MeaningBlock("Reversed Meaning", c.reversedMeaning)
                KeywordBlock(c.keywords)
            }
        } ?: Text(
            "Card not found",
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.error
        )
    }
}



fun line2For(card: Card): String {
    return if (card.arcana == Arcana.major) {
        "Major Arcana"
    } else {
        val rank = card.number?.let { "$it" } ?: "Court"
        "$rank of ${card.suit.name.replaceFirstChar { it.uppercase() }}"
    }
}

data class ToneColors(val container: Color, val text: Color, val accent: Color)

private val positiveTone = ToneColors(
    container = Color(0xFF1A291A),
    text = Color(0xFFB8E2B8),
    accent = Color(0xFF8FE08F)
)
private val negativeTone = ToneColors(
    container = Color(0xFF2A1A1A),
    text = Color(0xFFE2B8B8),
    accent = Color(0xFFE08F8F)
)
private val neutralTone = ToneColors(
    container = Color(0xFF201F24),
    text = Color(0xFFD2D0DB),
    accent = Color(0xFFB0ACC0)
)

internal fun toneColors(tone: Tone): ToneColors = when (tone) {
    Tone.positive -> positiveTone
    Tone.negative -> negativeTone
    Tone.neutral -> neutralTone
}

@Composable
private fun CardArtwork(card: Card, tint: Color = Color(0xFFB7B7B7)) {
    if (card.id in 0..21) {
        val res = tarotIconResource(card.id)
        if (res != 0) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(res),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    tint = tint
                )
            }
            return
        }
    }
    val suitRes = suitIconResource(card.suit)
    if (suitRes != 0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                card.number?.let { num ->
                    val roman = numberToRoman(num)
                    if (roman.isNotBlank()) {
                        Text(
                            text = roman,
                            style = MaterialTheme.typography.headlineSmall,
                            color = tint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Icon(
                    painter = painterResource(suitRes),
                    contentDescription = null,
                    modifier = Modifier.size(128.dp),
                    tint = tint
                )
            }
        }
        return
    }
}

@Composable
private fun MiniCardArtwork(card: Card, tint: Color? = null) {
    val iconTint = tint ?: Color(0xFFB7B7B7)
    if (card.id in 0..21) {
        val res = tarotIconResource(card.id)
        if (res != 0) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(res),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    tint = iconTint
                )
            }
            return
        }
    }
    val suitRes = suitIconResource(card.suit)
    if (suitRes != 0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                card.number?.let { num ->
                    val roman = numberToRoman(num)
                    if (roman.isNotBlank()) {
                        Text(
                            text = roman,
                            style = MaterialTheme.typography.titleLarge,
                            color = iconTint,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp
                        )
                    }
                }
                Icon(
                    painter = painterResource(suitRes),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = iconTint
                )
            }
        }
        return
    }
}

@Composable
private fun MeaningBlock(label: String, text: String) {
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
                color = MaterialTheme.colorScheme.primary,
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

@Composable
private fun KeywordBlock(keywords: List<String>) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Keywords".uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = keywords.joinToString("  ·  "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}



@Composable
private fun LabelBlock(label: String, text: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var userName by remember { mutableStateOf("") }
    var savedMessage by remember { mutableStateOf("") }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var restoreMessage by remember { mutableStateOf("") }
    var showBackupRestoreConfirm by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var backupMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    val featureStates = FeatureSettings.Feature.entries.associateWith {
        remember(it) { mutableStateOf(FeatureSettings.isEnabled(context, it)) }
    }
    var featureVersion by remember { mutableStateOf(0) }
    var widgetBackground by remember {
        mutableStateOf(FeatureSettings.isWidgetBackgroundEnabled(context))
    }
    var dynamicColor by remember {
        mutableStateOf(FeatureSettings.isDynamicColorEnabled(context))
    }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(BackupManager.MIME_TYPE)
    ) { uri ->
        if (uri != null) {
            val ok = BackupManager.createBackup(context, uri)
            backupMessage = if (ok) "Backup created." else "Backup failed."
        }
    }

    val restoreFilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
            showBackupRestoreConfirm = true
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = pendingRestoreUri
        if (result.resultCode == android.app.Activity.RESULT_OK && uri != null) {
            val ok = BackupManager.restoreBackup(context, uri)
            backupMessage = if (ok) "Backup restored." else "Restore failed — is this a valid backup file?"
        }
        pendingRestoreUri = null
    }

    LaunchedEffect(Unit) {
        userName = TarotApp.core.getUserName()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.headlineSmall) }
            )
        }
    ) { padding ->
        @Composable
        fun NameField() {
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Who are you?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Your name appears on the home greeting.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            NameField()
            Button(
                onClick = {
                    TarotApp.core.setUserName(userName.trim())
                    savedMessage = "Saved."
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Save")
            }
            if (savedMessage.isNotBlank()) {
                Text(
                    text = savedMessage,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Restore content",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Bring back any built-in spirits, deities, or spells you deleted. Your custom entries are never touched and no duplicates are created.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = { showRestoreConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Restore built-in entries")
            }
            if (restoreMessage.isNotBlank()) {
                Text(
                    text = restoreMessage,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Visible sections",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Turn off anything you don't use. Hidden sections disappear from the bottom bar and home screen — nothing is deleted.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            FeatureSettings.Feature.entries.forEach { feature ->
                val parent = FeatureSettings.sectionOf(feature)
                if (parent != null) return@forEach // rendered under its parent below

                val state = featureStates[feature] ?: remember { mutableStateOf(true) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = feature.label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = state.value,
                        onCheckedChange = { checked ->
                            // Toggling a section flips all its children with it.
                            FeatureSettings.Feature.entries
                                .filter { FeatureSettings.sectionOf(it) == feature }
                                .forEach { child ->
                                    featureStates[child]?.value = checked
                                    FeatureSettings.setEnabled(context, child, checked)
                                }
                            state.value = checked
                            FeatureSettings.setEnabled(context, feature, checked)
                            featureVersion++
                        }
                    )
                }

                val children = FeatureSettings.Feature.entries
                    .filter { FeatureSettings.sectionOf(it) == feature }
                if (children.isNotEmpty() && state.value) {
                    children.forEach { child ->
                        val childState = featureStates[child] ?: remember { mutableStateOf(true) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 28.dp, top = 2.dp, bottom = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = child.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = childState.value,
                                enabled = state.value,
                                onCheckedChange = { checked ->
                                    childState.value = checked
                                    FeatureSettings.setEnabled(context, child, checked)
                                    featureVersion++
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Widget background",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = widgetBackground,
                    onCheckedChange = { checked ->
                        widgetBackground = checked
                        FeatureSettings.setWidgetBackgroundEnabled(context, checked)
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Use system colors (Material You)",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = dynamicColor,
                    onCheckedChange = { checked ->
                        dynamicColor = checked
                        FeatureSettings.setDynamicColorEnabled(context, checked)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Backup & restore",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Back up everything — spirits, deities, spells, offerings, readings, settings, and your card/rune edits — as a single file you can save anywhere. Restoring replaces all current data with the backup's.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = { backupLauncher.launch(BackupManager.DEFAULT_FILE_NAME) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Create backup")
            }
            Button(
                onClick = {
                    restoreFilePicker.launch(
                        arrayOf("application/zip", "application/octet-stream", "application/x-terotbackup")
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Restore from backup")
            }
            if (backupMessage.isNotBlank()) {
                Text(
                    text = backupMessage,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            val context = LocalContext.current
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/zad1ag"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Support me on Ko-fi")
            }
            Text(
                text = "Credits",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp)
            )
            val linkColor = MaterialTheme.colorScheme.primary
            val linkStyle = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Icons from ")
                    Text(
                        text = "game-icons.net",
                        color = linkColor,
                        style = linkStyle,
                        modifier = Modifier.clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://game-icons.net")))
                        }
                    )
                    Text(" (CC BY 3.0)")
                }
            }
        }
    }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text("Restore built-in entries?") },
            text = { Text("All deleted built-in spirits, deities, and spells will be restored. Your custom entries will not be changed or duplicated.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        TarotApp.core.restoreBuiltins()
                        restoreMessage = "Built-in entries restored."
                        showRestoreConfirm = false
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBackupRestoreConfirm) {
        AlertDialog(
            onDismissRequest = {
                showBackupRestoreConfirm = false
                pendingRestoreUri = null
            },
            title = { Text("Restore from backup?") },
            text = { Text("Your current data will be replaced with the backup's contents. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBackupRestoreConfirm = false
                        pendingRestoreUri?.let { uri ->
                            val ok = BackupManager.restoreBackup(context, uri)
                            backupMessage = if (ok) "Backup restored." else "Restore failed — is this a valid backup file?"
                        }
                        pendingRestoreUri = null
                    }
                ) {
                    Text("Restore", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBackupRestoreConfirm = false
                    pendingRestoreUri = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EditCardDialog(
    card: Card,
    onDismiss: () -> Unit,
    onSave: (Card) -> Unit
) {
    var base by remember { mutableStateOf(card) }
    LaunchedEffect(card.id) {
        base = TarotApp.core.getCard(card.id) ?: card
    }
    var name by remember(card.id) { mutableStateOf(card.name) }
    var keywords by remember(card.id) { mutableStateOf(card.keywords.joinToString(", ")) }
    var upright by remember(card.id) { mutableStateOf(card.uprightMeaning) }
    var reversed by remember(card.id) { mutableStateOf(card.reversedMeaning) }
    val contextFields = remember(card.id) {
        mutableStateMapOf(
            "general" to card.contexts.general,
            "love_dating" to card.contexts.loveDating,
            "love_single" to card.contexts.loveSingle,
            "love_relationship" to card.contexts.loveRelationship,
            "career" to card.contexts.career,
            "money" to card.contexts.money,
            "health" to card.contexts.health
        )
    }
    val contextLabels = remember {
        mapOf(
            "general" to "General meaning",
            "love_dating" to "Dating meaning",
            "love_single" to "Single meaning",
            "love_relationship" to "Partner meaning",
            "career" to "Career meaning",
            "money" to "Money meaning",
            "health" to "Health meaning"
        )
    }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${card.name}") },
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
                    value = keywords,
                    onValueChange = { keywords = it },
                    label = { Text("Keywords (comma-separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = upright,
                    onValueChange = { upright = it },
                    label = { Text("Upright meaning") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = reversed,
                    onValueChange = { reversed = it },
                    label = { Text("Reversed meaning") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                contextFields.forEach { (key, value) ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = { contextFields[key] = it },
                        label = { Text(contextLabels[key] ?: key) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank() || upright.isBlank()) {
                        error = "Name and upright meaning are required."
                        return@TextButton
                    }
                    val editedContexts = ContextualMeanings(
                        general = contextFields["general"] ?: base.contexts.general,
                        loveDating = contextFields["love_dating"] ?: base.contexts.loveDating,
                        loveSingle = contextFields["love_single"] ?: base.contexts.loveSingle,
                        loveRelationship = contextFields["love_relationship"] ?: base.contexts.loveRelationship,
                        career = contextFields["career"] ?: base.contexts.career,
                        money = contextFields["money"] ?: base.contexts.money,
                        health = contextFields["health"] ?: base.contexts.health
                    )
                    TarotApp.overrides.saveCardOverride(
                        card.id,
                        CardOverride(
                            name = name.trim().takeIf { it != base.name },
                            keywords = keywords.split(",").map { it.trim() }.filter { it.isNotBlank() }.takeIf { it != base.keywords },
                            uprightMeaning = upright.trim().takeIf { it != base.uprightMeaning },
                            reversedMeaning = reversed.trim().takeIf { it != base.reversedMeaning },
                            contexts = editedContexts.takeIf { it != base.contexts }
                        )
                    )
                    onSave(card.withOverride(TarotApp.overrides.getCardOverride(card.id)))
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
