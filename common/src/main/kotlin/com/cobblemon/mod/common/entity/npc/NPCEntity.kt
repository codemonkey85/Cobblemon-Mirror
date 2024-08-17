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
import com.cobblemon.mod.common.CobblemonEntities
import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.CobblemonSensors
import com.cobblemon.mod.common.GenericsCheatClass.createNPCBrain
import com.cobblemon.mod.common.api.entity.PokemonSender
import com.cobblemon.mod.common.api.molang.MoLangFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.net.serializers.IdentifierDataSerializer
import com.cobblemon.mod.common.api.net.serializers.PoseTypeDataSerializer
import com.cobblemon.mod.common.api.net.serializers.StringSetDataSerializer
import com.cobblemon.mod.common.api.net.serializers.UUIDSetDataSerializer
import com.cobblemon.mod.common.api.npc.NPCClasses
import com.cobblemon.mod.common.api.npc.configuration.NPCBattleConfiguration
import com.cobblemon.mod.common.api.npc.configuration.NPCBehaviourConfiguration
import com.cobblemon.mod.common.api.npc.configuration.NPCInteractConfiguration
import com.cobblemon.mod.common.api.scheduling.Schedulable
import com.cobblemon.mod.common.api.scheduling.SchedulingTracker
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.ai.AttackAngryAtTask
import com.cobblemon.mod.common.entity.ai.FollowWalkTargetTask
import com.cobblemon.mod.common.entity.ai.GetAngryAtAttackerTask
import com.cobblemon.mod.common.entity.ai.MoveToAttackTargetTask
import com.cobblemon.mod.common.entity.ai.StayAfloatTask
import com.cobblemon.mod.common.entity.npc.ai.ChooseWanderTargetTask
import com.cobblemon.mod.common.entity.npc.ai.MeleeAttackTask
import com.cobblemon.mod.common.entity.npc.ai.SwitchToBattleTask
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.animation.PlayPosableAnimationPacket
import com.cobblemon.mod.common.net.messages.client.npc.CloseNPCEditorPacket
import com.cobblemon.mod.common.net.messages.client.npc.OpenNPCEditorPacket
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.withNPCValue
import com.google.common.collect.ImmutableList
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Dynamic
import java.util.UUID
import java.util.concurrent.CompletableFuture
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.resources.ResourceLocation
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
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink
import net.minecraft.world.entity.ai.behavior.RunOne
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget
import net.minecraft.world.entity.ai.behavior.StopBeingAngryIfTargetDead
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.Sensor
import net.minecraft.world.entity.ai.sensing.SensorType
import net.minecraft.world.entity.npc.Npc
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.schedule.Activity
import net.minecraft.world.level.Level

class NPCEntity(world: Level) : AgeableMob(CobblemonEntities.NPC, world), Npc, PosableEntity, PokemonSender, Schedulable {
    override val schedulingTracker = SchedulingTracker()

    override val struct = this.asMoLangValue()

    val runtime = MoLangRuntime().setup().withNPCValue(value = this)

    var editingPlayer: UUID? = null

    var npc = NPCClasses.random()
        set(value) {
            entityData.set(NPC_CLASS, value.resourceIdentifier)
            field = value
        }

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

    val aspects: Set<String>
        get() = entityData.get(ASPECTS)

    val battleIds: Set<UUID>
        get() = entityData.get(BATTLE_IDS)


