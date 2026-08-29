package com.countryquartet.game.model

/**
 * One country card of the deck. Every country belongs to exactly one [Quartet].
 *
 * [id] is the stable identifier used by the game engine, the AI and the UI.
 * Display names are never used as identifiers.
 */
data class Country(
    val id: String,
    val name: String,
    val quartetId: String,
    val capital: String,
    val language: String,
    val currency: String,
    val flagAsset: String,
    val funFact: String,
)
