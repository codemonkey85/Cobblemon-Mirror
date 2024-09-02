/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.entity

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.entity.PokemonSideDelegate
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunctions
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.scheduling.SchedulingTracker
import com.cobblemon.mod.common.api.scheduling.afterOnClient
import com.cobblemon.mod.common.api.scheduling.lerpOnClient
import com.cobblemon.mod.common.client.ClientMoLangFunctions
import com.cobblemon.mod.common.client.particle.BedrockParticleOptionsRepository
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.ActiveAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.PrimaryAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.client.render.pokemon.PokemonRenderer.Companion.ease
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.MovingSoundInstance
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

class PokemonClientDelegate : PosableState(), PokemonSideDelegate {
    companion object {
        const val BEAM_SHRINK_TIME = 0.4F
        const val BEAM_EXTEND_TIME = 0.2F
        const val POKEBALL_AIR_TIME = 0.5F
    }

    override val schedulingTracker: SchedulingTracker
        get() = currentEntity.schedulingTracker

    lateinit var currentEntity: PokemonEntity
    var phaseTarget: Entity? = null
    var entityScaleModifier = 1F

    override fun getEntity() = currentEntity

    override fun updatePartialTicks(partialTicks: Float) {
        this.currentPartialTicks = partialTicks
        schedulingTracker.update(0F)
    }

    var beamStartTime = System.currentTimeMillis()
    var ballStartTime = System.currentTimeMillis()
    var ballDone = false
    var ballOffset = 0f
    var ballRotOffset = 0f
    var sendOutPosition: Vec3? = null
    var sendOutOffset: Vec3? = null
    var playedSendOutSound: Boolean = false
    var playedThrowingSound: Boolean = false

    val secondsSinceBeamEffectStarted: Float
        get() = (System.currentTimeMillis() - beamStartTime) / 1000F

    val secondsSinceBallThrown: Float
        get() = (System.currentTimeMillis() - ballStartTime) / 1000F

    private var cryAnimation: ActiveAnimation? = null

