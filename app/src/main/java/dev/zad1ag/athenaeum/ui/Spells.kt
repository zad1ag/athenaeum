package dev.zad1ag.athenaeum

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.zad1ag.athenaeum.ui.AppTabRow
import dev.zad1ag.athenaeum.ui.FloatingPillTabs
import dev.zad1ag.athenaeum.ui.SelectionModeBar

sealed class SpellFilter(val label: String) {
    object All : SpellFilter("All")
    object Cleansing : SpellFilter("Cleansing")
    object Protection : SpellFilter("Protection")
    object Divination : SpellFilter("Divination")
    object Curses : SpellFilter("Curses")
    object Summoning : SpellFilter("Summoning")
    object Healing : SpellFilter("Healing")
    object Binding : SpellFilter("Binding")
    object Glamour : SpellFilter("Glamour")
    object Love : SpellFilter("Love")
    object Prosperity : SpellFilter("Prosperity")
    object Luck : SpellFilter("Luck")
    object Banishment : SpellFilter("Banishment")
    object Dream : SpellFilter("Dream")
    object Weather : SpellFilter("Weather")
    object Blessing : SpellFilter("Blessing")
    object Elemental : SpellFilter("Elemental")
    object Necromancy : SpellFilter("Necromancy")
    object Other : SpellFilter("Other")
}

private val spellFilters = listOf(
    SpellFilter.All,
    SpellFilter.Cleansing,
    SpellFilter.Protection,
    SpellFilter.Divination,
    SpellFilter.Curses,
    SpellFilter.Summoning,
    SpellFilter.Healing,
    SpellFilter.Binding,
    SpellFilter.Glamour,
    SpellFilter.Love,
    SpellFilter.Prosperity,
    SpellFilter.Luck,
    SpellFilter.Banishment,
    SpellFilter.Dream,
    SpellFilter.Weather,
    SpellFilter.Blessing,
    SpellFilter.Elemental,
    SpellFilter.Necromancy,
    SpellFilter.Other
)

private fun filterToCategory(filter: SpellFilter): SpellCategory? = when (filter) {
    SpellFilter.Cleansing -> SpellCategory.cleansing
    SpellFilter.Protection -> SpellCategory.protection
    SpellFilter.Divination -> SpellCategory.divination
    SpellFilter.Curses -> SpellCategory.curses
    SpellFilter.Summoning -> SpellCategory.summoning
    SpellFilter.Healing -> SpellCategory.healing
    SpellFilter.Binding -> SpellCategory.binding
    SpellFilter.Glamour -> SpellCategory.glamour
    SpellFilter.Love -> SpellCategory.love
    SpellFilter.Prosperity -> SpellCategory.prosperity
    SpellFilter.Luck -> SpellCategory.luck
    SpellFilter.Banishment -> SpellCategory.banishment
    SpellFilter.Dream -> SpellCategory.dream
    SpellFilter.Weather -> SpellCategory.weather
    SpellFilter.Blessing -> SpellCategory.blessing
    SpellFilter.Elemental -> SpellCategory.elemental
    SpellFilter.Necromancy -> SpellCategory.necromancy
    SpellFilter.Other -> SpellCategory.other
    else -> null
}

private fun spellCategoryColor(category: SpellCategory): Color = when (category) {
    SpellCategory.cleansing -> Color(0xFF2A3D2A)
    SpellCategory.protection -> Color(0xFF1E2E3A)
    SpellCategory.divination -> Color(0xFF2E2A3D)
    SpellCategory.curses -> Color(0xFF3A1E1E)
    SpellCategory.summoning -> Color(0xFF3D2E1A)
    SpellCategory.healing -> Color(0xFF1E3A3A)
    SpellCategory.binding -> Color(0xFF2E2836)
    SpellCategory.glamour -> Color(0xFF3A1E3A)
    SpellCategory.love -> Color(0xFF4A1E2E)
    SpellCategory.prosperity -> Color(0xFF2E3D1E)
    SpellCategory.luck -> Color(0xFF2E3D3A)
    SpellCategory.banishment -> Color(0xFF161616)
    SpellCategory.dream -> Color(0xFF26223D)
    SpellCategory.weather -> Color(0xFF3D2E2A)
    SpellCategory.blessing -> Color(0xFF3D3A1E)
    SpellCategory.elemental -> Color(0xFF1E3D3D)
    SpellCategory.necromancy -> Color(0xFF2E1E2E)
    SpellCategory.other -> Color(0xFF3A3A3A)
}

