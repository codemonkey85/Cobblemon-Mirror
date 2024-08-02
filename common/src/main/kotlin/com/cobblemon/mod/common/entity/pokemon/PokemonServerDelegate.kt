/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.entity.PokemonSender
import com.cobblemon.mod.common.api.entity.PokemonSideDelegate
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.activestate.ActivePokemonState
import com.cobblemon.mod.common.pokemon.activestate.SentOutState
import com.cobblemon.mod.common.util.getIsSubmerged
import com.cobblemon.mod.common.util.playSoundServer
import com.cobblemon.mod.common.util.update
import com.cobblemon.mod.common.world.gamerules.CobblemonGameRules
import java.util.Optional
import net.minecraft.world.entity.Entity
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.network.chat.Component
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.level.pathfinder.PathType

/** Handles purely server logic for a Pokémon */
class PokemonServerDelegate : PokemonSideDelegate {
    lateinit var entity: PokemonEntity
    var acknowledgedHPStat = -1

    /** Mocked properties exposed to the client [PokemonEntity]. */
    private val mock: PokemonProperties?
        get() = entity.effects.mockEffect?.mock

    override fun changePokemon(pokemon: Pokemon) {
        updatePathfindingPenalties(pokemon)
        entity.registerGoals()
        updateMaxHealth()
    }

    fun updatePathfindingPenalties(pokemon: Pokemon) {
        val moving = pokemon.form.behaviour.moving
        entity.setPathfindingMalus(PathType.LAVA, if (moving.swim.canSwimInLava) 12F else -1F)
        entity.setPathfindingMalus(PathType.WATER, if (moving.swim.canSwimInWater) 12F else -1F)
        entity.setPathfindingMalus(PathType.WATER_BORDER, if (moving.swim.canSwimInWater) 6F else -1F)
        if (moving.swim.canBreatheUnderwater) {
            entity.setPathfindingMalus(PathType.WATER, if (moving.walk.avoidsLand) 0F else 4F)
        }
        if (moving.swim.canBreatheUnderlava) {
            entity.setPathfindingMalus(PathType.LAVA, if (moving.swim.canSwimInLava) 4F else -1F)
        }
        if (moving.walk.avoidsLand) {
            entity.setPathfindingMalus(PathType.WALKABLE, 12F)
        }

        if (moving.walk.canWalk && moving.fly.canFly) {
            entity.setPathfindingMalus(PathType.WALKABLE, 0F)
        }

        entity.navigation.setCanPathThroughFire(entity.fireImmune())
    }

    fun updateMaxHealth() {
        val currentHealthRatio = entity.health.toDouble() / entity.maxHealth
        // Why you would remove HP is beyond me but protects us from obscure crash due to crappy addon
        acknowledgedHPStat = entity.form.baseStats[Stats.HP] ?: return

        val minStat = 50 // Metapod's base HP
        val maxStat = 150 // Slaking's base HP
        val baseStat = acknowledgedHPStat.coerceIn(minStat..maxStat)
        val r = (baseStat - minStat) / (maxStat - minStat).toDouble()
        val minPossibleHP = 10.0 // half of a player's HP
        val maxPossibleHP = 100.0 // Iron Golem HP
        val maxHealth = minPossibleHP + r * (maxPossibleHP - minPossibleHP)

        entity.getAttribute(Attributes.MAX_HEALTH)?.baseValue = maxHealth
        entity.health = currentHealthRatio.toFloat() * maxHealth.toFloat()
    }

    override fun initialize(entity: PokemonEntity) {
        this.entity = entity
        with(entity) {
            speed = 0.1F
            entity.despawner.beginTracking(this)
        }
        updateTrackedValues()
    }

    fun getBattle() = entity.battleId?.let(BattleRegistry::getBattle)

    fun updateTrackedValues() {
        val trackedSpecies = mock?.species ?: entity.pokemon.species.resourceIdentifier.toString()
        val trackedNickname =  mock?.nickname ?: entity.pokemon.nickname ?: Component.empty()
        val trackedAspects = mock?.aspects ?: entity.pokemon.aspects

        entity.entityData.set(PokemonEntity.SPECIES, trackedSpecies)
        if (entity.entityData.get(PokemonEntity.NICKNAME) != trackedNickname) {
            entity.entityData.set(PokemonEntity.NICKNAME, trackedNickname)
        }
        entity.entityData.set(PokemonEntity.ASPECTS, trackedAspects)
        entity.entityData.set(PokemonEntity.LABEL_LEVEL, entity.pokemon.level)
        entity.entityData.set(PokemonEntity.MOVING, entity.deltaMovement.multiply(1.0, if (entity.onGround()) 0.0 else 1.0, 1.0).length() > 0.005F)
        entity.entityData.set(PokemonEntity.FRIENDSHIP, entity.pokemon.friendship)

        updatePoseType()
    }

    override fun onSyncedDataUpdated(data: EntityDataAccessor<*>) {
        super.onSyncedDataUpdated(data)
        if (this::entity.isInitialized) {
            when (data) {
                PokemonEntity.BEHAVIOUR_FLAGS -> updatePoseType()
            }
        }
    }

