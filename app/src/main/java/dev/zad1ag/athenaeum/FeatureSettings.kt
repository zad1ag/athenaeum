package dev.zad1ag.athenaeum

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/**
 * Feature toggles stored in app-level SharedPreferences. Sections the user
 * has disabled are hidden from the top bar nav, the home screen, and the
 * Spells mode tabs.
 */
object FeatureSettings {

    enum class Feature(val label: String) {
        TAROT("Tarot"),
        ASTROLOGY("Astrology"),
        MAGIC("Magic"),
        SPELLS("Spells"),
        STAVES("Staves"),
        RUNES("Runes"),
        SPIRITS("Spirits"),
        ENTITIES("Entities"),
        DEITIES("Deities")
    }

    /** Sub-features grouped under a section toggle. */
    fun sectionOf(feature: Feature): Feature? = when (feature) {
        Feature.SPELLS, Feature.STAVES, Feature.RUNES -> Feature.MAGIC
        Feature.ENTITIES, Feature.DEITIES -> Feature.SPIRITS
        else -> null
    }

    private const val PREFS = "feature_settings"
    private const val KEY_WIDGET_BACKGROUND = "widget_background"
    private const val KEY_DYNAMIC_COLOR = "dynamic_color"

    /** Bumped on every toggle change so observers (MainActivity) recompose. */
    val version: MutableState<Int> = mutableStateOf(0)

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isEnabled(context: Context, feature: Feature): Boolean =
        prefs(context).getBoolean(feature.name, true)

    fun setEnabled(context: Context, feature: Feature, enabled: Boolean) {
        prefs(context).edit().putBoolean(feature.name, enabled).apply()
        version.value = version.value + 1
    }

    /** Widget container background toggle. Default on. */
    fun isWidgetBackgroundEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_WIDGET_BACKGROUND, true)

    fun setWidgetBackgroundEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_WIDGET_BACKGROUND, enabled).apply()
    }

    /** Material You (wallpaper) dynamic color. Default off — the wine/ink theme is the identity. */
    fun isDynamicColorEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DYNAMIC_COLOR, false)

    fun setDynamicColorEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        version.value = version.value + 1
    }
}