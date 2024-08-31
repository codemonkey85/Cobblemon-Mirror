/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.keybind.keybinds

import com.cobblemon.mod.common.CobblemonNetwork.sendToServer
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.battle.BattleGUI
import com.cobblemon.mod.common.client.keybind.CobblemonBlockingKeyBinding
import com.cobblemon.mod.common.client.keybind.KeybindCategories
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.BattleChallengePacket
import com.cobblemon.mod.common.net.messages.server.RequestPlayerInteractionsPacket
import com.cobblemon.mod.common.net.messages.server.SendOutPokemonPacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.traceFirstEntityCollision
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player

object PartySendBinding : CobblemonBlockingKeyBinding(
    "key.cobblemon.throwpartypokemon",
    InputConstants.Type.KEYSYM,
    InputConstants.KEY_R,
    KeybindCategories.COBBLEMON_CATEGORY
) {
    var canApplyChange = true
    var secondsSinceActioned = 0F

    fun actioned() {
        canApplyChange = false
        secondsSinceActioned = 0F
        wasDown = false
    }

    fun canAction() = canApplyChange

    override fun onTick() {
        if (secondsSinceActioned < 100) {
            secondsSinceActioned += Minecraft.getInstance().timer.getGameTimeDeltaPartialTick(false)
        }

        super.onTick()
    }

    override fun onRelease() {
        wasDown = false

        if (!canAction()) {
            canApplyChange = true
            return
        }

        canApplyChange = true
        val player = Minecraft.getInstance().player ?: return

        if (player.isSpectator) return

        val battle = CobblemonClient.battle
        if (battle != null) {
            battle.minimised = !battle.minimised
            if (!battle.minimised) {
                Minecraft.getInstance().setScreen(BattleGUI())
            }
            return
        }

        if (CobblemonClient.storage.selectedSlot != -1 && Minecraft.getInstance().screen == null) {
            val pokemon = CobblemonClient.storage.myParty.get(CobblemonClient.storage.selectedSlot)
            if (pokemon != null && pokemon.currentHealth > 0) {
                val targetEntity = player.traceFirstEntityCollision(entityClass = LivingEntity::class.java, ignoreEntity = player)
                if (targetEntity == null || (targetEntity is PokemonEntity && targetEntity.ownerUUID == player.uuid)) {
                    sendToServer(SendOutPokemonPacket(CobblemonClient.storage.selectedSlot))
                }
                else {
                    processEntityTarget(player, pokemon, targetEntity)
                }
            }
        }
    }

    private fun processEntityTarget(player: LocalPlayer, pokemon: Pokemon, entity: LivingEntity) {
        when (entity) {
            is Player -> {
                //This sends a packet to the server with the id of the player
                //The server sends a packet back that opens the player interaction menu with the proper options
                sendToServer(RequestPlayerInteractionsPacket(entity.uuid, entity.id, pokemon.uuid))
            }
            is PokemonEntity -> {
                if (!entity.canBattle(player)) return
                sendToServer(BattleChallengePacket(entity.id, pokemon.uuid))
            }
        }
    }

    override fun onPress() {
    }
}