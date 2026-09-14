package dev.zad1ag.athenaeum

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

class TarotCore(context: Context) {

    companion object {
        init {
            System.loadLibrary("tarot_core")
        }
    }

    private val gson = Gson()
    private var dbPath: String = ""

    @android.annotation.SuppressLint("FilePath")

    external fun nativeInit(dbPath: String)
    external fun nativeGetAllCards(): String
    external fun nativeGetMajorArcana(): String
    external fun nativeGetCardsBySuit(suitIdx: Int): String
    external fun nativeGetCard(id: Int): String
    external fun nativeGetAllEntities(): String
    external fun nativeGetEntity(id: Int): String
    external fun nativeGetEntitiesByType(typeIdx: Int): String
    external fun nativeSearchEntities(query: String): String
    external fun nativeAddEntity(json: String): String
    external fun nativeUpdateEntity(id: Int, json: String)
    external fun nativeDeleteEntity(id: Int)
    external fun nativeDeleteEntities(idsJson: String)
    external fun nativeGetAllDeities(): String
    external fun nativeGetDeity(id: Int): String
    external fun nativeGetDeitiesByReligion(religion: String): String
    external fun nativeSearchDeities(query: String): String
    external fun nativeAddDeity(json: String): String
    external fun nativeUpdateDeity(id: Int, json: String)
    external fun nativeDeleteDeity(id: Int)
    external fun nativeDeleteDeities(idsJson: String)
    external fun nativeRestoreBuiltins()
    external fun nativeGetAllAstroSigns(): String
    external fun nativeGetAstroSign(id: Int): String
    external fun nativeGetAllAstroBodies(): String
    external fun nativeGetAstroBody(id: Int): String
    external fun nativeDailyAstro(): String
    external fun nativeDeleteAstroEntries(signIdsJson: String, bodyIdsJson: String)
    external fun nativeGetAllBirthProfiles(): String
    external fun nativeSaveBirthProfile(id: Int, json: String): String
    external fun nativeDeleteBirthProfile(id: Int)
    external fun nativeNatalChart(json: String): String
    external fun nativeGetAllSpells(): String
    external fun nativeGetSpell(id: Int): String
    external fun nativeGetSpellsByCategory(categoryIdx: Int): String
    external fun nativeSearchSpells(query: String): String
    external fun nativeAddSpell(json: String): String
    external fun nativeDeleteSpell(id: Int)
    external fun nativeDeleteSpells(idsJson: String)
    external fun nativeUpdateSpell(id: Int, json: String)

    external fun nativeGetAllStaves(): String
    external fun nativeGetStave(id: Int): String
    external fun nativeGetStavesByCategory(category: String): String
    external fun nativeSearchStaves(query: String): String
    external fun nativeUpdateStave(id: Int, json: String)

    external fun nativeGetAllOfferings(): String
    external fun nativeGetOfferingsByDeity(deityId: Int): String
    external fun nativeGetOffering(id: Int): String
    external fun nativeSearchOfferings(query: String): String
    external fun nativeAddOffering(json: String): String
    external fun nativeDeleteOffering(id: Int)
    external fun nativeUpdateOffering(id: Int, json: String)
    external fun nativeGetUserName(): String
    external fun nativeSetUserName(name: String)
    external fun nativeGetAppSetting(key: String): String
    external fun nativeSetAppSetting(key: String, value: String)

    fun init(dbPath: String) {
        this.dbPath = dbPath
        nativeInit(dbPath)
    }

    /**
     * Re-open the native database after its file has been replaced from a
     * backup restore. The old connection is dropped and a fresh one is opened
     * against the same path.
     */
    fun reload() {
        nativeInit(this.dbPath)
    }

    fun getAllCards(): List<Card> {
        return parseList(nativeGetAllCards(), object : TypeToken<List<Card>>() {}.type)
    }

    fun getMajorArcana(): List<Card> {
        return parseList(nativeGetMajorArcana(), object : TypeToken<List<Card>>() {}.type)
    }

