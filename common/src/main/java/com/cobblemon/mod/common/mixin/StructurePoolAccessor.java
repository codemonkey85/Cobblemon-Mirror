/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

@Mixin(StructureTemplatePool.class)
public interface StructurePoolAccessor {
    @Accessor("rawTemplates")
    List<Pair<StructurePoolElement, Integer>> getElementCounts();

    @Mutable
    @Accessor("rawTemplates")
    void setElementCounts(List<Pair<StructurePoolElement, Integer>> elementCounts);

    @Accessor("templates")
    ObjectArrayList<StructurePoolElement> getElements();
}