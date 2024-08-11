package com.cobblemon.mod.common.pokedex.scanner

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUI
import com.cobblemon.mod.common.client.pokedex.PokedexScannerRenderer
import com.cobblemon.mod.common.client.sound.CancellableSoundController.playSound
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.pokedex.scanner.FinishScanningPacket
import com.cobblemon.mod.common.net.messages.server.pokedex.scanner.StartScanningPacket
import com.ibm.icu.impl.duration.impl.DataRecord.EGender.F
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.util.Mth.clamp
import kotlin.math.max
import kotlin.math.min

class PokedexUsageContext {
    //PokedexGUI
    var infoGuiOpen = false
    //PokedexScannerRenderer
    var scanningGuiOpen = false
    var pokemonInFocus: PokemonEntity? = null
    var scanningProgress: Int = 0
    var transitionTicks = 0
    var innerRingRotation = 0
    var usageTicks = 0
    var focusTicks = 0
    var zoomLevel = 0F

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
            if (scanningGuiOpen && transitionTicks < ENTRY_ANIM_STAGES) transitionTicks++
            innerRingRotation = (if (pokemonInFocus != null) (innerRingRotation + 10) else (innerRingRotation + 1)) % 360
            usageTicks++
        }
        if (pokemonInFocus != null) {
            focusTicks = min(focusTicks + 1, 9)
        }
    }

    fun tryOpenScanGui(user: LocalPlayer, ticksInUse: Int, inUse: Boolean) {
        if (inUse && ticksInUse == TIME_TO_OPEN_SCANNER) {

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
                focusTicks = 0
                StartScanningPacket(targetUUID).sendToServer()
            }
            user.playSound(CobblemonSounds.POKEDEX_SCAN_LOOP)
            if (scanningProgress > TICKS_TO_SCAN){
                FinishScanningPacket().sendToServer()
            }
        }
        else {
            pokemonInFocus = null
            scanningProgress = 0
            focusTicks = 0
        }
    }

    fun resetState() {
        scanningGuiOpen = false
        pokemonInFocus = null
        innerRingRotation = 0
        scanningProgress = 0
        transitionTicks = 0
        usageTicks = 0
        focusTicks = 0
        zoomLevel = 0F
    }

    //Entrypoint to UI called by platform events
    fun tryRenderOverlay(graphics: GuiGraphics, tickCounter: DeltaTracker) {
        renderer.onRenderOverlay(graphics, tickCounter)
    }

    fun adjustZoom(verticalScrollAmount: Double) {
        zoomLevel = clamp(zoomLevel + verticalScrollAmount.toFloat(), 0F, NUM_ZOOM_STAGES.toFloat())
    }

    //Higher multiplier = more zoomed out
    fun getFovMultiplier() = 1 - (zoomLevel / NUM_ZOOM_STAGES)

    companion object {
        //Time it takes before UI is opened, in ticks
        const val TIME_TO_OPEN_SCANNER = 20
        const val NUM_ZOOM_STAGES = 10
        const val TICKS_TO_SCAN = 60
        const val ENTRY_ANIM_STAGES = 12
    }
}