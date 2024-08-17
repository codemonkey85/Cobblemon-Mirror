/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net/minecraft/world/entity/monster/Phantom$PhantomSweepAttackGoal")
public class EntityPhantomMixin {
    // TODO: Figure out how to access the outer class that wraps SwoopMovementGoal
    // Need to find the cat mons that are near the phantom

//    @Inject(method = "shouldContinue", at = @At(value = "RETURN"), cancellable = true)
//    public void cobblemon$shouldContinue(CallbackInfoReturnable<Boolean> ci) {
////        var goalSelectorField = PhantomEntity.class.getClasses();// PhantomEntity.class.getFields();// .class.getDeclaredField("goalSelector");
//
////        goalSelectorField.setAccessible(true);
////        GoalSelector goalSelector = (GoalSelector) goalSelectorField.get(mobEntity);
////        final SwoopMovementGoal phantom = (SwoopMovementGoal) (Object) this;
////        if(ci.getReturnValue() == false) {
////            var result = this$0.getWorld().getEntitiesByClass(
////                    PokemonEntity.class,
////                    this$0.getBoundingBox().expand(16.0),
////                    entity ->  {
////                        return ((PokemonEntity) entity).getBehaviour().getEntityInteract().getAvoidedByPhantom();
////                    }
////            ).isEmpty();
////
////                ci.setReturnValue(result);
////        }
//    }
}

