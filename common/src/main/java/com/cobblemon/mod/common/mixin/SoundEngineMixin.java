/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.client.sound.BattleMusicController;
import com.cobblemon.mod.common.duck.SoundEngineDuck;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin implements SoundEngineDuck {

    @Shadow
    private boolean loaded;

    @Shadow
    private Multimap<SoundSource, SoundInstance> instanceBySource;

    @Shadow @Final
    private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

    @Shadow
    protected abstract void stop(SoundInstance sound);

    @Shadow
    public abstract boolean isActive(SoundInstance sound);

    /** Resumes the Source belonging to the queried SoundInstance. */
    private void resume(SoundInstance sound) {
        ChannelAccess.ChannelHandle sourceManager = this.instanceToChannel.get(sound);
        if (this.loaded && sourceManager != null) sourceManager.execute(Channel::unpause);
    }

    /** Pauses the Source belonging to the queried SoundInstance. */
    private void pause(SoundInstance sound) {
        ChannelAccess.ChannelHandle sourceManager = this.instanceToChannel.get(sound);
        if (this.loaded && sourceManager != null) sourceManager.execute(Channel::pause);
    }

    /** Resumes the SoundInstance(s) queried by id and/or category. */
    @Override
    public void resumeSounds(@Nullable ResourceLocation id, @Nullable SoundSource category) {
        if (category != null) {
            this.instanceBySource.get(category).forEach((sound) -> {
                if (id == null || sound.getLocation().equals(id)) resume(sound);
            });
        } else if (id == null) {
            this.instanceToChannel.keySet().forEach(this::resume);
        } else {
            this.instanceToChannel.keySet().forEach(sound -> {
                if (sound.getLocation().equals(id)) resume(sound);
            });
        }
    }

    /** Pauses the SoundInstances queried by id and/or category. */
    @Override
    public void pauseSounds(@Nullable ResourceLocation id, @Nullable SoundSource category) {
        if (category != null) {
            this.instanceBySource.get(category).forEach((sound) -> {
                if (id == null || sound.getLocation().equals(id)) pause(sound);
            });
        } else if (id == null) {
            this.instanceToChannel.keySet().forEach(this::pause);
        } else {
            this.instanceToChannel.keySet().forEach(sound -> {
                if (sound.getLocation().equals(id)) pause(sound);
            });
        }
    }

    /** Allows stopping all SoundInstances that belong to a queried category. */
    @Inject(method = "stop(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/sounds/SoundSource;)V", at = @At("HEAD"), cancellable = true)
    public void stopSounds(@Nullable ResourceLocation id, @Nullable SoundSource category, CallbackInfo cb) {
        if (id == null && category != null) {
            this.instanceBySource.get(category).forEach(this::stop);
            cb.cancel();
        }
    }

    /** Different behavior for resuming SoundInstances while a BattleMusicInstance is being played. We do not want to resume filtered sounds. */
    @Inject(method = "resume()V", at = @At("HEAD"), cancellable = true)
    public void resume(CallbackInfo cb) {
        if (this.isActive(BattleMusicController.INSTANCE.getMusic())) {
            this.instanceBySource.values().forEach(sound -> {
               if (sound == BattleMusicController.INSTANCE.getMusic() || !BattleMusicController.INSTANCE.getFilteredCategories().contains(sound.getSource())) {
                   resume(sound);
               }
            });
            cb.cancel();
        }
    }
}
