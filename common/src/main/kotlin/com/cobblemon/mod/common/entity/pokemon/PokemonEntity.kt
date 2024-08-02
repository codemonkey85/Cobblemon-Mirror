/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon

import com.cobblemon.mod.common.*
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.drop.DropTable
import com.cobblemon.mod.common.api.entity.Despawner
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.entity.PokemonEntityLoadEvent
import com.cobblemon.mod.common.api.events.entity.PokemonEntitySaveEvent
import com.cobblemon.mod.common.api.events.entity.PokemonEntitySaveToWorldEvent
import com.cobblemon.mod.common.api.events.pokemon.ShoulderMountEvent
import com.cobblemon.mod.common.api.interaction.PokemonEntityInteraction
import com.cobblemon.mod.common.api.net.serializers.PoseTypeDataSerializer
import com.cobblemon.mod.common.api.net.serializers.StringSetDataSerializer
import com.cobblemon.mod.common.api.pokemon.feature.ChoiceSpeciesFeatureProvider
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatures
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.reactive.ObservableSubscription
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.scheduling.Schedulable
import com.cobblemon.mod.common.api.scheduling.SchedulingTracker
import com.cobblemon.mod.common.api.storage.InvalidSpeciesException
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.battles.BagItems
import com.cobblemon.mod.common.battles.BattleBuilder
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.Poseable
import com.cobblemon.mod.common.entity.generic.GenericBedrockEntity
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.entity.pokemon.ai.PokemonMoveControl
import com.cobblemon.mod.common.entity.pokemon.ai.PokemonNavigation
import com.cobblemon.mod.common.entity.pokemon.ai.goals.*
import com.cobblemon.mod.common.entity.pokemon.effects.EffectTracker
import com.cobblemon.mod.common.entity.pokemon.effects.IllusionEffect
import com.cobblemon.mod.common.net.messages.client.animation.PlayPoseableAnimationPacket
import com.cobblemon.mod.common.net.messages.client.sound.UnvalidatedPlaySoundS2CPacket
import com.cobblemon.mod.common.net.messages.client.spawn.SpawnPokemonPacket
import com.cobblemon.mod.common.net.messages.client.ui.InteractPokemonUIPacket
import com.cobblemon.mod.common.net.serverhandling.storage.SendOutPokemonHandler.SEND_OUT_DURATION
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.activestate.ActivePokemonState
import com.cobblemon.mod.common.pokemon.activestate.InactivePokemonState
import com.cobblemon.mod.common.pokemon.activestate.ShoulderedState
import com.cobblemon.mod.common.pokemon.ai.FormPokemonBehaviour
import com.cobblemon.mod.common.pokemon.evolution.variants.ItemInteractionEvolution
import com.cobblemon.mod.common.pokemon.misc.GimmighoulStashHandler
import com.cobblemon.mod.common.util.*
import com.cobblemon.mod.common.world.gamerules.CobblemonGameRules
import java.util.EnumSet
import java.util.Optional
import java.util.UUID
import java.util.concurrent.CompletableFuture
import net.minecraft.entity.*
import net.minecraft.entity.ai.control.MoveControl
import net.minecraft.entity.ai.goal.EatGrassGoal
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.ai.pathing.PathNodeType
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.passive.TameableShoulderEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.FluidState
import net.minecraft.item.DyeItem
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsage
import net.minecraft.item.Items
import net.minecraft.item.SuspiciousStewItem
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtString
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.FluidTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.text.TextContent
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.EntityView
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent
import java.util.*

