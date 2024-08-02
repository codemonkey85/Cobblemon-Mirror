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
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.tags.CobblemonItemTags
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.IVs
import com.cobblemon.mod.common.pokemon.Nature
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbilityType
import net.minecraft.world.item.Item
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import kotlin.random.Random

interface BreedingLogic {

    /**
     * Takes two [Pokemon] and returns a [BreedingResult].
     *
     * @param mother The mother [Pokemon]
     * @param father The father [Pokemon]
     * @return The [BreedingResult] of the two [Pokemon]
     * @author Apion, Nick, whatsy
     */
    fun breed(mother: Pokemon, father: Pokemon) : BreedingResult {
        if(!this.canBreed(mother, father)) {
            return BreedingResult()
        }
        val form: FormData = this.calculateForm(mother, father)
        val nature: Nature = this.calculateNature(mother, father)
        val ability: Ability = this.calculateAbility(mother, father, form)
        val moveset: MoveSet = this.calculateMoveset(mother, father)
        val ivs: IVs = this.calculateIVs(mother, father)
        val gender = calculateGender(mother, father, form)
        val pokeball: PokeBall = this.calculatePokeball(mother, father)

        val newPoke = EggPokemon(
            ivs,
            nature,
            form.species,
            form,
            ability,
            moveset,
            false,
            gender,
            pokeball
        )

        return BreedingResult(newPoke)
    }

    /**
     * Determines if two [Pokemon] can breed.
     *
     * @param mother The mother [Pokemon]
     * @param father The father [Pokemon]
     * @return Whether the two [Pokemon] can breed
     * @author Apion, whatsy
     */
    fun canBreed(mother: Pokemon, father: Pokemon): Boolean {
        return true
    }

    /**
     * Determines the [Gender] of a child
     *
     * @param mother The mother [Pokemon]
     * @param father The father [Pokemon]
     * @param form The [FormData] of the child
     * @return The [Gender] of the child
     * @author Apion, whatsy
     */
    fun calculateGender(mother: Pokemon, father: Pokemon, form: FormData): Gender {
        val maleRatio = form.maleRatio
        if (maleRatio.toDouble() == -1.0) {
            return Gender.GENDERLESS
        }
        val rand = Random.nextDouble(0.0, 1.0)
        return if (rand < maleRatio) {
            Gender.MALE
        } else {
            Gender.FEMALE
        }
    }

