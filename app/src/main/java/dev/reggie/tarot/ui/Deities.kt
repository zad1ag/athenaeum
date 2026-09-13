package dev.reggie.tarot

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.reggie.tarot.ui.AppTabRow
import dev.reggie.tarot.ui.SelectionModeBar

sealed class ReligionFilter(val label: String) {
    object All : ReligionFilter("All")
    object Norse : ReligionFilter("Norse")
    object Greek : ReligionFilter("Greek")
    object Egyptian : ReligionFilter("Egyptian")
    object Abrahamic : ReligionFilter("Abrahamic")
    object Christianity : ReligionFilter("Christianity")
    object Hindu : ReligionFilter("Hindu")
    object Shinto : ReligionFilter("Shinto")
    object Celtic : ReligionFilter("Celtic")
    object Sumerian : ReligionFilter("Sumerian")
    object Satanism : ReligionFilter("Satanism")
    object Mesoamerican : ReligionFilter("Mesoamerican")
    object Other : ReligionFilter("Other")
}

private val religionFilters = listOf(
    ReligionFilter.All,
    ReligionFilter.Norse,
    ReligionFilter.Greek,
    ReligionFilter.Egyptian,
    ReligionFilter.Abrahamic,
    ReligionFilter.Christianity,
    ReligionFilter.Hindu,
    ReligionFilter.Shinto,
    ReligionFilter.Celtic,
    ReligionFilter.Sumerian,
    ReligionFilter.Satanism,
    ReligionFilter.Mesoamerican,
    ReligionFilter.Other
)

private fun religionColor(religion: String): Color {
    val known = knownReligionList.firstOrNull { religion.contains(it) }
    return when (known) {
        "Norse" -> Color(0xFF2A3D2A)
        "Greek" -> Color(0xFF2E4B5E)
        "Egyptian" -> Color(0xFF5A3A1E)
        "Abrahamic" -> Color(0xFF3A3A50)
        "Christianity" -> Color(0xFF4A3B2A)
        "Hindu" -> Color(0xFF5A1E3A)
        "Shinto" -> Color(0xFF1E3A3A)
        "Celtic" -> Color(0xFF2E2836)
        "Sumerian/Mesopotamian" -> Color(0xFF4A331B)
        "Satanism" -> Color(0xFF3A1E1E)
        "Mesoamerican" -> Color(0xFF3D2E1A)
        "Other" -> Color(0xFF3A3A3A)
        else -> Color(0xFF2E2E2E)
    }
}

private fun religionAccent(religion: String): Color {
    val known = knownReligionList.firstOrNull { religion.contains(it) }
    return when (known) {
        "Norse" -> Color(0xFF9AE6B4)
        "Greek" -> Color(0xFFA8D0F0)
        "Egyptian" -> Color(0xFFF0C89A)
        "Abrahamic" -> Color(0xFFB8B8D8)
        "Christianity" -> Color(0xFFE8D0A8)
        "Hindu" -> Color(0xFFFFA8D0)
        "Shinto" -> Color(0xFF8FE0D8)
        "Celtic" -> Color(0xFFB0ACC0)
        "Sumerian/Mesopotamian" -> Color(0xFFD8C8A0)
        "Satanism" -> Color(0xFFFF8F8F)
        "Mesoamerican" -> Color(0xFFF0C89A)
        "Other" -> Color(0xFFB7B7B7)
        else -> Color(0xFFB7B7B7)
    }
}

private val knownReligionList = listOf(
    "Norse", "Greek", "Egyptian", "Abrahamic", "Christianity", "Hindu",
    "Shinto", "Celtic", "Sumerian/Mesopotamian", "Satanism", "Mesoamerican", "Other"
)

private fun filterToReligion(filter: ReligionFilter): String? = when (filter) {
    ReligionFilter.Norse -> "Norse"
    ReligionFilter.Greek -> "Greek"
    ReligionFilter.Egyptian -> "Egyptian"
    ReligionFilter.Abrahamic -> "Abrahamic"
    ReligionFilter.Christianity -> "Christianity"
    ReligionFilter.Hindu -> "Hindu"
    ReligionFilter.Shinto -> "Shinto"
    ReligionFilter.Celtic -> "Celtic"
    ReligionFilter.Sumerian -> "Sumerian/Mesopotamian"
    ReligionFilter.Satanism -> "Satanism"
    ReligionFilter.Mesoamerican -> "Mesoamerican"
    ReligionFilter.Other -> "Other"
    else -> null
}

