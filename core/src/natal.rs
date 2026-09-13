//! Natal chart engine: planetary ecliptic longitudes (JPL approximate
//! Keplerian elements), placements (sign + degree + house), and aspects.

use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub enum NatalBody {
    Sun,
    Moon,
    Mercury,
    Venus,
    Mars,
    Jupiter,
    Saturn,
}

impl NatalBody {
    pub const ALL: [NatalBody; 7] = [
        NatalBody::Sun,
        NatalBody::Moon,
        NatalBody::Mercury,
        NatalBody::Venus,
        NatalBody::Mars,
        NatalBody::Jupiter,
        NatalBody::Saturn,
    ];

    pub fn symbol(self) -> &'static str {
        match self {
            NatalBody::Sun => "\u{2609}",
            NatalBody::Moon => "\u{263D}",
            NatalBody::Mercury => "\u{263F}",
            NatalBody::Venus => "\u{2640}",
            NatalBody::Mars => "\u{2642}",
            NatalBody::Jupiter => "\u{2643}",
            NatalBody::Saturn => "\u{2644}",
        }
    }
}

impl std::fmt::Display for NatalBody {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        let name = match self {
            NatalBody::Sun => "sun",
            NatalBody::Moon => "moon",
            NatalBody::Mercury => "mercury",
            NatalBody::Venus => "venus",
            NatalBody::Mars => "mars",
            NatalBody::Jupiter => "jupiter",
            NatalBody::Saturn => "saturn",
        };
        write!(f, "{name}")
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub enum AspectKind {
    Conjunction,
    Sextile,
    Square,
    Trine,
    Opposition,
}

impl AspectKind {
    pub fn angle(self) -> f64 {
        match self {
            AspectKind::Conjunction => 0.0,
            AspectKind::Sextile => 60.0,
            AspectKind::Square => 90.0,
            AspectKind::Trine => 120.0,
            AspectKind::Opposition => 180.0,
        }
    }

    pub fn orb(self) -> f64 {
        match self {
            AspectKind::Conjunction | AspectKind::Opposition => 8.0,
            AspectKind::Trine | AspectKind::Square => 7.0,
            AspectKind::Sextile => 5.0,
        }
    }

