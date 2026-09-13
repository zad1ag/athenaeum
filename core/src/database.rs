use crate::astro_data::{build_bodies, build_signs};
use crate::deities::build_deities;
use crate::deck::build_deck;
use crate::entities::build_entities;
use crate::models::{
    parse_arcana, parse_entity_type, parse_moon_phase_requirement,
    parse_spell_category, parse_suit, AstroBody, AstroSign, Arcana, Card, CardNumber, ContextualMeanings, Deity,
    Entity, EntityType, Offering, Spell, SpellCategory, Stave, Suit, Tone,
};
use crate::offerings::build_offerings;
use crate::spells::build_spells;
use crate::staves::build_staves;
use rusqlite::{Connection, Result as SqlResult};
use std::path::Path;

const SCHEMA_SQL: &str = r#"
CREATE TABLE IF NOT EXISTS cards (
    id INTEGER PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    arcana TEXT NOT NULL,
    suit TEXT NOT NULL,
    number INTEGER,
    roman_numeral TEXT,
    upright_meaning TEXT NOT NULL,
    reversed_meaning TEXT NOT NULL,
    keywords TEXT NOT NULL,
    image_ref TEXT NOT NULL,
    tone TEXT NOT NULL DEFAULT 'neutral',
    ctx_general TEXT NOT NULL DEFAULT '',
    ctx_love_dating TEXT NOT NULL DEFAULT '',
    ctx_love_single TEXT NOT NULL DEFAULT '',
    ctx_love_relationship TEXT NOT NULL DEFAULT '',
    ctx_career TEXT NOT NULL DEFAULT '',
    ctx_money TEXT NOT NULL DEFAULT '',
    ctx_health TEXT NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS settings (
    key TEXT PRIMARY KEY NOT NULL,
    value TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS deleted_builtins (
    kind TEXT NOT NULL,
    id INTEGER NOT NULL,
    PRIMARY KEY (kind, id)
);

CREATE TABLE IF NOT EXISTS birth_profiles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    jd REAL NOT NULL,
    latitude REAL,
    longitude REAL,
    timezone_offset_hours REAL NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS astro_signs (
    id INTEGER PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    dates TEXT NOT NULL,
    element TEXT NOT NULL,
    modality TEXT NOT NULL,
    ruler TEXT NOT NULL,
    symbol TEXT NOT NULL,
    traits TEXT NOT NULL,
    body_part TEXT NOT NULL,
    description TEXT NOT NULL,
    compatibility TEXT NOT NULL,
    magic_notes TEXT NOT NULL,
    is_custom INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS astro_bodies (
    id INTEGER PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    symbol TEXT NOT NULL,
    day TEXT NOT NULL,
    domain TEXT NOT NULL,
    description TEXT NOT NULL,
    color_note TEXT NOT NULL,
    is_custom INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS app_settings (
    key TEXT PRIMARY KEY NOT NULL,
    value TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS entities (
    id INTEGER PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    origin TEXT NOT NULL,
    description TEXT NOT NULL,
    signs TEXT NOT NULL,
    weaknesses TEXT NOT NULL,
    banishment TEXT NOT NULL,
    danger TEXT NOT NULL,
    tone TEXT NOT NULL DEFAULT 'neutral',
    is_custom INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS spells (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    purpose TEXT NOT NULL,
    difficulty INTEGER NOT NULL DEFAULT 1,
    risk_rating INTEGER NOT NULL DEFAULT 1,
    moon_phase TEXT NOT NULL DEFAULT 'any',
    ingredients TEXT NOT NULL,
    steps TEXT NOT NULL,
    warnings TEXT NOT NULL,
    related_entity_ids TEXT NOT NULL,
    source_note TEXT NOT NULL,
    is_custom INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_spells_category ON spells(category);

CREATE TABLE IF NOT EXISTS deities (
    id INTEGER PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    wiki_title TEXT,
    primary_religion TEXT NOT NULL,
    alignment TEXT NOT NULL,
    domains TEXT NOT NULL,
    description TEXT NOT NULL,
    versions TEXT NOT NULL,
    is_custom INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_deities_religion ON deities(primary_religion);

CREATE TABLE IF NOT EXISTS offerings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    deity_id INTEGER NOT NULL DEFAULT 0,
    religion TEXT NOT NULL,
    items TEXT NOT NULL,
    instructions TEXT NOT NULL,
    purpose TEXT NOT NULL,
    moon_phase TEXT NOT NULL DEFAULT 'any',
    best_time TEXT NOT NULL DEFAULT '',
    warnings TEXT NOT NULL DEFAULT '',
    source_note TEXT NOT NULL DEFAULT '',
    is_custom INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_offerings_deity_id ON offerings(deity_id);
CREATE INDEX IF NOT EXISTS idx_offerings_religion ON offerings(religion);

CREATE TABLE IF NOT EXISTS staves (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    icelandic_name TEXT NOT NULL,
    meaning TEXT NOT NULL,
    purpose TEXT NOT NULL,
    category TEXT NOT NULL,
    visual_notes TEXT NOT NULL,
    image_ref TEXT NOT NULL,
    is_custom INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_staves_category ON staves(category);

PRAGMA foreign_keys = ON;
"#;

#[derive(Debug, Clone)]
pub struct DbCard {
    pub id: i32,
    pub name: String,
    pub arcana: String,
    pub suit: String,
    pub number: Option<i32>,
    pub roman_numeral: Option<String>,
    pub upright_meaning: String,
    pub reversed_meaning: String,
    pub keywords: String,
    pub image_ref: String,
    pub tone: String,
    pub ctx_general: String,
    pub ctx_love_dating: String,
    pub ctx_love_single: String,
    pub ctx_love_relationship: String,
    pub ctx_career: String,
    pub ctx_money: String,
    pub ctx_health: String,
}

#[derive(Debug)]
pub struct TarotDatabase {
    conn: Connection,
}

fn keywords_csv(keywords: &[String]) -> String {
    keywords.join(",")
}

fn parse_keywords(s: &str) -> Vec<String> {
    s.split(',').map(|x| x.trim().to_string()).filter(|x| !x.is_empty()).collect()
}

impl TarotDatabase {
    pub fn open_in_memory() -> SqlResult<Self> {
        let conn = Connection::open_in_memory()?;
        conn.execute_batch("PRAGMA foreign_keys = ON;")?;
        let mut db = Self { conn };
        db.migrate()?;
        Ok(db)
    }

    pub fn open<P: AsRef<Path>>(path: P) -> SqlResult<Self> {
        let conn = Connection::open(path)?;
        conn.execute_batch("PRAGMA foreign_keys = ON;")?;
        let mut db = Self { conn };
        db.migrate()?;
        Ok(db)
    }

    fn migrate(&mut self) -> SqlResult<()> {
        self.conn.execute_batch(SCHEMA_SQL)?;
        self.migrate_entities_columns()?;
        self.migrate_spell_columns()?;
        self.migrate_offering_columns()?;
        self.migrate_deity_columns()?;

        self.seed_entities()?;

        let count: i64 = self.conn.query_row("SELECT COUNT(*) FROM cards", [], |row| row.get(0))?;
        if count == 0 {
            Self::seed_deck(&mut self.conn)?;
        }

        let spell_count: i64 = self
            .conn
            .query_row("SELECT COUNT(*) FROM spells", [], |row| row.get(0))?;
        if spell_count == 0 {
            self.seed_spells()?;
        } else {
            self.sync_spells()?;
        }

        self.seed_deities()?;
        self.seed_offerings()?;
        self.seed_staves()?;
        self.seed_astro()?;

        Ok(())
    }

    fn migrate_entities_columns(&self) -> SqlResult<()> {
        let cols: Vec<String> = self
            .conn
            .prepare("PRAGMA table_info(entities)")?
            .query_map([], |row| {
                let name: String = row.get(1)?;
                Ok(name)
            })?
            .collect::<SqlResult<Vec<String>>>()?;
        if !cols.contains(&"origin".into()) {
            self.conn.execute("ALTER TABLE entities ADD COLUMN origin TEXT NOT NULL DEFAULT ''", [])?;
        }
        if !cols.contains(&"banishment".into()) {
            self.conn.execute("ALTER TABLE entities ADD COLUMN banishment TEXT NOT NULL DEFAULT ''", [])?;
        }
        if !cols.contains(&"tone".into()) {
            self.conn.execute("ALTER TABLE entities ADD COLUMN tone TEXT NOT NULL DEFAULT 'neutral'", [])?;
        }
        if !cols.contains(&"is_custom".into()) {
            self.conn.execute("ALTER TABLE entities ADD COLUMN is_custom INTEGER NOT NULL DEFAULT 0", [])?;
        }
        Ok(())
    }

    fn migrate_spell_columns(&self) -> SqlResult<()> {
        let cols: Vec<String> = self
            .conn
            .prepare("PRAGMA table_info(spells)")?
            .query_map([], |row| {
                let name: String = row.get(1)?;
                Ok(name)
            })?
            .collect::<SqlResult<Vec<String>>>()?;
        if !cols.contains(&"is_custom".into()) {
            self.conn.execute("ALTER TABLE spells ADD COLUMN is_custom INTEGER NOT NULL DEFAULT 0", [])?;
        }
        if !cols.contains(&"difficulty".into()) {
            self.conn.execute("ALTER TABLE spells ADD COLUMN difficulty INTEGER NOT NULL DEFAULT 1", [])?;
        }
        if !cols.contains(&"risk_rating".into()) {
            self.conn.execute("ALTER TABLE spells ADD COLUMN risk_rating INTEGER NOT NULL DEFAULT 1", [])?;
        }
        if !cols.contains(&"moon_phase".into()) {
            self.conn.execute("ALTER TABLE spells ADD COLUMN moon_phase TEXT NOT NULL DEFAULT 'any'", [])?;
        }
        Ok(())
    }

    fn migrate_deity_columns(&self) -> SqlResult<()> {
        let cols: Vec<String> = self
            .conn
            .prepare("PRAGMA table_info(deities)")?
            .query_map([], |row| {
                let name: String = row.get(1)?;
                Ok(name)
            })?
            .collect::<SqlResult<Vec<String>>>()?;
        if !cols.contains(&"wiki_title".into()) {
            self.conn.execute("ALTER TABLE deities ADD COLUMN wiki_title TEXT", [])?;
        }
        Ok(())
    }

    fn migrate_offering_columns(&self) -> SqlResult<()> {
        let cols: Vec<String> = self
            .conn
            .prepare("PRAGMA table_info(offerings)")?
            .query_map([], |row| {
                let name: String = row.get(1)?;
                Ok(name)
            })?
            .collect::<SqlResult<Vec<String>>>()?;
        if !cols.contains(&"is_custom".into()) {
            self.conn.execute("ALTER TABLE offerings ADD COLUMN is_custom INTEGER NOT NULL DEFAULT 0", [])?;
        }
        if !cols.contains(&"deity_id".into()) {
            self.conn.execute("ALTER TABLE offerings ADD COLUMN deity_id INTEGER NOT NULL DEFAULT 0", [])?;
        }
        if !cols.contains(&"moon_phase".into()) {
            self.conn.execute("ALTER TABLE offerings ADD COLUMN moon_phase TEXT NOT NULL DEFAULT 'any'", [])?;
        }
        if !cols.contains(&"best_time".into()) {
            self.conn.execute("ALTER TABLE offerings ADD COLUMN best_time TEXT NOT NULL DEFAULT ''", [])?;
        }
        if !cols.contains(&"warnings".into()) {
            self.conn.execute("ALTER TABLE offerings ADD COLUMN warnings TEXT NOT NULL DEFAULT ''", [])?;
        }
        if !cols.contains(&"source_note".into()) {
            self.conn.execute("ALTER TABLE offerings ADD COLUMN source_note TEXT NOT NULL DEFAULT ''", [])?;
        }
        Ok(())
    }

    fn seed_deck(conn: &mut Connection) -> SqlResult<()> {
        let tx = conn.transaction()?;
        let mut stmt = tx.prepare(
            "INSERT INTO cards (
                id, name, arcana, suit, number, roman_numeral,
                upright_meaning, reversed_meaning, keywords, image_ref, tone,
                ctx_general, ctx_love_dating, ctx_love_single, ctx_love_relationship,
                ctx_career, ctx_money, ctx_health
            ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11,
                      ?12, ?13, ?14, ?15, ?16, ?17, ?18)"
        )?;

        for card in build_deck() {
            stmt.execute(rusqlite::params![
                card.id,
                card.name,
                card.arcana.to_string(),
                card.suit.to_string(),
                card.number.map(|n| n.0),
                card.roman_numeral,
                card.upright_meaning,
                card.reversed_meaning,
                keywords_csv(&card.keywords),
                card.image_ref,
                card.tone.to_string(),
                card.contexts.general,
                card.contexts.love_dating,
                card.contexts.love_single,
                card.contexts.love_relationship,
                card.contexts.career,
                card.contexts.money,
                card.contexts.health,
            ])?;
        }
        stmt.finalize()?;
        tx.commit()?;
        Ok(())
    }

    fn seed_entities(&mut self) -> SqlResult<()> {
        let tx = self.conn.transaction()?;
        let mut stmt = tx.prepare(
            "INSERT INTO entities (
                id, name, entity_type, origin, description, signs, weaknesses, banishment, danger, tone, is_custom
            ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, 0)
            ON CONFLICT(id) DO UPDATE SET
                name = excluded.name,
                entity_type = excluded.entity_type,
                origin = excluded.origin,
                description = excluded.description,
                signs = excluded.signs,
                weaknesses = excluded.weaknesses,
                banishment = excluded.banishment,
                danger = excluded.danger,
                tone = excluded.tone,
                is_custom = 0
            WHERE entities.is_custom = 0"
        )?;
        let seed_ids: Vec<i32> = build_entities().iter().map(|e| e.id).collect();
        let max_seed_id = seed_ids.iter().copied().max().unwrap_or(0);
        for e in build_entities() {
            stmt.execute(rusqlite::params![
                e.id,
                e.name,
                e.entity_type.to_string(),
                e.origin,
                e.description,
                keywords_csv(&e.signs),
                keywords_csv(&e.weaknesses),
                e.banishment,
                e.danger,
                e.tone.to_string(),
            ])?;
        }
        stmt.finalize()?;
        // Respect user-deleted built-ins: remove rows recorded in deleted_builtins.
        // Done AFTER the inserts so entries deleted before a backup/restore stay gone.
        tx.execute(
            "DELETE FROM entities WHERE is_custom = 0 AND id IN (SELECT id FROM deleted_builtins WHERE kind = 'entity')",
            [],
        )?;
        // Drop built-in rows whose id is no longer in the current seed set,
        // but only if they are not custom user entries.
        if !seed_ids.is_empty() {
            let placeholders: String = seed_ids.iter().map(|_| "?").collect::<Vec<_>>().join(",");
            let sql = format!(
                "DELETE FROM entities WHERE is_custom = 0 AND id NOT IN ({}) AND id <= ?",
                placeholders
            );
            let mut params: Vec<rusqlite::types::Value> = seed_ids
                .into_iter()
                .map(rusqlite::types::Value::from)
                .collect();
            params.push(rusqlite::types::Value::from(max_seed_id));
            tx.execute(&sql, rusqlite::params_from_iter(params.iter()))?;
        }
        tx.commit()?;
        Ok(())
    }

    fn seed_spells(&mut self) -> SqlResult<()> {
        let tx = self.conn.transaction()?;
        tx.execute("DELETE FROM spells WHERE is_custom = 0 AND id IN (SELECT id FROM deleted_builtins WHERE kind = 'spell')", [])?;
        let mut stmt = tx.prepare(
            "INSERT OR IGNORE INTO spells (
                id, name, category, purpose, difficulty, risk_rating, moon_phase, ingredients, steps, warnings, related_entity_ids, source_note
            ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12)"
        )?;
        for s in build_spells() {
            stmt.execute(rusqlite::params![
                s.id,
                s.name,
                s.category.to_string(),
                s.purpose,
                s.difficulty,
                s.risk_rating,
                s.moon_phase.to_string(),
                keywords_csv(&s.ingredients),
                keywords_csv(&s.steps),
                keywords_csv(&s.warnings),
                s.related_entity_ids.iter().map(|i| i.to_string()).collect::<Vec<_>>().join(","),
                s.source_note,
            ])?;
        }
        stmt.finalize()?;
        tx.execute(
            "DELETE FROM spells WHERE is_custom = 0 AND id IN (SELECT id FROM deleted_builtins WHERE kind = 'spell')",
            [],
        )?;
        tx.commit()?;
        Ok(())
    }

    fn sync_spells(&mut self) -> SqlResult<()> {
        let tx = self.conn.transaction()?;
        let mut stmt = tx.prepare(
            "INSERT INTO spells (
                id, name, category, purpose, difficulty, risk_rating, moon_phase, ingredients, steps, warnings, related_entity_ids, source_note, is_custom
            ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, 0)
            ON CONFLICT(id) DO UPDATE SET
                name = excluded.name,
                category = excluded.category,
                purpose = excluded.purpose,
                difficulty = excluded.difficulty,
                risk_rating = excluded.risk_rating,
                moon_phase = excluded.moon_phase,
                ingredients = excluded.ingredients,
                steps = excluded.steps,
                warnings = excluded.warnings,
                related_entity_ids = excluded.related_entity_ids,
                source_note = excluded.source_note,
                is_custom = 0
            WHERE spells.is_custom = 0"
        )?;
        let seed_ids: Vec<i32> = build_spells().iter().map(|s| s.id).collect();
        let max_seed_id = seed_ids.iter().copied().max().unwrap_or(0);
        for s in build_spells() {
            stmt.execute(rusqlite::params![
                s.id,
                s.name,
                s.category.to_string(),
                s.purpose,
                s.difficulty,
                s.risk_rating,
                s.moon_phase.to_string(),
                keywords_csv(&s.ingredients),
                keywords_csv(&s.steps),
                keywords_csv(&s.warnings),
                s.related_entity_ids.iter().map(|i| i.to_string()).collect::<Vec<_>>().join(","),
                s.source_note,
            ])?;
        }
        stmt.finalize()?;
        tx.execute(
            "DELETE FROM spells WHERE is_custom = 0 AND id IN (SELECT id FROM deleted_builtins WHERE kind = 'spell')",
            [],
        )?;
        if !seed_ids.is_empty() {
            let placeholders: String = seed_ids.iter().map(|_| "?").collect::<Vec<_>>().join(",");
            let sql = format!(
                "DELETE FROM spells WHERE is_custom = 0 AND id NOT IN ({}) AND id <= ?",
                placeholders
            );
            let mut params: Vec<rusqlite::types::Value> = seed_ids
                .into_iter()
                .map(rusqlite::types::Value::from)
                .collect();
            params.push(rusqlite::types::Value::from(max_seed_id));
            tx.execute(&sql, rusqlite::params_from_iter(params.iter()))?;
        }
        tx.commit()?;
        Ok(())
    }

    fn seed_deities(&mut self) -> SqlResult<()> {
        let tx = self.conn.transaction()?;
        let mut replace_stmt = tx.prepare(
            "INSERT INTO deities (
                id, name, wiki_title, primary_religion, alignment, domains, description, versions, is_custom
            ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, 0)
            ON CONFLICT(id) DO UPDATE SET
                name = excluded.name,
                wiki_title = excluded.wiki_title,
                primary_religion = excluded.primary_religion,
                alignment = excluded.alignment,
                domains = excluded.domains,
                description = excluded.description,
                versions = excluded.versions
            WHERE deities.is_custom = 0"
        )?;
        for d in build_deities() {
            replace_stmt.execute(rusqlite::params![
                d.id,
                d.name,
                d.wiki_title,
                d.primary_religion,
                d.alignment,
                d.domains.join(","),
                d.description,
                serde_json::to_string(&d.versions).unwrap_or_default(),
            ])?;
        }
        replace_stmt.finalize()?;
        // Respect user-deleted built-ins: remove rows recorded in deleted_builtins.
        tx.execute(
            "DELETE FROM deities WHERE is_custom = 0 AND id IN (SELECT id FROM deleted_builtins WHERE kind = 'deity')",
            [],
        )?;
        // Drop any built-in rows whose id is no longer in the current seed set,
        // but only if they are not custom user entries.
        let seed_ids: Vec<i32> = build_deities().iter().map(|d| d.id).collect();
        let placeholders: String = seed_ids.iter().map(|_| "?").collect::<Vec<_>>().join(",");
        let sql = format!(
            "DELETE FROM deities WHERE is_custom = 0 AND id NOT IN ({}) AND id <= 206",
            placeholders
        );
        tx.execute(&sql, rusqlite::params_from_iter(seed_ids.iter()))?;
        tx.commit()?;
        Ok(())
    }

    fn seed_offerings(&mut self) -> SqlResult<()> {
        let tx = self.conn.transaction()?;
        let mut stmt = tx.prepare(
            "INSERT OR IGNORE INTO offerings (
                id, name, deity_id, religion, items, instructions, purpose, moon_phase, best_time, warnings, source_note
            ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11)"
        )?;
        for o in build_offerings() {
            stmt.execute(rusqlite::params![
                o.id,
                o.name,
                o.deity_id,
                o.religion,
                keywords_csv(&o.items),
                o.instructions,
                o.purpose,
                o.moon_phase.to_string(),
                o.best_time,
                keywords_csv(&o.warnings),
                o.source_note,
            ])?;
        }
        stmt.finalize()?;
        tx.commit()?;
        Ok(())
    }

    fn seed_staves(&mut self) -> SqlResult<()> {
        let tx = self.conn.transaction()?;
        let mut stmt = tx.prepare(
            "INSERT INTO staves (
                id, name, icelandic_name, meaning, purpose, category, visual_notes, image_ref, is_custom
            ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, 0)
            ON CONFLICT(id) DO UPDATE SET
                name = excluded.name,
                icelandic_name = excluded.icelandic_name,
                meaning = excluded.meaning,
                purpose = excluded.purpose,
                category = excluded.category,
                visual_notes = excluded.visual_notes,
                image_ref = excluded.image_ref
            WHERE staves.is_custom = 0"
        )?;
        for s in build_staves() {
            stmt.execute(rusqlite::params![
                s.id,
                s.name,
                s.icelandic_name,
                s.meaning,
                s.purpose,
                s.category,
                s.visual_notes,
                s.image_ref,
            ])?;
        }
        stmt.finalize()?;
        let seed_ids: Vec<i32> = build_staves().iter().map(|s| s.id).collect();
        let placeholders: String = seed_ids.iter().map(|_| "?").collect::<Vec<_>>().join(",");
        let sql = format!(
            "DELETE FROM staves WHERE is_custom = 0 AND id NOT IN ({})",
            placeholders
        );
        tx.execute(&sql, rusqlite::params_from_iter(seed_ids.iter()))?;
        tx.commit()?;
        Ok(())
    }

    pub fn get_all_staves(&self) -> SqlResult<Vec<Stave>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, icelandic_name, meaning, purpose, category, visual_notes, image_ref, is_custom FROM staves ORDER BY name"
        )?;
        let rows = stmt.query_map([], |row| {
            Ok(Stave {
                id: row.get(0)?,
                name: row.get(1)?,
                icelandic_name: row.get(2)?,
                meaning: row.get(3)?,
                purpose: row.get(4)?,
                category: row.get(5)?,
                visual_notes: row.get(6)?,
                image_ref: row.get(7)?,
                is_custom: row.get::<_, i32>(8)? != 0,
            })
        })?;
        rows.collect()
    }

    pub fn get_stave(&self, id: i32) -> SqlResult<Option<Stave>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, icelandic_name, meaning, purpose, category, visual_notes, image_ref, is_custom FROM staves WHERE id = ?1"
        )?;
        let mut rows = stmt.query_map([id], |row| {
            Ok(Stave {
                id: row.get(0)?,
                name: row.get(1)?,
                icelandic_name: row.get(2)?,
                meaning: row.get(3)?,
                purpose: row.get(4)?,
                category: row.get(5)?,
                visual_notes: row.get(6)?,
                image_ref: row.get(7)?,
                is_custom: row.get::<_, i32>(8)? != 0,
            })
        })?;
        Ok(rows.next().transpose()?)
    }

    pub fn get_staves_by_category(&self, category: &str) -> SqlResult<Vec<Stave>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, icelandic_name, meaning, purpose, category, visual_notes, image_ref, is_custom FROM staves WHERE category = ?1 ORDER BY name"
        )?;
        let rows = stmt.query_map([category], |row| {
            Ok(Stave {
                id: row.get(0)?,
                name: row.get(1)?,
                icelandic_name: row.get(2)?,
                meaning: row.get(3)?,
                purpose: row.get(4)?,
                category: row.get(5)?,
                visual_notes: row.get(6)?,
                image_ref: row.get(7)?,
                is_custom: row.get::<_, i32>(8)? != 0,
            })
        })?;
        rows.collect()
    }

    pub fn update_stave(&mut self, id: i32, stave: &Stave) -> SqlResult<()> {
        self.conn.execute(
            "UPDATE staves SET name = ?1, icelandic_name = ?2, meaning = ?3, purpose = ?4, category = ?5, visual_notes = ?6, image_ref = ?7 WHERE id = ?8",
            [
                &stave.name, &stave.icelandic_name, &stave.meaning, &stave.purpose,
                &stave.category, &stave.visual_notes, &stave.image_ref, &id.to_string()
            ]
        )?;
        Ok(())
    }

    pub fn search_staves(&self, query: &str) -> SqlResult<Vec<Stave>> {
        let pattern = format!("%{}%", query.replace('%', r"\%").replace('_', r"\_"));
        let mut stmt = self.conn.prepare(
            "SELECT id, name, icelandic_name, meaning, purpose, category, visual_notes, image_ref, is_custom FROM staves WHERE name LIKE ?1 ESCAPE '\\' OR icelandic_name LIKE ?1 ESCAPE '\\' OR meaning LIKE ?1 ESCAPE '\\' OR purpose LIKE ?1 ESCAPE '\\' ORDER BY name"
        )?;
        let rows = stmt.query_map([&pattern], |row| {
            Ok(Stave {
                id: row.get(0)?,
                name: row.get(1)?,
                icelandic_name: row.get(2)?,
                meaning: row.get(3)?,
                purpose: row.get(4)?,
                category: row.get(5)?,
                visual_notes: row.get(6)?,
                image_ref: row.get(7)?,
                is_custom: row.get::<_, i32>(8)? != 0,
            })
        })?;
        rows.collect()
    }

    fn row_to_db_card(row: &rusqlite::Row) -> SqlResult<DbCard> {
        Ok(DbCard {
            id: row.get(0)?,
            name: row.get(1)?,
            arcana: row.get(2)?,
            suit: row.get(3)?,
            number: row.get(4)?,
            roman_numeral: row.get(5)?,
            upright_meaning: row.get(6)?,
            reversed_meaning: row.get(7)?,
            keywords: row.get(8)?,
            image_ref: row.get(9)?,
            tone: row.get(10)?,
            ctx_general: row.get(11)?,
            ctx_love_dating: row.get(12)?,
            ctx_love_single: row.get(13)?,
            ctx_love_relationship: row.get(14)?,
            ctx_career: row.get(15)?,
            ctx_money: row.get(16)?,
            ctx_health: row.get(17)?,
        })
    }

    fn parse_tone(s: &str) -> Tone {
        match s.to_lowercase().as_str() {
            "positive" => Tone::Positive,
            "negative" => Tone::Negative,
            _ => Tone::Neutral,
        }
    }

    fn db_card_to_card(c: DbCard) -> Card {
        let number = c.number.map(CardNumber);
        let arcana = parse_arcana(&c.arcana);
        let suit = parse_suit(&c.suit);
        Card {
            id: c.id,
            name: c.name,
            arcana,
            suit,
            number,
            roman_numeral: c.roman_numeral,
            upright_meaning: c.upright_meaning,
            reversed_meaning: c.reversed_meaning,
            keywords: parse_keywords(&c.keywords),
            image_ref: c.image_ref,
            tone: Self::parse_tone(&c.tone),
            contexts: ContextualMeanings {
                general: c.ctx_general,
                love_dating: c.ctx_love_dating,
                love_single: c.ctx_love_single,
                love_relationship: c.ctx_love_relationship,
                career: c.ctx_career,
                money: c.ctx_money,
                health: c.ctx_health,
            },
        }
    }

    pub fn get_all_cards(&self) -> SqlResult<Vec<Card>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, arcana, suit, number, roman_numeral,
                    upright_meaning, reversed_meaning, keywords, image_ref, tone,
                    ctx_general, ctx_love_dating, ctx_love_single, ctx_love_relationship,
                    ctx_career, ctx_money, ctx_health
             FROM cards ORDER BY id"
        )?;
        let db_cards = stmt
            .query_map([], Self::row_to_db_card)?
            .collect::<SqlResult<Vec<DbCard>>>()?;
        Ok(db_cards.into_iter().map(Self::db_card_to_card).collect())
    }

    pub fn get_card(&self, id: i32) -> SqlResult<Option<Card>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, arcana, suit, number, roman_numeral,
                    upright_meaning, reversed_meaning, keywords, image_ref, tone,
                    ctx_general, ctx_love_dating, ctx_love_single, ctx_love_relationship,
                    ctx_career, ctx_money, ctx_health
             FROM cards WHERE id = ?1 LIMIT 1"
        )?;
        let result = stmt.query_row([id], Self::row_to_db_card);
        match result {
            Ok(db) => Ok(Some(Self::db_card_to_card(db))),
            Err(rusqlite::Error::QueryReturnedNoRows) => Ok(None),
            Err(e) => Err(e),
        }
    }

    pub fn get_cards_by_arcana_or_suit(
        &self,
        arcana: Option<Arcana>,
        suit: Option<Suit>,
    ) -> SqlResult<Vec<Card>> {
        let mut sql = String::from(
            "SELECT id, name, arcana, suit, number, roman_numeral,
                    upright_meaning, reversed_meaning, keywords, image_ref, tone,
                    ctx_general, ctx_love_dating, ctx_love_single, ctx_love_relationship,
                    ctx_career, ctx_money, ctx_health
             FROM cards WHERE 1=1"
        );
        let mut params: Vec<Box<dyn rusqlite::ToSql>> = Vec::new();
        if let Some(a) = arcana {
            sql.push_str(" AND arcana = ?");
            params.push(Box::new(a.to_string()));
        }
        if let Some(s) = suit {
            sql.push_str(" AND suit = ?");
            params.push(Box::new(s.to_string()));
        }
        sql.push_str(" ORDER BY id");
        let mut stmt = self.conn.prepare(&sql)?;
        let refs: Vec<&dyn rusqlite::ToSql> = params.iter().map(|p| p.as_ref()).collect();
        let db_cards = stmt
            .query_map(&*refs.as_slice(), Self::row_to_db_card)?
            .collect::<SqlResult<Vec<DbCard>>>()?;
        Ok(db_cards.into_iter().map(Self::db_card_to_card).collect())
    }

    pub fn get_setting(&self, key: &str) -> SqlResult<Option<String>> {
        let mut stmt = self
            .conn
            .prepare("SELECT value FROM settings WHERE key = ?1 LIMIT 1")?;
        match stmt.query_row([key], |row| {
            let v: String = row.get(0)?;
            Ok(v)
        }) {
            Ok(v) => Ok(Some(v)),
            Err(rusqlite::Error::QueryReturnedNoRows) => Ok(None),
            Err(e) => Err(e),
        }
    }

    pub fn set_setting(&mut self, key: &str, value: &str) -> SqlResult<()> {
        self.conn.execute(
            "INSERT INTO settings (key, value) VALUES (?1, ?2)
             ON CONFLICT(key) DO UPDATE SET value = excluded.value",
            [key, value],
        )?;
        Ok(())
    }

    pub fn get_all_entities(&self) -> SqlResult<Vec<Entity>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, entity_type, origin, description, signs, weaknesses, banishment, danger, tone, is_custom
             FROM entities ORDER BY id"
        )?;
        let rows = stmt.query_map([], Self::row_to_entity)?;
        rows.collect::<SqlResult<Vec<Entity>>>()
    }

    pub fn get_entity(&self, id: i32) -> SqlResult<Option<Entity>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, entity_type, origin, description, signs, weaknesses, banishment, danger, tone, is_custom
             FROM entities WHERE id = ?1 LIMIT 1"
        )?;
        let result = stmt.query_row([id], Self::row_to_entity);
        match result {
            Ok(e) => Ok(Some(e)),
            Err(rusqlite::Error::QueryReturnedNoRows) => Ok(None),
            Err(e) => Err(e),
        }
    }

    pub fn get_entities_by_type(&self, entity_type: EntityType) -> SqlResult<Vec<Entity>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, entity_type, origin, description, signs, weaknesses, banishment, danger, tone, is_custom
             FROM entities WHERE entity_type = ?1 ORDER BY id"
        )?;
        let rows = stmt.query_map([entity_type.to_string()], Self::row_to_entity)?;
        rows.collect::<SqlResult<Vec<Entity>>>()
    }

    pub fn search_entities(&self, query: &str) -> SqlResult<Vec<Entity>> {
        let q = format!("%{}%", query);
        let mut stmt = self.conn.prepare(
            "SELECT id, name, entity_type, origin, description, signs, weaknesses, banishment, danger, tone, is_custom
             FROM entities WHERE name LIKE ?1 OR description LIKE ?1 OR origin LIKE ?1 ORDER BY id"
        )?;
        let rows = stmt.query_map([&q], Self::row_to_entity)?;
        rows.collect::<SqlResult<Vec<Entity>>>()
    }

    fn row_to_entity(row: &rusqlite::Row) -> SqlResult<Entity> {
        Ok(Entity {
            id: row.get(0)?,
            name: row.get(1)?,
            entity_type: parse_entity_type(&row.get::<_, String>(2)?),
            origin: row.get(3)?,
            description: row.get(4)?,
            signs: parse_keywords(&row.get::<_, String>(5)?),
            weaknesses: parse_keywords(&row.get::<_, String>(6)?),
            banishment: row.get(7)?,
            danger: row.get(8)?,
            tone: Self::parse_tone(&row.get::<_, String>(9)?),
            is_custom: row.get::<_, i32>(10)? != 0,
        })
    }

    pub fn add_entity(&mut self, entity: &Entity) -> SqlResult<i64> {
        let mut stmt = self.conn.prepare(
            "INSERT INTO entities (
                name, entity_type, origin, description, signs, weaknesses, banishment, danger, tone, is_custom
            ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, 1)"
        )?;
        stmt.execute(rusqlite::params![
            entity.name,
            entity.entity_type.to_string(),
            entity.origin,
            entity.description,
            keywords_csv(&entity.signs),
            keywords_csv(&entity.weaknesses),
            entity.banishment,
            entity.danger,
            entity.tone.to_string(),
        ])?;
        Ok(self.conn.last_insert_rowid())
    }

    pub fn update_entity(&mut self, id: i32, entity: &Entity) -> SqlResult<()> {
        self.conn.execute(
            "UPDATE entities SET
                name = ?1,
                entity_type = ?2,
                origin = ?3,
                description = ?4,
                signs = ?5,
                weaknesses = ?6,
                banishment = ?7,
                danger = ?8,
                tone = ?9,
                is_custom = 1
             WHERE id = ?10",
            rusqlite::params![
                entity.name,
                entity.entity_type.to_string(),
                entity.origin,
                entity.description,
                keywords_csv(&entity.signs),
                keywords_csv(&entity.weaknesses),
                entity.banishment,
                entity.danger,
                entity.tone.to_string(),
                id,
            ],
        )?;
        Ok(())
    }

    pub fn delete_entity(&mut self, id: i32) -> SqlResult<()> {
        self.conn.execute("DELETE FROM entities WHERE id = ?1", [id])?;
        Ok(())
    }

    pub fn delete_entities(&mut self, ids: &[i32]) -> SqlResult<()> {
        let tx = self.conn.transaction()?;
        for id in ids {
            tx.execute("DELETE FROM entities WHERE id = ?1", [id])?;
            tx.execute(
                "INSERT OR IGNORE INTO deleted_builtins (kind, id) VALUES ('entity', ?1)",
                [id],
            )?;
        }
        tx.commit()?;
        Ok(())
    }

    pub fn get_app_setting(&self, key: &str) -> SqlResult<Option<String>> {
        let mut stmt = self.conn.prepare(
            "SELECT value FROM app_settings WHERE key = ?1 LIMIT 1"
        )?;
        let result = stmt.query_row([key], |row| {
            let value: String = row.get(0)?;
            Ok(value)
        });
        match result {
            Ok(v) => Ok(Some(v)),
            Err(rusqlite::Error::QueryReturnedNoRows) => Ok(None),
            Err(e) => Err(e),
        }
    }

    pub fn set_app_setting(&mut self, key: &str, value: &str) -> SqlResult<()> {
        self.conn.execute(
            "INSERT INTO app_settings (key, value) VALUES (?1, ?2)
             ON CONFLICT(key) DO UPDATE SET value = excluded.value",
            [key, value],
        )?;
        Ok(())
    }

    fn row_to_spell(row: &rusqlite::Row) -> SqlResult<Spell> {
        Ok(Spell {
            id: row.get(0)?,
            name: row.get(1)?,
            category: parse_spell_category(&row.get::<_, String>(2)?),
            purpose: row.get(3)?,
            difficulty: row.get(4)?,
            risk_rating: row.get(5)?,
            moon_phase: parse_moon_phase_requirement(&row.get::<_, String>(6)?),
            ingredients: parse_keywords(&row.get::<_, String>(7)?),
            steps: parse_keywords(&row.get::<_, String>(8)?),
            warnings: parse_keywords(&row.get::<_, String>(9)?),
            related_entity_ids: parse_id_list(&row.get::<_, String>(10)?),
            source_note: row.get(11)?,
            is_custom: row.get::<_, i32>(12)? != 0,
        })
    }

    pub fn get_all_spells(&self) -> SqlResult<Vec<Spell>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, category, purpose, difficulty, risk_rating, moon_phase, ingredients, steps, warnings, related_entity_ids, source_note, is_custom
             FROM spells ORDER BY id"
        )?;
        let rows = stmt.query_map([], Self::row_to_spell)?;
        rows.collect::<SqlResult<Vec<Spell>>>()
    }

    pub fn get_spells_by_category(&self, category: SpellCategory) -> SqlResult<Vec<Spell>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, category, purpose, difficulty, risk_rating, moon_phase, ingredients, steps, warnings, related_entity_ids, source_note, is_custom
             FROM spells WHERE category = ?1 ORDER BY id"
        )?;
        let rows = stmt.query_map([category.to_string()], Self::row_to_spell)?;
        rows.collect::<SqlResult<Vec<Spell>>>()
    }

    pub fn search_spells(&self, query: &str) -> SqlResult<Vec<Spell>> {
        let q = format!("%{}%", query.to_lowercase());
        let mut stmt = self.conn.prepare(
            "SELECT id, name, category, purpose, difficulty, risk_rating, moon_phase, ingredients, steps, warnings, related_entity_ids, source_note, is_custom
             FROM spells WHERE lower(name) LIKE ?1 OR lower(purpose) LIKE ?1 OR lower(ingredients) LIKE ?1
             ORDER BY id"
        )?;
        let rows = stmt.query_map([q], Self::row_to_spell)?;
        rows.collect::<SqlResult<Vec<Spell>>>()
    }

    pub fn get_spell(&self, id: i32) -> SqlResult<Option<Spell>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, category, purpose, difficulty, risk_rating, moon_phase, ingredients, steps, warnings, related_entity_ids, source_note, is_custom
             FROM spells WHERE id = ?1 LIMIT 1"
        )?;
        let result = stmt.query_row([id], Self::row_to_spell);
        match result {
            Ok(s) => Ok(Some(s)),
            Err(rusqlite::Error::QueryReturnedNoRows) => Ok(None),
            Err(e) => Err(e),
        }
    }

    pub fn add_spell(&mut self, spell: &Spell) -> SqlResult<i64> {
        let mut stmt = self.conn.prepare(
            "INSERT INTO spells (
                name, category, purpose, difficulty, risk_rating, moon_phase, ingredients, steps, warnings, related_entity_ids, source_note, is_custom
            ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, 1)"
        )?;
        stmt.execute(rusqlite::params![
            spell.name,
            spell.category.to_string(),
            spell.purpose,
            spell.difficulty,
            spell.risk_rating,
            spell.moon_phase.to_string(),
            keywords_csv(&spell.ingredients),
            keywords_csv(&spell.steps),
            keywords_csv(&spell.warnings),
            spell.related_entity_ids.iter().map(|i| i.to_string()).collect::<Vec<_>>().join(","),
            spell.source_note,
        ])?;
        Ok(self.conn.last_insert_rowid())
    }

    pub fn delete_spell(&mut self, id: i32) -> SqlResult<()> {
        self.conn.execute("DELETE FROM spells WHERE id = ?1", [id])?;
        Ok(())
    }

    pub fn delete_spells(&mut self, ids: &[i32]) -> SqlResult<()> {
        let tx = self.conn.transaction()?;
        for id in ids {
            tx.execute("DELETE FROM spells WHERE id = ?1", [id])?;
            tx.execute(
                "INSERT OR IGNORE INTO deleted_builtins (kind, id) VALUES ('spell', ?1)",
                [id],
            )?;
        }
        tx.commit()?;
        Ok(())
    }

    pub fn update_spell(&mut self, id: i32, spell: &Spell) -> SqlResult<()> {
        self.conn.execute(
            "UPDATE spells SET
                name = ?1,
                category = ?2,
                purpose = ?3,
                difficulty = ?4,
                risk_rating = ?5,
                moon_phase = ?6,
                ingredients = ?7,
                steps = ?8,
                warnings = ?9,
                related_entity_ids = ?10,
                source_note = ?11,
                is_custom = 1
             WHERE id = ?12",
            rusqlite::params![
                spell.name,
                spell.category.to_string(),
                spell.purpose,
                spell.difficulty,
                spell.risk_rating,
                spell.moon_phase.to_string(),
                keywords_csv(&spell.ingredients),
                keywords_csv(&spell.steps),
                keywords_csv(&spell.warnings),
                spell.related_entity_ids.iter().map(|i| i.to_string()).collect::<Vec<_>>().join(","),
                spell.source_note,
                id,
            ],
        )?;
        Ok(())
    }

    pub fn get_all_deities(&self) -> SqlResult<Vec<Deity>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, wiki_title, primary_religion, alignment, domains, description, versions FROM deities ORDER BY id"
        )?;
        let rows = stmt.query_map([], |row| {
            Ok(Deity {
                id: row.get(0)?,
                name: row.get(1)?,
                wiki_title: row.get(2)?,
                primary_religion: row.get(3)?,
                alignment: row.get(4)?,
                domains: parse_keywords(&row.get::<_, String>(5)?),
                description: row.get(6)?,
                versions: serde_json::from_str(&row.get::<_, String>(7)?).unwrap_or_default(),
                is_custom: false,
            })
        })?;
        rows.collect::<SqlResult<Vec<Deity>>>()
    }

    pub fn get_deities_by_religion(&self, religion: &str) -> SqlResult<Vec<Deity>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, wiki_title, primary_religion, alignment, domains, description, versions FROM deities WHERE primary_religion = ?1 ORDER BY id"
        )?;
        let rows = stmt.query_map([religion], |row| {
            Ok(Deity {
                id: row.get(0)?,
                name: row.get(1)?,
                wiki_title: row.get(2)?,
                primary_religion: row.get(3)?,
                alignment: row.get(4)?,
                domains: parse_keywords(&row.get::<_, String>(5)?),
                description: row.get(6)?,
                versions: serde_json::from_str(&row.get::<_, String>(7)?).unwrap_or_default(),
                is_custom: false,
            })
        })?;
        rows.collect::<SqlResult<Vec<Deity>>>()
    }

    pub fn search_deities(&self, query: &str) -> SqlResult<Vec<Deity>> {
        let q = format!("%{}%", query.to_lowercase());
        let mut stmt = self.conn.prepare(
            "SELECT id, name, wiki_title, primary_religion, alignment, domains, description, versions
             FROM deities WHERE lower(name) LIKE ?1 OR lower(description) LIKE ?1 OR lower(domains) LIKE ?1
             ORDER BY id"
        )?;
        let rows = stmt.query_map([q], |row| {
            Ok(Deity {
                id: row.get(0)?,
                name: row.get(1)?,
                wiki_title: row.get(2)?,
                primary_religion: row.get(3)?,
                alignment: row.get(4)?,
                domains: parse_keywords(&row.get::<_, String>(5)?),
                description: row.get(6)?,
                versions: serde_json::from_str(&row.get::<_, String>(7)?).unwrap_or_default(),
                is_custom: false,
            })
        })?;
        rows.collect::<SqlResult<Vec<Deity>>>()
    }

    pub fn get_deity(&self, id: i32) -> SqlResult<Option<Deity>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, wiki_title, primary_religion, alignment, domains, description, versions FROM deities WHERE id = ?1 LIMIT 1"
        )?;
        let result = stmt.query_row([id], |row| {
            Ok(Deity {
                id: row.get(0)?,
                name: row.get(1)?,
                wiki_title: row.get(2)?,
                primary_religion: row.get(3)?,
                alignment: row.get(4)?,
                domains: parse_keywords(&row.get::<_, String>(5)?),
                description: row.get(6)?,
                versions: serde_json::from_str(&row.get::<_, String>(7)?).unwrap_or_default(),
                is_custom: false,
            })
        });
        match result {
            Ok(d) => Ok(Some(d)),
            Err(rusqlite::Error::QueryReturnedNoRows) => Ok(None),
            Err(e) => Err(e),
        }
    }

    pub fn add_deity(&mut self, deity: &Deity) -> SqlResult<i64> {
        let mut stmt = self.conn.prepare(
            "INSERT INTO deities (
                name, wiki_title, primary_religion, alignment, domains, description, versions, is_custom
            ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, 1)"
        )?;
        stmt.execute(rusqlite::params![
            deity.name,
            deity.wiki_title,
            deity.primary_religion,
            deity.alignment,
            deity.domains.join(","),
            deity.description,
            serde_json::to_string(&deity.versions).unwrap_or_default(),
        ])?;
        Ok(self.conn.last_insert_rowid())
    }

    pub fn update_deity(&mut self, id: i32, deity: &Deity) -> SqlResult<()> {
        self.conn.execute(
            "UPDATE deities SET
                name = ?1,
                wiki_title = ?2,
                primary_religion = ?3,
                alignment = ?4,
                domains = ?5,
                description = ?6,
                versions = ?7,
                is_custom = 1
             WHERE id = ?8",
            rusqlite::params![
                deity.name,
                deity.wiki_title,
                deity.primary_religion,
                deity.alignment,
                deity.domains.join(","),
                deity.description,
                serde_json::to_string(&deity.versions).unwrap_or_default(),
                id,
            ],
        )?;
        Ok(())
    }

    pub fn delete_deity(&mut self, id: i32) -> SqlResult<()> {
        self.conn.execute("DELETE FROM deities WHERE id = ?1", [id])?;
        Ok(())
    }

    pub fn delete_deities(&mut self, ids: &[i32]) -> SqlResult<()> {
        let tx = self.conn.transaction()?;
        for id in ids {
            tx.execute("DELETE FROM deities WHERE id = ?1", [id])?;
            tx.execute(
                "INSERT OR IGNORE INTO deleted_builtins (kind, id) VALUES ('deity', ?1)",
                [id],
            )?;
        }
        tx.commit()?;
        Ok(())
    }

    /// Clear all recorded built-in deletions and re-run the seed-sync so the
    /// built-in rows come back. Custom rows are never touched, and built-ins
    /// reseed by id, so nothing duplicates.
    pub fn restore_builtin_deletions(&mut self) -> SqlResult<()> {
        self.conn.execute("DELETE FROM deleted_builtins", [])?;
        self.seed_entities()?;
        self.sync_spells()?;
        self.seed_deities()?;
        self.seed_astro()?;
        Ok(())
    }

    fn seed_astro(&mut self) -> SqlResult<()> {
        let tx = self.conn.transaction()?;

        // Signs: upsert semantics — inserts missing, updates non-custom rows,
        // removes rows no longer in the seed set, skips user deletions.
        {
            let mut stmt = tx.prepare(
                "INSERT INTO astro_signs (
                    id, name, dates, element, modality, ruler, symbol, traits, body_part, description, compatibility, magic_notes, is_custom
                ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, 0)
                ON CONFLICT(id) DO UPDATE SET
                    name = excluded.name,
                    dates = excluded.dates,
                    element = excluded.element,
                    modality = excluded.modality,
                    ruler = excluded.ruler,
                    symbol = excluded.symbol,
                    traits = excluded.traits,
                    body_part = excluded.body_part,
                    description = excluded.description,
                    compatibility = excluded.compatibility,
                    magic_notes = excluded.magic_notes,
                    is_custom = 0
                WHERE astro_signs.is_custom = 0"
            )?;
            let seed_ids: Vec<i32> = build_signs().iter().map(|s| s.id).collect();
            for s in build_signs() {
                stmt.execute(rusqlite::params![
                    s.id,
                    s.name,
                    s.dates,
                    s.element,
                    s.modality,
                    s.ruler,
                    s.symbol,
                    keywords_csv(&s.traits),
                    s.body_part,
                    s.description,
                    keywords_csv(&s.compatibility),
                    s.magic_notes,
                ])?;
            }
            stmt.finalize()?;
            if !seed_ids.is_empty() {
                let placeholders: String = seed_ids.iter().map(|_| "?").collect::<Vec<_>>().join(",");
                let max_id = seed_ids.iter().copied().max().unwrap_or(0);
                let sql = format!(
                    "DELETE FROM astro_signs WHERE is_custom = 0 AND id NOT IN ({}) AND id <= ? AND id NOT IN (SELECT id FROM deleted_builtins WHERE kind = 'astro_sign')",
                    placeholders
                );
                let mut params: Vec<rusqlite::types::Value> = seed_ids
                    .into_iter()
                    .map(rusqlite::types::Value::from)
                    .collect();
                params.push(rusqlite::types::Value::from(max_id));
                tx.execute(&sql, rusqlite::params_from_iter(params.iter()))?;
            }
        }

        // Bodies.
        {
            let mut stmt = tx.prepare(
                "INSERT INTO astro_bodies (
                    id, name, symbol, day, domain, description, color_note, is_custom
                ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, 0)
                ON CONFLICT(id) DO UPDATE SET
                    name = excluded.name,
                    symbol = excluded.symbol,
                    day = excluded.day,
                    domain = excluded.domain,
                    description = excluded.description,
                    color_note = excluded.color_note,
                    is_custom = 0
                WHERE astro_bodies.is_custom = 0"
            )?;
            let seed_ids: Vec<i32> = build_bodies().iter().map(|b| b.id).collect();
            for b in build_bodies() {
                stmt.execute(rusqlite::params![
                    b.id,
                    b.name,
                    b.symbol,
                    b.day,
                    b.domain,
                    b.description,
                    b.color_note,
                ])?;
            }
            stmt.finalize()?;
            if !seed_ids.is_empty() {
                let placeholders: String = seed_ids.iter().map(|_| "?").collect::<Vec<_>>().join(",");
                let max_id = seed_ids.iter().copied().max().unwrap_or(0);
                let sql = format!(
                    "DELETE FROM astro_bodies WHERE is_custom = 0 AND id NOT IN ({}) AND id <= ? AND id NOT IN (SELECT id FROM deleted_builtins WHERE kind = 'astro_body')",
                    placeholders
                );
                let mut params: Vec<rusqlite::types::Value> = seed_ids
                    .into_iter()
                    .map(rusqlite::types::Value::from)
                    .collect();
                params.push(rusqlite::types::Value::from(max_id));
                tx.execute(&sql, rusqlite::params_from_iter(params.iter()))?;
            }
        }

        tx.commit()?;
        Ok(())
    }

    fn row_to_astro_sign(row: &rusqlite::Row) -> SqlResult<AstroSign> {
        Ok(AstroSign {
            id: row.get(0)?,
            name: row.get(1)?,
            dates: row.get(2)?,
            element: row.get(3)?,
            modality: row.get(4)?,
            ruler: row.get(5)?,
            symbol: row.get(6)?,
            traits: parse_keywords(&row.get::<_, String>(7)?),
            body_part: row.get(8)?,
            description: row.get(9)?,
            compatibility: parse_keywords(&row.get::<_, String>(10)?),
            magic_notes: row.get(11)?,
            is_custom: row.get::<_, i32>(12)? != 0,
        })
    }

    fn row_to_astro_body(row: &rusqlite::Row) -> SqlResult<AstroBody> {
        Ok(AstroBody {
            id: row.get(0)?,
            name: row.get(1)?,
            symbol: row.get(2)?,
            day: row.get(3)?,
            domain: row.get(4)?,
            description: row.get(5)?,
            color_note: row.get(6)?,
            is_custom: row.get::<_, i32>(7)? != 0,
        })
    }

    pub fn get_all_astro_signs(&self) -> SqlResult<Vec<AstroSign>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, dates, element, modality, ruler, symbol, traits, body_part, description, compatibility, magic_notes, is_custom
             FROM astro_signs ORDER BY id"
        )?;
        let rows = stmt.query_map([], Self::row_to_astro_sign)?;
        rows.collect::<SqlResult<Vec<AstroSign>>>()
    }

    pub fn get_astro_sign(&self, id: i32) -> SqlResult<Option<AstroSign>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, dates, element, modality, ruler, symbol, traits, body_part, description, compatibility, magic_notes, is_custom
             FROM astro_signs WHERE id = ?1 LIMIT 1"
        )?;
        let result = stmt.query_row([id], Self::row_to_astro_sign);
        match result {
            Ok(s) => Ok(Some(s)),
            Err(rusqlite::Error::QueryReturnedNoRows) => Ok(None),
            Err(e) => Err(e),
        }
    }

    pub fn get_all_astro_bodies(&self) -> SqlResult<Vec<AstroBody>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, symbol, day, domain, description, color_note, is_custom
             FROM astro_bodies ORDER BY id"
        )?;
        let rows = stmt.query_map([], Self::row_to_astro_body)?;
        rows.collect::<SqlResult<Vec<AstroBody>>>()
    }

    pub fn get_astro_body(&self, id: i32) -> SqlResult<Option<AstroBody>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, symbol, day, domain, description, color_note, is_custom
             FROM astro_bodies WHERE id = ?1 LIMIT 1"
        )?;
        let result = stmt.query_row([id], Self::row_to_astro_body);
        match result {
            Ok(b) => Ok(Some(b)),
            Err(rusqlite::Error::QueryReturnedNoRows) => Ok(None),
            Err(e) => Err(e),
        }
    }

    pub fn delete_astro_entries(&mut self, sign_ids: &[i32], body_ids: &[i32]) -> SqlResult<()> {
        let tx = self.conn.transaction()?;
        for id in sign_ids {
            tx.execute("DELETE FROM astro_signs WHERE id = ?1", [id])?;
            tx.execute(
                "INSERT OR IGNORE INTO deleted_builtins (kind, id) VALUES ('astro_sign', ?1)",
                [id],
            )?;
        }
        for id in body_ids {
            tx.execute("DELETE FROM astro_bodies WHERE id = ?1", [id])?;
            tx.execute(
                "INSERT OR IGNORE INTO deleted_builtins (kind, id) VALUES ('astro_body', ?1)",
                [id],
            )?;
        }
        tx.commit()?;
        Ok(())
    }


    pub fn get_all_birth_profiles(&self) -> SqlResult<Vec<crate::models::BirthProfile>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, jd, latitude, longitude, timezone_offset_hours FROM birth_profiles ORDER BY id"
        )?;
        let rows = stmt.query_map([], |row| {
            Ok(crate::models::BirthProfile {
                id: row.get(0)?,
                name: row.get(1)?,
                jd: row.get(2)?,
                latitude: row.get(3)?,
                longitude: row.get(4)?,
                timezone_offset_hours: row.get(5)?,
                is_custom: true,
            })
        })?;
        rows.collect::<SqlResult<Vec<crate::models::BirthProfile>>>()
    }

    pub fn delete_birth_profile(&mut self, id: i32) -> SqlResult<()> {
        self.conn.execute("DELETE FROM birth_profiles WHERE id = ?1", [id])?;
        Ok(())
    }

    pub fn add_birth_profile(&mut self, p: &crate::models::BirthProfile) -> SqlResult<i64> {
        self.conn.execute(
            "INSERT INTO birth_profiles (name, jd, latitude, longitude, timezone_offset_hours) VALUES (?1, ?2, ?3, ?4, ?5)",
            rusqlite::params![p.name, p.jd, p.latitude, p.longitude, p.timezone_offset_hours],
        )?;
        Ok(self.conn.last_insert_rowid())
    }

    pub fn update_birth_profile(&mut self, id: i32, p: &crate::models::BirthProfile) -> SqlResult<()> {
        self.conn.execute(
            "UPDATE birth_profiles SET name = ?1, jd = ?2, latitude = ?3, longitude = ?4, timezone_offset_hours = ?5 WHERE id = ?6",
            rusqlite::params![p.name, p.jd, p.latitude, p.longitude, p.timezone_offset_hours, id],
        )?;
        Ok(())
    }

    pub fn compute_natal_chart_for_profile(&self, p: &crate::models::BirthProfile) -> crate::natal::NatalChart {
        crate::natal::compute_natal_chart(p.jd, p.latitude, p.longitude, p.timezone_offset_hours)
    }

    fn row_to_offering(row: &rusqlite::Row) -> SqlResult<Offering> {
        Ok(Offering {
            id: row.get(0)?,
            name: row.get(1)?,
            deity_id: row.get(2)?,
            religion: row.get(3)?,
            items: parse_keywords(&row.get::<_, String>(4)?),
            instructions: row.get(5)?,
            purpose: row.get(6)?,
            moon_phase: parse_moon_phase_requirement(&row.get::<_, String>(7)?),
            best_time: row.get(8)?,
            warnings: parse_keywords(&row.get::<_, String>(9)?),
            source_note: row.get(10)?,
            is_custom: row.get::<_, i32>(11)? != 0,
        })
    }

    pub fn get_all_offerings(&self) -> SqlResult<Vec<Offering>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, deity_id, religion, items, instructions, purpose, moon_phase, best_time, warnings, source_note, is_custom
             FROM offerings ORDER BY id"
        )?;
        let rows = stmt.query_map([], Self::row_to_offering)?;
        rows.collect::<SqlResult<Vec<Offering>>>()
    }

    pub fn get_offerings_by_deity(&self, deity_id: i32) -> SqlResult<Vec<Offering>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, deity_id, religion, items, instructions, purpose, moon_phase, best_time, warnings, source_note, is_custom
             FROM offerings WHERE deity_id = ?1 ORDER BY id"
        )?;
        let rows = stmt.query_map([deity_id], Self::row_to_offering)?;
        rows.collect::<SqlResult<Vec<Offering>>>()
    }

    pub fn get_offering(&self, id: i32) -> SqlResult<Option<Offering>> {
        let mut stmt = self.conn.prepare(
            "SELECT id, name, deity_id, religion, items, instructions, purpose, moon_phase, best_time, warnings, source_note, is_custom
             FROM offerings WHERE id = ?1 LIMIT 1"
        )?;
        let result = stmt.query_row([id], Self::row_to_offering);
        match result {
            Ok(o) => Ok(Some(o)),
            Err(rusqlite::Error::QueryReturnedNoRows) => Ok(None),
            Err(e) => Err(e),
        }
    }

    pub fn search_offerings(&self, query: &str) -> SqlResult<Vec<Offering>> {
        let q = format!("%{}%", query.to_lowercase());
        let mut stmt = self.conn.prepare(
            "SELECT id, name, deity_id, religion, items, instructions, purpose, moon_phase, best_time, warnings, source_note, is_custom
             FROM offerings WHERE lower(name) LIKE ?1 OR lower(purpose) LIKE ?1 OR lower(items) LIKE ?1
             ORDER BY id"
        )?;
        let rows = stmt.query_map([q], Self::row_to_offering)?;
        rows.collect::<SqlResult<Vec<Offering>>>()
    }

    pub fn add_offering(&mut self, offering: &Offering) -> SqlResult<i64> {
        let mut stmt = self.conn.prepare(
            "INSERT INTO offerings (
                name, deity_id, religion, items, instructions, purpose, moon_phase, best_time, warnings, source_note, is_custom
            ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, 1)"
        )?;
        stmt.execute(rusqlite::params![
            offering.name,
            offering.deity_id,
            offering.religion,
            keywords_csv(&offering.items),
            offering.instructions,
            offering.purpose,
            offering.moon_phase.to_string(),
            offering.best_time,
            keywords_csv(&offering.warnings),
            offering.source_note,
        ])?;
        Ok(self.conn.last_insert_rowid())
    }

    pub fn update_offering(&mut self, id: i32, offering: &Offering) -> SqlResult<()> {
        self.conn.execute(
            "UPDATE offerings SET
                name = ?1,
                deity_id = ?2,
                religion = ?3,
                items = ?4,
                instructions = ?5,
                purpose = ?6,
                moon_phase = ?7,
                best_time = ?8,
                warnings = ?9,
                source_note = ?10,
                is_custom = 1
             WHERE id = ?11",
            rusqlite::params![
                offering.name,
                offering.deity_id,
                offering.religion,
                keywords_csv(&offering.items),
                offering.instructions,
                offering.purpose,
                offering.moon_phase.to_string(),
                offering.best_time,
                keywords_csv(&offering.warnings),
                offering.source_note,
                id,
            ],
        )?;
        Ok(())
    }

    pub fn delete_offering(&mut self, id: i32) -> SqlResult<()> {
        self.conn.execute("DELETE FROM offerings WHERE id = ?1", [id])?;
        Ok(())
    }
}

