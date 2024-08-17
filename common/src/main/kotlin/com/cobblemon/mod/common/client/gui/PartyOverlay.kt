/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.gui.drawPosablePortrait
import com.cobblemon.mod.common.api.text.darkGray
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.battle.BattleGUI
import com.cobblemon.mod.common.client.gui.toast.CobblemonToast
import com.cobblemon.mod.common.client.keybind.boundKey
import com.cobblemon.mod.common.client.keybind.keybinds.HidePartyBinding
import com.cobblemon.mod.common.client.keybind.keybinds.SummaryBinding
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.getDepletableRedGreen
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.client.render.renderScaledGuiItemIcon
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.toasts.AdvancementToast
import net.minecraft.client.gui.components.toasts.Toast
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.util.Mth

class PartyOverlay : Gui(Minecraft.getInstance()) {

    companion object {
        private const val SLOT_HEIGHT = 30
        private const val SLOT_WIDTH = 62
        private const val SLOT_SPACING = 4
        private const val PORTRAIT_DIAMETER = 21
        private const val SCALE = 0.5F

        private val partySlot = cobblemonResource("textures/gui/party/party_slot.png")
        private val partySlotActive = cobblemonResource("textures/gui/party/party_slot_active.png")
        private val partySlotFainted = cobblemonResource("textures/gui/party/party_slot_fainted.png")
        private val partySlotFaintedActive = cobblemonResource("textures/gui/party/party_slot_fainted_active.png")
        private val partySlotCollapsed = cobblemonResource("textures/gui/party/party_slot_collapsed.png")
        private val genderIconMale = cobblemonResource("textures/gui/party/party_gender_male.png")
        private val genderIconFemale = cobblemonResource("textures/gui/party/party_gender_female.png")
        private val portraitBackground = cobblemonResource("textures/gui/party/party_slot_portrait_background.png")
        val state = FloatingState()
    }

    private val screenExemptions: List<Class<out Screen>> = listOf(
        ChatScreen::class.java,
        BattleGUI::class.java
    )

    val starterToast = CobblemonToast(
        Mth.createInsecureUUID(),
        CobblemonItems.POKE_BALL.defaultInstance,
        lang("ui.starter.choose_starter_title", SummaryBinding.boundKey().displayName).red(),
        lang("ui.starter.choose_starter_description", SummaryBinding.boundKey().displayName).darkGray(),
        AdvancementToast.BACKGROUND_SPRITE,
        -1F,
        0
    )

    private var attachedToast = false

    fun resetAttachedToast() {
        val minecraft = Minecraft.getInstance()
        minecraft.toasts.clear()
        starterToast.nextVisibility = Toast.Visibility.SHOW
        attachedToast = false
    }

