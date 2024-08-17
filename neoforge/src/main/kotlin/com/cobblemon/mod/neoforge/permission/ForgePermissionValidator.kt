/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.neoforge.permission

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.permission.Permission
import com.cobblemon.mod.common.api.permission.PermissionValidator
import net.minecraft.commands.CommandSourceStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.server.permission.PermissionAPI
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent
import net.neoforged.neoforge.server.permission.nodes.PermissionNode
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes

object ForgePermissionValidator : PermissionValidator {

    private val nodes = hashMapOf<ResourceLocation, PermissionNode<Boolean>>()

    init {
        NeoForge.EVENT_BUS.addListener<PermissionGatherEvent.Nodes> { event ->
            Cobblemon.LOGGER.info("Starting Forge permission node registry")
            event.addNodes(this.createNodes())
            Cobblemon.LOGGER.debug("Finished Forge permission node registry")
        }
    }

    override fun initialize() {
        Cobblemon.LOGGER.info("Booting ForgePermissionApiPermissionValidator, player permissions will be checked using MinecraftForge' PermissionAPI, non player command sources will use Minecraft' permission level system, see https://docs.minecraftforge.net/en/latest/ and https://minecraft.fandom.com/wiki/Permission_level")
    }

    override fun hasPermission(player: ServerPlayer, permission: Permission): Boolean {
        val node = this.findNode(permission) ?: return player.hasPermissions(permission.level.numericalValue)
        return PermissionAPI.getPermission(player, node)
    }

    override fun hasPermission(source: CommandSourceStack, permission: Permission): Boolean {
        val player = this.extractPlayerFromSource(source) ?: return source.hasPermission(permission.level.numericalValue)
        val node = this.findNode(permission) ?: return source.hasPermission(permission.level.numericalValue)
        return PermissionAPI.getPermission(player, node)
    }

    private fun createNodes() = CobblemonPermissions.all().map { permission ->
        // 3rd arg is default value if no implementation is present essentially
        val node = PermissionNode(permission.identifier, PermissionTypes.BOOLEAN, { player, _, _ -> player?.hasPermissions(permission.level.numericalValue) == true })
        this.nodes[permission.identifier] = node
        Cobblemon.LOGGER.debug("Registered Forge permission node ${node.nodeName}")
        node
    }

    private fun findNode(permission: Permission) = this.nodes[permission.identifier]

    private fun extractPlayerFromSource(source: CommandSourceStack) = source.player

}