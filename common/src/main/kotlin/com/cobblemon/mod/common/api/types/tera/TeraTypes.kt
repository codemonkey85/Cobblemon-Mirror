/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.types.tera

import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.api.types.tera.elemental.ElementalTypeTeraType
import com.cobblemon.mod.common.api.types.tera.gimmick.StellarTeraType
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.resources.ResourceLocation

/**
 * The registry of all [TeraType]s.
 */
@Suppress("unused")
object TeraTypes : Iterable<TeraType> {
    private val types = hashMapOf<ResourceLocation, TeraType>()

    @JvmStatic
    val NORMAL = this.create(cobblemonResource("normal"), ElementalTypeTeraType(ElementalTypes.NORMAL))

    @JvmStatic
    val FIRE = this.create(cobblemonResource("fire"), ElementalTypeTeraType(ElementalTypes.FIRE))

    @JvmStatic
    val WATER = this.create(cobblemonResource("water"), ElementalTypeTeraType(ElementalTypes.WATER))

    @JvmStatic
    val GRASS = this.create(cobblemonResource("grass"), ElementalTypeTeraType(ElementalTypes.GRASS))

    @JvmStatic
    val ELECTRIC = this.create(cobblemonResource("electric"), ElementalTypeTeraType(ElementalTypes.ELECTRIC))

    @JvmStatic
    val ICE = this.create(cobblemonResource("ice"), ElementalTypeTeraType(ElementalTypes.ICE))

    @JvmStatic
    val FIGHTING = this.create(cobblemonResource("fighting"), ElementalTypeTeraType(ElementalTypes.FIGHTING))

    @JvmStatic
    val POISON = this.create(cobblemonResource("poison"), ElementalTypeTeraType(ElementalTypes.POISON))

    @JvmStatic
    val GROUND = this.create(cobblemonResource("ground"), ElementalTypeTeraType(ElementalTypes.GROUND))

    @JvmStatic
    val FLYING = this.create(cobblemonResource("flying"), ElementalTypeTeraType(ElementalTypes.FLYING))

    @JvmStatic
    val PSYCHIC = this.create(cobblemonResource("psychic"), ElementalTypeTeraType(ElementalTypes.PSYCHIC))

    @JvmStatic
    val BUG = this.create(cobblemonResource("bug"), ElementalTypeTeraType(ElementalTypes.BUG))

    @JvmStatic
    val ROCK = this.create(cobblemonResource("rock"), ElementalTypeTeraType(ElementalTypes.ROCK))

    @JvmStatic
    val GHOST = this.create(cobblemonResource("ghost"), ElementalTypeTeraType(ElementalTypes.GHOST))

    @JvmStatic
    val DRAGON = this.create(cobblemonResource("dragon"), ElementalTypeTeraType(ElementalTypes.DRAGON))

    @JvmStatic
    val DARK = this.create(cobblemonResource("dark"), ElementalTypeTeraType(ElementalTypes.DARK))

    @JvmStatic
    val STEEL = this.create(cobblemonResource("steel"), ElementalTypeTeraType(ElementalTypes.STEEL))

    @JvmStatic
    val FAIRY = this.create(cobblemonResource("fairy"), ElementalTypeTeraType(ElementalTypes.FAIRY))

    @JvmStatic
    val STELLAR = this.create(StellarTeraType.ID, StellarTeraType())

    /**
     * Pick a random [TeraType].
     *
     * @param legalOnly If [TeraType.legalAsStatic] should be respected.
     * @return The selected [TeraType].
     */
    @JvmStatic
    fun random(legalOnly: Boolean): TeraType {
        val possible = this.types.values
        if (legalOnly) {
            return possible.filter(TeraType::legalAsStatic).random()
        }
        return possible.random()
    }

    /**
     * Gets a [TeraType] by its [id].
     *
     * @param id The [ResourceLocation] expected to match against a [TeraType.id].
     * @return The found [TeraType] or null.
     */
    @JvmStatic
    fun get(id: ResourceLocation): TeraType? = this.types[id]

    /**
     * Gets a [TeraType] by its [id].
     *
     * @param id The string representation of a [ResourceLocation] if no namespace is present assumes Cobblemon' instead of Minecraft'.
     * @return The found [TeraType] or null.
     */
    @JvmStatic
    fun get(id: String): TeraType? = this.get(cobblemonResource(id))

    /**
     * Gets the corresponding tera type for a [ElementalType].
     *
     * @param type The [ElementalType] being checked.
     * @return The associated [TeraType].
     */
    @JvmStatic
    fun forElementalType(type: ElementalType): TeraType = this.get(cobblemonResource(type.name))!! // it's safe to do

    private fun create(id: ResourceLocation, type: TeraType): TeraType {
        this.types[id] = type
        return type
    }

    override fun iterator(): Iterator<TeraType> = this.types.values.iterator()
}