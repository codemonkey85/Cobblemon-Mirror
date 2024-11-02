package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.battle.BattleAppendTargetedEntityPacket
import net.minecraft.client.Minecraft

object BattleSetTargetedEntityHandler : ClientNetworkPacketHandler<BattleAppendTargetedEntityPacket> {
    override fun handle(packet: BattleAppendTargetedEntityPacket, client: Minecraft) {
        val sourceEntity = Minecraft.getInstance().level?.getEntity(packet.sourceEntityId)
        val sourcePokemon = sourceEntity as? PokemonEntity
        if (sourcePokemon != null) {
            (sourcePokemon.delegate as PokemonClientDelegate).targetedEntityId = packet.targetedEntityId
        }
    }

}