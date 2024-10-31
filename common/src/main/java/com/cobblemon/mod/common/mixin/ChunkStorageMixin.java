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
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkStorage.class)
public class ChunkStorageMixin {
    //Modifies the NBT returned by the vanilla upgradeChunkTag to also incorporate cobblemon fixup stuff
    @ModifyExpressionValue(
        method = "upgradeChunkTag(Lnet/minecraft/resources/ResourceKey;Ljava/util/function/Supplier;Lnet/minecraft/nbt/CompoundTag;Ljava/util/Optional;)Lnet/minecraft/nbt/CompoundTag;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/datafix/DataFixTypes;updateToCurrentVersion(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/CompoundTag;I)Lnet/minecraft/nbt/CompoundTag;"
        )
    )
    public CompoundTag cobblemon$doChunkFix(
        CompoundTag vanillaFixed
    ) {
        int curVersion = vanillaFixed.contains(VERSION_KEY) ? vanillaFixed.getInt(VERSION_KEY) : 0;
        return DataFixTypes.CHUNK.update(CobblemonSchemas.getDATA_FIXER(), vanillaFixed, curVersion, CobblemonSchemas.DATA_VERSION);
    }

    @Inject(
        method = "upgradeChunkTag(Lnet/minecraft/resources/ResourceKey;Ljava/util/function/Supplier;Lnet/minecraft/nbt/CompoundTag;Ljava/util/Optional;)Lnet/minecraft/nbt/CompoundTag;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/nbt/NbtUtils;addCurrentDataVersion(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;"
        )
    )
    public void cobblemon$addChunkVersion(
        ResourceKey<Level> resourceKey,
        Supplier<DimensionDataStorage> supplier,
        CompoundTag compoundTag,
        Optional<ResourceKey<MapCodec<? extends ChunkGenerator>>> optional,
        CallbackInfoReturnable<CompoundTag> cir
    ) {
        compoundTag.put(VERSION_KEY, IntTag.valueOf(CobblemonSchemas.DATA_VERSION));
    }
}
