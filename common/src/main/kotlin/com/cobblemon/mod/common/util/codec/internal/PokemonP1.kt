/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.codec.internal

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.abilities.Ability
import com.cobblemon.mod.common.api.moves.BenchedMoves
import com.cobblemon.mod.common.api.moves.MoveSet
import com.cobblemon.mod.common.pokemon.*
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.UUIDUtil
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import java.util.*

internal class PokemonP1(
    val uuid: UUID,
    val species: Species,
    val form: FormData,
    val nickname: Optional<Component>,
    val level: Int,
    val experience: Int,
    val friendship: Int,
    val currentHealth: Int,
    val gender: Gender,
    val ivs: IVs,
    val evs: EVs,
    val moveSet: MoveSet,
    val benchedMoves: BenchedMoves,
    val scaleModifier: Float,
    val shiny: Boolean,
    val ability: Ability
) : Partial<Pokemon> {

    override fun into(other: Pokemon): Pokemon {
        other.uuid = this.uuid
        // This is done beforehand so the ability is legalized by species/form change
        other.ability = this.ability
        other.species = this.species
        other.form = this.form
        this.nickname.ifPresent { other.nickname = it.copy() }
        other.level = this.level
        other.experience = this.experience
        other.setFriendship(this.friendship)
        // Applied before current health for calcs to take place
        other.ivs = this.ivs
        other.evs = this.evs
        other.currentHealth = this.currentHealth
        other.gender = this.gender
        other.moveSet.copyFrom(this.moveSet)
        other.benchedMoves = this.benchedMoves
        other.scaleModifier = this.scaleModifier
        other.shiny = this.shiny
        return other
    }

    companion object {
        internal val CODEC: MapCodec<PokemonP1> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                UUIDUtil.LENIENT_CODEC.fieldOf(DataKeys.POKEMON_UUID).forGetter(PokemonP1::uuid),
                Species.BY_IDENTIFIER_CODEC.fieldOf(DataKeys.POKEMON_SPECIES_IDENTIFIER).forGetter(PokemonP1::species),
                Codec.STRING.fieldOf(DataKeys.POKEMON_FORM_ID).forGetter { pokemon -> pokemon.form.formOnlyShowdownId() },
                ComponentSerialization.CODEC.optionalFieldOf(DataKeys.POKEMON_NICKNAME).forGetter(PokemonP1::nickname),
                CodecUtils.dynamicIntRange(1) { Cobblemon.config.maxPokemonLevel }.fieldOf(DataKeys.POKEMON_LEVEL).forGetter(PokemonP1::level),
                Codec.intRange(0, Int.MAX_VALUE).fieldOf(DataKeys.POKEMON_EXPERIENCE).forGetter(PokemonP1::experience),
                CodecUtils.dynamicIntRange(0) { Cobblemon.config.maxPokemonFriendship }.fieldOf(DataKeys.POKEMON_FRIENDSHIP).forGetter(PokemonP1::friendship),
                Codec.intRange(0, Int.MAX_VALUE).fieldOf(DataKeys.POKEMON_HEALTH).forGetter(PokemonP1::currentHealth),
                Gender.CODEC.fieldOf(DataKeys.POKEMON_GENDER).forGetter(PokemonP1::gender),
                IVs.CODEC.fieldOf(DataKeys.POKEMON_IVS).forGetter(PokemonP1::ivs),
                EVs.CODEC.fieldOf(DataKeys.POKEMON_EVS).forGetter(PokemonP1::evs),
                MoveSet.CODEC.fieldOf(DataKeys.POKEMON_MOVESET).forGetter(PokemonP1::moveSet),
                BenchedMoves.CODEC.fieldOf(DataKeys.BENCHED_MOVES).forGetter(PokemonP1::benchedMoves),
                Codec.FLOAT.fieldOf(DataKeys.POKEMON_SCALE_MODIFIER).forGetter(PokemonP1::scaleModifier),
                Codec.BOOL.fieldOf(DataKeys.POKEMON_SHINY).forGetter(PokemonP1::shiny),
                Ability.CODEC.fieldOf(DataKeys.POKEMON_ABILITY).forGetter(PokemonP1::ability)
            ).apply(instance) { uuid, species, formId, nickname, level, experience, friendship, currentHealth, gender, ivs, evs, moveSet, benchedMoves, scaleModifier, shiny, ability ->
                val form = species.forms.firstOrNull { it.formOnlyShowdownId() == formId } ?: species.standardForm
                PokemonP1(uuid, species, form, nickname, level, experience, friendship, currentHealth, gender, ivs, evs, moveSet, benchedMoves, scaleModifier, shiny, ability)
            }
        }

        internal fun from(pokemon: Pokemon): PokemonP1 = PokemonP1(
            pokemon.uuid,
            pokemon.species,
            pokemon.form,
            Optional.ofNullable(pokemon.nickname),
            pokemon.level,
            pokemon.experience,
            pokemon.friendship,
            pokemon.currentHealth,
            pokemon.gender,
            pokemon.ivs,
            pokemon.evs,
            pokemon.moveSet,
            pokemon.benchedMoves,
            pokemon.scaleModifier,
            pokemon.shiny,
            pokemon.ability,
        )
    }

}
