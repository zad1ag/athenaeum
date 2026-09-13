package dev.reggie.tarot

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class RuneOverride(
    val name: String? = null,
    val letters: String? = null,
    val meaning: String? = null,
    val keywords: List<String>? = null,
    val reversed: String? = null,
    val bindruneUses: List<String>? = null
)

data class CardOverride(
    val name: String? = null,
    val keywords: List<String>? = null,
    val uprightMeaning: String? = null,
    val reversedMeaning: String? = null,
    val contexts: ContextualMeanings? = null
)

class UserOverrides(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_overrides", Context.MODE_PRIVATE)
    private val gson = Gson()

    var revision by mutableIntStateOf(0)
        private set

    private inline fun <reified T> getMap(key: String): Map<Int, T> {
        val json = prefs.getString(key, null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, T>>() {}.type
        return try {
            gson.fromJson<Map<String, T>>(json, type).mapKeys { it.key.toInt() }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun <T> putMap(key: String, map: Map<Int, T>) {
        val serializable = map.mapKeys { it.key.toString() }
        prefs.edit().putString(key, gson.toJson(serializable)).apply()
        revision++
    }

    fun getRuneOverrides(): Map<Int, RuneOverride> = getMap("rune_overrides")

    fun getCardOverrides(): Map<Int, CardOverride> = getMap("card_overrides")

    fun getRuneOverride(id: Int): RuneOverride = getRuneOverrides()[id] ?: RuneOverride()

    fun getCardOverride(id: Int): CardOverride = getCardOverrides()[id] ?: CardOverride()

    fun saveRuneOverride(id: Int, override: RuneOverride) {
        val map = getRuneOverrides().toMutableMap()
        if (override.let { it.name == null && it.letters == null && it.meaning == null && it.keywords == null && it.reversed == null && it.bindruneUses == null }) {
            map.remove(id)
        } else {
            map[id] = override
        }
        putMap("rune_overrides", map)
    }

    fun saveCardOverride(id: Int, override: CardOverride) {
        val map = getCardOverrides().toMutableMap()
        if (override.let { it.name == null && it.keywords == null && it.uprightMeaning == null && it.reversedMeaning == null && it.contexts == null }) {
            map.remove(id)
        } else {
            map[id] = override
        }
        putMap("card_overrides", map)
    }

    fun resetRune(id: Int) {
        val map = getRuneOverrides().toMutableMap()
        map.remove(id)
        putMap("rune_overrides", map)
    }

    fun resetCard(id: Int) {
        val map = getCardOverrides().toMutableMap()
        map.remove(id)
        putMap("card_overrides", map)
    }

    /**
     * Re-read from disk. Used after restoring a backup that overwrote the
     * shared-prefs XML file on disk; SharedPreferences caches its in-memory
     * state, so without this the restored values would be ignored until the
     * next process start.
     */
    fun reloadFromDisk(context: Context) {
        val field = prefs.javaClass.getDeclaredField("mLoaded")
        field.isAccessible = true
        field.set(prefs, false)
        // Any subsequent access re-reads the XML from disk.
        prefs.all
    }
}

fun Rune.withOverride(o: RuneOverride): Rune = copy(
    name = o.name ?: name,
    letters = o.letters ?: letters,
    meaning = o.meaning ?: meaning,
    keywords = o.keywords ?: keywords,
    reversed = o.reversed ?: reversed,
    bindruneUses = o.bindruneUses ?: bindruneUses
)

fun Card.withOverride(o: CardOverride): Card = copy(
    name = o.name ?: name,
    keywords = o.keywords ?: keywords,
    uprightMeaning = o.uprightMeaning ?: uprightMeaning,
    reversedMeaning = o.reversedMeaning ?: reversedMeaning,
    contexts = o.contexts ?: contexts
)
