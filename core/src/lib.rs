mod astro;
mod astro_data;
mod database;
mod deities;
mod deck;
mod entities;
mod models;
mod natal;
mod offerings;
mod spells;
mod staves;

use crate::database::TarotDatabase;
use crate::models::{Deity, Offering};
use std::sync::{Arc, Mutex};

pub use crate::models::{Arcana, Card, Deity as ExportedDeity, Entity, EntityType, MoonPhaseRequirement, Offering as ExportedOffering, Spell, SpellCategory, Stave, Suit};

#[derive(Debug, Clone)]
pub enum CoreError {
    NotFound,
    DatabaseError(String),
    InvalidInput(String),
}

impl std::fmt::Display for CoreError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            CoreError::NotFound => write!(f, "not found"),
            CoreError::DatabaseError(e) => write!(f, "database error: {e}"),
            CoreError::InvalidInput(e) => write!(f, "invalid input: {e}"),
        }
    }
}

impl std::error::Error for CoreError {}

pub struct TarotCore {
    db: Arc<Mutex<TarotDatabase>>,
}

impl TarotCore {
    pub fn open_in_memory() -> Result<Self, CoreError> {
        let db = TarotDatabase::open_in_memory().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        Ok(Self {
            db: Arc::new(Mutex::new(db)),
        })
    }

    pub fn open(path: &str) -> Result<Self, CoreError> {
        let db = TarotDatabase::open(path).map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        Ok(Self {
            db: Arc::new(Mutex::new(db)),
        })
    }

    pub fn get_all_cards(&self) -> Result<Vec<Card>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_all_cards().map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_card(&self, id: i32) -> Result<Option<Card>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_card(id).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_user_name(&self) -> Result<Option<String>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_setting("user_name").map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn set_user_name(&self, name: &str) -> Result<(), CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.set_setting("user_name", name)
            .map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_app_setting(&self, key: &str) -> Result<Option<String>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_app_setting(key).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn set_app_setting(&self, key: &str, value: &str) -> Result<(), CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.set_app_setting(key, value)
            .map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_all_astro_signs(&self) -> Result<Vec<crate::models::AstroSign>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_all_astro_signs().map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_astro_sign(&self, id: i32) -> Result<Option<crate::models::AstroSign>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_astro_sign(id).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_all_astro_bodies(&self) -> Result<Vec<crate::models::AstroBody>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_all_astro_bodies().map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_astro_body(&self, id: i32) -> Result<Option<crate::models::AstroBody>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_astro_body(id).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn daily_astro(&self) -> Result<crate::astro::DailyAstro, CoreError> {
        Ok(crate::astro::daily_astro())
    }

