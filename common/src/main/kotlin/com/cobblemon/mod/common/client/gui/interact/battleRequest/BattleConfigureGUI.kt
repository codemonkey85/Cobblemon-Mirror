/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.interact.battleRequest

import com.cobblemon.mod.common.api.gui.ColourLibrary
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.battles.BattleFormat
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.battle.ClientBattleChallenge
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.net.messages.client.PlayerInteractOptionsPacket
import com.cobblemon.mod.common.net.messages.server.*
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation

class BattleConfigureGUI(
        private val packet: PlayerInteractOptionsPacket,
        private val activeRequest: ClientBattleChallenge? = null,
        private val activeTeamRequest: ClientBattleChallenge? = null
) : Screen(lang("challenge.request_battle_title")) {
    companion object {
        const val SIZE = 113
        private val backgroundResource = cobblemonResource("textures/gui/interact/request/battle_request.png")
        private val battleArrowsResource = cobblemonResource("textures/gui/interact/request/battle_request_arrows.png")
        private var requestButton: BattleRequestButton? = null
        var autoLevelNavButtonLeft: BattleRequestNavigationButton? = null
        var autoLevelNavButtonRight: BattleRequestNavigationButton? = null

        val battleRequestMap = mutableMapOf(
                Pair(
                        PlayerInteractOptionsPacket.Options.SINGLE_BATTLE,
                        BattleTypeTile(
                                option = PlayerInteractOptionsPacket.Options.SINGLE_BATTLE,
                                battleFormat = BattleFormat.GEN_9_SINGLES,
                                tileTexture = cobblemonResource("textures/gui/interact/request/battle_request_single.png"),
                                overlayTexture = cobblemonResource("textures/gui/interact/request/battle_request_overlay.png"),
                                title = lang("challenge.request_battle_title").bold(),
                                subTitle = lang("battle.types.singles").bold(),
                                buttonText = lang("challenge.challenge").bold(),
                                onRequest = { packet, battleFormat -> sendBattleRequest(battleFormat, packet) },
                                onResponse = { packet, accept -> sendBattleResponse(packet, accept) }
                        )
                ),
                Pair(
                        PlayerInteractOptionsPacket.Options.DOUBLE_BATTLE,
                        BattleTypeTile(
                                option = PlayerInteractOptionsPacket.Options.DOUBLE_BATTLE,
                                battleFormat = BattleFormat.GEN_9_DOUBLES,
                                tileTexture = cobblemonResource("textures/gui/interact/request/battle_request_double.png"),
                                overlayTexture = cobblemonResource("textures/gui/interact/request/battle_request_overlay.png"),
                                title = lang("challenge.request_battle_title").bold(),
                                subTitle = lang("battle.types.doubles").bold(),
                                buttonText = lang("challenge.challenge").bold(),
                                onRequest = { packet, battleFormat -> sendBattleRequest(battleFormat, packet) },
                                onResponse = { packet, accept -> sendBattleResponse(packet, accept) }
                        ),
                ),
                Pair(
                        PlayerInteractOptionsPacket.Options.TRIPLE_BATTLE,
                        BattleTypeTile(
                                option = PlayerInteractOptionsPacket.Options.TRIPLE_BATTLE,
                                battleFormat = BattleFormat.GEN_9_TRIPLES,
                                tileTexture = cobblemonResource("textures/gui/interact/request/battle_request_triple.png"),
                                overlayTexture = cobblemonResource("textures/gui/interact/request/battle_request_overlay.png"),
                                title = lang("challenge.request_battle_title").bold(),
                                subTitle = lang("battle.types.triples").bold(),
                                buttonText = lang("challenge.challenge").bold(),
                                onRequest = { packet, battleFormat -> sendBattleRequest(battleFormat, packet) },
                                onResponse = { packet, accept -> sendBattleResponse(packet, accept) }
                        ),
                ),
                Pair(
                        PlayerInteractOptionsPacket.Options.TEAM_REQUEST,
                        BattleTypeTile(
                                option = PlayerInteractOptionsPacket.Options.TEAM_REQUEST,
                                battleFormat = BattleFormat.GEN_9_MULTI,
                                tileTexture = cobblemonResource("textures/gui/interact/request/battle_request_multi.png"),
                                overlayTexture = cobblemonResource("textures/gui/interact/request/battle_request_multi_overlay_partner.png"),
                                title = lang("challenge.request_team_join_title").bold(),
                                subTitle = lang("battle.types.multi").bold(),
                                buttonText = lang("challenge.multi.invite").bold(),
                                color = ColourLibrary.SIDE_1_ALLY_BATTLE_COLOUR,
                                onRequest = { packet, battleFormat -> BattleTeamRequestPacket(packet.numericTargetId).sendToServer()  },
                                onResponse = { packet, accept -> BattleTeamResponsePacket(packet.numericTargetId, accept).sendToServer() }
                        ),
                ),
                Pair(
                        PlayerInteractOptionsPacket.Options.TEAM_LEAVE,
                        BattleTypeTile(
                                option = PlayerInteractOptionsPacket.Options.TEAM_LEAVE,
                                battleFormat = BattleFormat.GEN_9_MULTI,
                                tileTexture = cobblemonResource("textures/gui/interact/request/battle_request_multi.png"),
                                overlayTexture = cobblemonResource("textures/gui/interact/request/battle_request_multi_overlay_partner_leave.png"),
                                title = lang("challenge.request_team_leave_title").bold(),
                                subTitle = lang("battle.types.multi").bold(),
                                buttonText = lang("challenge.multi.leave").bold(),
                                color = ColourLibrary.SIDE_1_ALLY_BATTLE_COLOUR,
                                onRequest = { _, _ -> BattleTeamLeavePacket().sendToServer() },
                                onResponse = { _, _ -> Unit }
                        ),
                ),
                Pair(
                        PlayerInteractOptionsPacket.Options.MULTI_BATTLE,
                        BattleTypeTile(
                                option = PlayerInteractOptionsPacket.Options.MULTI_BATTLE,
                                battleFormat = BattleFormat.GEN_9_MULTI,
                                tileTexture = cobblemonResource("textures/gui/interact/request/battle_request_multi.png"),
                                overlayTexture = cobblemonResource("textures/gui/interact/request/battle_request_multi_overlay_opponent.png"),
                                title = lang("challenge.request_battle_title").bold(),
                                subTitle = lang("battle.types.multi").bold(),
                                buttonText = lang("challenge.challenge").bold(),
                                onRequest = { packet, battleFormat -> sendBattleRequest(battleFormat, packet) },
                                onResponse = { packet, accept -> sendBattleResponse(packet, accept) }
                        ),
                ),
                Pair(
                        PlayerInteractOptionsPacket.Options.ROYAL_BATTLE,
                        BattleTypeTile(
                                option = PlayerInteractOptionsPacket.Options.ROYAL_BATTLE,
                                battleFormat =  BattleFormat.GEN_9_ROYAL,
                                tileTexture = cobblemonResource("textures/gui/interact/request/battle_request_royal.png"),
                                overlayTexture = cobblemonResource("textures/gui/interact/request/battle_request_royal_overlay.png"),
                                title = lang("challenge.request_battle_title").bold(),
                                subTitle = lang("battle.types.freeforall").bold(),
                                buttonText = lang("challenge.challenge").bold(),
                                onRequest = { packet, battleFormat -> sendBattleRequest(battleFormat, packet) },
                                onResponse = { packet, accept -> sendBattleResponse(packet, accept) }
                        ),
                ),
        )

        val levelRulesetOption = mutableListOf(-1, 50, 100, 5)

        fun sendBattleRequest(battleFormat: BattleFormat, packet: PlayerInteractOptionsPacket) {
            BattleChallengePacket(packet.numericTargetId, packet.selectedPokemonId, battleFormat).sendToServer()
        }
        private fun sendBattleResponse(packet: PlayerInteractOptionsPacket, accept: Boolean) {
            BattleChallengeResponsePacket(packet.numericTargetId, packet.selectedPokemonId, accept).sendToServer()
        }
        private var options: List<PlayerInteractOptionsPacket.Options> = emptyList()
        private val blinkRate = 35
    }

    class BattleTypeTile(
            val option: PlayerInteractOptionsPacket.Options,
            val battleFormat: BattleFormat,
            val tileTexture: ResourceLocation?,
            val overlayTexture: ResourceLocation?,
            val title: MutableComponent,
            val subTitle: MutableComponent,
            val buttonText: MutableComponent,
            val color: Int = ColourLibrary.SIDE_1_BATTLE_COLOUR,
            val onRequest: (packet : PlayerInteractOptionsPacket, battleFormat: BattleFormat) -> Unit,
            val onResponse: (packet: PlayerInteractOptionsPacket, accept: Boolean) -> Unit,
    )

    private var currentPage = 0
        set(value) {
            // If value is within min and max
            field = if (value > 0 && value < options.count()) value
            // If value is less than zero, wrap around to end
            else if (value < 0) options.count() - 1
            // Else it's greater than max, wrap around to start
            else 0
        }

    private var levelRulesetOptionIndex = 0
        set(value) {
            // If value is within min and max
            field = if (value > 0 && value < levelRulesetOption.count()) value
            // If value is less than zero, wrap around to end
            else if (value < 0) levelRulesetOption.count() - 1
            // Else it's greater than max, wrap around to start
            else 0
        }

    private var targetName = Component.literal("Target Name").bold()
    private var hasRequest = false
    private var ticksPassed = 0F

    override fun renderBlurredBackground(delta: Float) { }
    override fun renderMenuBackground(context: GuiGraphics) { }


    override fun init() {

        val challenge = activeRequest
        targetName = Minecraft.getInstance().player?.level()?.getPlayerByUUID(packet.targetId)?.name?.plainCopy()?.bold() ?: targetName
        hasRequest = challenge != null || activeTeamRequest != null
        if (activeTeamRequest != null) {
            options = listOf(PlayerInteractOptionsPacket.Options.TEAM_REQUEST)
        } else if (challenge != null) {
            options = packet.options.keys.filter { packet.options[it] === PlayerInteractOptionsPacket.OptionStatus.AVAILABLE && battleRequestMap[it]?.battleFormat?.battleType?.name == challenge.battleFormat?.battleType?.name }
            val level = challenge.battleFormat?.getAdjustLevelRule() ?: -1
            levelRulesetOptionIndex = maxOf(0, levelRulesetOption.indexOf(level))
        } else {
            options = packet.options.keys.filter { packet.options[it] === PlayerInteractOptionsPacket.OptionStatus.AVAILABLE && battleRequestMap.containsKey(it) }.toList()
        }
        val (x, y) = getDimensions()


        if(hasRequest) {
            // Draw Accept/Decline buttons
            this.addRenderableWidget(
                    BattleResponseButton(
                            x + 22,
                            y + 99,
                            true
                    ) {
                        battleRequestMap[options[currentPage]]?.onResponse?.let { it1 -> it1(packet, true) }
                        closeGUI()
                    }
            )

            this.addRenderableWidget(
                    BattleResponseButton(
                            x + 56,
                            y + 99,
                            false
                    ) {
                        battleRequestMap[options[currentPage]]?.onResponse?.let { it1 -> it1(packet, false) }
                        closeGUI()
                    }
            )

        } else {
            // Draw Challenge button
            requestButton = BattleRequestButton(
                    x + 22,
                    y + 99,
                    lang("challenge.challenge"),
            ) {
                //TODO: add additional battle rules, otherwise this call feels pretty silly
                battleRequestMap[options[currentPage]]?.onRequest?.let { it1 ->
                    val battleFormat = battleRequestMap[options[currentPage]]?.battleFormat ?: BattleFormat.GEN_9_SINGLES
                    val level = levelRulesetOption[levelRulesetOptionIndex]
                    if (level > 0) {
                        battleFormat.ruleSet += "Adjust Level = $level"
                    }
                    it1(packet, battleFormat)
                }
                closeGUI()
            }
            requestButton?.let {
                this.addRenderableWidget(it)
            }

            // BattleFormat Selection buttons
            this.addRenderableWidget(
                    BattleRequestNavigationButton(
                    pX = x + 2,
                    pY = y + 30,
                    clickHeight = BattleRequestNavigationButton.HEIGHT * 6,
                    forward = false
                    ) { currentPage = (currentPage - 1) % options.count()
                        updateRequestButtonText()
                    }
            )

            this.addRenderableWidget(
                    BattleRequestNavigationButton(
                    pX = x + 106,
                    pY = y + 30,
                    clickHeight = BattleRequestNavigationButton.HEIGHT * 6,
                    forward = true
                    ) { currentPage = (currentPage + 1) % options.count()
                        updateRequestButtonText()
                    }
            )


            if (battleRequestMap[options[currentPage]]?.option != PlayerInteractOptionsPacket.Options.TEAM_REQUEST) {

                // Level Selection buttons
                autoLevelNavButtonLeft = BattleRequestNavigationButton(
                        pX = x + 15,
                        pY = y + 75,
                        forward = false
                ) { levelRulesetOptionIndex -= 1 }
                this.addRenderableWidget(autoLevelNavButtonLeft)

                autoLevelNavButtonRight = BattleRequestNavigationButton(
                        pX = x + 93,
                        pY = y + 75,
                        forward = true
                ) {levelRulesetOptionIndex += 1 }
                this.addRenderableWidget(autoLevelNavButtonRight)
            }
        }
    }

    fun updateRequestButtonText() {
        requestButton.let {
            if (it != null) {
                it.text = battleRequestMap[options[currentPage]]?.buttonText ?: lang("challenge.challenge")
            }
        }
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        ticksPassed += delta
        val battleTypeData = battleRequestMap[options[currentPage]] ?: return
        // Render background panel
        val (x, y) = getDimensions()
        blitk(
            matrixStack = context.pose(),
            texture = backgroundResource,
            x = x,
            y = y,
            width = 113,
            height = 126
        )

        // Render Screen title
        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = battleTypeData.title,
            x = x + 42,
            y = y + 2,
            centered = true,
            shadow = true
        )

        // Render battle type icon
        blitk(
            matrixStack = context.pose(),
            texture = battleTypeData.tileTexture,
            x = 2*(x + 113 / 2 - 190 / 4),
            y = 2*(y + 126 / 2 - 120 / 4 - 10),
            width = 190,
            height = 120,
            scale = 0.5F,
        )
        // Render battle type overlay
        if(ticksPassed % blinkRate < blinkRate / 2) {
            blitk(
                matrixStack = context.pose(),
                texture = battleTypeData.overlayTexture,
                x = 2*(x + 113 / 2 - 190 / 4),
                y = 2*(y + 126 / 2 - 120 / 4 - 10),
                width = 190,
                height = 120,
                scale = 0.5F,
            )
        }


        // Battle type display text
        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = battleTypeData.subTitle,
            x = x + 55,
            y = y + 14,
            centered = true,
            shadow = true
        )

        if (battleRequestMap[options[currentPage]]?.option != PlayerInteractOptionsPacket.Options.TEAM_REQUEST) {
            // Level Rule Display Text
            drawScaledText(
                    context = context,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = if (levelRulesetOption[levelRulesetOptionIndex] < 0) lang("challenge.rule.anything_goes").bold()
                    else lang("challenge.rule.level", levelRulesetOption[levelRulesetOptionIndex]).bold(),
                    x = x + 55,
                    y = y + 75,
                    centered = true,
                    shadow = true
            )

            autoLevelNavButtonLeft.let {
                if (it != null) {
                    it.active = true
                }
            }
            autoLevelNavButtonRight.let {
                if (it != null) {
                    it.active = true
                }
            }
        } else {
            autoLevelNavButtonLeft.let {
                if (it != null) {
                    it.active = false
                }
            }
            autoLevelNavButtonRight.let {
                if (it != null) {
                    it.active = false
                }
            }
        }

        // Opponent display name
        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = targetName,
            x = x + 55,
            y = y + 87,
            centered = true,
            shadow = true
        )

        // Draw colored decor arrows
        blitk(
            matrixStack = context.pose(),
            texture = battleArrowsResource,
            x = x + 1,
            y = y + 106,
            width = 111,
            height = 5,
            scale = 1F,
            red = ((battleTypeData.color shr 16) and 0b11111111) / 255F,
            green = ((battleTypeData.color shr 8) and 0b11111111) / 255F,
            blue = (battleTypeData.color and 0b11111111) / 255F,
        )

        super.render(context, mouseX, mouseY, delta)
    }

    private fun getDimensions(): Pair<Int, Int> {
        return Pair(
            (width - SIZE) / 2,
            (height - SIZE) / 2
        )
    }


    override fun isPauseScreen() = false

    private fun closeGUI() {
        Minecraft.getInstance().setScreen(null)
    }

}