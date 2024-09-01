/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.pokeball;

import com.cobblemon.mod.common.entity.pokeball.WaterDragModifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ThrowableProjectile.class)
public abstract class ThrownEntityMixin extends Entity {

    public ThrownEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @ModifyVariable(method = "tick", at = @At("STORE"), ordinal = 0)
    private float cobblemon$waterDragModifier(float value) {
        if (this.isInWater() && this instanceof WaterDragModifier) {
            return ((WaterDragModifier) this).waterDrag();
        }
        return value;
    }

}
