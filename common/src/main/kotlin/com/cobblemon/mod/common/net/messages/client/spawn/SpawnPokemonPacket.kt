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
import com.cobblemon.mod.common.entity.PlatformType
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Gender
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
    private val speciesId: ResourceLocation,
    private val gender: Gender,
    private val shiny: Boolean,
    private val formName: String,
    private val aspects: Set<String>,
    private val battleId: UUID?,
    private val phasingTargetId: Int,
    private val beamMode: Byte,
    private val platform: PlatformType,
    private val nickname: MutableComponent?,
    private val labelLevel: Int,
    private val poseType: PoseType,
    private val unbattlable: Boolean,
    private val hideLabel: Boolean,
    private val caughtBall: ResourceLocation,
    private val spawnYaw: Float,
    private val friendship: Int,
    private val freezeFrame: Float,
    vanillaSpawnPacket: ClientboundAddEntityPacket
) : SpawnExtraDataEntityPacket<SpawnPokemonPacket, PokemonEntity>(vanillaSpawnPacket) {

    override val id: ResourceLocation = ID

    constructor(entity: PokemonEntity, vanillaSpawnPacket: ClientboundAddEntityPacket) : this(
        entity.ownerUUID,
        entity.pokemon.scaleModifier,
        entity.exposedSpecies.resourceIdentifier,
        entity.pokemon.gender,
        entity.pokemon.shiny,
        entity.pokemon.form.formOnlyShowdownId(),
        entity.pokemon.aspects,
        entity.battleId,
        entity.phasingTargetId,
        entity.beamMode.toByte(),
        entity.platform,
        entity.pokemon.nickname,
        if (Cobblemon.config.displayEntityLevelLabel) entity.entityData.get(PokemonEntity.LABEL_LEVEL) else -1,
        entity.entityData.get(PokemonEntity.POSE_TYPE),
        entity.entityData.get(PokemonEntity.UNBATTLEABLE),
        entity.entityData.get(PokemonEntity.HIDE_LABEL),
        entity.pokemon.caughtBall.name,
        entity.entityData.get(PokemonEntity.SPAWN_DIRECTION),
        entity.entityData.get(PokemonEntity.FRIENDSHIP),
        entity.entityData.get(PokemonEntity.FREEZE_FRAME),
        vanillaSpawnPacket
    )

    override fun encodeEntityData(buffer: RegistryFriendlyByteBuf) {
        buffer.writeNullable(ownerId) { _, v -> buffer.writeUUID(v) }
        buffer.writeFloat(this.scaleModifier)
        buffer.writeIdentifier(this.speciesId)
        buffer.writeEnumConstant(this.gender)
        buffer.writeBoolean(this.shiny)
        buffer.writeString(this.formName)
        buffer.writeCollection(this.aspects) { pb, value -> pb.writeString(value) }
        buffer.writeNullable(this.battleId) { pb, value -> pb.writeUUID(value) }
        buffer.writeInt(this.phasingTargetId)
        buffer.writeByte(this.beamMode.toInt())
        buffer.writeEnumConstant(this.platform)
        buffer.writeNullable(this.nickname) { _, v -> buffer.writeText(v) }
        buffer.writeInt(this.labelLevel)
        buffer.writeEnumConstant(this.poseType)
        buffer.writeBoolean(this.unbattlable)
        buffer.writeBoolean(this.hideLabel)
        buffer.writeIdentifier(this.caughtBall)
        buffer.writeFloat(this.spawnYaw)
        buffer.writeInt(this.friendship)
        buffer.writeFloat(this.freezeFrame)
    }

    override fun applyData(entity: PokemonEntity) {
        entity.ownerUUID = ownerId
        entity.pokemon.apply {
            scaleModifier = this@SpawnPokemonPacket.scaleModifier
            species = this@SpawnPokemonPacket.speciesId.let { PokemonSpecies.getByIdentifier(it) ?: PokemonSpecies.random() }
            gender = this@SpawnPokemonPacket.gender
            shiny = this@SpawnPokemonPacket.shiny
            form = this@SpawnPokemonPacket.formName.let { formName -> species.forms.find { it.formOnlyShowdownId() == formName }} ?: species.standardForm
            forcedAspects = this@SpawnPokemonPacket.aspects
            nickname = this@SpawnPokemonPacket.nickname
            PokeBalls.getPokeBall(this@SpawnPokemonPacket.caughtBall)?.let { caughtBall = it }
        }
        entity.phasingTargetId = this.phasingTargetId
        entity.beamMode = this.beamMode.toInt()
        entity.platform = this.platform
        entity.battleId = this.battleId
        entity.entityData.set(PokemonEntity.LABEL_LEVEL, labelLevel)
        entity.entityData.set(PokemonEntity.SPECIES, entity.pokemon.species.resourceIdentifier.toString())
        entity.entityData.set(PokemonEntity.ASPECTS, aspects)
        entity.entityData.set(PokemonEntity.POSE_TYPE, poseType)
        entity.entityData.set(PokemonEntity.UNBATTLEABLE, unbattlable)
        entity.entityData.set(PokemonEntity.HIDE_LABEL, hideLabel)
        entity.entityData.set(PokemonEntity.SPAWN_DIRECTION, spawnYaw)
        entity.entityData.set(PokemonEntity.FRIENDSHIP, friendship)
        entity.entityData.set(PokemonEntity.FREEZE_FRAME, freezeFrame)
    }

    override fun checkType(entity: Entity): Boolean = entity is PokemonEntity

    companion object {
        val ID = cobblemonResource("spawn_pokemon_entity")
        fun decode(buffer: RegistryFriendlyByteBuf): SpawnPokemonPacket {
            val ownerId = buffer.readNullable { buffer.readUUID() }
            val scaleModifier = buffer.readFloat()
            val speciesId = buffer.readIdentifier()
            val gender = buffer.readEnumConstant(Gender::class.java)
            val shiny = buffer.readBoolean()
            val formName = buffer.readString()
            val aspects = buffer.readList { it.readString() }.toSet()
            val battleId = buffer.readNullable { buffer.readUUID() }
            val phasingTargetId = buffer.readInt()
            val beamModeEmitter = buffer.readByte()
            val platform = buffer.readEnumConstant(PlatformType::class.java)
            val nickname = buffer.readNullable { buffer.readText().copy() }
            val labelLevel = buffer.readInt()
            val poseType = buffer.readEnumConstant(PoseType::class.java)
            val unbattlable = buffer.readBoolean()
            val hideLabel = buffer.readBoolean()
            val caughtBall = buffer.readIdentifier()
            val spawnAngle = buffer.readFloat()
            val friendship = buffer.readInt()
            val freezeFrame = buffer.readFloat()
            val vanillaPacket = decodeVanillaPacket(buffer)

            return SpawnPokemonPacket(ownerId, scaleModifier, speciesId, gender, shiny, formName, aspects, battleId, phasingTargetId, beamModeEmitter, platform, nickname, labelLevel, poseType, unbattlable, hideLabel, caughtBall, spawnAngle, friendship, freezeFrame, vanillaPacket)
        }
    }

}