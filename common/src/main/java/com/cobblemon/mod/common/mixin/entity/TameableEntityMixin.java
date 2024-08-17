/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.entity;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TamableAnimal.class)
public class TameableEntityMixin {

    @Redirect(method = "die", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z"))
    public boolean cobblemon$checkIfPokemonBeforeSendingMessage(Level world) {
        return world.isClientSide || (this.getClass().isAssignableFrom(PokemonEntity.class));
    }

}
