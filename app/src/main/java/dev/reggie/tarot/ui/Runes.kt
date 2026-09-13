package dev.reggie.tarot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class RuneAett {
    Freyr, Hagalaz, Tyr
}

data class Rune(
    val id: Int,
    val name: String,
    val symbol: String,
    val letters: String,
    val aett: RuneAett,
    val meaning: String,
    val keywords: List<String>,
    val reversed: String,
    val bindruneUses: List<String>
)

private fun aettColor(aett: RuneAett): Color = when (aett) {
    RuneAett.Freyr -> Color(0xFF3D2E1A)
    RuneAett.Hagalaz -> Color(0xFF1E2E3A)
    RuneAett.Tyr -> Color(0xFF2E2840)
}

private fun aettAccent(aett: RuneAett): Color = when (aett) {
    RuneAett.Freyr -> Color(0xFFF0C89A)
    RuneAett.Hagalaz -> Color(0xFFA8D0F0)
    RuneAett.Tyr -> Color(0xFFC9B8F0)
}

private val runes: List<Rune> = listOf(
    Rune(
        id = 1, name = "Fehu", symbol = "\u16A0", letters = "f, v",
        aett = RuneAett.Freyr,
        meaning = "Cattle, mobile wealth, the reward of effort, the foundational resource. Fehu is prosperity earned through work, not a gift.",
        keywords = listOf("wealth", "reward", "abundance", "primal value"),
        reversed = "Loss of wealth, scarcity, greed, poverty mindset, resources slipping away.",
        bindruneUses = listOf(
            "Combine with Uruz for wealth built on strength.",
            "Combine with Berkano for flourishing, growing assets.",
            "Wear on the dominant hand to attract paid opportunities."
        )
    ),
    Rune(
        id = 2, name = "Uruz", symbol = "\u16A2", letters = "u, û",
        aett = RuneAett.Freyr,
        meaning = "Aurochs, untamed strength, endurance, and the muscle needed to shape one's life. It is initiation and raw vitality.",
        keywords = listOf("strength", "power", "wild force", "endurance"),
        reversed = "Weakness, recklessness, misapplied force, burnout, lack of follow-through.",
        bindruneUses = listOf(
            "Combine with Fehu to turn effort into tangible reward.",
            "Combine with Thurisaz to break barriers with brute will.",
            "Carve before physical trials or endurance work."
        )
    ),
    Rune(
        id = 3, name = "Thurisaz", symbol = "\u16A6", letters = "th",
        aett = RuneAett.Freyr,
        meaning = "Thorn, Thor's hammer, conflict, disruption, and the protective edge. It is a weapon against stagnation.",
        keywords = listOf("defense", "conflict", "catharsis", "breaking point"),
        reversed = "Impotence, danger, cruelty, self-sabotage, traps laid by others.",
        bindruneUses = listOf(
            "Combine with Algiz for layered protection against harm.",
            "Combine with Isa to freeze an enemy's advance.",
            "Use as a warding point facing outward on doorposts."
        )
    ),
    Rune(
        id = 4, name = "Ansuz", symbol = "\u16A8", letters = "a, ã",
        aett = RuneAett.Freyr,
        meaning = "Mouth, breath, Odin, communication, divine inspiration, and the spoken word as a creative force.",
        keywords = listOf("communication", "inspiration", "wisdom", "breath"),
        reversed = "Miscommunication, empty speech, manipulation, lies, blocked inspiration.",
        bindruneUses = listOf(
            "Combine with Ehwaz to send intent across distance.",
            "Combine with Raidho for persuasive, far-reaching speech.",
            "Trace on the throat before divination or important conversations."
        )
    ),
    Rune(
        id = 5, name = "Raidho", symbol = "\u16B1", letters = "r",
        aett = RuneAett.Freyr,
        meaning = "Wagon, rhythm, movement, ordered travel, and the right path. Raidho is the journey as much as the destination.",
        keywords = listOf("journey", "movement", "right action", "rhythm"),
        reversed = "Detours, delays, aimless motion, travel mishaps, resisting the path.",
        bindruneUses = listOf(
            "Combine with Ehwaz for safe, transformative travel.",
            "Combine with Laguz to ride emotional currents without drowning.",
            "Mark luggage or vehicles for protection on the road."
        )
    ),
    Rune(
        id = 6, name = "Kenaz", symbol = "\u16B2", letters = "k, c",
        aett = RuneAett.Freyr,
        meaning = "Torch, controlled fire, craft, knowledge, and the inner light that reveals. Kenaz is skill applied with purpose.",
        keywords = listOf("skill", "craft", "illumination", "creativity"),
        reversed = "Creative block, burned out, poor technique, destructive knowledge.",
        bindruneUses = listOf(
            "Combine with Uruz to forge raw talent into mastery.",
            "Combine with Sowilo to push a project to completion.",
            "Use on tools dedicated to making or learning."
        )
    ),
    Rune(
        id = 7, name = "Gebo", symbol = "\u16B7", letters = "g",
        aett = RuneAett.Freyr,
        meaning = "Gift, reciprocity, balance, and sacred exchange. Gebo marks obligations that must be honored and partnerships of equal value.",
        keywords = listOf("gift", "balance", "partnership", "exchange"),
        reversed = "Gebo has no reversed form; imbalance here is read as a warning that an exchange has become one-sided.",
        bindruneUses = listOf(
            "Combine with Wunjo for joy shared between people.",
            "Combine with Mannaz to seal formal pacts.",
            "Carve when making an oath or accepting a meaningful gift."
        )
    ),
    Rune(
        id = 8, name = "Wunjo", symbol = "\u16B9", letters = "w",
        aett = RuneAett.Freyr,
        meaning = "Joy, fellowship, harmony, and the reward of right community. Wunjo is the calm after earned victory.",
        keywords = listOf("joy", "harmony", "contentment", "community"),
        reversed = "Discord, alienation, shallow happiness, community turned toxic.",
        bindruneUses = listOf(
            "Combine with Berkano for joyful family bonds.",
            "Combine with Mannaz for workplace or social harmony.",
            "Wear during gatherings or reconciliation work."
        )
    ),
    Rune(
        id = 9, name = "Hagalaz", symbol = "\u16BA", letters = "h",
        aett = RuneAett.Hagalaz,
        meaning = "Hail, destructive natural force, uncontrollable change, and the trial that clears the ground for new growth.",
        keywords = listOf("disruption", "crisis", "necessary destruction", "change"),
        reversed = "Hagalaz has no reversed form; its presence always signals disruption, though the scale may vary.",
        bindruneUses = listOf(
            "Combine with Nauthiz to survive hardship with discipline.",
            "Combine with Berkano to rebuild after collapse.",
            "Use only when old structures must be torn down."
        )
    ),
    Rune(
        id = 10, name = "Nauthiz", symbol = "\u16BE", letters = "n",
        aett = RuneAett.Hagalaz,
        meaning = "Need, necessity, friction that forces innovation, and the fire born from constraint. Nauthiz strengthens what endures.",
        keywords = listOf("need", "resistance", "willpower", "discipline"),
        reversed = "Unresolved longing, deprivation, dependency, giving up under pressure.",
        bindruneUses = listOf(
            "Combine with Uruz to endure and overcome hardship.",
            "Combine with Sowilo to break through impossible deadlines.",
            "Carve when you must do without something and still win."
        )
    ),
    Rune(
        id = 11, name = "Isa", symbol = "\u16C1", letters = "i, ï",
        aett = RuneAett.Hagalaz,
        meaning = "Ice, stillness, stasis, and the pause where clarity forms. Isa can preserve or imprison depending on intent.",
        keywords = listOf("stillness", "concentration", "delay", "preservation"),
        reversed = "Isa has no reversed form; drawn reversed, it warns that stasis has become a trap.",
        bindruneUses = listOf(
            "Combine with Algiz to freeze out an attacker or intruder.",
            "Combine with Perthro during divination to hold a situation still for study.",
            "Use in bindings that need a firm halt."
        )
    ),
    Rune(
        id = 12, name = "Jera", symbol = "\u16C3", letters = "j, y",
        aett = RuneAett.Hagalaz,
        meaning = "Year, harvest, cycles, and the reward that follows patience. Jera is the slow turning of seasons.",
        keywords = listOf("harvest", "cycles", "patience", "reward"),
        reversed = "Jera has no reversed form; read against the grain as premature harvest, bad timing, or ecological neglect.",
        bindruneUses = listOf(
            "Combine with Fehu to secure a future harvest.",
            "Combine with Berkano for long-term growth plans.",
            "Mark garden or project journals to track slow progress."
        )
    ),
    Rune(
        id = 13, name = "Eihwaz", symbol = "\u16C7", letters = "ï, ï",
        aett = RuneAett.Hagalaz,
        meaning = "Yew,death and life joined, the axis between worlds, and the long memory of roots. Eihwaz grants passage through fear.",
        keywords = listOf("endurance", "transition", "world-tree", "resilience"),
        reversed = "Eihwaz has no reversed form; a warning of entombment, rotting foundations, or being split between worlds.",
        bindruneUses = listOf(
            "Combine with Uruz to survive initiatory ordeal.",
            "Combine with Algiz for safe spirit-road passage.",
            "Wear before mediumship or ancestor work."
        )
    ),
    Rune(
        id = 14, name = "Perthro", symbol = "\u16C8", letters = "p",
        aett = RuneAett.Hagalaz,
        meaning = "Dice-cup, fate, chance, hidden matters, and the womb from which outcomes emerge. Perthro is the well of wyrd.",
        keywords = listOf("fate", "chance", "mystery", "womb"),
        reversed = "Unwelcome revelation, gambling losses, hidden enemies, secrets surfacing destructively.",
        bindruneUses = listOf(
            "Combine with Ansuz for clear divinatory messages.",
            "Combine with Laguz to read water or dream omens.",
            "Mark a divination pouch or cup."
        )
    ),
    Rune(
        id = 15, name = "Algiz", symbol = "\u16C9", letters = "z",
        aett = RuneAett.Hagalaz,
        meaning = "Elk, protection, warding, and the raised hand that turns away harm. Algiz is the active shield.",
        keywords = listOf("protection", "ward", "sanctuary", "awareness"),
        reversed = "Vulnerability, neglected defenses, warning that protection has gaps or that paranoia is blocking growth.",
        bindruneUses = listOf(
            "Combine with Thurisaz for an aggressive defensive wall.",
            "Combine with Isa to freeze hostile intent.",
            "Carve on thresholds or draw on skin before entering dangerous situations."
        )
    ),
    Rune(
        id = 16, name = "Sowilo", symbol = "\u16CB", letters = "s",
        aett = RuneAett.Hagalaz,
        meaning = "Sun, victory, health, and the guiding light that carries one through. Sowilo is triumph after effort.",
        keywords = listOf("victory", "guidance", "vitality", "success"),
        reversed = "Sowilo has no reversed form; shadowed, it suggests false triumph, sunstroke, or overexposure.",
        bindruneUses = listOf(
            "Combine with Tiwaz for justice that publicly succeeds.",
            "Combine with Sowilo itself to magnify solar will.",
            "Carve on solar talismans or above a goal statement."
        )
    ),
    Rune(
        id = 17, name = "Tiwaz", symbol = "\u16CF", letters = "t, d",
        aett = RuneAett.Tyr,
        meaning = "Tiwaz's spear, honor, right action, and the sacrifices required for justice. It favors those who keep their word.",
        keywords = listOf("honor", "justice", "sacrifice", "discipline"),
        reversed = "Dishonor, misplaced sacrifice, legal defeat, promises broken, martyrdom without cause.",
        bindruneUses = listOf(
            "Combine with Sowilo for victorious integrity.",
            "Combine with Uruz for strength in a just fight.",
            "Carve on petitions, court papers, or oath-rings."
        )
    ),
    Rune(
        id = 18, name = "Berkano", symbol = "\u16D2", letters = "b, p",
        aett = RuneAett.Tyr,
        meaning = "Birch, growth, motherhood, hidden regeneration, and the gentle force that breaks stone. Berkano is becoming.",
        keywords = listOf("growth", "birth", "healing", "renewal"),
        reversed = "Stunted growth, family conflict, smothering care, barrenness of body or plan.",
        bindruneUses = listOf(
            "Combine with Fehu to grow resources.",
            "Combine with Laguz to heal emotional wounds.",
            "Use over seeds, children, creative beginnings."
        )
    ),
    Rune(
        id = 19, name = "Ehwaz", symbol = "\u16D6", letters = "e, m",
        aett = RuneAett.Tyr,
        meaning = "Horse, trustful transition, partnership between rider and ridden, and movement between worlds.",
        keywords = listOf("movement", "trust", "partnership", "passage"),
        reversed = "Distrust, betrayal, broken contracts, travel delayed, partnership turned sour.",
        bindruneUses = listOf(
            "Combine with Raidho for safe, swift travel.",
            "Combine with Ansuz to send trustworthy messages.",
            "Carve on vehicles or before spirit-journey work."
        )
    ),
    Rune(
        id = 20, name = "Mannaz", symbol = "\u16D7", letters = "m",
        aett = RuneAett.Tyr,
        meaning = "Humanity, the self and the collective, social intelligence, and the mirror of relationships.",
        keywords = listOf("community", "identity", "cooperation", "self"),
        reversed = "Isolation, arrogance, social failure, false face, inability to accept help.",
        bindruneUses = listOf(
            "Combine with Wunjo for happy teamwork.",
            "Combine with Gebo to formalize shared labor.",
            "Use in self-knowledge work or before public speaking."
        )
    ),
    Rune(
        id = 21, name = "Laguz", symbol = "\u16DA", letters = "l",
        aett = RuneAett.Tyr,
        meaning = "Water, ocean, intuition, the unconscious, and the flow that dissolves resistance. Laguz is emotion as force.",
        keywords = listOf("flow", "intuition", "dreams", "emotion"),
        reversed = "Overwhelm, addiction, irrational fear, being dragged by currents instead of surfing them.",
        bindruneUses = listOf(
            "Combine with Berkano to birth intuitive ideas.",
            "Combine with Isa to control emotional flooding.",
            "Mark ritual bowls, sea bags, or divination mirrors."
        )
    ),
    Rune(
        id = 22, name = "Ingwaz", symbol = "\u16DC", letters = "ng",
        aett = RuneAett.Tyr,
        meaning = "Seed, Ing, potential held in quiet, masculine generative force, and the necessary incubation before action.",
        keywords = listOf("potential", "fertility", "patience", "internal work"),
        reversed = "Ingwaz has no reversed form; a shadow reading warns of endless preparation with no release.",
        bindruneUses = listOf(
            "Combine with Jera for projects that need a full season to germinate.",
            "Combine with Fehu to seed future wealth.",
            "Carve on stored seed packets or project boxes."
        )
    ),
    Rune(
        id = 23, name = "Dagaz", symbol = "\u16DE", letters = "d, ð",
        aett = RuneAett.Tyr,
        meaning = "Day, breakthrough, awakening, balance of light and dark, and the moment of clarity that changes everything.",
        keywords = listOf("awakening", "breakthrough", "balance", "transformation"),
        reversed = "Dagaz has no reversed form; a dim reading signals missed dawn, imbalance, or disorientation from too sudden a change.",
        bindruneUses = listOf(
            "Combine with Sowilo for decisive victory.",
            "Combine with Ansuz for illumination and sudden insight.",
            "Use when starting a new chapter or ending insomnia."
        )
    ),
    Rune(
        id = 24, name = "Othala", symbol = "\u16DF", letters = "o, õ",
        aett = RuneAett.Tyr,
        meaning = "Inheritance, homeland, ancestral property, and the spiritual real estate one protects or must leave behind.",
        keywords = listOf("inheritance", "ancestry", "home", "legacy"),
        reversed = "Home conflict, loss of inheritance, displacement, ancestral debts, rejection of roots.",
        bindruneUses = listOf(
            "Combine with Berkano to protect and grow the family.",
            "Combine with Ehwaz to journey safely home.",
            "Carve on land-talismans or ancestor altars."
        )
    )
)

