/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.mixin;

import com.cobblemon.mod.common.client.render.shader.CobblemonShaders;
import com.cobblemon.mod.common.util.ShaderRegistryData;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.impl.client.rendering.FabricShaderProgram;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {


    @Inject(method = "reloadShaders", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void lodestone$registerShaders(ResourceProvider factory, CallbackInfo ci, List<Program> list, List<Pair<ShaderInstance, Consumer<ShaderInstance>>> list2) throws IOException {
        CobblemonShaders.INSTANCE.init();
        CobblemonShaders.INSTANCE.getSHADERS_TO_REGISTER().forEach((pair) -> {
            ShaderRegistryData data = pair.getFirst().invoke(factory);
            FabricShaderProgram program = null;
            try {
                program = new FabricShaderProgram(data.getResourceFactory(), data.getShaderName(), data.getVertexFormat());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            list2.add(Pair.of(program, pair.getSecond()));
        });
    }
}
