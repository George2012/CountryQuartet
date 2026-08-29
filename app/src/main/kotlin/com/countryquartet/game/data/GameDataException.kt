package com.countryquartet.game.data

/** Raised when the bundled game content cannot be read, parsed or validated. */
class GameDataException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
