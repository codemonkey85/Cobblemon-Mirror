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
import com.cobblemon.mod.common.pokedex.scanner.PokedexUsageContext;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.SmoothDouble;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Shadow
    private double accumulatedScrollY;

    @Shadow @Final private Minecraft minecraft;

    @Shadow @Final private SmoothDouble smoothTurnY;

    @Shadow @Final private SmoothDouble smoothTurnX;

    @Shadow private double accumulatedDX;

    @Shadow private double accumulatedDY;

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
    public void cobblemon$scrollParty(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (PartySendBinding.INSTANCE.getWasDown()) {
            int i = (int) accumulatedScrollY;
            if (i > 0) {
                accumulatedScrollY -= i;
                CobblemonClient.INSTANCE.getStorage().shiftSelected(false);
                ci.cancel();
                PartySendBinding.INSTANCE.actioned();
            } else if (i < 0) {
                accumulatedScrollY -= i;
                CobblemonClient.INSTANCE.getStorage().shiftSelected(true);
                ci.cancel();
                PartySendBinding.INSTANCE.actioned();
            }
        }
    }

    @Inject(
        method = "onScroll",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;swapPaint(D)V"
        ),
        cancellable = true
    )
    public void cobblemon$doPokedexZoom(long window, double horizontal, double vertical, CallbackInfo ci) {
        PokedexUsageContext usageContext = CobblemonClient.INSTANCE.getPokedexUsageContext();
        if (usageContext.getScanningGuiOpen()) {
            usageContext.adjustZoom(vertical);
            ci.cancel();
        }
    }

    @WrapWithCondition(
            method = "turnPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
            )
    )
    public boolean cobblemon$modifyRotation(LocalPlayer player, double cursorDeltaX, double cursorDeltaY, @Local(argsOnly = true) double d) {
        // I know this technically doesn't need to be a WrapWithCondition, but it'll make things easier later for riding.
        PokedexUsageContext usageContext = CobblemonClient.INSTANCE.getPokedexUsageContext();
        if (usageContext.getScanningGuiOpen()) {
            this.smoothTurnY.reset();
            this.smoothTurnX.reset();
            var defaultSensitivity = this.minecraft.options.sensitivity().get() * 0.6000000238418579 + 0.20000000298023224;
            var spyglassSensitivity = Math.pow(defaultSensitivity, 3);
            var lookSensitivity = spyglassSensitivity * 8.0;
            var sensitivity = Mth.lerp(usageContext.getFovMultiplier(), spyglassSensitivity, lookSensitivity);
            player.turn(this.accumulatedDX * sensitivity, (this.accumulatedDY * sensitivity));
            return false;
        }
        return true;
    }

}
