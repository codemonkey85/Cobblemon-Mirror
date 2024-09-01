/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.effects

import com.cobblemon.mod.common.api.entity.pokemon.*
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor
import com.cobblemon.mod.common.api.scheduling.afterOnServer
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import java.util.concurrent.CompletableFuture

/**
 * A [BattleEffect] that alters a [PokemonEntity] to be disguised as a target [Pokemon].
 *
 * @param disguise The [Pokemon] to use as a disguise.
 * @author Segfault Guy
 * @since March 5th, 2024
 */
class IllusionEffect(
    override var mock: PokemonProperties = PokemonProperties(),
    override var scale: Float = 1.0F
) : BattleEffect(), MocKEffect {

    constructor(disguise: Pokemon) : this(
        mock = disguise.createPokemonProperties(PokemonPropertyExtractor.ILLUSION),
        scale = disguise.form.baseScale * disguise.scaleModifier
    )

    override fun apply(entity: PokemonEntity, future: CompletableFuture<PokemonEntity>) {
        entity.effects.mockEffect = this
        future.complete(entity)
    }

    override fun revert(entity: PokemonEntity, future: CompletableFuture<PokemonEntity>) {
        entity.effects.mockEffect = null
        afterOnServer(seconds = 1.0F) {
            entity.cry()
            future.complete(entity)
        }
    }

    override fun saveToNbt(registryLookup: HolderLookup.Provider): CompoundTag {
        val nbt = CompoundTag()
        nbt.putString(DataKeys.ENTITY_EFFECT_ID, ID)
        nbt.put(DataKeys.POKEMON_ENTITY_MOCK, mock.saveToNBT(registryLookup))
        nbt.putFloat(DataKeys.POKEMON_ENTITY_SCALE, scale)
        return nbt
    }

    override fun loadFromNBT(nbt: CompoundTag, registryLookup: HolderLookup.Provider) {
        if (nbt.contains(DataKeys.POKEMON_ENTITY_MOCK)) this.mock = PokemonProperties().loadFromNBT(nbt.getCompound(DataKeys.POKEMON_ENTITY_MOCK), registryLookup)
        if (nbt.contains(DataKeys.POKEMON_ENTITY_SCALE)) this.scale = nbt.getFloat(DataKeys.POKEMON_ENTITY_SCALE)
    }

    companion object {
        val ID = "ILLUSION"
    }
}