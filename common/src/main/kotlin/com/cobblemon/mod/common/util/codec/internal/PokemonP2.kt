/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.codec.internal

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty
import com.cobblemon.mod.common.api.types.tera.TeraType
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.*
import com.cobblemon.mod.common.pokemon.activestate.ShoulderedState
import com.cobblemon.mod.common.pokemon.evolution.CobblemonEvolutionProxy
import com.cobblemon.mod.common.pokemon.evolution.controller.ServerEvolutionController
import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.UUIDUtil
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import java.util.*

internal data class PokemonP2(
    val state: Optional<ShoulderedState>,
    val status: Optional<PersistentStatusContainer>,
    val caughtBall: PokeBall,
    val faintedTimer: Int,
    val healTimer: Int,
    val evolutionController: Optional<ServerEvolutionController.Intermediate>,
    val customProperties: MutableList<CustomPokemonProperty>,
    val nature: Nature,
    val mintedNature: Optional<Nature>,
    val heldItem: ItemStack,
    val persistentData: CompoundTag,
    val tetheringId: Optional<UUID>,
    val teraType: TeraType,
    val dmaxLevel: Int,
    val gmaxFactor: Boolean,
    val tradeable: Boolean
) : Partial<Pokemon> {

    override fun into(other: Pokemon): Pokemon {
        this.state.ifPresent { other.state = it }
        this.status.ifPresent { other.status = it }
        other.caughtBall = this.caughtBall
        other.faintedTimer = this.faintedTimer
        other.healTimer = this.healTimer
        this.evolutionController.ifPresent {
            (other.evolutionProxy as? CobblemonEvolutionProxy)?.overrideController(it.create(other))
        }
        other.customProperties.clear()
        other.customProperties += customProperties
        other.nature = this.nature
        this.mintedNature.ifPresent { other.mintedNature = it }
        other.heldItem = this.heldItem.copy()
        other.persistentData = this.persistentData
        this.tetheringId.ifPresent { other.tetheringId = it }
        other.teraType = this.teraType
        other.dmaxLevel = this.dmaxLevel
        other.gmaxFactor = this.gmaxFactor
        other.tradeable = this.tradeable
        return other
    }

    companion object {
        internal val CODEC: MapCodec<PokemonP2> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ShoulderedState.CODEC.optionalFieldOf(DataKeys.POKEMON_STATE).forGetter(PokemonP2::state),
                PersistentStatusContainer.CODEC.optionalFieldOf(DataKeys.POKEMON_STATUS).forGetter(PokemonP2::status),
                PokeBall.BY_IDENTIFIER_CODEC.fieldOf(DataKeys.POKEMON_CAUGHT_BALL).forGetter(PokemonP2::caughtBall),
                Codec.INT.fieldOf(DataKeys.POKEMON_FAINTED_TIMER).forGetter(PokemonP2::faintedTimer),
                Codec.INT.fieldOf(DataKeys.POKEMON_HEALING_TIMER).forGetter(PokemonP2::healTimer),
                ServerEvolutionController.CODEC.optionalFieldOf(DataKeys.POKEMON_EVOLUTIONS).forGetter(PokemonP2::evolutionController),
                PokemonProperties.CUSTOM_PROPERTIES_CODEC.optionalFieldOf(DataKeys.POKEMON_DATA, arrayListOf()).forGetter(PokemonP2::customProperties),
                Nature.BY_IDENTIFIER_CODEC.fieldOf(DataKeys.POKEMON_NATURE).forGetter(PokemonP2::nature),
                Nature.BY_IDENTIFIER_CODEC.optionalFieldOf(DataKeys.POKEMON_MINTED_NATURE).forGetter(PokemonP2::mintedNature),
                ItemStack.CODEC.optionalFieldOf(DataKeys.HELD_ITEM, ItemStack.EMPTY).forGetter(PokemonP2::heldItem),
                CompoundTag.CODEC.fieldOf(DataKeys.POKEMON_PERSISTENT_DATA).forGetter(PokemonP2::persistentData),
                UUIDUtil.LENIENT_CODEC.optionalFieldOf(DataKeys.TETHERING_ID).forGetter(PokemonP2::tetheringId),
                TeraType.BY_IDENTIFIER_CODEC.fieldOf(DataKeys.POKEMON_TERA_TYPE).forGetter(PokemonP2::teraType),
                CodecUtils.dynamicIntRange(0) { Cobblemon.config.maxDynamaxLevel }.fieldOf(DataKeys.POKEMON_DMAX_LEVEL).forGetter(PokemonP2::dmaxLevel),
                Codec.BOOL.fieldOf(DataKeys.POKEMON_GMAX_FACTOR).forGetter(PokemonP2::gmaxFactor),
                Codec.BOOL.fieldOf(DataKeys.POKEMON_TRADEABLE).forGetter(PokemonP2::tradeable)
            ).apply(instance, ::PokemonP2)
        }

        internal fun from(pokemon: Pokemon): PokemonP2 = PokemonP2(
            Optional.ofNullable(pokemon.state as? ShoulderedState),
            Optional.ofNullable(pokemon.status),
            pokemon.caughtBall,
            pokemon.faintedTimer,
            pokemon.healTimer,
            Optional.ofNullable((pokemon.evolutionProxy.current() as? ServerEvolutionController)?.asIntermediate()),
            pokemon.customProperties,
            pokemon.nature,
            Optional.ofNullable(pokemon.mintedNature),
            pokemon.heldItemNoCopy(),
            pokemon.persistentData,
            Optional.ofNullable(pokemon.tetheringId),
            pokemon.teraType,
            pokemon.dmaxLevel,
            pokemon.gmaxFactor,
            pokemon.tradeable
        )
    }

}