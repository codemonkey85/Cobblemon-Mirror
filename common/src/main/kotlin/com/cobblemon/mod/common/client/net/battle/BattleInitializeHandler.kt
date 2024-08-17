/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon
import com.cobblemon.mod.common.client.battle.ClientBattle
import com.cobblemon.mod.common.client.battle.ClientBattleActor
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon
import com.cobblemon.mod.common.client.gui.battle.BattleGUI
import com.cobblemon.mod.common.net.messages.client.battle.BattleInitializePacket
import net.minecraft.client.Minecraft

object BattleInitializeHandler : ClientNetworkPacketHandler<BattleInitializePacket> {
    override fun handle(packet: BattleInitializePacket, client: Minecraft) {
        val playerUUID = Minecraft.getInstance().player?.uuid
        CobblemonClient.battle = ClientBattle(
            packet.battleId,
            packet.battleFormat
        ).apply {
            val mySide = if (packet.side2.actors.none { it.uuid == playerUUID }) {
                packet.side1
            } else {
                packet.side2
            }

            val otherSide = if (mySide == packet.side1) packet.side2 else packet.side1
            val sides = listOf(packet.side1, packet.side2)
            spectating = !sides.any { it.actors.any { it.uuid == Minecraft.getInstance().player?.uuid } }
            side1.actors.addAll(mySide.actors.map{ actorFromDTO(it, !spectating) })
            side2.actors.addAll(otherSide.actors.map{ actorFromDTO(it, false) })
            for (side in listOf(side1, side2)) {
                side.battle = this
                for (actor in side.actors) {
                    actor.side = side
                    for (pokemon in actor.activePokemon) {
                        pokemon.battlePokemon?.actor = actor
                    }
                }
            }
            minimised = false
            this.wildActor = this.findWildActor()
        }

        Minecraft.getInstance().setScreen(BattleGUI())
    }

    fun actorFromDTO(actorDTO: BattleInitializePacket.BattleActorDTO, isAlly: Boolean): ClientBattleActor {
        return ClientBattleActor(
            showdownId = actorDTO.showdownId,
            displayName = actorDTO.displayName,
            uuid = actorDTO.uuid,
            type = actorDTO.type
        ).apply {
            activePokemon.addAll(actorDTO.activePokemon.map {
                ActiveClientBattlePokemon(this, it?.let {
                    ClientBattlePokemon(
                        uuid = it.uuid,
                        properties = it.properties,
                        aspects = it.aspects,
                        displayName = it.displayName,
                        hpValue = it.hpValue,
                        maxHp = it.maxHp,
                        isHpFlat = isAlly,
                        status = it.status,
                        statChanges = it.statChanges,
                    )
                })
            })
        }
    }
}