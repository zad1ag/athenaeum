use std::fmt;

/// Zodiac signs in ecliptic order. Index doubles as the sign's 0-11 slot:
/// sign(n) spans ecliptic longitude n*30..(n+1)*30 degrees.
#[derive(Debug, Clone, Copy, PartialEq, Eq, serde::Serialize, serde::Deserialize)]
#[serde(rename_all = "snake_case")]
pub enum ZodiacSign {
    Aries,
    Taurus,
    Gemini,
    Cancer,
    Leo,
    Virgo,
    Libra,
    Scorpio,
    Sagittarius,
    Capricorn,
    Aquarius,
    Pisces,
}

impl ZodiacSign {
    pub fn all() -> [ZodiacSign; 12] {
        [
            ZodiacSign::Aries,
            ZodiacSign::Taurus,
            ZodiacSign::Gemini,
            ZodiacSign::Cancer,
            ZodiacSign::Leo,
            ZodiacSign::Virgo,
            ZodiacSign::Libra,
            ZodiacSign::Scorpio,
            ZodiacSign::Sagittarius,
            ZodiacSign::Capricorn,
            ZodiacSign::Aquarius,
            ZodiacSign::Pisces,
        ]
    }

    pub fn index(self) -> usize {
        ZodiacSign::all()
            .iter()
            .position(|s| *s == self)
            .unwrap_or_default()
    }

    pub fn of_index(i: usize) -> ZodiacSign {
        ZodiacSign::all()[i % 12]
    }

    pub fn short_symbol(self) -> &'static str {
        match self {
            ZodiacSign::Aries => "\u{2648}",
            ZodiacSign::Taurus => "\u{2649}",
            ZodiacSign::Gemini => "\u{264A}",
            ZodiacSign::Cancer => "\u{264B}",
            ZodiacSign::Leo => "\u{264C}",
            ZodiacSign::Virgo => "\u{264D}",
            ZodiacSign::Libra => "\u{264E}",
            ZodiacSign::Scorpio => "\u{264F}",
            ZodiacSign::Sagittarius => "\u{2650}",
            ZodiacSign::Capricorn => "\u{2651}",
            ZodiacSign::Aquarius => "\u{2652}",
            ZodiacSign::Pisces => "\u{2653}",
        }
    }
}

impl fmt::Display for ZodiacSign {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        let name = match self {
            ZodiacSign::Aries => "aries",
            ZodiacSign::Taurus => "taurus",
            ZodiacSign::Gemini => "gemini",
            ZodiacSign::Cancer => "cancer",
            ZodiacSign::Leo => "leo",
            ZodiacSign::Virgo => "virgo",
            ZodiacSign::Libra => "libra",
            ZodiacSign::Scorpio => "scorpio",
            ZodiacSign::Sagittarius => "sagittarius",
            ZodiacSign::Capricorn => "capricorn",
            ZodiacSign::Aquarius => "aquarius",
            ZodiacSign::Pisces => "pisces",
        };
        write!(f, "{name}")
    }
}

pub fn parse_zodiac_sign(s: &str) -> ZodiacSign {
    match s.to_lowercase().as_str() {
        "aries" => ZodiacSign::Aries,
        "taurus" => ZodiacSign::Taurus,
        "gemini" => ZodiacSign::Gemini,
        "cancer" => ZodiacSign::Cancer,
        "leo" => ZodiacSign::Leo,
        "virgo" => ZodiacSign::Virgo,
        "libra" => ZodiacSign::Libra,
        "scorpio" => ZodiacSign::Scorpio,
        "sagittarius" => ZodiacSign::Sagittarius,
        "capricorn" => ZodiacSign::Capricorn,
        "aquarius" => ZodiacSign::Aquarius,
        _ => ZodiacSign::Pisces,
    }
}

/// Classical planets in Chaldean order (speed order). Used for planetary
/// days and (later) planetary hours.
#[derive(Debug, Clone, Copy, PartialEq, Eq, serde::Serialize, serde::Deserialize)]
#[serde(rename_all = "snake_case")]
pub enum Planet {
    Sun,
    Moon,
    Mercury,
    Venus,
    Mars,
    Jupiter,
    Saturn,
}

