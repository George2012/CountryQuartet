package com.countryquartet.game.data

import com.countryquartet.game.data.GameDataValidator.EXPECTED_PHYSICIST_COUNT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The bundled roster the three computer players are drawn from. */
class PhysicistDataTest {

    private val physicists = GameDataParser.parsePhysicists(AssetFiles.readPhysicistsJson())

    @Test
    fun `the roster holds ten physicists`() {
        assertEquals(EXPECTED_PHYSICIST_COUNT, physicists.size)
    }

    @Test
    fun `the bundled roster passes validation`() {
        assertEquals(emptyList<String>(), GameDataValidator.validatePhysicists(physicists))
    }

    @Test
    fun `ids are unique`() {
        assertEquals(physicists.size, physicists.map { it.id }.distinct().size)
    }

    @Test
    fun `every physicist has a name and a short name`() {
        physicists.forEach { physicist ->
            assertTrue("${physicist.id} has a blank name", physicist.name.isNotBlank())
            assertTrue("${physicist.id} has a blank short name", physicist.shortName.isNotBlank())
        }
    }

    @Test
    fun `short names fit the score board`() {
        // Four players share the width of a phone, so a long name would be cut
        // off rather than shown.
        physicists.forEach { physicist ->
            assertTrue(
                "${physicist.shortName} is too long for a quarter of the score board",
                physicist.shortName.length <= MAX_SHORT_NAME_LENGTH,
            )
        }
    }

    @Test
    fun `a short roster is rejected`() {
        val problems = GameDataValidator.validatePhysicists(physicists.take(2))

        assertTrue("$problems", problems.any { it.contains("at least") })
    }

    @Test
    fun `a duplicated physicist is rejected`() {
        val problems = GameDataValidator.validatePhysicists(
            physicists.dropLast(1) + physicists.first(),
        )

        assertTrue("$problems", problems.any { it.contains("duplicate physicist id") })
    }

    private companion object {
        const val MAX_SHORT_NAME_LENGTH = 10
    }
}
