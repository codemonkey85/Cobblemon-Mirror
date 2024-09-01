/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.status

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.status.Status
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.asTranslated
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import kotlin.random.Random
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation

/**
 * Represents a status that persists outside of battle.
 *
 * @author Deltric
 */
open class PersistentStatus(
    name: ResourceLocation,
    showdownName: String,
    applyMessage: String,
    removeMessage: String,
    private val defaultDuration: IntRange = 0..0
) : Status(name, showdownName, applyMessage, removeMessage) {
    /**
     * Called when a status duration is expired.
     */
    open fun onStatusExpire(player: ServerPlayer, pokemon: Pokemon, random: Random) {
        player.sendSystemMessage(removeMessage.asTranslated(pokemon.getDisplayName()))
    }

    /**
     * Called every second on the Pokémon for the status
     */
    open fun onSecondPassed(player: ServerPlayer, pokemon: Pokemon, random: Random) {

    }

    /**
     * The random period that this status could last.
     * @return the random period of the status.
     */
    fun statusPeriod(): IntRange {
        return Cobblemon.config.passiveStatuses[name.toString()] ?: defaultDuration
    }

    /**
     * The status's period as a config entry.
     * @return Status id with random period as a pair.
     */
    fun configEntry(): Pair<String, IntRange> {
        return name.toString() to defaultDuration
    }

    companion object {

        /**
         * A [Codec] for [PersistentStatus].
         */
        @JvmStatic
        val CODEC: Codec<PersistentStatus> = Status.CODEC.comapFlatMap(
            { status -> if (status is PersistentStatus) DataResult.success(status) else DataResult.error { "${status.name} is not a ${PersistentStatus::class.simpleName}" } },
            { status -> status }
        )
    }
}