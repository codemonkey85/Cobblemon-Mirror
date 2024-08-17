/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.keybind.keybinds

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUI
import com.cobblemon.mod.common.client.keybind.CobblemonKeyBinding
import com.cobblemon.mod.common.client.keybind.KeybindCategories
import com.cobblemon.mod.common.client.pokedex.PokedexTypes
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import net.minecraft.sound.SoundCategory

object PokedexBinding : CobblemonKeyBinding(
    "key.cobblemon.pokedex",
    InputConstants.Type.KEYSYM,
    InputConstants.KEY_K,
    KeybindCategories.COBBLEMON_CATEGORY
) {
    override fun onPress() {
        try {
            PokedexGUI.open(CobblemonClient.clientPokedexData, PokedexTypes.RED)
            MinecraftClient.getInstance().player?.playSoundToPlayer(CobblemonSounds.POKEDEX_SHOW, SoundCategory.PLAYERS, 1F, 1F)
        } catch (e: Exception) {
            Cobblemon.LOGGER.debug("Failed to open the Pokedex from the Pokedex keybind", e)
        }
    }
}