fn parse_id_list(s: &str) -> Vec<i32> {
    s.split(',')
        .map(|x| x.trim())
        .filter(|x| !x.is_empty())
        .filter_map(|x| x.parse().ok())
        .collect()
}

fn now_secs() -> i64 {
    std::time::SystemTime::now()
        .duration_since(std::time::UNIX_EPOCH)
        .map(|d| d.as_secs() as i64)
        .unwrap_or(0)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::models::Arcana;

    #[test]
    fn in_memory_migrations_seed_deck() {
        let db = TarotDatabase::open_in_memory().unwrap();
        let cards = db.get_all_cards().unwrap();
        assert_eq!(cards.len(), 78);
    }

    #[test]
    fn get_card_by_id() {
        let db = TarotDatabase::open_in_memory().unwrap();
        let card = db.get_card(0).unwrap().expect("Fool should exist");
        assert!(card.name.to_lowercase().contains("fool"));
        assert_eq!(card.arcana, Arcana::Major);

        assert!(db.get_card(999).unwrap().is_none());
    }

    #[test]
    fn spreads_seeded_removed() {
        let db = TarotDatabase::open_in_memory().unwrap();
        assert!(db.get_all_cards().unwrap().len() == 78);
    }

    #[test]
    fn entities_seeded() {
        let db = TarotDatabase::open_in_memory().unwrap();
        let entities = db.get_all_entities().unwrap();
        assert!(!entities.is_empty());
    }

    #[test]
    fn spells_seeded() {
        let db = TarotDatabase::open_in_memory().unwrap();
        let spells = db.get_all_spells().unwrap();
        assert!(!spells.is_empty());
    }

    #[test]
    fn offerings_seeded() {
        let db = TarotDatabase::open_in_memory().unwrap();
        let offerings = db.get_all_offerings().unwrap();
        assert!(!offerings.is_empty());
    }
}