    override fun onSyncedDataUpdated(data: EntityDataAccessor<*>) {
        super.onSyncedDataUpdated(data)
        if (this::currentEntity.isInitialized) {
            if (data == PokemonEntity.SPECIES) {
                val identifier = ResourceLocation.parse(currentEntity.entityData.get(PokemonEntity.SPECIES))
                currentPose = null
                currentEntity.pokemon.species = PokemonSpecies.getByIdentifier(identifier)!! // TODO exception handling
                // force a model update - handles edge case where the PosableState's tracked PosableModel isn't updated until the LivingEntityRenderer render is run
                currentModel = PokemonModelRepository.getPoser(identifier, currentEntity.aspects)
            } else if (data == PokemonEntity.ASPECTS) {
                currentAspects = currentEntity.entityData.get(PokemonEntity.ASPECTS)
            } else if (data == PokemonEntity.DYING_EFFECTS_STARTED) {
                val isDying = currentEntity.entityData.get(PokemonEntity.DYING_EFFECTS_STARTED)
                if (isDying) {
                    val model = currentModel ?: return
                    val animation = try {
                        model.getAnimation(this, "faint", runtime)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    } ?: return
                    val primaryAnimation = PrimaryAnimation(animation)
                    after(seconds = 3F) { entityScaleModifier = 0F }
                    this.addPrimaryAnimation(primaryAnimation)
                }
            } else if (data == PokemonEntity.BEAM_MODE) {
                // If you make adjustments to this code, make sure to Find Usages for both PokemonEntity.beamMode and PokemonEntity.BEAM_MODE
                // TODO: change beamMode to an enum, or a set of booleans (send-out vs recall, delay vs delay)
                val beamMode = currentEntity.beamMode
                when (beamMode) {
                    0 -> { /* Do nothing */ }
                    1 -> {
                        // Scaling up out of pokeball
                        playedSendOutSound = false
                        entityScaleModifier = 0F
                        beamStartTime = System.currentTimeMillis()
                        ballStartTime = System.currentTimeMillis()
                        currentEntity.isInvisible = true
                        ballDone = false
                        var soundPos = currentEntity.position()
                        currentEntity.ownerUUID?.let{
                            currentEntity.level().getPlayerByUUID(it)?.let {
                                val offset = it.position().subtract(currentEntity.position().add(0.0, 2.0 - (ballOffset.toDouble()/10f), 0.0)).normalize().scale(-ease(ballOffset.toDouble()))
                                with(it.position().subtract(currentEntity.position())) {
                                    var newOffset = offset.scale(2.0)
                                    val distance = it.position().distanceTo(currentEntity.position())
                                    newOffset = newOffset.scale((distance / 10.0) * 5)
                                    soundPos = currentEntity.position().add(newOffset)
                                }
                                it.swing(it.usedItemHand ?: InteractionHand.MAIN_HAND)
                            }
                        }
                        val client = Minecraft.getInstance()
                        val sound = MovingSoundInstance(SoundEvent.createVariableRangeEvent(CobblemonSounds.POKE_BALL_TRAIL.location), SoundSource.PLAYERS, { sendOutPosition?.add(sendOutOffset) }, 0.1f, 1f, false, 20, 0)
                        if (!playedThrowingSound){
                            client.soundManager.play(sound)
                            playedThrowingSound = true
                        }
                        lerpOnClient(POKEBALL_AIR_TIME) { ballOffset = it }
                        ballRotOffset = ((Math.random()) * currentEntity.level().random.nextIntBetweenInclusive(-15, 15)).toFloat()

                        currentEntity.after(seconds = POKEBALL_AIR_TIME){
                            beamStartTime = System.currentTimeMillis()
                            ballDone = true
                            if (client.soundManager.getSoundEvent(CobblemonSounds.POKE_BALL_SEND_OUT.location) != null && !playedSendOutSound) {
                                client.level?.playSound(client.player, soundPos.x, soundPos.y, soundPos.z, SoundEvent.createVariableRangeEvent(CobblemonSounds.POKE_BALL_SEND_OUT.location), SoundSource.PLAYERS, 0.6f, 1f)
                                playedSendOutSound = true
                            }
                            currentEntity.ownerUUID?.let {
                                    client.level?.playSound(client.player, soundPos.x, soundPos.y, soundPos.z, SoundEvent.createVariableRangeEvent(CobblemonSounds.POKE_BALL_SEND_OUT.location), SoundSource.PLAYERS, 0.6f, 1f)
                                    playedSendOutSound = true
                                    /// create end rod particles in a 0.1 radius around the soundPos with a count of 50 and a random velocity of 0.1
                                    sendOutPosition?.let{
                                        val newPos = it.add(sendOutOffset)
                                        val ballType = currentEntity.pokemon.caughtBall.name.path.toLowerCase().replace("_","")
                                        val mode = if(currentEntity.isBattling) "battle" else "casual"
                                        val sendflash = BedrockParticleOptionsRepository.getEffect(cobblemonResource("${ballType}/${mode}/sendflash"))
                                        sendflash?.let { effect ->
                                            val wrapper = MatrixWrapper()
                                            val matrix = PoseStack()
                                            matrix.translate(newPos.x, newPos.y, newPos.z)
                                            wrapper.updateMatrix(matrix.last().pose())
                                            val world = Minecraft.getInstance().level ?: return@let
                                            ParticleStorm(effect, wrapper, world).spawn()
                                            val ballsparks = BedrockParticleOptionsRepository.getEffect(cobblemonResource("${ballType}/${mode}/ballsparks"))
                                            val ballsendsparkle = BedrockParticleOptionsRepository.getEffect(cobblemonResource("${ballType}/${mode}/ballsendsparkle"))
                                            // using afterOnClient because it's such a small timeframe that it's unlikely the entity has been removed & we'd like the precision
                                            afterOnClient(seconds = 0.01667f) {
                                                ballsparks?.let { effect -> ParticleStorm(effect, wrapper, world).spawn() }
                                                ballsendsparkle?.let { effect -> ParticleStorm(effect, wrapper, world).spawn() }
                                                currentEntity.after(seconds = 0.4f) {
                                                    val ballsparkle = BedrockParticleOptionsRepository.getEffect(cobblemonResource("${ballType}/ballsparkle"))
                                                    ballsparkle?.let { effect -> ParticleStorm(effect, wrapper, world).spawn() }
                                            }
                                        }
                                    }
                                }
                            }
                            currentEntity.after(seconds = BEAM_EXTEND_TIME) {
                                lerpOnClient(BEAM_SHRINK_TIME) { entityScaleModifier = it }
                                currentEntity.isInvisible = false
                                currentEntity.after(seconds = POKEBALL_AIR_TIME*2){
                                    ballOffset = 0f
                                    ballRotOffset = 0f
                                    sendOutPosition = null
                                }
                            }
                        }
                    }
                    2 -> {
                        // Scaling up with no delay and no particles
                        playedSendOutSound = false
                        entityScaleModifier = 0F
                        currentEntity.isInvisible = false
                        val soundPos = currentEntity.position()
                        val client = Minecraft.getInstance()

                        if (client.soundManager.getSoundEvent(CobblemonSounds.POKE_BALL_SEND_OUT.location) != null && !playedSendOutSound) {
                            client.level?.playSound(
                                client.player,
                                soundPos.x,
                                soundPos.y,
                                soundPos.z,
                                CobblemonSounds.POKE_BALL_SEND_OUT,
                                SoundSource.PLAYERS,
                                0.6f,
                                1f
                            )
                            playedSendOutSound = true
                        }
                        lerpOnClient(BEAM_SHRINK_TIME) { entityScaleModifier = it }
                        currentEntity.after(seconds = BEAM_SHRINK_TIME * 2) {
                            ballOffset = 0f
                            ballRotOffset = 0f
                            sendOutPosition = null
                        }
                    }
                    3 -> {
                        // Scaling down into pokeball
                        entityScaleModifier = 1F
                        beamStartTime = System.currentTimeMillis()
                        ballOffset = 0f
                        ballRotOffset = 0f
                        sendOutPosition = null
                        afterOnClient(seconds = BEAM_EXTEND_TIME) {
                            lerpOnClient(BEAM_SHRINK_TIME) {
                                entityScaleModifier = (1 - it)
                            }
                        }
                    }
                    else -> { /* Do nothing */ }
                }
            } else if (data == PokemonEntity.LABEL_LEVEL) {
                currentEntity.entityData.get(PokemonEntity.LABEL_LEVEL)
                    .takeIf { it > 0 }
                    ?.let { currentEntity.pokemon.level = it }
            } else if (data == PokemonEntity.PHASING_TARGET_ID) {
                val phasingTargetId = currentEntity.entityData.get(PokemonEntity.PHASING_TARGET_ID)
                if (phasingTargetId != -1) {
                    setPhaseTarget(phasingTargetId)
                } else {
                    phaseTarget = null
                }
            }
        }
    }

