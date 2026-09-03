package com.countryquartet.game.model

/**
 * One of the computer players: a physicist who takes a seat at the table.
 *
 * Purely who the opponent is, never how they play - every opponent uses the
 * same strategy.
 *
 * [shortName] is what the score board shows, because four players share the
 * width of a phone and "James Clerk Maxwell" does not fit a quarter of it.
 */
data class Physicist(
    val id: String,
    val name: String,
    val shortName: String,
)
