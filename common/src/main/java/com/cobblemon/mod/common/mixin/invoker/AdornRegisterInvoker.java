/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.invoker;

import java.util.List;

import com.cobblemon.mod.common.integration.AdornCompatibility;
import juuxel.adorn.block.variant.BlockVariantSet;
import juuxel.adorn.block.variant.BlockVariantSets;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//This is a yucky hack but better than what we were doing before
@Mixin(BlockVariantSets.class)
public abstract class AdornRegisterInvoker {
    @Final
    @Shadow(remap = false)
    private static List<BlockVariantSet> variantSets;

    @Inject(method = "register()V", at = @At("HEAD"), remap = false)
    private static void register(CallbackInfo ci) {
        variantSets.add(new AdornCompatibility());
    }
}
