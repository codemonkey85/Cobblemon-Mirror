/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.ai.EntityBehaviour;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSkeleton.class)
public class EntitySkeletonMixin extends Mob {

    private EntitySkeletonMixin(EntityType<? extends Mob> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "registerGoals", at = @At(value = "TAIL"), cancellable = false)
    private void cobblemon$initGoals(CallbackInfo callbackInfo) {
        final AbstractSkeleton skeleton = (AbstractSkeleton) (Object) this;
        // Pokemon Entities
        this.goalSelector.addGoal(
            3,
            new AvoidEntityGoal<>(
                skeleton,
                PokemonEntity.class,
                6.0f,
                1.0,
                1.2,
                 entity -> ((PokemonEntity)entity).getBehaviour().getEntityInteract().getAvoidedBySkeleton() && ((PokemonEntity)entity).getBeamMode() != 1
            )
        );

        // Players with shoulder mounted Pokemon
        this.goalSelector.addGoal(
            3,
            new AvoidEntityGoal<>(
                skeleton,
                ServerPlayer.class,
                6.0f,
                1.0,
                1.2,
                entity -> EntityBehaviour.Companion.hasSkeletonFearedShoulderMount((ServerPlayer)entity)
            )
        );

    }
}

