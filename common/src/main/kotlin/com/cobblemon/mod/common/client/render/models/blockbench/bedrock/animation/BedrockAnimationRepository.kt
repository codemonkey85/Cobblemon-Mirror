/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.client.render.models.blockbench.BedrockAnimationReferenceFactory
import com.cobblemon.mod.common.client.render.models.blockbench.JsonPose
import com.cobblemon.mod.common.util.fromJson
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import net.minecraft.server.packs.resources.ResourceManager

/**
 * Handles the loading and retrieval of bedrock animations. These animations are agnostic of the type of
 * model that they are loaded onto.
 *
 * @author landonjw
 * @since January 5, 2022
 */
object BedrockAnimationRepository {

    private val gson = GsonBuilder()
        .disableHtmlEscaping()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(BedrockAnimation::class.java, BedrockAnimationAdapter)
        .create()

    private val animationGroups = mutableMapOf<String, BedrockAnimationGroup>()

    fun loadAnimations(resourceManager: ResourceManager, directories: List<String>) {
        JsonPose.registerAnimationFactory("bedrock", BedrockAnimationReferenceFactory)

        LOGGER.info("Loading animations...")
        var animationCount = 0
        animationGroups.clear()
        var wereValidationErrors = false
        for (directory in directories) {
            resourceManager.listResources(directory) { it.path.endsWith(".animation.json") }
                .forEach { (identifier, resource) ->
                    try {
                        val animationGroup = gson.fromJson<BedrockAnimationGroup>(resource.open().reader())
                        animationGroup.animations.entries.forEach { (name, animation) ->
                            animation.name = name
                            try {
                                animation.checkForErrors()
                            } catch (e: Throwable) {
                                LOGGER.error("Failed to load animation $name in group $identifier: ${e.message}")
                                wereValidationErrors= true
                            }
                        }
                        val animationGroupName = identifier.path.substringAfterLast("/").replace(".animation.json", "")
                        animationGroups[animationGroupName] = animationGroup
                        animationCount += animationGroup.animations.size
                    } catch (e: Exception) {
                        LOGGER.error("Failed to load animation group $identifier", e)
                    }
                }
        }
        if (wereValidationErrors) {
            LOGGER.error("There were errors in the animations. See above for details. You should fix these or there might be crashes later.")
        }
        LOGGER.info("Loaded $animationCount animations from ${animationGroups.size} animation groups")
    }

    fun tryGetAnimation(fileName: String, animationName: String): BedrockAnimation? {
        val animationGroup = animationGroups[fileName] ?: return null
        return animationGroup.animations[animationName]
    }

    fun getAnimation(fileName: String, animationName: String): BedrockAnimation {
        val animationGroup = animationGroups[fileName]
            ?: throw IllegalArgumentException("Unknown animation group: $fileName")

        return animationGroup.animations[animationName]
            ?: throw IllegalArgumentException("Animation $animationName not found in animation group $fileName")
    }

    fun getAnimationOrNull(fileName: String, animationName: String): BedrockAnimation? {
        return animationGroups[fileName]?.animations?.get(animationName)
    }
}