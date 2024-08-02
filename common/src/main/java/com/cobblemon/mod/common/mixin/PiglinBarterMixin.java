/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.CobblemonItems;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(PiglinAi.class)
public class PiglinBarterMixin {

    @Inject(method = "isBarterCurrency", at = @At(value = "RETURN"), cancellable = true)
    private static void cobblemon$acceptsForBarter(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
        if(!ci.getReturnValue()) {
            //TODO: Make a tag?
            if(stack.is(CobblemonItems.RELIC_COIN_POUCH))
                ci.setReturnValue(true);
        }
    }


}
