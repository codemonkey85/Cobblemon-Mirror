/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.client.CobblemonClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to give us a hook to render the PartyOverlay below the Chat
 *
 * @author Qu
 * @since 2022-02-22
 */
@Mixin(Gui.class)
public class GuiMixin {
    //This injects into the actual BooleanSupplier lambda for the "this.layers.add(layeredDraw, () -> !minecraft.options.hideGui)" call
    // sus af. You have to get the method name by looking at the bytecode of the class. Can do in intellij By clicking View -> Show Bytecode (top left)
    @Inject(method = "method_55797", at = @At("HEAD"), cancellable = true)
    private static void cobblemon$dontRenderUiInPokedex(Minecraft minecraft, CallbackInfoReturnable<Boolean> cir) {
        if (CobblemonClient.INSTANCE.getPokedexUsageContext().getScanningGuiOpen() && minecraft.options.getCameraType().isFirstPerson()) {
            cir.setReturnValue(false);
        }
    }
}
