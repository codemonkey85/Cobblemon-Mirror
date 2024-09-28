/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.keybind.keybinds

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.keybind.CobblemonKeyBinding
import com.cobblemon.mod.common.client.keybind.KeybindCategories
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object DebugKeybindings {
    val keybindings = listOf(
        ScaleUpKeybinding(),
        ScaleDownKeybinding(),
        TranslateLeftKeybinding(),
        TranslateRightKeybinding(),
        TranslateUpKeybinding(),
        TranslateDownKeybinding(),
        PrintModelSettingsKeybinding()
    )
    class ScaleUpKeybinding: CobblemonKeyBinding(
        "key.cobblemon.scaleportraitup",
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_EQUALS,
        KeybindCategories.COBBLEMON_DEBUG_CATEGORY
    ) {
        override fun onPress() {
            val currentlySelectedPokemon = CobblemonClient.storage.myParty.get(CobblemonClient.storage.selectedSlot)
            if (currentlySelectedPokemon != null) {
                val state = FloatingState().also { it.currentAspects = currentlySelectedPokemon.aspects }
                val model = VaryingModelRepository.getPoser(currentlySelectedPokemon.species.resourceIdentifier, state)
                model.portraitScale += 0.01F
            }

        }
    }
    class ScaleDownKeybinding: CobblemonKeyBinding(
        "key.cobblemon.scaleportraitdown",
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_MINUS,
        KeybindCategories.COBBLEMON_DEBUG_CATEGORY
    ) {
        override fun onPress() {
            val currentlySelectedPokemon = CobblemonClient.storage.myParty.get(CobblemonClient.storage.selectedSlot)
            if (currentlySelectedPokemon != null) {
                val state = FloatingState().also { it.currentAspects = currentlySelectedPokemon.aspects }
                val model = VaryingModelRepository.getPoser(currentlySelectedPokemon.species.resourceIdentifier, state)
                model.portraitScale -= 0.01F
            }

        }
    }

    class TranslateUpKeybinding: CobblemonKeyBinding(
        "key.cobblemon.translateportraitup",
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_I,
        KeybindCategories.COBBLEMON_DEBUG_CATEGORY
    ) {
        override fun onPress() {
            val currentlySelectedPokemon = CobblemonClient.storage.myParty.get(CobblemonClient.storage.selectedSlot)
            if (currentlySelectedPokemon != null) {
                val state = FloatingState().also { it.currentAspects = currentlySelectedPokemon.aspects }
                val model = VaryingModelRepository.getPoser(currentlySelectedPokemon.species.resourceIdentifier, state)
                model.portraitTranslation = model.portraitTranslation.add(0.0, -0.01, 0.0)
            }

        }
    }
    class TranslateDownKeybinding: CobblemonKeyBinding(
        "key.cobblemon.translateportraitdown",
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_K,
        KeybindCategories.COBBLEMON_DEBUG_CATEGORY
    ) {
        override fun onPress() {
            val currentlySelectedPokemon = CobblemonClient.storage.myParty.get(CobblemonClient.storage.selectedSlot)
            if (currentlySelectedPokemon != null) {
                val state = FloatingState().also { it.currentAspects = currentlySelectedPokemon.aspects }
                val model = VaryingModelRepository.getPoser(currentlySelectedPokemon.species.resourceIdentifier, state)
                model.portraitTranslation = model.portraitTranslation.add(0.0, 0.01, 0.0)
            }

        }
    }

    class TranslateLeftKeybinding: CobblemonKeyBinding(
        "key.cobblemon.translateportraitleft",
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_J,
        KeybindCategories.COBBLEMON_DEBUG_CATEGORY
    ) {
        override fun onPress() {
            val currentlySelectedPokemon = CobblemonClient.storage.myParty.get(CobblemonClient.storage.selectedSlot)
            if (currentlySelectedPokemon != null) {
                val state = FloatingState().also { it.currentAspects = currentlySelectedPokemon.aspects }
                val model = VaryingModelRepository.getPoser(currentlySelectedPokemon.species.resourceIdentifier, state)
                model.portraitTranslation = model.portraitTranslation.add(-0.01, 0.0, 0.0)
            }

        }
    }

    class TranslateRightKeybinding: CobblemonKeyBinding(
        "key.cobblemon.translateportraitright",
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_J,
        KeybindCategories.COBBLEMON_DEBUG_CATEGORY
    ) {
        override fun onPress() {
            val currentlySelectedPokemon = CobblemonClient.storage.myParty.get(CobblemonClient.storage.selectedSlot)
            if (currentlySelectedPokemon != null) {
                val state = FloatingState().also { it.currentAspects = currentlySelectedPokemon.aspects }
                val model = VaryingModelRepository.getPoser(currentlySelectedPokemon.species.resourceIdentifier, state)
                model.portraitTranslation = model.portraitTranslation.add(0.01, 0.0, 0.0)
            }
        }
    }

    class PrintModelSettingsKeybinding : CobblemonKeyBinding(
        "key.cobblemon.printmodelsettings",
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_PERIOD,
        KeybindCategories.COBBLEMON_DEBUG_CATEGORY
    ) {
        override fun onPress() {
            val currentlySelectedPokemon = CobblemonClient.storage.myParty.get(CobblemonClient.storage.selectedSlot)
            if (currentlySelectedPokemon != null) {
                val state = FloatingState().also { it.currentAspects = currentlySelectedPokemon.aspects }
                val model = VaryingModelRepository.getPoser(currentlySelectedPokemon.species.resourceIdentifier, state)
                Minecraft.getInstance().player?.sendSystemMessage(Component.literal("Portrait Translation: ${model.portraitTranslation}"))
                Minecraft.getInstance().player?.sendSystemMessage(Component.literal("Portrait Scale: ${model.portraitScale}"))
                Cobblemon.LOGGER.info("override var portraitTranslation = Vec3d(${model.portraitTranslation.x}, ${model.portraitTranslation.y}, ${model.portraitTranslation.z})")
                Cobblemon.LOGGER.info("override var portraitScale = ${model.portraitScale}F")
            }
        }
    }
}