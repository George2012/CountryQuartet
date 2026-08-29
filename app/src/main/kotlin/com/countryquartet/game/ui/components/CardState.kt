package com.countryquartet.game.ui.components

/**
 * How a country card is being used at the moment.
 *
 * The states are shared by every card component so a card looks the same
 * wherever it appears.
 */
enum class CardState {
    /** Part of a list, not picked and not owned. */
    Normal,

    /** The card the player is currently acting on. */
    Selected,

    /** Visible but not usable right now, for instance during a computer turn. */
    Disabled,

    /** A card the player holds. */
    Owned,

    /** A country the player is asking an opponent for. */
    Requested,
}