impl Planet {
    pub fn symbol(self) -> &'static str {
        match self {
            Planet::Sun => "\u{2609}",
            Planet::Moon => "\u{263D}",
            Planet::Mercury => "\u{263F}",
            Planet::Venus => "\u{2640}",
            Planet::Mars => "\u{2642}",
            Planet::Jupiter => "\u{2643}",
            Planet::Saturn => "\u{2644}",
        }
    }
}

impl fmt::Display for Planet {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        let name = match self {
            Planet::Sun => "sun",
            Planet::Moon => "moon",
            Planet::Mercury => "mercury",
            Planet::Venus => "venus",
            Planet::Mars => "mars",
            Planet::Jupiter => "jupiter",
            Planet::Saturn => "saturn",
        };
        write!(f, "{name}")
    }
}

// ---------------------------------------------------------------------------
// Astronomical engine
// ---------------------------------------------------------------------------

/// Julian Day at midnight UTC for a civil date (proleptic Gregorian).
pub fn julian_day(year: i32, month: u32, day: u32) -> f64 {
    // Meeus chapter 7 — valid for all Gregorian dates. Returns JD at 00:00 UT.
    let y = year as i64;
    let m = month as i64;
    let d = day as i64;
    let jd = 367 * y
        - (7 * (y + (m + 9) / 12)).div_euclid(4)
        + (275 * m).div_euclid(9)
        + d
        + 1_721_013;
    jd as f64 + 0.5
}

fn jd_now_approx() -> f64 {
    // Current time as JD; uses std::time to avoid chrono dependency here.
    // Precision of minutes is more than enough for the moon (13 deg/day).
    let now = std::time::SystemTime::now()
        .duration_since(std::time::UNIX_EPOCH)
        .unwrap_or_default();
    let unix_days = now.as_secs() as f64 + now.subsec_nanos() as f64 / 1e9;
    // Unix epoch (1970-01-01T00:00:00Z) = JD 2440587.5
    2440587.5 + unix_days / 86400.0
}

/// Meeus low-precision apparent longitude of the Moon (chapter 47, truncated).
/// Accurate to roughly +-0.3 degrees — ample for 30-degree sign boundaries.
pub fn moon_ecliptic_longitude(jd: f64) -> f64 {
    let t = (jd - 2451545.0) / 36525.0;

    // Moon's mean longitude (deg), Meeus 47.1
    let l_prime =
        218.3164477 + 481267.88123421 * t - 0.0015786 * t * t + t * t * t / 538841.0;
    // Moon's mean elongation
    let d = 297.8501921 + 445267.1114034 * t - 0.0018819 * t * t;
    // Sun's mean anomaly
    let m = 357.5291092 + 35999.0502909 * t - 0.0001536 * t * t;
    // Moon's mean anomaly
    let m_prime = 134.9633964 + 477198.8675055 * t + 0.0087414 * t * t;
    // Argument of latitude
    let f = 93.2720950 + 483202.0175233 * t - 0.0036539 * t * t;

    // First eccentricity of Earth's orbit
    let e = 1.0 - 0.002516 * t - 0.0000074 * t * t;

    let d = rad(normalize_deg(d));
    let m = rad(m);
    let m_prime = rad(m_prime);
    let f = rad(f);
    let _ = e; // e-terms below are second order; omitted deliberately

    // Dominant periodic terms (degrees). The six largest, with the second
    // largest elongation term doubled as the standard low-precision set.
    let mut lon = l_prime
        + 6.288774 * m_prime.sin()
        + 1.274027 * (2.0 * d - m_prime).sin()
        + 0.658314 * (2.0 * d).sin()
        + 0.213618 * (2.0 * m_prime).sin()
        - 0.185116 * m.sin() // equation of centre of the Sun's orbit
        - 0.114332 * (2.0 * f).sin()
        + 0.058793 * (2.0 * (d - m_prime)).sin()
        + 0.057066 * (2.0 * d - m - m_prime).sin()
        + 0.053322 * (2.0 * d + m_prime).sin()
        + 0.045758 * (2.0 * d - m).sin()
        - 0.040923 * (m - m_prime).sin()
        - 0.034720 * d.sin()
        - 0.030383 * (m + m_prime).sin();

    // A few smaller terms that keep the cusp error under an hour except in
    // pathological edge cases:
    lon += 0.015327 * (2.0 * d - 2.0 * f).sin();
    lon -= 0.012528 * (m_prime + 2.0 * f).sin();
    lon += 0.010980 * (m_prime - 2.0 * f).sin();
    lon += 0.010675 * (4.0 * d - m_prime).sin();
    lon += 0.010034 * (3.0 * m_prime).sin();
    lon += 0.008548 * (4.0 * d - 2.0 * m_prime).sin();
    lon -= 0.007888 * (2.0 * d + m - m_prime).sin();
    lon -= 0.006766 * (2.0 * d + m).sin();
    lon -= 0.005163 * (d - m_prime).sin();
    lon += 0.004950 * (d + m).sin();
    lon += 0.004065 * (2.0 * d - m + m_prime).sin();
    lon += 0.003000 * (2.0 * d + 2.0 * m_prime).sin();
    lon += 0.002600 * (2.0 * d + m - m_prime).sin();

    normalize_deg(lon)
}

