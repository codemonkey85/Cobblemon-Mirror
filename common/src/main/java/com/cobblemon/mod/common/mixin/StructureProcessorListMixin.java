/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.bridges.StructureProcessorListBridge;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StructureProcessorList.class)
public class StructureProcessorListMixin implements StructureProcessorListBridge {

    @Final
    @Mutable
    @Shadow
    private List<StructureProcessor> list;

    @Override
    public void append(StructureProcessor processor) {
        List<StructureProcessor> mutable = new ArrayList<>(this.list);
        mutable.add(processor);
        this.list = ImmutableList.copyOf(mutable);
    }

    @Override
    public void clear() {
        this.list = ImmutableList.of();
    }


}
