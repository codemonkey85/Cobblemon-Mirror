package com.cobblemon.mod.common.pokedex.scanner

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUI
import com.cobblemon.mod.common.client.pokedex.PokedexScannerRenderer
import com.cobblemon.mod.common.client.sound.CancellableSoundController.playSound
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.pokedex.scanner.StartScanningPacket
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.resources.sounds.SimpleSoundInstance

class PokedexUsageContext {
    //PokedexGUI
    var infoGuiOpen = false
    //PokedexScannerRenderer
    var scanningGuiOpen = false
    var pokemonInFocus: PokemonEntity? = null
    var scanningProgress: Int = 0
    var transitionTicks = 0
    //Tracks whether the hud should be enabled or disabled after closing scan UI
    var originalHudHidden: Boolean = false
    var innerRingRotation = 0
    var usageTicks = 0
    var focusTicks = 0

    val renderer = PokedexScannerRenderer()

    fun startUsing() {

    }

    fun stopUsing(user: LocalPlayer, ticksInUse: Int) {
        tryOpenInfoGui(user, ticksInUse)
        resetState()
    }

    //TODO: Make sure that inventoryTick and useTick dont both happen in the same tick
    fun tick(user: LocalPlayer, ticksInUse: Int, inUse: Boolean) {
        tryOpenScanGui(user, ticksInUse, inUse)
        if (scanningGuiOpen) {
            tryScanPokemon(user)
        }

        if (inUse) {
            if (scanningGuiOpen && transitionTicks < 12) transitionTicks++
            innerRingRotation = (if (pokemonInFocus != null) (innerRingRotation + 10) else (innerRingRotation + 1)) % 360
            usageTicks++
        }
    }

    fun tryOpenScanGui(user: LocalPlayer, ticksInUse: Int, inUse: Boolean) {
        if (inUse && ticksInUse == TIME_TO_OPEN_SCANNER) {
            val client = Minecraft.getInstance()

            // Hide the HUD during scan mode
            originalHudHidden = client.options.hideGui
            client.options.hideGui = true
            scanningGuiOpen = true
            user.playSound(CobblemonSounds.POKEDEX_SCAN_OPEN)
        }
    }

    fun tryOpenInfoGui(user: LocalPlayer, ticksInUse: Int) {
        if (ticksInUse < TIME_TO_OPEN_SCANNER) {
            openPokedexGUI(user)
            infoGuiOpen = true
        }
    }

    fun openPokedexGUI(user: LocalPlayer) {
        PokedexGUI.open(CobblemonClient.clientPokedexData, "red", null)
        user.playSound(CobblemonSounds.POKEDEX_OPEN)
    }

    fun tryScanPokemon(user: LocalPlayer) {
        val targetPokemon = PokemonScanner.findPokemon(user)
        val targetUUID = targetPokemon?.uuid
        if (targetPokemon != null && targetUUID != null) {
            scanningProgress++
            if (targetPokemon != pokemonInFocus) {
                pokemonInFocus = targetPokemon
                StartScanningPacket(targetUUID).sendToServer()
            }
            user.playSound(CobblemonSounds.POKEDEX_SCAN_LOOP)
        }
        else {
            scanningProgress = 0
        }
    }

    fun resetState() {
        val client = Minecraft.getInstance()
        client.options.hideGui = originalHudHidden
        scanningGuiOpen = false
        pokemonInFocus = null
        innerRingRotation = 0
        scanningProgress = 0
        transitionTicks = 0
        usageTicks = 0
        focusTicks = 0
    }

    fun tryRenderOverlay(graphics: GuiGraphics, tickCounter: DeltaTracker) {
        renderer.onRenderOverlay(graphics, tickCounter)
    }

    companion object {
        //Time it takes before UI is opened, in ticks
        val TIME_TO_OPEN_SCANNER = 20
    }
}