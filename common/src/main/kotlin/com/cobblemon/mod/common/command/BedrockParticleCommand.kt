/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormEntityParticlePacket
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormParticlePacket
import com.cobblemon.mod.common.util.alias
import com.cobblemon.mod.common.util.distanceTo
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.toBlockPos
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.entity.Entity
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.DimensionArgument
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.server.level.ServerLevel
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3

object BedrockParticleCommand {
    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        val command = dispatcher.register(Commands.literal("bedrockparticle")
            .permission(CobblemonPermissions.BEDROCK_PARTICLE)
            .then(
                Commands.argument("effect", ResourceLocationArgument.id())
                    .then(
                        Commands.argument("target", EntityArgument.entities())
                            .executes {
                                val effectId = ResourceLocationArgument.getId(it, "effect")
                                val entities = EntityArgument.getEntities(it, "target")
                                return@executes entities.sumOf { entity -> execute(it.source, effectId, entity.level() as ServerLevel, entity.position()) }
                            }
                            .then(
                                Commands.argument("locator", StringArgumentType.word())
                                    .executes {
                                        val effectId = ResourceLocationArgument.getId(it, "effect")
                                        val entities = EntityArgument.getEntities(it, "target")
                                        val locator = StringArgumentType.getString(it, "locator")

                                        return@executes entities.sumOf { entity -> execute(it.source, effectId, entity.level() as ServerLevel, entity, listOf(locator)) }
                                    }
                            )
                    )
                    .then(
                        Commands.argument("world", DimensionArgument.dimension())
                            .then(
                                Commands.argument("pos", Vec3Argument.vec3())
                                    .executes {
                                        val effectId = ResourceLocationArgument.getId(it, "effect")
                                        val world = DimensionArgument.getDimension(it, "world")
                                        val pos = Vec3Argument.getVec3(it, "pos")
                                        return@executes execute(it.source, effectId, world as ServerLevel, pos)
                                    }
                            )
                    )
                )
            )
        dispatcher.register(command.alias("bedrockparticle"))
    }

    private fun execute(source: CommandSourceStack, effectId: ResourceLocation, world: ServerLevel, target: Vec3): Int {
        val pos = target.toBlockPos()
        val nearbyPlayers = world.getPlayers { it.distanceTo(pos) < 1000 }
        nearbyPlayers.forEach { player -> player.sendPacket(SpawnSnowstormParticlePacket(effectId, target)) }
        return Command.SINGLE_SUCCESS
    }

    private fun execute(source: CommandSourceStack, effectId: ResourceLocation, world: ServerLevel, target: Entity, locator: List<String>): Int {
        val pos = target.blockPosition()
        val nearbyPlayers = world.getPlayers { it.distanceTo(pos) < 1000 }
        nearbyPlayers.forEach { player -> player.sendPacket(SpawnSnowstormEntityParticlePacket(effectId, target.id, locator)) }
        return Command.SINGLE_SUCCESS
    }

}