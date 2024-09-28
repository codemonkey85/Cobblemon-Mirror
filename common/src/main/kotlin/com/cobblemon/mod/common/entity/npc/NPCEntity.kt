/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.npc

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.struct.VariableStruct
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonEntities
import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.CobblemonSensors
import com.cobblemon.mod.common.api.entity.PokemonSender
import com.cobblemon.mod.common.api.molang.MoLangFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.cobblemon.mod.common.api.net.serializers.IdentifierDataSerializer
import com.cobblemon.mod.common.api.net.serializers.NPCPlayerTextureSerializer
import com.cobblemon.mod.common.api.net.serializers.PoseTypeDataSerializer
import com.cobblemon.mod.common.api.net.serializers.StringSetDataSerializer
import com.cobblemon.mod.common.api.net.serializers.UUIDSetDataSerializer
import com.cobblemon.mod.common.api.npc.NPCClasses
import com.cobblemon.mod.common.api.npc.configuration.NPCBattleConfiguration
import com.cobblemon.mod.common.api.npc.configuration.NPCBehaviourConfiguration
import com.cobblemon.mod.common.api.npc.configuration.NPCInteractConfiguration
import com.cobblemon.mod.common.api.npc.partyproviders.DynamicNPCParty
import com.cobblemon.mod.common.api.npc.partyproviders.NPCParty
import com.cobblemon.mod.common.api.npc.partyproviders.StaticNPCParty
import com.cobblemon.mod.common.api.scheduling.Schedulable
import com.cobblemon.mod.common.api.scheduling.SchedulingTracker
import com.cobblemon.mod.common.api.storage.party.NPCPartyStore
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.animation.PlayPosableAnimationPacket
import com.cobblemon.mod.common.net.messages.client.npc.CloseNPCEditorPacket
import com.cobblemon.mod.common.net.messages.client.npc.OpenNPCEditorPacket
import com.cobblemon.mod.common.net.messages.client.spawn.SpawnNPCPacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.getBattleState
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.makeEmptyBrainDynamic
import com.cobblemon.mod.common.util.withNPCValue
import com.google.common.collect.ImmutableList
import com.mojang.authlib.GameProfile
import com.mojang.authlib.ProfileLookupCallback
import com.mojang.serialization.Dynamic
import java.lang.Exception
import java.net.URI
import java.util.UUID
import java.util.concurrent.CompletableFuture
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.AgeableMob
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.ai.Brain
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.Sensor
import net.minecraft.world.entity.ai.sensing.SensorType
import net.minecraft.world.entity.npc.Npc
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

class NPCEntity(world: Level) : AgeableMob(CobblemonEntities.NPC, world), Npc, PosableEntity, PokemonSender, Schedulable {
    override val schedulingTracker = SchedulingTracker()

    override val struct = this.asMoLangValue()

    val runtime = MoLangRuntime().setup().withNPCValue(value = this)

    var editingPlayer: UUID? = null

    var npc = NPCClasses.dummy()
        set(value) {
            entityData.set(NPC_CLASS, value.id)
            val valueChanged = field != value
            field = value
            if (valueChanged) {
                customName = value.names.randomOrNull() ?: "NPC".text()
                brain = makeBrain(brainDynamic ?: makeEmptyBrainDynamic())
            }
        }

    var skill: Int? = npc.skill ?: 0 // range from 0 - 5

    var party: NPCParty? = null
    val staticParty: NPCPartyStore?
        get() = (party as? StaticNPCParty)?.party

    val appliedAspects = mutableSetOf<String>()
    override val delegate = if (world.isClientSide) {
        com.cobblemon.mod.common.client.entity.NPCClientDelegate()
    } else {
        NPCServerDelegate()
    }

    var battle: NPCBattleConfiguration? = null
    var behaviour: NPCBehaviourConfiguration? = null

    var interaction: NPCInteractConfiguration? = null

    var data = VariableStruct()
    var config = VariableStruct()

    val aspects: Set<String>
        get() = entityData.get(ASPECTS)

    val battleIds: Set<UUID>
        get() = entityData.get(BATTLE_IDS)

    var actionEffect: ActionEffectContext? = null

    /** Essentially a cached form of what was serialized to make memory reloads still work despite dynamic brain activities on class change. */
    private var brainDynamic: Dynamic<*>? = null


