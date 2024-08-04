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
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatures
import com.cobblemon.mod.common.api.pokemon.feature.SynchronizedSpeciesFeature
import com.cobblemon.mod.common.api.pokemon.feature.SynchronizedSpeciesFeatureProvider
import com.cobblemon.mod.common.pokemon.*
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.UUIDUtil
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import java.util.*

internal class ClientPokemonP1(
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
    val features: List<CompoundTag>,
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
        other.moveSet = this.moveSet
        other.benchedMoves = this.benchedMoves
        other.scaleModifier = this.scaleModifier
        this.features.forEach { featureNbt ->
            val featureId = featureNbt.getString(FEATURE_ID)
            if (featureId.isEmpty()) {
                return@forEach
            }
            val speciesFeatureProviders = SpeciesFeatures.getFeaturesFor(this.species)
                .filterIsInstance<SynchronizedSpeciesFeatureProvider<*>>()
            val feature = speciesFeatureProviders.firstNotNullOfOrNull { provider -> provider(featureNbt) } ?: return@forEach
            if (
                featureNbt.contains("keys", Tag.TAG_STRING.toInt()) &&
                !featureNbt.getList("keys", Tag.TAG_STRING.toInt()).contains(StringTag.valueOf(featureId))
            ) {
                return@forEach
            }
            other.features.removeIf { it.name == feature.name }
            other.features.add(feature)
        }
        return other
    }

    companion object {

        private const val FEATURES = "Features"
        private const val FEATURE_ID = "${Cobblemon.MODID}:feature_id"

        internal val CODEC: MapCodec<ClientPokemonP1> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                UUIDUtil.LENIENT_CODEC.fieldOf(DataKeys.POKEMON_UUID).forGetter(ClientPokemonP1::uuid),
                Species.BY_IDENTIFIER_CODEC.fieldOf(DataKeys.POKEMON_SPECIES_IDENTIFIER).forGetter(ClientPokemonP1::species),
                Codec.STRING.fieldOf(DataKeys.POKEMON_FORM_ID).forGetter { pokemon -> pokemon.form.formOnlyShowdownId() },
                ComponentSerialization.CODEC.optionalFieldOf(DataKeys.POKEMON_NICKNAME).forGetter(ClientPokemonP1::nickname),
                CodecUtils.dynamicIntRange(1) { Cobblemon.config.maxPokemonLevel }.fieldOf(DataKeys.POKEMON_LEVEL).forGetter(ClientPokemonP1::level),
                Codec.intRange(0, Int.MAX_VALUE).fieldOf(DataKeys.POKEMON_EXPERIENCE).forGetter(ClientPokemonP1::experience),
                CodecUtils.dynamicIntRange(0) { Cobblemon.config.maxPokemonFriendship }.fieldOf(DataKeys.POKEMON_FRIENDSHIP).forGetter(ClientPokemonP1::friendship),
                Codec.intRange(0, Int.MAX_VALUE).fieldOf(DataKeys.POKEMON_HEALTH).forGetter(ClientPokemonP1::currentHealth),
                Gender.CODEC.fieldOf(DataKeys.POKEMON_GENDER).forGetter(ClientPokemonP1::gender),
                IVs.CODEC.fieldOf(DataKeys.POKEMON_IVS).forGetter(ClientPokemonP1::ivs),
                EVs.CODEC.fieldOf(DataKeys.POKEMON_EVS).forGetter(ClientPokemonP1::evs),
                MoveSet.CODEC.fieldOf(DataKeys.POKEMON_MOVESET).forGetter(ClientPokemonP1::moveSet),
                BenchedMoves.CODEC.fieldOf(DataKeys.BENCHED_MOVES).forGetter(ClientPokemonP1::benchedMoves),
                Codec.FLOAT.fieldOf(DataKeys.POKEMON_SCALE_MODIFIER).forGetter(ClientPokemonP1::scaleModifier),
                Codec.list(CompoundTag.CODEC).fieldOf(FEATURES).forGetter(ClientPokemonP1::features),
                Ability.CODEC.fieldOf(DataKeys.POKEMON_ABILITY).forGetter(ClientPokemonP1::ability)
            ).apply(instance) { uuid, species, formId, nickname, level, experience, friendship, currentHealth, gender, ivs, evs, moveSet, benchedMoves, scaleModifier, shiny, ability ->
                val form = species.forms.firstOrNull { it.formOnlyShowdownId() == formId } ?: species.standardForm
                ClientPokemonP1(uuid, species, form, nickname, level, experience, friendship, currentHealth, gender, ivs, evs, moveSet, benchedMoves, scaleModifier, shiny, ability)
            }
        }

        internal fun from(pokemon: Pokemon): ClientPokemonP1 = ClientPokemonP1(
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
            this.collectVisibleFeatures(pokemon),
            pokemon.ability,
        )

        private fun collectVisibleFeatures(pokemon: Pokemon) = pokemon.features
            .filterIsInstance<SynchronizedSpeciesFeature>()
            .filter { (SpeciesFeatures.getFeature(it.name) as? SynchronizedSpeciesFeatureProvider<*>)?.visible == true }
            .map { feature ->
                val nbt = CompoundTag()
                nbt.putString(FEATURE_ID, feature.name)
                feature.saveToNBT(nbt)
            }

    }

}