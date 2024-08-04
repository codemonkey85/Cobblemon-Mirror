/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone;
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin implements Bone {
    @Shadow public abstract void render(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color);

    @Shadow public abstract void translateAndRotate(PoseStack matrixStack);

    @Shadow @Final
    public Map<String, ModelPart> children;

    @NotNull
    @Override
    public Map<String, Bone> getChildren() {
        return (Map<String, Bone>) (Object) children;
    }

    @Override
    public void render(RenderContext context, PoseStack stack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.render(stack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public void transform(PoseStack matrixStack) {
        this.translateAndRotate(matrixStack);
    }
}
