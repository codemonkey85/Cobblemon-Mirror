/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.api.apricorn.Apricorn
import com.cobblemon.mod.common.block.grower.ApricornTreeGrower
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.level.block.SaplingBlock

class ApricornSaplingBlock(properties : Properties, val apricorn: Apricorn) : SaplingBlock(ApricornTreeGrower(apricorn), properties) {

    override fun codec(): MapCodec<out SaplingBlock> {
        return CODEC
    }

    companion object {
        val CODEC: MapCodec<ApricornSaplingBlock> = RecordCodecBuilder.mapCodec { it.group(
            propertiesCodec(),
            Apricorn.CODEC.fieldOf("apricorn").forGetter(ApricornSaplingBlock::apricorn)
        ).apply(it, ::ApricornSaplingBlock) }
    }

}