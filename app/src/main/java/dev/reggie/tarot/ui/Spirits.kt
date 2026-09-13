package dev.reggie.tarot

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.reggie.tarot.ui.AppTabRow
import dev.reggie.tarot.ui.FloatingPillTabs
import dev.reggie.tarot.ui.SelectionModeBar

sealed class SpiritFilter(val label: String) {
    object All : SpiritFilter("All")
    object Ghosts : SpiritFilter("Ghosts")
    object Spirits : SpiritFilter("Spirits")
    object Demons : SpiritFilter("Demons")
    object Fae : SpiritFilter("Fae")
    object Shades : SpiritFilter("Shades")
    object Wraiths : SpiritFilter("Wraiths")
    object Poltergeists : SpiritFilter("Poltergeists")
    object Angels : SpiritFilter("Angels")
    object Other : SpiritFilter("Other")
}

private val spiritFilters = listOf(
    SpiritFilter.All,
    SpiritFilter.Ghosts,
    SpiritFilter.Spirits,
    SpiritFilter.Demons,
    SpiritFilter.Fae,
    SpiritFilter.Shades,
    SpiritFilter.Wraiths,
    SpiritFilter.Poltergeists,
    SpiritFilter.Angels,
    SpiritFilter.Other
)

fun EntityType.toFilter(): SpiritFilter? = when (this) {
    EntityType.ghost -> SpiritFilter.Ghosts
    EntityType.spirit -> SpiritFilter.Spirits
    EntityType.demon -> SpiritFilter.Demons
    EntityType.fae -> SpiritFilter.Fae
    EntityType.shade -> SpiritFilter.Shades
    EntityType.wraith -> SpiritFilter.Wraiths
    EntityType.poltergeist -> SpiritFilter.Poltergeists
    EntityType.angel -> SpiritFilter.Angels
    EntityType.other -> SpiritFilter.Other
}

private fun entityTypeColor(entityType: EntityType): Color = when (entityType) {
    EntityType.ghost -> Color(0xFF3A3A50)
    EntityType.spirit -> Color(0xFF2E4B5E)
    EntityType.demon -> Color(0xFF5A1E1E)
    EntityType.fae -> Color(0xFF2E5A3A)
    EntityType.shade -> Color(0xFF161616)
    EntityType.wraith -> Color(0xFF3B2E5A)
    EntityType.poltergeist -> Color(0xFF5A3A1E)
    EntityType.angel -> Color(0xFF5A4A2E)
    EntityType.other -> Color(0xFF3A3A3A)
}

private fun entityTypeAccent(entityType: EntityType): Color = when (entityType) {
    EntityType.ghost -> Color(0xFFB8B8D8)
    EntityType.spirit -> Color(0xFFA8D0F0)
    EntityType.demon -> Color(0xFFFF8F8F)
    EntityType.fae -> Color(0xFF9AE6B4)
    EntityType.shade -> Color(0xFF999999)
    EntityType.wraith -> Color(0xFFC9B8F0)
    EntityType.poltergeist -> Color(0xFFF0C89A)
    EntityType.angel -> Color(0xFFFFE8B0)
    EntityType.other -> Color(0xFFB7B7B7)
}

private enum class SpiritMode {
    Entities,
    Deities
}