fn rad(deg: f64) -> f64 {
    deg.to_radians()
}

fn normalize_deg(deg: f64) -> f64 {
    let mut d = deg % 360.0;
    if d < 0.0 {
        d += 360.0;
    }
    d
}

pub fn zodiac_sign_of_longitude(lon: f64) -> ZodiacSign {
    let normalized = normalize_deg(lon);
    let idx = (normalized / 30.0).floor() as usize;
    ZodiacSign::of_index(idx)
}

pub fn moon_zodiac_sign(jd: f64) -> ZodiacSign {
    zodiac_sign_of_longitude(moon_ecliptic_longitude(jd))
}

/// Approximate JD (UTC) of the next moment the Moon's longitude reaches
/// `target_deg` (0-360, the absolute ecliptic longitude). Bisection over up
/// to two days; the moon covers ~13 deg/day so a bracket is easy.
fn next_moon_longitude_crossing(jd_start: f64, target_deg: f64, forward: bool) -> f64 {
    let target = normalize_deg(target_deg);
    let step = if forward { 0.02 } else { -0.02 }; // ~29 minute steps
    let mut t_prev = jd_start;
    let mut lon_prev = moon_ecliptic_longitude(t_prev);

    // Walk until we wrap past the target.
    for _ in 0..12000 {
        let t_next = t_prev + step;
        let lon_now = moon_ecliptic_longitude(t_next);
        let crossed = if forward {
            // Handle wraparound at 360->0
            let a = normalize_deg(lon_now - lon_prev);
            let to_target = normalize_deg(target - lon_prev);
            a >= to_target && to_target >= 0.0 && a < 30.0
        } else {
            let a = normalize_deg(lon_prev - lon_now);
            let to_target = normalize_deg(lon_prev - target);
            a >= to_target && to_target >= 0.0 && a < 30.0
        };
        if crossed {
            // Bisect between t_prev and t_next.
            let (mut lo, mut hi) = if forward {
                (t_prev, t_next)
            } else {
                (t_next, t_prev)
            };
            for _ in 0..40 {
                let mid = (lo + hi) / 2.0;
                let _mid_lon = moon_ecliptic_longitude(mid);
                // Determine whether mid is before or after the crossing.
                let before = if forward {
                    normalize_deg(target - mid).abs() >
                        normalize_deg(mid - target).abs()
                } else {
                    normalize_deg(mid - target).abs() >
                        normalize_deg(target - mid).abs()
                };
                if before {
                    lo = mid;
                } else {
                    hi = mid;
                }
            }
            return (lo + hi) / 2.0;
        }
        t_prev = t_next;
        lon_prev = lon_now;
    }
    jd_start
}

/// The moment (JD, UTC) the Moon next enters `sign` after `jd_start`. If it is
/// already in `sign`, returns the moment it *leaves* and re-enters — rarely
/// what is wanted — so callers should check current sign first.
pub fn moon_sign_ingress_jd(jd_start: f64, sign: ZodiacSign) -> f64 {
    let target = (sign.index() * 30) as f64;
    next_moon_longitude_crossing(jd_start, target, true)
}

