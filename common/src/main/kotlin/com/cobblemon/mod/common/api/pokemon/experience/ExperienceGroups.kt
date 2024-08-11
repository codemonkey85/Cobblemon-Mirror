/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.experience

import com.cobblemon.mod.common.api.CachedLevelThresholds
import com.cobblemon.mod.common.api.LevelCurve
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.math.pow
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import kotlin.math.max

/**
 * A Pokémon's experience group, as an implementation of [LevelCurve]. Complicated experience groups
 * should extend [CachedExperienceGroup] to save you from having to invert the level-to-experience
 * equation.
 *
 * @author Hiroku
 * @since March 21st, 2022
 */
@Suppress("unused")
interface ExperienceGroup : LevelCurve {
    val displayName: Component
    companion object {

        private val registry = hashMapOf<ResourceLocation, ExperienceGroup>()

        @JvmStatic
        val CODEC: Codec<ExperienceGroup> = ResourceLocation.CODEC.flatXmap(
            { id -> this.registry[id]?.let { DataResult.success(it) } ?: DataResult.error { "Cannot find a group with ID $id" } },
            { group ->
                val result = this.registry.entries.firstOrNull { it.value == group }
                if (result != null) {
                    return@flatXmap DataResult.success(result.key)
                }
                return@flatXmap DataResult.error { "Cannot resolve ExperienceGroup ${group::class.simpleName}, it's not correctly registered" }
            }
        )

        @JvmStatic val ERRATIC = this.register(cobblemonResource("erratic"), Erratic)
        @JvmStatic val FAST = this.register(cobblemonResource("fast"), Fast)
        @JvmStatic val MEDIUM_FAST = this.register(cobblemonResource("medium_fast"), MediumFast)
        @JvmStatic val MEDIUM_SLOW = this.register(cobblemonResource("medium_slow"), MediumSlow)
        @JvmStatic val SLOW = this.register(cobblemonResource("slow"), Slow)
        @JvmStatic val FLUCTUATING = this.register(cobblemonResource("fluctuating"), Fluctuating)
        @JvmStatic
        fun register(id: ResourceLocation, group: ExperienceGroup): ExperienceGroup {
            this.registry[id] = group
            return group
        }
    }
}

/**
 * An experience group which uses [CachedLevelThresholds] to answer how
 * to get a level from an experience value, given only the opposite equation.
 *
 * Mainly because my maths skills have deteriorated over the years and I don't
 * trust myself to invert these equations manually.
 *
 * @author Hiroku
 * @since March 21st, 2022
 */
abstract class CachedExperienceGroup : ExperienceGroup {
    private val thresholds = CachedLevelThresholds(experienceToLevel = ::getExperience)
    override fun getLevel(experience: Int) = thresholds.getLevel(experience)
}

object Erratic : CachedExperienceGroup() {
    override val displayName: Component = lang("experience_group.erratic")
    override fun getExperience(level: Int): Int {
        return when {
            level == 1 -> 0
            level < 50 -> level.pow(3) * (100 - level) / 50
            level < 68 -> level.pow(3) * (150 - level) / 100
            level < 98 -> level.pow(3) * (1911 - 10 * level) / 3 / 500
            else -> level.pow(3) * (160 - level) / 100
        }
    }
}

object Fast : CachedExperienceGroup() {
    override val displayName: Component = lang("experience_group.fast")
    override fun getExperience(level: Int) = if (level == 1) 0 else 4 * level.pow(3) / 5
}

object MediumFast : CachedExperienceGroup() {
    override val displayName: Component = lang("experience_group.medium_fast")
    override fun getExperience(level: Int) = if (level == 1) 0 else level.pow(3)
}

object MediumSlow : CachedExperienceGroup() {
    override val displayName: Component = lang("experience_group.medium_slow")
    override fun getExperience(level: Int) = max(0, level.pow(3) * 6 / 5 - 15 * level.pow(2) + 100 * level - 140)
}

object Slow : CachedExperienceGroup() {
    override val displayName: Component = lang("experience_group.slow")
    override fun getExperience(level: Int) = if (level == 1) 0 else 5 * level.pow(3) / 4
}

object Fluctuating : CachedExperienceGroup() {
    override val displayName: Component = lang("experience_group.fluctuating")
    override fun getExperience(level: Int): Int {
        return when {
            level == 1 -> 0
            level < 15 -> level.pow(3) * ((level + 1) / 3 + 24) / 50
            level < 36 -> level.pow(3) * (level + 14) / 50
            else -> level.pow(3) * (level / 2 + 32) / 50
        }
    }
}