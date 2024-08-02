/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.spawning

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.isRegularFile
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException
import java.net.URI
import kotlin.io.path.extension

internal class SpawnValidator {

    companion object {
        val GSON: Gson = GsonBuilder().create()
        const val RESOURCE_PATH = "data/cobblemon/"
    }

    @Test
    fun `Validate Spawn Files`() {
        val species = loadSpecies()
        val invalidSpawns = findInvalidSpawns(species)

        if (invalidSpawns.isNotEmpty()) {
            println("Detected invalid spawn parameters (${invalidSpawns.size})")
            invalidSpawns.forEach { println("- $it") }
        }

        assertTrue(invalidSpawns.isEmpty(), "There should be no invalid spawn parameters.")
    }

    private fun loadSpecies(): Collection<String> {
        val speciesPath = Paths.get(cobblemonResource("species"))

        return Files.walk(speciesPath)
            .filter { path -> path.isRegularFile() && path.extension == "json"  }
            .map { it.fileName.toString().removeSuffix(".json") }
            .toList()
    }

    private fun findInvalidSpawns(species: Collection<String>): List<String> {
        val spawnsPath = Paths.get(cobblemonResource("spawn_pool_world"))

        return Files.list(spawnsPath)
            .filter { path -> path.isRegularFile() && path.extension == "json"  }
            .toList()
            .flatMap { spawn ->
                val json = GSON.fromJson(Files.newBufferedReader(spawn), JsonObject::class.java)

                json.getAsJsonArray("spawns").mapNotNull { option ->
                    val obj = option as JsonObject
                    var target = obj.get("pokemon").asString
                    if (target.contains(" ")) {
                        target = target.substringBefore(' ')
                    }
                    if (!species.contains(target)) spawn.fileName.toString() else null
                }
            }
    }

    private fun cobblemonResource(path: String): URI {
        val adjustedPath = RESOURCE_PATH + path

        return javaClass.classLoader.getResource(adjustedPath)?.toURI()
            ?: throw FileNotFoundException(adjustedPath)
    }
}