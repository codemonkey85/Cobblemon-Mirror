/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.mixin.pokeball;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.pokeball.PokeBall;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.ModelBakery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Mixin(ModelBakery.class)
public abstract class ModelLoaderMixin {

//    @Shadow protected abstract void addModelToBake(ModelIdentifier modelId, UnbakedModel unbakedModel);
//
//    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 1, shift = At.Shift.BEFORE))
//    private void cobblemon$load3dPokeballModels(BlockColors blockColors, Profiler profiler, Map jsonUnbakedModels, Map blockStates, CallbackInfo ci) throws IOException {
//        profiler.swap(Cobblemon.MODID + "_pokeball_3d_model");
//        for (PokeBall pokeBall : PokeBalls.INSTANCE.all()) {
//            this.addModelToBake(new ModelIdentifier(pokeBall.getModel3d(), "inventory"), loadModelFromJson(pokeBall.getModel3d()));
//        }
//    }
//
//    @Shadow
//    private JsonUnbakedModel loadModelFromJson(Identifier id) throws IOException {
//        return null;
//    }
}
