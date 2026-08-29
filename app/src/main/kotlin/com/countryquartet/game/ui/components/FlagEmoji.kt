package com.countryquartet.game.ui.components

/**
 * Turns an ISO 3166-1 alpha-2 country code into the pair of regional indicator
 * symbols that Android renders as that country's flag.
 *
 * The country ids of the dataset are exactly those codes, so no flag images
 * need to be shipped. Returns an empty string for anything that is not a plain
 * two letter code, which lets the caller fall back to the code itself.
 */
fun flagEmoji(countryCode: String): String {
    val code = countryCode.trim().uppercase()
    if (code.length != 2 || code.any { it !in 'A'..'Z' }) return ""
    return code
        .map { letter -> Character.toChars(REGIONAL_INDICATOR_A + (letter - 'A')).concatToString() }
        .joinToString(separator = "")
}

/** Unicode code point of REGIONAL INDICATOR SYMBOL LETTER A. */
private const val REGIONAL_INDICATOR_A = 0x1F1E6
