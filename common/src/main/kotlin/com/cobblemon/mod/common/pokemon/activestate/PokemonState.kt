/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.activestate

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.isPokemonEntity
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.playSoundServer
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.UUIDUtil
import java.util.UUID
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

sealed class PokemonState {
    companion object {

        // If we ever move to need more NBT/JSON save/load types other than ShoulderedState we need a registry Codec.
        val states = mapOf(
            "inactive" to InactivePokemonState::class.java,
            "sent-out" to SentOutState::class.java,
            ShoulderedState.ID to ShoulderedState::class.java
        )

        fun fromBuffer(buffer: RegistryFriendlyByteBuf): PokemonState {
            val type = buffer.readString()
            return states[type]?.newInstance()?.readFromBuffer(buffer) ?: InactivePokemonState()
        }
    }

    val name: String
        get() = states.entries.find { it.value == this::class.java }!!.key

    open fun getIcon(pokemon: Pokemon): ResourceLocation? = null

    open fun writeToNBT(nbt: CompoundTag): CompoundTag? {
        nbt.putString(DataKeys.POKEMON_STATE_TYPE, name)
        return nbt
    }

    open fun readFromNBT(nbt: CompoundTag): PokemonState = this
    open fun writeToJSON(json: JsonObject): JsonObject? = json

    open fun readFromJSON(json: JsonObject): PokemonState = this
    open fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(name)
    }
    open fun readFromBuffer(buffer: RegistryFriendlyByteBuf): PokemonState = this
}
class InactivePokemonState : PokemonState() {
    override fun writeToNBT(nbt: CompoundTag) = null
    override fun equals(other: Any?) = other === this || other is InactivePokemonState
    override fun hashCode() = 0
    companion object {
        @JvmStatic
        val CODEC: Codec<InactivePokemonState> = Codec.unit { InactivePokemonState() }
    }
}

sealed class ActivePokemonState : PokemonState() {
    abstract val entity: PokemonEntity?
    abstract fun recall()
}
class SentOutState() : ActivePokemonState() {
    private var entityId: Int = -1
    private var dimension = Level.OVERWORLD

    override val entity: PokemonEntity?
        get() = Cobblemon.getLevel(dimension)?.getEntity(entityId) as? PokemonEntity

    constructor(entity: PokemonEntity): this() {
        this.entityId = entity.id
        this.dimension = entity.level().dimension()
    }

    override fun getIcon(pokemon: Pokemon) = cobblemonResource("textures/gui/party/party_icon_released.png")
    override fun writeToNBT(nbt: CompoundTag) = null
    override fun writeToJSON(json: JsonObject) = null

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        super.writeToBuffer(buffer)
        buffer.writeInt(entityId)
        buffer.writeString(dimension.location().toString())
    }

    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf): SentOutState {
        super.readFromBuffer(buffer)
        entityId = buffer.readInt()
        dimension = ResourceKey.create(ResourceKey.createRegistryKey(dimension.location()), ResourceLocation.parse(buffer.readString()))
        return this
    }

    fun update(entity: PokemonEntity) {
        entityId =  entity.id
        dimension = entity.level().dimension()
    }

    override fun recall() {
        entity?.discard()
    }
}
class ShoulderedState() : ActivePokemonState() {
    var isLeftShoulder = false
    lateinit var playerUUID: UUID
    lateinit var pokemonUUID: UUID
    var stateId = UUID.randomUUID()

    constructor(playerUUID: UUID, isLeftShoulder: Boolean, pokemonUUID: UUID): this() {
        this.isLeftShoulder = isLeftShoulder
        this.playerUUID = playerUUID
        this.pokemonUUID = pokemonUUID
    }

    override val entity: PokemonEntity? = null

    override fun getIcon(pokemon: Pokemon): ResourceLocation {
        val suffix = if (isLeftShoulder) "left" else "right"
        return cobblemonResource("textures/gui/party/party_icon_shoulder_$suffix.png")
    }
    override fun writeToNBT(nbt: CompoundTag): CompoundTag {
        super.writeToNBT(nbt)
        nbt.putBoolean(DataKeys.POKEMON_STATE_SHOULDER, isLeftShoulder)
        nbt.putUUID(DataKeys.POKEMON_STATE_PLAYER_UUID, playerUUID)
        nbt.putUUID(DataKeys.POKEMON_STATE_ID, stateId)
        nbt.putUUID(DataKeys.POKEMON_STATE_POKEMON_UUID, pokemonUUID)
        return nbt
    }

