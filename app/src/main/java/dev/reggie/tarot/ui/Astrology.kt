package dev.reggie.tarot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private enum class AstroTab(val label: String) {
    Today("Today"),
    Signs("Signs"),
    Planets("Planets"),
    Natal("Natal")
}

private fun elementColor(element: String): Color = when (element.lowercase()) {
    "fire" -> Color(0xFF4A1E1E)
    "earth" -> Color(0xFF2A3D2A)
    "air" -> Color(0xFF2E3D4A)
    "water" -> Color(0xFF1E2E4A)
    else -> Color(0xFF3A3A3A)
}

private fun elementAccent(element: String): Color = when (element.lowercase()) {
    "fire" -> Color(0xFFFF8F8F)
    "earth" -> Color(0xFF9AE6B4)
    "air" -> Color(0xFFA8D0F0)
    "water" -> Color(0xFF8FB8F0)
    else -> Color(0xFFB7B7B7)
}

private fun String.pretty(): String =
    replaceFirstChar { it.uppercase() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstrologyScreen(
    onSignClick: (Int) -> Unit,
    onBodyClick: (Int) -> Unit,
    onOpenNatal: () -> Unit = {},
    contentWindowInsets: androidx.compose.foundation.layout.WindowInsets = ScaffoldDefaults.contentWindowInsets
) {
    var tab by remember { mutableStateOf(AstroTab.Today) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = contentWindowInsets
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = "Astrology",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
            ) {
                AstroTab.entries.forEach { t ->
                    val selected = t == tab
                    TextButton(
                        onClick = { tab = t },
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else Color.Transparent,
                            contentColor = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(t.label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            val pagerState = rememberPagerState(initialPage = tab.ordinal, pageCount = { AstroTab.entries.size })

            // Tabs drive the pager; swiping the pager drives the tabs.
            LaunchedEffect(tab) {
                if (pagerState.currentPage != tab.ordinal) {
                    pagerState.animateScrollToPage(tab.ordinal)
                }
            }
            LaunchedEffect(pagerState.currentPage) {
                if (tab != AstroTab.entries[pagerState.currentPage]) {
                    tab = AstroTab.entries[pagerState.currentPage]
                }
            }

            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 2,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (AstroTab.entries[page]) {
                    AstroTab.Today -> TodayTab()
                    AstroTab.Signs -> SignsTab(onSignClick)
                    AstroTab.Planets -> PlanetsTab(onBodyClick)
                    AstroTab.Natal -> NatalEntry(onOpenNatal = onOpenNatal)
                }
            }
        }
    }
}

@Composable
private fun TodayTab() {
    var daily by remember { mutableStateOf<DailyAstro?>(null) }

    LaunchedEffect(Unit) {
        daily = TarotApp.core.dailyAstro()
    }

    val d = daily
    if (d == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Calculating today's sky…", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${d.weekdayName} — ${d.planetaryDay} day",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.size(12.dp))
                Icon(
                    painter = painterResource(signIconRes(d.moonSign)),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                )
                Text(
                    text = "Moon in ${d.moonSign.pretty()}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "${d.moonDegreesIntoSign.toInt()}° into ${d.moonSign.pretty()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Next: Moon enters ${d.nextSign.pretty()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(Modifier.size(16.dp))

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SUN",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "The Sun is in ${d.sunSign.pretty()} — see the Signs tab for its full profile.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "PLANETARY DAY",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    text = "Today is ${d.weekdayName}, ruled by ${d.planetaryDay.pretty()} — a ${d.planetaryDay.pretty()} day favours workings of ${rulerDomain(d.planetaryDay)}.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(Modifier.size(24.dp))
    }
}


private fun signIconRes(name: String): Int = when (name.lowercase()) {
    "aries" -> R.drawable.ic_sign_aries
    "taurus" -> R.drawable.ic_sign_taurus
    "gemini" -> R.drawable.ic_sign_gemini
    "cancer" -> R.drawable.ic_sign_cancer
    "leo" -> R.drawable.ic_sign_leo
    "virgo" -> R.drawable.ic_sign_virgo
    "libra" -> R.drawable.ic_sign_libra
    "scorpio" -> R.drawable.ic_sign_scorpio
    "sagittarius" -> R.drawable.ic_sign_sagittarius
    "capricorn" -> R.drawable.ic_sign_capricorn
    "aquarius" -> R.drawable.ic_sign_aquarius
    "ophiuchus" -> R.drawable.ic_sign_ophiuchus
    else -> R.drawable.ic_sign_pisces
}


private fun bodyIconRes(name: String): Int = when (name.lowercase()) {
    "sun" -> R.drawable.ic_astro_sun
    else -> R.drawable.ic_astro_moon
}

private fun rulerDomain(planet: String): String = when (planet.lowercase()) {
    "sun" -> "success, vitality, and sovereignty"
    "moon" -> "dreams, intuition, and the ancestors"
    "mercury" -> "communication, study, and safe travel"
    "venus" -> "love, beauty, and reconciliation"
    "mars" -> "courage, protection, and cutting away"
    "jupiter" -> "luck, expansion, and justice"
    "saturn" -> "banishing, binding, and endings"
    else -> "the day's business"
}

@Composable
private fun SignsTab(onSignClick: (Int) -> Unit) {
    var signs by remember { mutableStateOf(listOf<AstroSign>()) }

    LaunchedEffect(Unit) {
        signs = TarotApp.core.getAllAstroSigns()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        items(signs, key = { it.id }) { sign ->
            val container = elementColor(sign.element)
            val accent = elementAccent(sign.element)
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSignClick(sign.id) },
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.outlinedCardColors(containerColor = container)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        painter = painterResource(signIconRes(sign.name)),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(52.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${sign.element} · ${sign.modality}",
                            style = MaterialTheme.typography.labelMedium,
                            color = accent.copy(alpha = 0.9f)
                        )
                        Text(
                            text = sign.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = sign.dates,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanetsTab(onBodyClick: (Int) -> Unit) {
    var bodies by remember { mutableStateOf(listOf<AstroBody>()) }

    LaunchedEffect(Unit) {
        bodies = TarotApp.core.getAllAstroBodies()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        items(bodies, key = { it.id }) { body ->
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBodyClick(body.id) },
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        painter = painterResource(bodyIconRes(body.name)),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = body.day,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = body.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = body.domain,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstroSignDetailScreen(id: Int, onBack: () -> Unit) {
    var sign by remember { mutableStateOf<AstroSign?>(null) }

    LaunchedEffect(id) {
        sign = TarotApp.core.getAstroSign(id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sign?.name ?: "Sign", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val s = sign
        val container = s?.let { elementColor(it.element) } ?: MaterialTheme.colorScheme.surface
        val accent = s?.let { elementAccent(it.element) } ?: MaterialTheme.colorScheme.primary

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            s?.let { sign ->
                ElevatedCard(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(containerColor = container)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(signIconRes(sign.name)),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(96.dp)
                        )
                        Text(
                            text = "${sign.element} · ${sign.modality} · ruled by ${sign.ruler}",
                            style = MaterialTheme.typography.labelLarge,
                            color = accent,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = sign.name,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = sign.dates,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                TextBlockCard("Overview", sign.description, accent)
                TextBlockCard("Traits", sign.traits.joinToString(" · ") { it.pretty() }, accent)
                TextBlockCard("Body", sign.bodyPart.pretty(), accent)
                TextBlockCard("Compatible signs", sign.compatibility.joinToString(", "), accent)
                TextBlockCard("In magic", sign.magicNotes, accent)
                Spacer(Modifier.size(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstroBodyDetailScreen(id: Int, onBack: () -> Unit) {
    var body by remember { mutableStateOf<AstroBody?>(null) }

    LaunchedEffect(id) {
        body = TarotApp.core.getAstroBody(id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(body?.name ?: "Planet", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        body?.let { b ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(bodyIconRes(b.name)),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(96.dp)
                        )
                        Text(
                            text = b.name,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = b.day,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                TextBlockCard("Domain", b.domain.pretty(), MaterialTheme.colorScheme.primary)
                TextBlockCard("Overview", b.description, MaterialTheme.colorScheme.primary)
                TextBlockCard("Colours", b.colorNote, MaterialTheme.colorScheme.primary)
                Spacer(Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun TextBlockCard(label: String, text: String, accent: Color) {
    OutlinedCard(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun Spacer(modifier: Modifier) {
    androidx.compose.foundation.layout.Spacer(modifier)
}

@Composable
private fun NatalEntry(onOpenNatal: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\u2609",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Natal Charts",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "A snapshot of the sky at the moment you were born - every planet's sign, house, and the aspects between them. Add a birth profile to compute yours.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 8.dp)
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onOpenNatal) {
            Text("Open Natal Charts")
        }
    }
}