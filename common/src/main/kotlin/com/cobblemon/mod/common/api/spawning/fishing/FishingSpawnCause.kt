/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.fishing

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.fishing.FishingBait
import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.item.interactive.PokerodItem
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import kotlin.random.Random.Default.nextInt

/**
 * A spawning cause that is embellished with fishing information. Could probably also
 * have the bobber entity or something.
 *
 * @author Hiroku
 * @since February 3rd, 2024
 */
class FishingSpawnCause(
    spawner: Spawner,
    bucket: SpawnBucket,
    entity: Entity?,
    val rodStack: ItemStack
) : SpawnCause(spawner, bucket, entity) {
    companion object {
        const val FISHED_ASPECT = "fished"
    }

    val rodItem = rodStack.item as? PokerodItem
    val bait = PokerodItem.getBaitOnRod(rodStack)

    override fun affectSpawn(entity: Entity) {
        super.affectSpawn(entity)
        if (entity is PokemonEntity) {
            entity.pokemon.forcedAspects += FISHED_ASPECT

            bait?.effects?.forEach { it ->
                if (Math.random() > it.chance) return
                when (it.type) {
                    FishingBait.Effects.SHINY_REROLL -> shinyReroll(entity, it)
                    FishingBait.Effects.NATURE -> alterNatureAttempt(entity, it)
                    FishingBait.Effects.IV -> alterIVAttempt(entity, it)
                    FishingBait.Effects.GENDER_CHANCE -> alterGenderAttempt(entity, it)
                    FishingBait.Effects.LEVEL_RAISE -> alterLevelAttempt(entity, it)
                    FishingBait.Effects.TERA -> alterTeraAttempt(entity, it)
                    FishingBait.Effects.HIDDEN_ABILITY_CHANCE -> alterHAAttempt(entity)
                    FishingBait.Effects.FRIENDSHIP -> alterFriendshipAttempt(entity, it)
                }
            }
        }
    }

    override fun affectWeight(detail: SpawnDetail, ctx: SpawningContext, weight: Float): Float {
        if (bait != null){
            if (detail is PokemonSpawnDetail) {
               val detailSpecies = detail.pokemon.species?.let { PokemonSpecies.getByName(it) }

                val baitEVEffect = bait.effects.firstOrNull { it.type == FishingBait.Effects.EV && detailSpecies?.evYield?.get(Stats.getStat(it.subcategory?.path.toString()))!! > 0 }

               if(detailSpecies != null && baitEVEffect != null) {
                   return super.affectWeight(detail, ctx, weight * baitEVEffect.value.toFloat())
               }
            }
        }
        return super.affectWeight(detail, ctx, weight)
    }

    private fun shinyReroll(pokemonEntity: PokemonEntity, effect: FishingBait.Effect) {
        if (pokemonEntity.pokemon.shiny) return

        val shinyOdds = Cobblemon.config.shinyRate.toInt()
        val randomNumber = nextInt(0, shinyOdds + 1)

        if (randomNumber <= (effect.value).toInt()) {
            pokemonEntity.pokemon.shiny = true
        }
    }

    private fun alterNatureAttempt(pokemonEntity: PokemonEntity, effect: FishingBait.Effect) {
        // TIMNOTE: This replaces the static lists. It's less performant because it's being reviewed every time,
        // but also it's not something that goes off too often.
        val possibleNatures = Natures.all().filter { it.increasedStat?.identifier == effect.subcategory }
        if (possibleNatures.isEmpty() || possibleNatures.any { it == pokemonEntity.pokemon.nature }) return
        val takenNature = possibleNatures.random()

        pokemonEntity.pokemon.nature = Natures.getNature(takenNature.name.namespace) ?: return
    }

    private fun alterIVAttempt(pokemonEntity: PokemonEntity, effect: FishingBait.Effect) {
        val iv = effect.subcategory ?: return

        if ((pokemonEntity.pokemon.ivs[Stats.getStat(iv.namespace)] ?: 0) + effect.value > 31) // if HP IV is already less than 3 away from 31
            pokemonEntity.pokemon.ivs[Stats.getStat(iv.namespace)] = 31
        else
            pokemonEntity.pokemon.ivs[Stats.getStat(iv.namespace)] = (pokemonEntity.pokemon.ivs[Stats.getStat(iv.namespace)] ?: 0) + (effect.value).toInt()
    }

    private fun alterGenderAttempt(pokemonEntity: PokemonEntity, effect: FishingBait.Effect) {
        if (pokemonEntity.pokemon.species.maleRatio > 0 && pokemonEntity.pokemon.species.maleRatio < 1) // if the pokemon is allowed to be male or female
            when (effect.subcategory) {
                cobblemonResource("male") -> if (pokemonEntity.pokemon.gender != Gender.MALE) pokemonEntity.pokemon.gender = Gender.MALE
                cobblemonResource("female") -> if (pokemonEntity.pokemon.gender != Gender.FEMALE) pokemonEntity.pokemon.gender = Gender.FEMALE
            }
    }

    private fun alterLevelAttempt(pokemonEntity: PokemonEntity, effect: FishingBait.Effect) {
        var level = pokemonEntity.pokemon.level + effect.value.toInt()
        if (level > Cobblemon.config.maxPokemonLevel)
            level = Cobblemon.config.maxPokemonLevel
        else
            pokemonEntity.pokemon.level = level
    }

    private fun alterTeraAttempt(pokemonEntity: PokemonEntity, effect: FishingBait.Effect) {
        if (pokemonEntity.pokemon.teraType == effect.subcategory?.let { ElementalTypes.get(it.namespace) } ||
            ElementalTypes.get(effect.subcategory!!.namespace) == null) return


        pokemonEntity.pokemon.teraType = ElementalTypes.get(effect.subcategory.namespace)!!
    }

    private fun alterHAAttempt(pokemonEntity: PokemonEntity) {
        val species = pokemonEntity.pokemon.species.let { PokemonSpecies.getByName(it.name) } ?: return
        val ability = species.abilities.mapping[Priority.LOW]?.first()?.template ?: return
        pokemonEntity.pokemon.ability = ability.create()

        // Old code from Licious that might be helpful if the above proves to not work

        /*
        // This will iterate from highest to lowest priority
        pokemon.form.abilities.mapping.values.forEach { abilities ->
            abilities.filterIsInstance<HiddenAbility>()
                    .randomOrNull ()
                    ?.let { ability ->
                        // No need to force, this is legal
                        pokemon.ability = ability.template.create(false)
                        return true
                    }
        }
        // There was never a hidden ability :( possible but not by default
        return

         */
    }

    private fun alterFriendshipAttempt(pokemonEntity: PokemonEntity, effect: FishingBait.Effect) {
        if (pokemonEntity.pokemon.friendship + effect.value > 255)
            pokemonEntity.pokemon.setFriendship(255)
        else
            pokemonEntity.pokemon.setFriendship(pokemonEntity.pokemon.friendship + effect.value.toInt())
    }
}