    /* TODO NPC Valuables to add:
     *
     * -- An 'interaction' configuration. This can be loaded from a JSON or API or even a .js (ambitious). Handles what happens
     * -- when you right click. Can be a dialogue tree with some complexity, or provides options to open a shopkeeper GUI,
     * -- that sort of deal. As extensible as we can manage it (and we can manage a lot).
     *
     * A 'party provider' configuration. This is for an NPC that's going to be used as a trainer. A stack of configuration
     * planning has been done by Vera and Design, get it from them and tweak to be clean.
     *
     * A pathing configuration. Another one that could be loaded from JSON or .js or API. Controls AI.
     *
     * npcs should be able to sleep lol
     */

    init {
        delegate.initialize(this)
        addPosableFunctions(struct)
        runtime.environment.query.addFunctions(struct.functions)
        refreshDimensions()
        navigation.setCanFloat(true)
        makeBrain(brainDynamic ?: makeEmptyBrainDynamic())
    }

    // This has to be below constructor and entity tracker fields otherwise initialization order is weird and breaks them syncing
    companion object {
        fun createAttributes(): AttributeSupplier.Builder = createMobAttributes()
            .add(Attributes.ATTACK_DAMAGE, 1.0)

        val NPC_CLASS = SynchedEntityData.defineId(NPCEntity::class.java, IdentifierDataSerializer)
        val ASPECTS = SynchedEntityData.defineId(NPCEntity::class.java, StringSetDataSerializer)
        val POSE_TYPE = SynchedEntityData.defineId(NPCEntity::class.java, PoseTypeDataSerializer)
        val BATTLE_IDS = SynchedEntityData.defineId(NPCEntity::class.java, UUIDSetDataSerializer)
        val NPC_PLAYER_TEXTURE = SynchedEntityData.defineId(NPCEntity::class.java, NPCPlayerTextureSerializer)


//        val BATTLING = Activity.register("npc_battling")

        val SENSORS: Collection<SensorType<out Sensor<in NPCEntity>>> = listOf(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.HURT_BY,
            SensorType.NEAREST_PLAYERS,
            CobblemonSensors.BATTLING_POKEMON,
            CobblemonSensors.NPC_BATTLING
        )

        val MEMORY_MODULES: List<MemoryModuleType<*>> = ImmutableList.of(
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,
            MemoryModuleType.IS_PANICKING,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            CobblemonMemories.NPC_BATTLING,
            CobblemonMemories.BATTLING_POKEMON,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.ANGRY_AT,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            CobblemonMemories.DIALOGUES,
            CobblemonMemories.ACTIVE_ACTION_EFFECT,
        )

        const val SEND_OUT_ANIMATION = "send_out"
        const val RECALL_ANIMATION = "recall"
        const val LOSE_ANIMATION = "lose"
        const val WIN_ANIMATION = "win"
    }