    override fun tick(entity: PokemonEntity) {
        val state = entity.pokemon.state
        if (state !is ActivePokemonState || state.entity != entity) {
            if (!entity.isDeadOrDying && entity.health > 0) {
                entity.pokemon.state = SentOutState(entity)
            }
        }

        if (entity.ownerUUID != null && entity.pokemon.storeCoordinates.get() == null) {
            return entity.discard()
        } else if (entity.pokemon.isNPCOwned() && entity.owner?.isAlive != true) {
            return entity.discard()
        } else if (entity.pokemon.isNPCOwned() && entity.ownerUUID == null) {
            entity.ownerUUID = entity.pokemon.getOwnerUUID()
        }

        val tethering = entity.tethering
        if (tethering != null && entity.pokemon.tetheringId != tethering.tetheringId) {
            return entity.discard()
        }

//        if (!entity.behaviour.moving.walk.canWalk && entity.behaviour.moving.fly.canFly && !entity.getBehaviourFlag(PokemonBehaviourFlag.FLYING)) {
//            entity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, true)
//        }

        entity.entityData.update(PokemonEntity.BATTLE_ID) { opt ->
            val battleId = opt.orElse(null)
            if (battleId != null && BattleRegistry.getBattle(battleId).let { it == null || it.ended }) {
                Optional.empty()
            } else {
                opt
            }
        }

        val battle = getBattle()
        if (entity.ticksLived % 20 == 0 && battle != null) {
            val activeBattlePokemon = battle
                .activePokemon
                .find { it.battlePokemon?.uuid == entity.pokemon.uuid }

            if (activeBattlePokemon != null) {
                activeBattlePokemon.position = entity.level() as ServerLevel to entity.position()
            }
        }

        if (entity.form.baseStats[Stats.HP] != acknowledgedHPStat) {
            updateMaxHealth()
        }

        if (entity.ownerUUID != entity.pokemon.getOwnerUUID()) {
            entity.ownerUUID = entity.pokemon.getOwnerUUID()
        }

        if (entity.ownerUUID == null && tethering != null) {
            entity.ownerUUID = tethering.playerId
        }

        if (entity.ownerUUID != null && entity.owner == null && entity.tethering == null) {
            entity.remove(Entity.RemovalReason.DISCARDED)
        }

        updateTrackedValues()
    }

    fun updatePoseType() {
        val isSleeping = entity.pokemon.status?.status == Statuses.SLEEP && entity.behaviour.resting.canSleep
        val isMoving = entity.entityData.get(PokemonEntity.MOVING)
        val isPassenger = entity.isPassenger()
        val isUnderwater = entity.getIsSubmerged()
        val isFlying = entity.getBehaviourFlag(PokemonBehaviourFlag.FLYING)

        val poseType = when {
            isPassenger -> PoseType.STAND
            isSleeping -> PoseType.SLEEP
            isMoving && isUnderwater  -> PoseType.SWIM
            isUnderwater -> PoseType.FLOAT
            isMoving && isFlying -> PoseType.FLY
            isFlying -> PoseType.HOVER
            isMoving -> PoseType.WALK
            else -> PoseType.STAND
        }

        entity.entityData.set(PokemonEntity.POSE_TYPE, poseType)
    }

    override fun drop(source: DamageSource?) {
        val player = source?.directEntity as? ServerPlayer
        if (entity.pokemon.isWild()) {
            entity.killer = player
        }
    }

    override fun updatePostDeath() {
        // clear active effects before proceeding
        val owner = entity.owner
        if (!entity.entityData.get(PokemonEntity.DYING_EFFECTS_STARTED)) {
            entity.entityData.set(PokemonEntity.DYING_EFFECTS_STARTED, true)
            if (owner is PokemonSender && entity.beamMode == -1) {
                entity.recallWithAnimation()
            }
        }
        if (entity.deathTime == 0) {
            entity.effects.wipe()
            entity.deathTime = 1
            return
        } else if (entity.effects.progress?.isDone == false) {
            return
        }

        ++entity.deathTime

        if (entity.deathTime == 30) {
            if (owner != null && owner !is PokemonSender) {
                entity.level().playSoundServer(owner.position(), CobblemonSounds.POKE_BALL_RECALL, volume = 0.6F)
//                entity.recallWithAnimation()
                entity.entityData.set(PokemonEntity.PHASING_TARGET_ID, owner.id)
                entity.entityData.set(PokemonEntity.BEAM_MODE, 3)
            }
        }

        if (entity.deathTime == 60) {
            if (entity.owner == null) {
                entity.level().broadcastEntityEvent(entity, 60.toByte()) // Sends smoke effect
                if(entity.level().gameRules.getBoolean(CobblemonGameRules.DO_POKEMON_LOOT)) {
                    (entity.drops ?: entity.pokemon.form.drops).drop(entity, entity.level() as ServerLevel, entity.position(), entity.killer)
                }
            }

            entity.remove(Entity.RemovalReason.KILLED)
        }
    }
}