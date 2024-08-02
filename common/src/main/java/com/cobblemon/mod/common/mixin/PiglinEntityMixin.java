/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.CobblemonItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Piglin.class)
public abstract class PiglinEntityMixin  {
    @Inject(method = "holdInOffHand", at = @At(value = "HEAD"), cancellable = true)
    public void cobblemon$isValidBarteringItem(ItemStack stack, CallbackInfo ci) {
        Piglin entity = (Piglin)(Object) this;
        if (stack.is(CobblemonItems.RELIC_COIN_POUCH)) {
            entity.setItemSlot(EquipmentSlot.OFFHAND, stack);
            entity.setGuaranteedDrop(EquipmentSlot.OFFHAND);
            ci.cancel();
        }
    }
}
