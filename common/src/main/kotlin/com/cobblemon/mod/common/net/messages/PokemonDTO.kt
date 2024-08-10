/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages

import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.moves.BenchedMoves
import com.cobblemon.mod.common.api.moves.MoveSet
import com.cobblemon.mod.common.api.net.Decodable
import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatures
import com.cobblemon.mod.common.api.pokemon.feature.SynchronizedSpeciesFeature
import com.cobblemon.mod.common.api.pokemon.feature.SynchronizedSpeciesFeatureProvider
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.types.tera.TeraTypes
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.pokemon.*
import com.cobblemon.mod.common.pokemon.activestate.PokemonState
import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import io.netty.buffer.Unpooled
import java.util.UUID
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.MutableText
import net.minecraft.util.Identifier

/**
 * A data transfer object for an entire [Pokemon], complete with all of the information a player is allowed
 * to know about their own Pokémon. The main purpose of this is to have a write- and read-safe object that
 * doesn't need reference data to be loaded prior to being mapped back into a [Pokemon] instance.
 *
 * @author Hiroku
 * @since November 10th, 2022
 */
class PokemonDTO : Encodable, Decodable {
    var toClient = false
    var uuid = UUID.randomUUID()
    lateinit var species: Identifier
    var nickname: MutableText? = null
    var form = ""
    var level = 1
    var experience = 1
    var friendship = 0
    var currentHealth = 0
    var gender = Gender.MALE
    var ivs = IVs()
    var evs = EVs()
    var moveSet = MoveSet()
    var scaleModifier = 0F
    var ability = ""
    var shiny = false
    var status: Identifier? = null
    lateinit var state: PokemonState
    lateinit var caughtBall: Identifier
    var benchedMoves = BenchedMoves()
    var aspects = setOf<String>()
    lateinit var evolutionBuffer: PacketByteBuf
    lateinit var nature: Identifier
    var mintNature: Identifier? = null
    var heldItem: ItemStack = ItemStack.EMPTY
    var tetheringId: UUID? = null
    var teraType = ""
    var dmaxLevel = 0
    var gmaxFactor = false
    var tradeable = true
//    var features: List<SynchronizedSpeciesFeature> = emptyList()
    lateinit var featuresBuffer: PacketByteBuf
    var originalTrainerType: OriginalTrainerType = OriginalTrainerType.NONE
    var originalTrainer: String? = null
    var originalTrainerName: String? = null

