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
import com.cobblemon.mod.common.api.data.ShowdownIdentifiable
import com.cobblemon.mod.common.api.moves.animations.ActionEffectTimeline
import com.cobblemon.mod.common.api.moves.categories.DamageCategory
import com.cobblemon.mod.common.api.registry.RegistryElement
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.battles.MoveTarget
import com.cobblemon.mod.common.registry.CobblemonRegistries
import com.cobblemon.mod.common.util.simplify
import com.mojang.serialization.Codec
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.Registry
import net.minecraft.core.RegistryCodecs
import net.minecraft.network.chat.Component
import net.minecraft.resources.RegistryFixedCodec
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey

/**
 * This class represents the base of a Move.
 *
 * @property power The base power of this move.
 * @property accuracy The accuracy of this move.
 * @property pp The power points of this move, these mean how many times the move can be used before some manner of restoration is required.
 * @property damageCategory The [DamageCategory] of this move.
 * //@property type The [ElementalType] of this move.
 * @property priority The priority of this move.
 * @property target The [MoveTarget] of this move.
 * @property flags The [MoveFlag]s of this move.
 * @property noPpBoosts
 * @property critRatio The ratio at which this move will land a critical hit.
 * @property effectChances The effect chances if any ordered by effect.
 * @property actionEffect The [ActionEffectTimeline] of this move if any.
 * @property displayName The display name of this move.
 * @property description The description of this move.
 */
class MoveTemplate(
    val power: Int,
    val accuracy: Float,
    val pp: Int,
    val damageCategory: DamageCategory,
    private val typeKey: ResourceLocation,
    val priority: Int,
    val target: MoveTarget,
    val flags: Set<MoveFlag>,
    // TODO: val damage: Optional<FixedDamage> type: number | 'level',
    val noPpBoosts: Boolean,
    // TODO: val gimmickType: Optional<GimmickMove>
    val critRatio: Float,
    val effectChances: Array<Float>,
    val actionEffect: ActionEffectTimeline?,
    val displayName: Component,
    val description: Component,
): RegistryElement<MoveTemplate>, ShowdownIdentifiable {

    val struct: MoStruct by lazy {
        QueryStruct(hashMapOf())
            .addFunction("id") { StringValue(resourceLocation().simplify()) }
            .addFunction("type") { StringValue(type.resourceLocation().simplify()) }
            .addFunction("damage_category") { StringValue(damageCategory.name) }
            .addFunction("power") { DoubleValue(power) }
            .addFunction("target") { StringValue(target.name) }
            .addFunction("accuracy") { DoubleValue(accuracy) }
            .addFunction("pp") { DoubleValue(pp) }
            .addFunction("priority") { DoubleValue(priority) }
            .addFunction("crit_ratio") { DoubleValue(critRatio) }
    }
    val maxPp: Int
        get() = if (this.noPpBoosts) this.pp else 8 * pp / 5

    // TODO: Remove me later when this is a dynamic registry since elemental type will load earlier.
    /**
     * The [ElementalType] of this move.
     */
    val type: ElementalType
        get() = CobblemonRegistries.ELEMENTAL_TYPE.getOrThrow(
            ResourceKey.create(
                CobblemonRegistries.ELEMENTAL_TYPE_KEY,
                this.typeKey
            )
        )

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

    override fun registry(): Registry<MoveTemplate> = CobblemonRegistries.MOVE

    override fun resourceKey(): ResourceKey<MoveTemplate> = this.registry().getResourceKey(this)
        .orElseThrow { IllegalStateException("Unregistered MoveTemplate") }

    override fun isTaggedBy(tag: TagKey<MoveTemplate>): Boolean = this.registry()
        .getHolder(this.resourceKey())
        .orElseThrow { IllegalStateException("Unregistered MoveTemplate") }
        .`is`(tag)

    override fun showdownId(): String {
        return ShowdownIdentifiable.EXCLUSIVE_REGEX.replace(this.resourceLocation().simplify(), "")
    }

    companion object {
        @JvmStatic
        val CODEC: Codec<Holder<MoveTemplate>> = RegistryFixedCodec.create(CobblemonRegistries.MOVE_KEY)
        @JvmStatic
        val LIST_CODEC: Codec<HolderSet<MoveTemplate>> = RegistryCodecs.homogeneousList(CobblemonRegistries.MOVE_KEY)
    }
}