    pub fn label(self) -> &'static str {
        match self {
            AspectKind::Conjunction => "conjunction",
            AspectKind::Sextile => "sextile",
            AspectKind::Square => "square",
            AspectKind::Trine => "trine",
            AspectKind::Opposition => "opposition",
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Placement {
    pub body: NatalBody,
    pub longitude: f64,
    pub sign: String,
    pub degree_in_sign: f64,
    pub house: i32,
    pub retrograde: bool,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct NatalAspect {
    pub a: NatalBody,
    pub b: NatalBody,
    pub kind: AspectKind,
    pub separation: f64,
    pub orb: f64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct NatalChart {
    pub jd: f64,
    pub placements: Vec<Placement>,
    pub aspects: Vec<NatalAspect>,
    pub ascendant_longitude: Option<f64>,
    pub ascendant_sign: Option<String>,
    pub latitude: Option<f64>,
}

// ---------------------------------------------------------------------------
// Keplerian machinery
// ---------------------------------------------------------------------------

fn normalize360(x: f64) -> f64 {
    let mut v = x % 360.0;
    if v < 0.0 {
        v += 360.0;
    }
    v
}

fn solve_kepler(m_deg: f64, e: f64) -> f64 {
    let m = m_deg.to_radians();
    let mut e_anom = m;
    for _ in 0..10 {
        let delta = (e_anom - e * e_anom.sin() - m) / (1.0 - e * e_anom.cos());
        e_anom -= delta;
        if delta.abs() < 1e-12 {
            break;
        }
    }
    e_anom
}

/// JPL approximate Keplerian elements (valid 1800–2050), evaluated at
/// t = centuries past J2000. Returns (a, e, i, L, long.peri, long.node).
fn elements(body: NatalBody, t: f64) -> (f64, f64, f64, f64, f64, f64) {
    let (a0, ad, e0, ed, i0, id, l0, ld, om0, omd, w0, wd) = match body {
        NatalBody::Mercury => (0.38709927, 0.00000037, 0.20563593, 0.00001906, 7.00497902, -0.00594749, 252.25032350, 149472.67411175, 48.33076593, -0.12534081, 77.45779628, 0.16047689),
        NatalBody::Venus => (0.72333566, 0.00000390, 0.00677672, -0.00004107, 3.39467605, -0.00442519, 181.97909950, 58517.81538729, 76.67984255, -0.02755403, 131.60246718, 0.00273796),
        NatalBody::Mars => (1.52371034, 0.00001847, 0.09339410, 0.00007882, 1.84969142, -0.00813131, -4.55343205, 19140.30268499, 49.55953891, -0.29257343, -23.94362959, 0.44242246),
        NatalBody::Jupiter => (5.20288700, -0.00011607, 0.04838624, -0.00013253, 1.30439695, -0.00183714, 34.39644051, 10920.97395910, 100.47390909, 0.00416667, 14.72847983, 0.21252668),
        NatalBody::Saturn => (9.53667594, -0.00125060, 0.05386179, -0.00050991, 2.48599187, 0.00193609, 49.95424423, 1222.49362201, 113.66242448, -0.01138667, 92.59887831, -0.04106686),
        _ => (0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
    };
    (
        a0 + ad * t,
        e0 + ed * t,
        i0 + id * t,
        l0 + ld * t,
        om0 + omd * t,
        w0 + wd * t,
    )
}

/// Heliocentric ecliptic rectangular coordinates (AU) for a planet.
fn heliocentric(body: NatalBody, t: f64) -> (f64, f64, f64) {
    let (a, e, inc_deg, l_deg, node_deg, peri_deg) = match body {
        NatalBody::Sun | NatalBody::Moon => return (0.0, 0.0, 0.0),
        _ => elements(body, t),
    };

    let arg_peri = (peri_deg - node_deg) * D2R;
    let node = node_deg * D2R;
    let inc = inc_deg * D2R;

    let m = normalize360(l_deg - peri_deg);
    let e_anom = solve_kepler(m, e);

    let xv = a * (e_anom.cos() - e);
    let yv = a * (1.0 - e * e).sqrt() * e_anom.sin();
    let r = (xv * xv + yv * yv).sqrt();
    let v = yv.atan2(xv);

    let u = arg_peri + v;
    let x = r * (node.cos() * u.cos() - node.sin() * u.sin() * inc.cos());
    let y = r * (node.sin() * u.cos() + node.cos() * u.sin() * inc.cos());
    let _z = r * u.sin() * inc.sin();
    (x, y, 0.0)
}

/// Sun's apparent geocentric ecliptic longitude (Meeus 25, low precision).
fn sun_longitude(jd: f64) -> f64 {
    let t = (jd - 2451545.0) / 36525.0;
    let l0 = 280.46646 + 36000.76983 * t + 0.0003032 * t * t;
    let m = 357.52911 + 35999.05029 * t - 0.0001537 * t * t;
    let m_rad = m.to_radians();
    let c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * m_rad.sin()
        + (0.019993 - 0.000101 * t) * (2.0 * m_rad).sin()
        + 0.000289 * (3.0 * m_rad).sin();
    normalize360(l0 + c)
}

use crate::astro::moon_ecliptic_longitude;

/// Earth heliocentric x (AU): opposite of Sun's geocentric direction.
fn earth_x_heliocentric(jd: f64) -> f64 {
    let r = earth_sun_distance(jd);
    -r * sun_longitude(jd).to_radians().cos()
}

fn earth_y_heliocentric(jd: f64) -> f64 {
    let r = earth_sun_distance(jd);
    -r * sun_longitude(jd).to_radians().sin()
}

fn earth_sun_distance(jd: f64) -> f64 {
    let t = (jd - 2451545.0) / 36525.0;
    let m = (357.52911 + 35999.05029 * t - 0.0001537 * t * t).to_radians();
    let e = 0.016708634 - 0.000042037 * t - 0.0000001267 * t * t;
    1.000001018 * (1.0 - e * e) / (1.0 + e * m.cos())
}

/// Ecliptic longitude of a body (geocentric, degrees 0-360).
pub fn body_longitude(body: NatalBody, jd: f64) -> f64 {
    match body {
        NatalBody::Sun => sun_longitude(jd),
        NatalBody::Moon => moon_ecliptic_longitude(jd),
        _ => {
            let t = (jd - 2451545.0) / 36525.0;
            let (px, py, _pz) = heliocentric(body, t);
            let gx = px - earth_x_heliocentric(jd);
            let gy = py - earth_y_heliocentric(jd);
            normalize360(gy.atan2(gx).to_degrees())
        }
    }
}

/// Retrograde = the body's longitude is decreasing over a short interval.
pub fn is_retrograde(body: NatalBody, jd: f64) -> bool {
    match body {
        NatalBody::Sun | NatalBody::Moon => false,
        _ => {
            let l1 = body_longitude(body, jd);
            let l2 = body_longitude(body, jd + 0.05);
            normalize360(l2 - l1) > 180.0
        }
    }
}

/// Whole-sign house: house 1 = ascendant's sign. Without an ascendant, all
/// placements report house 0 (unknown).
fn house_of(lon: f64, asc: Option<f64>) -> i32 {
    match asc {
        None => 0,
        Some(a) => {
            let asc_sign = (normalize360(a) / 30.0).floor() as i32;
            let body_sign = (normalize360(lon) / 30.0).floor() as i32;
            ((body_sign - asc_sign).rem_euclid(12)) + 1
        }
    }
}

/// Ascendant: ecliptic longitude rising on the eastern horizon.
/// Meeus-style low precision: needs local sidereal time and latitude.
fn ascendant_longitude(jd: f64, latitude_deg: f64, timezone_offset_hours: f64) -> Option<f64> {
    // Local civil time -> UT is already handled by the caller via jd.
    // Greenwich mean sidereal time (Meeus 12.4), in degrees:
    let t = (jd - 2451545.0) / 36525.0;
    let gmst = normalize360(
        280.46061837 + 360.98564736629 * (jd - 2451545.0)
            + 0.000387933 * t * t
            - t * t * t / 38710000.0,
    );
    // Without a longitude we can't get local sidereal time; caller should
    // supply approximate longitude of birthplace. We use latitude only +
    // a supplied `birth_longitude` — see compute_natal_chart.
    let _ = gmst;
    let _ = latitude_deg;
    let _ = timezone_offset_hours;
    None
}

const D2R: f64 = std::f64::consts::PI / 180.0;

/// Compute a full natal chart from a birth date-time and location.
/// `jd` = Julian day (UT) of the birth instant.
pub fn compute_natal_chart(jd: f64, latitude: Option<f64>, longitude: Option<f64>, timezone_offset_hours: f64) -> NatalChart {
    // Ascendant (needs both lat and lon):
    let asc = match (latitude, longitude) {
        (Some(lat), Some(lon)) => {
            // Local sidereal time in degrees
            let t = (jd - 2451545.0) / 36525.0;
            let gmst = normalize360(
                280.46061837 + 360.98564736629 * (jd - 2451545.0)
                    + 0.000387933 * t * t
            );
            let lst = normalize360(gmst + lon);
            // Ascendant formula (standard tropical):
            let ramc = lst;
            let asc = ((ramc * D2R).tan().cos().acos() * lat.to_radians().cos() - 0.0);
            // Use the classical formula:
            // asc = atan2(cos(ramc), -(sin(ramc)*cos(lat) + tan(eps)*sin(lat)))
            let eps = 23.4392911 * D2R; // obliquity, good for centuries around 2000
            let ramc_rad = ramc * D2R;
            let tan_eps = eps.tan();
            let asc_num = ramc_rad.cos();
            let asc_den = -(ramc_rad.sin() * lat.to_radians().cos() + tan_eps * lat.to_radians().sin());
            let mut asc_lon = asc_den.atan2(asc_num);
            // Quadrant correction: ascendant must be within ~90 deg below MC
            if asc_den < 0.0 {
                asc_lon += std::f64::consts::PI;
            }
            let asc_lon = normalize360(asc_lon.to_degrees());
            Some(asc_lon)
        }
        _ => None,
    };

    let mut placements = Vec::new();
    for body in NatalBody::ALL {
        let lon = body_longitude(body, jd);
        placements.push(Placement {
            body,
            longitude: lon,
            sign: crate::astro::zodiac_sign_of_longitude(lon).to_string(),
            degree_in_sign: normalize360(lon) % 30.0,
            house: house_of(lon, asc),
            retrograde: is_retrograde(body, jd),
        });
    }

    let mut aspects = Vec::new();
    for i in 0..placements.len() {
        for j in (i + 1)..placements.len() {
            let (a, b) = (&placements[i], &placements[j]);
            let mut sep = (a.longitude - b.longitude).abs() % 360.0;
            if sep > 180.0 {
                sep = 360.0 - sep;
            }
            for kind in [
                AspectKind::Conjunction,
                AspectKind::Sextile,
                AspectKind::Square,
                AspectKind::Trine,
                AspectKind::Opposition,
            ] {
                let orb = (sep - kind.angle()).abs();
                let max_orb = kind.orb();
                // Luminaries get wider orbs
                let luminary = a.body == NatalBody::Sun || a.body == NatalBody::Moon
                    || b.body == NatalBody::Sun || b.body == NatalBody::Moon;
                let limit = if luminary { max_orb + 1.0 } else { max_orb - 1.0 };
                if orb <= limit {
                    aspects.push(NatalAspect {
                        a: a.body,
                        b: b.body,
                        kind,
                        separation: sep,
                        orb,
                    });
                }
            }
        }
    }

    NatalChart {
        jd,
        placements,
        aspects,
        ascendant_longitude: asc,
        ascendant_sign: asc.map(|a| crate::astro::zodiac_sign_of_longitude(a).to_string()),
        latitude,
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    /// Sun in the right place: 2000-03-20 07:35 UT was the March equinox
    /// (Sun at ~0 deg Aries).
    #[test]
    fn sun_position_known_date() {
        // JD 2451635 = 2000-03-31 12:00 UT; Sun ~11.09 deg Aries.
        let jd = 2451635.0;
        let lon = body_longitude(NatalBody::Sun, jd);
        assert!((lon - 11.09).abs() < 0.2, "sun should be ~11.09 Aries, got {lon}");
    }

    #[test]
    fn mercury_never_far_from_sun() {
        // Mercury's maximum elongation is ~28 deg.
        let jd = 2460400.0;
        let m = body_longitude(NatalBody::Mercury, jd);
        let s = body_longitude(NatalBody::Sun, jd);
        let mut diff = (m - s).abs();
        if diff > 180.0 { diff = 360.0 - diff; }
        assert!(diff <= 28.0, "mercury elongation {diff}");
    }

    #[test]
    fn venus_max_elongation() {
        let jd = 2460400.0;
        let v = body_longitude(NatalBody::Venus, jd);
        let s = body_longitude(NatalBody::Sun, jd);
        let mut diff = (v - s).abs();
        if diff > 180.0 { diff = 360.0 - diff; }
        assert!(diff <= 47.0, "venus elongation {diff}");
    }

    #[test]
    fn full_chart_produces_all_bodies() {
        let chart = compute_natal_chart(2460400.0, Some(64.14), Some(-21.9), 0.0);
        assert_eq!(chart.placements.len(), 7);
        assert!(chart.ascendant_longitude.is_some());
        for p in &chart.placements {
            assert!(p.longitude >= 0.0 && p.longitude < 360.0, "lon {}", p.longitude);
            assert!(!p.sign.is_empty());
        }
    }
}
