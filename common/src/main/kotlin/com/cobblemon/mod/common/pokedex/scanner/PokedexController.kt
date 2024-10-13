package com.cobblemon.mod.common.pokedex.scanner

import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer
import java.util.*

enum class STATE {
    IDLE,
    SCANNING,
    SCANNED
}

class PokedexController(val player: ServerPlayer) {
    companion object {
        private val controllers = mutableMapOf<UUID, PokedexController>()
        fun getController(player: ServerPlayer): PokedexController {
            val uuid = player.uuid
            if (!controllers.containsKey(uuid)) controllers[uuid] = PokedexController(player)
            return controllers[uuid]!!
        }

        const val GOAL_TICKS = 50
    }

    var zoom: Int = 10
    var targetPokemon: Pokemon? = null
    private var startTime: Int? = null

    var state: STATE = STATE.IDLE

    fun useTick(remainingUseTicks: Int) {
        val target = PokemonScanner.findPokemon(player, zoom)
        if (target === null) {
            // Not pointed at anything.
            startTime = null
            targetPokemon = null
            zoom = 10
            state = STATE.IDLE
            // TODO: Send IDLE update packet
            /**
             * {
             *   state: "IDLE"
             * }
             */
            return
        }
        val newTargetPokemon = target.pokemon
        if (targetPokemon !== newTargetPokemon) {
            // Pointed at a new thing.
            startTime = remainingUseTicks
            targetPokemon = newTargetPokemon
            state = STATE.SCANNING
            // TODO: Send SCANNING update packet
            /**
             * {
             *   state: "SCANNING"
             * }
             */
            return
        }
        if (state == STATE.SCANNING && startTime!! - remainingUseTicks >= GOAL_TICKS) {
            state = STATE.SCANNED
            // TODO: Send SCANNED update packet
            /**
             * {
             *   state: "SCANNED",
             *   pokemon: targetPokemon
             * }
             */
        }
    }

    fun end() {
        startTime = null
        targetPokemon = null
        if (state !== STATE.IDLE) {
            // TODO: Send IDLE update packet (as above, so below)
            state = STATE.IDLE
        }
    }
}