use serde::{Deserialize, Serialize};
use std::fmt;

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "lowercase")]
pub enum Arcana {
    Major,
    Minor,
}

impl fmt::Display for Arcana {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Arcana::Major => write!(f, "major"),
            Arcana::Minor => write!(f, "minor"),
        }
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "lowercase")]
pub enum Suit {
    None,
    Wands,
    Cups,
    Swords,
    Pentacles,
}

impl fmt::Display for Suit {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Suit::None => write!(f, "none"),
            Suit::Wands => write!(f, "wands"),
            Suit::Cups => write!(f, "cups"),
            Suit::Swords => write!(f, "swords"),
            Suit::Pentacles => write!(f, "pentacles"),
        }
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub struct CardNumber(pub i32);

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Card {
    pub id: i32,
    pub name: String,
    pub arcana: Arcana,
    pub suit: Suit,
    pub number: Option<CardNumber>,
    pub roman_numeral: Option<String>,
    pub upright_meaning: String,
    pub reversed_meaning: String,
    pub keywords: Vec<String>,
    pub image_ref: String,
    pub tone: Tone,
    pub contexts: ContextualMeanings,
}

#[derive(Debug, Clone, Default, Serialize, Deserialize)]
pub struct ContextualMeanings {
    pub general: String,
    pub love_dating: String,
    pub love_single: String,
    pub love_relationship: String,
    pub career: String,
    pub money: String,
    pub health: String,
}

#[derive(Debug, Default, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "lowercase")]
pub enum Tone {
    Positive,
    #[default]
    Neutral,
    Negative,
}

impl fmt::Display for Tone {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Tone::Positive => write!(f, "positive"),
            Tone::Neutral => write!(f, "neutral"),
            Tone::Negative => write!(f, "negative"),
        }
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub enum EntityType {
    Ghost,
    Spirit,
    Demon,
    Fae,
    Shade,
    Wraith,
    Poltergeist,
    Angel,
    Other,
}

impl fmt::Display for EntityType {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            EntityType::Ghost => write!(f, "ghost"),
            EntityType::Spirit => write!(f, "spirit"),
            EntityType::Demon => write!(f, "demon"),
            EntityType::Fae => write!(f, "fae"),
            EntityType::Shade => write!(f, "shade"),
            EntityType::Wraith => write!(f, "wraith"),
            EntityType::Poltergeist => write!(f, "poltergeist"),
            EntityType::Angel => write!(f, "angel"),
            EntityType::Other => write!(f, "other"),
        }
    }
}

pub fn parse_entity_type(s: &str) -> EntityType {
    match s.to_lowercase().as_str() {
        "ghost" => EntityType::Ghost,
        "spirit" => EntityType::Spirit,
        "demon" => EntityType::Demon,
        "fae" => EntityType::Fae,
        "shade" => EntityType::Shade,
        "wraith" => EntityType::Wraith,
        "poltergeist" => EntityType::Poltergeist,
        "angel" => EntityType::Angel,
        "other" => EntityType::Other,
        _ => EntityType::Spirit,
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub enum SpellCategory {
    Cleansing,
    Protection,
    Divination,
    Curses,
    Summoning,
    Healing,
    Binding,
    Glamour,
    Love,
    Prosperity,
    Luck,
    Banishment,
    Dream,
    Weather,
    Blessing,
    Elemental,
    Necromancy,
    Other,
}

impl fmt::Display for SpellCategory {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            SpellCategory::Cleansing => write!(f, "cleansing"),
            SpellCategory::Protection => write!(f, "protection"),
            SpellCategory::Divination => write!(f, "divination"),
            SpellCategory::Curses => write!(f, "curses"),
            SpellCategory::Summoning => write!(f, "summoning"),
            SpellCategory::Healing => write!(f, "healing"),
            SpellCategory::Binding => write!(f, "binding"),
            SpellCategory::Glamour => write!(f, "glamour"),
            SpellCategory::Love => write!(f, "love"),
            SpellCategory::Prosperity => write!(f, "prosperity"),
            SpellCategory::Luck => write!(f, "luck"),
            SpellCategory::Banishment => write!(f, "banishment"),
            SpellCategory::Dream => write!(f, "dream"),
            SpellCategory::Weather => write!(f, "weather"),
            SpellCategory::Blessing => write!(f, "blessing"),
            SpellCategory::Elemental => write!(f, "elemental"),
            SpellCategory::Necromancy => write!(f, "necromancy"),
            SpellCategory::Other => write!(f, "other"),
        }
    }
}

pub fn parse_spell_category(s: &str) -> SpellCategory {
    match s.to_lowercase().as_str() {
        "cleansing" => SpellCategory::Cleansing,
        "protection" => SpellCategory::Protection,
        "divination" => SpellCategory::Divination,
        "curses" => SpellCategory::Curses,
        "summoning" => SpellCategory::Summoning,
        "healing" => SpellCategory::Healing,
        "binding" => SpellCategory::Binding,
        "glamour" => SpellCategory::Glamour,
        "love" => SpellCategory::Love,
        "prosperity" => SpellCategory::Prosperity,
        "luck" => SpellCategory::Luck,
        "banishment" => SpellCategory::Banishment,
        "dream" => SpellCategory::Dream,
        "weather" => SpellCategory::Weather,
        "blessing" => SpellCategory::Blessing,
        "elemental" => SpellCategory::Elemental,
        "necromancy" => SpellCategory::Necromancy,
        _ => SpellCategory::Other,
    }
}