    pub fn delete_astro_entries(&self, sign_ids: &[i32], body_ids: &[i32]) -> Result<(), CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.delete_astro_entries(sign_ids, body_ids).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }


    pub fn get_all_birth_profiles(&self) -> Result<Vec<crate::models::BirthProfile>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_all_birth_profiles().map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn add_birth_profile(&self, p: &crate::models::BirthProfile) -> Result<i64, CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.add_birth_profile(p).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn update_birth_profile(&self, id: i32, p: &crate::models::BirthProfile) -> Result<(), CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.update_birth_profile(id, p).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn delete_birth_profile(&self, id: i32) -> Result<(), CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.delete_birth_profile(id).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn natal_chart_for_profile(&self, p: &crate::models::BirthProfile) -> Result<crate::natal::NatalChart, CoreError> {
        Ok(crate::natal::compute_natal_chart(p.jd, p.latitude, p.longitude, p.timezone_offset_hours))
    }

    pub fn get_all_entities(&self) -> Result<Vec<Entity>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_all_entities().map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_entity(&self, id: i32) -> Result<Option<Entity>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_entity(id).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_entities_by_type(&self, entity_type: EntityType,
    ) -> Result<Vec<Entity>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_entities_by_type(entity_type).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn search_entities(&self, query: &str) -> Result<Vec<Entity>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.search_entities(query).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn add_entity(&self, entity: &Entity) -> Result<i64, CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.add_entity(entity).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn update_entity(&self, id: i32, entity: &Entity) -> Result<(), CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.update_entity(id, entity).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn delete_entity(&self, id: i32) -> Result<(), CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.delete_entity(id).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn delete_entities(&self, ids: &[i32]) -> Result<(), CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.delete_entities(ids).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_all_spells(&self) -> Result<Vec<Spell>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_all_spells().map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_spells_by_category(&self, category: SpellCategory) -> Result<Vec<Spell>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_spells_by_category(category).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn search_spells(&self, query: &str) -> Result<Vec<Spell>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.search_spells(query).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_spell(&self, id: i32) -> Result<Option<Spell>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_spell(id).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn add_spell(&self, spell: &Spell) -> Result<i64, CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.add_spell(spell).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn delete_spell(&self, id: i32) -> Result<(), CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.delete_spell(id).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn delete_spells(&self, ids: &[i32]) -> Result<(), CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.delete_spells(ids).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn update_spell(&self, id: i32, spell: &Spell) -> Result<(), CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.update_spell(id, spell).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_all_deities(&self) -> Result<Vec<Deity>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_all_deities().map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_deity(&self, id: i32) -> Result<Option<Deity>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_deity(id).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_deities_by_religion(&self, religion: &str) -> Result<Vec<Deity>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_deities_by_religion(religion).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn search_deities(&self, query: &str) -> Result<Vec<Deity>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.search_deities(query).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn add_deity(&self, deity: &Deity) -> Result<i64, CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.add_deity(deity).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn update_deity(&self, id: i32, deity: &Deity) -> Result<(), CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.update_deity(id, deity).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn delete_deity(&self, id: i32) -> Result<(), CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.delete_deity(id).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn delete_deities(&self, ids: &[i32]) -> Result<(), CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.delete_deities(ids).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn restore_builtin_deletions(&self) -> Result<(), CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.restore_builtin_deletions().map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_all_offerings(&self) -> Result<Vec<Offering>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_all_offerings().map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_all_staves(&self) -> Result<Vec<Stave>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_all_staves().map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_stave(&self, id: i32) -> Result<Option<Stave>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_stave(id).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_staves_by_category(&self, category: &str
    ) -> Result<Vec<Stave>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_staves_by_category(category).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn search_staves(&self, query: &str) -> Result<Vec<Stave>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.search_staves(query).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn update_stave(&self, id: i32, json: &str) -> Result<(), CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        let stave: Stave = serde_json::from_str(json).map_err(|e| CoreError::InvalidInput(e.to_string()))?;
        db.update_stave(id, &stave).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_offerings_by_deity(&self, deity_id: i32) -> Result<Vec<Offering>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_offerings_by_deity(deity_id).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_offering(&self, id: i32) -> Result<Option<Offering>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_offering(id).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn search_offerings(&self, query: &str) -> Result<Vec<Offering>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.search_offerings(query).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn add_offering(&self, offering: &Offering) -> Result<i64, CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.add_offering(offering).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn update_offering(&self, id: i32, offering: &Offering) -> Result<(), CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.update_offering(id, offering).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn delete_offering(&self, id: i32) -> Result<(), CoreError> {
        let mut db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.delete_offering(id).map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_cards_by_suit(&self, suit: Suit) -> Result<Vec<Card>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_cards_by_arcana_or_suit(Some(Arcana::Minor), Some(suit))
            .map_err(|e| CoreError::DatabaseError(e.to_string()))
    }

    pub fn get_major_arcana(&self) -> Result<Vec<Card>, CoreError> {
        let db = self.db.lock().map_err(|e| CoreError::DatabaseError(e.to_string()))?;
        db.get_cards_by_arcana_or_suit(Some(Arcana::Major), None)
            .map_err(|e| CoreError::DatabaseError(e.to_string()))
    }
}

