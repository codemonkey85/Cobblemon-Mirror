/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.moves

import com.bedrockk.molang.runtime.struct.MoStruct
import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.moves.animations.ActionEffectTimeline
import com.cobblemon.mod.common.api.moves.categories.DamageCategories
import com.cobblemon.mod.common.api.moves.categories.DamageCategory
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.battles.MoveTarget
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.lang
import com.google.gson.annotations.SerializedName
import com.mojang.serialization.Codec
import net.minecraft.network.chat.MutableComponent

/**
 * This class represents the base of a Move.
 * To build a Move you need to use its template
 *
 * @param name The name in Showdown ID form.
 * @param num The numerical ID of this move on Showdown.
 * @param elementalType The [ElementalType] of this move.
 * @param damageCategory The [DamageCategory] of this move.
 * @param power The base power of this move.
 * @param target The [MoveTarget] of this move.
 * @param accuracy The accuracy of this move.
 * @param pp The power points of this move, these mean how many times the move can be used before some manner of restoration is required.
 * @param priority The priority of this move.
 * @param critRatio The ratio at which this move will land a critical hit.
 * @param effectChances The effect chances if any ordered by effect.
 */
open class MoveTemplate(
    val name: String,
    val num: Int,
    @SerializedName("type")
    val elementalType: ElementalType,
    val damageCategory: DamageCategory,
    val power: Double,
    val target: MoveTarget,
    val accuracy: Double,
    val pp: Int,
    val priority: Int,
    val critRatio: Double,
    val effectChances: Array<Double>,
    val actionEffect: ActionEffectTimeline?
) {
    val struct: MoStruct by lazy {
        QueryStruct(hashMapOf())
            .addFunction("name") { StringValue(name) }
            .addFunction("type") { StringValue(elementalType.name) }
            .addFunction("damage_category") { StringValue(damageCategory.name) }
            .addFunction("power") { DoubleValue(power) }
            .addFunction("target") { StringValue(target.name) }
            .addFunction("accuracy") { DoubleValue(accuracy) }
            .addFunction("pp") { DoubleValue(pp) }
            .addFunction("priority") { DoubleValue(priority) }
            .addFunction("crit_ratio") { DoubleValue(critRatio) }
    }


    val displayName: MutableComponent
        get() = lang("move.$name")
    val description: MutableComponent
        get() = lang("move.$name.desc")
    val maxPp: Int
        get() = 8 * pp / 5
    class Dummy(name: String) : MoveTemplate(
        name = name,
        num = -1,
        elementalType = ElementalTypes.NORMAL,
        damageCategory = DamageCategories.STATUS,
        power = 0.0,
        target = MoveTarget.all,
        accuracy = 100.0,
        pp = 5,
        priority = 0,
        critRatio = 0.0,
        effectChances = emptyArray(),
        actionEffect = null
    )

    companion object {
        fun dummy(name: String) = Dummy(name)

        @JvmStatic
        val BY_STRING_CODEC: Codec<MoveTemplate> = CodecUtils.createByStringCodec(
            Moves::getByName,
            MoveTemplate::name
        ) { id -> "No MoveTemplate for ID $id" }
        val hiddenPowerTable = arrayOf(
            ElementalTypes.FIGHTING,
            ElementalTypes.FLYING,
            ElementalTypes.POISON,
            ElementalTypes.GROUND,
            ElementalTypes.ROCK,
            ElementalTypes.BUG,
            ElementalTypes.GHOST,
            ElementalTypes.STEEL,
            ElementalTypes.FIRE,
            ElementalTypes.WATER,
            ElementalTypes.GRASS,
            ElementalTypes.ELECTRIC,
            ElementalTypes.PSYCHIC,
            ElementalTypes.ICE,
            ElementalTypes.DRAGON,
            ElementalTypes.DARK
        )
    }

    /**
     * Creates the Move with full PP
     */
    fun create() = create(pp)

    /**
     * Creates the Move with given PP out of the normal maximum
     */
    fun create(currentPp: Int) = create(currentPp, 0)

    /**
     * Creates the Move with given current PP and the given raised PP stages.
     */
    fun create(currentPp: Int, raisedPpStages: Int): Move {
        return Move(
            currentPp = currentPp,
            raisedPpStages = raisedPpStages,
            template = this
        )
    }

    fun getEffectiveElementalType(pokemon: Pokemon?) : ElementalType {
        if(pokemon == null) {
            return this.elementalType
        }
        if (name == "hiddenpower") {
            val ivs = pokemon.ivs
            val ivArray = arrayOf(
                ivs[Stats.HP],
                ivs[Stats.ATTACK],
                ivs[Stats.DEFENCE],
                ivs[Stats.SPEED],
                ivs[Stats.SPECIAL_ATTACK],
                ivs[Stats.SPECIAL_DEFENCE]
            ).map { it ?: return@getEffectiveElementalType ElementalTypes.NORMAL }
            var tableIndex = 0
            ivArray.forEachIndexed { index, it ->
                tableIndex += (it % 2) shl index
            }
            tableIndex = tableIndex * 15 / 63
            return hiddenPowerTable[tableIndex.coerceAtMost(hiddenPowerTable.size - 1)]
        }
        // TODO: Handle ability suppression: clientactivebattlepokemon needs data about volatiles
        // TODO: Handle Liquid Voice: need to know what moves have the sound flag
        // TODO: Handle weatherball, naturalgift, judgement, technoblast, terrainpulse, and terrablast
        if (this.elementalType == ElementalTypes.NORMAL) {
            if( this.damageCategory != DamageCategories.STATUS) {
                return when (pokemon.ability.name) {
                    "pixilate" -> ElementalTypes.FAIRY
                    "aerilate" -> ElementalTypes.FLYING
                    "refrigerate" -> ElementalTypes.ICE
                    "galvanize" -> ElementalTypes.ELECTRIC
                    else -> this.elementalType
                }
            }
        } else if (pokemon.ability.name == "normalize") {
            /*
            * Exceptions that ignore normalize that we'll need to deal with at some point:
            * hiddenpower
            * weatherball
            * naturalgift
            * judgement
            * technoblast
            * multi-attack
            * z-moves
            * terrainpulse
            * terrablast?
            * */
            return ElementalTypes.NORMAL
        }
        return this.elementalType
    }
}
