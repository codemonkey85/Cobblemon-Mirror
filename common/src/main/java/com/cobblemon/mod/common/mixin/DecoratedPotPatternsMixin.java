/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.sherds.CobblemonSherds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DecoratedPotPatterns.class)
public abstract class DecoratedPotPatternsMixin {
    @Inject(method = "getPatternFromItem", at=@At("HEAD"), cancellable = true)
    private static void cobblemon$getCobblemonSherdTexture(
        Item sherd,
        CallbackInfoReturnable<ResourceKey<DecoratedPotPattern>> cir
    ) {
        if (CobblemonSherds.INSTANCE.getSherdToPattern().containsKey(sherd)) {
            cir.setReturnValue(CobblemonSherds.INSTANCE.getSherdToPattern().get(sherd));
            cir.cancel();
        }
    }
}
