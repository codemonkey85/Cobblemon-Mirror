/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.api.scheduling.ClientTaskTracker;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Unique
    long lastTime = -1;

    @Inject(
            method = "render",
            at = @At(value = "TAIL")
    )
    public void render(DeltaTracker counter, boolean tick, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        long newTime = System.currentTimeMillis();
        // Don't play scheduled animations when the game is paused
        if (client.isPaused()) {
            lastTime = newTime;
            return;
        }

        if (lastTime != -1) {
            ClientTaskTracker.INSTANCE.update((newTime - lastTime) / 1000F);
        }
        lastTime = newTime;
    }

}
