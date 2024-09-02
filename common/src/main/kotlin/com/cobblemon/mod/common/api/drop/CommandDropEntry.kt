/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.drop

import com.cobblemon.mod.common.util.substitute
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3

/**
 * A drop entry which 'drops' by running a command. The command supports the following placeholders:
 * {{player}} - the player name.
 * {{world}} - the identifier for the world it's being 'dropped', such as minecraft:the_overworld
 * {{x}} - The decimal x coordinate of where it's being 'dropped'.
 * {{y}} - The decimal y coordinate of where it's being 'dropped'.
 * {{z}} - The decimal z coordinate of where it's being 'dropped'.
 *
 * If [requiresPlayer] is true, then it will not be able to drop without a player being the cause of the drop.
 *
 * @author Hiroku
 * @since July 24th, 2022
 */
class CommandDropEntry : DropEntry {
    val requiresPlayer = true
    val command = ""
    override val percentage = 100F
    override val quantity = 1
    override val maxSelectableTimes = 1

    override fun drop(entity: LivingEntity?, world: ServerLevel, pos: Vec3, player: ServerPlayer?) {
        if (requiresPlayer && player == null) {
            return
        }

        world.server.commands.performPrefixedCommand(
            world.server.createCommandSourceStack(),
            command.substitute("player", player?.name?.string ?: "")
                .substitute("world", world.dimension().location())
                .substitute("x", pos.x)
                .substitute("y", pos.y)
                .substitute("z", pos.z)
        )
    }
}