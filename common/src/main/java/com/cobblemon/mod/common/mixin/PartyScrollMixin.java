/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.keybind.keybinds.PartySendBinding;
import com.cobblemon.mod.common.item.PokedexItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.Mth;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class PartyScrollMixin {
    @Shadow
    private double accumulatedScrollY;

    @Shadow @Final private Minecraft minecraft;

    @Inject(
            method = "onScroll",
            at = @At(
                    value = "FIELD",
                    target="Lnet/minecraft/client/MouseHandler;accumulatedScrollY:D",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 2,
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    public void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (PartySendBinding.INSTANCE.getWasDown()) {
            int i = (int)accumulatedScrollY;
            if (i > 0) {
                while (i-- > 0) CobblemonClient.INSTANCE.getStorage().shiftSelected(false);
                ci.cancel();
                accumulatedScrollY = 0;
                PartySendBinding.INSTANCE.actioned();
            } else if (i < 0) {
                while (i++ < 0) CobblemonClient.INSTANCE.getStorage().shiftSelected(true);
                ci.cancel();
                accumulatedScrollY = 0;
                PartySendBinding.INSTANCE.actioned();
            }
        } else if (minecraft.player != null && minecraft.player.getMainHandItem().getItem() instanceof PokedexItem) {
            PokedexItem pokedexItem = (PokedexItem) minecraft.player.getMainHandItem().getItem();

            // Check if isScanning is true
            if (pokedexItem.isScanning()) {
                // Adjust zoom level based on scroll direction
                if (vertical != 0) {
                    pokedexItem.zoomLevel += vertical * 0.1;
                    pokedexItem.zoomLevel = Mth.clamp(pokedexItem.zoomLevel, 1.0, 2.3);
                    pokedexItem.changeFOV(70.0); // Update the FOV

                    ci.cancel();
                    accumulatedScrollY = 0;
                }
            }
        }

    }
}