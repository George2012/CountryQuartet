package com.countryquartet.game.data

/**
 * Reads the shipped asset files, so the JVM tests exercise exactly the JSON
 * that ends up in the APK.
 *
 * `src/main/assets` is registered as a unit test resource directory in
 * `app/build.gradle.kts`, which also makes Gradle re-run these tests whenever
 * the content changes.
 */
object AssetFiles : GameDataSource {

    override fun readCountriesJson(): String = read(AssetGameDataSource.COUNTRIES_ASSET)

    override fun readQuartetsJson(): String = read(AssetGameDataSource.QUARTETS_ASSET)

    override fun readPhysicistsJson(): String = read(AssetGameDataSource.PHYSICISTS_ASSET)

    private fun read(name: String): String {
        val stream = AssetFiles::class.java.getResourceAsStream("/$name")
            ?: error("Asset $name is not on the test classpath")
        return stream.bufferedReader().use { it.readText() }
    }
}
