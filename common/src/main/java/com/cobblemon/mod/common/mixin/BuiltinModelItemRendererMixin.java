/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.client.render.item.CobblemonBuiltinItemRenderer;
import com.cobblemon.mod.common.client.render.item.CobblemonBuiltinItemRendererRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public class BuiltinModelItemRendererMixin {

    @Inject(method = "renderByItem", at = @At("HEAD"), cancellable = true)
    private void cobblemon$useDynamicItemRenderer(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, CallbackInfo ci) {
        CobblemonBuiltinItemRenderer renderer = CobblemonBuiltinItemRendererRegistry.INSTANCE.rendererOf(stack.getItem());
        if (renderer != null) {
            renderer.render(stack, mode, matrices, vertexConsumers, light, overlay);
            ci.cancel();
        }
    }

}
