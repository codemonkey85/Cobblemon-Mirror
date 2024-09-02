/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon

import com.cobblemon.mod.common.api.berry.Flavor
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.pokemon.Nature
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.resources.ResourceLocation

/**
 * Registry for all Nature types
 * Get or register nature types
 *
 * @author Deltric
 * @since January 13th, 2022
 */
object Natures {
    private val allNatures = mutableListOf<Nature>()

    val HARDY = registerNature(
        Nature(cobblemonResource("hardy"), "cobblemon.nature.hardy",
        null, null, null, null)
    )

    val LONELY = registerNature(
        Nature(cobblemonResource("lonely"), "cobblemon.nature.lonely",
        Stats.ATTACK, Stats.DEFENCE, Flavor.SPICY, Flavor.SOUR)
    )

    val BRAVE = registerNature(
        Nature(cobblemonResource("brave"), "cobblemon.nature.brave",
            Stats.ATTACK, Stats.SPEED, Flavor.SPICY, Flavor.SWEET)
    )

    val ADAMANT = registerNature(
        Nature(cobblemonResource("adamant"), "cobblemon.nature.adamant",
        Stats.ATTACK, Stats.SPECIAL_ATTACK, Flavor.SPICY, Flavor.DRY)
    )

    val NAUGHTY = registerNature(
        Nature(cobblemonResource("naughty"), "cobblemon.nature.naughty",
        Stats.ATTACK, Stats.SPECIAL_DEFENCE, Flavor.SPICY, Flavor.BITTER)
    )

    val BOLD = registerNature(
        Nature(cobblemonResource("bold"), "cobblemon.nature.bold",
        Stats.DEFENCE, Stats.ATTACK, Flavor.SOUR, Flavor.SPICY)
    )

    val DOCILE = registerNature(
        Nature(cobblemonResource("docile"), "cobblemon.nature.docile",
        null, null, null, null)
    )

    val RELAXED = registerNature(
        Nature(cobblemonResource("relaxed"), "cobblemon.nature.relaxed",
        Stats.DEFENCE, Stats.SPEED, Flavor.SOUR, Flavor.SWEET)
    )

    val IMPISH = registerNature(
        Nature(cobblemonResource("impish"), "cobblemon.nature.impish",
        Stats.DEFENCE, Stats.SPECIAL_ATTACK, Flavor.SOUR, Flavor.DRY)
    )

    val LAX = registerNature(
        Nature(cobblemonResource("lax"), "cobblemon.nature.lax",
        Stats.DEFENCE, Stats.SPECIAL_DEFENCE, Flavor.SOUR, Flavor.BITTER)
    )

    val TIMID = registerNature(
        Nature(cobblemonResource("timid"), "cobblemon.nature.timid",
        Stats.SPEED, Stats.ATTACK, Flavor.SWEET, Flavor.SPICY)
    )

    val HASTY = registerNature(
        Nature(cobblemonResource("hasty"), "cobblemon.nature.hasty",
        Stats.SPEED, Stats.DEFENCE, Flavor.SWEET, Flavor.SOUR)
    )

    val SERIOUS = registerNature(
        Nature(cobblemonResource("serious"), "cobblemon.nature.serious",
        null, null, null, null)
    )

    val JOLLY = registerNature(
        Nature(cobblemonResource("jolly"), "cobblemon.nature.jolly",
        Stats.SPEED, Stats.SPECIAL_ATTACK, Flavor.SWEET, Flavor.DRY)
    )

    val NAIVE = registerNature(
        Nature(cobblemonResource("naive"), "cobblemon.nature.naive",
        Stats.SPEED, Stats.SPECIAL_DEFENCE, Flavor.SWEET, Flavor.BITTER)
    )

    val MODEST = registerNature(
        Nature(cobblemonResource("modest"), "cobblemon.nature.modest",
        Stats.SPECIAL_ATTACK, Stats.ATTACK, null, null)
    )

    val MILD = registerNature(
        Nature(cobblemonResource("mild"), "cobblemon.nature.mild",
        Stats.SPECIAL_ATTACK, Stats.DEFENCE, Flavor.DRY, Flavor.SOUR)
    )

    val QUIET = registerNature(
        Nature(cobblemonResource("quiet"), "cobblemon.nature.quiet",
        Stats.SPECIAL_ATTACK, Stats.SPEED, Flavor.DRY, Flavor.SWEET)
    )

    val BASHFUL = registerNature(
        Nature(cobblemonResource("bashful"), "cobblemon.nature.bashful",
        null, null, null, null)
    )

    val RASH = registerNature(
        Nature(cobblemonResource("rash"), "cobblemon.nature.rash",
        Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Flavor.DRY, Flavor.BITTER)
    )

    val CALM = registerNature(
        Nature(cobblemonResource("calm"), "cobblemon.nature.calm",
        Stats.SPECIAL_DEFENCE, Stats.ATTACK, Flavor.BITTER, Flavor.SPICY)
    )

    val GENTLE = registerNature(
        Nature(cobblemonResource("gentle"), "cobblemon.nature.gentle",
        Stats.SPECIAL_DEFENCE, Stats.DEFENCE, Flavor.BITTER, Flavor.SOUR)
    )

    val SASSY = registerNature(
        Nature(cobblemonResource("sassy"), "cobblemon.nature.sassy",
        Stats.SPECIAL_DEFENCE, Stats.SPEED, Flavor.BITTER, Flavor.SWEET)
    )

    val CAREFUL = registerNature(
        Nature(cobblemonResource("careful"), "cobblemon.nature.careful",
        Stats.SPECIAL_DEFENCE, Stats.SPECIAL_ATTACK, Flavor.BITTER, Flavor.DRY)
    )

    val QUIRKY = registerNature(
        Nature(cobblemonResource("quirky"), "cobblemon.nature.quirky",
        null, null, null, null)
    )


    /**
     * Registers a new nature type
     */
    fun registerNature(nature: Nature): Nature {
        allNatures.add(nature)
        return nature
    }

    /**
     * Gets a nature by registry name
     * @return a nature type or null
     */
    fun getNature(name: ResourceLocation): Nature? {
        return allNatures.find { nature -> nature.name == name }
    }

    /**
     * Utility method to get a nature by string
     * @return a nature type or null
     */
    fun getNature(identifier: String): Nature? {
        val nature = getNature(cobblemonResource(identifier))
        if(nature != null) return nature
        return getNature(ResourceLocation.parse(identifier))
    }

    /**
     * Helper function for a random Nature
     * @return a random nature type
     */
    fun getRandomNature(): Nature {
        return allNatures.random()
    }

    fun all(): Collection<Nature> = this.allNatures.toList()

}