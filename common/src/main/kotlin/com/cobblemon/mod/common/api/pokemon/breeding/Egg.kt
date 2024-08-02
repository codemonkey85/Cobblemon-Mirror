/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.breeding

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.block.entity.NestBlockEntity
import com.cobblemon.mod.common.util.DataKeys
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation

data class Egg(
    val hatchedPokemon: EggPokemon,
    val patternId: ResourceLocation,
    val baseColor: String,
    val overlayColor: String?,
    //Time in ticks to hatch
    var timeToHatch: Int
) {

    fun asItemStack(blockEntityNbt: CompoundTag): ItemStack {
        val stack = CobblemonBlocks.EGG.asItem().defaultInstance
        //FIXME: Use properties
        //BlockItem.setBlockEntityNbt(stack, CobblemonBlockEntities.EGG, blockEntityNbt)
        return stack
    }

    fun getPattern(): EggPattern? {
        return EggPatterns.patternMap[patternId]
    }

    companion object {
        fun fromBlockNbt(blockNbt: CompoundTag): Egg {
            val eggNbt = blockNbt.get(DataKeys.EGG) as CompoundTag
            return fromNbt(eggNbt)
        }
        fun fromNbt(nbt: CompoundTag): Egg {
            return Egg(
                EggPokemon.fromNBT(nbt.get(DataKeys.HATCHED_POKEMON) as CompoundTag),
                ResourceLocation.tryParse(nbt.getString(DataKeys.EGG_PATTERN))!!,
                nbt.getString(DataKeys.PRIMARY_COLOR),
                if (nbt.contains(DataKeys.SECONDARY_COLOR)) nbt.getString(DataKeys.SECONDARY_COLOR) else null,
                nbt.getInt(DataKeys.TIME_TO_HATCH)
            )
        }

        //Less expensive than deserializing the whole thing, used for the color provider since it gets
        //called every frame
        fun getColorsFromNbt(nbt: CompoundTag): Pair<String, String> {
            return Pair(
                nbt.getString(DataKeys.PRIMARY_COLOR),
                nbt.getString(DataKeys.SECONDARY_COLOR)
            )
        }
    }
}