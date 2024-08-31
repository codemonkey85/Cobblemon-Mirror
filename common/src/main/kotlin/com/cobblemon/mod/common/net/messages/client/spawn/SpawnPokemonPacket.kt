/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.spawn

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.*
import java.util.UUID
import net.minecraft.world.entity.Entity
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.resources.ResourceLocation

class SpawnPokemonPacket(
    private val ownerId: UUID?,
    private val scaleModifier: Float,
    private val species: Species,
    private val form: FormData,
    private val aspects: Set<String>,
    private val battleId: UUID?,
    private val phasingTargetId: Int,
    private val beamMode: Byte,
    private val nickname: MutableComponent?,
    private val labelLevel: Int,
    private val poseType: PoseType,
    private val unbattlable: Boolean,
    private val hideLabel: Boolean,
    private val caughtBall: ResourceLocation,
    private val spawnYaw: Float,
    private val friendship: Int,
    vanillaSpawnPacket: ClientboundAddEntityPacket
) : SpawnExtraDataEntityPacket<SpawnPokemonPacket, PokemonEntity>(vanillaSpawnPacket) {

    override val id: ResourceLocation = ID

    constructor(entity: PokemonEntity, vanillaSpawnPacket: ClientboundAddEntityPacket) : this(
        entity.ownerUUID,
        entity.pokemon.scaleModifier,
        entity.exposedSpecies,
        entity.pokemon.form,
        entity.pokemon.aspects,
        entity.battleId,
        entity.phasingTargetId,
        entity.beamMode.toByte(),
        entity.pokemon.nickname,
        if (Cobblemon.config.displayEntityLevelLabel) entity.entityData.get(PokemonEntity.LABEL_LEVEL) else -1,
        entity.entityData.get(PokemonEntity.POSE_TYPE),
        entity.entityData.get(PokemonEntity.UNBATTLEABLE),
        entity.entityData.get(PokemonEntity.HIDE_LABEL),
        entity.pokemon.caughtBall.name,
        entity.entityData.get(PokemonEntity.SPAWN_DIRECTION),
        entity.entityData.get(PokemonEntity.FRIENDSHIP),
        vanillaSpawnPacket
    )

    override fun encodeEntityData(buffer: RegistryFriendlyByteBuf) {
        buffer.writeNullable(ownerId) { _, v -> buffer.writeUUID(v) }
        buffer.writeFloat(this.scaleModifier)
        buffer.writeIdentifier(this.species.resourceIdentifier)
        buffer.writeString(this.form.formOnlyShowdownId())
        buffer.writeCollection(this.aspects) { pb, value -> pb.writeString(value) }
        buffer.writeNullable(this.battleId) { pb, value -> pb.writeUUID(value) }
        buffer.writeInt(this.phasingTargetId)
        buffer.writeByte(this.beamMode.toInt())
        buffer.writeNullable(this.nickname) { _, v -> buffer.writeText(v) }
        buffer.writeInt(this.labelLevel)
        buffer.writeEnumConstant(this.poseType)
        buffer.writeBoolean(this.unbattlable)
        buffer.writeBoolean(this.hideLabel)
        buffer.writeIdentifier(this.caughtBall)
        buffer.writeFloat(this.spawnYaw)
        buffer.writeInt(this.friendship)
    }

    override fun applyData(entity: PokemonEntity) {
        entity.ownerUUID = ownerId
        entity.pokemon.apply {
            scaleModifier = this@SpawnPokemonPacket.scaleModifier
            species = this@SpawnPokemonPacket.species
            form = this@SpawnPokemonPacket.form
            forcedAspects = this@SpawnPokemonPacket.aspects
            nickname = this@SpawnPokemonPacket.nickname
            PokeBalls.getPokeBall(this@SpawnPokemonPacket.caughtBall)?.let { caughtBall = it }
        }
        entity.phasingTargetId = this.phasingTargetId
        entity.beamMode = this.beamMode.toInt()
        entity.battleId = this.battleId
        entity.entityData.set(PokemonEntity.LABEL_LEVEL, labelLevel)
        entity.entityData.set(PokemonEntity.SPECIES, entity.pokemon.species.resourceIdentifier.toString())
        entity.entityData.set(PokemonEntity.ASPECTS, aspects)
        entity.entityData.set(PokemonEntity.POSE_TYPE, poseType)
        entity.entityData.set(PokemonEntity.UNBATTLEABLE, unbattlable)
        entity.entityData.set(PokemonEntity.HIDE_LABEL, hideLabel)
        entity.entityData.set(PokemonEntity.SPAWN_DIRECTION, spawnYaw)
        entity.entityData.set(PokemonEntity.FRIENDSHIP, friendship)
    }

    override fun checkType(entity: Entity): Boolean = entity is PokemonEntity

    companion object {
        val ID = cobblemonResource("spawn_pokemon_entity")
        fun decode(buffer: RegistryFriendlyByteBuf): SpawnPokemonPacket {
            val ownerId = buffer.readNullable { buffer.readUUID() }
            val scaleModifier = buffer.readFloat()
            val identifier = buffer.readIdentifier()
            val species = requireNotNull(PokemonSpecies.getByIdentifier(identifier)) { "received unknown PokemonSpecies: $identifier" }
            val showdownId = buffer.readString()
            val form = species.forms.firstOrNull { it.formOnlyShowdownId() == showdownId } ?: species.standardForm
            val aspects = buffer.readList { it.readString() }.toSet()
            val battleId = buffer.readNullable { buffer.readUUID() }
            val phasingTargetId = buffer.readInt()
            val beamModeEmitter = buffer.readByte()
            val nickname = buffer.readNullable { buffer.readText().copy() }
            val labelLevel = buffer.readInt()
            val poseType = buffer.readEnumConstant(PoseType::class.java)
            val unbattlable = buffer.readBoolean()
            val hideLabel = buffer.readBoolean()
            val caughtBall = buffer.readIdentifier()
            val spawnAngle = buffer.readFloat()
            val friendship = buffer.readInt()
            val vanillaPacket = decodeVanillaPacket(buffer)

            return SpawnPokemonPacket(ownerId, scaleModifier, species, form, aspects, battleId, phasingTargetId, beamModeEmitter, nickname, labelLevel, poseType, unbattlable, hideLabel, caughtBall, spawnAngle, friendship, vanillaPacket)
        }
    }

}