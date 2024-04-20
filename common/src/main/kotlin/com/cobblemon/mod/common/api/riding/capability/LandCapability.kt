/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.capability

import com.cobblemon.mod.common.api.riding.controller.properties.RideControllerProperties
import com.cobblemon.mod.common.api.tags.CobblemonBlockTags
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.blockPositionsAsList
import com.cobblemon.mod.common.util.blockPositionsAsListRounded
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.FluidTags
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3i
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import java.util.function.Predicate

class LandCapability(override val properties: RideControllerProperties) : RidingCapability {

    override val key: Identifier = RidingCapability.LAND
    override val condition: Predicate<PokemonEntity> = Predicate<PokemonEntity> { entity ->
        //Are there any blocks under the mon that aren't air or fluid
        //Cant just check one block since some mons may be more than one block big
        //This should be changed so that the any predicate is only ran on blocks under the mon
        return@Predicate VoxelShapes.cuboid(entity.boundingBox).blockPositionsAsListRounded().any {
            //Need to check other fluids
            if (entity.isTouchingWater || entity.isSubmergedInWater) {
                return@any false
            }
            //This might not actually work, depending on what the yPos actually is. yPos of the middle of the entity? the feet?
            if (it.y.toDouble() == (entity.pos.y)) {
                val blockState = entity.world.getBlockState(it.down())
                return@any !blockState.isAir && blockState.fluidState.isEmpty
            }
            false
        }
    }

}