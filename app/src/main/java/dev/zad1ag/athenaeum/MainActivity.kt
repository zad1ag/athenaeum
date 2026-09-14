package dev.zad1ag.athenaeum

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.zad1ag.athenaeum.BuildConfig
import dev.zad1ag.athenaeum.ui.NatalChartScreen
import dev.zad1ag.athenaeum.ui.OnboardingScreen

// Palette anchors: wine #5C1F2B (primary family), ink #1A1A22 (neutral family).
private val Wine = Color(0xFF5C1F2B)
private val Ink = Color(0xFF1A1A22)
private val PaleGold = Color(0xFFC9A959)
private val Rust = Color(0xFF8B3A2F)

private val AppDarkScheme = darkColorScheme(
    primary = Color(0xFFE0B5BD),          // lifted wine for contrast on ink
    onPrimary = Color(0xFF44121E),
    primaryContainer = Wine,
    onPrimaryContainer = Color(0xFFFFD9E0),
    inversePrimary = Color(0xFF7A3944),
    secondary = Color(0xFFC7B4B8),        // smoky rose-gray
    onSecondary = Color(0xFF33272A),
    secondaryContainer = Color(0xFF493235),
    onSecondaryContainer = Color(0xFFF2D9DD),
    tertiary = PaleGold,                  // gold accent retained
    onTertiary = Color(0xFF221A00),
    tertiaryContainer = Color(0xFF4A3D1F),
    onTertiaryContainer = Color(0xFFF5E0A5),
    background = Ink,
    onBackground = Color(0xFFE3E1E8),
    surface = Color(0xFF1E1D25),          // ink, one step up
    onSurface = Color(0xFFE3E1E8),
    surfaceVariant = Color(0xFF3B3840),
    onSurfaceVariant = Color(0xFFC5C2CB),
    surfaceTint = Color(0xFFE0B5BD),
    inverseSurface = Color(0xFFE3E1E8),
    inverseOnSurface = Color(0xFF2B2A32),
    outline = Color(0xFF99919B),
    outlineVariant = Color(0xFF4B4650),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF5C1A13),
    errorContainer = Rust,
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0xFF000000)
)

private val AppLightScheme = lightColorScheme(
    primary = Wine,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF9D9DE),
    onPrimaryContainer = Color(0xFF3F111A),
    inversePrimary = Color(0xFFE0B5BD),
    secondary = Color(0xFF6F5257),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF2DADE),
    onSecondaryContainer = Color(0xFF583D42),
    tertiary = Color(0xFF6E5814),         // gold, darkened for light bg contrast
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF5E0A5),
    onTertiaryContainer = Color(0xFF221B00),
    background = Color(0xFFFBF2F3),       // warm pale rosed paper
    onBackground = Color(0xFF1C1B1D),
    surface = Color(0xFFFBF2F3),
    onSurface = Color(0xFF1C1B1D),
    surfaceVariant = Color(0xFFE8E0E3),
    onSurfaceVariant = Color(0xFF4E4548),
    surfaceTint = Wine,
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF0),
    outline = Color(0xFF7F7477),
    outlineVariant = Color(0xFFD1C2C6),
    error = Rust,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD4),
    onErrorContainer = Color(0xFF3F0700),
    scrim = Color(0xFF000000)
)

@Composable
fun TarotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    // Re-keyed on FeatureSettings.version so flipping the toggle in Settings
    // recomposes the theme immediately.
    val dynamicVersion = FeatureSettings.version.value
    val useDynamicColor = remember(dynamicVersion) { FeatureSettings.isDynamicColorEnabled(context) }
    val colors = when {
        dynamicColor && useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AppDarkScheme
        else -> AppLightScheme
    }
    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography,
        content = content
    )
}

sealed class TopLevelScreen(val route: String, val label: String, val icon: @Composable () -> Unit) {
    object Home : TopLevelScreen("home", "Home", { Icon(Icons.Default.Home, contentDescription = "Home") })
    object Tarot : TopLevelScreen("tarot", "Tarot", { Icon(Icons.Default.Menu, contentDescription = "Tarot") })
    object Astrology : TopLevelScreen("astrology", "Astrology", { Icon(Icons.Default.Star, contentDescription = "Astrology") })
    object Spirits : TopLevelScreen("spirits", "Spirits", { Icon(painterResource(R.drawable.ic_skull), contentDescription = "Spirits") })
    object Spells : TopLevelScreen("spells", "Magic", { Icon(Icons.Default.AutoFixHigh, contentDescription = "Magic") })
    object Settings : TopLevelScreen("settings", "Settings", { Icon(Icons.Default.Settings, contentDescription = "Settings") })
}

private val topLevelDestinations = listOf(
    TopLevelScreen.Home,
    TopLevelScreen.Tarot,
    TopLevelScreen.Astrology,
    TopLevelScreen.Spirits,
    TopLevelScreen.Spells,
    TopLevelScreen.Settings
)

private fun enabledDestinations(context: android.content.Context): List<TopLevelScreen> =
    topLevelDestinations.filter { screen ->
        when (screen) {
            TopLevelScreen.Home, TopLevelScreen.Settings -> true
            TopLevelScreen.Tarot -> FeatureSettings.isEnabled(context, FeatureSettings.Feature.TAROT)
            TopLevelScreen.Astrology -> FeatureSettings.isEnabled(context, FeatureSettings.Feature.ASTROLOGY)
            TopLevelScreen.Spirits -> FeatureSettings.isEnabled(context, FeatureSettings.Feature.SPIRITS)
            TopLevelScreen.Spells -> FeatureSettings.isEnabled(context, FeatureSettings.Feature.MAGIC)
        }
    }

