/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.effects

import com.cobblemon.mod.common.api.entity.pokemon.EntityEffect
import com.cobblemon.mod.common.api.entity.pokemon.MocKEffect
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.DataKeys
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import java.util.concurrent.CompletableFuture

/**
 * The [EntityEffect]s that are active for a [PokemonEntity].
 *
 * @author Segfault Guy
 * @since March 10th, 2024
 */
class EffectTracker(val entity: PokemonEntity) {

    /** The progression of the latest [EntityEffect] applied to an entity. */
    var progress: CompletableFuture<PokemonEntity>? = null

    /** When present, alters the client-side appearance of the [PokemonEntity]. */
    var mockEffect: MocKEffect? = null

    /** Clears all active [EntityEffect]s gracefully. */
    fun wipe(): CompletableFuture<PokemonEntity>? {
        return mockEffect?.end(entity)
    }

    /** Abruptly clears all active [EntityEffect]s.  */
    fun forceWipe() {
        mockEffect = null
    }

    fun saveToNbt(registryLookup: HolderLookup.Provider): CompoundTag {
        val nbt = CompoundTag()
        mockEffect?.let { effect -> nbt.put(DataKeys.ENTITY_EFFECT_MOCK, effect.saveToNbt(registryLookup)) }
        return nbt
    }

    fun loadFromNBT(nbt: CompoundTag, registryLookup: HolderLookup.Provider) {
        if (nbt.contains(DataKeys.ENTITY_EFFECT_MOCK)) {
            val mockTag = nbt.getCompound(DataKeys.ENTITY_EFFECT_MOCK)
            this.mockEffect = EntityEffect.loadFromNbt(mockTag, registryLookup)?.takeIf { it is MocKEffect }?.let { it as MocKEffect }
        }
    }
}