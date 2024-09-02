/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.entity

import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.nbt.CompoundTag

/**
 * An event fired when a [PokemonEntity] is being loaded from NBT. This could be from sending a Pokémon
 * through a portal or saved into the world. Subscribers to this event should read from the NBT and apply
 * any changes that are relevant to the entity.
 *
 * Cancelling this event will dispose of the Pokémon after it loads. Use this when the existence of this
 * entity is no longer appropriate, such as if it was an event Pokémon that has expired.
 *
 * @property pokemonEntity The entity being loaded from NBT. All other native loading has been done by this time.
 * @property nbt The NBT the entity is being loaded from.
 *
 * @author Hiroku
 * @since January 7th, 2022
 */
data class PokemonEntityLoadEvent(val pokemonEntity: PokemonEntity, val nbt: CompoundTag) : Cancelable()