    fun getCardsBySuit(suit: Suit): List<Card> {
        val idx = when (suit) {
            Suit.wands -> 1
            Suit.cups -> 2
            Suit.swords -> 3
            Suit.pentacles -> 4
            Suit.none -> 0
        }
        return parseList(nativeGetCardsBySuit(idx), object : TypeToken<List<Card>>() {}.type)
    }

    fun getCard(id: Int): Card? {
        val envelope = gson.fromJson(nativeGetCard(id), ResponseEnvelope::class.java)
        return if (envelope.success && envelope.data != null) {
            gson.fromJson(envelope.data, Card::class.java)
        } else {
            Log.e("TarotCore", "getCard failed: ${envelope.error}")
            null
        }
    }

    fun getAllEntities(): List<Entity> {
        return parseList(nativeGetAllEntities(), object : TypeToken<List<Entity>>() {}.type)
    }

    fun getEntity(id: Int): Entity? {
        val envelope = gson.fromJson(nativeGetEntity(id), ResponseEnvelope::class.java)
        return if (envelope.success && envelope.data != null) {
            gson.fromJson(envelope.data, Entity::class.java)
        } else {
            Log.e("TarotCore", "getEntity failed: ${envelope.error}")
            null
        }
    }

    fun getEntitiesByType(type: EntityType): List<Entity> {
        val idx = when (type) {
            EntityType.ghost -> 1
            EntityType.spirit -> 2
            EntityType.demon -> 3
            EntityType.fae -> 4
            EntityType.shade -> 5
            EntityType.wraith -> 6
            EntityType.poltergeist -> 7
            EntityType.angel -> 8
            EntityType.other -> 9
        }
        return parseList(nativeGetEntitiesByType(idx), object : TypeToken<List<Entity>>() {}.type)
    }

    fun searchEntities(query: String): List<Entity> {
        return parseList(nativeSearchEntities(query), object : TypeToken<List<Entity>>() {}.type)
    }

    fun addEntity(entity: Entity): Long {
        val json = gson.toJson(entity)
        val envelope = gson.fromJson(nativeAddEntity(json), ResponseEnvelope::class.java)
        return if (envelope.success && envelope.data != null) {
            gson.fromJson(envelope.data, Long::class.java) ?: -1L
        } else {
            Log.e("TarotCore", "addEntity failed: ${envelope.error}")
            -1L
        }
    }

    fun updateEntity(id: Int, entity: Entity) {
        val json = gson.toJson(entity)
        nativeUpdateEntity(id, json)
    }

    fun deleteEntity(id: Int) {
        nativeDeleteEntity(id)
    }

    fun deleteEntities(ids: List<Int>) {
        nativeDeleteEntities(gson.toJson(ids))
    }

    fun getAllDeities(): List<Deity> {
        return parseList(nativeGetAllDeities(), object : TypeToken<List<Deity>>() {}.type)
    }

    fun getDeity(id: Int): Deity? {
        val envelope = gson.fromJson(nativeGetDeity(id), ResponseEnvelope::class.java)
        return if (envelope.success && envelope.data != null) {
            gson.fromJson(envelope.data, Deity::class.java)
        } else {
            Log.e("TarotCore", "getDeity failed: ${envelope.error}")
            null
        }
    }

    fun getDeitiesByReligion(religion: String): List<Deity> {
        return parseList(nativeGetDeitiesByReligion(religion), object : TypeToken<List<Deity>>() {}.type)
    }

    fun searchDeities(query: String): List<Deity> {
        return parseList(nativeSearchDeities(query), object : TypeToken<List<Deity>>() {}.type)
    }

    fun addDeity(deity: Deity): Long {
        val json = gson.toJson(deity)
        val envelope = gson.fromJson(nativeAddDeity(json), ResponseEnvelope::class.java)
        return if (envelope.success && envelope.data != null) {
            gson.fromJson(envelope.data, Long::class.java) ?: -1L
        } else {
            Log.e("TarotCore", "addDeity failed: ${envelope.error}")
            -1L
        }
    }

    fun updateDeity(id: Int, deity: Deity) {
        val json = gson.toJson(deity)
        nativeUpdateDeity(id, json)
    }

    fun deleteDeity(id: Int) {
        nativeDeleteDeity(id)
    }

