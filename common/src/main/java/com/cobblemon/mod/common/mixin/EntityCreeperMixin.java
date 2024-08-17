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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public class EntityCreeperMixin extends Mob {

    private EntityCreeperMixin(net.minecraft.world.entity.EntityType<? extends Creeper> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "registerGoals", at = @At(value = "TAIL"), cancellable = false)
    private void cobblemon$initGoals(CallbackInfo callbackInfo) {
        final Creeper creeper = (Creeper) (Object) this;
        // Pokemon Entities
        this.goalSelector.addGoal(
            3,
            new AvoidEntityGoal<>(
                creeper,
                PokemonEntity.class,
                6.0f,
                1.0,
                1.2,
                 entity -> ((PokemonEntity)entity).getBehaviour().getEntityInteract().getAvoidedByCreeper() && ((PokemonEntity)entity).getBeamMode() != 1
            )
        );

        // Players with shoulder mounted Pokemon
        this.goalSelector.addGoal(
            3,
            new AvoidEntityGoal<>(
                creeper,
                ServerPlayer.class,
                6.0f,
                1.0,
                1.2,
                entity -> EntityBehaviour.Companion.hasCreeperFearedShoulderMount((ServerPlayer)entity)
            )
        );

    }
}

