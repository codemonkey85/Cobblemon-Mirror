/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.entity

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.nbt.CompoundTag

/**
 * Event fired when a Pokémon is being saved to NBT. This could be for
 * teleporting across worlds or for saving to the world or probably other
 * things.
 *
 * This event exists so that you can save additional information about a
 * Pokémon into entity NBT so that you can handle the [PokemonEntityLoadEvent]
 * later and restore that unique state. For example, an entity might be some
 * kind of boss, and you want to restore those properties after a world
 * reload.
 *
 * @property pokemonEntity The Pokémon entity being saved.
 * @property nbt The [NbtCompound] that the Pokémon is being saved to.
 *      You should add or remove from this for whatever you
 *      want to accomplish. The existing contents will be all
 *      of what Cobblemon saves natively.
 *
 * @author Hiroku
 * @since January 6th, 2023
 */
data class PokemonEntitySaveEvent(val pokemonEntity: PokemonEntity, val nbt: CompoundTag)