private fun spellCategoryAccent(category: SpellCategory): Color = when (category) {
    SpellCategory.cleansing -> Color(0xFF9AE6B4)
    SpellCategory.protection -> Color(0xFFA8D0F0)
    SpellCategory.divination -> Color(0xFFC9B8F0)
    SpellCategory.curses -> Color(0xFFFF8F8F)
    SpellCategory.summoning -> Color(0xFFF0C89A)
    SpellCategory.healing -> Color(0xFF8FE0D8)
    SpellCategory.binding -> Color(0xFFB0ACC0)
    SpellCategory.glamour -> Color(0xFFF0A8E0)
    SpellCategory.love -> Color(0xFFFFA8C0)
    SpellCategory.prosperity -> Color(0xFFC8E6A0)
    SpellCategory.luck -> Color(0xFFA0E6D8)
    SpellCategory.banishment -> Color(0xFF999999)
    SpellCategory.dream -> Color(0xFFB8A8F0)
    SpellCategory.weather -> Color(0xFFF0C89A)
    SpellCategory.blessing -> Color(0xFFFFE8A0)
    SpellCategory.elemental -> Color(0xFF8FE0D8)
    SpellCategory.necromancy -> Color(0xFFD8A0C8)
    SpellCategory.other -> Color(0xFFB7B7B7)
}

private enum class SpellMode { Spells, Staves, Runes }