    override fun changePokemon(pokemon: Pokemon) {
        pokemon.isClient = true
    }

    override fun addToStruct(struct: QueryStruct) {
        super.addToStruct(struct)
        struct.addFunctions(functions.functions)
        struct.addFunctions(ClientMoLangFunctions.clientFunctions)
        runtime.environment.query = struct
    }

    override fun initialize(entity: PokemonEntity) {
        this.currentEntity = entity
        this.age = entity.tickCount

        this.runtime.environment.query.addFunctions(mapOf(
            "in_battle" to java.util.function.Function {
                return@Function DoubleValue(currentEntity.isBattling)
            },
            "shiny" to java.util.function.Function {
                return@Function DoubleValue(currentEntity.pokemon.shiny)
            },
            "form" to java.util.function.Function {
                return@Function StringValue(currentEntity.pokemon.form.name)
            },
            "width" to java.util.function.Function {
                return@Function DoubleValue(currentEntity.boundingBox.xsize)
            },
            "height" to java.util.function.Function {
                return@Function DoubleValue(currentEntity.boundingBox.ysize)
            },
            "weight" to java.util.function.Function {
                return@Function DoubleValue(currentEntity.pokemon.species.weight.toDouble())
            },
            "friendship" to java.util.function.Function {
                return@Function DoubleValue(currentEntity.pokemon.friendship.toDouble())
            }
        ))
    }

    override fun tick(entity: PokemonEntity) {
        incrementAge(entity)
    }

    fun setPhaseTarget(targetId: Int) {
        this.phaseTarget = currentEntity.level().getEntity(targetId)
    }

    override fun handleStatus(status: Byte) {
        if (status == 10.toByte()) {
            val model = (currentModel ?: return)
            val animation = model.getEatAnimation(this) ?: return
            activeAnimations.add(animation)
        }
    }

    override fun updatePostDeath() {
        ++currentEntity.deathTime
    }

    fun cry() {
        val model = currentModel ?: return
        if (cryAnimation != null && (cryAnimation in activeAnimations || cryAnimation == primaryAnimation)) {
            return
        }

        val animation = model.cryAnimation(this) ?: return
        if (animation is PrimaryAnimation) {
            addPrimaryAnimation(animation)
        } else {
            activeAnimations.add(animation)
        }
        cryAnimation = animation
    }
}