/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.world.CobblemonStructureIDs;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.*;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;

@Mixin(JigsawPlacement.Placer.class)
public abstract class StructurePoolGeneratorMixin {

    Map<String, Integer> generatedStructureGroupCounts;

    private static final Map<String, Integer> structureMaxes;
    private static final Map<ResourceLocation, Set<String>> structureGroups = new HashMap<>();
    static {
        String pokecenter = "pokecenter";
        String berry = "berry_farm";

        structureGroups.put(CobblemonStructureIDs.VILLAGE_PLAINS_POKECENTER, Set.of(pokecenter));
        structureGroups.put(CobblemonStructureIDs.VILLAGE_DESERT_POKECENTER, Set.of(pokecenter));
        structureGroups.put(CobblemonStructureIDs.VILLAGE_SAVANNA_POKECENTER, Set.of(pokecenter));
        structureGroups.put(CobblemonStructureIDs.VILLAGE_SNOWY_POKECENTER, Set.of(pokecenter));
        structureGroups.put(CobblemonStructureIDs.VILLAGE_TAIGA_POKECENTER, Set.of(pokecenter));

        structureGroups.put(CobblemonStructureIDs.SAVANNA_BERRY_SMALL, Set.of(berry));
        structureGroups.put(CobblemonStructureIDs.SAVANNA_BERRY_LARGE, Set.of(berry));
        structureGroups.put(CobblemonStructureIDs.PLAINS_BERRY_SMALL, Set.of(berry));
        structureGroups.put(CobblemonStructureIDs.PLAINS_BERRY_LARGE, Set.of(berry));
        structureGroups.put(CobblemonStructureIDs.SNOWY_BERRY_SMALL, Set.of(berry));
        structureGroups.put(CobblemonStructureIDs.SNOWY_BERRY_LARGE, Set.of(berry));
        structureGroups.put(CobblemonStructureIDs.DESERT_BERRY_SMALL, Set.of(berry));
        structureGroups.put(CobblemonStructureIDs.DESERT_BERRY_LARGE, Set.of(berry));
        structureGroups.put(CobblemonStructureIDs.TAIGA_BERRY_SMALL, Set.of(berry));
        structureGroups.put(CobblemonStructureIDs.TAIGA_BERRY_LARGE, Set.of(berry));

        //Mapped using location string as key
        Map<String, Integer> aMap = new HashMap<>();
        aMap.put(pokecenter, 1);
        aMap.put(berry, 2);

//        aMap.put(CobblemonStructureIDs.PLAINS_LONG_PATH, 3);
//        aMap.put(CobblemonStructureIDs.DESERT_LONG_PATH, 3);
//        aMap.put(CobblemonStructureIDs.SAVANNA_LONG_PATH, 3);
//        aMap.put(CobblemonStructureIDs.SNOWY_LONG_PATH, 3);
//        aMap.put(CobblemonStructureIDs.TAIGA_LONG_PATH, 3);
        structureMaxes = Collections.unmodifiableMap(aMap);
    }

    public Set<String> getGroups(ResourceLocation structureIdentifier) {
        return structureGroups.getOrDefault(structureIdentifier, Set.of());
    }

    public boolean hasReachedMaximum(ResourceLocation structureIdentifier) {
        Set<String> groups = getGroups(structureIdentifier);
        for (String group : groups) {
            int count = generatedStructureGroupCounts.getOrDefault(group, 0);
            if (count >= structureMaxes.getOrDefault(group, Integer.MAX_VALUE)) {
                return true;
            }
        }

        return false;
    }

    public void incrementStructureCount(ResourceLocation structureIdentifier) {
        Set<String> groups = getGroups(structureIdentifier);
        for (String group : groups) {
            generatedStructureGroupCounts.put(group, generatedStructureGroupCounts.getOrDefault(group, 0) + 1);
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onStructurePoolGeneratorCreation(Registry<StructureTemplatePool> registry, int maxSize, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, List<? super PoolElementStructurePiece> children, RandomSource random, CallbackInfo ci) {
        generatedStructureGroupCounts = new HashMap<>();
    }

    @ModifyVariable(method = "tryPlacingChildren", at = @At("STORE"), ordinal = 1)
    private Iterator<StructurePoolElement> reduceStructurePoolElementIterator(Iterator<StructurePoolElement> iterator) {
        List<StructurePoolElement> reducedList = new ArrayList<>();

        while (iterator.hasNext()) {
            StructurePoolElement structure = iterator.next();
            ResourceLocation structurePieceLocationKey = getCobblemonOnlyLocation(structure);
            if (structurePieceLocationKey == null) {
                reducedList.add(structure);
                continue;
            }

            if (!hasReachedMaximum(structurePieceLocationKey)) {
                reducedList.add(structure);
            }
        }

        return reducedList.iterator();
    }

    @Inject(method = "tryPlacingChildren", at = @At(value = "NEW", target = "net/minecraft/world/level/levelgen/structure/PoolElementStructurePiece", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void afterTryPlacingChildren(
        PoolElementStructurePiece poolElementStructurePiece,
        MutableObject<VoxelShape> mutableObject,
        int i,
        boolean bl,
        LevelHeightAccessor levelHeightAccessor,
        RandomState randomState,
        PoolAliasLookup poolAliasLookup,
        LiquidSettings liquidSettings,
        CallbackInfo ci,
        @Local(ordinal = 1) StructurePoolElement structurePoolElement
    ) {
        ResourceLocation structureLocationKey = getCobblemonOnlyLocation(structurePoolElement);
        if (structureLocationKey != null) {
            incrementStructureCount(structureLocationKey);
        }
    }

    private static ResourceLocation getCobblemonOnlyLocation(StructurePoolElement structurePoolElement) {
        ResourceLocation location = getLocationIfAvailable(structurePoolElement);
        if (location == null) return null;

        if (!location.getNamespace().equals("cobblemon")) return null;

        return location;
    }

    private static ResourceLocation getLocationIfAvailable(StructurePoolElement structurePoolElement) {
        if (structurePoolElement instanceof LegacySinglePoolElement legacySinglePoolElement) {
            if (legacySinglePoolElement.template.left().isEmpty()) return null;

            return legacySinglePoolElement.template.left().get();
        } else if (structurePoolElement instanceof SinglePoolElement singlePoolElement) {
            if (singlePoolElement.template.left().isEmpty()) return null;

            return singlePoolElement.template.left().get();
        } else {
            return null;
        }
    }
}