    /* TODO NPC Valuables to add:
     *
     * An 'interaction' configuration. This can be loaded from a JSON or API or even a .js (ambitious). Handles what happens
     * when you right click. Can be a dialogue tree with some complexity, or provides options to open a shopkeeper GUI,
     * that sort of deal. As extensible as we can manage it (and we can manage a lot).
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
    }

    // This has to be below constructor and entity tracker fields otherwise initialization order is weird and breaks them syncing
    companion object {
        fun createAttributes(): AttributeSupplier.Builder = createMobAttributes()
            .add(Attributes.ATTACK_DAMAGE, 1.0)

        val NPC_CLASS = SynchedEntityData.defineId(NPCEntity::class.java, IdentifierDataSerializer)
        val ASPECTS = SynchedEntityData.defineId(NPCEntity::class.java, StringSetDataSerializer)
        val POSE_TYPE = SynchedEntityData.defineId(NPCEntity::class.java, PoseTypeDataSerializer)
        val BATTLE_IDS = SynchedEntityData.defineId(NPCEntity::class.java, UUIDSetDataSerializer)

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
        )

        const val SEND_OUT_ANIMATION = "send_out"
        const val RECALL_ANIMATION = "recall"
        const val LOSE_ANIMATION = "lose"
        const val WIN_ANIMATION = "win"
    }

    override fun brainProvider() = createNPCBrain(MEMORY_MODULES, SENSORS)
    override fun getBreedOffspring(world: ServerLevel, entity: AgeableMob) = null // No lovemaking! Unless...
    override fun getCurrentPoseType() = this.entityData.get(POSE_TYPE)

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(NPC_CLASS, NPCClasses.classes.first().resourceIdentifier)
        builder.define(ASPECTS, emptySet())
        builder.define(POSE_TYPE, PoseType.STAND)
        builder.define(BATTLE_IDS, setOf())
    }

    override fun makeBrain(dynamic: Dynamic<*>): Brain<NPCEntity> {
        val brain = brainProvider().makeBrain(dynamic)
        brain.addActivity(
            Activity.CORE, ImmutableList.of(
            Pair.of(0, StayAfloatTask(0.8F)),
            Pair.of(0, GetAngryAtAttackerTask.create()),
            Pair.of(0, StopBeingAngryIfTargetDead.create())
        ))

        brain.addActivity(Activity.IDLE, ImmutableList.of(
            Pair.of(1, RunOne(
                ImmutableList.of(
                    Pair.of(LookAtTargetSink(45, 90), 2),
                    Pair.of(SetEntityLookTarget.create(15F), 2),
                    Pair.of(ChooseWanderTargetTask.create(horizontalRange = 10, verticalRange = 5, walkSpeed = 0.33F, completionRange = 1), 1)
                )
            )),
            Pair.of(1, FollowWalkTargetTask()),
            Pair.of(0, SwitchToBattleTask.create()),
            Pair.of(1, AttackAngryAtTask.create()),
            Pair.of(1, MoveToAttackTargetTask.create()),
            Pair.of(1, MeleeAttackTask.create(2F, 30L))
        ))
//        brain.setTaskList(BATTLING, ImmutableList.of(
//            Pair.of(0, SwitchFromBattleTask.create()),
//            Pair.of(1, LookAroundTask(45, 90)),
//            Pair.of(2, LookAtBattlingPokemonTask.create()),
//
//        ))
        brain.setCoreActivities(setOf(Activity.CORE))
        brain.setDefaultActivity(Activity.IDLE)
        brain.useDefaultActivity()
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
        nbt.putString(DataKeys.NPC_CLASS, npc.resourceIdentifier.toString())
        nbt.put(DataKeys.NPC_ASPECTS, ListTag().also { list -> appliedAspects.forEach { list.add(StringTag.valueOf(it)) } })
        interaction?.let {
            val interactionNBT = CompoundTag()
            it.writeToNBT(interactionNBT)
            nbt.put(DataKeys.NPC_INTERACTION, interactionNBT)
        }
        val battle = battle
        if (battle != null) {
            val battleNBT = CompoundTag()
            battle.saveToNBT(battleNBT)
            nbt.put(DataKeys.NPC_BATTLE_CONFIGURATION, battleNBT)
        }
        return nbt
    }

    override fun load(nbt: CompoundTag) {
        super.load(nbt)
        npc = NPCClasses.getByIdentifier(ResourceLocation.parse(nbt.getString(DataKeys.NPC_CLASS))) ?: NPCClasses.classes.first()
        data = MoLangFunctions.readMoValueFromNBT(nbt.getCompound(DataKeys.NPC_DATA)) as VariableStruct
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
        updateAspects()
    }

    override fun getDefaultDimensions(pose: Pose) = npc.hitbox

    override fun mobInteract(player: Player, hand: InteractionHand): InteractionResult {
        if (player is ServerPlayer && hand == InteractionHand.MAIN_HAND) {
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

    fun playAnimation(vararg animation: String) {
        val packet = PlayPosableAnimationPacket(
            entityId = id,
            animation = animation.toSet(),
            expressions = emptySet()
        )
        packet.sendToPlayers(level().players().filterIsInstance<ServerPlayer>().filter { it.distanceTo(this) < 256 })
    }

    override fun recalling(pokemonEntity: PokemonEntity): CompletableFuture<Unit> {
        playAnimation(RECALL_ANIMATION)
        return delayedFuture(seconds = 1.6F)
    }

    override fun sendingOut(): CompletableFuture<Unit> {
        playAnimation(SEND_OUT_ANIMATION)
        return delayedFuture(seconds = 1.6F)
    }

    override fun onSyncedDataUpdated(data: EntityDataAccessor<*>) {
        super.onSyncedDataUpdated(data)
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