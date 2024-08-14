/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.effect

import com.cobblemon.mod.common.api.scheduling.ServerTaskTracker
import com.cobblemon.mod.common.pokemon.activestate.ShoulderedState
import com.cobblemon.mod.common.pokemon.effects.PotionBaseEffect
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.party
import com.mojang.serialization.Lifecycle
import com.mojang.serialization.MapCodec
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus

fun interface ShoulderEffectType<T : ShoulderEffect> {

    fun codec(): MapCodec<T>

    companion object {
        internal val REGISTRY = MappedRegistry<ShoulderEffectType<*>>(
            ResourceKey.createRegistryKey(cobblemonResource("shoulder_effect")),
            Lifecycle.stable()
        )

        @JvmStatic
        val POTION_EFFECT = this.register(cobblemonResource("potion_effect"), PotionBaseEffect.CODEC)

        fun <T : ShoulderEffect> register(id: ResourceLocation, codec: MapCodec<T>): ShoulderEffectType<T> {
            return Registry.register(REGISTRY, id, ShoulderEffectType { codec })
        }

        // It was removed by a source such as milk, reapply
        @ApiStatus.Internal
        @JvmStatic
        fun onEffectEnd(player: ServerPlayer) {
            // Do this next tick so the client syncs correctly.
            // While it is a ticks worth of downtime it's still 1/20th of a second, doubt they'll notice.
            ServerTaskTracker.momentarily { this.refreshEffects(player) }
        }

        private fun refreshEffects(player: ServerPlayer) {
            player.party().filter { it.state is ShoulderedState }.forEach { pkm ->
                pkm.species.shoulderEffects.forEach {
                    it.applyEffect(
                        pokemon = pkm,
                        player = player,
                        isLeft = (pkm.state as ShoulderedState).isLeftShoulder
                    )
                }
            }
        }
    }

}