/// Days from `jd_start` until the Moon ingresses `sign`.
pub fn days_until_moon_sign(jd_start: f64, sign: ZodiacSign) -> f64 {
    let target = (sign.index() * 30) as f64;
    let current = moon_ecliptic_longitude(jd_start);
    let mut delta = normalize_deg(target - current);
    if delta < 0.0 {
        delta += 360.0;
    }
    delta / 13.176 // mean daily motion of the moon in degrees
}

// ---------------------------------------------------------------------------
// Sun sign (static date windows — accurate to +-1 day across centuries)
// ---------------------------------------------------------------------------

pub fn sun_sign(month: u32, day: u32) -> ZodiacSign {
    match (month, day) {
        (3, 21..=31) | (4, 1..=19) => ZodiacSign::Aries,
        (4, 20..=30) | (5, 1..=20) => ZodiacSign::Taurus,
        (5, 21..=31) | (6, 1..=20) => ZodiacSign::Gemini,
        (6, 21..=30) | (7, 1..=22) => ZodiacSign::Cancer,
        (7, 23..=31) | (8, 1..=22) => ZodiacSign::Leo,
        (8, 23..=31) | (9, 1..=22) => ZodiacSign::Virgo,
        (9, 23..=30) | (10, 1..=22) => ZodiacSign::Libra,
        (10, 23..=31) | (11, 1..=21) => ZodiacSign::Scorpio,
        (11, 22..=30) | (12, 1..=21) => ZodiacSign::Sagittarius,
        (12, 22..=31) | (1, 1..=19) => ZodiacSign::Capricorn,
        (1, 20..=31) | (2, 1..=18) => ZodiacSign::Aquarius,
        _ => ZodiacSign::Pisces,
    }
}

/// Planetary day ruler. Sunrise-to-sunrise ruling planet per weekday. Uses
/// the Chaldean order with the day starting at sunrise (classical) simplified
/// here to civil midnight for app purposes.
pub fn planetary_day_of_week(weekday_index: u32) -> Planet {
    // weekday_index: 0=Sunday .. 6=Saturday
    // Chaldean rulership of the days:
    // Sunday=Sun, Monday=Moon, Tuesday=Mars, Wednesday=Mercury,
    // Thursday=Jupiter, Friday=Venus, Saturday=Saturn
    match weekday_index {
        0 => Planet::Sun,
        1 => Planet::Moon,
        2 => Planet::Mars,
        3 => Planet::Mercury,
        4 => Planet::Jupiter,
        5 => Planet::Venus,
        _ => Planet::Saturn,
    }
}

/// Aggregate snapshot describing today's astro weather.
#[derive(Debug, Clone, serde::Serialize, serde::Deserialize)]
pub struct DailyAstro {
    pub sun_sign: ZodiacSign,
    pub moon_sign: ZodiacSign,
    /// Degrees the moon has traveled into its sign (0-30).
    pub moon_degrees_into_sign: f64,
    /// Percent of the moon's face illuminated (0-100), matching the app's
    /// existing moon phase math.
    pub moon_illumination: f64,
    /// Next sign the moon enters, and that crossing in ISO-ish "HH:MM" local
    /// hours from now.
    pub next_sign: ZodiacSign,
    /// JD at which the moon crosses into `next_sign`.
    pub next_sign_jd: f64,
    pub planetary_day: Planet,
    pub weekday_name: &'static str,
}