#[cfg(target_os = "android")]
mod android {
    use super::*;
    use jni::objects::{JClass, JString};
    use jni::JNIEnv;
    use jni::sys::jstring;
    use log::error;

    struct GlobalState {
        core: Option<TarotCore>,
    }

    static mut GLOBAL: GlobalState = GlobalState { core: None };

    fn with_global<F, T>(f: F) -> T
    where
        F: FnOnce(&mut GlobalState) -> T,
    {
        unsafe { f(&mut *std::ptr::addr_of_mut!(GLOBAL)) }
    }

    fn jstring_to_string(env: &mut JNIEnv, s: &JString) -> String {
        if s.is_null() {
            return String::new();
        }
        match env.get_string(s) {
            Ok(java_str) => java_str.to_string_lossy().into_owned(),
            Err(_) => String::new(),
        }
    }

    fn response_json<T: serde::Serialize>(result: Result<T, CoreError>) -> String {
        match result {
            Ok(v) => serde_json::to_string(&serde_json::json!({
                "success": true,
                "data": v
            }))
            .unwrap_or_else(|_| r#"{"success":false,"error":"serialization"}"#.into()),
            Err(e) => serde_json::to_string(&serde_json::json!({
                "success": false,
                "error": e.to_string()
            }))
            .unwrap_or_else(|_| r#"{"success":false,"error":"unknown"}"#.into()),
        }
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeInit(
        mut env: JNIEnv,
        _class: JClass,
        db_path: JString,
    ) {
        android_logger::init_once(
            android_logger::Config::default().with_max_level(log::LevelFilter::Debug),
        );
        let path = jstring_to_string(&mut env, &db_path);
        let core = if path.is_empty() {
            TarotCore::open_in_memory()
        } else {
            TarotCore::open(&path)
        };
        with_global(|g| {
            g.core = core.ok();
            if g.core.is_none() {
                error!("Failed to initialize TarotCore");
            }
        });
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetAllCards<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_all_cards(),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetCard<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_card(id),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetUserName<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_user_name().map(|o| o.unwrap_or_default()),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeSetUserName<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        name: JString,
    ) {
        let name_str = jstring_to_string(&mut env, &name);
        with_global(|g| {
            if let Some(core) = g.core.as_ref() {
                if let Err(e) = core.set_user_name(&name_str) {
                    error!("set_user_name failed: {e}");
                }
            }
        });
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetMajorArcana<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_major_arcana(),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetCardsBySuit<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        suit_idx: i32,
    ) -> jstring {
        let suit = match suit_idx {
            1 => Suit::Wands,
            2 => Suit::Cups,
            3 => Suit::Swords,
            4 => Suit::Pentacles,
            _ => Suit::None,
        };
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_cards_by_suit(suit),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetAppSetting<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        key: JString,
    ) -> jstring {
        let key_str = jstring_to_string(&mut env, &key);
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_app_setting(&key_str),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeSetAppSetting<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        key: JString,
        value: JString,
    ) {
        let key_str = jstring_to_string(&mut env, &key);
        let value_str = jstring_to_string(&mut env, &value);
        with_global(|g| {
            if let Some(core) = g.core.as_ref() {
                if let Err(e) = core.set_app_setting(&key_str, &value_str) {
                    error!("set_app_setting failed: {e}");
                }
            }
        });
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetAllEntities<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_all_entities(),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetEntity<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_entity(id),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetEntitiesByType<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        type_idx: i32,
    ) -> jstring {
        let entity_type = match type_idx {
            1 => EntityType::Ghost,
            2 => EntityType::Spirit,
            3 => EntityType::Demon,
            4 => EntityType::Fae,
            5 => EntityType::Shade,
            6 => EntityType::Wraith,
            7 => EntityType::Poltergeist,
            _ => EntityType::Spirit,
        };
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_entities_by_type(entity_type),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeSearchEntities<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        query: JString,
    ) -> jstring {
        let q = jstring_to_string(&mut env, &query);
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.search_entities(&q),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeAddEntity<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        json: JString,
    ) -> jstring {
        let s = jstring_to_string(&mut env, &json);
        let result: Result<i64, CoreError> = match serde_json::from_str::<Entity>(&s) {
            Ok(entity) => with_global(|g| {
                g.core.as_ref().map_or(
                    Err(CoreError::DatabaseError("not initialized".into())),
                    |c| c.add_entity(&entity),
                )
            }),
            Err(e) => Err(CoreError::InvalidInput(e.to_string())),
        };
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeUpdateEntity<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
        json: JString,
    ) {
        let s = jstring_to_string(&mut env, &json);
        if let Ok(entity) = serde_json::from_str::<Entity>(&s) {
            with_global(|g| {
                if let Some(core) = g.core.as_ref() {
                    if let Err(e) = core.update_entity(id, &entity) {
                        error!("update_entity failed: {e}");
                    }
                }
            });
        } else {
            error!("update_entity failed to parse JSON");
        }
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeDeleteEntity<'local>(
        mut _env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
    ) {
        with_global(|g| {
            if let Some(core) = g.core.as_ref() {
                if let Err(e) = core.delete_entity(id) {
                    error!("delete_entity failed: {e}");
                }
            }
        });
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeDeleteEntities<'local>(
        mut _env: JNIEnv<'local>,
        _class: JClass<'local>,
        ids_json: JString,
    ) {
        let ids: Vec<i32> = serde_json::from_str(&jstring_to_string(&mut _env, &ids_json))
            .unwrap_or_default();
        with_global(|g| {
            if let Some(core) = g.core.as_ref() {
                if let Err(e) = core.delete_entities(&ids) {
                    error!("delete_entities failed: {e}");
                }
            }
        });
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeRestoreBuiltins<'local>(
        mut _env: JNIEnv<'local>,
        _class: JClass<'local>,
    ) {
        with_global(|g| {
            if let Some(core) = g.core.as_ref() {
                if let Err(e) = core.restore_builtin_deletions() {
                    error!("restore_builtins failed: {e}");
                }
            }
        });
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetAllBirthProfiles<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_all_birth_profiles(),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeSaveBirthProfile<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
        json: JString,
    ) -> jstring {
        let s = jstring_to_string(&mut env, &json);
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| match serde_json::from_str::<crate::models::BirthProfile>(&s) {
                    Ok(p) => {
                        if id > 0 {
                            c.update_birth_profile(id, &p).map(|_| id as i64)
                        } else {
                            c.add_birth_profile(&p)
                        }
                    }
                    Err(e) => Err(CoreError::InvalidInput(e.to_string())),
                },
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeDeleteBirthProfile<'local>(
        mut _env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
    ) {
        with_global(|g| {
            if let Some(core) = g.core.as_ref() {
                if let Err(e) = core.delete_birth_profile(id) {
                    error!("delete_birth_profile failed: {e}");
                }
            }
        });
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeNatalChart<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        json: JString,
    ) -> jstring {
        let s = jstring_to_string(&mut env, &json);
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| match serde_json::from_str::<crate::models::BirthProfile>(&s) {
                    Ok(p) => c.natal_chart_for_profile(&p),
                    Err(e) => Err(CoreError::InvalidInput(e.to_string())),
                },
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetAllAstroSigns<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_all_astro_signs(),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetAstroSign<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_astro_sign(id),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetAllAstroBodies<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_all_astro_bodies(),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetAstroBody<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_astro_body(id),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeDailyAstro<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.daily_astro(),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeDeleteAstroEntries<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        sign_ids_json: JString,
        body_ids_json: JString,
    ) {
        let signs: Vec<i32> =
            serde_json::from_str(&jstring_to_string(&mut env, &sign_ids_json)).unwrap_or_default();
        let bodies: Vec<i32> =
            serde_json::from_str(&jstring_to_string(&mut env, &body_ids_json)).unwrap_or_default();
        with_global(|g| {
            if let Some(core) = g.core.as_ref() {
                if let Err(e) = core.delete_astro_entries(&signs, &bodies) {
                    error!("delete_astro_entries failed: {e}");
                }
            }
        });
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetAllDeities<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_all_deities(),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetDeity<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_deity(id),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetDeitiesByReligion<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        religion: JString,
    ) -> jstring {
        let r = jstring_to_string(&mut env, &religion);
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_deities_by_religion(&r),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeSearchDeities<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        query: JString,
    ) -> jstring {
        let q = jstring_to_string(&mut env, &query);
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.search_deities(&q),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeAddDeity<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        json: JString,
    ) -> jstring {
        let s = jstring_to_string(&mut env, &json);
        let result: Result<i64, CoreError> = match serde_json::from_str::<Deity>(&s) {
            Ok(deity) => with_global(|g| {
                g.core.as_ref().map_or(
                    Err(CoreError::DatabaseError("not initialized".into())),
                    |c| c.add_deity(&deity),
                )
            }),
            Err(e) => Err(CoreError::InvalidInput(e.to_string())),
        };
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeUpdateDeity<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
        json: JString,
    ) {
        let s = jstring_to_string(&mut env, &json);
        if let Ok(deity) = serde_json::from_str::<Deity>(&s) {
            with_global(|g| {
                if let Some(core) = g.core.as_ref() {
                    if let Err(e) = core.update_deity(id, &deity) {
                        error!("update_deity failed: {e}");
                    }
                }
            });
        } else {
            error!("update_deity failed to parse JSON");
        }
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeDeleteDeity<'local>(
        mut _env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
    ) {
        with_global(|g| {
            if let Some(core) = g.core.as_ref() {
                if let Err(e) = core.delete_deity(id) {
                    error!("delete_deity failed: {e}");
                }
            }
        });
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeDeleteDeities<'local>(
        mut _env: JNIEnv<'local>,
        _class: JClass<'local>,
        ids_json: JString,
    ) {
        let ids: Vec<i32> = serde_json::from_str(&jstring_to_string(&mut _env, &ids_json))
            .unwrap_or_default();
        with_global(|g| {
            if let Some(core) = g.core.as_ref() {
                if let Err(e) = core.delete_deities(&ids) {
                    error!("delete_deities failed: {e}");
                }
            }
        });
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetOfferingsByDeity<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        deity_id: i32,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_offerings_by_deity(deity_id),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetAllOfferings<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_all_offerings(),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetOffering<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_offering(id),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeSearchOfferings<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        query: JString,
    ) -> jstring {
        let q = jstring_to_string(&mut env, &query);
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.search_offerings(&q),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeAddOffering<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        json: JString,
    ) -> jstring {
        let s = jstring_to_string(&mut env, &json);
        let result: Result<i64, CoreError> = match serde_json::from_str::<Offering>(&s) {
            Ok(offering) => with_global(|g| {
                g.core.as_ref().map_or(
                    Err(CoreError::DatabaseError("not initialized".into())),
                    |c| c.add_offering(&offering),
                )
            }),
            Err(e) => Err(CoreError::InvalidInput(e.to_string())),
        };
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeUpdateOffering<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
        json: JString,
    ) {
        let s = jstring_to_string(&mut env, &json);
        if let Ok(offering) = serde_json::from_str::<Offering>(&s) {
            with_global(|g| {
                if let Some(core) = g.core.as_ref() {
                    if let Err(e) = core.update_offering(id, &offering) {
                        error!("update_offering failed: {e}");
                    }
                }
            });
        } else {
            error!("update_offering failed to parse JSON");
        }
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeDeleteOffering<'local>(
        mut _env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
    ) {
        with_global(|g| {
            if let Some(core) = g.core.as_ref() {
                if let Err(e) = core.delete_offering(id) {
                    error!("delete_offering failed: {e}");
                }
            }
        });
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetAllSpells<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_all_spells(),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetSpell<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_spell(id),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetSpellsByCategory<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        category_idx: i32,
    ) -> jstring {
        let category = match category_idx {
            1 => SpellCategory::Cleansing,
            2 => SpellCategory::Protection,
            3 => SpellCategory::Divination,
            4 => SpellCategory::Curses,
            5 => SpellCategory::Summoning,
            6 => SpellCategory::Healing,
            7 => SpellCategory::Binding,
            8 => SpellCategory::Glamour,
            9 => SpellCategory::Love,
            10 => SpellCategory::Prosperity,
            11 => SpellCategory::Luck,
            12 => SpellCategory::Banishment,
            13 => SpellCategory::Dream,
            14 => SpellCategory::Weather,
            15 => SpellCategory::Blessing,
            16 => SpellCategory::Elemental,
            17 => SpellCategory::Necromancy,
            18 => SpellCategory::Other,
            _ => SpellCategory::Cleansing,
        };
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_spells_by_category(category),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeSearchSpells<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        query: JString,
    ) -> jstring {
        let q = jstring_to_string(&mut env, &query);
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.search_spells(&q),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeAddSpell<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        json: JString,
    ) -> jstring {
        let s = jstring_to_string(&mut env, &json);
        let result: Result<i64, CoreError> = match serde_json::from_str::<Spell>(&s) {
            Ok(spell) => with_global(|g| {
                g.core.as_ref().map_or(
                    Err(CoreError::DatabaseError("not initialized".into())),
                    |c| c.add_spell(&spell),
                )
            }),
            Err(e) => Err(CoreError::InvalidInput(e.to_string())),
        };
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeDeleteSpell<'local>(
        mut _env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
    ) {
        with_global(|g| {
            if let Some(core) = g.core.as_ref() {
                if let Err(e) = core.delete_spell(id) {
                    error!("delete_spell failed: {e}");
                }
            }
        });
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeDeleteSpells<'local>(
        mut _env: JNIEnv<'local>,
        _class: JClass<'local>,
        ids_json: JString,
    ) {
        let ids: Vec<i32> = serde_json::from_str(&jstring_to_string(&mut _env, &ids_json))
            .unwrap_or_default();
        with_global(|g| {
            if let Some(core) = g.core.as_ref() {
                if let Err(e) = core.delete_spells(&ids) {
                    error!("delete_spells failed: {e}");
                }
            }
        });
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeUpdateSpell<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
        json: JString,
    ) {
        let s = jstring_to_string(&mut env, &json);
        if let Ok(spell) = serde_json::from_str::<Spell>(&s) {
            with_global(|g| {
                if let Some(core) = g.core.as_ref() {
                    if let Err(e) = core.update_spell(id, &spell) {
                        error!("update_spell failed: {e}");
                    }
                }
            });
        } else {
            error!("update_spell failed to parse JSON");
        }
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetAllStaves<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_all_staves(),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetStave<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
    ) -> jstring {
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.get_stave(id),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeGetStavesByCategory<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        category: JString,
    ) -> jstring {
        let c = jstring_to_string(&mut env, &category);
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c2| c2.get_staves_by_category(&c),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeSearchStaves<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        query: JString,
    ) -> jstring {
        let q = jstring_to_string(&mut env, &query);
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.search_staves(&q),
            )
        });
        let json = response_json(result);
        env.new_string(json).expect("new_string").into_raw()
    }

    #[allow(unused_mut)]
    #[no_mangle]
    pub extern "C" fn Java_dev_zad1ag_athenaeum_TarotCore_nativeUpdateStave<'local>(
        mut env: JNIEnv<'local>,
        _class: JClass<'local>,
        id: i32,
        json: JString,
    ) {
        let s = jstring_to_string(&mut env, &json);
        let result = with_global(|g| {
            g.core.as_ref().map_or(
                Err(CoreError::DatabaseError("not initialized".into())),
                |c| c.update_stave(id, &s),
            )
        });
        if let Err(e) = result {
            log::warn!("update_stave failed: {}", e);
        }
    }
}