    fun deleteDeities(ids: List<Int>) {
        nativeDeleteDeities(gson.toJson(ids))
    }

    fun restoreBuiltins() {
        nativeRestoreBuiltins()
    }


    fun getAllBirthProfiles(): List<BirthProfile> {
        return parseList(nativeGetAllBirthProfiles(), object : TypeToken<List<BirthProfile>>() {}.type)
    }

    fun saveBirthProfile(id: Int, profile: BirthProfile): Long {
        val envelope = gson.fromJson(nativeSaveBirthProfile(id, gson.toJson(profile)), ResponseEnvelope::class.java)
        return if (envelope.success && envelope.data != null) {
            gson.fromJson(envelope.data, Long::class.java) ?: -1L
        } else {
            Log.e("TarotCore", "saveBirthProfile failed: ${envelope.error}")
            -1L
        }
    }

    fun deleteBirthProfile(id: Int) {
        nativeDeleteBirthProfile(id)
    }

    fun natalChart(profile: BirthProfile): NatalChart? {
        val envelope = gson.fromJson(natalChartData(profile), ResponseEnvelope::class.java)
        return if (envelope.success && envelope.data != null) {
            gson.fromJson(envelope.data, NatalChart::class.java)
        } else {
            Log.e("TarotCore", "natalChart failed: ${envelope.error}")
            null
        }
    }

    private fun natalChartData(profile: BirthProfile): String {
        // NativeNatalChart expects the BirthProfile JSON directly.
        val json = gson.toJson(profile)
        return nativeNatalChart(json)
    }

    fun getAllAstroSigns(): List<AstroSign> {
        return parseList(nativeGetAllAstroSigns(), object : TypeToken<List<AstroSign>>() {}.type)
    }

    fun getAstroSign(id: Int): AstroSign? {
        val envelope = gson.fromJson(nativeGetAstroSign(id), ResponseEnvelope::class.java)
        return if (envelope.success && envelope.data != null) {
            gson.fromJson(envelope.data, AstroSign::class.java)
        } else {
            Log.e("TarotCore", "getAstroSign failed: ${envelope.error}")
            null
        }
    }

    fun getAllAstroBodies(): List<AstroBody> {
        return parseList(nativeGetAllAstroBodies(), object : TypeToken<List<AstroBody>>() {}.type)
    }

    fun getAstroBody(id: Int): AstroBody? {
        val envelope = gson.fromJson(nativeGetAstroBody(id), ResponseEnvelope::class.java)
        return if (envelope.success && envelope.data != null) {
            gson.fromJson(envelope.data, AstroBody::class.java)
        } else {
            Log.e("TarotCore", "getAstroBody failed: ${envelope.error}")
            null
        }
    }

    fun dailyAstro(): DailyAstro? {
        val envelope = gson.fromJson(nativeDailyAstro(), ResponseEnvelope::class.java)
        return if (envelope.success && envelope.data != null) {
            gson.fromJson(envelope.data, DailyAstro::class.java)
        } else {
            Log.e("TarotCore", "dailyAstro failed: ${envelope.error}")
            null
        }
    }

    fun deleteAstroEntries(signIds: List<Int>, bodyIds: List<Int>) {
        nativeDeleteAstroEntries(gson.toJson(signIds), gson.toJson(bodyIds))
    }

    fun getAllSpells(): List<Spell> {
        return parseList(nativeGetAllSpells(), object : TypeToken<List<Spell>>() {}.type)
    }

    fun getSpell(id: Int): Spell? {
        val envelope = gson.fromJson(nativeGetSpell(id), ResponseEnvelope::class.java)
        return if (envelope.success && envelope.data != null) {
            gson.fromJson(envelope.data, Spell::class.java)
        } else {
            Log.e("TarotCore", "getSpell failed: ${envelope.error}")
            null
        }
    }

