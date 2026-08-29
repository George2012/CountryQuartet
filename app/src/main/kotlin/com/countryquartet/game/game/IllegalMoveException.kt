package com.countryquartet.game.game

/**
 * Raised when a request breaks the rules of the game.
 *
 * The UI and the AI are expected to offer legal moves only, so this signals a
 * programming error rather than something a player can trigger.
 */
class IllegalMoveException(message: String) : IllegalArgumentException(message)