    override fun readFromNBT(nbt: CompoundTag): PokemonState {
        super.readFromNBT(nbt)
        isLeftShoulder = nbt.getBoolean(DataKeys.POKEMON_STATE_SHOULDER)
        playerUUID = nbt.getUUID(DataKeys.POKEMON_STATE_PLAYER_UUID)
        stateId = nbt.getUUID(DataKeys.POKEMON_STATE_ID)
        pokemonUUID = nbt.getUUID(DataKeys.POKEMON_STATE_POKEMON_UUID)
        return this
    }

    override fun writeToJSON(json: JsonObject): JsonObject? {
        super.writeToJSON(json)
        json.addProperty(DataKeys.POKEMON_STATE_SHOULDER, isLeftShoulder)
        json.addProperty(DataKeys.POKEMON_STATE_PLAYER_UUID, playerUUID.toString())
        json.addProperty(DataKeys.POKEMON_STATE_ID, stateId.toString())
        json.addProperty(DataKeys.POKEMON_STATE_POKEMON_UUID, pokemonUUID.toString())
        return json
    }

    override fun readFromJSON(json: JsonObject): PokemonState {
        super.readFromJSON(json)
        isLeftShoulder = json.get(DataKeys.POKEMON_STATE_SHOULDER).asBoolean
        playerUUID = UUID.fromString(json.get(DataKeys.POKEMON_STATE_PLAYER_UUID).asString)
        stateId = UUID.fromString(json.get(DataKeys.POKEMON_STATE_ID).asString)
        pokemonUUID = UUID.fromString(json.get(DataKeys.POKEMON_STATE_POKEMON_UUID).asString)
        return this
    }

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        super.writeToBuffer(buffer)
        buffer.writeBoolean(isLeftShoulder)
        buffer.writeUUID(playerUUID)
        buffer.writeUUID(stateId)
        buffer.writeUUID(pokemonUUID)
    }

    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf): PokemonState {
        super.readFromBuffer(buffer)
        isLeftShoulder = buffer.readBoolean()
        playerUUID = buffer.readUUID()
        stateId = buffer.readUUID()
        pokemonUUID = buffer.readUUID()
        return this
    }

    /**
     * Removes the cobblemon from the player's shoulder. (currently not used)
     */
    override fun recall() {
        val player = playerUUID.getPlayer() ?: return
        val nbt = if (isLeftShoulder) player.shoulderEntityLeft else player.shoulderEntityRight
        if (this.isShoulderedPokemon(nbt)) {
            player.level().playSoundServer(player.position(), SoundEvents.CANDLE_FALL)
            if (isLeftShoulder) {
                player.shoulderEntityLeft = CompoundTag()
            } else {
                player.shoulderEntityRight = CompoundTag()
            }
            this.removeShoulderEffects(player)
        }
    }

    private fun removeShoulderEffects(player: ServerPlayer) {
        val partyPokemon = player.party().find { pokemon -> pokemon.uuid == this.pokemonUUID }
        partyPokemon?.form?.shoulderEffects?.forEach { effect -> effect.removeEffect(partyPokemon, player, isLeftShoulder) }
    }

    private fun isShoulderedPokemon(nbt: CompoundTag): Boolean = nbt.isPokemonEntity()
            && nbt.getCompound(DataKeys.POKEMON)
            .getCompound(DataKeys.POKEMON_STATE)
            .getUUID(DataKeys.POKEMON_STATE_ID) == this.stateId

    fun isStillShouldered(player: ServerPlayer) = isShoulderedPokemon(if (isLeftShoulder) player.shoulderEntityLeft else player.shoulderEntityRight)

    companion object {

        internal const val ID = "shouldered"
        // If we ever move to need more NBT/JSON save/load we need a registry Codec.
        @JvmStatic
        val CODEC: Codec<ShoulderedState> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf(DataKeys.POKEMON_STATE_TYPE).forGetter { ID }, // Keep me for the sake of if we ever migrate to a registry.
                Codec.BOOL.fieldOf(DataKeys.POKEMON_STATE_SHOULDER).forGetter(ShoulderedState::isLeftShoulder),
                UUIDUtil.LENIENT_CODEC
                    .fieldOf(DataKeys.POKEMON_STATE_PLAYER_UUID).forGetter(ShoulderedState::playerUUID),
                UUIDUtil.LENIENT_CODEC.fieldOf(DataKeys.POKEMON_STATE_ID)
                    .forGetter(ShoulderedState::stateId),
                UUIDUtil.LENIENT_CODEC
                    .fieldOf(DataKeys.POKEMON_STATE_POKEMON_UUID).forGetter(ShoulderedState::pokemonUUID)
            ).apply(instance) { _, isLeftShoulder, playerUuid, stateId, pokemonUuid ->
                val state = ShoulderedState(playerUuid, isLeftShoulder, pokemonUuid)
                state.stateId = stateId
                return@apply state
            }
        }
    }
}