@Composable
private fun availableModes(): List<SpellMode> {
    val context = LocalContext.current
    val modes = mutableListOf<SpellMode>()
    if (FeatureSettings.isEnabled(context, FeatureSettings.Feature.SPELLS)) modes += SpellMode.Spells
    if (FeatureSettings.isEnabled(context, FeatureSettings.Feature.STAVES)) modes += SpellMode.Staves
    if (FeatureSettings.isEnabled(context, FeatureSettings.Feature.RUNES)) modes += SpellMode.Runes
    return modes
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SpellsScreen(
    onSpellClick: (Int) -> Unit,
    onStaveClick: (Int) -> Unit,
    onEntityClick: (Int) -> Unit,
    onRuneClick: (Int) -> Unit
) {
    val enabledModes = availableModes()
    var selectedFilter by rememberSaveable { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var mode by rememberSaveable {
        mutableStateOf(enabledModes.firstOrNull() ?: SpellMode.Spells)
    }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { spellFilters.size })
    var allSpells by remember { mutableStateOf(listOf<Spell>()) }
    var allStaves by remember { mutableStateOf(listOf<Stave>()) }
    var selectedStaveCategory by rememberSaveable { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val staveCategories by remember(allStaves) {
        val sorted = allStaves.map { it.category }.distinct().sorted()
        mutableStateOf(listOf("All") + sorted)
    }

    LaunchedEffect(Unit, refresh) {
        allSpells = TarotApp.core.getAllSpells()
        allStaves = TarotApp.core.getAllStaves()
    }

    LaunchedEffect(selectedFilter) {
        if (pagerState.currentPage != selectedFilter) {
            pagerState.animateScrollToPage(selectedFilter)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        if (!isSearching) {
            selectedFilter = pagerState.currentPage
        }
    }

    LaunchedEffect(enabledModes, mode) {
        if (enabledModes.isNotEmpty() && mode !in enabledModes) {
            mode = enabledModes.first()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (selectionMode) {
                SelectionModeBar(
                    selectedCount = selectedIds.size,
                    onCancel = {
                        selectionMode = false
                        selectedIds = emptySet()
                    },
                    onDelete = { showDeleteConfirm = true }
                )
            }
        },
        floatingActionButton = {
            if (!selectionMode) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isSearching) {
                        androidx.compose.material3.FloatingActionButton(
                            onClick = { isSearching = true },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                    if (mode == SpellMode.Spells) {
                        ExtendedFloatingActionButton(
                            onClick = { showAddDialog = true },
                            icon = { Icon(Icons.Default.Add, contentDescription = null) },
                            text = { Text("Add Spell") }
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column {
                ModeTabs(modes = enabledModes, mode = mode, onModeSelected = { mode = it })
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = when (mode) {
                    SpellMode.Spells -> "Spells"
                    SpellMode.Staves -> "Staves"
                    SpellMode.Runes -> "Runes"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            if (isSearching) {
                SearchHeader(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClose = {
                        isSearching = false
                        searchQuery = ""
                    }
                )
            }
            if (enabledModes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "All magic sections are disabled.\nEnable them again in Settings.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else when (mode) {
                SpellMode.Staves -> {
                    if (!isSearching) {
                        StaveCategoryTabs(
                            categories = staveCategories,
                            selected = selectedStaveCategory,
                            onSelected = { selectedStaveCategory = it }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }
                    val staves = if (isSearching) {
                        if (searchQuery.isBlank()) allStaves else allStaves.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                                    it.icelandicName.contains(searchQuery, ignoreCase = true) ||
                                    it.meaning.contains(searchQuery, ignoreCase = true) ||
                                    it.purpose.contains(searchQuery, ignoreCase = true)
                        }
                    } else {
                        val cat = staveCategories.getOrElse(selectedStaveCategory) { "All" }
                        if (cat == "All") allStaves else allStaves.filter { it.category == cat }
                    }
                    StavesList(
                        staves = staves,
                        onStaveClick = onStaveClick
                    )
                }
                SpellMode.Runes -> {
                    RunesScreen(onRuneClick = onRuneClick)
                }
                SpellMode.Spells -> {
                    if (!isSearching) {
                        CategoryTabs(
                            selectedFilter = selectedFilter,
                            onFilterSelected = { selectedFilter = it }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }
                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 4,
                        userScrollEnabled = !isSearching,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val category = filterToCategory(spellFilters[page])
                        val spells = if (isSearching) {
                            if (searchQuery.isBlank()) allSpells else allSpells.filter {
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                        it.purpose.contains(searchQuery, ignoreCase = true) ||
                                        it.ingredients.any { i -> i.contains(searchQuery, ignoreCase = true) }
                            }
                        } else {
                            if (category == null) allSpells else allSpells.filter { it.category == category }
                        }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(spells, key = { it.id }) { spell ->
                                val selected = spell.id in selectedIds
                                Box(
                                    modifier = Modifier.then(
                                        if (selected) Modifier.border(
                                            3.dp,
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.shapes.large
                                        ) else Modifier
                                    )
                                ) {
                                    SpellListItem(
                                        spell = spell,
                                        onSpellClick = onSpellClick,
                                        modifier = Modifier.combinedClickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                if (selectionMode) {
                                                    selectedIds = if (selected) selectedIds - spell.id else selectedIds + spell.id
                                                } else {
                                                    onSpellClick(spell.id)
                                                }
                                            },
                                            onLongClick = {
                                                selectionMode = true
                                                selectedIds = selectedIds + spell.id
                                            }
                                        )
                                    )
                                    if (selectionMode && selected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(10.dp)
                                                .size(28.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    if (showAddDialog) {
        AddSpellDialog(
            spell = null,
            onDismiss = { showAddDialog = false },
            onSave = {
                refresh++
                showAddDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete selected spells?") },
            text = { Text("${selectedIds.size} ${if (selectedIds.size == 1) "entry" else "entries"} will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        TarotApp.core.deleteSpells(selectedIds.toList())
                        selectionMode = false
                        selectedIds = emptySet()
                        showDeleteConfirm = false
                        refresh++
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
internal fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search spells...") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true
        )
        TextButton(onClick = onClose) {
            Text("Cancel")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeTabs(
    modes: List<SpellMode>,
    mode: SpellMode,
    onModeSelected: (SpellMode) -> Unit
) {
    if (modes.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppTabRow(
            tabs = modes.map { it.name },
            selectedIndex = modes.indexOf(mode).coerceAtLeast(0),
            onSelected = { onModeSelected(modes[it]) },
            modifier = Modifier.fillMaxWidth(),
            centered = true,
            compact = true
        )
    }
}

@Composable
private fun StaveCategoryTabs(
    categories: List<String>,
    selected: Int,
    onSelected: (Int) -> Unit
) {
    AppTabRow(
        tabs = categories,
        selectedIndex = selected,
        onSelected = onSelected
    )
}

@Composable
private fun CategoryTabs(
    selectedFilter: Int,
    onFilterSelected: (Int) -> Unit
) {
    AppTabRow(
        tabs = spellFilters.map { it.label },
        selectedIndex = selectedFilter,
        onSelected = onFilterSelected
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SpellListItem(
    spell: Spell,
    onSpellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val container = remember(spell.category) { spellCategoryColor(spell.category) }
    val accent = remember(spell.category) { spellCategoryAccent(spell.category) }
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(containerColor = container)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = spell.category.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelMedium,
                color = accent.copy(alpha = 0.9f)
            )
            Text(
                text = spell.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = spell.purpose,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellDetailScreen(
    id: Int,
    onBack: () -> Unit,
    onEntityClick: (Int) -> Unit
) {
    var spell by remember { mutableStateOf<Spell?>(null) }
    var relatedEntities by remember { mutableStateOf(mapOf<Int, Entity>()) }
    var refresh by remember { mutableIntStateOf(0) }

    LaunchedEffect(id, refresh) {
        spell = TarotApp.core.getSpell(id)
        spell?.let { s ->
            val map = mutableMapOf<Int, Entity>()
            s.relatedEntityIds.forEach { entityId ->
                TarotApp.core.getEntity(entityId)?.let { entity ->
                    map[entityId] = entity
                }
            }
            relatedEntities = map
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        spell?.name ?: "Spell",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var showEditDialog by remember { mutableStateOf(false) }
                    var showDeleteConfirm by remember { mutableStateOf(false) }
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit spell")
                    }
                    if (showEditDialog) {
                        AddSpellDialog(
                            spell = spell,
                            onDismiss = { showEditDialog = false },
                            onSave = {
                                refresh++
                                showEditDialog = false
                            }
                        )
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete spell")
                    }
                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("Delete spell?") },
                            text = { Text("This spell will be removed from your grimoire.") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        TarotApp.core.deleteSpell(id)
                                        showDeleteConfirm = false
                                        onBack()
                                    }
                                ) {
                                    Text("Delete", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        spell?.let { s ->
            val scroll = rememberScrollState()
            val container = remember(s.category) { spellCategoryColor(s.category) }
            val accent = remember(s.category) { spellCategoryAccent(s.category) }

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
                        Icon(
                            painter = painterResource(R.drawable.ic_spells),
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = Color.White
                        )
                        Text(
                            text = s.category.name.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelLarge,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = s.name,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (s.moonPhase != MoonPhaseRequirement.any) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clickable { }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(s.moonPhase.iconRes()),
                                contentDescription = s.moonPhase.toReadable(),
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = "MOON PHASE",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = s.moonPhase.toReadable(),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedCard(modifier = Modifier.weight(1f).padding(bottom = 16.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "DIFFICULTY",
                                style = MaterialTheme.typography.labelMedium,
                                color = accent,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${s.difficulty}/5",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    OutlinedCard(modifier = Modifier.weight(1f).padding(bottom = 16.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "RISK",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${s.riskRating}/5",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                TextBlock(label = "Purpose", text = s.purpose, accent = accent)
                ChecklistBlock(label = "Ingredients", items = s.ingredients, accent = accent)
                NumberedBlock(label = "Steps", items = s.steps, accent = accent)
                BulletBlock(label = "Warnings", items = s.warnings, accent = accent)

                if (relatedEntities.isNotEmpty()) {
                    RelatedEntitiesBlock(entities = relatedEntities, accent = accent, onEntityClick = onEntityClick)
                }

                TextBlock(label = "Source", text = s.sourceNote, accent = accent)

                Spacer(modifier = Modifier.size(24.dp))
            }
        } ?: Text(
            "Spell not found",
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun RelatedEntitiesBlock(
    entities: Map<Int, Entity>,
    accent: Color,
    onEntityClick: (Int) -> Unit
) {
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
                text = "Related Spirits".uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            entities.forEach { (entityId, entity) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEntityClick(entityId) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entity.entityType.name.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = entity.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Open",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            }
        }
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
private fun BulletBlock(label: String, items: List<String>, accent: Color) {
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
                modifier = Modifier.padding(bottom = 12.dp)
            )
            items.forEach { item ->
                Row(
                    modifier = Modifier.padding(bottom = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        color = accent,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = item.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun ChecklistBlock(label: String, items: List<String>, accent: Color) {
    val checked = remember(items) { mutableStateOf<Set<Int>>(emptySet()) }
    val allChecked = checked.value.size == items.size && items.isNotEmpty()

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = accent
                )
                TextButton(
                    onClick = { checked.value = emptySet() }
                ) {
                    Text("Reset")
                }
            }
            items.forEachIndexed { index, item ->
                val isChecked = index in checked.value
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            checked.value = if (isChecked) {
                                checked.value - index
                            } else {
                                checked.value + index
                            }
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = isChecked,
                        onCheckedChange = { newChecked ->
                            checked.value = if (newChecked) checked.value + index else checked.value - index
                        }
                    )
                    Text(
                        text = item.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            if (allChecked) {
                Text(
                    text = "All ingredients gathered",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun NumberedBlock(label: String, items: List<String>, accent: Color) {
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
                modifier = Modifier.padding(bottom = 12.dp)
            )
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.padding(bottom = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${index + 1}.",
                        color = accent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = item.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSpellDialog(
    spell: Spell?,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val isEdit = spell != null
    var name by remember { mutableStateOf(spell?.name ?: "") }
    var purpose by remember { mutableStateOf(spell?.purpose ?: "") }
    var category by remember { mutableStateOf(spell?.category ?: SpellCategory.cleansing) }
    var ingredients by remember { mutableStateOf(spell?.ingredients?.joinToString("\n") ?: "") }
    var steps by remember { mutableStateOf(spell?.steps?.joinToString("\n") ?: "") }
    var warnings by remember { mutableStateOf(spell?.warnings?.joinToString("\n") ?: "") }
    var sourceNote by remember { mutableStateOf(spell?.sourceNote ?: "") }
    var relatedEntityIds by remember { mutableStateOf(spell?.relatedEntityIds ?: emptyList()) }
    var difficulty by remember { mutableIntStateOf(spell?.difficulty ?: 1) }
    var riskRating by remember { mutableIntStateOf(spell?.riskRating ?: 1) }
    var moonPhase by remember { mutableStateOf(spell?.moonPhase ?: MoonPhaseRequirement.any) }
    var showEntityPicker by remember { mutableStateOf(false) }
    val allEntities by remember { mutableStateOf(TarotApp.core.getAllEntities()) }

    val categories = listOf(
        SpellCategory.cleansing,
        SpellCategory.protection,
        SpellCategory.divination,
        SpellCategory.curses,
        SpellCategory.summoning,
        SpellCategory.healing,
        SpellCategory.binding,
        SpellCategory.glamour,
        SpellCategory.love,
        SpellCategory.prosperity,
        SpellCategory.luck,
        SpellCategory.banishment,
        SpellCategory.dream,
        SpellCategory.weather,
        SpellCategory.blessing,
        SpellCategory.elemental,
        SpellCategory.necromancy,
        SpellCategory.other
    )

    if (showEntityPicker) {
        EntityPickerDialog(
            allEntities = allEntities,
            selectedIds = relatedEntityIds,
            onDismiss = { showEntityPicker = false },
            onConfirm = { ids ->
                relatedEntityIds = ids
                showEntityPicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Spell" else "Add Your Spell") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { c ->
                            val selected = c == category
                            val accent = spellCategoryAccent(c)
                            val container = spellCategoryColor(c)
                            TextButton(
                                onClick = { category = c },
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                    containerColor = if (selected) container else MaterialTheme.colorScheme.surface,
                                    contentColor = if (selected) accent else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    text = c.name.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Purpose") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Difficulty",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..5).forEach { n ->
                                val selected = n <= difficulty
                                TextButton(
                                    onClick = { difficulty = n },
                                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(n.toString(), fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Risk",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..5).forEach { n ->
                                val selected = n <= riskRating
                                TextButton(
                                    onClick = { riskRating = n },
                                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface,
                                        contentColor = if (selected) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(n.toString(), fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Moon phase",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = moonPhase.toReadable(),
                            onValueChange = {},
                            readOnly = true,
                            label = null,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            MoonPhaseRequirement.entries.forEach { phase ->
                                DropdownMenuItem(
                                    text = { Text(phase.toReadable()) },
                                    onClick = {
                                        moonPhase = phase
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(phase.iconRes()),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = ingredients,
                    onValueChange = { ingredients = it },
                    label = { Text("Ingredients (one per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
                OutlinedTextField(
                    value = steps,
                    onValueChange = { steps = it },
                    label = { Text("Steps (one per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )
                OutlinedTextField(
                    value = warnings,
                    onValueChange = { warnings = it },
                    label = { Text("Warnings (one per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                OutlinedTextField(
                    value = sourceNote,
                    onValueChange = { sourceNote = it },
                    label = { Text("Source / Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showEntityPicker = true }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Related Spirits",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (relatedEntityIds.isEmpty()) {
                            Text(
                                text = "Tap to link spirits...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            val names = allEntities
                                .filter { it.id in relatedEntityIds }
                                .joinToString(", ") { it.name }
                            Text(
                                text = names,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank() || purpose.isBlank()) return@TextButton
                    val newSpell = Spell(
                        id = spell?.id ?: 0,
                        name = name.trim(),
                        category = category,
                        purpose = purpose.trim(),
                        difficulty = difficulty,
                        riskRating = riskRating,
                        moonPhase = moonPhase,
                        ingredients = ingredients.lines().map { it.trim() }.filter { it.isNotBlank() },
                        steps = steps.lines().map { it.trim() }.filter { it.isNotBlank() },
                        warnings = warnings.lines().map { it.trim() }.filter { it.isNotBlank() },
                        relatedEntityIds = relatedEntityIds,
                        sourceNote = sourceNote.trim(),
                        isCustom = true
                    )
                    if (isEdit) {
                        TarotApp.core.updateSpell(newSpell.id, newSpell)
                    } else {
                        TarotApp.core.addSpell(newSpell)
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntityPickerDialog(
    allEntities: List<Entity>,
    selectedIds: List<Int>,
    onDismiss: () -> Unit,
    onConfirm: (List<Int>) -> Unit
) {
    val selected = remember { mutableStateOf(selectedIds.toMutableList()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Link Spirits") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allEntities, key = { it.id }) { entity ->
                    val checked = entity.id in selected.value
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (checked) {
                                    selected.value = selected.value.filter { it != entity.id }.toMutableList()
                                } else {
                                    selected.value = (selected.value + entity.id).toMutableList()
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    selected.value = (selected.value + entity.id).toMutableList()
                                } else {
                                    selected.value = selected.value.filter { it != entity.id }.toMutableList()
                                }
                            }
                        )
                        Text(
                            text = entity.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected.value.toList()) }) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
