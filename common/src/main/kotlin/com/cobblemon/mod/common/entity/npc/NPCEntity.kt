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
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.MoLangFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.molang.runScript
import com.cobblemon.mod.common.api.net.serializers.IdentifierDataSerializer
import com.cobblemon.mod.common.api.net.serializers.PoseTypeDataSerializer
import com.cobblemon.mod.common.api.net.serializers.StringSetDataSerializer
import com.cobblemon.mod.common.api.net.serializers.UUIDSetDataSerializer
import com.cobblemon.mod.common.api.npc.NPCClasses
import com.cobblemon.mod.common.api.npc.configuration.NPCBattleConfiguration
import com.cobblemon.mod.common.api.npc.configuration.NPCBehaviourConfiguration
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
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.withNPCValue
import com.cobblemon.mod.common.util.withPlayerValue
import com.google.common.collect.ImmutableList
import com.mojang.datafixers.util.Either
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Dynamic
import java.util.UUID
import java.util.concurrent.CompletableFuture
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityPose
import net.minecraft.entity.Npc
import net.minecraft.entity.ai.brain.Activity
import net.minecraft.entity.ai.brain.Brain
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.ai.brain.sensor.Sensor
import net.minecraft.entity.ai.brain.sensor.SensorType
import net.minecraft.entity.ai.brain.task.ForgetAngryAtTargetTask
import net.minecraft.entity.ai.brain.task.LookAroundTask
import net.minecraft.entity.ai.brain.task.LookAtMobTask
import net.minecraft.entity.ai.brain.task.RandomTask
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.world.World

class NPCEntity(world: World) : PassiveEntity(CobblemonEntities.NPC, world), Npc, PosableEntity, PokemonSender, Schedulable {
    override val schedulingTracker = SchedulingTracker()

    override val struct = this.asMoLangValue()

    val runtime = MoLangRuntime().setup().withNPCValue(value = this)

    var editingPlayer: UUID? = null

    var npc = NPCClasses.random()
        set(value) {
            dataTracker.set(NPC_CLASS, value.resourceIdentifier)
            field = value
        }

    val appliedAspects = mutableSetOf<String>()
    override val delegate = if (world.isClient) {
        com.cobblemon.mod.common.client.entity.NPCClientDelegate()
    } else {
        NPCServerDelegate()
    }

    var battle: NPCBattleConfiguration? = null
    var behaviour: NPCBehaviourConfiguration? = null

    var interaction: Either<Identifier, ExpressionLike>? = null

    var data = VariableStruct()

    val aspects: Set<String>
        get() = dataTracker.get(ASPECTS)