    override fun render(context: GuiGraphics, tickCounter: DeltaTracker) {
        val partialDeltaTicks = tickCounter.getGameTimeDeltaPartialTick(false)
        val minecraft = Minecraft.getInstance()

        // Hiding if a Screen is open and not exempt
        if (minecraft.screen != null) {
            if (!screenExemptions.contains(minecraft.screen?.javaClass as Class<out Screen>))
                return
        }
        if (minecraft.debugOverlay.showDebugScreen()) {
            return
        }
        // Hiding if toggled via Keybind
        if (HidePartyBinding.shouldHide) {
            return
        }

        val panelX = 0
        val party = CobblemonClient.storage.myParty
        val matrices = context.pose()
        if (party.slots.none { it != null }) {
            if (CobblemonClient.clientPlayerData.promptStarter &&
                !CobblemonClient.clientPlayerData.starterLocked &&
                !CobblemonClient.clientPlayerData.starterSelected &&
                !CobblemonClient.checkedStarterScreen
            ) {
                if (!this.attachedToast) {
                    // Adding starter toast on start and connect of client to server.
                    minecraft.toasts.addToast(this.starterToast)
                    this.attachedToast = true
                }
            }
            return // Don't render the stuff below if no Pokémon in party
        }

        val totalHeight = party.slots.size * SLOT_HEIGHT
        val midY = minecraft.window.guiScaledHeight / 2
        val startY = (midY - totalHeight / 2) - ((SLOT_SPACING * 5) / 2)
        val portraitFrameOffsetX = 22
        val portraitFrameOffsetY = 2
        val selectedSlot = CobblemonClient.storage.selectedSlot

        party.forEachIndexed { index, pokemon ->
            if (pokemon != null) {
                val selectedOffsetX = if (selectedSlot == index) 6 else 0
                val indexOffsetY = (SLOT_HEIGHT + SLOT_SPACING) * index
                val y = startY + indexOffsetY + portraitFrameOffsetY

                blitk(
                    matrixStack = matrices,
                    texture = portraitBackground,
                    x = panelX + portraitFrameOffsetX + selectedOffsetX,
                    y = y,
                    height = PORTRAIT_DIAMETER,
                    width = PORTRAIT_DIAMETER
                )

                context.enableScissor(
                    panelX + portraitFrameOffsetX + selectedOffsetX,
                    y,
                    panelX + portraitFrameOffsetX + selectedOffsetX + PORTRAIT_DIAMETER,
                    y + PORTRAIT_DIAMETER
                )

                matrices.pushPose()
                matrices.translate(
                    panelX + portraitFrameOffsetX + selectedOffsetX + PORTRAIT_DIAMETER / 2.0 - 1.0,
                    y.toDouble() - 12,
                    0.0
                )

                drawPosablePortrait(
                    identifier = pokemon.species.resourceIdentifier,
                    aspects = pokemon.aspects,
                    matrixStack = matrices,
                    partialTicks = 0F, // partialDeltaTicks, //Before you get any funny ideas about party animated pokemon, make sure they each get their own state instead of sharing.
                    contextScale = pokemon.form.baseScale,
                    repository = PokemonModelRepository,
                    state = state
                )
                matrices.popPose()
                context.disableScissor()
            }

            val selectedOffsetX = if (selectedSlot == index) 6 else 0
            val indexOffsetY = (SLOT_HEIGHT + SLOT_SPACING) * index
            val indexY = startY + indexOffsetY

            val slotTexture = if (pokemon != null)
                if (pokemon.isFainted())
                    if (selectedSlot == index) partySlotFaintedActive else partySlotFainted
                else
                    if (selectedSlot == index) partySlotActive else partySlot
            else partySlotCollapsed

            blitk(
                matrixStack = matrices,
                texture = slotTexture,
                x = panelX,
                y = indexY,
                height = SLOT_HEIGHT,
                width = SLOT_WIDTH
            )

            if (pokemon != null) {
                val stateIcon = pokemon.state.getIcon(pokemon)
                if (stateIcon != null) {
                    blitk(
                        matrixStack = matrices,
                        texture = stateIcon,
                        x = (panelX + selectedOffsetX + 8) / SCALE,
                        y = (indexY + portraitFrameOffsetY + 1) / SCALE,
                        height = 17,
                        width = 24,
                        scale = SCALE
                    )
                }

                drawScaledText(
                    context = context,
                    text = lang("ui.lv"),
                    x = panelX + selectedOffsetX + 6.5F,
                    y = indexY + 13.5,
                    scale = SCALE,
                    centered = true,
                    shadow = true
                )

                drawScaledText(
                    context = context,
                    text = pokemon.level.toString().text(),
                    x = panelX + selectedOffsetX + 6.5F,
                    y = indexY + 18,
                    scale = SCALE,
                    centered = true,
                    shadow = true
                )

                drawScaledText(
                    context = context,
                    text = pokemon.getDisplayName(),
                    x = panelX + selectedOffsetX + 2.5F,
                    y = indexY + 25,
                    scale = SCALE
                )

                if (pokemon.gender != Gender.GENDERLESS) {
                    blitk(
                        matrixStack = matrices,
                        texture = if (pokemon.gender == Gender.MALE) genderIconMale else genderIconFemale,
                        x = (panelX + selectedOffsetX + 40) / SCALE,
                        y = (indexY + 25)  / SCALE,
                        height = 7,
                        width = 5,
                        scale = SCALE
                    )
                }

                val hpRatio = pokemon.currentHealth / pokemon.hp.toFloat()
                val barHeightMax = 18
                val hpBarWidth = 2
                val hpBarHeight = hpRatio * barHeightMax

                val (red, green) = getDepletableRedGreen(hpRatio)

                blitk(
                    matrixStack = matrices,
                    texture = CobblemonResources.WHITE,
                    x = panelX + selectedOffsetX + 46,
                    y = indexY + (barHeightMax - hpBarHeight) + 5,
                    width = hpBarWidth,
                    height = hpBarHeight,
                    textureHeight = hpBarHeight / hpRatio,
                    vOffset = barHeightMax - hpBarHeight,
                    red = red * 0.8F,
                    green = green * 0.8F,
                    blue = 0.27F
                )

                val expForThisLevel = pokemon.experience - if (pokemon.level == 1) 0 else pokemon.experienceGroup.getExperience(pokemon.level)
                val expToNextLevel = pokemon.experienceGroup.getExperience(pokemon.level + 1) - pokemon.experienceGroup.getExperience(pokemon.level)
                val expRatio = expForThisLevel / expToNextLevel.toFloat()

                val expBarWidth = 1
                val expBarHeight = expRatio * barHeightMax

                blitk(
                    matrixStack = matrices,
                    texture = CobblemonResources.WHITE,
                    x = panelX + selectedOffsetX + 49,
                    y = indexY + (barHeightMax - expBarHeight) + 5,
                    width = expBarWidth,
                    height = expBarHeight,
                    textureHeight = expBarHeight / expRatio,
                    vOffset = barHeightMax - expBarHeight,
                    red = 0.2,
                    green = 0.65,
                    blue = 0.84
                )

                val ballIcon = cobblemonResource("textures/gui/ball/" + pokemon.caughtBall.name.path + ".png")
                val ballHeight = 22
                blitk(
                    matrixStack = matrices,
                    texture = ballIcon,
                    x = (panelX + selectedOffsetX + 43.5) / SCALE,
                    y = (indexY + 22) / SCALE,
                    height = ballHeight,
                    width = 18,
                    vOffset = if (stateIcon != null) ballHeight else 0,
                    textureHeight = ballHeight * 2,
                    scale = SCALE
                )

                val status = pokemon.status?.status
                if (!pokemon.isFainted() && status != null) {
                    val statusName = status.showdownName
                    blitk(
                        matrixStack = matrices,
                        texture = cobblemonResource("textures/gui/party/status_$statusName.png"),
                        x = panelX + selectedOffsetX + 51,
                        y = indexY + 8,
                        height = 14,
                        width = 4
                    )
                }

                // Held Item
                val heldItem = pokemon.heldItemNoCopy()
                if (!heldItem.isEmpty) {
                    renderScaledGuiItemIcon(
                        itemStack = heldItem,
                        x = panelX + selectedOffsetX + 12.0,
                        y = indexY + 14.0,
                        scale = 0.5,
                        matrixStack = matrices,
                        zTranslation = 0.0F
                    )
                }
            }
        }
    }
}