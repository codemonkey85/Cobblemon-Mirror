/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.breeding

import com.cobblemon.mod.common.api.abilities.Ability
import com.cobblemon.mod.common.api.moves.MoveSet
import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.tags.CobblemonItemTags
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.IVs
import com.cobblemon.mod.common.pokemon.Nature
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbilityType
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.entry.RegistryEntry
import kotlin.random.Random

interface BreedingLogic {

    fun breed(mother: Pokemon, father: Pokemon) : BreedingResult {
        if(!this.canBreed(mother, father)) {
            return BreedingResult()
        }

        val form: FormData = this.calculateForm(mother, father)
        val nature: Nature = this.calculateNature(mother, father)
        val ability: Ability = this.calculateAbility(mother, father, form)
        val moveset: MoveSet = this.calculateMoveset(mother, father)
        val ivs: IVs = this.calculateIVs(mother, father)
        val pokeball: PokeBall = this.calculatePokeball(mother, father)

        return BreedingResult(null)
    }

    fun canBreed(mother: Pokemon, father: Pokemon): Boolean {
        return false
    }

    fun calculateForm(mother: Pokemon, father: Pokemon): FormData {
        /*
         * In most cases, if a hatched species has multiple forms (not dependent on in-battle conditions), it will
         * often inherit the form of the mother or non-Ditto parent. Burmy will always hatch with the same cloak as
         * its mother, and if bred between Mothim and Ditto, it will always hatch with a Plant Cloak. Rockruff with the
         * Ability Own Tempo, which evolves into Lycanroc's Dusk Form, is treated as a separate form and can pass down
         * this trait accordingly to its offspring. Oricorio, which changes its form using nectars, can also pass down
         * its form by breeding.
         *
         * There are a few exceptions to this inheritance pattern:
         *
         * - Rotom will always hatch into its normal form, but can be changed afterwards.
         * - Vivillon's pattern depends on the real-world, geographic area of the save file it originated from, regardless of its parent's pattern.
         *  - The form that Scatterbug will evolve into is predetermined when the Egg is first obtained, not when hatched or evolved.
         *  - Scatterbug hatched in Scarlet and Violet will only evolve into Fancy Pattern Vivillon.
         * - Furfrou, whose trims are temporary, always hatch in its Natural Form.
         * - Sinistea will always hatch as a Phony Form, even if its parent is an Antique Form.
         *
         * In addition, species with regional forms will hatch into whichever form is native to that region. However,
         * if a parent of a foreign form and the same family is holding an Everstone, the offspring will be of that
         * parent's form instead.
         *
         * The Gigantamax Factor, Alpha, Jumbo and Mini marks cannot be passed down by breeding.
         *
         * Pokémon such as Dunsparce, Wurmple, and Tandemaus, where the species and form they will evolve into is
         * predetermined when they are first generated, do not follow form inheritance rules. The forms of the parents
         * have no influence on the personality value or encryption constant.
         *
         * TODO - Handle this via form data parameters?
         */
        return FormData()
    }

    fun calculateNature(mother: Pokemon, father: Pokemon): Nature {
        val mom = mother.heldItemNoCopy().item?.let { Registries.ITEM.getEntry(it) }?.isIn(CobblemonItemTags.EVERSTONE) ?: false
        val dad = father.heldItemNoCopy().item?.let { Registries.ITEM.getEntry(it) }?.isIn(CobblemonItemTags.EVERSTONE) ?: false

        return if(mom && dad) {
            if(Random.nextInt(100) < 50) {
                mother.nature
            } else {
                father.nature
            }
        } else if(mom) {
            mother.nature
        } else if(dad) {
            father.nature
        } else {
            Natures.getRandomNature()
        }
    }

    fun calculateAbility(mother: Pokemon, father: Pokemon, child: FormData): Ability {
        val parent = this.nonDittoPreferMother(mother, father)
        val hidden = parent.form.abilities.filter { it.type == HiddenAbilityType }
            .map { it.template.name }
            .map { it == parent.ability.name }
            .first()

        return if(hidden) {
            val rng = Random.nextInt(0, 100) < 60
            if(rng) {
                parent.ability
            } else {
                child.abilities.select(child.species, child.aspects.toSet()).first
            }
        } else {
            val rng = Random.nextInt(0, 100) < 80
            if(rng) {
                parent.ability
            } else {
                child.abilities.select(child.species, child.aspects.toSet()).first
            }
        }
    }

    fun calculateMoveset(mother: Pokemon, father: Pokemon): MoveSet {
        return mother.moveSet
    }

    fun calculateIVs(mother: Pokemon, father: Pokemon): IVs {
        val motherItem = mother.heldItemNoCopy().item?.let { Registries.ITEM.getEntry(it) }
        val fatherItem = father.heldItemNoCopy().item?.let { Registries.ITEM.getEntry(it) }

        fun guaranteedStat(entry: RegistryEntry<Item>?): Stat? {
            if(entry == null) {
                return null
            }

            return if(entry.isIn(CobblemonItemTags.POWER_ANKLET)) {
                Stats.SPEED
            } else if(entry.isIn(CobblemonItemTags.POWER_BAND)) {
                Stats.SPECIAL_DEFENCE
            } else if(entry.isIn(CobblemonItemTags.POWER_BELT)) {
                Stats.DEFENCE
            } else if(entry.isIn(CobblemonItemTags.POWER_BRACER)) {
                Stats.ATTACK
            } else if(entry.isIn(CobblemonItemTags.POWER_LENS)) {
                Stats.SPECIAL_ATTACK
            } else if(entry.isIn(CobblemonItemTags.POWER_WEIGHT)) {
                Stats.HP
            } else {
                null
            }
        }

        val isMotherDestinyKnotted = motherItem?.isIn(CobblemonItemTags.DESTINY_KNOT) == true
        val isFatherDestinyKnotted = fatherItem?.isIn(CobblemonItemTags.DESTINY_KNOT) == true
        val perfect = if(isMotherDestinyKnotted || isFatherDestinyKnotted) {
            5
        } else {
            3
        }

        val guaranteed: Stat? = if(isMotherDestinyKnotted) {
            guaranteedStat(fatherItem)
        } else if(isFatherDestinyKnotted) {
            guaranteedStat(motherItem)
        } else {
            null
        }

        val ivs = IVs.createRandomIVs()
        val options = Stats.PERMANENT.shuffled().sortedBy { if(it == guaranteed) -1 else 0 }
        for (index in 0..perfect) {
            ivs[options[index]] = IVs.MAX_VALUE
        }

        return IVs.createRandomIVs()
    }

    fun calculatePokeball(mother: Pokemon, father: Pokemon): PokeBall {
        val anyDitto = listOf(mother, father).any { it.form.eggGroups.contains(EggGroup.DITTO) }

        val ball: PokeBall = if(anyDitto) {
            this.nonDittoPreferMother(mother, father).caughtBall
        } else if(mother.species.resourceIdentifier == father.species.resourceIdentifier) {
            if(Random.nextInt(100) < 50) {
                mother.caughtBall
            } else {
                father.caughtBall
            }
        } else {
            mother.caughtBall
        }

        /*
         * The Master Ball, Cherish Ball, and Strange Ball cannot be inherited via breeding; instead, the game treats
         *  any parent obtained in those balls as if they were in a standard Poké Ball for the purposes of inheritance.
         */
        // TODO - Item Tag for defaulting balls?
        return ball
    }

    fun nonDittoPreferMother(mother: Pokemon, father: Pokemon): Pokemon {
        return if(mother.form.eggGroups.contains(EggGroup.DITTO)) father else mother
    }
}