/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.client.sound.BlockEntitySoundTracker;
import com.cobblemon.mod.common.client.sound.EntitySoundTracker;
import com.cobblemon.mod.common.client.sound.BattleMusicController;
import com.cobblemon.mod.common.client.sound.SoundTracker;
import com.cobblemon.mod.common.duck.SoundManagerDuck;
import com.cobblemon.mod.common.duck.SoundEngineDuck;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.UnderwaterAmbientSoundInstances;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public abstract class SoundManagerMixin implements SoundManagerDuck {

    @Shadow
    public abstract boolean isActive(SoundInstance sound);

    @Shadow @Final
    private SoundEngine soundEngine;

    // Never-ending ambient loops are a special exception (they are only initiated once) and should always be paused if filtered.
    private boolean isAmbientLoop(SoundInstance sound) {
        return sound instanceof BiomeAmbientSoundsHandler.LoopSoundInstance || sound instanceof UnderwaterAmbientSoundInstances.UnderwaterAmbientSoundInstance;
    }

    private boolean filterCondition(SoundInstance sound) {
        return !isAmbientLoop(sound) &&
            this.isActive(BattleMusicController.INSTANCE.getMusic()) &&
                BattleMusicController.INSTANCE.getFilteredCategories().contains(sound.getSource());
    }

    private boolean ambientLoopCondition(SoundInstance sound) {
        return isAmbientLoop(sound) &&
            this.isActive(BattleMusicController.INSTANCE.getMusic()) &&
                BattleMusicController.INSTANCE.getFilteredCategories().contains(sound.getSource());
    }

    /** Pauses the queried SoundInstance(s). If id is null, will pause all sounds belonging to the SoundCategory. */
    @Override
    public void pauseSounds(@Nullable ResourceLocation id, @Nullable SoundSource category) {
        ((SoundEngineDuck)soundEngine).pauseSounds(id, category);
    }

    /** Resumes the queried SoundInstance(s). If id is null, will resume all sounds belonging to the SoundCategory. */
    @Override
    public void resumeSounds(@Nullable ResourceLocation id, @Nullable SoundSource category) {
        ((SoundEngineDuck)soundEngine).resumeSounds(id, category);
    }

    /** Blocks filtered sounds from being played while a BattleMusicInstance is in progress. */
    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    public void playStart(SoundInstance sound, CallbackInfo cb) {
        if (filterCondition(sound)) cb.cancel();
    }

    @Inject(method = "playDelayed(Lnet/minecraft/client/resources/sounds/SoundInstance;I)V", at = @At("HEAD"), cancellable = true)
    public void playStart(SoundInstance sound, int delay, CallbackInfo cb) {
        if (filterCondition(sound)) cb.cancel();
    }

    /** Pauses ambient loops while a BattleMusicInstance is in progress. */
    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V", at = @At("TAIL"))
    public void playEnd(SoundInstance sound, CallbackInfo cb) {
        if (ambientLoopCondition(sound)) this.pauseSounds(sound.getLocation(), SoundSource.AMBIENT);
    }

    @Inject(method = "playDelayed(Lnet/minecraft/client/resources/sounds/SoundInstance;I)V", at = @At("TAIL"))
    public void playEnd(SoundInstance sound, int delay, CallbackInfo cb) {
        if (ambientLoopCondition(sound)) this.pauseSounds(sound.getLocation(), SoundSource.AMBIENT);
    }

    /** Clears tracked sounds on the client when disconnected. */
    @Inject(method = "stop()V", at = @At("TAIL"))
    public void stop(CallbackInfo cb) {
        SoundTracker.Companion.clear();
    }
}