    constructor()
    constructor(pokemon: Pokemon, toClient: Boolean) {
        this.toClient = toClient
        this.uuid = pokemon.uuid
        this.species = pokemon.species.resourceIdentifier
        this.nickname = pokemon.nickname
        this.form = pokemon.form.name
        this.level = pokemon.level
        this.experience = pokemon.experience
        this.friendship = pokemon.friendship
        this.currentHealth = pokemon.currentHealth
        this.gender = pokemon.gender
        this.ivs = pokemon.ivs
        this.evs = pokemon.evs
        this.moveSet = pokemon.moveSet
        this.scaleModifier = pokemon.scaleModifier
        this.ability = pokemon.ability.name
        this.shiny = pokemon.shiny
        this.status = pokemon.status?.status?.name
        this.state = pokemon.state
        this.caughtBall = pokemon.caughtBall.name
        this.benchedMoves = pokemon.benchedMoves
        this.aspects = pokemon.aspects
        evolutionBuffer = PacketByteBuf(Unpooled.buffer())
        pokemon.evolutionProxy.saveToBuffer(evolutionBuffer, toClient)
        this.nature = pokemon.nature.name
        this.mintNature = pokemon.mintedNature?.name
        this.heldItem = pokemon.heldItemNoCopy()
        this.tetheringId = pokemon.tetheringId
        this.teraType = pokemon.teraType.id.toString()
        this.dmaxLevel = pokemon.dmaxLevel
        this.gmaxFactor = pokemon.gmaxFactor
        this.tradeable = pokemon.tradeable
        this.featuresBuffer = PacketByteBuf(Unpooled.buffer())
        val visibleFeatures = pokemon.features
            .filterIsInstance<SynchronizedSpeciesFeature>()
            .filter { (SpeciesFeatures.getFeature(it.name) as? SynchronizedSpeciesFeatureProvider<*>)?.visible == true }
        featuresBuffer.writeCollection(visibleFeatures) { _, value ->
            featuresBuffer.writeString(value.name)
            value.encode(featuresBuffer)
        }

        this.originalTrainerType = pokemon.originalTrainerType
        this.originalTrainer = pokemon.originalTrainer
        this.originalTrainerName = pokemon.originalTrainerName
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeBoolean(toClient)
        buffer.writeUuid(uuid)
        buffer.writeIdentifier(species)
        buffer.writeNullable(nickname) { _, v -> buffer.writeText(v) }
        buffer.writeString(form)
        buffer.writeInt(experience)
        buffer.writeSizedInt(IntSize.U_SHORT, level)
        buffer.writeShort(friendship)
        buffer.writeShort(currentHealth)
        buffer.writeSizedInt(IntSize.U_BYTE, gender.ordinal)
        ivs.saveToBuffer(buffer)
        evs.saveToBuffer(buffer)
        moveSet.saveToBuffer(buffer)
        buffer.writeFloat(scaleModifier)
        buffer.writeString(ability)
        buffer.writeBoolean(shiny)
        state.writeToBuffer(buffer)
        buffer.writeNullable(status) { _, v -> buffer.writeIdentifier(v) }
        buffer.writeIdentifier(caughtBall)
        benchedMoves.saveToBuffer(buffer)
        buffer.writeSizedInt(IntSize.U_BYTE, aspects.size)
        aspects.forEach { buffer.writeString(it) }
        val byteCount = evolutionBuffer.readableBytes()
        buffer.writeSizedInt(IntSize.U_SHORT, byteCount)
        buffer.writeBytes(evolutionBuffer)
        evolutionBuffer.release()
        buffer.writeIdentifier(nature)
        buffer.writeNullable(mintNature) { _, v -> buffer.writeIdentifier(v) }
        buffer.writeItemStack(this.heldItem)
        buffer.writeNullable(tetheringId) { _, v -> buffer.writeUuid(v) }
        buffer.writeString(teraType)
        buffer.writeInt(dmaxLevel)
        buffer.writeBoolean(gmaxFactor)
        buffer.writeBoolean(tradeable)
        val featureByteCount = featuresBuffer.readableBytes()
        buffer.writeSizedInt(IntSize.U_SHORT, featureByteCount)
        buffer.writeBytes(featuresBuffer)
        featuresBuffer.release()
        buffer.writeString(originalTrainerType.name)
        buffer.writeNullable(originalTrainer) { _, v -> buffer.writeString(v) }
        buffer.writeNullable(originalTrainerName) { _, v -> buffer.writeString(v) }
    }

    override fun decode(buffer: PacketByteBuf) {
        toClient = buffer.readBoolean()
        uuid = buffer.readUuid()
        species = buffer.readIdentifier()
        nickname = buffer.readNullable { buffer.readText().copy() }
        form = buffer.readString()
        experience = buffer.readInt()
        level = buffer.readSizedInt(IntSize.U_SHORT)
        friendship = buffer.readUnsignedShort()
        currentHealth = buffer.readUnsignedShort()
        gender = Gender.values()[buffer.readSizedInt(IntSize.U_BYTE)]
        ivs.loadFromBuffer(buffer)
        evs.loadFromBuffer(buffer)
        moveSet.loadFromBuffer(buffer)
        scaleModifier = buffer.readFloat()
        ability = buffer.readString()
        shiny = buffer.readBoolean()
        state = PokemonState.fromBuffer(buffer)
        status = buffer.readNullable { buffer.readIdentifier() }
        caughtBall = buffer.readIdentifier()
        benchedMoves.loadFromBuffer(buffer)
        val aspects = mutableSetOf<String>()
        repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
            aspects.add(buffer.readString())
        }
        this.aspects = aspects
        val bytesToRead = buffer.readSizedInt(IntSize.U_SHORT)
        evolutionBuffer = PacketByteBuf(buffer.readBytes(bytesToRead))
        nature = buffer.readIdentifier()
        mintNature = buffer.readNullable { buffer.readIdentifier() }
        heldItem = buffer.readItemStack()
        tetheringId = buffer.readNullable { buffer.readUuid() }
        teraType = buffer.readString()
        dmaxLevel = buffer.readInt()
        gmaxFactor = buffer.readBoolean()
        tradeable = buffer.readBoolean()

