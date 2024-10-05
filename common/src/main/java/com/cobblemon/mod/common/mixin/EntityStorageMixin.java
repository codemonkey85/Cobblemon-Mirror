/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import static com.cobblemon.mod.common.datafixer.CobblemonSchemas.VERSION_KEY;

import com.cobblemon.mod.common.datafixer.CobblemonSchemas;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityStorage.class)
public class EntityStorageMixin {
    //Modifies the NBT returned by the vanilla upgradeChunkTag to also incorporate cobblemon fixup stuff
    @ModifyExpressionValue(
        method = "method_31731(Lnet/minecraft/world/level/ChunkPos;Ljava/util/Optional;)Lnet/minecraft/world/level/entity/ChunkEntities;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/storage/SimpleRegionStorage;upgradeChunkTag(Lnet/minecraft/nbt/CompoundTag;I)Lnet/minecraft/nbt/CompoundTag;"
        )
    )
    public CompoundTag cobblemon$doEntityFix(
        CompoundTag vanillaFixed
    ) {
        int curVersion = vanillaFixed.contains(VERSION_KEY) ? vanillaFixed.getInt(VERSION_KEY) : 0;
        CompoundTag newTag =  DataFixTypes.ENTITY_CHUNK.update(CobblemonSchemas.getDATA_FIXER(), vanillaFixed, curVersion, CobblemonSchemas.DATA_VERSION);
        newTag.put(VERSION_KEY, IntTag.valueOf(CobblemonSchemas.DATA_VERSION));
        return newTag;
    }
}
