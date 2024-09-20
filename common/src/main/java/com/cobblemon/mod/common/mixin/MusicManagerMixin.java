/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.client.sound.BattleMusicController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public class MusicManagerMixin {

    @Shadow @Final
    private Minecraft minecraft;

    @Shadow @Nullable
    private SoundInstance currentMusic;

    /** Freezes the tracker while a BattleMusicInstance is in progress. Current SoundInstance being tracked will be paused by BattleMusicHandler. */
    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo cb) {
        if (this.minecraft.getSoundManager().isActive(BattleMusicController.INSTANCE.getMusic())) cb.cancel();
    }
}
