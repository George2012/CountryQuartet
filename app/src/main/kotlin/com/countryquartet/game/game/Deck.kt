package com.countryquartet.game.game

import com.countryquartet.game.model.GameData
import kotlin.random.Random

/** Builds the 52 card deck out of the loaded content. Cards are country ids. */
object Deck {

    /** The full deck in dataset order. */
    fun create(gameData: GameData): List<String> = gameData.countryIds

    /** The full deck in random order. Pass a seeded [random] for repeatable games. */
    fun shuffled(gameData: GameData, random: Random = Random.Default): List<String> =
        create(gameData).shuffled(random)
}
