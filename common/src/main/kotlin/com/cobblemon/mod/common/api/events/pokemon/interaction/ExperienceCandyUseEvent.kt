/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokemon.interaction

import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.item.interactive.CandyItem
import com.cobblemon.mod.common.pokemon.AddExperienceResult
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer

/**
 * The base of the [CandyItem] related events.
 * For the generic experience gain event see [CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE] and [CobblemonEvents.EXPERIENCE_GAINED_EVENT_POST]
 *
 * @author Licious
 * @since May 5th, 2022
 */
interface ExperienceCandyUseEvent {

    /**
     * The [ServerPlayer] that fired the interaction.
     */
    val player: ServerPlayer

    /**
     * The [Pokemon] being targeted.
     */
    val pokemon: Pokemon

    /**
     * item The [CandyItem] variant being used.
     */
    val item: CandyItem

    /**
     * Fired when a player attempts to use an experience candy on a Pokémon.
     * Canceling this event will prevent the consumption of the item and the experience yield.
     * For the event that is fired after all the calculations took place see [ExperienceCandyUseEvent.Post].
     *
     * @property baseExperienceYield The default amount of experience the [pokemon] would earn.
     * @property experienceYield The current amount of experience the [pokemon] will earn.
     */
    class Pre(
        override val player: ServerPlayer,
        override val pokemon: Pokemon,
        override val item: CandyItem,
        val baseExperienceYield: Int,
        var experienceYield: Int
    ) : ExperienceCandyUseEvent, Cancelable()

    /**
     * Fired after a player used an experience candy on Pokémon and the experience yield was processed.
     *
     * @property experienceResult The resulting [AddExperienceResult] of the interaction.
     */
    class Post(
        override val player: ServerPlayer,
        override val pokemon: Pokemon,
        override val item: CandyItem,
        val experienceResult: AddExperienceResult
    ) : ExperienceCandyUseEvent {

        /**
         * Checks if the candy use resulted in any experience gain.
         * This will nearly always be true unless the Pokémon is at level cap.
         * This does not confirm the candy was consumed as the player may be in creative mode for that use [wasCandyConsumed].
         *
         * @return If any experience was gained from the candy.
         */
        fun wasExperienceGiven() = this.experienceResult.experienceAdded > 0

        /**
         * Checks if the candy use resulted in any experience gain and if it was consumed.
         * This will nearly always be true unless the Pokémon is at level cap and/or the player is in creative mode.
         * If you just want to know if the candy yielded experience use [wasExperienceGiven].
         *
         * @return If any experience was gained from the candy and if it was consumed.
         */
        fun wasCandyConsumed() = this.experienceResult.experienceAdded > 0 && !player.isCreative
    }

}