    override fun brainProvider(): Brain.Provider<out NPCEntity> = Brain.provider(MEMORY_MODULES, SENSORS)
    override fun getBreedOffspring(world: ServerLevel, entity: AgeableMob) = null // No lovemaking! Unless...
    override fun getCurrentPoseType() = this.entityData.get(POSE_TYPE)

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(NPC_CLASS, NPCClasses.classes.first().id)
        builder.define(ASPECTS, emptySet())
        builder.define(POSE_TYPE, PoseType.STAND)
        builder.define(BATTLE_IDS, setOf())
        builder.define(NPC_PLAYER_TEXTURE, NPCPlayerTexture(ByteArray(1), NPCPlayerModelType.NONE))
    }

    override fun getAddEntityPacket(serverEntity: ServerEntity) = ClientboundCustomPayloadPacket(
        SpawnNPCPacket(
            this,
            super.getAddEntityPacket(serverEntity) as ClientboundAddEntityPacket
        )
    ) as Packet<ClientGamePacketListener>

    override fun makeBrain(dynamic: Dynamic<*>): Brain<out NPCEntity> {
        this.brainDynamic = dynamic
        val brain = brainProvider().makeBrain(dynamic)
        if (npc != null) {
            NPCBrain.configure(this, npc, brain)
        }
        return brain
    }

    override fun doHurtTarget(target: Entity): Boolean {
        target as ServerPlayer
        return target.hurt(this.damageSources().mobAttack(this), attributes.getValue(Attributes.ATTACK_DAMAGE).toFloat() * 5F)
    }

    override fun getBrain() = super.getBrain() as Brain<NPCEntity>

    fun updateAspects() {
        entityData.set(ASPECTS, appliedAspects)
    }

    fun isInBattle() = battleIds.isNotEmpty()
    fun getBattleConfiguration() = battle ?: npc.battleConfiguration

    override fun tick() {
        super.tick()
        delegate.tick(this)
        schedulingTracker.update(1/20F)
    }

    override fun customServerAiStep() {
        super.customServerAiStep()
        getBrain().tick(level() as ServerLevel, this)
    }

    override fun saveWithoutId(nbt: CompoundTag): CompoundTag {
        super.saveWithoutId(nbt)
        nbt.put(DataKeys.NPC_DATA, MoLangFunctions.writeMoValueToNBT(data))
        nbt.put(DataKeys.NPC_CONFIG, MoLangFunctions.writeMoValueToNBT(config))
        nbt.putString(DataKeys.NPC_CLASS, npc.id.toString())
        nbt.put(DataKeys.NPC_ASPECTS, ListTag().also { list -> appliedAspects.forEach { list.add(StringTag.valueOf(it)) } })
        interaction?.let {
            val interactionNBT = CompoundTag()
            interactionNBT.putString(DataKeys.NPC_INTERACT_TYPE, it.type)
            it.writeToNBT(interactionNBT)
            nbt.put(DataKeys.NPC_INTERACTION, interactionNBT)
        }
        val battle = battle
        if (battle != null) {
            val battleNBT = CompoundTag()
            battle.saveToNBT(battleNBT)
            nbt.put(DataKeys.NPC_BATTLE_CONFIGURATION, battleNBT)
        }
        nbt.putInt("skill", skill ?: 0)
        val party = party
        if (party != null) {
            val partyNBT = CompoundTag()
            partyNBT.putBoolean(DataKeys.NPC_PARTY_IS_STATIC, party is StaticNPCParty)
            party.saveToNBT(partyNBT, registryAccess())
            nbt.put(DataKeys.NPC_PARTY, partyNBT)
        }
        val playerTexture = entityData.get(NPC_PLAYER_TEXTURE)
        if (playerTexture.model != NPCPlayerModelType.NONE) {
            nbt.put(DataKeys.NPC_PLAYER_TEXTURE, CompoundTag().also {
                it.putString(DataKeys.NPC_PLAYER_TEXTURE_MODEL, playerTexture.model.name)
                it.putByteArray(DataKeys.NPC_PLAYER_TEXTURE_TEXTURE, playerTexture.texture)
            })
        }
        return nbt
    }

    override fun hasCustomName() = true
    override fun isCustomNameVisible() = true

    override fun load(nbt: CompoundTag) {
        npc = NPCClasses.getByIdentifier(ResourceLocation.parse(nbt.getString(DataKeys.NPC_CLASS))) ?: NPCClasses.classes.first()
        super.load(nbt)
        data = MoLangFunctions.readMoValueFromNBT(nbt.getCompound(DataKeys.NPC_DATA)) as VariableStruct
        config = if (nbt.contains(DataKeys.NPC_CONFIG)) MoLangFunctions.readMoValueFromNBT(nbt.getCompound(DataKeys.NPC_CONFIG)) as VariableStruct else VariableStruct()
        appliedAspects.addAll(nbt.getList(DataKeys.NPC_ASPECTS, Tag.TAG_STRING.toInt()).map { it.asString })
        nbt.getCompound(DataKeys.NPC_INTERACTION).takeIf { !it.isEmpty }?.let { nbt ->
            val type = nbt.getString("type")
            val configType = NPCInteractConfiguration.types[type] ?: return@let
            interaction = configType.clazz.getConstructor().newInstance().also { it.readFromNBT(nbt) }
        }
        val battleNBT = nbt.getCompound(DataKeys.NPC_BATTLE_CONFIGURATION)
        if (!battleNBT.isEmpty) {
            battle = NPCBattleConfiguration().also { it.loadFromNBT(battleNBT) }
        }
        this.skill = npc.skill
        val partyNBT = nbt.getCompound(DataKeys.NPC_PARTY)
        if (!partyNBT.isEmpty) {
            val isStatic = partyNBT.getBoolean(DataKeys.NPC_PARTY_IS_STATIC)
            this.party = if (isStatic) {
                    StaticNPCParty(NPCPartyStore(this)).also { it.loadFromNBT(partyNBT, registryAccess()) }
            } else {
                val type = partyNBT.getString(DataKeys.NPC_PARTY_TYPE)
                val clazz = DynamicNPCParty.types[type]
                if (clazz == null) {
                    Cobblemon.LOGGER.error("Tried deserializing NPC entity with unknown party type: $type. I am at $x $y $z. Setting party to null.")
                    null
                } else {
                    val party = clazz.getConstructor().newInstance()
                    party.loadFromNBT(partyNBT, registryAccess())
                    party
                }
            }
        }
        if (nbt.contains(DataKeys.NPC_PLAYER_TEXTURE)) {
            val textureNBT = nbt.getCompound(DataKeys.NPC_PLAYER_TEXTURE)
            val model = NPCPlayerModelType.valueOf(textureNBT.getString(DataKeys.NPC_PLAYER_TEXTURE_MODEL))
            val texture = textureNBT.getByteArray(DataKeys.NPC_PLAYER_TEXTURE_TEXTURE)
            entityData.set(NPC_PLAYER_TEXTURE, NPCPlayerTexture(texture, model))
        }
        updateAspects()
    }

    fun loadTextureFromGameProfileName(username: String) {
        val server = server ?: return
        server.profileRepository.findProfilesByNames(arrayOf(username), object : ProfileLookupCallback {
            override fun onProfileLookupSucceeded(profile: GameProfile) {
                val deepProfile = server.sessionService.fetchProfile(profile.id, false)?.profile ?: return Cobblemon.LOGGER.error("Failed to fetch profile for game profile name: $username")
                val textures = server.sessionService.getTextures(deepProfile)
                val skin = textures.skin!!
                val url = skin.url
                val model = NPCPlayerModelType.valueOf((skin.getMetadata("model") ?: "default").uppercase())
                loadTexture(URI(url), model)
            }

            override fun onProfileLookupFailed(profileName: String, exception: Exception) {
                Cobblemon.LOGGER.error("Unable to load texture for game profile name: $username")
            }
        })
    }

    fun loadTexture(uri: URI, model: NPCPlayerModelType) {
        appliedAspects -= "model-default"
        appliedAspects -= "model-slim"
        appliedAspects += "model-${model.name.lowercase()}"
        entityData.set(NPC_PLAYER_TEXTURE, NPCPlayerTexture(uri.toURL().openStream().readBytes(), model))
        updateAspects()
    }

    override fun getDefaultDimensions(pose: Pose) = npc.hitbox

    fun initialize(level: Int) {
        appliedAspects.clear()
        npc.config.forEach { it.applyDefault(this) }
        npc.variations.values.forEach { this.appliedAspects.addAll(it.provideAspects(this)) }
        party = npc.party?.provide(this, level)
    }

    override fun mobInteract(player: Player, hand: InteractionHand): InteractionResult {
        if (player is ServerPlayer && hand == InteractionHand.MAIN_HAND) {
            if (player.getBattleState()?.first?.getActor(this) != null) {
                return InteractionResult.PASS
            }

            (interaction ?: npc.interaction)?.interact(this, player)
//            val battle = getBattleConfiguration()
//            if (battle.canChallenge) {
//                val provider = battle.party
//                if (provider != null) {
//                    val party = provider.provide(this, listOf(player))
//                    val result = BattleBuilder.pvn(
//                        player = player,
//                        npcEntity = this
//                    )
//                }
//            }
        }
        return InteractionResult.SUCCESS
    }

    fun playAnimation(animation: String, expressions: List<String> = emptyList()) {
        val packet = PlayPosableAnimationPacket(
            entityId = id,
            animation = setOf(animation),
            expressions = expressions
        )
        packet.sendToPlayers(level().players().filterIsInstance<ServerPlayer>().filter { it.distanceTo(this) < 256 })
    }

    override fun recalling(pokemonEntity: PokemonEntity): CompletableFuture<Unit> {
        playAnimation(RECALL_ANIMATION, pokemonExpressions(pokemonEntity.pokemon))
        return delayedFuture(seconds = 1.6F)
    }

    override fun sendingOut(pokemon: Pokemon): CompletableFuture<Unit> {
        playAnimation(SEND_OUT_ANIMATION, pokemonExpressions(pokemon))
        return delayedFuture(seconds = 1.6F)
    }

    private fun pokemonExpressions(pokemon: Pokemon): List<String> {
        return listOf(
            "v.actioning_pokemon_name=\"${pokemon.species.name}\";",
            "v.actioning_pokemon_level=${pokemon.level};",
            "v.actioning_pokemon_ball=\"${pokemon.caughtBall.name}\";",
            "v.actioning_pokemon_shiny=\"${pokemon.shiny}\";",
        )
    }

    override fun onSyncedDataUpdated(data: EntityDataAccessor<*>) {
        super.onSyncedDataUpdated(data)
        if (delegate != null) {
            delegate.onSyncedDataUpdated(data)
        }
    }

    fun edit(player: ServerPlayer) {
        val lastEditing = editingPlayer?.getPlayer()
        if (lastEditing != null) {
            lastEditing.sendPacket(CloseNPCEditorPacket())
        }
        player.sendPacket(OpenNPCEditorPacket(this))
        editingPlayer = player.uuid
    }
}