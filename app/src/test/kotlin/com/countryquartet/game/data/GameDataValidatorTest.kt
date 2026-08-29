package com.countryquartet.game.data

import com.countryquartet.game.model.Quartet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GameDataValidatorTest {

    private val countries = TestDataset.countries()
    private val quartets = TestDataset.quartets()

    @Test
    fun `a complete dataset is valid`() {
        assertEquals(emptyList<String>(), GameDataValidator.validate(countries, quartets))
    }

    @Test
    fun `requireValid returns the dataset`() {
        val data = GameDataValidator.requireValid(countries, quartets)
        assertEquals(52, data.countries.size)
        assertEquals(13, data.quartets.size)
        assertEquals(4, data.countriesOf("q0").size)
    }

    @Test
    fun `a missing country is reported`() {
        val problems = GameDataValidator.validate(countries.dropLast(1), quartets)
        assertProblem(problems, "expected 52 countries but found 51")
    }

    @Test
    fun `a missing quartet is reported`() {
        val problems = GameDataValidator.validate(countries, quartets.dropLast(1))
        assertProblem(problems, "expected 13 quartets but found 12")
    }

    @Test
    fun `a duplicate country id is reported`() {
        val duplicated = countries.dropLast(1) + countries.first()
        assertProblem(GameDataValidator.validate(duplicated, quartets), "duplicate country id: c0_0")
    }

    @Test
    fun `a duplicate country name is reported`() {
        val renamed = countries.toMutableList()
        renamed[1] = renamed[1].copy(name = renamed[0].name)
        assertProblem(GameDataValidator.validate(renamed, quartets), "duplicate country name")
    }

    @Test
    fun `a duplicate quartet id is reported`() {
        val duplicated = quartets.dropLast(1) + quartets.first()
        assertProblem(GameDataValidator.validate(countries, duplicated), "duplicate quartet id: q0")
    }

    @Test
    fun `a quartet with the wrong number of countries is reported`() {
        val shortened = quartets.replaceFirst { it.copy(countryIds = it.countryIds.dropLast(1)) }
        assertProblem(
            GameDataValidator.validate(countries, shortened),
            "quartet q0 has 3 countries, expected 4",
        )
    }

    @Test
    fun `a quartet listing the same country twice is reported`() {
        val repeated = quartets.replaceFirst {
            it.copy(countryIds = listOf("c0_0", "c0_0", "c0_2", "c0_3"))
        }
        assertProblem(GameDataValidator.validate(countries, repeated), "lists country c0_0 twice")
    }

    @Test
    fun `a quartet referencing an unknown country is reported`() {
        val unknown = quartets.replaceFirst {
            it.copy(countryIds = listOf("atlantis", "c0_1", "c0_2", "c0_3"))
        }
        assertProblem(
            GameDataValidator.validate(countries, unknown),
            "quartet q0 references unknown country: atlantis",
        )
    }

    @Test
    fun `a country referencing an unknown quartet is reported`() {
        val orphan = countries.map { country ->
            if (country.id == "c0_0") country.copy(quartetId = "nowhere") else country
        }
        assertProblem(
            GameDataValidator.validate(orphan, quartets),
            "country c0_0 references unknown quartet: nowhere",
        )
    }

    @Test
    fun `a country claimed by two quartets is reported`() {
        val overlapping = quartets.map { quartet ->
            if (quartet.id == "q1") {
                quartet.copy(countryIds = listOf("c0_0", "c1_1", "c1_2", "c1_3"))
            } else {
                quartet
            }
        }
        val problems = GameDataValidator.validate(countries, overlapping)
        assertProblem(problems, "country c0_0 claims quartet q0 but is listed by q0, q1")
        assertProblem(problems, "country c1_0 is not part of any quartet")
    }

    @Test
    fun `requireValid throws and lists every problem`() {
        val broken = quartets.replaceFirst { it.copy(countryIds = it.countryIds.dropLast(1)) }
        val error = assertThrows(GameDataException::class.java) {
            GameDataValidator.requireValid(countries, broken)
        }
        val message = requireNotNull(error.message)
        assertTrue(message, message.contains("quartet q0 has 3 countries"))
        assertTrue(message, message.contains("country c0_3 is not part of any quartet"))
    }

    private fun assertProblem(problems: List<String>, expected: String) {
        assertTrue(
            "expected a problem containing \"$expected\" but got $problems",
            problems.any { it.contains(expected) },
        )
    }

    private fun List<Quartet>.replaceFirst(transform: (Quartet) -> Quartet): List<Quartet> =
        mapIndexed { index, quartet -> if (index == 0) transform(quartet) else quartet }
}