@Composable
private fun availableSpiritModes(): List<SpiritMode> {
    val context = LocalContext.current
    val modes = mutableListOf<SpiritMode>()
    if (FeatureSettings.isEnabled(context, FeatureSettings.Feature.ENTITIES)) modes += SpiritMode.Entities
    if (FeatureSettings.isEnabled(context, FeatureSettings.Feature.DEITIES)) modes += SpiritMode.Deities
    return modes
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SpiritsScreen(
    onEntityClick: (Int) -> Unit,
    onDeityClick: (Int) -> Unit
) {
    val enabledModes = availableSpiritModes()
    var mode by rememberSaveable { mutableStateOf(SpiritMode.Entities) }
    LaunchedEffect(enabledModes, mode) {
        if (enabledModes.isNotEmpty() && mode !in enabledModes) {
            mode = enabledModes.first()
        }
    }
    var selectedFilter by rememberSaveable { mutableIntStateOf(0) }
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { spiritFilters.size })
    var allEntities by remember { mutableStateOf(listOf<Entity>()) }
    val entitiesCache = remember { mutableStateMapOf<Int, List<Entity>>() }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit, refresh) {
        allEntities = TarotApp.core.getAllEntities().sortedBy { it.name }
    }

    val displayedEntities = remember(allEntities, selectedFilter, searchQuery, refresh, isSearching) {
        if (isSearching && searchQuery.isNotBlank()) {
            TarotApp.core.searchEntities(searchQuery)
        } else {
            val filter = spiritFilters[selectedFilter]
            if (filter == SpiritFilter.All) allEntities else allEntities.filter { it.entityType == filterToEntityType(filter) }
        }
    }

    LaunchedEffect(selectedFilter) {
        if (pagerState.currentPage != selectedFilter) {
            pagerState.animateScrollToPage(selectedFilter)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        selectedFilter = pagerState.currentPage
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
            if (mode == SpiritMode.Entities && !selectionMode) {
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
                    ExtendedFloatingActionButton(
                        onClick = { showAddDialog = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Add Spirit") }
                    )
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
            // Section headline switches Entities / Deities.
            Text(
                text = when (mode) {
                    SpiritMode.Entities -> "Entities"
                    SpiritMode.Deities -> "Deities"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            if (enabledModes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "All spirit sections are disabled.\nEnable them again in Settings.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else when (mode) {
                SpiritMode.Entities -> {
                    if (isSearching) {
                        SearchHeader(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onClose = {
                                isSearching = false
                                searchQuery = ""
                            }
                        )
                    } else {
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
                        val filter = spiritFilters[page]
                        val entities = if (isSearching) {
                            if (searchQuery.isBlank()) allEntities else TarotApp.core.searchEntities(searchQuery)
                        } else {
                            if (filter == SpiritFilter.All) allEntities else allEntities.filter { it.entityType == filterToEntityType(filter) }
                        }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(entities, key = { it.id }) { entity ->
                                val selected = entity.id in selectedIds
                                Box(
                                    modifier = Modifier
                                        .animateItemPlacement()
                                        .then(
                                            if (selected) Modifier.border(
                                                3.dp,
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.shapes.large
                                            ) else Modifier
                                        )
                                ) {
                                    EntityListItem(
                                        entity = entity,
                                        onEntityClick = {
                                            if (selectionMode) {
                                                selectedIds = if (selected) selectedIds - entity.id else selectedIds + entity.id
                                            } else {
                                                onEntityClick(entity.id)
                                            }
                                        },
                                        modifier = Modifier.combinedClickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                if (selectionMode) {
                                                    selectedIds = if (selected) selectedIds - entity.id else selectedIds + entity.id
                                                } else {
                                                    onEntityClick(entity.id)
                                                }
                                            },
                                            onLongClick = {
                                                selectionMode = true
                                                selectedIds = selectedIds + entity.id
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
                SpiritMode.Deities -> {
                    Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                        DeitiesScreen(
                            onDeityClick = onDeityClick,
                            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEntityDialog(
            entity = null,
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
            title = { Text("Delete selected spirits?") },
            text = { Text("${selectedIds.size} ${if (selectedIds.size == 1) "entry" else "entries"} will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        TarotApp.core.deleteEntities(selectedIds.toList())
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
private fun ModeTabs(
    modes: List<SpiritMode>,
    mode: SpiritMode,
    onModeSelected: (SpiritMode) -> Unit
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
private fun CategoryTabs(
    selectedFilter: Int,
    onFilterSelected: (Int) -> Unit
) {
    AppTabRow(
        tabs = spiritFilters.map { it.label },
        selectedIndex = selectedFilter,
        onSelected = onFilterSelected
    )
}

private fun filterToEntityType(filter: SpiritFilter): EntityType = when (filter) {
    SpiritFilter.Ghosts -> EntityType.ghost
    SpiritFilter.Spirits -> EntityType.spirit
    SpiritFilter.Demons -> EntityType.demon
    SpiritFilter.Fae -> EntityType.fae
    SpiritFilter.Shades -> EntityType.shade
    SpiritFilter.Wraiths -> EntityType.wraith
    SpiritFilter.Poltergeists -> EntityType.poltergeist
    SpiritFilter.Angels -> EntityType.angel
    SpiritFilter.Other -> EntityType.other
    else -> EntityType.spirit
}

private fun entityIconResource(entityType: EntityType): Int = when (entityType) {
    EntityType.ghost -> R.drawable.ic_entity_ghost
    EntityType.spirit -> R.drawable.ic_entity_spirit
    EntityType.demon -> R.drawable.ic_entity_demon
    EntityType.fae -> R.drawable.ic_entity_fae
    EntityType.shade -> R.drawable.ic_entity_shade
    EntityType.wraith -> R.drawable.ic_entity_wraith
    EntityType.poltergeist -> R.drawable.ic_entity_poltergeist
    EntityType.angel -> R.drawable.ic_entity_angel
    EntityType.other -> R.drawable.ic_entity_other
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EntityListItem(
    entity: Entity,
    onEntityClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val container = remember(entity.entityType) { entityTypeColor(entity.entityType) }
    val accent = remember(entity.entityType) { entityTypeAccent(entity.entityType) }
    val tone = remember(entity.tone) { toneColors(entity.tone.toTone()) }
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth(),
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
            Icon(
                painter = painterResource(entityIconResource(entity.entityType)),
                contentDescription = entity.entityType.name,
                modifier = Modifier.size(72.dp),
                tint = accent
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entity.entityType.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = accent.copy(alpha = 0.9f)
                )
                Text(
                    text = entity.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (entity.origin.isNotBlank()) {
                    Text(
                        text = entity.origin,
                        style = MaterialTheme.typography.titleMedium,
                        color = accent.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = entity.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2
                )
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(tone.container)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = toneLabel(entity.tone.toTone()),
                        style = MaterialTheme.typography.labelSmall,
                        color = tone.text
                    )
                }
            }
        }
    }
}

private fun toneLabel(tone: Tone): String = when (tone) {
    Tone.positive -> "Good"
    Tone.negative -> "Evil"
    Tone.neutral -> "Neutral"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntityDetailScreen(id: Int, onBack: () -> Unit) {
    var entity by remember { mutableStateOf<Entity?>(null) }

    LaunchedEffect(id) {
        entity = TarotApp.core.getEntity(id)
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        entity?.name ?: "Entity",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit spirit")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete spirit")
                    }
                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("Delete spirit?") },
                            text = { Text("This spirit will be removed from your bestiary.") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        TarotApp.core.deleteEntity(id)
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
        if (showEditDialog) {
            AddEntityDialog(
                entity = entity,
                onDismiss = { showEditDialog = false },
                onSave = {
                    entity = TarotApp.core.getEntity(id)
                    showEditDialog = false
                }
            )
        }

        entity?.let { e ->
            val scroll = rememberScrollState()
            val container = entityTypeColor(e.entityType)
            val accent = entityTypeAccent(e.entityType)

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
                            painter = painterResource(entityIconResource(e.entityType)),
                            contentDescription = e.entityType.name,
                            modifier = Modifier.size(96.dp),
                            tint = Color.White
                        )
                        Text(
                            text = e.entityType.name.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelLarge,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                                Text(
                                    text = e.name,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                val detailTone = remember(e.tone) { toneColors(e.tone.toTone()) }
                                Box(
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.small)
                                        .background(detailTone.container)
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = toneLabel(e.tone.toTone()),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = detailTone.text
                                    )
                                }
                                Text(
                                    text = e.danger,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                    }
                }

                TextBlock(label = "Origin", text = e.origin, accent = accent)
                TextBlock(label = "Description", text = e.description, accent = accent)
                BulletBlock(label = "Signs", items = e.signs, accent = accent)
                BulletBlock(label = "Weaknesses", items = e.weaknesses, accent = accent)
                TextBlock(label = "Banishment", text = e.banishment, accent = accent)

                Spacer(modifier = Modifier.size(24.dp))
            }
        } ?: Text(
            "Entity not found",
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.error
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEntityDialog(
    entity: Entity?,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val isEdit = entity != null
    var name by remember { mutableStateOf(entity?.name ?: "") }
    var entityType by remember { mutableStateOf(entity?.entityType ?: EntityType.spirit) }
    var origin by remember { mutableStateOf(entity?.origin ?: "") }
    var description by remember { mutableStateOf(entity?.description ?: "") }
    var signs by remember { mutableStateOf(entity?.signs?.joinToString("\n") ?: "") }
    var weaknesses by remember { mutableStateOf(entity?.weaknesses?.joinToString("\n") ?: "") }
    var banishment by remember { mutableStateOf(entity?.banishment ?: "") }
    var danger by remember { mutableStateOf(entity?.danger ?: "") }
    var tone by remember { mutableStateOf(entity?.tone?.toTone() ?: Tone.neutral) }

    val toneOptions = listOf(Tone.positive, Tone.neutral, Tone.negative)

    val types = listOf(
        EntityType.ghost,
        EntityType.spirit,
        EntityType.demon,
        EntityType.fae,
        EntityType.shade,
        EntityType.wraith,
        EntityType.poltergeist,
        EntityType.angel,
        EntityType.other
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Spirit" else "Add Spirit") },
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
                        text = "Type",
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
                        types.forEach { t ->
                            val selected = t == entityType
                            val accent = entityTypeAccent(t)
                            val container = entityTypeColor(t)
                            TextButton(
                                onClick = { entityType = t },
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                    containerColor = if (selected) container else MaterialTheme.colorScheme.surface,
                                    contentColor = if (selected) accent else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    text = t.name.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = origin,
                    onValueChange = { origin = it },
                    label = { Text("Origin") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
                OutlinedTextField(
                    value = signs,
                    onValueChange = { signs = it },
                    label = { Text("Signs (one per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
                OutlinedTextField(
                    value = weaknesses,
                    onValueChange = { weaknesses = it },
                    label = { Text("Weaknesses (one per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
                OutlinedTextField(
                    value = banishment,
                    onValueChange = { banishment = it },
                    label = { Text("Banishment") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                OutlinedTextField(
                    value = danger,
                    onValueChange = { danger = it },
                    label = { Text("Danger Level") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Disposition",
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
                        toneOptions.forEach { t ->
                            val selected = t == tone
                            val tc = toneColors(t)
                            TextButton(
                                onClick = { tone = t },
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                    containerColor = if (selected) tc.container else MaterialTheme.colorScheme.surface,
                                    contentColor = if (selected) tc.text else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    text = toneLabel(t),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank() || description.isBlank()) return@TextButton
                    val newEntity = Entity(
                        id = entity?.id ?: 0,
                        name = name.trim(),
                        entityType = entityType,
                        origin = origin.trim(),
                        description = description.trim(),
                        signs = signs.lines().map { it.trim() }.filter { it.isNotBlank() },
                        weaknesses = weaknesses.lines().map { it.trim() }.filter { it.isNotBlank() },
                        banishment = banishment.trim(),
                        danger = danger.trim(),
                        tone = when (tone) {
                            Tone.positive -> "positive"
                            Tone.negative -> "negative"
                            Tone.neutral -> "neutral"
                        },
                        isCustom = true
                    )
                    if (isEdit) {
                        TarotApp.core.updateEntity(newEntity.id, newEntity)
                    } else {
                        TarotApp.core.addEntity(newEntity)
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