pub fn daily_astro() -> DailyAstro {
    let jd = jd_now_approx();
    let lon = moon_ecliptic_longitude(jd);
    let sign = zodiac_sign_of_longitude(lon);
    let deg_into = lon - (sign.index() as f64) * 30.0;

    // Next sign ingress: whichever of the twelve comes soonest.
    let mut best_sign = sign;
    let mut best_delta = f64::INFINITY;
    for s in ZodiacSign::all() {
        if s == sign {
            continue;
        }
        let target = (s.index() * 30) as f64;
        let mut delta = normalize_deg(target - lon);
        if delta < 0.0 {
            delta += 360.0;
        }
        if delta < best_delta {
            best_delta = delta;
            best_sign = s;
        }
    }
    let next_sign_jd = moon_sign_ingress_jd(jd, best_sign);

    // Weekday + sun sign from local civil date via unix seconds.
    let unix_secs = now_unix_secs();
    let days = unix_secs / 86400.0;
    let weekday = ((days as i64) + 4).rem_euclid(7) as u32; // 1970-01-01 was Thursday(4)
    let (year, month, day) = civil_from_unix_days(unix_secs / 86400.0);

    DailyAstro {
        sun_sign: sun_sign(month, day),
        moon_sign: sign,
        moon_degrees_into_sign: deg_into,
        moon_illumination: (1.0 - (2.0 * std::f64::consts::PI * (days / 29.53059) - std::f64::consts::PI).cos()) * 50.0,
        next_sign: best_sign,
        next_sign_jd,
        planetary_day: planetary_day_of_week(weekday),
        weekday_name: weekday_name(weekday),
    }
}

fn now_unix_secs() -> f64 {
    std::time::SystemTime::now()
        .duration_since(std::time::UNIX_EPOCH)
        .map(|d| d.as_secs() as f64)
        .unwrap_or_default()
}

fn weekday_name(idx: u32) -> &'static str {
    match idx {
        0 => "Sunday",
        1 => "Monday",
        2 => "Tuesday",
        3 => "Wednesday",
        4 => "Thursday",
        5 => "Friday",
        _ => "Saturday",
    }
}

fn civil_from_unix_days(days: f64) -> (i32, u32, u32) {
    // Howard Hinnant's civil_from_days algorithm.
    let z = days as i64 + 719468;
    let era = if z >= 0 { z } else { z - 146096 } / 146097;
    let doe = (z - era * 146097) as i64;
    let yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365;
    let y = yoe + era * 400;
    let doy = doe - (365 * yoe + yoe / 4 - yoe / 100);
    let mp = (5 * doy + 2) / 153;
    let d = (doy - (153 * mp + 2) / 5 + 1) as u32;
    let m = if mp < 10 { mp + 3 } else { mp - 9 } as u32;
    let year = if m <= 2 { y + 1 } else { y };
    (year as i32, m as u32, d)
}

#[cfg(test)]
mod tests {
    use super::*;

    fn jd_from_date(y: i32, m: u32, d: u32) -> f64 {
        julian_day(y, m, d)
    }

    #[test]
    fn sun_signs_match_windows() {
        assert!(matches!(sun_sign(3, 21), ZodiacSign::Aries));
        assert!(matches!(sun_sign(4, 19), ZodiacSign::Aries));
        assert!(matches!(sun_sign(4, 20), ZodiacSign::Taurus));
        assert!(matches!(sun_sign(12, 25), ZodiacSign::Capricorn));
    }

    #[test]
    fn moon_longitude_sane() {
        // 2024-04-08 was a new moon (sun and moon at ~18-19 deg Aries).
        let jd = jd_from_date(2024, 4, 8) + 0.5;
        let lon = moon_ecliptic_longitude(jd);
        assert!(
            lon > 10.0 && lon < 25.0,
            "new moon 2024-04-08 expected moon near 18-19 Aries, got {lon}"
        );
    }

    #[test]
    fn moon_moves_about_half_sign_per_day() {
        let jd0 = 2460000.0;
        let d0 = moon_ecliptic_longitude(jd0);
        let d1 = moon_ecliptic_longitude(jd0 + 1.0);
        let delta = (d1 - d0 + 360.0) % 360.0;
        assert!(delta > 11.0 && delta < 16.0, "lunar daily motion {delta}");
    }
}
#[cfg(test)]
mod jd_tests {
    #[test]
    fn jd_known_value() {
        // JD for 2024-04-08 00:00 UT is 2460408.5.
        assert!((super::julian_day(2024, 4, 8) - 2460408.5).abs() < 0.01,
            "jd 2024-04-08 = {}", super::julian_day(2024, 4, 8));
    }
}