    fun getSpellsByCategory(category: SpellCategory): List<Spell> {
        val idx = when (category) {
            SpellCategory.cleansing -> 1
            SpellCategory.protection -> 2
            SpellCategory.divination -> 3
            SpellCategory.curses -> 4
            SpellCategory.summoning -> 5
            SpellCategory.healing -> 6
            SpellCategory.binding -> 7
            SpellCategory.glamour -> 8
            SpellCategory.love -> 9
            SpellCategory.prosperity -> 10
            SpellCategory.luck -> 11
            SpellCategory.banishment -> 12
            SpellCategory.dream -> 13
            SpellCategory.weather -> 14
            SpellCategory.blessing -> 15
            SpellCategory.elemental -> 16
            SpellCategory.necromancy -> 17
            SpellCategory.other -> 18
        }
        return parseList(nativeGetSpellsByCategory(idx), object : TypeToken<List<Spell>>() {}.type)
    }

    fun searchSpells(query: String): List<Spell> {
        return parseList(nativeSearchSpells(query), object : TypeToken<List<Spell>>() {}.type)
    }

    fun addSpell(spell: Spell): Long {
        val json = gson.toJson(spell)
        val envelope = gson.fromJson(nativeAddSpell(json), ResponseEnvelope::class.java)
        return if (envelope.success && envelope.data != null) {
            gson.fromJson(envelope.data, Long::class.java) ?: -1L
        } else {
            Log.e("TarotCore", "addSpell failed: ${envelope.error}")
            -1L
        }
    }

    fun deleteSpell(id: Int) {
        nativeDeleteSpell(id)
    }

    fun deleteSpells(ids: List<Int>) {
        nativeDeleteSpells(gson.toJson(ids))
    }

    fun updateSpell(id: Int, spell: Spell) {
        val json = gson.toJson(spell)
        nativeUpdateSpell(id, json)
    }

    fun getAllStaves(): List<Stave> {
        return parseList(nativeGetAllStaves(), object : TypeToken<List<Stave>>() {}.type)
    }

    fun getStave(id: Int): Stave? {
        val envelope = gson.fromJson(nativeGetStave(id), ResponseEnvelope::class.java)
        return if (envelope.success && envelope.data != null) {
            gson.fromJson(envelope.data, Stave::class.java)
        } else {
            Log.e("TarotCore", "getStave failed: ${envelope.error}")
            null
        }
    }

    fun getStavesByCategory(category: String): List<Stave> {
        return parseList(nativeGetStavesByCategory(category), object : TypeToken<List<Stave>>() {}.type)
    }

    fun searchStaves(query: String): List<Stave> {
        return parseList(nativeSearchStaves(query), object : TypeToken<List<Stave>>() {}.type)
    }

    fun updateStave(stave: Stave) {
        val json = gson.toJson(stave)
        nativeUpdateStave(stave.id, json)
    }

    fun getAllOfferings(): List<Offering> {
        return parseList(nativeGetAllOfferings(), object : TypeToken<List<Offering>>() {}.type)
    }

    fun getOfferingsByDeity(deityId: Int): List<Offering> {
        return parseList(nativeGetOfferingsByDeity(deityId), object : TypeToken<List<Offering>>() {}.type)
    }

    fun getOffering(id: Int): Offering? {
        val envelope = gson.fromJson(nativeGetOffering(id), ResponseEnvelope::class.java)
        return if (envelope.success && envelope.data != null) {
            gson.fromJson(envelope.data, Offering::class.java)
        } else {
            Log.e("TarotCore", "getOffering failed: ${envelope.error}")
            null
        }
    }

    fun searchOfferings(query: String): List<Offering> {
        return parseList(nativeSearchOfferings(query), object : TypeToken<List<Offering>>() {}.type)
    }

    fun addOffering(offering: Offering): Long {
        val json = gson.toJson(offering)
        val envelope = gson.fromJson(nativeAddOffering(json), ResponseEnvelope::class.java)
        return if (envelope.success && envelope.data != null) {
            gson.fromJson(envelope.data, Long::class.java) ?: -1L
        } else {
            Log.e("TarotCore", "addOffering failed: ${envelope.error}")
            -1L
        }
    }

    fun updateOffering(id: Int, offering: Offering) {
        val json = gson.toJson(offering)
        nativeUpdateOffering(id, json)
    }

    fun deleteOffering(id: Int) {
        nativeDeleteOffering(id)
    }

