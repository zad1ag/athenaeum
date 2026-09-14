package dev.zad1ag.athenaeum

fun tarotIconResource(cardId: Int): Int = when (cardId) {
    0 -> R.drawable.tarot_00_the_fool
    1 -> R.drawable.tarot_01_the_magician
    2 -> R.drawable.tarot_02_the_high_priestess
    3 -> R.drawable.tarot_03_the_empress
    4 -> R.drawable.tarot_04_the_emperor
    5 -> R.drawable.tarot_05_the_hierophant
    6 -> R.drawable.tarot_06_the_lovers
    7 -> R.drawable.tarot_07_the_chariot
    8 -> R.drawable.tarot_08_strength
    9 -> R.drawable.tarot_09_the_hermit
    10 -> R.drawable.tarot_10_wheel_of_fortune
    11 -> R.drawable.tarot_11_justice
    12 -> R.drawable.tarot_12_the_hanged_man
    13 -> R.drawable.tarot_13_death
    14 -> R.drawable.tarot_14_temperance
    15 -> R.drawable.tarot_15_the_devil
    16 -> R.drawable.tarot_16_the_tower
    17 -> R.drawable.tarot_17_the_star
    18 -> R.drawable.tarot_18_the_moon
    19 -> R.drawable.tarot_19_the_sun
    20 -> R.drawable.tarot_20_judgement
    21 -> R.drawable.tarot_21_the_world
    else -> 0
}

fun suitIconResource(suit: Suit): Int = when (suit) {
    Suit.wands -> R.drawable.tarot_suit_wands
    Suit.cups -> R.drawable.tarot_suit_cups
    Suit.swords -> R.drawable.tarot_suit_swords
    Suit.pentacles -> R.drawable.tarot_suit_pentacles
    Suit.none -> 0
}

fun numberToRoman(number: Int): String = when (number) {
    1 -> "I"
    2 -> "II"
    3 -> "III"
    4 -> "IV"
    5 -> "V"
    6 -> "VI"
    7 -> "VII"
    8 -> "VIII"
    9 -> "IX"
    10 -> "X"
    else -> ""
}
