/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokeball

import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokeball.catching.CaptureEffect
import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier
import com.cobblemon.mod.common.item.PokeBallItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec
import net.minecraft.world.item.ItemStack
import net.minecraft.resources.ResourceLocation

/**
 * Base poke ball object
 * It is intended that there is one poke ball object initialized for a given poke ball type.
 *
 * @property name the poke ball registry name
 * @property catchRateModifier The [CatchRateModifier] of this Pokéball.
 * @property effects list of all [CaptureEffect]s applicable to the Pokéball
 * @property waterDragValue The value of the water drag modifier when the entity travels, default is 0.8.
 * @property model2d The identifier for the resource this Pokéball will use for the 2d model.
 * @property model3d The identifier for the resource this Pokéball will use for the 3d model.
 */
open class PokeBall(
    val name: ResourceLocation,
    val catchRateModifier: CatchRateModifier = CatchRateModifier.DUMMY,
    val effects: List<CaptureEffect> = listOf(),
    val waterDragValue: Float,
    val model2d: ResourceLocation,
    val model3d: ResourceLocation,
    val throwPower: Float,
    val ancient: Boolean
) {

    // This gets attached during item registry
    internal lateinit var item: PokeBallItem

    fun item(): PokeBallItem = this.item

    fun stack(count: Int = 1): ItemStack =
        ItemStack(this.item(), count)

    @Deprecated("This is a temporary solution for the safari ball dilemma", ReplaceWith("target.currentHealth"))
    internal fun hpForCalculation(target: Pokemon): Int = if (this.name == PokeBalls.SAFARI_BALL.name) target.hp else target.currentHealth

    companion object {

        @JvmStatic
        val BY_IDENTIFIER_CODEC: Codec<PokeBall> = CodecUtils.createByIdentifierCodec(
            PokeBalls::getPokeBall,
            PokeBall::name
        ) { identifier -> "No PokeBall for ID $identifier" }

    }

}