#[derive(Debug, Default, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub enum MoonPhaseRequirement {
    #[default]
    Any,
    NewMoon,
    WaxingCrescent,
    FirstQuarter,
    WaxingGibbous,
    FullMoon,
    WaningGibbous,
    LastQuarter,
    WaningCrescent,
}

impl fmt::Display for MoonPhaseRequirement {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            MoonPhaseRequirement::Any => write!(f, "any"),
            MoonPhaseRequirement::NewMoon => write!(f, "new_moon"),
            MoonPhaseRequirement::WaxingCrescent => write!(f, "waxing_crescent"),
            MoonPhaseRequirement::FirstQuarter => write!(f, "first_quarter"),
            MoonPhaseRequirement::WaxingGibbous => write!(f, "waxing_gibbous"),
            MoonPhaseRequirement::FullMoon => write!(f, "full_moon"),
            MoonPhaseRequirement::WaningGibbous => write!(f, "waning_gibbous"),
            MoonPhaseRequirement::LastQuarter => write!(f, "last_quarter"),
            MoonPhaseRequirement::WaningCrescent => write!(f, "waning_crescent"),
        }
    }
}

pub fn parse_moon_phase_requirement(s: &str) -> MoonPhaseRequirement {
    match s.to_lowercase().as_str() {
        "new_moon" => MoonPhaseRequirement::NewMoon,
        "waxing_crescent" => MoonPhaseRequirement::WaxingCrescent,
        "first_quarter" => MoonPhaseRequirement::FirstQuarter,
        "waxing_gibbous" => MoonPhaseRequirement::WaxingGibbous,
        "full_moon" => MoonPhaseRequirement::FullMoon,
        "waning_gibbous" => MoonPhaseRequirement::WaningGibbous,
        "last_quarter" => MoonPhaseRequirement::LastQuarter,
        "waning_crescent" => MoonPhaseRequirement::WaningCrescent,
        _ => MoonPhaseRequirement::Any,
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Spell {
    pub id: i32,
    pub name: String,
    pub category: SpellCategory,
    pub purpose: String,
    pub difficulty: i32,
    pub risk_rating: i32,
    pub moon_phase: MoonPhaseRequirement,
    pub ingredients: Vec<String>,
    pub steps: Vec<String>,
    pub warnings: Vec<String>,
    pub related_entity_ids: Vec<i32>,
    pub source_note: String,
    #[serde(default)]
    pub is_custom: bool,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Entity {
    pub id: i32,
    pub name: String,
    pub entity_type: EntityType,
    pub origin: String,
    pub description: String,
    pub signs: Vec<String>,
    pub weaknesses: Vec<String>,
    pub banishment: String,
    pub danger: String,
    #[serde(default)]
    pub tone: Tone,
    #[serde(default)]
    pub is_custom: bool,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AstroSign {
    pub id: i32,
    pub name: String,
    pub dates: String,
    pub element: String,
    pub modality: String,
    pub ruler: String,
    pub symbol: String,
    pub traits: Vec<String>,
    pub body_part: String,
    pub description: String,
    pub compatibility: Vec<String>,
    pub magic_notes: String,
    #[serde(default)]
    pub is_custom: bool,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AstroBody {
    pub id: i32,
    pub name: String,
    pub symbol: String,
    pub day: String,
    pub domain: String,
    pub description: String,
    pub color_note: String,
    #[serde(default)]
    pub is_custom: bool,
}

fn default_tz() -> f64 { 0.0 }

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct BirthProfile {
    pub id: i32,
    pub name: String,
    /// Birth date/time as Julian Day (UT) of the instant.
    pub jd: f64,
    #[serde(default)]
    pub latitude: Option<f64>,
    #[serde(default)]
    pub longitude: Option<f64>,
    #[serde(default = "default_tz")]
    pub timezone_offset_hours: f64,
    #[serde(default)]
    pub is_custom: bool,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TraditionVersion {
    pub tradition: String,
    pub name_variant: String,
    pub description: String,
    pub domains: Vec<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Deity {
    pub id: i32,
    pub name: String,
    #[serde(default)]
    pub wiki_title: Option<String>,
    pub primary_religion: String,
    pub alignment: String,
    pub domains: Vec<String>,
    pub description: String,
    pub versions: Vec<TraditionVersion>,
    #[serde(default)]
    pub is_custom: bool,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Offering {
    pub id: i32,
    pub name: String,
    pub deity_id: i32,
    pub religion: String,
    pub items: Vec<String>,
    pub instructions: String,
    pub purpose: String,
    #[serde(default)]
    pub moon_phase: MoonPhaseRequirement,
    #[serde(default)]
    pub best_time: String,
    #[serde(default)]
    pub warnings: Vec<String>,
    #[serde(default)]
    pub source_note: String,
    #[serde(default)]
    pub is_custom: bool,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Stave {
    pub id: i32,
    pub name: String,
    pub icelandic_name: String,
    pub meaning: String,
    pub purpose: String,
    pub category: String,
    pub visual_notes: String,
    pub image_ref: String,
    #[serde(default)]
    pub is_custom: bool,
}

pub fn normalize_card_id(id: i32) -> Option<i32> {
    if (0..=77).contains(&id) {
        Some(id)
    } else {
        None
    }
}

pub fn parse_arcana(s: &str) -> Arcana {
    match s.to_lowercase().as_str() {
        "minor" => Arcana::Minor,
        _ => Arcana::Major,
    }
}

pub fn parse_suit(s: &str) -> Suit {
    match s.to_lowercase().as_str() {
        "wands" => Suit::Wands,
        "cups" => Suit::Cups,
        "swords" => Suit::Swords,
        "pentacles" => Suit::Pentacles,
        _ => Suit::None,
    }
}
