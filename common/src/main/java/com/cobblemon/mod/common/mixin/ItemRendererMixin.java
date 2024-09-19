/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.ModAPI;
import com.cobblemon.mod.common.client.CobblemonBakingOverrides;
import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.item.PokedexItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
    I think it would be nice to maybe use the existing mixin we have for custom item rendering [BuiltinModelItemRendererMixin]
    Obviously that would require changes. Potentially each item renderer defines what modes it overrides for?
 */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Unique private final String MODEL_PATH = Cobblemon.implementation.getModAPI() == ModAPI.FABRIC ? "fabric_resource" : "standalone";

    @Shadow @Final private ItemModelShaper itemModelShaper;

    @Shadow public abstract void render(ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model);

    @Inject(
        method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void cobblemon$overrideItemModel(ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource multiBufferSource, int light, int overlay, BakedModel model, CallbackInfo ci) {
        boolean shouldBe2d = renderMode == ItemDisplayContext.GUI || renderMode == ItemDisplayContext.FIXED;
        if (!shouldBe2d && stack.getItem() instanceof PokedexItem) {
            BakedModel replacementModel = CobblemonBakingOverrides.INSTANCE.getPokedexOverride(((PokedexItem) stack.getItem()).getType()).getModel();
            if (!cobblemon$isSameModel(model, replacementModel)) {
                ci.cancel();
                render(stack, renderMode, leftHanded, matrices, multiBufferSource, light, overlay, replacementModel);
            }
        } else if (shouldBe2d && stack.getItem() instanceof PokeBallItem pokeBallItem) {
            BakedModel replacementModel = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation(pokeBallItem.getPokeBall().getModel2d(), "inventory"));
            if (!cobblemon$isSameModel(model, replacementModel)) {
                ci.cancel();
                render(stack, renderMode, leftHanded, matrices, multiBufferSource, light, overlay, replacementModel);
            }
        }
    }

    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void cobblemon$bakePokeballModel(ItemStack stack, Level world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        if (stack.getItem() instanceof PokeBallItem pokeBallItem) {
            BakedModel model = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation(pokeBallItem.getPokeBall().getModel3d(), MODEL_PATH));
            ClientLevel clientWorld = world instanceof ClientLevel ? (ClientLevel) world : null;
            BakedModel overriddenModel = model.getOverrides().resolve(model, stack, clientWorld, entity, seed);
            cir.setReturnValue(overriddenModel == null ? this.itemModelShaper.getModelManager().getMissingModel() : overriddenModel);
        }
    }

    // this method aims to cover an issue with modernfixes and it's "dynamic resource" causing the usual .equals check to always fail, resulting in a StackOverflow
    // this falls back to comparing the model contents (e.g. location of the texture, width and height)
    @Unique
    private static boolean cobblemon$isSameModel(BakedModel original, BakedModel replacement) {
        return original.equals(replacement) ||
                       (original.getParticleIcon().contents().name().equals(replacement.getParticleIcon().contents().name()) &&
                                original.getParticleIcon().contents().width() == replacement.getParticleIcon().contents().width() &&
                                original.getParticleIcon().contents().height() == replacement.getParticleIcon().contents().height());
    }

}
