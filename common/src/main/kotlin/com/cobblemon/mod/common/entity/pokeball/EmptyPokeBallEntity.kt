/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokeball

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonEntities.EMPTY_POKEBALL
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokeball.PokeBallCaptureCalculatedEvent
import com.cobblemon.mod.common.api.events.pokeball.ThrownPokeballHitEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.api.net.serializers.StringSetDataSerializer
import com.cobblemon.mod.common.api.net.serializers.Vec3DataSerializer
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.scheduling.Schedulable
import com.cobblemon.mod.common.api.scheduling.ScheduledTask
import com.cobblemon.mod.common.api.scheduling.SchedulingTracker
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.BattleCaptureAction
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.BattleTypes
import com.cobblemon.mod.common.battles.ForcePassActionResponse
import com.cobblemon.mod.common.client.entity.EmptyPokeBallClientDelegate
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.Poseable
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonServerDelegate
import com.cobblemon.mod.common.net.messages.client.animation.PlayPoseableAnimationPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleCaptureStartPacket
import com.cobblemon.mod.common.net.messages.client.spawn.SpawnPokeballPacket
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty
import com.cobblemon.mod.common.util.*
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityPose
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.MathHelper.PI
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.concurrent.CompletableFuture

class EmptyPokeBallEntity : ThrownItemEntity, Poseable, WaterDragModifier, Schedulable {
    enum class CaptureState {
        NOT,
        HIT,
        FALL,
        SHAKE,
        CAPTURED,
        CAPTURED_CRITICAL,
        BROKEN_FREE
    }

