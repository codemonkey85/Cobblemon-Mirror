/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokedex.scanner

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.api.pokedex.PokedexLearnedInformation
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUI
import com.cobblemon.mod.common.client.pokedex.PokedexScannerRenderer
import com.cobblemon.mod.common.client.pokedex.PokedexTypes
import com.cobblemon.mod.common.net.messages.client.pokedex.ServerConfirmedRegisterPacket
import com.cobblemon.mod.common.net.messages.server.pokedex.scanner.FinishScanningPacket
import com.cobblemon.mod.common.net.messages.server.pokedex.scanner.StartScanningPacket
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.player.LocalPlayer
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth.clamp
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class PokedexUsageContext {
    companion object {
        const val ZOOM_STAGES = 10
        const val BLOCK_LENGTH_PER_ZOOM_STAGE = 2
        const val OPEN_SCANNER_BUFFER_TICKS = 5 // Open scanner interface if usage ticks are above this threshold
        const val VIEW_INFO_BUFFER_TICKS = 10
        const val SUCCESS_SCAN_SERVER_TICKS = 15 // Used by FinishScanningHandler to determine if user has scanned for long enough to register
        const val MAX_SCAN_PROGRESS = 100
        const val TRANSITION_INTERVALS = 12F
        const val FOCUS_INTERVALS = 9F
        const val CENTER_INFO_DISPLAY_INTERVALS = 5F
        const val CENTER_INFO_LINGER_INTERVALS = 35F
        const val RENDER_UPDATES_PER_SECOND = (1/0.0175).toFloat() // How many times the render should update in a second
    }

    var infoGuiOpen: Boolean = false
    var scanningGuiOpen: Boolean = false
    var isPokemonInFocusOwned: Boolean = false
    var registerCompleted: Boolean = false
    var scannedSpecies: ResourceLocation? = null
    var scannableEntityInFocus: ScannableEntity? = null
    var viewInfoTicks: Int = 0
    var scanningProgress: Float = 0F
    var displayRegisterInfoIntervals: Float = 0F
    var transitionIntervals: Float = 0F
    var usageIntervals: Float = 0F
    var focusIntervals: Float = 0F
    var innerRingRotation: Float = 0F
    var zoomLevel: Float = 0F
    var newPokemonInfo: PokedexLearnedInformation = PokedexLearnedInformation.NONE
    var type: PokedexTypes = PokedexTypes.RED
    var availableInfoFrames: MutableList<Boolean?> = mutableListOf(null, null, null, null)
    val renderer: PokedexScannerRenderer = PokedexScannerRenderer()

    fun stopUsing(user: LocalPlayer, ticksInUse: Int) {
        tryOpenInfoGui(user, ticksInUse)
        resetState(false)
    }

    fun renderUpdate(user: LocalPlayer, graphics: GuiGraphics, tickCounter: DeltaTracker) {
        val tickDelta = tickCounter.realtimeDeltaTicks.takeIf { !Minecraft.getInstance().isPaused } ?: 0F
        val updateInterval = (tickDelta / 20) * RENDER_UPDATES_PER_SECOND

        if (scanningGuiOpen && viewInfoTicks < VIEW_INFO_BUFFER_TICKS) {
            if (transitionIntervals < TRANSITION_INTERVALS) transitionIntervals = min(transitionIntervals + updateInterval, TRANSITION_INTERVALS)
            innerRingRotation = (if (scannableEntityInFocus != null) (innerRingRotation + (updateInterval * 10F)) else (innerRingRotation + updateInterval)) % 360
            usageIntervals += updateInterval
        } else {
            if (transitionIntervals > 0) {
                if (transitionIntervals == TRANSITION_INTERVALS) user.playSound(CobblemonSounds.POKEDEX_SCAN_CLOSE)
                transitionIntervals = max(transitionIntervals - updateInterval, 0F)
                if (transitionIntervals <= 0) {

                    if (viewInfoTicks >= VIEW_INFO_BUFFER_TICKS) openPokedexGUI(user, type,
                        scannableEntityInFocus!!.resolvePokemonScan()?.species?.resourceIdentifier
                    )
                    resetState()
                }
            }
        }

        if (scannedSpecies !== null && scannableEntityInFocus != null) {
            val targetId = scannableEntityInFocus?.resolveEntityScan()?.id
            if (scanningProgress == 0F) targetId?.let { StartScanningPacket(it, zoomLevel.toInt()).sendToServer() }
            if (scanningProgress < (MAX_SCAN_PROGRESS + CENTER_INFO_DISPLAY_INTERVALS)) scanningProgress += updateInterval
            if (scanningProgress >= MAX_SCAN_PROGRESS) targetId?.let { FinishScanningPacket(it, zoomLevel.toInt()).sendToServer() }
            if (focusIntervals > 0) focusIntervals = max(0F, focusIntervals - updateInterval)
        } else {
            if (scannableEntityInFocus != null) {
                if (focusIntervals < FOCUS_INTERVALS) focusIntervals = min(focusIntervals + updateInterval, FOCUS_INTERVALS)
            } else {
                if (focusIntervals > 0) focusIntervals = max(0F, focusIntervals - updateInterval)
            }
        }

        if (registerCompleted) {
            if (displayRegisterInfoIntervals < (CENTER_INFO_DISPLAY_INTERVALS + CENTER_INFO_LINGER_INTERVALS)) displayRegisterInfoIntervals = min((CENTER_INFO_DISPLAY_INTERVALS + CENTER_INFO_LINGER_INTERVALS),displayRegisterInfoIntervals + updateInterval)
            if (displayRegisterInfoIntervals >= (CENTER_INFO_DISPLAY_INTERVALS + CENTER_INFO_LINGER_INTERVALS)) registerCompleted = false
        } else {
            if (displayRegisterInfoIntervals > 0) displayRegisterInfoIntervals = max(0F, displayRegisterInfoIntervals - updateInterval)
        }

        renderer.onRenderOverlay(graphics, tickCounter)
    }

    fun useTick(user: LocalPlayer, ticksInUse: Int, inUse: Boolean) {
        tryOpenScanGui(user, ticksInUse, inUse)
        if (scanningGuiOpen) tryScanPokemon(user)
        if (scannedSpecies !== null && scannableEntityInFocus !== null) user.playSound(CobblemonSounds.POKEDEX_SCAN_LOOP)
    }

    fun tryOpenScanGui(user: AbstractClientPlayer, ticksInUse: Int, inUse: Boolean) {
        if (inUse && ticksInUse == OPEN_SCANNER_BUFFER_TICKS) {
            scanningGuiOpen = true
            user.playSound(CobblemonSounds.POKEDEX_SCAN_OPEN)
        }
    }

    fun tryOpenInfoGui(user: LocalPlayer, ticksInUse: Int) {
        if (ticksInUse < OPEN_SCANNER_BUFFER_TICKS) {
            openPokedexGUI(user, type)
            infoGuiOpen = true
        }
    }

    fun openPokedexGUI(user: LocalPlayer, types: PokedexTypes = PokedexTypes.RED, speciesId: ResourceLocation? = null) {
        PokedexGUI.open(CobblemonClient.clientPokedexData, types, speciesId)
        user.playSound(CobblemonSounds.POKEDEX_OPEN)
    }

    fun attackKeyHeld(user: LocalPlayer, isHeld: Boolean) {
        if (isHeld && scannableEntityInFocus !== null && viewInfoTicks < VIEW_INFO_BUFFER_TICKS && scanningProgress == 0F) {
            viewInfoTicks++
            if (viewInfoTicks % 2 == 0) user.playSound(CobblemonSounds.POKEDEX_SCAN_LOOP)
        } else if (viewInfoTicks > 0 && viewInfoTicks < VIEW_INFO_BUFFER_TICKS) {
            viewInfoTicks--
        }
    }

    fun tryScanPokemon(user: LocalPlayer) {
        val targetScannableEntity = PokemonScanner.findScannableEntity(user, zoomLevel.toInt())
        if (targetScannableEntity != null) {
            if (targetScannableEntity != scannableEntityInFocus) {
                resetFocusedPokemonState()
                scannableEntityInFocus = targetScannableEntity
                val resolvedPokemon = scannableEntityInFocus!!.resolvePokemonScan()
                if(resolvedPokemon == null){
                    resetFocusedPokemonState()
                    return
                }

                // Check if Pokémon in focus is owned
                isPokemonInFocusOwned = CobblemonClient.clientPokedexData.getHighestKnowledgeForSpecies(resolvedPokemon) == PokedexEntryProgress.CAUGHT

                // Randomize info frames for render
                if (focusIntervals == 0F) {
                    availableInfoFrames = mutableListOf(null, null, null, null)
                    for (i in 0..2) {
                        var randomIndex = Random.nextInt(availableInfoFrames.size)
                        if (availableInfoFrames[randomIndex] !== null) randomIndex = availableInfoFrames.indexOfFirst { it == null }
                        availableInfoFrames.set(randomIndex, Random.nextBoolean())
                    }
                }

                // Check if Pokémon in focus is new or has new data
                newPokemonInfo = CobblemonClient.clientPokedexData.getNewInformation(resolvedPokemon)
                if (newPokemonInfo == PokedexLearnedInformation.NONE) user.playSound(CobblemonSounds.POKEDEX_SCAN_DETAIL)
                scannedSpecies = resolvedPokemon.species.resourceIdentifier //TODO: Why was this an IF/ELSE?
            }
        } else {
            resetFocusedPokemonState()
        }
    }

    fun onServerConfirmedRegister(packet: ServerConfirmedRegisterPacket) {
        if (scannedSpecies?.equals(packet.species) == true) {
            newPokemonInfo = packet.newInformation
            registerCompleted = true
            scannedSpecies = null
            scanningProgress = 0F
            val player = Minecraft.getInstance().player ?: return
            player.playSound(
                if (newPokemonInfo == PokedexLearnedInformation.SPECIES) CobblemonSounds.POKEDEX_SCAN_REGISTER_POKEMON
                else CobblemonSounds.POKEDEX_SCAN_REGISTER_ASPECT
            )
        }
    }

    fun resetFocusedPokemonState() {
        scannableEntityInFocus = null
        scannedSpecies = null
        viewInfoTicks = 0
        scanningProgress = 0F
        displayRegisterInfoIntervals = 0F
        registerCompleted = false
        isPokemonInFocusOwned = false
        newPokemonInfo = PokedexLearnedInformation.NONE
    }

    fun resetState(resetAnimationStates: Boolean = true) {
        scanningGuiOpen = false
        zoomLevel = 0F
        focusIntervals = 0F
        resetFocusedPokemonState()

        if (resetAnimationStates) {
            innerRingRotation = 0F
            transitionIntervals = 0F
            usageIntervals = 0F
        }
    }

    fun adjustZoom(verticalScrollAmount: Double) {
        zoomLevel = clamp(zoomLevel + verticalScrollAmount.toFloat(), 0F, ZOOM_STAGES.toFloat())
        val player = Minecraft.getInstance().player ?: return
        if (zoomLevel > 0F && zoomLevel < 10F) {
            player.playSound(CobblemonSounds.POKEDEX_SCAN_ZOOM_INCREMENT)
        }
    }

    // Higher multiplier = more zoomed out
    fun getFovMultiplier() = 1 - (zoomLevel / ZOOM_STAGES)
}