/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching


import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.world.entity.LivingEntity

/**
 * Represents an effect triggered by a Pokeball after a successful capture.
 * These effects are triggered immediately after the capture was successful but before the Pokémon is added to a store.
 *
 * @author Licious
 * @since May 7th, 2022
 */
fun interface CaptureEffect {

    /**
     * Apply this capture effect.
     *
     * @param thrower The [LivingEntity] that threw the Pokéball.
     * @param pokemon The [Pokemon] being affected.
     */
    fun apply(thrower: LivingEntity, pokemon: Pokemon)

}