    companion object {
        val CAPTURE_STATE = DataTracker.registerData(EmptyPokeBallEntity::class.java, TrackedDataHandlerRegistry.BYTE)
        val HIT_TARGET_POSITION = DataTracker.registerData(EmptyPokeBallEntity::class.java, Vec3DataSerializer)
        val HIT_VELOCITY = DataTracker.registerData(EmptyPokeBallEntity::class.java, Vec3DataSerializer)
        val SHAKE = DataTracker.registerData(EmptyPokeBallEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
        val ASPECTS = DataTracker.registerData(EmptyPokeBallEntity::class.java, StringSetDataSerializer)

        const val SECONDS_BETWEEN_SHAKES = 1.25F
        const val SECONDS_BEFORE_SHAKE = 1F

        val DIMENSIONS = EntityDimensions(0.4F, 0.4F, true)
    }

    val dataTrackerEmitter = SimpleObservable<TrackedData<*>>()

    override val schedulingTracker = SchedulingTracker()
    var capturingPokemon: PokemonEntity? = null
    val captureFuture = CompletableFuture<Boolean>()
    var captureState: CaptureState
        get() = CaptureState.values()[dataTracker.get(CAPTURE_STATE).toInt()]
        set(value) { dataTracker.set(CAPTURE_STATE, value.ordinal.toByte()) }
    var aspects: Set<String>
        get() = dataTracker.get(ASPECTS)
        set(value) { dataTracker.set(ASPECTS, value) }

    override val delegate = if (world.isClient) {
        EmptyPokeBallClientDelegate()
    } else {
        EmptyPokeBallServerDelegate()
    }

    override fun initDataTracker() {
        super.initDataTracker()
        dataTracker.startTracking(CAPTURE_STATE, CaptureState.NOT.ordinal.toByte())
        dataTracker.startTracking(ASPECTS, emptySet())
        dataTracker.startTracking(HIT_TARGET_POSITION, Vec3d.ZERO)
        dataTracker.startTracking(HIT_VELOCITY, Vec3d.ZERO)
        dataTracker.startTracking(SHAKE, false)
    }

    override fun onTrackedDataSet(data: TrackedData<*>) {
        super.onTrackedDataSet(data)
        // See what I said about this in PokemonEntity
        if (delegate != null) {
            delegate.onTrackedDataSet(data)
        }
        if (data == CAPTURE_STATE) {
            val newState = dataTracker.get(CAPTURE_STATE)
            when (CaptureState.values()[newState.toInt()]) {
                CaptureState.NOT -> setNoGravity(false)
                CaptureState.HIT -> {}
                CaptureState.FALL -> setNoGravity(false)
                CaptureState.SHAKE -> setNoGravity(true)
                CaptureState.CAPTURED, CaptureState.CAPTURED_CRITICAL -> {}
                CaptureState.BROKEN_FREE -> {}
            }
        }
        dataTrackerEmitter.emit(data)
    }

    init {
        delegate.initialize(this)
    }

    constructor(world: World) : this(pokeBall = PokeBalls.POKE_BALL, world = world)
    constructor(
        pokeBall: PokeBall,
        world: World,
        entityType: EntityType<out EmptyPokeBallEntity> = EMPTY_POKEBALL
    ): super(entityType, world) {
        this.pokeBall = pokeBall
    }

    constructor(
        pokeBall: PokeBall,
        world: World,
        ownerEntity: LivingEntity,
        entityType: EntityType<out EmptyPokeBallEntity> = EMPTY_POKEBALL
    ): super(entityType, ownerEntity, world) {
        this.pokeBall = pokeBall
    }

    var pokeBall: PokeBall = PokeBalls.POKE_BALL

    override fun onBlockHit(hitResult: BlockHitResult) {
        if (captureState == CaptureState.NOT) {
            if (world.isServerSide()) {
                super.onBlockHit(hitResult)
                world.sendParticlesServer(ParticleTypes.CLOUD, hitResult.pos, 2, hitResult.pos.subtract(pos).normalize().multiply(-0.1), 0.0)
                world.playSoundServer(pos, SoundEvents.BLOCK_WOOD_PLACE, pitch = 2.5F)
                discard()
                val player = this.owner as? ServerPlayerEntity
                if (player?.isCreative == false) {
                    dropItem(defaultItem)
                }
            }
        } else {
            setNoGravity(false)
            velocity = Vec3d.ZERO

        }
    }

    override fun onEntityHit(hitResult: EntityHitResult) {
        if (captureState == CaptureState.NOT) {
            if (hitResult.entity is PokemonEntity && world.isServerSide()) {
                val pokemonEntity = hitResult.entity as PokemonEntity

                val battle = (pokemonEntity.delegate as PokemonServerDelegate).getBattle()
                val owner = owner

                if (!pokemonEntity.pokemon.isWild()) {
                    owner?.sendMessage(lang("capture.not_wild", pokemonEntity.exposedSpecies.translatedName).red())
                    return drop()
                }

                if (!UncatchableProperty.isCatchable(pokemonEntity)) {
                    owner?.sendMessage(lang("capture.cannot_be_caught").red())
                    return drop()
                }

                if (battle != null && owner != null && owner is LivingEntity) {
                    val throwerActor = battle.getActor(owner.uuid)
                    val hitActor = battle.actors.find { it.isForPokemon(pokemonEntity) }
                    val hitBattlePokemon = hitActor?.activePokemon?.find { it.battlePokemon?.effectedPokemon?.entity == pokemonEntity }

                    if (throwerActor == null) {
                        owner.sendMessage(lang("capture.in_battle", pokemonEntity.exposedSpecies.translatedName).red())
                        return drop()
                    }

                    if (hitActor == null || hitBattlePokemon == null) {
                        return drop() // Weird, shouldn't be possible
                    }

                    if (battle.format.battleType != BattleTypes.SINGLES || hitActor.pokemonList.count { it.health > 0 } > 1) {
                        owner.sendMessage(lang("capture.not_single").red())
                        return drop()
                    }

                    val canFitForcedAction = throwerActor.canFitForcedAction()
                    if (!canFitForcedAction) {
                        owner.sendMessage(lang("capture.not_your_turn").red())
                        return drop()
                    }

                    battle.captureActions.add(BattleCaptureAction(battle, hitBattlePokemon, this).also { it.attach() })

                    battle.broadcastChatMessage(
                        lang(
                            "capture.attempted_capture",
                            throwerActor.getName(),
                            pokeBall.item().name,
                            pokemonEntity.exposedSpecies.translatedName
                        ).yellow()
                    )
                    battle.sendUpdate(BattleCaptureStartPacket(pokeBall.name, aspects, hitBattlePokemon.getPNX()))
                    throwerActor.forceChoose(ForcePassActionResponse())
                } else if (pokemonEntity.isBusy) {
                    owner?.sendMessage(lang("capture.busy", pokemonEntity.exposedSpecies.translatedName).red())
                    return drop()
                } else if (owner is ServerPlayerEntity && BattleRegistry.getBattleByParticipatingPlayer(owner) != null) {
                    owner.sendMessage(lang("you_in_battle").red())
                    return drop()
                }


                capturingPokemon = pokemonEntity
                dataTracker.set(HIT_VELOCITY, velocity.normalize())
                dataTracker.set(HIT_TARGET_POSITION, hitResult.pos)
                attemptCatch(pokemonEntity)
                return
            }
        }
        super.onEntityHit(hitResult)
    }

    private fun drop() {
        val owner = owner
        discard()
        val player = owner?.takeIf { it is ServerPlayerEntity } as? ServerPlayerEntity
        if (player?.isCreative != true) dropItem(defaultItem)
        return
    }

    // Poké Balls don't save to the world.
    override fun shouldSave() = false

    override fun tick() {
        super.tick()
        delegate.tick(this)

        if (world.isServerSide()) {
            capturingPokemon?.let {
                if (!it.isInvisible) {
                    dataTracker.set(HIT_TARGET_POSITION, it.pos)
                }
                CobblemonEvents.THROWN_POKEBALL_HIT.postThen(
                    event = ThrownPokeballHitEvent(this, it),
                    ifSucceeded = {},
                    ifCanceled = {
                        drop()
                        return
                    }
                )
            }

            if (this.age > 600 && this.capturingPokemon == null) {
                this.remove(RemovalReason.DISCARDED)
            }

            if (owner == null || !owner!!.isAlive || (captureState != CaptureState.NOT && capturingPokemon?.isAlive != true)) {
                breakFree()
                discard()
                return
            }
        }

        // Look at the target, if the target is known.
        val hitTargetPosition = dataTracker.get(HIT_TARGET_POSITION)
        if (hitTargetPosition.length() != 0.0) {
            val diff = hitTargetPosition.subtract(pos)
            yaw = ((MathHelper.atan2(diff.x, diff.z) * 180 / Math.PI).toFloat())
        }

        schedulingTracker.update(1/20F)
    }

    private fun shakeBall(task: ScheduledTask, rollsRemaining: Int, captureResult: CaptureContext) {
        if (this.capturingPokemon?.isAlive != true || !this.isAlive || this.owner == null|| owner?.isAlive != true) {
            if (this.capturingPokemon?.isAlive == true) {
                this.breakFree()
            }
            this.discard()
            task.expire()
            return
        }

        if (rollsRemaining <= 0) {
            if (captureResult.isSuccessfulCapture) {
                captureState = if (captureResult.isCriticalCapture) CaptureState.CAPTURED_CRITICAL else CaptureState.CAPTURED
                // Do a capture
                world.playSoundServer(pos, CobblemonSounds.POKE_BALL_CAPTURE_SUCCEEDED, volume = 0.8F, pitch = 1F)
                val pokemon = capturingPokemon ?: return
                val player = this.owner as? ServerPlayerEntity ?: return

                after(seconds = 1F) {
                    // Dupes occurred by double-adding Pokémon, this hopefully prevents it triple-condom style
                    if (pokemon.pokemon.isWild() && pokemon.isAlive && !captureFuture.isDone) {
                        pokemon.discard()
                        discard()
                        captureFuture.complete(true)
                        val party = Cobblemon.storage.getParty(player.uuid)
                        pokemon.pokemon.caughtBall = pokeBall
                        pokeBall.effects.forEach { effect -> effect.apply(player, pokemon.pokemon) }
                        party.add(pokemon.pokemon)
                        CobblemonEvents.POKEMON_CAPTURED.post(PokemonCapturedEvent(pokemon.pokemon, player, this))
                    }
                }
                return
            } else {
                breakFree()
            }
            return
        }

        world.playSoundServer(pos, CobblemonSounds.POKE_BALL_SHAKE, volume = 0.8F)
        // Emits a shake by changing the value to the opposite of what it currently is. Sends an update to the client basically.
        // We could replace this with a packet, but it feels awfully excessive when we already have 5 bajillion packets.
        dataTracker.update(SHAKE) { !it }
    }

    private fun breakFree() {
        val pokemon = capturingPokemon ?: return
        pokemon.setPosition(pos)
        pokemon.beamMode = 2
        pokemon.isInvisible = false

        if (pokemon.battleId == null) {
            pokemon.pokemon.status?.takeIf { it.status == Statuses.SLEEP }?.let { pokemon.pokemon.status = null }
        }

        captureState = CaptureState.BROKEN_FREE
        world.playSoundServer(pos, CobblemonSounds.POKE_BALL_OPEN, volume = 0.8F)

        after(seconds = 1F) {
            pokemon.busyLocks.remove(this)
            captureFuture.complete(false)
            world.sendParticlesServer(ParticleTypes.CLOUD, pos, 20, Vec3d(0.0, 0.2, 0.0), 0.05)
            discard()
        }
    }

    override fun getDefaultItem(): Item = pokeBall.item()

    override fun getDimensions(pPose: EntityPose) = DIMENSIONS

    private fun attemptCatch(pokemonEntity: PokemonEntity) {
        pokemonEntity.busyLocks.add(this)
        val displace = velocity
        captureState = CaptureState.HIT
        val mul = if (random.nextBoolean()) 1 else -1
        world.playSoundServer(pos, CobblemonSounds.POKE_BALL_HIT, volume = 0.4F)

        // Hit Pokémon plays recoil animation
        val pkt = PlayPoseableAnimationPacket(pokemonEntity.id, setOf("recoil"), emptySet())
        pkt.sendToPlayersAround(
            x = pokemonEntity.x,
            y = pokemonEntity.y,
            z = pokemonEntity.z,
            worldKey = pokemonEntity.world.registryKey,
            distance = 50.0
        )

        // Bounce backwards away from the hit Pokémon
        velocity = displace.multiply(-1.0, 0.0, -1.0).normalize().rotateY(mul * PI/3).multiply(0.1, 0.0, 0.1).add(0.0, 1.0 / 3, 0.0)
        pokemonEntity.phasingTargetId = this.id
        after(seconds = 0.7F) {
            // Start beaming them up.
            velocity = Vec3d.ZERO
            setNoGravity(true)
            world.playSoundServer(pos, CobblemonSounds.POKE_BALL_CAPTURE_STARTED, volume = 0.6F)
            pokemonEntity.beamMode = 3
        }

        after(seconds = 2.2F) {
            // Time to begin falling
            pokemonEntity.phasingTargetId = -1
            pokemonEntity.beamMode = 0
            pokemonEntity.isInvisible = true
            captureState = CaptureState.FALL
            after(seconds = 1.5F) {
                // If it was still falling after a second and a half, just assume it's landed because we can't wait all day.
                if (captureState == CaptureState.FALL) {
                    velocity = Vec3d.ZERO
                    setNoGravity(true)
                    isOnGround = true
                    beginCapture()
                }
            }
        }
    }

    override fun onCollision(hitResult: HitResult) {
        super.onCollision(hitResult)
        if (captureState == CaptureState.FALL && hitResult.type == HitResult.Type.BLOCK) {
            captureState = CaptureState.SHAKE
            if (world.isServerSide()) {
                beginCapture()
            }
        }
    }

    fun beginCapture() {
        // We have hit the ground, time to stop falling and start shaking! Calculate capture.
        capturingPokemon?.setPositionSafely(pos)
        val thrower = owner as LivingEntity
        val captureResult = Cobblemon.config.captureCalculator.processCapture(thrower, this, capturingPokemon!!).let {
            val event = PokeBallCaptureCalculatedEvent(thrower = thrower, pokemonEntity = capturingPokemon!!, pokeBallEntity = this, captureResult = it)
            CobblemonEvents.POKE_BALL_CAPTURE_CALCULATED.post(event)
            event.captureResult
        }

        var rollsRemaining = captureResult.numberOfShakes
        if (rollsRemaining == 4) {
            rollsRemaining--
        }

        taskBuilder()
            .iterations(captureResult.numberOfShakes + 1)
            .delay(SECONDS_BEFORE_SHAKE)
            .interval(SECONDS_BETWEEN_SHAKES)
            .execute {
                shakeBall(it, rollsRemaining, captureResult)
                rollsRemaining--
            }
            .build()
    }
    override fun getCurrentPoseType(): PoseType {
        return PoseType.NONE
    }

    override fun canUsePortals() = false

    override fun createSpawnPacket(): Packet<ClientPlayPacketListener> = CobblemonNetwork.asVanillaClientBound(SpawnPokeballPacket(this.pokeBall, this.aspects, super.createSpawnPacket() as EntitySpawnS2CPacket))

    override fun waterDrag(): Float = this.pokeBall.waterDragValue

}