internal fun getRune(id: Int): Rune? = runes.find { it.id == id }?.withOverride(TarotApp.overrides.getRuneOverride(id))

internal fun runesWithOverrides(): List<Rune> {
    val overrides = TarotApp.overrides.getRuneOverrides()
    return runes.map { it.withOverride(overrides[it.id] ?: RuneOverride()) }
}

@Composable
internal fun RunesScreen(
    onRuneClick: (Int) -> Unit
) {
    val items = remember(TarotApp.overrides.revision) { runesWithOverrides() }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.id }) { rune ->
            RuneListItem(rune, onRuneClick)
        }
    }
}

@Composable
private fun RuneListItem(rune: Rune, onRuneClick: (Int) -> Unit) {
    val container = remember(rune.aett) { aettColor(rune.aett) }
    val accent = remember(rune.aett) { aettAccent(rune.aett) }
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRuneClick(rune.id) },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(containerColor = container)
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
                Text(
                    text = rune.symbol,
                    color = accent,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rune.letters.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent.copy(alpha = 0.9f)
                )
                Text(
                    text = rune.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = rune.meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuneDetailScreen(
    id: Int,
    onBack: () -> Unit
) {
    var rune by remember { mutableStateOf<Rune?>(null) }

    LaunchedEffect(id) {
        rune = getRune(id)
    }

    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(rune?.name ?: "Rune", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    rune?.let { current ->
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit rune")
                        }
                        if (showEditDialog) {
                            EditRuneDialog(
                                rune = current,
                                onDismiss = { showEditDialog = false },
                                onSave = { updated ->
                                    rune = updated
                                    showEditDialog = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        rune?.let { r ->
            val container = remember(r.aett) { aettColor(r.aett) }
            val accent = remember(r.aett) { aettAccent(r.aett) }
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
                        Text(
                            text = r.symbol,
                            color = Color.White,
                            fontSize = 96.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = r.aett.name + "'s Aett",
                            style = MaterialTheme.typography.labelLarge,
                            color = accent,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = r.name,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Latin: ${r.letters.uppercase()}",
                            style = MaterialTheme.typography.titleLarge,
                            color = accent.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                TextBlock(label = "Meaning", text = r.meaning, accent = accent)
                TextBlock(label = "Keywords", text = r.keywords.joinToString("  ·  "), accent = accent)
                if (r.reversed.isNotBlank()) {
                    TextBlock(label = "Reversed / Shadow", text = r.reversed, accent = accent)
                }
                BindruneBlock(bindruneUses = r.bindruneUses, accent = accent)

                Spacer(modifier = Modifier.size(24.dp))
            }
        } ?: Text(
            text = "Rune not found",
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

@Composable
private fun BindruneBlock(bindruneUses: List<String>, accent: Color) {
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
                text = "Bindrune Use".uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "A bindrune layers two or more runes into a single sigil to combine and focus their forces.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            bindruneUses.forEach { use ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyLarge,
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = use,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Text(
                text = "Tip: Let shared strokes overlap naturally; keep the primary intent rune dominant and read the finished sigil as a single statement.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditRuneDialog(
    rune: Rune,
    onDismiss: () -> Unit,
    onSave: (Rune) -> Unit
) {
    val base = runes.find { it.id == rune.id } ?: rune
    var name by remember(rune.id) { mutableStateOf(rune.name) }
    var letters by remember(rune.id) { mutableStateOf(rune.letters) }
    var meaning by remember(rune.id) { mutableStateOf(rune.meaning) }
    var keywords by remember(rune.id) { mutableStateOf(rune.keywords.joinToString(", ")) }
    var reversed by remember(rune.id) { mutableStateOf(rune.reversed) }
    var bindrune by remember(rune.id) { mutableStateOf(rune.bindruneUses.joinToString("\n")) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${rune.name}") },
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
                    value = letters,
                    onValueChange = { letters = it },
                    label = { Text("Latin letter(s)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = meaning,
                    onValueChange = { meaning = it },
                    label = { Text("Meaning") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = keywords,
                    onValueChange = { keywords = it },
                    label = { Text("Keywords (comma-separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reversed,
                    onValueChange = { reversed = it },
                    label = { Text("Reversed / shadow meaning") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = bindrune,
                    onValueChange = { bindrune = it },
                    label = { Text("Bindrune uses (one per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank() || letters.isBlank() || meaning.isBlank()) {
                        error = "Name, letters, and meaning are required."
                        return@TextButton
                    }
                    TarotApp.overrides.saveRuneOverride(
                        rune.id,
                        RuneOverride(
                            name = name.trim().takeIf { it != base.name },
                            letters = letters.trim().takeIf { it != base.letters },
                            meaning = meaning.trim().takeIf { it != base.meaning },
                            keywords = keywords.split(",").map { it.trim() }.filter { it.isNotBlank() }.takeIf { it != base.keywords },
                            reversed = reversed.trim().takeIf { it != base.reversed },
                            bindruneUses = bindrune.split("\n").map { it.trim() }.filter { it.isNotBlank() }.takeIf { it != base.bindruneUses }
                        )
                    )
                    onSave(rune.withOverride(TarotApp.overrides.getRuneOverride(rune.id)))
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