class MainActivity : ComponentActivity() {
    private val openMoonCalendar = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        openMoonCalendar.value = intent?.getBooleanExtra("open_moon_calendar", false) ?: false
        val appContext = applicationContext

        // One-time onboarding: show only until the user finishes or skips it.
        // DEBUG builds force it on every install so the flow stays testable;
        // release builds only ever show it once.
        val onboardingDone = if (BuildConfig.DEBUG) {
            false
        } else {
            TarotCore(appContext)
                .getAppSetting("onboarding_complete", "false").toBooleanStrictOrNull() == true
        }

        setContent {
            TarotTheme {
                val navController = rememberNavController()
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentDestination = currentBackStack?.destination
                val currentRoute = currentDestination?.route
                // FeatureSettings.version is a MutableState bumped on every
                // toggle flip, so the bottom bar recomposes live.
                val featureVersion by FeatureSettings.version
                val visibleDestinations = remember(featureVersion) {
                    enabledDestinations(appContext)
                }
                val startDestination = if (onboardingDone) {
                    TopLevelScreen.Home.route
                } else {
                    "onboarding"
                }

                Scaffold(
                    bottomBar = {
                        if (currentRoute != "onboarding") {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                visibleDestinations.forEach { screen ->
                                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                    NavigationBarItem(
                                        icon = screen.icon,
                                        label = {
                                            Text(
                                                screen.label,
                                                maxLines = 1,
                                                softWrap = false,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(padding)
                    ) {
                        composable("onboarding") {
                            OnboardingScreen(
                                onFinish = {
                                    TarotApp.core.setAppSetting("onboarding_complete", "true")
                                    navController.navigate(TopLevelScreen.Home.route) {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(TopLevelScreen.Home.route) {
                            HomeScreen(
                                openMoonCalendar = openMoonCalendar.value,
                                openMoonCalendarReset = { openMoonCalendar.value = false },
                                onTarotClick = {
                                    navController.navigate(TopLevelScreen.Tarot.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                        composable(TopLevelScreen.Tarot.route) {
                            TarotScreen(
                                onCardClick = { id ->
                                    navController.navigate("card_detail/$id")
                                }
                            )
                        }
                        composable(TopLevelScreen.Astrology.route) {
                            AstrologyScreen(
                                onSignClick = { id ->
                                    navController.navigate("astro_sign_detail/$id")
                                },
                                onBodyClick = { id ->
                                    navController.navigate("astro_body_detail/$id")
                                },
                                onOpenNatal = {
                                    navController.navigate("natal_charts")
                                }
                            )
                        }
                        composable(TopLevelScreen.Spirits.route) {
                            SpiritsScreen(
                                onEntityClick = { id ->
                                    navController.navigate("entity_detail/$id")
                                },
                                onDeityClick = { id ->
                                    navController.navigate("deity_detail/$id")
                                }
                            )
                        }
                         composable(TopLevelScreen.Spells.route) {
                             SpellsScreen(
                                 onSpellClick = { id ->
                                     navController.navigate("spell_detail/$id")
                                 },
                                 onStaveClick = { id ->
                                     navController.navigate("stave_detail/$id")
                                 },
                                 onEntityClick = { id ->
                                     navController.navigate("entity_detail/$id")
                                 },
                                 onRuneClick = { id ->
                                     navController.navigate("rune_detail/$id")
                                 }
                             )
                         }
                        composable(TopLevelScreen.Settings.route) {
                            SettingsScreen()
                        }
                        composable("card_detail/{cardId}") { entry ->
                            val cardId = entry.arguments?.getString("cardId")?.toIntOrNull() ?: 0
                            CardDetailScreen(cardId = cardId, onBack = { navController.popBackStack() })
                        }
                        composable("entity_detail/{entityId}") { entry ->
                            val id = entry.arguments?.getString("entityId")?.toIntOrNull() ?: 0
                            EntityDetailScreen(id = id, onBack = { navController.popBackStack() })
                        }
                        composable("spell_detail/{spellId}") { entry ->
                            val id = entry.arguments?.getString("spellId")?.toIntOrNull() ?: 0
                            SpellDetailScreen(
                                id = id,
                                onBack = { navController.popBackStack() },
                                onEntityClick = { entityId ->
                                    navController.navigate("entity_detail/$entityId")
                                }
                            )
                        }
                         composable("deity_detail/{deityId}") { entry ->
                             val id = entry.arguments?.getString("deityId")?.toIntOrNull() ?: 0
                             DeityDetailScreen(
                                 id = id,
                                 onBack = { navController.popBackStack() }
                             )
                         }
                         composable("stave_detail/{staveId}") { entry ->
                             val id = entry.arguments?.getString("staveId")?.toIntOrNull() ?: 0
                             StaveDetailScreen(
                                 id = id,
                                 onBack = { navController.popBackStack() }
                             )
                         }
                          composable("natal_charts") {
                              NatalChartScreen(onBack = { navController.popBackStack() })
                          }
                          composable("astro_sign_detail/{signId}") { entry ->
                              val id = entry.arguments?.getString("signId")?.toIntOrNull() ?: 0
                              AstroSignDetailScreen(
                                  id = id,
                                  onBack = { navController.popBackStack() }
                              )
                          }
                          composable("astro_body_detail/{bodyId}") { entry ->
                              val id = entry.arguments?.getString("bodyId")?.toIntOrNull() ?: 0
                              AstroBodyDetailScreen(
                                  id = id,
                                  onBack = { navController.popBackStack() }
                              )
                          }
                          composable("rune_detail/{runeId}") { entry ->
                             val id = entry.arguments?.getString("runeId")?.toIntOrNull() ?: 0
                             RuneDetailScreen(
                                 id = id,
                                 onBack = { navController.popBackStack() }
                             )
                         }
                      }
                 }
             }
         }
     }
 }
