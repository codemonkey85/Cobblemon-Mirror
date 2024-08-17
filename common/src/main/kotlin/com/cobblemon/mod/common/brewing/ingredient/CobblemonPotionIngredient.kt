/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.brewing.ingredient

import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.PotionContents
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
class CobblemonPotionIngredient(val potion: Holder<Potion>) : CobblemonIngredient {

    override fun matches(stack: ItemStack): Boolean {
        return stack.get(DataComponents.POTION_CONTENTS)?.`is`(potion) ?: false
    }

    override fun matchingStacks(): List<ItemStack> {
        val list = arrayListOf<ItemStack>()
        list += PotionContents.createItemStack(Items.POTION, potion)
        list += PotionContents.createItemStack(Items.SPLASH_POTION, potion)
        list += PotionContents.createItemStack(Items.LINGERING_POTION, potion)
        return list
    }

}