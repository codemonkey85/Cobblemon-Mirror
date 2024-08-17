/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.storage

import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer

/**
 * The base of the events around the release of Pokémon from pc or party.
 * For the generic experience gain event see [CobblemonEvents.POKEMON_RELEASED_EVENT_PRE] and [CobblemonEvents.POKEMON_RELEASED_EVENT_POST]
 *
 * @author Polymeta
 * @since March 22nd, 2023
 */
interface ReleasePokemonEvent {

    /**
     * The [ServerPlayer] that is releasing the Pokémon.
     */
    val player: ServerPlayer

    /**
     * The [Pokemon] being released.
     */
    val pokemon: Pokemon

    /**
     * The [PokemonStore] from which the Pokémon is being released from.
     */
    val storage: PokemonStore<*>

    /**
     * Fired when a player attempts to release a Pokémon from their pc or party.
     * Canceling this event will prevent the Pokémon from being released.
     * Users of this event are advised to send the player a message to let them know that they can't release that Pokémon and why.
     * For the event that is fired after all the calculations took place see [ReleasePokemonEvent.Post].
     */
    class Pre(
        override val player: ServerPlayer,
        override val pokemon: Pokemon,
        override val storage: PokemonStore<*>
    ) : ReleasePokemonEvent, Cancelable()

    /**
     * Fired after a player released a Pokémon from their pc or party.
     */
    class Post(
        override val player: ServerPlayer,
        override val pokemon: Pokemon,
        override val storage: PokemonStore<*>
    ) : ReleasePokemonEvent
}