    fun getUserName(): String {
        val envelope = gson.fromJson(nativeGetUserName(), ResponseEnvelope::class.java)
        if (!envelope.success || envelope.data == null) {
            Log.e("TarotCore", "getUserName failed: ${envelope.error}")
            return ""
        }
        return gson.fromJson(envelope.data, String::class.java) ?: ""
    }

    fun setUserName(name: String) {
        nativeSetUserName(name)
    }

    fun getAppSetting(key: String, defaultValue: String = ""): String {
        val envelope = gson.fromJson(nativeGetAppSetting(key), ResponseEnvelope::class.java)
        return if (envelope.success && envelope.data != null) {
            gson.fromJson(envelope.data, String::class.java) ?: defaultValue
        } else {
            defaultValue
        }
    }

    fun setAppSetting(key: String, value: String) {
        nativeSetAppSetting(key, value)
    }

    private fun <T> parseList(json: String, type: java.lang.reflect.Type): List<T> {
        val envelope = gson.fromJson(json, ResponseEnvelope::class.java)
        if (!envelope.success || envelope.data == null) {
            Log.e("TarotCore", "native call failed: ${envelope.error}")
            return emptyList()
        }
        return gson.fromJson(envelope.data, type) ?: emptyList()
    }

    private class ResponseEnvelope(
        val success: Boolean,
        val data: JsonElement?,
        val error: String?
    )
}

enum class Arcana {
    major, minor
}

enum class Suit {
    none, wands, cups, swords, pentacles
}

enum class Tone {
    positive, neutral, negative
}

enum class EntityType {
    ghost, spirit, demon, fae, shade, wraith, poltergeist, angel, other
}

enum class SpellCategory {
    cleansing, protection, divination, curses, summoning, healing, binding, glamour,
    love, prosperity, luck, banishment, dream, weather, blessing, elemental, necromancy, other
}

fun String.toTone(): Tone = try {
    Tone.valueOf(this)
} catch (_: IllegalArgumentException) {
    Tone.neutral
}

data class ContextualMeanings(
    val general: String,
    @SerializedName("love_dating")
    val loveDating: String,
    @SerializedName("love_single")
    val loveSingle: String,
    @SerializedName("love_relationship")
    val loveRelationship: String,
    val career: String,
    val money: String,
    val health: String
) {
    fun get(label: String): String = when (label) {
        "general" -> general
        "love_dating" -> loveDating
        "love_single" -> loveSingle
        "love_relationship" -> loveRelationship
        "career" -> career
        "money" -> money
        "health" -> health
        else -> general
    }
}

data class Card(
    val id: Int,
    val name: String,
    val arcana: Arcana,
    val suit: Suit,
    val number: Int?,
    @SerializedName("roman_numeral")
    val romanNumeral: String?,
    @SerializedName("upright_meaning")
    val uprightMeaning: String,
    @SerializedName("reversed_meaning")
    val reversedMeaning: String,
    val keywords: List<String>,
    @SerializedName("image_ref")
    val imageRef: String,
    val tone: String,
    val contexts: ContextualMeanings
)

data class Entity(
    val id: Int,
    val name: String,
    @SerializedName("entity_type") val entityType: EntityType,
    val origin: String,
    val description: String,
    val signs: List<String>,
    val weaknesses: List<String>,
    val banishment: String,
    val danger: String,
    val tone: String = "neutral",
    @SerializedName("is_custom") val isCustom: Boolean = false,
)


data class BirthProfile(
    val id: Int,
    val name: String,
    val jd: Double,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("timezone_offset_hours")
    val timezoneOffsetHours: Double,
    @SerializedName("is_custom")
    val isCustom: Boolean = false
)

data class Placement(
    val body: String,
    val longitude: Double,
    val sign: String,
    @SerializedName("degree_in_sign")
    val degreeInSign: Double,
    val house: Int,
    val retrograde: Boolean
)

data class NatalAspect(
    val a: String,
    val b: String,
    val kind: String,
    val separation: Double,
    val orb: Double
)

