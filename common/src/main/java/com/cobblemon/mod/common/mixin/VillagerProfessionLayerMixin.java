/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonVillagerProfessions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import kotlin.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderLayer.class)
public abstract class VillagerProfessionLayerMixin {
    @Inject(method = "renderColoredCutoutModel", at = @At(value = "HEAD"), cancellable = true)
    private static <T extends LivingEntity> void cobblemon$renderVillagerProfessionLayer(EntityModel<T> entityModel, ResourceLocation resourceLocation, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, int j, CallbackInfo ci) {
        if (resourceLocation.getNamespace().equals(Cobblemon.MODID)) {
            VillagerProfession profession = null;
            boolean isZombie = false;

            if (livingEntity instanceof Villager) profession = ((Villager) livingEntity).getVillagerData().getProfession();
            if (livingEntity instanceof ZombieVillager) {
                profession = ((ZombieVillager) livingEntity).getVillagerData().getProfession();
                isZombie = true;
            }

            Pair<String, String[]> override = CobblemonVillagerProfessions.INSTANCE.getNameTagOverride(profession);
            if (profession != null && override != null) {
                String entityName = ChatFormatting.stripFormatting(livingEntity.getName().getString());
                boolean nameMatches = false;
                for (String name: override.getSecond())
                    if (entityName.endsWith(name)) {
                        nameMatches = true;
                        break;
                    }
                if (nameMatches && resourceLocation.equals(getProfessionResourceLocation(resourceLocation.getNamespace(), ResourceLocation.parse(profession.name()).getPath(), isZombie))) {
                    ResourceLocation textureLocation = getProfessionResourceLocation(resourceLocation.getNamespace(), override.getFirst(), isZombie);
                    VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(textureLocation));
                    entityModel.renderToBuffer(poseStack, vertexConsumer, i, LivingEntityRenderer.getOverlayCoords(livingEntity, 0.0F), j);
                    ci.cancel();
                }
            }
        }
    }

    private static ResourceLocation getProfessionResourceLocation(String namespace, String profession, Boolean isZombie) {
        return ResourceLocation.fromNamespaceAndPath(namespace, "textures/entity/" + (isZombie ? "zombie_villager" : "villager") + "/profession/" + profession + ".png");
    }
}