    /**
     * Determines the [FormData] of a child
     *
     * @param mother The mother [Pokemon]
     * @param father The father [Pokemon]
     * @return The [FormData] of the child
     * @author Nick, Apion, whatsy
     */
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
         * - The form that Scatterbug will evolve into is predetermined when the Egg is first obtained, not when hatched or evolved.
         * - Scatterbug hatched in Scarlet and Violet will only evolve into Fancy Pattern Vivillon.
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
        return nonDittoPreferMother(mother, father).form
    }

    /**
     * Determines the [Nature] of a child
     *
     * @param mother The mother [Pokemon]
     * @param father The father [Pokemon]
     * @return The [Nature] of the child
     * @author Nick, whatsy
     */
    fun calculateNature(mother: Pokemon, father: Pokemon): Nature {
        //FIXME: Use registry lookup
        val dad = father.heldItemNoCopy().item.let { BuiltInRegistries.ITEM.wrapAsHolder(it) }.`is`(CobblemonItemTags.EVERSTONE) ?: false
        val mom = mother.heldItemNoCopy().item.let { BuiltInRegistries.ITEM.wrapAsHolder(it) }.`is`(CobblemonItemTags.EVERSTONE) ?: false

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

    /**
     * Determines the [Ability] of a child
     *
     * @param mother The mother [Pokemon]
     * @param father The father [Pokemon]
     * @param child The [FormData] of the child
     * @return The [Ability] of the child
     * @author Nick, whatsy
     */
    fun calculateAbility(mother: Pokemon, father: Pokemon, child: FormData): Ability {
        val parent = this.nonDittoPreferMother(mother, father)
        val hidden = parent.form.abilities.filter { it.type == HiddenAbilityType }
            .map { it.template.name }
            .map { it == parent.ability.name }
            .firstOrNull()

        return if(hidden == true) {
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

    /**
     * Determines the [MoveSet] of a child
     *
     * @param mother The mother [Pokemon]
     * @param father The father [Pokemon]
     * @return The [MoveSet] of the child
     * @author Nick, Apion, whatsy
     */
    // TODO: This, based off of https://pokemondb.net/mechanics/breeding
    fun calculateMoveset(mother: Pokemon, father: Pokemon): MoveSet {
        val momMoveSet = mother.moveSet
        val dadMoveSet = father.moveSet
        return mother.moveSet
    }

    /**
     * Determines the [IVs] of a child
     *
     * @param mother The mother [Pokemon]
     * @param father The father [Pokemon]
     * @return The [IVs] of the child
     * @author Nick, whatsy, Apion
     */
    fun calculateIVs(mother: Pokemon, father: Pokemon): IVs {
        //FIXME: Use registry holder
        val motherItem = mother.heldItemNoCopy().item.let { BuiltInRegistries.ITEM.wrapAsHolder(it) }
        val fatherItem = father.heldItemNoCopy().item.let { BuiltInRegistries.ITEM.wrapAsHolder(it) }
        val isMotherDestinyKnotted = motherItem.`is`(CobblemonItemTags.DESTINY_KNOT)
        val isFatherDestinyKnotted = fatherItem.`is`(CobblemonItemTags.DESTINY_KNOT)
        val passedStats = mutableSetOf<Stat>()
        val childStats = IVs.createRandomIVs()
        var numPassed = if(isMotherDestinyKnotted || isFatherDestinyKnotted) {
            5
        } else {
            3
        }

        fun calcForcedStat(holder: Holder<Item>?, mon: Pokemon) {
            fun passStat(stats: Stat) {
                if (!passedStats.contains(stats)) {
                    childStats[stats] = mon.ivs[stats]!!
                    passedStats.add(stats)
                    numPassed--
                }
            }
            if(holder == null) {
                return
            }

            if(holder.`is`(CobblemonItemTags.POWER_ANKLET)) {
                passStat(Stats.SPEED)
            } else if(holder.`is`(CobblemonItemTags.POWER_BAND)) {
                passStat(Stats.SPECIAL_DEFENCE)
            } else if(holder.`is`(CobblemonItemTags.POWER_BELT)) {
                passStat(Stats.DEFENCE)
            } else if(holder.`is`(CobblemonItemTags.POWER_BRACER)) {
                passStat(Stats.ATTACK)
            } else if(holder.`is`(CobblemonItemTags.POWER_LENS)) {
                passStat(Stats.SPECIAL_ATTACK)
            } else if(holder.`is`(CobblemonItemTags.POWER_WEIGHT)) {
                passStat(Stats.HP)
            }
        }
        calcForcedStat(fatherItem, father)
        calcForcedStat(motherItem, mother)

        val parentIvs = mother.ivs.toMutableList()
        parentIvs.addAll(father.ivs)
        while (numPassed > 0 && parentIvs.isNotEmpty()) {
            val statPicked: Map.Entry<Stat, Int> = parentIvs.random()
            if (!passedStats.contains(statPicked.key)) {
                passedStats.add(statPicked.key)
                childStats[statPicked.key] = statPicked.value
                parentIvs.removeAll {it.key == statPicked.key}
                numPassed--
            }
        }

        return childStats
    }

    /**
     * Determines the [PokeBall] of a child
     *
     * @param mother The mother [Pokemon]
     * @param father The father [Pokemon]
     * @return The [PokeBall] of the child
     * @author Nick, whatsy
     */
    fun calculatePokeball(mother: Pokemon, father: Pokemon): PokeBall {
        /*
         * The Master Ball, Cherish Ball, and Strange Ball cannot be inherited via breeding; instead, the game treats
         *  any parent obtained in those balls as if they were in a standard Poké Ball for the purposes of inheritance.
         *  To add Poké Balls to this list, add to the defaulting_poke_balls.json item tag.
         */
        val anyDitto = listOf(mother, father).any { it.form.eggGroups.contains(EggGroup.DITTO) }

        val motherBallItem = mother.caughtBall.item().let { BuiltInRegistries.ITEM.wrapAsHolder(it) }
        val fatherBallItem = father.caughtBall.item().let { BuiltInRegistries.ITEM.wrapAsHolder(it) }
        println(motherBallItem.`is`(CobblemonItemTags.DEFAULTING_POKE_BALLS))
        val motherBall = if (motherBallItem.`is`(CobblemonItemTags.DEFAULTING_POKE_BALLS)) PokeBalls.POKE_BALL else mother.caughtBall
        val fatherBall = if (fatherBallItem.`is`(CobblemonItemTags.DEFAULTING_POKE_BALLS)) PokeBalls.POKE_BALL else father.caughtBall

        val ball: PokeBall = if(anyDitto) {
            if (nonDittoPreferMother(mother, father) == mother) motherBall else fatherBall
        } else if(mother.species.resourceIdentifier == father.species.resourceIdentifier) {
            if(Random.nextInt(100) < 50) {
                motherBall
            } else {
                fatherBall
            }
        } else {
            motherBall
        }

        return ball
    }

    /**
     * If the mother is a Ditto, returns the father.
     * Otherwise, returns the mother.
     *
     * @param mother The mother [Pokemon]
     * @param father The father [Pokemon]
     * @author Nick
     */
    fun nonDittoPreferMother(mother: Pokemon, father: Pokemon): Pokemon {
        return if(mother.form.eggGroups.contains(EggGroup.DITTO)) father else mother
    }
}