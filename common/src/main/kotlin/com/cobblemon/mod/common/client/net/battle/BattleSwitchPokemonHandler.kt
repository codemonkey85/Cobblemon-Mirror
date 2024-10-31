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
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon
import com.cobblemon.mod.common.client.battle.animations.MoveTileOffscreenAnimation
import com.cobblemon.mod.common.client.battle.animations.MoveTileOnscreenAnimation
import com.cobblemon.mod.common.net.messages.client.battle.BattleSwitchPokemonPacket
import net.minecraft.client.Minecraft

object BattleSwitchPokemonHandler : ClientNetworkPacketHandler<BattleSwitchPokemonPacket> {
    override fun handle(packet: BattleSwitchPokemonPacket, client: Minecraft) {
        val battle = CobblemonClient.battle ?: return
        val (actor, activeBattlePokemon) = battle.getPokemonFromPNX(packet.pnx)

        val lastAnimation = activeBattlePokemon.animations.lastOrNull()
        if (activeBattlePokemon.battlePokemon != null && lastAnimation !is MoveTileOffscreenAnimation) {
            activeBattlePokemon.animations.add(MoveTileOffscreenAnimation())
        }

        val newPokemon = with(packet.newPokemon) {
            ClientBattlePokemon(
                uuid = uuid,
                displayName = displayName,
                properties = properties,
                aspects = aspects,
                hpValue = hpValue,
                maxHp = maxHp,
                isHpFlat = packet.isAlly,
                status = status,
                statChanges = statChanges
            ).also {
                it.actor = actor
            }
        }

        // battlePokemon is null when sending out a pokemon (from an InactivePokemonState) on turn 0
        if (activeBattlePokemon.battlePokemon == null) {
            // battlePokemon needs to be present before the respective tile can actually be rendered
            activeBattlePokemon.battlePokemon = newPokemon
            activeBattlePokemon.animations.add(
                MoveTileOnscreenAnimation(null)
            )
        }
        else {
            activeBattlePokemon.animations.add(
                MoveTileOnscreenAnimation(newPokemon)
            )
        }

        // Only update currently selected Pokémon if it's our Pokémon being switched in
        if (actor == battle.getParticipatingActor(client.user.profileId)) {
            CobblemonClient.storage.switchToPokemon(packet.newPokemon.uuid)
        }
    }
}