/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.client.CobblemonBakingOverrides;
import com.cobblemon.mod.common.item.PokedexItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
    I think it would be nice to maybe use the existing mixin we have for custom item rendering [BuiltinModelItemRendererMixin]
    Obviously that would require changes. Potentially each item renderer defines what modes it overrides for?
 */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow @Final private ItemModelShaper itemModelShaper;

    @Shadow public abstract void render(ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model);

    @Inject(
        method = "Lnet/minecraft/client/renderer/entity/ItemRenderer;render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void cobblemon$overridePokedexModel(ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource multiBufferSource, int i, int j, BakedModel model, CallbackInfo ci) {
        boolean shouldOverride = renderMode != ItemDisplayContext.GUI && renderMode != ItemDisplayContext.FIXED && renderMode != ItemDisplayContext.GROUND;
        if (shouldOverride && stack.getItem() instanceof PokedexItem) {
            BakedModel replacementModel = CobblemonBakingOverrides.INSTANCE.getPokedexOverride(((PokedexItem) stack.getItem()).getType()).getModel();
            if (!model.equals(replacementModel)) {
                ci.cancel();
                render(stack, renderMode, leftHanded, matrices, multiBufferSource, i, j, replacementModel);
            }
        }
    }

}