        val featureBytesToRead = buffer.readSizedInt(IntSize.U_SHORT)
        featuresBuffer = PacketByteBuf(buffer.readBytes(featureBytesToRead))
        originalTrainerType = OriginalTrainerType.valueOf(buffer.readString())
        originalTrainer = buffer.readNullable { buffer.readString() }
        originalTrainerName = buffer.readNullable { buffer.readString() }
    }

    fun create(): Pokemon {
        return Pokemon().also {
            it.isClient = toClient
            it.uuid = uuid
            it.species = PokemonSpecies.getByIdentifier(species)!!
            it.nickname = nickname
            it.form = it.species.forms.find { it.name == form } ?: it.species.standardForm
            it.experience = experience
            it.level = level
            it.setFriendship(friendship)
            it.gender = gender
            ivs.forEach { stat ->
                it.setIV(stat.key, stat.value)
            }
            evs.forEach { stat ->
                it.setEV(stat.key, stat.value)
            }
            it.currentHealth = currentHealth
            it.moveSet.clear()
            for (move in moveSet) {
                it.moveSet.add(move)
            }
            it.scaleModifier = scaleModifier
            it.ability = Abilities.getOrException(ability).create()
            it.shiny = shiny
            it.state = state
            it.status = status?.let { id ->
                val statusType = Statuses.getStatus(id)
                if (statusType is PersistentStatus) {
                    PersistentStatusContainer(statusType, 0)
                } else {
                    null
                }
            }
            it.caughtBall = PokeBalls.getPokeBall(caughtBall)!!
            it.benchedMoves.addAll(benchedMoves)
            it.aspects = aspects
            it.evolutionProxy.loadFromBuffer(evolutionBuffer)
            evolutionBuffer.release()
            it.nature = Natures.getNature(nature)!!
            it.mintedNature = mintNature?.let { id -> Natures.getNature(id)!! }
            it.swapHeldItem(heldItem, false)
            it.tetheringId = tetheringId
            it.teraType = TeraTypes.get(this.teraType.asIdentifierDefaultingNamespace())!!
            it.dmaxLevel = dmaxLevel
            it.gmaxFactor = gmaxFactor
            it.tradeable = tradeable
            repeat(times = featuresBuffer.readSizedInt(IntSize.U_BYTE)) { _ ->
                val species = PokemonSpecies.getByIdentifier(this.species)!!
                val speciesFeatureName = featuresBuffer.readString()
                val featureProviders = SpeciesFeatures
                    .getFeaturesFor(species)
                    .filterIsInstance<SynchronizedSpeciesFeatureProvider<*>>()
                val feature = featureProviders.firstNotNullOfOrNull { it(featuresBuffer, speciesFeatureName) }
                    ?: throw IllegalArgumentException("Couldn't find a feature provider to deserialize this feature. Something's wrong.")
                it.features.removeIf { it.name == feature.name }
                it.features.add(feature)
            }
            when (originalTrainerType)
            {
                OriginalTrainerType.NONE -> {
                    it.removeOriginalTrainer()
                }
                OriginalTrainerType.PLAYER -> {
                    originalTrainer?.let { ot ->
                        UUID.fromString(ot)?.let { uuid ->
                            it.setOriginalTrainer(uuid)
                        }
                    }
                }
                OriginalTrainerType.NPC ->
                {
                    originalTrainer?.let { ot ->
                        it.setOriginalTrainer(ot)
                    }
                }
            }
            it.originalTrainerName = originalTrainerName
        }
    }
}