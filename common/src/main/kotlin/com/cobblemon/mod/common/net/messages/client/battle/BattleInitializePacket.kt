/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.battles.BattleFormat
import com.cobblemon.mod.common.battles.BattleSide
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.readMapK
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeMapK
import com.cobblemon.mod.common.util.writeSizedInt
import com.cobblemon.mod.common.util.writeString
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.MutableComponent

/**
 * Initializes the client's understanding of a battle. This can be for a participant or for a spectator.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.BattleInitializeHandler].
 *
 * @author Hiroku
 * @since May 10th, 2022
 */
class BattleInitializePacket() : NetworkPacket<BattleInitializePacket> {

    override val id = ID

    lateinit var battleId: UUID
    lateinit var battleFormat: BattleFormat

    lateinit var side1: BattleSideDTO
    lateinit var side2: BattleSideDTO

    /**
     * @param battle The battle to initialize on the client
     * @param allySide The [BattleSide] the client is on, null if the client is a spectator
     */
    constructor(battle: PokemonBattle, allySide: BattleSide?): this() {
        battleId = battle.battleId
        battleFormat = battle.format
        val sides = arrayOf(battle.side1, battle.side2).map { side ->
            BattleSideDTO(
                actors = side.actors.map { actor ->
                    BattleActorDTO(
                        uuid = actor.uuid,
                        showdownId = actor.showdownId,
                        displayName = actor.getName(),
                        activePokemon = actor.activePokemon.map { it.battlePokemon?.let {
                            pkm -> ActiveBattlePokemonDTO.fromPokemon(pkm, allySide == side, illusion = it.illusion)
                        } },
                        type = actor.type
                    )
                }
            )
        }
        side1 = sides[0]
        side2 = sides[1]
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(battleId)
        battleFormat.saveToBuffer(buffer)
        for (side in arrayOf(side1, side2)) {
            buffer.writeSizedInt(IntSize.U_BYTE, side.actors.size)
            for (actor in side.actors) {
                buffer.writeUUID(actor.uuid)
                ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buffer, actor.displayName)
                buffer.writeString(actor.showdownId)
                buffer.writeSizedInt(IntSize.U_BYTE, actor.activePokemon.size)
                for (activePokemon in actor.activePokemon) {
                    buffer.writeBoolean(activePokemon != null)
                    activePokemon?.saveToBuffer(buffer)
                }
                buffer.writeSizedInt(IntSize.U_BYTE, actor.type.ordinal)
            }
        }
    }

    private fun decode(buffer: RegistryFriendlyByteBuf) {
        battleId = buffer.readUUID()
        battleFormat = BattleFormat.loadFromBuffer(buffer)
        val sides = mutableListOf<BattleSideDTO>()
        repeat(times = 2) {
            val actors = mutableListOf<BattleActorDTO>()
            repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
                val uuid = buffer.readUUID()
                val displayName = ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buffer) as MutableComponent
                val showdownId = buffer.readString()
                val activePokemon = mutableListOf<ActiveBattlePokemonDTO?>()
                repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
                    if (buffer.readBoolean()) {
                        activePokemon.add(ActiveBattlePokemonDTO.loadFromBuffer(buffer))
                    } else {
                        activePokemon.add(null)
                    }
                }
                val type = ActorType.values()[buffer.readSizedInt(IntSize.U_BYTE)]
                actors.add(
                    BattleActorDTO(
                        uuid = uuid,
                        displayName = displayName,
                        showdownId = showdownId,
                        activePokemon = activePokemon,
                        type = type
                    )
                )
            }
            sides.add(BattleSideDTO(actors))
        }
        side1 = sides[0]
        side2 = sides[1]
    }

    companion object {
        val ID = cobblemonResource("battle_initialize")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleInitializePacket().apply { decode(buffer) }
    }

    data class BattleSideDTO(val actors: List<BattleActorDTO>)

    data class BattleActorDTO(
        val uuid: UUID,
        val displayName: MutableComponent,
        val showdownId: String,
        val activePokemon: List<ActiveBattlePokemonDTO?>,
        val type: ActorType,
    )

    data class ActiveBattlePokemonDTO(
        val uuid: UUID,
        val displayName: MutableComponent,
        val properties: PokemonProperties,
        val aspects: Set<String>,
        val status: PersistentStatus?,
        val hpValue: Float,
        val maxHp: Float,
        val isFlatHp: Boolean,
        val statChanges: MutableMap<Stat, Int>
    ) {
        companion object {
            fun fromPokemon(battlePokemon: BattlePokemon, isAlly: Boolean, illusion: BattlePokemon? = null): ActiveBattlePokemonDTO {
                val pokemon = battlePokemon.effectedPokemon
                val exposed = if (isAlly) pokemon else illusion?.effectedPokemon ?: pokemon
                val hpValue = if (isAlly) pokemon.currentHealth.toFloat() else pokemon.currentHealth.toFloat() / pokemon.maxHealth
                return ActiveBattlePokemonDTO(
                    uuid = exposed.uuid,
                    displayName = exposed.getDisplayName(),
                    properties = exposed.createPokemonProperties(
                        PokemonPropertyExtractor.SPECIES,
                        PokemonPropertyExtractor.GENDER,
                        PokemonPropertyExtractor.FORM,
                        PokemonPropertyExtractor.SHINY
                    ).apply { level = pokemon.level },
                    aspects = exposed.aspects,
                    status = pokemon.status?.status,
                    hpValue = hpValue,
                    maxHp = pokemon.maxHealth.toFloat(),
                    isFlatHp = isAlly,
                    statChanges = battlePokemon.statChanges
                )
            }

            fun fromMock(battlePokemon: BattlePokemon, isAlly: Boolean, mock: PokemonProperties): ActiveBattlePokemonDTO {
                val pokemon = battlePokemon.effectedPokemon
                val hpValue = if (isAlly) pokemon.currentHealth.toFloat() else pokemon.currentHealth.toFloat() / pokemon.maxHealth
                return ActiveBattlePokemonDTO(
                    uuid = battlePokemon.uuid,
                    displayName = pokemon.getDisplayName(),
                    properties = mock.apply { level = pokemon.level },
                    aspects = mock.aspects,
                    status = pokemon.status?.status,
                    hpValue = hpValue,
                    maxHp = pokemon.maxHealth.toFloat(),
                    isFlatHp = isAlly,
                    statChanges = battlePokemon.statChanges
                )
            }

            fun loadFromBuffer(buffer: RegistryFriendlyByteBuf): ActiveBattlePokemonDTO {
                val uuid = buffer.readUUID()
                val pokemonDisplayName = ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buffer).copy()
                val properties = PokemonProperties.parse(buffer.readString(), delimiter = " ")
                val aspects = buffer.readList { buffer.readString() }.toSet()
                val status = if (buffer.readBoolean()) {
                    Statuses.getStatus(buffer.readIdentifier()) as? PersistentStatus
                } else {
                    null
                }
                val hpRatio = buffer.readFloat()
                val maxHp = buffer.readFloat()
                val isFlatHp = buffer.readBoolean()
                val statChanges = mutableMapOf<Stat, Int>()
                buffer.readMapK(size = IntSize.U_BYTE, statChanges) {
                    val stat = Cobblemon.statProvider.decode(buffer)
                    val stages = buffer.readSizedInt(IntSize.BYTE)
                    stat to stages
                }
                return ActiveBattlePokemonDTO(
                    uuid = uuid,
                    displayName = pokemonDisplayName,
                    properties = properties,
                    aspects = aspects,
                    status = status,
                    hpValue = hpRatio,
                    maxHp = maxHp,
                    isFlatHp = isFlatHp,
                    statChanges = statChanges
                )
            }
        }

        fun saveToBuffer(buffer: RegistryFriendlyByteBuf): ActiveBattlePokemonDTO {
            buffer.writeUUID(uuid)
            ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buffer, displayName)
            buffer.writeString(properties.asString())
            buffer.writeCollection(aspects) { buf, it -> buf.writeString(it) }
            buffer.writeBoolean(status != null)
            status?.let { buffer.writeString(it.name.toString()) }
            buffer.writeFloat(hpValue)
            buffer.writeFloat(maxHp)
            buffer.writeBoolean(isFlatHp)
            buffer.writeMapK(IntSize.U_BYTE, statChanges) { (stat, stages) ->
                Cobblemon.statProvider.encode(buffer, stat)
                buffer.writeSizedInt(IntSize.BYTE, stages)
            }
            return this
        }
    }
}