private val alignments = listOf(
    "Lawful Good", "Neutral Good", "Chaotic Good",
    "Lawful Neutral", "True Neutral", "Chaotic Neutral",
    "Lawful Evil", "Neutral Evil", "Chaotic Evil"
)

private fun alignmentColor(alignment: String): Color = when (alignment.takeLast(4)) {
    "Good" -> Color(0xFF2A3D2A)
    "Evil" -> Color(0xFF3A1E1E)
    else -> Color(0xFF3A3A3A)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DeitiesScreen(
    onDeityClick: (Int) -> Unit,
    contentWindowInsets: androidx.compose.foundation.layout.WindowInsets = ScaffoldDefaults.contentWindowInsets
) {
    var selectedFilter by rememberSaveable { mutableIntStateOf(0) }
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { religionFilters.size })
    var showAddDialog by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    var allDeities by remember { mutableStateOf(listOf<Deity>()) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit, refresh) {
        allDeities = TarotApp.core.getAllDeities()
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

    val displayedDeities = remember(allDeities, selectedFilter, searchQuery, refresh, isSearching) {
        if (isSearching && searchQuery.isNotBlank()) {
            TarotApp.core.searchDeities(searchQuery)
        } else {
            val religion = filterToReligion(religionFilters[selectedFilter])
            if (religion == null) allDeities else allDeities.filter { it.primaryReligion.contains(religion) }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = contentWindowInsets,
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
                    ExtendedFloatingActionButton(
                        onClick = { showAddDialog = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Add Deity") }
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                ReligionTabs(
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
                val religion = filterToReligion(religionFilters[page])
                val deities = if (isSearching) {
                    if (searchQuery.isBlank()) allDeities else TarotApp.core.searchDeities(searchQuery)
                } else {
                    if (religion == null) allDeities else allDeities.filter { it.primaryReligion.contains(religion) }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(deities, key = { it.id }) { deity ->
                        val selected = deity.id in selectedIds
                        Box(
                            modifier = Modifier.then(
                                if (selected) Modifier.border(
                                    3.dp,
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.shapes.large
                                ) else Modifier
                            )
                        ) {
                            DeityListItem(
                                deity = deity,
                                onDeityClick = onDeityClick,
                                modifier = Modifier.combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        if (selectionMode) {
                                            selectedIds = if (selected) selectedIds - deity.id else selectedIds + deity.id
                                        } else {
                                            onDeityClick(deity.id)
                                        }
                                    },
                                    onLongClick = {
                                        selectionMode = true
                                        selectedIds = selectedIds + deity.id
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

    if (showAddDialog) {
        AddDeityDialog(
            deity = null,
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
            title = { Text("Delete selected deities?") },
            text = { Text("${selectedIds.size} ${if (selectedIds.size == 1) "entry" else "entries"} will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        TarotApp.core.deleteDeities(selectedIds.toList())
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
private fun ReligionTabs(
    selectedFilter: Int,
    onFilterSelected: (Int) -> Unit
) {
    AppTabRow(
        tabs = religionFilters.map { it.label },
        selectedIndex = selectedFilter,
        onSelected = onFilterSelected
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DeityListItem(
    deity: Deity,
    onDeityClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val container = remember(deity.primaryReligion) { religionColor(deity.primaryReligion) }
    val accent = remember(deity.primaryReligion) { religionAccent(deity.primaryReligion) }
    val alignmentContainer = remember(deity.alignment) { alignmentColor(deity.alignment) }
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(containerColor = container)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = deity.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                OutlinedCard(
                    shape = MaterialTheme.shapes.small,
                    colors = CardDefaults.outlinedCardColors(containerColor = alignmentContainer)
                ) {
                    Text(
                        text = deity.alignment,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Text(
                text = deity.primaryReligion,
                style = MaterialTheme.typography.labelMedium,
                color = accent.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = deity.domains.joinToString(", ") { it.replaceFirstChar { c -> c.uppercase() } },
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
fun DeityDetailScreen(id: Int, onBack: () -> Unit) {
    var deity by remember { mutableStateOf<Deity?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    var refreshOfferings by remember { mutableIntStateOf(0) }

    LaunchedEffect(id, refresh) {
        deity = TarotApp.core.getDeity(id)
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        deity?.name ?: "Deity",
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
                        Icon(Icons.Default.Edit, contentDescription = "Edit deity")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete deity")
                    }
                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("Delete deity?") },
                            text = { Text("This deity will be removed from your pantheon.") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        TarotApp.core.deleteDeity(id)
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
            AddDeityDialog(
                deity = deity,
                onDismiss = { showEditDialog = false },
                onSave = {
                    deity = TarotApp.core.getDeity(id)
                    showEditDialog = false
                }
            )
        }

        deity?.let { d ->
            val scroll = rememberScrollState()
            val container = remember(d.primaryReligion) { religionColor(d.primaryReligion) }
            val accent = remember(d.primaryReligion) { religionAccent(d.primaryReligion) }
            val alignmentContainer = remember(d.alignment) { alignmentColor(d.alignment) }

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
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = Color.White
                        )
                        Text(
                            text = d.primaryReligion,
                            style = MaterialTheme.typography.labelLarge,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = d.name,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = d.alignment,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                TextBlock(label = "Domains", text = d.domains.joinToString(", ") { it.replaceFirstChar { c -> c.uppercase() } }, accent = accent)
                TextBlock(label = "Overview", text = d.description, accent = accent)

                val wikiTitle = d.wikiTitle?.takeIf { it.isNotBlank() } ?: d.name
                WikipediaButton("https://en.wikipedia.org/wiki/" + java.net.URLEncoder.encode(wikiTitle, "UTF-8").replace("+", "_"))

                OfferingsSection(d, accent) { refreshOfferings++ }

                if (d.versions.isNotEmpty()) {
                    Text(
                        text = "TRADITIONS & VERSIONS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = accent,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    d.versions.forEach { version ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = version.tradition + if (version.nameVariant.isNotBlank() && version.nameVariant != d.name) " — ${version.nameVariant}" else "",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = version.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                if (version.domains.isNotEmpty()) {
                                    Text(
                                        text = version.domains.joinToString(", ") { it.replaceFirstChar { c -> c.uppercase() } },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.size(24.dp))
            }
        } ?: Text(
            "Deity not found",
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.error
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDeityDialog(
    deity: Deity?,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val isEdit = deity != null
    var name by remember { mutableStateOf(deity?.name ?: "") }
    var wikiTitle by remember { mutableStateOf(deity?.wikiTitle ?: "") }
    var alignment by remember { mutableStateOf(deity?.alignment ?: alignments[4]) }
    var domains by remember { mutableStateOf(deity?.domains?.joinToString(", ") ?: "") }
    var description by remember { mutableStateOf(deity?.description ?: "") }
    var versions by remember { mutableStateOf(deity?.versions ?: emptyList()) }
    var editingVersion by remember { mutableStateOf<TraditionVersion?>(null) }
    var editingVersionIndex by remember { mutableIntStateOf(-1) }

    val knownReligions = listOf(
        "Norse", "Greek", "Egyptian", "Abrahamic", "Christianity",
        "Hindu", "Shinto", "Celtic", "Sumerian/Mesopotamian", "Satanism",
        "Mesoamerican", "Other"
    )
    val storedReligion = deity?.primaryReligion ?: ""
    val initialReligions = remember(storedReligion) {
        val known = knownReligions.filter { storedReligion.contains(it) }
        if (known.isEmpty()) setOf("Other") else known.toSet()
    }
    var selectedReligions by remember { mutableStateOf(initialReligions) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Deity" else "Add Deity") },
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
                        text = "Primary religion (select all that apply)",
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
                        knownReligions.forEach { r ->
                            val selected = r in selectedReligions
                            val accent = religionAccent(r)
                            val container = religionColor(r)
                            TextButton(
                                onClick = {
                                    selectedReligions = if (selected) {
                                        val next = selectedReligions - r
                                        if (next.isEmpty()) setOf("Other") else next
                                    } else {
                                        selectedReligions + r
                                    }
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (selected) container else MaterialTheme.colorScheme.surface,
                                    contentColor = if (selected) accent else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    text = r,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = alignment,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Alignment") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        alignments.forEach { a ->
                            DropdownMenuItem(
                                text = { Text(a) },
                                onClick = {
                                    alignment = a
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = domains,
                    onValueChange = { domains = it },
                    label = { Text("Domains (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                OutlinedTextField(
                    value = wikiTitle,
                    onValueChange = { wikiTitle = it },
                    label = { Text("Wikipedia article title (optional)") },
                    placeholder = { Text("e.g. \"Maat\" for Ma'at") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )

                Text(
                    text = "TRADITIONS & VERSIONS",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                versions.forEachIndexed { index, version ->
                    VersionListItem(
                        version = version,
                        onEdit = {
                            editingVersionIndex = index
                            editingVersion = version
                        },
                        onDelete = { versions = versions.toMutableList().apply { removeAt(index) } }
                    )
                }

                TextButton(
                    onClick = {
                        editingVersionIndex = -1
                        editingVersion = TraditionVersion("", "", "", emptyList())
                    },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Add tradition / version")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) return@TextButton
                    val finalReligion = selectedReligions.joinToString(", ")
                    val newDeity = Deity(
                        id = deity?.id ?: 0,
                        name = name.trim(),
                        wikiTitle = wikiTitle.trim().takeIf { it.isNotBlank() },
                        primaryReligion = finalReligion,
                        alignment = alignment,
                        domains = domains.split(",").map { it.trim() }.filter { it.isNotBlank() },
                        description = description.trim(),
                        versions = versions,
                        isCustom = true
                    )
                    if (isEdit) {
                        TarotApp.core.updateDeity(newDeity.id, newDeity)
                    } else {
                        TarotApp.core.addDeity(newDeity)
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

    editingVersion?.let { version ->
        EditVersionDialog(
            version = version,
            onDismiss = {
                editingVersion = null
                editingVersionIndex = -1
            },
            onSave = { updated ->
                versions = if (editingVersionIndex >= 0) {
                    versions.toMutableList().apply { set(editingVersionIndex, updated) }
                } else {
                    versions + updated
                }
                editingVersion = null
                editingVersionIndex = -1
            }
        )
    }
}

@Composable
private fun VersionListItem(
    version: TraditionVersion,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onEdit() },
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = version.tradition,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (version.nameVariant.isNotBlank()) {
                    Text(
                        text = "Variant: ${version.nameVariant}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (version.domains.isNotEmpty()) {
                    Text(
                        text = version.domains.joinToString(", ") { it.replaceFirstChar { c -> c.uppercase() } },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove version",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditVersionDialog(
    version: TraditionVersion,
    onDismiss: () -> Unit,
    onSave: (TraditionVersion) -> Unit
) {
    var tradition by remember { mutableStateOf(version.tradition) }
    var nameVariant by remember { mutableStateOf(version.nameVariant) }
    var description by remember { mutableStateOf(version.description) }
    var domains by remember { mutableStateOf(version.domains.joinToString(", ")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Tradition") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = tradition,
                    onValueChange = { tradition = it },
                    label = { Text("Tradition / source") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = nameVariant,
                    onValueChange = { nameVariant = it },
                    label = { Text("Name variant (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
                OutlinedTextField(
                    value = domains,
                    onValueChange = { domains = it },
                    label = { Text("Domains (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (tradition.isBlank()) return@TextButton
                    onSave(
                        TraditionVersion(
                            tradition = tradition.trim(),
                            nameVariant = nameVariant.trim(),
                            description = description.trim(),
                            domains = domains.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        )
                    )
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
private fun OfferingsSection(deity: Deity, accent: Color, onChanged: () -> Unit) {
    var offerings by remember(deity.id) { mutableStateOf(listOf<Offering>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Offering?>(null) }
    var deleting by remember { mutableStateOf<Offering?>(null) }

    LaunchedEffect(deity.id, onChanged) {
        offerings = TarotApp.core.getOfferingsByDeity(deity.id)
    }

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
                BadgedBox(badge = {
                    if (offerings.isNotEmpty()) {
                        Badge { Text(offerings.size.toString()) }
                    }
                }) {
                    Text(
                        text = "OFFERINGS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = accent
                    )
                }
                TextButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Add offering")
                }
            }
            if (offerings.isEmpty()) {
                Text(
                    text = "No offerings recorded for ${deity.name} yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.size(8.dp))
                offerings.forEach { offering ->
                    OfferingCard(
                        offering = offering,
                        accent = accent,
                        onEdit = { editing = offering },
                        onDelete = { deleting = offering }
                    )
                    if (offering != offerings.last()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        EditOfferingDialog(
            offering = null,
            deityId = deity.id,
            religion = deity.primaryReligion,
            onDismiss = { showAddDialog = false },
            onSave = {
                offerings = TarotApp.core.getOfferingsByDeity(deity.id)
                onChanged()
                showAddDialog = false
            }
        )
    }
    editing?.
    let { o ->
        EditOfferingDialog(
            offering = o,
            deityId = o.deityId,
            religion = o.religion,
            onDismiss = { editing = null },
            onSave = {
                offerings = TarotApp.core.getOfferingsByDeity(deity.id)
                onChanged()
                editing = null
            }
        )
    }
    deleting?.
    let { o ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete offering?") },
            text = { Text("\"${o.name}\" will be removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        TarotApp.core.deleteOffering(o.id)
                        offerings = TarotApp.core.getOfferingsByDeity(deity.id)
                        onChanged()
                        deleting = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun OfferingCard(offering: Offering, accent: Color, onEdit: () -> Unit, onDelete: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = offering.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = offering.purpose,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit offering", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete offering", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
        if (offering.items.isNotEmpty()) {
            Text(
                text = "Items: ${offering.items.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = accent,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (offering.moonPhase != MoonPhaseRequirement.any) {
            Text(
                text = "Best moon: ${offering.moonPhase.name.replace("_", " ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        if (offering.warnings.isNotEmpty()) {
            Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                Text(
                    text = offering.warnings.joinToString(" "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditOfferingDialog(
    offering: Offering?,
    deityId: Int,
    religion: String,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val isEdit = offering != null
    var name by remember { mutableStateOf(offering?.name ?: "") }
    var items by remember { mutableStateOf(offering?.items?.joinToString(", ") ?: "") }
    var purpose by remember { mutableStateOf(offering?.purpose ?: "") }
    var instructions by remember { mutableStateOf(offering?.instructions ?: "") }
    var bestTime by remember { mutableStateOf(offering?.bestTime ?: "") }
    var warnings by remember { mutableStateOf(offering?.warnings?.joinToString(", ") ?: "") }
    var sourceNote by remember { mutableStateOf(offering?.sourceNote ?: "") }
    var selectedPhase by remember { mutableStateOf(offering?.moonPhase ?: MoonPhaseRequirement.any) }
    var expanded by remember { mutableStateOf(false) }
    val phases = MoonPhaseRequirement.values()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Offering" else "Add Offering") },
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
                OutlinedTextField(
                    value = items,
                    onValueChange = { items = it },
                    label = { Text("Items (comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Purpose") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
                OutlinedTextField(
                    value = bestTime,
                    onValueChange = { bestTime = it },
                    label = { Text("Best day / time") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedPhase.name.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Moon phase") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        phases.forEach { phase ->
                            DropdownMenuItem(
                                text = { Text(phase.name.replace("_", " ")) },
                                onClick = {
                                    selectedPhase = phase
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = warnings,
                    onValueChange = { warnings = it },
                    label = { Text("Warnings (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                OutlinedTextField(
                    value = sourceNote,
                    onValueChange = { sourceNote = it },
                    label = { Text("Source / tradition note") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) return@TextButton
                    val o = Offering(
                        id = offering?.id ?: 0,
                        deityId = deityId,
                        religion = religion,
                        name = name.trim(),
                        items = items.split(",").map { it.trim() }.filter { it.isNotBlank() },
                        purpose = purpose.trim(),
                        instructions = instructions.trim(),
                        moonPhase = selectedPhase,
                        bestTime = bestTime.trim(),
                        warnings = warnings.split(",").map { it.trim() }.filter { it.isNotBlank() },
                        sourceNote = sourceNote.trim()
                    )
                    if (isEdit) {
                        TarotApp.core.updateOffering(o.id, o)
                    } else {
                        TarotApp.core.addOffering(o)
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

@Composable
private fun WikipediaButton(url: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Button(
        onClick = {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_moon_full),
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text("Read on Wikipedia")
    }
}
