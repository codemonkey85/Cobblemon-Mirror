/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.evolution

import com.cobblemon.mod.common.pokemon.evolution.variants.BlockClickEvolution
import com.cobblemon.mod.common.pokemon.evolution.variants.ItemInteractionEvolution
import com.cobblemon.mod.common.pokemon.evolution.variants.LevelUpEvolution
import com.cobblemon.mod.common.pokemon.evolution.variants.TradeEvolution
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.serialization.Lifecycle
import com.mojang.serialization.MapCodec
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

fun interface EvolutionType<T : Evolution> {

    fun codec(): MapCodec<T>

    companion object {

        internal val REGISTRY: Registry<EvolutionType<*>> = MappedRegistry(
            ResourceKey.createRegistryKey(cobblemonResource("evolution_type")),
            Lifecycle.stable()
        )

        @JvmStatic
        val BLOCK_CLICK = this.register(cobblemonResource("block_click"), BlockClickEvolution.CODEC)

        @JvmStatic
        val LEVEL_UP = this.register(cobblemonResource("level_up"), LevelUpEvolution.CODEC)

        @JvmStatic
        val PASSIVE = this.register(cobblemonResource("passive"), LevelUpEvolution.CODEC)

        @JvmStatic
        val ITEM_INTERACTION = this.register(cobblemonResource("item_interact"), ItemInteractionEvolution.CODEC)

        @JvmStatic
        val TRADE = this.register(cobblemonResource("trade"), TradeEvolution.CODEC)

        @JvmStatic
        fun <T : Evolution> register(id: ResourceLocation, codec: MapCodec<T>): EvolutionType<T> {
            return Registry.register(REGISTRY, id, EvolutionType { codec })
        }

    }

}