data class NatalChart(
    val jd: Double,
    val placements: List<Placement>,
    val aspects: List<NatalAspect>,
    @SerializedName("ascendant_longitude")
    val ascendantLongitude: Double?,
    @SerializedName("ascendant_sign")
    val ascendantSign: String?,
    val latitude: Double?
)

data class AstroSign(
    val id: Int,
    val name: String,
    val dates: String,
    val element: String,
    val modality: String,
    val ruler: String,
    val symbol: String,
    val traits: List<String>,
    @SerializedName("body_part") val bodyPart: String,
    val description: String,
    val compatibility: List<String>,
    @SerializedName("magic_notes") val magicNotes: String,
    @SerializedName("is_custom") val isCustom: Boolean = false
)

data class AstroBody(
    val id: Int,
    val name: String,
    val symbol: String,
    val day: String,
    val domain: String,
    val description: String,
    @SerializedName("color_note") val colorNote: String,
    @SerializedName("is_custom") val isCustom: Boolean = false
)

data class DailyAstro(
    @SerializedName("sun_sign") val sunSign: String,
    @SerializedName("moon_sign") val moonSign: String,
    @SerializedName("moon_degrees_into_sign") val moonDegreesIntoSign: Double,
    @SerializedName("moon_illumination") val moonIllumination: Double,
    @SerializedName("next_sign") val nextSign: String,
    @SerializedName("next_sign_jd") val nextSignJd: Double,
    @SerializedName("planetary_day") val planetaryDay: String,
    @SerializedName("weekday_name") val weekdayName: String
)

data class TraditionVersion(
    val tradition: String,
    @SerializedName("name_variant")
    val nameVariant: String,
    val description: String,
    val domains: List<String>
)

data class Deity(
    val id: Int,
    val name: String,
    @SerializedName("wiki_title")
    val wikiTitle: String? = null,
    @SerializedName("primary_religion")
    val primaryReligion: String,
    val alignment: String,
    val domains: List<String>,
    val description: String,
    val versions: List<TraditionVersion>,
    @SerializedName("is_custom")
    val isCustom: Boolean = false
)

enum class MoonPhaseRequirement {
    any, new_moon, waxing_crescent, first_quarter, waxing_gibbous,
    full_moon, waning_gibbous, last_quarter, waning_crescent
}

data class Spell(
    val id: Int,
    val name: String,
    val category: SpellCategory,
    val purpose: String,
    val difficulty: Int = 1,
    @SerializedName("risk_rating")
    val riskRating: Int = 1,
    @SerializedName("moon_phase")
    val moonPhase: MoonPhaseRequirement = MoonPhaseRequirement.any,
    val ingredients: List<String>,
    val steps: List<String>,
    val warnings: List<String>,
    @SerializedName("related_entity_ids")
    val relatedEntityIds: List<Int>,
    @SerializedName("source_note")
    val sourceNote: String,
    @SerializedName("is_custom")
    val isCustom: Boolean = false
)

data class Offering(
    val id: Int,
    val name: String,
    @SerializedName("deity_id")
    val deityId: Int,
    val religion: String,
    val items: List<String>,
    val instructions: String,
    val purpose: String,
    @SerializedName("moon_phase")
    val moonPhase: MoonPhaseRequirement = MoonPhaseRequirement.any,
    @SerializedName("best_time")
    val bestTime: String = "",
    val warnings: List<String> = emptyList(),
    @SerializedName("source_note")
    val sourceNote: String = "",
    @SerializedName("is_custom")
    val isCustom: Boolean = false
)

data class Stave(
    val id: Int,
    val name: String,
    @SerializedName("icelandic_name")
    val icelandicName: String,
    val meaning: String,
    val purpose: String,
    val category: String,
    @SerializedName("visual_notes")
    val visualNotes: String,
    @SerializedName("image_ref")
    val imageRef: String,
    @SerializedName("is_custom")
    val isCustom: Boolean = false
)

data class DrawnCard(
    val card: Card,
    val reversed: Boolean,
    val position: Int
)

data class SpreadPosition(
    val index: Int,
    val label: String,
    val meaning: String
)

data class Spread(
    @SerializedName("spread_type")
    val spreadType: String,
    val name: String,
    val description: String,
    val positions: List<SpreadPosition>
)
