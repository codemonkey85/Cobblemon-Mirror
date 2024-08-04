package com.cobblemon.mod.common.pokedex.scanner

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.pokedex.scanner.StartScanningPacket
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player

class PokedexScanningContext {
    var zoomLevel: Double = 1.0
    var active = false
    var innerRingRotation = 0
    var pokemonInFocus: PokemonEntity? = null
    var lastPokemonInFocus: PokemonEntity? = null
    var pokemonBeingScanned: PokemonEntity? = null
    var scanningProgress: Int = 0
    var transitionTicks = 0
    var originalHudHidden: Boolean = false
    var bufferImageSnap:  Boolean = false

    fun startScanning() {

    }

    fun stopScanning() {

    }

    //TODO: Make sure that inventoryTick and useTick dont both happen in the same tick
    fun tick(user: LocalPlayer, ticksInUse: Int, inUse: Boolean) {
        openScanGui(user, ticksInUse, inUse)
        tryScanPokemon(user)
    }

    fun openScanGui(user: LocalPlayer, ticksInUse: Int, inUse: Boolean) {
        if (inUse && ticksInUse == TIME_TO_OPEN) {
            println("Opened GUI")
            if (transitionTicks < 12) transitionTicks++
            innerRingRotation = (if (pokemonInFocus != null) (innerRingRotation + 10) else (innerRingRotation + 1)) % 360

            user.playSound(CobblemonSounds.POKEDEX_SCAN_OPEN)
            val client = Minecraft.getInstance()

            // Hide the HUD during scan mode
            originalHudHidden = client.options.hideGui
            client.options.hideGui = true
            active = true
        }
    }

    fun tryScanPokemon(user: LocalPlayer) {
        val targetUUID = PokemonScanner.findPokemon(user)?.uuid
        if (targetUUID != null) {
            Cobblemon.implementation.networkManager.sendToServer(StartScanningPacket(targetUUID))
            active = true
        }
    }

    companion object {
        //Time it takes before UI is opened, in ticks
        val TIME_TO_OPEN = 20
    }
}