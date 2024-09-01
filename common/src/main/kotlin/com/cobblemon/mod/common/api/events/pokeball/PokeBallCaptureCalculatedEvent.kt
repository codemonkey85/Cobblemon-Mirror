/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokeball

import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity

/**
 * Event fired when a Poké Ball has completed its capture calculation and is about to begin shaking
 * or breaking free. The result of the capture can be changed by replacing [captureResult].
 *
 * @author Hiroku
 * @since August 20th, 2023
 */
class PokeBallCaptureCalculatedEvent(
    val thrower: LivingEntity,
    val pokemonEntity: PokemonEntity,
    val pokeBallEntity: EmptyPokeBallEntity,
    var captureResult: CaptureContext
) {
    fun ifPlayer(action: PokeBallCaptureCalculatedEvent.(player: ServerPlayer) -> Unit) {
        if (thrower is ServerPlayer) {
            action(this, thrower)
        }
    }
}