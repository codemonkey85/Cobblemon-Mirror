/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.effect

import com.cobblemon.mod.common.api.scheduling.ServerTaskTracker
import com.cobblemon.mod.common.platform.events.PlatformEvents
import com.cobblemon.mod.common.pokemon.activestate.ShoulderedState
import com.cobblemon.mod.common.pokemon.effects.PotionBaseEffect
import com.cobblemon.mod.common.util.party
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus

/**
 * Registry object for ShoulderEffects
 *
 * @author Qu
 * @since 2022-01-26
 */
@Suppress("unused")
object ShoulderEffectRegistry {

    private val effects = mutableMapOf<String, Class<out ShoulderEffect>>()

    // Effects - START
    val POTION_EFFECT = register("potion_effect", PotionBaseEffect::class.java)
    // Effects - END

    // Internal so 3rd party can't accidentally subscriber over n over.
    internal fun register() {
        PlatformEvents.SERVER_PLAYER_LOGIN.subscribe { this.refreshEffects(it.player) }
    }

    fun register(name: String, effect: Class<out ShoulderEffect>) = effect.also { effects[name] = it }

    fun unregister(name: String) = effects.remove(name)

    fun getName(clazz: Class<out ShoulderEffect>) = effects.firstNotNullOf { if (it.value == clazz) it.key else null }

    fun get(name: String): Class<out ShoulderEffect>? = effects[name]

    // It was removed by a source such as milk, reapply
    @ApiStatus.Internal
    fun onEffectEnd(player: ServerPlayer) {
        // Do this next tick so the client syncs correctly.
        // While it is a ticks worth of downtime it's still 1/20th of a second, doubt they'll notice.
        ServerTaskTracker.momentarily { this.refreshEffects(player) }
    }

    private fun refreshEffects(player: ServerPlayer) {
        player.party().filter { it.state is ShoulderedState }.forEach { pkm ->
            pkm.form.shoulderEffects.forEach {
                it.applyEffect(
                    pokemon = pkm,
                    player = player,
                    isLeft = (pkm.state as ShoulderedState).isLeftShoulder
                )
            }
        }
    }

}