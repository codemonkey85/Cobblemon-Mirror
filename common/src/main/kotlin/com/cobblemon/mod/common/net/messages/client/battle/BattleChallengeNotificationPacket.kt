/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.battles.BattleFormat
import com.cobblemon.mod.common.battles.ChallengeManager
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.*

/**
 * Packet send when a player has challenged to battle. The responsibility
 * of this packet currently is to send a battle challenge message that includes
 * the keybind to challenge them back.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattleChallengeNotificationHandler].
 *
 * @param challengeID The unique identifier of the challenge.
 * @param senderID The unique identifier of the party that sent the challenge.
 * @param challengerIDs In the case of a multi battle, the unique identifiers of all the players on the challenging team.
 * Otherwise, the unique identifier of the challenging player (same as [senderID]).
 * @param battleFormat The showdown format of the challenge.
 * @param expiryTime How long (in seconds) the challenge is active.
 *
 * @author Hiroku
 * @since August 5th, 2022
 */
class BattleChallengeNotificationPacket(
    val challengeID: UUID,
    val senderID: UUID,
    val challengerIDs: List<UUID>,
    val battleFormat: BattleFormat,
    val expiryTime: Int
): NetworkPacket<BattleChallengeNotificationPacket> {
    override val id = ID

    constructor(battleChallengeId: UUID, challengerId: UUID, battleFormat: BattleFormat, expiryTime: Int) :
        this(battleChallengeId, challengerId, listOf(challengerId), battleFormat, expiryTime)

    constructor(challenge: ChallengeManager.BattleChallenge) : this(
        challengeID = challenge.requestID,
        senderID = challenge.senderID,
        challengerIDs =
            if (challenge is ChallengeManager.MultiBattleChallenge)
                challenge.senderTeam.teamPlayersUUID
            else
                listOf(challenge.senderID),
        battleFormat = challenge.battleFormat,
        expiryTime = challenge.expiryTime
    )

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(challengeID)
        buffer.writeUUID(senderID)
        buffer.writeCollection(challengerIDs) { _, value -> buffer.writeUUID(value) }
        battleFormat.saveToBuffer(buffer)
        buffer.writeInt(expiryTime)
    }

    companion object {
        val ID = cobblemonResource("battle_challenge_notification")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleChallengeNotificationPacket(
            buffer.readUUID(),
            buffer.readUUID(),
            buffer.readList { it.readUUID() },
            BattleFormat.loadFromBuffer(buffer),
            buffer.readInt()
        )
    }
}