    val battleIds: Set<UUID>
        get() = dataTracker.get(BATTLE_IDS)


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
        calculateDimensions()
        navigation.setCanSwim(true)
    }

    // This has to be below constructor and entity tracker fields otherwise initialization order is weird and breaks them syncing
    companion object {
        fun createAttributes(): DefaultAttributeContainer.Builder = createMobAttributes()
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0)

        val NPC_CLASS = DataTracker.registerData(NPCEntity::class.java, IdentifierDataSerializer)
        val ASPECTS = DataTracker.registerData(NPCEntity::class.java, StringSetDataSerializer)
        val POSE_TYPE = DataTracker.registerData(NPCEntity::class.java, PoseTypeDataSerializer)
        val BATTLE_IDS = DataTracker.registerData(NPCEntity::class.java, UUIDSetDataSerializer)

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
            MemoryModuleType.VISIBLE_MOBS,
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

    override fun createBrainProfile() = createNPCBrain(MEMORY_MODULES, SENSORS)
    override fun createChild(world: ServerWorld, entity: PassiveEntity) = null // No lovemaking! Unless...
    override fun getCurrentPoseType() = this.getDataTracker().get(POSE_TYPE)

    override fun initDataTracker(builder: DataTracker.Builder) {
        super.initDataTracker(builder)
        builder.add(NPC_CLASS, NPCClasses.classes.first().resourceIdentifier)
        builder.add(ASPECTS, emptySet())
        builder.add(POSE_TYPE, PoseType.STAND)
        builder.add(BATTLE_IDS, setOf())
    }

    override fun deserializeBrain(dynamic: Dynamic<*>): Brain<NPCEntity> {
        val brain = createBrainProfile().deserialize(dynamic)
        brain.setTaskList(Activity.CORE, ImmutableList.of(
            Pair.of(0, StayAfloatTask(0.8F)),
            Pair.of(0, GetAngryAtAttackerTask.create()),
            Pair.of(0, ForgetAngryAtTargetTask.create())
        ))
        brain.setTaskList(Activity.IDLE, ImmutableList.of(
            Pair.of(1, RandomTask(
                ImmutableList.of(
                    Pair.of(LookAroundTask(45, 90), 2),
                    Pair.of(LookAtMobTask.create(15F), 2),
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
        brain.resetPossibleActivities()
        return brain
    }

    override fun tryAttack(target: Entity): Boolean {
        target as ServerPlayerEntity
        return target.damage(this.damageSources.mobAttack(this), attributes.getValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat() * 5F)
    }

    override fun onFinishPathfinding() {
//        brain.forget(MemoryModuleType.WALK_TARGET)
    }

    override fun getBrain() = super.getBrain() as Brain<NPCEntity>

    fun updateAspects() {
        dataTracker.set(ASPECTS, appliedAspects)
    }

    fun isInBattle() = battleIds.isNotEmpty()
    fun getBattleConfiguration() = battle ?: npc.battleConfiguration

    override fun tick() {
        super.tick()
        delegate.tick(this)
        schedulingTracker.update(1/20F)
    }

    override fun mobTick() {
        super.mobTick()
        getBrain().tick(world as ServerWorld, this)
    }

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        super.writeNbt(nbt)
        nbt.put(DataKeys.NPC_DATA, MoLangFunctions.writeMoValueToNBT(data))
        nbt.putString(DataKeys.NPC_CLASS, npc.resourceIdentifier.toString())
        nbt.put(DataKeys.NPC_ASPECTS, NbtList().also { list -> appliedAspects.forEach { list.add(NbtString.of(it)) } })
        interaction?.let {
            nbt.putString(DataKeys.NPC_INTERACTION, it.map(Identifier::toString, ExpressionLike::toString))
        }
        val battle = battle
        if (battle != null) {
            val battleNBT = NbtCompound()
            battle.saveToNBT(battleNBT)
            nbt.put(DataKeys.NPC_BATTLE_CONFIGURATION, battleNBT)
        }
        return nbt
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        npc = NPCClasses.getByIdentifier(Identifier.of(nbt.getString(DataKeys.NPC_CLASS))) ?: NPCClasses.classes.first()
        data = MoLangFunctions.readMoValueFromNBT(nbt.getCompound(DataKeys.NPC_DATA)) as VariableStruct
        appliedAspects.addAll(nbt.getList(DataKeys.NPC_ASPECTS, NbtList.STRING_TYPE.toInt()).map { it.asString() })
        nbt.getString(DataKeys.NPC_INTERACTION).takeIf { it.isNotBlank() }?.let {
            if (Identifier.tryParse(it) != null) {
                interaction = Either.left(Identifier.of(it))
            } else {
                interaction = Either.right(it.asExpressionLike())
            }
        }
        val battleNBT = nbt.getCompound(DataKeys.NPC_BATTLE_CONFIGURATION)
        if (!battleNBT.isEmpty) {
            battle = NPCBattleConfiguration().also { it.loadFromNBT(battleNBT) }
        }
        updateAspects()
    }

    override fun getBaseDimensions(pose: EntityPose) = npc.hitbox

    override fun interactMob(player: PlayerEntity, hand: Hand): ActionResult {
        if (player is ServerPlayerEntity && hand == Hand.MAIN_HAND) {
            (interaction ?: npc.interaction)?.runScript(runtime.withPlayerValue(value = player))
            runtime.environment.query.functions.remove("player")
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
        return ActionResult.SUCCESS
    }

    fun playAnimation(vararg animation: String) {
        val packet = PlayPosableAnimationPacket(
            entityId = id,
            animation = animation.toSet(),
            expressions = emptySet()
        )
        packet.sendToPlayers(world.players.filterIsInstance<ServerPlayerEntity>().filter { it.distanceTo(this) < 256 })
    }

    override fun recalling(pokemonEntity: PokemonEntity): CompletableFuture<Unit> {
        playAnimation(RECALL_ANIMATION)
        return delayedFuture(seconds = 1.6F)
    }

    override fun sendingOut(): CompletableFuture<Unit> {
        playAnimation(SEND_OUT_ANIMATION)
        return delayedFuture(seconds = 1.6F)
    }

    override fun onTrackedDataSet(data: TrackedData<*>) {
        super.onTrackedDataSet(data)
    }

    fun edit(player: ServerPlayerEntity) {
        val lastEditing = editingPlayer?.getPlayer()
        if (lastEditing != null) {
            lastEditing.sendPacket(CloseNPCEditorPacket())
        }
        player.sendPacket(OpenNPCEditorPacket(this))
        editingPlayer = player.uuid
    }
}