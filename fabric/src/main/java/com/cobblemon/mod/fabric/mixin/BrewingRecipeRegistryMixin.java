/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.mixin;

import com.cobblemon.mod.fabric.brewing.CobblemonFabricBreweryRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionBrewing.class)
public class BrewingRecipeRegistryMixin {

    @Inject(method = "isIngredient", at = @At("RETURN"), cancellable = true)
    private void cobblemon$isIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            return;
        }
        cir.setReturnValue(CobblemonFabricBreweryRegistry.INSTANCE.isValidIngredientSlot(stack));
    }

    @Inject(method = "hasMix", at = @At(value = "HEAD"), cancellable = true)
    private void cobblemon$hasMix(ItemStack input, ItemStack ingredient,
        CallbackInfoReturnable<Boolean> cir) {
        if (CobblemonFabricBreweryRegistry.INSTANCE.hasRecipe(input, ingredient)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mix", at = @At("RETURN"), cancellable = true)
    private void cobblemon$mix(ItemStack ingredient, ItemStack input,
        CallbackInfoReturnable<ItemStack> cir) {
        if (!cir.getReturnValue().isEmpty()) {
            final ItemStack result = CobblemonFabricBreweryRegistry.INSTANCE.recipeResultOf(input, ingredient);
            if (!result.isEmpty()) {
                cir.setReturnValue(result);
            }
        }
    }


}
