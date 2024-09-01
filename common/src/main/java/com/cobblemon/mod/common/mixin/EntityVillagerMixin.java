/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.cobblemon.mod.common.villager.VillagerGatherableItems;

import java.util.Objects;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;

@Mixin(Villager.class)
public abstract class EntityVillagerMixin {

    @Inject(method = "wantsToPickUp", at = @At(value = "RETURN"), cancellable = true)
    private void cobblemon$canGather(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
        final Villager villager = (Villager) (Object) this;
        if(ci.getReturnValue() == false && Objects.equals(villager.getVillagerData().getProfession(), VillagerProfession.FARMER) && villager.getInventory().canAddItem(stack)) {
            if(VillagerGatherableItems.INSTANCE.getVillagerGatherableItems().contains(stack.getItem()))
                ci.setReturnValue(true);
        }
    }
}
