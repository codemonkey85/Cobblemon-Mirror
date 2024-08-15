/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.types

import com.cobblemon.mod.common.api.registry.CobblemonRegistry
import com.cobblemon.mod.common.registry.CobblemonRegistries
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

object ElementalTypes : CobblemonRegistry<ElementalType>() {

    @JvmStatic val NORMAL = this.key("normal")
    @JvmStatic val FIRE = this.key("fire")
    @JvmStatic val WATER = this.key("water")
    @JvmStatic val GRASS = this.key("grass")
    @JvmStatic val ELECTRIC = this.key("electric")
    @JvmStatic val ICE = this.key("ice")
    @JvmStatic val FIGHTING = this.key("fighting")
    @JvmStatic val POISON = this.key("poison")
    @JvmStatic val GROUND = this.key("ground")
    @JvmStatic val FLYING = this.key("flying")
    @JvmStatic val PSYCHIC = this.key("psychic")
    @JvmStatic val BUG = this.key("bug")
    @JvmStatic val ROCK = this.key("rock")
    @JvmStatic val GHOST = this.key("ghost")
    @JvmStatic val DRAGON = this.key("dragon")
    @JvmStatic val DARK = this.key("dark")
    @JvmStatic val STEEL = this.key("steel")
    @JvmStatic val FAIRY = this.key("fairy")
    @JvmStatic val STELLAR = this.key("stellar")

    fun getOrException(name: String): ElementalType {
        return CobblemonRegistries.ELEMENTAL_TYPE.getHolderOrThrow(
            ResourceKey.create(
                CobblemonRegistries.ELEMENTAL_TYPE_KEY,
                cobblemonResource(name)
            )
        ).value()
    }
    
    override fun registry(): Registry<ElementalType> = CobblemonRegistries.ELEMENTAL_TYPE

    override fun registryKey(): ResourceKey<Registry<ElementalType>> = CobblemonRegistries.ELEMENTAL_TYPE_KEY
}