@Suppress("unused")
open class PokemonEntity(
    world: World,
    pokemon: Pokemon = Pokemon().apply { isClient = world.isClient },
    type: EntityType<out PokemonEntity> = CobblemonEntities.POKEMON,
) : TameableShoulderEntity(type, world), Poseable, Shearable, Schedulable {
    companion object {
        @JvmStatic val SPECIES = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.STRING)
        @JvmStatic val NICKNAME = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.TEXT_COMPONENT)
        @JvmStatic val NICKNAME_VISIBLE = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
        @JvmStatic val SHOULD_RENDER_NAME = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
        @JvmStatic val MOVING = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
        @JvmStatic val BEHAVIOUR_FLAGS = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.BYTE)
        @JvmStatic val PHASING_TARGET_ID = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
        @JvmStatic val BEAM_MODE = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.BYTE)
        @JvmStatic val BATTLE_ID = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.OPTIONAL_UUID)
        @JvmStatic val ASPECTS = DataTracker.registerData(PokemonEntity::class.java, StringSetDataSerializer)
        @JvmStatic val DYING_EFFECTS_STARTED = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
        @JvmStatic val POSE_TYPE = DataTracker.registerData(PokemonEntity::class.java, PoseTypeDataSerializer)
        @JvmStatic val LABEL_LEVEL = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
        @JvmStatic val HIDE_LABEL = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
        @JvmStatic val UNBATTLEABLE = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
        @JvmStatic val COUNTS_TOWARDS_SPAWN_CAP = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
        @JvmStatic val SPAWN_DIRECTION = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.FLOAT)
        @JvmStatic val FRIENDSHIP = DataTracker.registerData(PokemonEntity::class.java, TrackedDataHandlerRegistry.INTEGER)

        const val BATTLE_LOCK = "battle"

        fun createAttributes(): DefaultAttributeContainer.Builder = LivingEntity.createLivingAttributes()
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE)
            .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK)
    }
    val removalObservable = SimpleObservable<RemovalReason?>()
    /** A list of observable subscriptions related to this entity that need to be cleaned up when the entity is removed. */
    val subscriptions = mutableListOf<ObservableSubscription<*>>()

    override val schedulingTracker = SchedulingTracker()

    val form: FormData
        get() = pokemon.form
    val behaviour: FormPokemonBehaviour
        get() = form.behaviour

    var pokemon: Pokemon = pokemon
        set(value) {
            value.isClient = this.world.isClient
            field = value
            delegate.changePokemon(value)
            stepHeight = behaviour.moving.stepHeight
            // We need to update this value every time the Pokémon changes, other eye height related things will be dynamic.
            this.updateEyeHeight()
        }

    var despawner: Despawner<PokemonEntity> = Cobblemon.bestSpawner.defaultPokemonDespawner

    /** The player that caused this Pokémon to faint. */
    var killer: ServerPlayerEntity? = null

    var evolutionEntity: GenericBedrockEntity? = null

    var ticksLived = 0
    val busyLocks = mutableListOf<Any>()
    val isBusy: Boolean
        get() = busyLocks.isNotEmpty()
    val aspects: Set<String>
        get() = dataTracker.get(ASPECTS)
    var battleId: UUID?
        get() = dataTracker.get(BATTLE_ID).orElse(null)
        set(value) = dataTracker.set(BATTLE_ID, Optional.ofNullable(value))
    val isBattling: Boolean
        get() = dataTracker.get(BATTLE_ID).isPresent
    val friendship: Int
        get() = dataTracker.get(FRIENDSHIP)

    var drops: DropTable? = null

    var tethering: PokemonPastureBlockEntity.Tethering? = null

    var queuedToDespawn = false

    /**
     * The amount of steps this entity has traveled.
     */
    var blocksTraveled: Double = 0.0
    var countsTowardsSpawnCap = true
    /**
     * 0 is do nothing,
     * 1 is appearing from a pokeball so needs to be small then grows,
     * 2 is being captured/recalling so starts large and shrinks.
     */
    var beamMode: Int
        get() = dataTracker.get(BEAM_MODE).toInt()
        set(value) { dataTracker.set(BEAM_MODE, value.toByte()) }

    var phasingTargetId: Int
        get() = dataTracker.get(PHASING_TARGET_ID)
        set(value) { dataTracker.set(PHASING_TARGET_ID, value) }

    // properties like the above are synced and can be subscribed to for changes on either side

    override val delegate = if (world.isClient) {
        PokemonClientDelegate()
    } else {
        PokemonServerDelegate()
    }

    /** The effects that are modifying this entity. */
    var effects: EffectTracker = EffectTracker(this)

    /** The species exposed to the client and used on entity spawn. */
    val exposedSpecies: Species get() = this.effects.mockEffect?.exposedSpecies ?: this.pokemon.species

    /** The form exposed to the client and used for calculating hitbox and height. */
    val exposedForm: FormData get() = this.effects.mockEffect?.exposedForm ?: this.pokemon.form

    init {
        delegate.initialize(this)
        delegate.changePokemon(pokemon)
        calculateDimensions()
    }

    override fun initDataTracker() {
        super.initDataTracker()
        dataTracker.startTracking(SPECIES, "")
        dataTracker.startTracking(NICKNAME, Text.empty())
        dataTracker.startTracking(NICKNAME_VISIBLE, true)
        dataTracker.startTracking(SHOULD_RENDER_NAME, true)
        dataTracker.startTracking(MOVING, false)
        dataTracker.startTracking(BEHAVIOUR_FLAGS, 0)
        dataTracker.startTracking(BEAM_MODE, 0)
        dataTracker.startTracking(PHASING_TARGET_ID, -1)
        dataTracker.startTracking(BATTLE_ID, Optional.empty())
        dataTracker.startTracking(ASPECTS, emptySet())
        dataTracker.startTracking(DYING_EFFECTS_STARTED, false)
        dataTracker.startTracking(POSE_TYPE, PoseType.STAND)
        dataTracker.startTracking(LABEL_LEVEL, 1)
        dataTracker.startTracking(HIDE_LABEL, false)
        dataTracker.startTracking(UNBATTLEABLE, false)
        dataTracker.startTracking(COUNTS_TOWARDS_SPAWN_CAP, true)
        dataTracker.startTracking(SPAWN_DIRECTION, world.random.nextFloat() * 360F)
        dataTracker.startTracking(FRIENDSHIP, 0)
    }

    override fun onTrackedDataSet(data: TrackedData<*>) {
        super.onTrackedDataSet(data)
        // "But it's imposs-" shut up nerd, it happens during super construction and that's before delegate is assigned by class construction
        if (delegate != null) {
            delegate.onTrackedDataSet(data)
        }
        // common datatracker handling
        when (data) {
            SPECIES -> calculateDimensions()
            POSE_TYPE -> {
                val value = dataTracker.get(data) as PoseType
                if (value == PoseType.FLY || value == PoseType.HOVER) {
                    setNoGravity(true)
                } else {
                    setNoGravity(false)
                }
            }
            BATTLE_ID -> {
                if (battleId != null) {
                    busyLocks.remove(BATTLE_LOCK) // Remove in case it's hopped across to another battle, don't want extra battle locks
                    busyLocks.add(BATTLE_LOCK)
                } else {
                    busyLocks.remove(BATTLE_LOCK)
                }
            }
        }
    }

    override fun canWalkOnFluid(state: FluidState): Boolean {
//        val node = navigation.currentPath?.currentNode
//        val targetPos = node?.blockPos
//        if (targetPos == null || world.getBlockState(targetPos.up()).isAir) {
        return if (state.isIn(FluidTags.WATER) && !isSubmergedIn(FluidTags.WATER)) {
            behaviour.moving.swim.canWalkOnWater
        } else if (state.isIn(FluidTags.LAVA) && !isSubmergedIn(FluidTags.LAVA)) {
            behaviour.moving.swim.canWalkOnLava
        } else {
            super.canWalkOnFluid(state)
        }
//        }
//
//        return super.canWalkOnFluid(state)
    }

    override fun handleStatus(status: Byte) {
        delegate.handleStatus(status)
        super.handleStatus(status)
    }

    override fun tick() {
        super.tick()
        // We will be handling idle logic ourselves thank you
        this.setDespawnCounter(0)
        if (queuedToDespawn) {
            return remove(RemovalReason.DISCARDED)
        }
        if (evolutionEntity != null) {
            evolutionEntity!!.setPosition(pokemon.entity!!.x, pokemon.entity!!.y, pokemon.entity!!.z)
            pokemon.entity!!.navigation.stop()
        }
        delegate.tick(this)
        ticksLived++
        if (this.ticksLived % 20 == 0) {
            this.updateEyeHeight()
        }

        if (ticksLived <= 20) {
            clearPositionTarget()
            val spawnDirection = dataTracker.get(SPAWN_DIRECTION)
            setBodyYaw(spawnDirection)
            prevBodyYaw = spawnDirection
        }

        if (this.tethering != null && !this.tethering!!.box.contains(this.x, this.y, this.z)) {
            this.tethering = null
            this.pokemon.recall()
        }
        //This is so that pokemon in the pasture block are ALWAYS in sync with the pokemon box
        //Before, pokemon entities in pastures would hold an old ref to a pokemon obj and changes to that would not appear to the underlying file
        if (this.tethering != null) {
            //Only for online players
            if (world.getPlayerByUuid(ownerUuid) != null){
                this.ownerUuid?.let {
                    val actualPokemon = Cobblemon.storage.getPC(it)[this.pokemon.uuid]
                    actualPokemon?.let {
                        if (it !== pokemon) {
                            pokemon = it
                        }
                    }
                }
            }
        }

        schedulingTracker.update(1/20F)
    }

    fun setMoveControl(moveControl: MoveControl) {
        this.moveControl = moveControl
    }

    /**
     * Prevents water type Pokémon from taking drowning damage.
     */
    override fun canBreatheInWater(): Boolean {
        return behaviour.moving.swim.canBreatheUnderwater
    }

    /**
     * Prevents fire type Pokémon from taking fire damage.
     */
    override fun isFireImmune(): Boolean {
        return pokemon.isFireImmune()
    }

    /**
     * Prevents flying type Pokémon from taking fall damage.
     */
    override fun handleFallDamage(fallDistance: Float, damageMultiplier: Float, damageSource: DamageSource?): Boolean {
        return if (ElementalTypes.FLYING in pokemon.types || pokemon.ability.name == "levitate" || pokemon.species.behaviour.moving.fly.canFly) {
            false
        } else {
            super.handleFallDamage(fallDistance, damageMultiplier, damageSource)
        }
    }

    override fun isInvulnerableTo(damageSource: DamageSource): Boolean {
        // If the entity is busy, it cannot be hurt.
        if (busyLocks.isNotEmpty()) {
            return true
        }

        // Owned Pokémon cannot be hurt by players or suffocation
        if (ownerUuid != null && (damageSource.attacker is PlayerEntity || damageSource.isOf(DamageTypes.IN_WALL))) {
            return true
        }

        if (!Cobblemon.config.playerDamagePokemon && damageSource.attacker is PlayerEntity) {
            return true
        }

        return super.isInvulnerableTo(damageSource)
    }

    /**
     * A utility method that checks if this Pokémon has the [UncatchableProperty.uncatchable] property.
     *
     * @return If the Pokémon is uncatchable.
     */
    fun isUncatchable() = pokemon.isUncatchable()

    fun recallWithAnimation(): CompletableFuture<Pokemon> {
        val owner = owner
        val future = CompletableFuture<Pokemon>()
        if (dataTracker.get(PHASING_TARGET_ID) == -1 && owner != null) {
            owner.getWorld().playSoundServer(pos, CobblemonSounds.POKE_BALL_RECALL, volume = 0.6F)
            dataTracker.set(PHASING_TARGET_ID, owner.id)
            dataTracker.set(BEAM_MODE, 3)
            val state = pokemon.state
            after(seconds = SEND_OUT_DURATION) {
                // only recall if the Pokémon hasn't been recalled yet for this state
                if (state == pokemon.state) {
                    pokemon.recall()
                }
                future.complete(pokemon)
            }
        } else {
            pokemon.recall()
            future.complete(pokemon)
        }

        return future
    }

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        val tethering = this.tethering
        if (tethering != null) {
            val tetheringNbt = NbtCompound()
            tetheringNbt.putUuid(DataKeys.TETHERING_ID, tethering.tetheringId)
            tetheringNbt.putUuid(DataKeys.POKEMON_UUID, tethering.pokemonId)
            tetheringNbt.putUuid(DataKeys.POKEMON_OWNER_ID, tethering.playerId)
            tetheringNbt.putUuid(DataKeys.PC_ID, tethering.pcId)
            tetheringNbt.put(DataKeys.TETHER_MIN_ROAM_POS, NbtHelper.fromBlockPos(tethering.minRoamPos))
            tetheringNbt.put(DataKeys.TETHER_MAX_ROAM_POS, NbtHelper.fromBlockPos(tethering.maxRoamPos))
            nbt.put(DataKeys.TETHERING, tetheringNbt)
        } else {
            nbt.put(DataKeys.POKEMON, pokemon.saveToNBT(NbtCompound()))
        }
        val battleIdToSave = battleId
        if (battleIdToSave != null) {
            nbt.putUuid(DataKeys.POKEMON_BATTLE_ID, battleIdToSave)
        }
        nbt.putString(DataKeys.POKEMON_POSE_TYPE, dataTracker.get(POSE_TYPE).name)
        nbt.putByte(DataKeys.POKEMON_BEHAVIOUR_FLAGS, dataTracker.get(BEHAVIOUR_FLAGS))

        if (dataTracker.get(HIDE_LABEL)) {
            nbt.putBoolean(DataKeys.POKEMON_HIDE_LABEL, true)
        }
        if (dataTracker.get(UNBATTLEABLE)) {
            nbt.putBoolean(DataKeys.POKEMON_UNBATTLEABLE, true)
        }
        if (!countsTowardsSpawnCap) {
            nbt.putBoolean(DataKeys.POKEMON_COUNTS_TOWARDS_SPAWN_CAP, false)
        }

        // save active effects
        nbt.put(DataKeys.ENTITY_EFFECTS, effects.saveToNbt())

        CobblemonEvents.POKEMON_ENTITY_SAVE.post(PokemonEntitySaveEvent(this, nbt))

        return super.writeNbt(nbt)
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        if (nbt.contains(DataKeys.TETHERING)) {
            val tetheringNBT = nbt.getCompound(DataKeys.TETHERING)
            val tetheringId = tetheringNBT.getUuid(DataKeys.TETHERING_ID)
            val pcId = tetheringNBT.getUuid(DataKeys.PC_ID)
            val pokemonId = tetheringNBT.getUuid(DataKeys.POKEMON_UUID)
            val playerId = tetheringNBT.getUuid(DataKeys.POKEMON_OWNER_ID)
            val minRoamPos = NbtHelper.toBlockPos(tetheringNBT.getCompound(DataKeys.TETHER_MIN_ROAM_POS))
            val maxRoamPos = NbtHelper.toBlockPos(tetheringNBT.getCompound(DataKeys.TETHER_MAX_ROAM_POS))

            val loadedPokemon = Cobblemon.storage.getPC(pcId)[pokemonId]
            if (loadedPokemon != null && loadedPokemon.tetheringId == tetheringId) {
                pokemon = loadedPokemon
                tethering = PokemonPastureBlockEntity.Tethering(
                    minRoamPos = minRoamPos,
                    maxRoamPos = maxRoamPos,
                    playerId = playerId,
                    playerName = "",
                    tetheringId = tetheringId,
                    pokemonId = pokemonId,
                    pcId = pcId,
                    entityId = id // Doesn't really matter on the entity
                )
            } else {
                pokemon = this.createSidedPokemon()
                health = 0F
            }
        } else {
            pokemon = try {
                this.createSidedPokemon().loadFromNBT(nbt.getCompound(DataKeys.POKEMON))
            } catch (_: InvalidSpeciesException) {
                health = 0F
                this.createSidedPokemon()
            }
        }

        val savedBattleId = if (nbt.containsUuid(DataKeys.POKEMON_BATTLE_ID)) nbt.getUuid(DataKeys.POKEMON_BATTLE_ID) else null
        if (savedBattleId != null) {
            val battle = BattleRegistry.getBattle(savedBattleId)
            if (battle != null) {
                battleId = savedBattleId
            }
        }

        // apply active effects
        if (nbt.contains(DataKeys.ENTITY_EFFECTS)) effects.loadFromNBT(nbt.getCompound(DataKeys.ENTITY_EFFECTS))

        // init dataTracker
        dataTracker.set(SPECIES, effects.mockEffect?.mock?.species ?: pokemon.species.resourceIdentifier.toString())
        dataTracker.set(NICKNAME, pokemon.nickname ?: Text.empty())
        dataTracker.set(LABEL_LEVEL, pokemon.level)
        dataTracker.set(POSE_TYPE, PoseType.valueOf(nbt.getString(DataKeys.POKEMON_POSE_TYPE)))
        dataTracker.set(BEHAVIOUR_FLAGS, nbt.getByte(DataKeys.POKEMON_BEHAVIOUR_FLAGS))

        if (nbt.contains(DataKeys.POKEMON_HIDE_LABEL)) {
            dataTracker.set(HIDE_LABEL, nbt.getBoolean(DataKeys.POKEMON_HIDE_LABEL))
        }
        if (nbt.contains(DataKeys.POKEMON_UNBATTLEABLE)) {
            dataTracker.set(UNBATTLEABLE, nbt.getBoolean(DataKeys.POKEMON_UNBATTLEABLE))
        }
        if (nbt.contains(DataKeys.POKEMON_COUNTS_TOWARDS_SPAWN_CAP)) {
            countsTowardsSpawnCap = nbt.getBoolean(DataKeys.POKEMON_COUNTS_TOWARDS_SPAWN_CAP)
        }

        CobblemonEvents.POKEMON_ENTITY_LOAD.postThen(
            event = PokemonEntityLoadEvent(this, nbt),
            ifSucceeded = {},
            ifCanceled = { this.discard() }
        )
    }

    override fun createSpawnPacket(): Packet<ClientPlayPacketListener> = CobblemonNetwork.asVanillaClientBound(SpawnPokemonPacket(this, super.createSpawnPacket() as EntitySpawnS2CPacket))

    override fun getPathfindingPenalty(nodeType: PathNodeType): Float {
        return if (nodeType == PathNodeType.OPEN) 2F else super.getPathfindingPenalty(nodeType)
    }

    override fun getNavigation() = navigation as PokemonNavigation
    override fun createNavigation(world: World) = PokemonNavigation(world, this)

    @Suppress("SENSELESS_COMPARISON")
    public override fun initGoals() {
        // DO NOT REMOVE
        // LivingEntity#getActiveEyeHeight is called in the constructor of Entity
        // Pokémon param is not available yet
        if (this.pokemon == null) {
            return
        }
        moveControl = PokemonMoveControl(this)
        goalSelector.clear { true }
        goalSelector.add(0, PokemonInBattleMovementGoal(this, 10))
        goalSelector.add(0, object : Goal() {
            override fun canStart() = this@PokemonEntity.dataTracker.get(PHASING_TARGET_ID) != -1 || pokemon.status?.status == Statuses.SLEEP || dataTracker.get(DYING_EFFECTS_STARTED) || evolutionEntity != null
            override fun shouldContinue(): Boolean {
                if (pokemon.status?.status == Statuses.SLEEP && !canSleep() && !isBusy) {
                    return false
                } else if (pokemon.status?.status == Statuses.SLEEP || isBusy) {
                    return true
                }
                return false
            }
            override fun getControls() = EnumSet.allOf(Control::class.java)
        })

        goalSelector.add(1, PokemonBreatheAirGoal(this))
        goalSelector.add(2, PokemonFloatToSurfaceGoal(this))
        goalSelector.add(3, PokemonFollowOwnerGoal(this, 1.0, 8F, 2F, false))
        goalSelector.add(4, PokemonMoveIntoFluidGoal(this))
        goalSelector.add(5, SleepOnTrainerGoal(this))
        goalSelector.add(5, WildRestGoal(this))

        if (pokemon.getFeature<FlagSpeciesFeature>(DataKeys.HAS_BEEN_SHEARED) != null) {
            goalSelector.add(5, EatGrassGoal(this))
        }

        goalSelector.add(6, PokemonWanderAroundGoal(this))
        goalSelector.add(7, PokemonLookAtEntityGoal(this, ServerPlayerEntity::class.java, 5F))
        goalSelector.add(8, PokemonPointAtSpawnGoal(this))
    }

    fun canSleep(): Boolean {
        val rest = behaviour.resting
        val worldTime = (world.timeOfDay % 24000).toInt()
        val light = world.getLightLevel(blockPos)
        val block = world.getBlockState(blockPos).block
        val biome = world.getBiome(blockPos).value()

        return rest.canSleep &&
                !this.getBehaviourFlag(PokemonBehaviourFlag.EXCITED) &&
                worldTime in this.behaviour.resting.times &&
                light in rest.light &&
                (rest.blocks.isEmpty() || rest.blocks.any { it.fits(block, this.world.registryManager.get(RegistryKeys.BLOCK)) }) &&
                (rest.biomes.isEmpty() || rest.biomes.any { it.fits(biome, this.world.registryManager.get(RegistryKeys.BIOME)) })
    }

    override fun createChild(level: ServerWorld, partner: PassiveEntity) = null

    override fun isReadyToSitOnPlayer(): Boolean {
        return pokemon.form.shoulderMountable
    }

    override fun interactMob(player: PlayerEntity, hand: Hand) : ActionResult {
        val itemStack = player.getStackInHand(hand)
        val colorFeatureType = SpeciesFeatures.getFeaturesFor(pokemon.species).find { it is ChoiceSpeciesFeatureProvider && DataKeys.CAN_BE_COLORED in it.keys }
        val colorFeature = pokemon.getFeature<StringSpeciesFeature>(DataKeys.CAN_BE_COLORED)

        if (ownerUuid == player.uuid || ownerUuid == null) {
            if (itemStack.isOf(Items.SHEARS) && this.isShearable) {
                this.sheared(SoundCategory.PLAYERS)
                this.emitGameEvent(GameEvent.SHEAR, player)
                itemStack.damage(1, player) { it.sendToolBreakStatus(hand) }
                return ActionResult.SUCCESS
            } else if (itemStack.isOf(Items.BUCKET)) {
                if (pokemon.getFeature<FlagSpeciesFeature>(DataKeys.CAN_BE_MILKED) != null) {
                    player.playSound(SoundEvents.ENTITY_GOAT_MILK, 1.0f, 1.0f)
                    val milkBucket = ItemUsage.exchangeStack(itemStack, player, Items.MILK_BUCKET.defaultStack)
                    player.setStackInHand(hand, milkBucket)
                    return ActionResult.success(world.isClient)
                }
            } else if (itemStack.isOf(Items.BOWL)) {
                if (pokemon.aspects.any { it.contains("mooshtank") }) {
                    player.playSound(SoundEvents.ENTITY_MOOSHROOM_MILK, 1.0f, 1.0f)
                    // if the Mooshtank ate a Flower beforehand
                    if (pokemon.lastFlowerFed != ItemStack.EMPTY && pokemon.aspects.any() { it.contains("mooshtank-brown") }) {
                        var effect: StatusEffect? = null
                        var duration = 0

                        if (pokemon.lastFlowerFed.isOf(Items.ALLIUM)) {
                            effect = StatusEffect.byRawId(12) // Fire Resistance
                            duration = 80 // 4 seconds
                        } else if (pokemon.lastFlowerFed.isOf(Items.AZURE_BLUET)) {
                            effect = StatusEffect.byRawId(15) // Blindness
                            duration = 160 // 8 seconds
                        } else if (pokemon.lastFlowerFed.isOf(Items.BLUE_ORCHID) || pokemon.lastFlowerFed.isOf(Items.DANDELION)) {
                            effect = StatusEffect.byRawId(23) // Saturation
                            duration = 7 // .35 seconds
                        } else if (pokemon.lastFlowerFed.isOf(Items.CORNFLOWER)) {
                            effect = StatusEffect.byRawId(8) // Jump Boost
                            duration = 120 // 6 seconds
                        } else if (pokemon.lastFlowerFed.isOf(Items.LILY_OF_THE_VALLEY)) {
                            effect = StatusEffect.byRawId(19) // Poison
                            duration = 240 // 12 seconds
                        } else if (pokemon.lastFlowerFed.isOf(Items.OXEYE_DAISY)) {
                            effect = StatusEffect.byRawId(10) // Regeneration
                            duration = 160 // 8 seconds
                        } else if (pokemon.lastFlowerFed.isOf(Items.POPPY) || pokemon.lastFlowerFed.isOf(Items.TORCHFLOWER)) {
                            effect = StatusEffect.byRawId(16) // Night Vision
                            duration = 100 // 5 seconds
                        } else if (pokemon.lastFlowerFed.isOf(Items.PINK_TULIP) || pokemon.lastFlowerFed.isOf(Items.RED_TULIP) || pokemon.lastFlowerFed.isOf(
                                Items.WHITE_TULIP
                            ) || pokemon.lastFlowerFed.isOf(Items.ORANGE_TULIP)
                        ) {
                            effect = StatusEffect.byRawId(18) // Weakness
                            duration = 180 // 9 seconds
                        } else if (pokemon.lastFlowerFed.isOf(Items.WITHER_ROSE)) {
                            effect = StatusEffect.byRawId(20) // Wither
                            duration = 160 // 8 seconds
                        } else if (pokemon.lastFlowerFed.isOf(CobblemonItems.PEP_UP_FLOWER)) {
                            effect = StatusEffect.byRawId(25) // Levitation
                            duration = 160 // 8 seconds
                        }


                        // modify the suspicious stew with the effect
                        val susStewStack = Items.SUSPICIOUS_STEW.defaultStack
                        SuspiciousStewItem.addEffectToStew(susStewStack, effect, duration)
                        val susStewEffect = ItemUsage.exchangeStack(itemStack, player, susStewStack)
                        //give player modified Suspicious Stew
                        player.setStackInHand(hand, susStewEffect)
                        // reset the flower fed state
                        pokemon.lastFlowerFed = ItemStack.EMPTY
                        return ActionResult.success(world.isClient)
                    } else {
                        val mushroomStew = ItemUsage.exchangeStack(itemStack, player, Items.MUSHROOM_STEW.defaultStack)
                        player.setStackInHand(hand, mushroomStew)
                        return ActionResult.success(world.isClient)
                    }

                }
            }
            // Flowers used on brown MooshTanks
            else if (itemStack.isOf(Items.ALLIUM) ||
                itemStack.isOf(Items.AZURE_BLUET) ||
                itemStack.isOf(Items.BLUE_ORCHID) ||
                itemStack.isOf(Items.DANDELION) ||
                itemStack.isOf(Items.CORNFLOWER) ||
                itemStack.isOf(Items.LILY_OF_THE_VALLEY) ||
                itemStack.isOf(Items.OXEYE_DAISY) ||
                itemStack.isOf(Items.POPPY) ||
                itemStack.isOf(Items.TORCHFLOWER) ||
                itemStack.isOf(Items.PINK_TULIP) ||
                itemStack.isOf(Items.RED_TULIP) ||
                itemStack.isOf(Items.WHITE_TULIP) ||
                itemStack.isOf(Items.ORANGE_TULIP) ||
                itemStack.isOf(Items.WITHER_ROSE) ||
                itemStack.isOf(CobblemonItems.PEP_UP_FLOWER)
            ) {
                if (pokemon.aspects.any { it.contains("mooshtank") }) {
                    player.playSound(SoundEvents.ENTITY_MOOSHROOM_EAT, 1.0f, 1.0f)
                    pokemon.lastFlowerFed = itemStack
                    return ActionResult.success(world.isClient)
                }
            } else if (!player.isSneaking && player.uuid == ownerUuid && (itemStack.isOf(CobblemonItems.RELIC_COIN)
                        || itemStack.isOf(CobblemonItems.RELIC_COIN_POUCH)
                        || itemStack.isOf(CobblemonItems.RELIC_COIN_SACK)
                        || itemStack.isOf(Items.NETHERITE_SCRAP)
                        || itemStack.isOf(Items.NETHERITE_INGOT)
                        || itemStack.isOf(Items.NETHERITE_BLOCK))
            ) {
                if (GimmighoulStashHandler.interactMob(player, hand, pokemon)) {
                    return ActionResult.SUCCESS
                }
            } else if (itemStack.item is DyeItem && colorFeatureType != null) {
                val currentColor = colorFeature?.value ?: ""
                val item = itemStack.item as DyeItem
                if (!item.color.name.equals(currentColor, ignoreCase = true)) {
                    if (player is ServerPlayerEntity) {
                        if (colorFeature != null) {
                            colorFeature.value = item.color.name.lowercase()
                            this.pokemon.markFeatureDirty(colorFeature)
                        } else {
                            val newColorFeature =
                                StringSpeciesFeature(DataKeys.CAN_BE_COLORED, item.color.name.lowercase())
                            this.pokemon.features.add(newColorFeature)
                            this.pokemon.anyChangeObservable.emit(pokemon)
                        }

                        this.pokemon.updateAspects()
                        if (!player.isCreative) {
                            itemStack.decrement(1)
                        }
                    }
                    return ActionResult.success(world.isClient)
                }
            } else if (itemStack.item.equals(Items.WATER_BUCKET) && colorFeatureType != null) {
                if (player is ServerPlayerEntity) {
                    if (colorFeature != null) {
                        if (!player.isCreative) {
                            itemStack.decrement(1)
                            player.giveOrDropItemStack(Items.BUCKET.defaultStack)
                        }
                        colorFeature.value = ""
                        this.pokemon.markFeatureDirty(colorFeature)
                        this.pokemon.updateAspects()
                    }
                }
                return ActionResult.success(world.isClient)
            }
        }

        if (hand == Hand.MAIN_HAND && player is ServerPlayerEntity && pokemon.getOwnerPlayer() == player) {
            if (player.isSneaking) {
                InteractPokemonUIPacket(this.getUuid(), isReadyToSitOnPlayer && pokemon in player.party()).sendToPlayer(player)
            } else {
                // TODO #105
                if (this.attemptItemInteraction(player, player.getStackInHand(hand))) return ActionResult.SUCCESS
            }
        }

        return super.interactMob(player, hand)
    }

    override fun getDimensions(pose: EntityPose): EntityDimensions {
        val scale = effects.mockEffect?.scale ?: (form.baseScale * pokemon.scaleModifier)
        return this.exposedForm.hitbox.scaled(scale)
    }

    override fun canTakeDamage() = super.canTakeDamage() && !isBusy
    override fun damage(source: DamageSource?, amount: Float): Boolean {
        return if (super.damage(source, amount)) {
            effects.mockEffect?.takeIf { it is IllusionEffect && this.battleId == null }?.end(this)
            if (this.health == 0F) {
                pokemon.currentHealth = 0
            } else {
//                pokemon.currentHealth = this.health.toInt()
            }
            true
        } else false
    }

    override fun shouldSave(): Boolean {
        if (ownerUuid == null && (Cobblemon.config.savePokemonToWorld || isPersistent)) {
            CobblemonEvents.POKEMON_ENTITY_SAVE_TO_WORLD.postThen(PokemonEntitySaveToWorldEvent(this)) {
                return true
            }
        }
        return tethering != null
    }

    override fun checkDespawn() {
        if (pokemon.getOwnerUUID() == null && !isPersistent && despawner.shouldDespawn(this)) {
            discard()
        }
    }

    override fun getEyeHeight(pose: EntityPose): Float = this.exposedForm.eyeHeight(this)

    @Suppress("SENSELESS_COMPARISON")
    override fun getActiveEyeHeight(pose: EntityPose, dimensions: EntityDimensions): Float {
        // DO NOT REMOVE
        // LivingEntity#getActiveEyeHeight is called in the constructor of Entity
        // Pokémon param is not available yet
        if (this.pokemon == null) {
            return super.getActiveEyeHeight(pose, dimensions)
        }
        return this.exposedForm.eyeHeight(this)
    }

    fun setBehaviourFlag(flag: PokemonBehaviourFlag, on: Boolean) {
        dataTracker.set(BEHAVIOUR_FLAGS, setBitForByte(dataTracker.get(BEHAVIOUR_FLAGS), flag.bit, on))
    }

    fun getBehaviourFlag(flag: PokemonBehaviourFlag): Boolean = getBitForByte(dataTracker.get(BEHAVIOUR_FLAGS), flag.bit)

    @Suppress("UNUSED_PARAMETER")
    fun canBattle(player: PlayerEntity): Boolean {
        if (dataTracker.get(UNBATTLEABLE)) {
            return false
        } else if (isBusy) {
            return false
        } else if (battleId != null) {
            return false
        } else if (ownerUuid != null) {
            return false
        } else if (health <= 0F || isDead) {
            return false
        }

        return true
    }

    /**
     * The level this entity should display.
     *
     * @return The level that should be displayed, if equal or lesser than 0 the level is not intended to be displayed.
     */
    fun labelLevel() = dataTracker.get(LABEL_LEVEL)

    override fun playAmbientSound() {
        if (!this.isSilent || this.busyLocks.filterIsInstance<EmptyPokeBallEntity>().isEmpty()) {
            val sound = Identifier(this.pokemon.species.resourceIdentifier.namespace, "pokemon.${this.pokemon.showdownId()}.ambient")
            // ToDo distance to travel is currently hardcoded to default we can maybe find a way to work around this down the line
            UnvalidatedPlaySoundS2CPacket(sound, this.soundCategory, this.x, this.y, this.z, this.soundVolume, this.soundPitch)
                .sendToPlayersAround(this.x, this.y, this.z, 16.0, this.world.registryKey)
        }
    }

    // We never want to allow an actual sound event here, we do not register our sounds to the sound registry as species are loaded by the time the registry is frozen.
    // Super call would do the same but might as well future-proof.
    override fun getAmbientSound() = null
    override fun getMinAmbientSoundDelay() = Cobblemon.config.ambientPokemonCryTicks

    private fun attemptItemInteraction(player: PlayerEntity, stack: ItemStack): Boolean {
        if (stack.isEmpty) {
            return false
        }

        if (player is ServerPlayerEntity && isBattling) {
            val battle = battleId?.let(BattleRegistry::getBattle) ?: return false

            val bagItemConvertible = BagItems.getConvertibleForStack(stack) ?: return false

            val battlePokemon = battle.actors.flatMap { it.pokemonList }.find { it.effectedPokemon.uuid == pokemon.uuid } ?: return false // Shouldn't be possible but anyway
            if (battlePokemon.actor.getSide().actors.none { it.isForPlayer(player)}) {
                return true
            }

            return bagItemConvertible.handleInteraction(player, battlePokemon, stack)
        }
        if (player !is ServerPlayerEntity || this.isBusy) {
            return false
        }

        // Check evolution item interaction
        if (pokemon.getOwnerPlayer() == player) {
            val context = ItemInteractionEvolution.ItemInteractionContext(stack, player.world)
            pokemon.lockedEvolutions
                .filterIsInstance<ItemInteractionEvolution>()
                .forEach { evolution ->
                    if (evolution.attemptEvolution(pokemon, context)) {
                        if (!player.isCreative) {
                            stack.decrement(1)
                        }
                        this.world.playSoundServer(position = this.pos, sound = CobblemonSounds.ITEM_USE, volume = 1F, pitch = 1F)
                        return true
                    }
                }
        }

        (stack.item as? PokemonEntityInteraction)?.let {
            if (it.onInteraction(player, this, stack)) {
                it.sound?.let {
                    this.world.playSoundServer(
                        position = this.pos,
                        sound = it,
                        volume = 1F,
                        pitch = 1F
                    )
                }
                return true
            }
        }
        return false
    }

    fun offerHeldItem(player: PlayerEntity, stack: ItemStack): Boolean {
        if (player !is ServerPlayerEntity || this.isBusy || this.pokemon.getOwnerPlayer() != player) {
            return false
        }
        // We want the count of 1 in order to match the ItemStack#areEqual
        val giving = stack.copy().apply { count = 1 }
        val possibleReturn = this.pokemon.heldItemNoCopy()
        if (stack.isEmpty && possibleReturn.isEmpty) {
            return false
        }
        if (ItemStack.areEqual(giving, possibleReturn)) {
            player.sendMessage(lang("held_item.already_holding", this.pokemon.getDisplayName(), stack.name))
            return true
        }
        val returned = this.pokemon.swapHeldItem(stack = stack, decrement = !player.isCreative)
        val text = when {
            giving.isEmpty -> lang("held_item.take", returned.name, this.pokemon.getDisplayName())
            returned.isEmpty -> lang("held_item.give", this.pokemon.getDisplayName(), giving.name)
            else -> lang("held_item.replace", returned.name, this.pokemon.getDisplayName(), giving.name)
        }
        player.giveOrDropItemStack(returned)
        player.sendMessage(text)
        this.world.playSoundServer(position = this.pos, sound = SoundEvents.ENTITY_ITEM_PICKUP, volume = 0.6F, pitch = 1.4F)
        return true
    }

    fun tryMountingShoulder(player: ServerPlayerEntity): Boolean {
        if (this.pokemon.belongsTo(player) && this.hasRoomToMount(player)) {
            CobblemonEvents.SHOULDER_MOUNT.postThen(ShoulderMountEvent(player, pokemon, isLeft = player.shoulderEntityLeft.isEmpty)) {
                val dirToPlayer = player.eyePos.subtract(pos).multiply(1.0, 0.0, 1.0).normalize()
                velocity = dirToPlayer.multiply(0.8).add(0.0, 0.5, 0.0)
                val lock = Any()
                busyLocks.add(lock)
                after(seconds = 0.5F) {
                    busyLocks.remove(lock)
                    if (!isBusy && isAlive) {
                        val isLeft = player.shoulderEntityLeft.isEmpty
                        if (isLeft || player.shoulderEntityRight.isEmpty) {
                            pokemon.state = ShoulderedState(player.uuid, isLeft, pokemon.uuid)
                            this.mountOnto(player)
                            this.pokemon.form.shoulderEffects.forEach { it.applyEffect(this.pokemon, player, isLeft) }
                            this.world.playSoundServer(position = this.pos, sound = SoundEvents.ENTITY_ITEM_PICKUP, volume = 0.7F, pitch = 1.4F)
                            discard()
                        }
                    }
                }
                return true
            }
        }
        return false
    }

    override fun mountOnto(player: ServerPlayerEntity): Boolean {
        if (!super.mountOnto(player)) {
            return false
        }
        val nbt = when {
            player.shoulderEntityRight.isPokemonEntity() && player.shoulderEntityRight.getCompound(DataKeys.POKEMON).getUuid(DataKeys.POKEMON_UUID) == this.pokemon.uuid -> player.shoulderEntityRight
            player.shoulderEntityLeft.isPokemonEntity() && player.shoulderEntityLeft.getCompound(DataKeys.POKEMON).getUuid(DataKeys.POKEMON_UUID) == this.pokemon.uuid -> player.shoulderEntityLeft
            else -> return true
        }
        nbt.putUuid(DataKeys.SHOULDER_UUID, this.pokemon.uuid)
        nbt.putString(DataKeys.SHOULDER_SPECIES, this.pokemon.species.resourceIdentifier.toString())
        nbt.putString(DataKeys.SHOULDER_FORM, this.pokemon.form.name)
        nbt.put(DataKeys.SHOULDER_ASPECTS, this.pokemon.aspects.map(NbtString::of).toNbtList())
        nbt.putFloat(DataKeys.SHOULDER_SCALE_MODIFIER, this.pokemon.scaleModifier)
        return true
    }

    override fun remove(reason: RemovalReason) {
        val stateEntity = (pokemon.state as? ActivePokemonState)?.entity
        super.remove(reason)
        if (stateEntity == this) {
            pokemon.state = InactivePokemonState()
        }
        subscriptions.forEach(ObservableSubscription<*>::unsubscribe)
        removalObservable.emit(reason)

        if (reason.shouldDestroy() && pokemon.tetheringId != null) {
            pokemon.tetheringId = null
        }
        if (evolutionEntity != null) {
            evolutionEntity!!.kill()
            pokemon.entity?.evolutionEntity = null
        }
    }

    // Copy and paste of how vanilla checks it, unfortunately no util method you can only add then wait for the result
    fun hasRoomToMount(player: PlayerEntity): Boolean {
        return (player.shoulderEntityLeft.isEmpty || player.shoulderEntityRight.isEmpty)
                && !player.hasVehicle()
                && player.isOnGround
                && !player.isTouchingWater
                && !player.inPowderSnow
    }

    fun cry() {
        if(this.isSilent) return
        val pkt = PlayPoseableAnimationPacket(id, setOf("cry"), emptySet())
        world.getEntitiesByClass(ServerPlayerEntity::class.java, Box.of(pos, 64.0, 64.0, 64.0), { true }).forEach {
            it.sendPacket(pkt)
        }
    }

    override fun drop(source: DamageSource?) {
        if (pokemon.isWild()) {
            super.drop(source)
            delegate.drop(source)
        }
    }

    override fun dropXp() {
        // Copied over the entire function because it's the simplest way to switch out the gamerule check
        if (
            world is ServerWorld && !this.isExperienceDroppingDisabled &&
            (shouldAlwaysDropXp() || playerHitTimer > 0 && shouldDropXp() && world.gameRules.getBoolean(CobblemonGameRules.DO_POKEMON_LOOT))
        ) {
            ExperienceOrbEntity.spawn(world as ServerWorld, pos, this.xpToDrop)
        }
    }

    override fun updatePostDeath() {
        // Do not invoke super we need to keep a tight lid on this due to the Thorium mod forcing the ticks to a max of 20 on server side if we invoke a field update here
        // Client delegate is mimicking expected behavior on client end.
        delegate.updatePostDeath()
    }

    override fun onEatingGrass() {
        super.onEatingGrass()

        val feature = pokemon.getFeature<FlagSpeciesFeature>(DataKeys.HAS_BEEN_SHEARED)
        if (feature != null) {
            feature.enabled = false
            pokemon.markFeatureDirty(feature)
            pokemon.updateAspects()
        }
    }

    override fun travel(movementInput: Vec3d) {
        val prevBlockPos = this.blockPos
        super.travel(movementInput)
        this.updateBlocksTraveled(prevBlockPos)
    }

    private fun updateBlocksTraveled(fromBp: BlockPos) {
        // Riding or falling shouldn't count, other movement sources are fine
        if (this.hasVehicle() || this.isFalling()) {
            return
        }
        val blocksTaken = this.blockPos.getSquaredDistance(fromBp)
        if (blocksTaken > 0) this.blocksTraveled += blocksTaken
    }

    private fun updateEyeHeight() {
        @Suppress("CAST_NEVER_SUCCEEDS")
        (this as com.cobblemon.mod.common.mixin.accessor.AccessorEntity).standingEyeHeight(this.getActiveEyeHeight(EntityPose.STANDING, this.type.dimensions))
    }

    fun isFlying() = this.getBehaviourFlag(PokemonBehaviourFlag.FLYING)
    fun couldStopFlying() = isFlying() && !behaviour.moving.walk.avoidsLand && behaviour.moving.walk.canWalk
    fun isFalling() = this.fallDistance > 0 && this.world.getBlockState(this.blockPos.down()).isAir && !this.isFlying()
    fun getIsSubmerged() = isInLava || isSubmergedInWater
    override fun getCurrentPoseType(): PoseType = this.dataTracker.get(POSE_TYPE)

    /**
     * Returns the [Species.translatedName] of the backing [pokemon].
     *
     * @return The [Species.translatedName] of the backing [pokemon].
     */
    override fun getDefaultName(): Text = this.pokemon.species.translatedName

    /**
     * If this Pokémon has a nickname, then the nickname is returned.
     * Otherwise, [getDefaultName] is returned
     *
     * @return The current display name of this entity.
     */
    override fun getName(): Text {
        if (!dataTracker.get(NICKNAME_VISIBLE)) return defaultName
        return dataTracker.get(NICKNAME)?.takeIf { it.content != TextContent.EMPTY } ?: pokemon.getDisplayName()
    }

    /**
     * Returns the custom name of this entity, in the context of Cobblemon it is the [Pokemon.nickname].
     *
     * @return The nickname of the backing [pokemon].
     */
    override fun getCustomName(): Text? = pokemon.nickname

    /**
     * Sets the custom name of this entity.
     * In the context of a Pokémon entity this affects the [Pokemon.nickname].
     *
     * @param name The new name being set, if null the [Pokemon.nickname] is removed.
     */
    override fun setCustomName(name: Text?) {
        // We do this as a compromise to keep as much compatibility as possible with other mods expecting this entity to act like a vanilla one
        this.pokemon.nickname = Text.literal(name?.string)
    }

    /**
     * Checks if the backing [pokemon] has a non-null [Pokemon.nickname].
     *
     * @return If the backing [pokemon] has a non-null [Pokemon.nickname].
     */
    override fun hasCustomName(): Boolean = pokemon.nickname != null && pokemon.nickname?.content != TextContent.EMPTY

    /**
     * This method toggles the visibility of the entity name,
     * Unlike the vanilla implementation in our context it changes between displaying the species name or nickname of the Pokémon.
     *
     * @param visible The state of custom name visibility.
     */
    override fun setCustomNameVisible(visible: Boolean) {
        // We do this as a compromise to keep as much compatibility as possible with other mods expecting this entity to act like a vanilla one
        dataTracker.set(NICKNAME_VISIBLE, visible)
    }

    /**
     * Attempts to force initiate a battle with this Pokémon.
     *
     * @param player The player to attempt a battle with.
     * @return Whether the battle was successfully started.
     */
    fun forceBattle(player: ServerPlayerEntity): Boolean {
        if (!canBattle(player)) {
            return false
        }
        var isSuccessful = false
        BattleBuilder.pve(player, this).ifSuccessful { isSuccessful = true }
        return isSuccessful
    }

    /**
     * In the context of a Pokémon entity this checks if the Pokémon is currently set to displaying its nickname.
     *
     * @return If the custom name of this entity should display, in this case the [getCustomName] is the nickname but if null the [getDefaultName] will be used.
     */
    override fun isCustomNameVisible(): Boolean = dataTracker.get(NICKNAME_VISIBLE)

    /**
     * Returns whether the entity is currently set to having its name displayed.
     *
     * @return If this entity should render the name label.
     */
    override fun shouldRenderName(): Boolean = dataTracker.get(SHOULD_RENDER_NAME)

    /**
     * Sets the entity to having its name hidden.
     */
    fun hideNameRendering() { dataTracker.set(SHOULD_RENDER_NAME, false) }

    override fun isBreedingItem(stack: ItemStack): Boolean = false

    override fun canBreedWith(other: AnimalEntity): Boolean = false

    override fun breed(world: ServerWorld, other: AnimalEntity) {}

    override fun method_48926(): EntityView {
        return this.world
    }

    override fun sheared(shearedSoundCategory: SoundCategory) {
        this.world.playSoundFromEntity(null, this,SoundEvents.ENTITY_SHEEP_SHEAR, shearedSoundCategory, 1.0F, 1.0F)
        val feature = this.pokemon.getFeature<FlagSpeciesFeature>(DataKeys.HAS_BEEN_SHEARED) ?: return
        feature.enabled = true
        this.pokemon.markFeatureDirty(feature)
        this.pokemon.updateAspects()
        val i = this.random.nextInt(3) + 1
        for (j in 0 .. i) {
            val color = this.pokemon.getFeature<StringSpeciesFeature>(DataKeys.CAN_BE_COLORED)?.value ?: "white"
            val woolItem = when (color) {
                "black" -> Items.BLACK_WOOL
                "blue" -> Items.BLUE_WOOL
                "brown" -> Items.BROWN_WOOL
                "cyan" -> Items.CYAN_WOOL
                "gray" -> Items.GRAY_WOOL
                "green" -> Items.GREEN_WOOL
                "light_blue" -> Items.LIGHT_BLUE_WOOL
                "light_gray" -> Items.LIGHT_GRAY_WOOL
                "lime" -> Items.LIME_WOOL
                "magenta" -> Items.MAGENTA_WOOL
                "orange" -> Items.ORANGE_WOOL
                "purple" -> Items.PURPLE_WOOL
                "red" -> Items.RED_WOOL
                "yellow" -> Items.YELLOW_WOOL
                "pink" -> Items.PINK_WOOL
                else -> Items.WHITE_WOOL
            }
            val itemEntity =  this.dropItem(woolItem, 1) ?: return
            itemEntity.velocity = itemEntity.velocity.add(
                ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f).toDouble(),
                (this.random.nextFloat() * 0.05f).toDouble(),
                ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f).toDouble()
            )
        }
    }

    override fun isShearable(): Boolean {
        val feature = this.pokemon.getFeature<FlagSpeciesFeature>(DataKeys.HAS_BEEN_SHEARED) ?: return false
        return !this.isBusy && !this.pokemon.isFainted() && !feature.enabled
    }

    override fun canUsePortals() = false

    override fun setAir(air: Int) {
        if (this.isBattling) {
            this.dataTracker.set(AIR, 300)
            return
        }
        super.setAir(air)
    }

    override fun onStoppedTrackingBy(player: ServerPlayerEntity?) {
        if (player == null) {
            return
        }

        if (this.ownerUuid == player.uuid && tethering == null) {
            queuedToDespawn = true
            return
        }
//
//            val chunkPos = ChunkPos(BlockPos(x.toInt(), y.toInt(), z.toInt()))
//            (world as ServerWorld).chunkManager
//                .addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 0, id)
//            this.goalSelector.tick()
//            if(distanceTo(player.blockPos) > 100) pokemon.recall()
//        }
    }

    override fun canBeLeashedBy(player: PlayerEntity): Boolean {
        return this.ownerUuid == null || this.ownerUuid == player.uuid
    }

    /** Retrieves the battle theme associated with this Pokemon's Species/Form, or the default PVW theme if not found. */
    fun getBattleTheme() = Registries.SOUND_EVENT.get(this.form.battleTheme) ?: CobblemonSounds.PVW_BATTLE

    /**
     * A utility method to instance a [Pokemon] aware if the [world] is client sided or not.
     *
     * @return The side safe [Pokemon] with the [Pokemon.isClient] set.
     */
    private fun createSidedPokemon(): Pokemon = Pokemon().apply { isClient = this@PokemonEntity.world.isClient }
}
