package com.countryquartet.game.model

/**
 * A group of exactly four countries. Collecting all four completes the quartet
 * and scores one point.
 */
data class Quartet(
    val id: String,
    val name: String,
    val countryIds: List<String>,
)
