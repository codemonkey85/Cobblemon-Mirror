/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.trade

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.ExitButton
import com.cobblemon.mod.common.client.gui.TypeIcon
import com.cobblemon.mod.common.client.gui.summary.Summary
import com.cobblemon.mod.common.client.gui.summary.widgets.common.reformatNatureTextIfMinted
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.trade.ClientTrade
import com.cobblemon.mod.common.net.messages.client.trade.TradeStartedPacket.TradeablePokemon
import com.cobblemon.mod.common.net.messages.server.trade.CancelTradePacket
import com.cobblemon.mod.common.net.messages.server.trade.ChangeTradeAcceptancePacket
import com.cobblemon.mod.common.net.messages.server.trade.UpdateTradeOfferPacket
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.MutableComponent
import net.minecraft.sounds.SoundEvent
import java.util.*

/**
 * Notes for Village:
 * When your chosen trade Pokémon is changed, [UpdateTradeOfferPacket].
 * When you change ready status, [ChangeTradeAcceptancePacket].
 * When you cancel the trade, just run this.close()
 */
class TradeGUI(
    val trade: ClientTrade,
    val traderId: UUID,
    val traderName: MutableComponent,
    val traderParty: MutableList<TradeablePokemon?>,
    val party: MutableList<TradeablePokemon?>
): Screen(lang("trade.gui.title")) {

    companion object {
        const val BASE_WIDTH = 293
        const val BASE_HEIGHT = 212
        const val BASE_BACKGROUND_WIDTH = 157
        const val BASE_BACKGROUND_HEIGHT = 85
        const val PARTY_SLOT_PADDING = 4
        const val PORTRAIT_SIZE = 78
        const val TYPE_SPACER_WIDTH = 134
        const val TYPE_SPACER_HEIGHT = 12
        const val TRADE_READY_WIDTH = 28
        const val TRADE_READY_HEIGHT = 6
        const val TRADE_READY_TOP_HEIGHT = 5
        const val READY_PROGRESS_LIMIT = 6
        const val SCALE = 0.5F

        private val baseResource = cobblemonResource("textures/gui/trade/trade_base.png")
        private val baseBackgroundResource = cobblemonResource("textures/gui/trade/trade_background.png")
        private val typeSpacerResource = cobblemonResource("textures/gui/trade/type_spacer.png")
        private val typeSpacerSingleResource = cobblemonResource("textures/gui/trade/type_spacer_single.png")
        private val typeSpacerDoubleResource = cobblemonResource("textures/gui/trade/type_spacer_double.png")
        private val tradeReadyResource = cobblemonResource("textures/gui/trade/trade_ready.png")
        private val tradeReadyTopResource = cobblemonResource("textures/gui/trade/trade_ready_top.png")
        private val opposingTradeReadyResource = cobblemonResource("textures/gui/trade/trade_ready_opposing.png")
        private val opposingTradeReadyTopResource = cobblemonResource("textures/gui/trade/trade_ready_top_opposing.png")
    }

    private var offeredPokemonModel: ModelWidget? = null
    private var opposingOfferedPokemonModel: ModelWidget? = null

    var offeredPokemon: Pokemon? = null
    var opposingOfferedPokemon: Pokemon? = null

    var ticksElapsed = 0
    var selectPointerOffsetY = 0
    var readyProgress = 0
    var selectPointerOffsetIncrement = false
    var protectiveTicks = 0

    init {
        trade.cancelEmitter.subscribe {
            super.onClose()
            // Maybe a sound
        }

        trade.completedEmitter.subscribe {
            val (pokemonId1, pokemonId2) = it
            val myTradedPokemon = party.find { it?.pokemonId == pokemonId1 }
            val theirTradedPokemon = traderParty.find { it?.pokemonId == pokemonId2 }
            if (myTradedPokemon == null || theirTradedPokemon == null) {
                CobblemonNetwork.sendToServer(CancelTradePacket())
                return@subscribe onClose()
            }
            val i1 = party.indexOf(myTradedPokemon)
            val i2 = traderParty.indexOf(theirTradedPokemon)
            party[i1] = theirTradedPokemon
            traderParty[i2] = myTradedPokemon
            offeredPokemon = null
            opposingOfferedPokemon = null
            ticksElapsed = 0
            readyProgress = 0
            trade.oppositeAcceptedMyOffer.set(false)
            setOfferedPokemon(pokemon = null, isOpposing = true)
            setOfferedPokemon(pokemon = null, isOpposing = false)
            rebuildWidgets()
            // Make a sound maybe
        }
        trade.oppositeOffer.subscribe { newOffer: Pokemon? ->
            setOfferedPokemon(pokemon = newOffer, isOpposing = true)
        }
        trade.myOffer.subscribe { myOffer: Pokemon? ->
            setOfferedPokemon(pokemon = myOffer)
        }
        trade.oppositeAcceptedMyOffer.subscribe {
            ticksElapsed = 0
            readyProgress = 0
        }
    }

    override fun init() {
        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        // Exit Button
        this.addRenderableWidget(
            ExitButton(pX = x + 265, pY = y + 6) {
                playSound(CobblemonSounds.GUI_CLICK)
                onClose()
                Minecraft.getInstance().setScreen(null)
            }
        )

        // Trade Button
        this.addRenderableWidget(
            TradeButton(
                x = x + 120,
                y = y + 119,
                parent = this,
                onPress = {
                    if (offeredPokemon != null && opposingOfferedPokemon != null && protectiveTicks <= 0) {
                        ticksElapsed = 0
                        if (trade.acceptedOppositeOffer) {
//                            trade.acceptedOppositeOffer = false;
                            readyProgress = 0
                            CobblemonNetwork.sendToServer(ChangeTradeAcceptancePacket(opposingOfferedPokemon!!.uuid, false))
                        } else {
//                            trade.acceptedOppositeOffer = true;
                            readyProgress = 0
                            CobblemonNetwork.sendToServer(ChangeTradeAcceptancePacket(opposingOfferedPokemon!!.uuid, true))
                        }
                    }
                }
            )
        )

        // Party
        for (partyIndex in 0..5) {
            var slotX = x + 9
            var slotY = y + 38

            if (partyIndex > 0) {
                val isEven = partyIndex % 2 == 0
                val offsetIndex = (partyIndex - (if (isEven) 0 else 1)) / 2
                val offsetX = if (isEven) 0 else (PartySlot.SIZE + PARTY_SLOT_PADDING)
                val offsetY = if (isEven) 0 else -8

                slotX += offsetX
                slotY += ((PartySlot.SIZE + PARTY_SLOT_PADDING) * offsetIndex) + offsetY
            }

            val pokemon = party[partyIndex]
            PartySlot(
                x = slotX,
                y = slotY,
                pokemon = pokemon,
                parent = this,
                onPress = {
                    if (!trade.acceptedOppositeOffer) {
                        val pk = if (offeredPokemon?.uuid == pokemon?.pokemonId) null else pokemon
                        CobblemonNetwork.sendToServer(UpdateTradeOfferPacket(pk?.let { it.pokemonId to PartyPosition(partyIndex) }))
                    }
                }
            ).also { widget -> addRenderableWidget(widget) }
        }

        // Opposing Party
        for (partyIndex in 0..5) {
            var slotX = x + 230
            var slotY = y + 30

            if (partyIndex > 0) {
                val isEven = partyIndex % 2 == 0
                val offsetIndex = (partyIndex - (if (isEven) 0 else 1)) / 2
                val offsetX = if (isEven) 0 else (PartySlot.SIZE + PARTY_SLOT_PADDING)
                val offsetY = if (isEven) 0 else 8

                slotX += offsetX
                slotY += ((PartySlot.SIZE + PARTY_SLOT_PADDING) * offsetIndex) + offsetY
            }

            PartySlot(
                x = slotX,
                y = slotY,
                pokemon = traderParty[partyIndex],
                parent = this,
                isOpposing = true,
                onPress = {}
            ).also { widget -> addRenderableWidget(widget) }
        }

        setOfferedPokemon(pokemon = offeredPokemon, isOpposing = false)
        setOfferedPokemon(pokemon = opposingOfferedPokemon, isOpposing = true)
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2
        val matrices = context.pose()

        // Render Background Resource
        val backgroundX = x + 68
        val backgroundY = y + 23
        blitk(
            matrixStack = matrices,
            texture = baseBackgroundResource,
            x = backgroundX,
            y = backgroundY,
            width = BASE_BACKGROUND_WIDTH,
            height = BASE_BACKGROUND_HEIGHT
        )

        // Render Model Portraits
        context.enableScissor(
            backgroundX,
            backgroundY,
            backgroundX + BASE_BACKGROUND_WIDTH,
            backgroundY +  BASE_BACKGROUND_HEIGHT
        )
        offeredPokemonModel?.render(context, mouseX, mouseY, delta)
        opposingOfferedPokemonModel?.render(context, mouseX, mouseY, delta)
        context.disableScissor()

        // Render Base Resource
        blitk(
            matrixStack = matrices,
            texture = baseResource,
            x = x, y = y,
            width = BASE_WIDTH,
            height = BASE_HEIGHT
        )

        renderInfoLabels(context, x, y)

        renderPokemonInfo(offeredPokemon, false, context, x, y, mouseX, mouseY)
        renderPokemonInfo(opposingOfferedPokemon, true, context, x, y, mouseX, mouseY)

        if (trade.acceptedOppositeOffer) {
            blitk(
                matrixStack = matrices,
                texture = tradeReadyResource,
                x = x + 85,
                y = y + 126,
                width = TRADE_READY_WIDTH,
                height = TRADE_READY_HEIGHT,
                vOffset = TRADE_READY_HEIGHT * readyProgress,
                textureHeight = TRADE_READY_HEIGHT * READY_PROGRESS_LIMIT
            )

            blitk(
                matrixStack = matrices,
                texture = tradeReadyTopResource,
                x = x + 112,
                y = y + 2,
                width = TRADE_READY_WIDTH,
                height = TRADE_READY_TOP_HEIGHT,
                vOffset = TRADE_READY_TOP_HEIGHT * readyProgress,
                textureHeight = TRADE_READY_TOP_HEIGHT * READY_PROGRESS_LIMIT
            )
        }

        if (trade.oppositeAcceptedMyOffer.get()) {
            blitk(
                matrixStack = matrices,
                texture = opposingTradeReadyResource,
                x = x + 180,
                y = y + 126,
                width = TRADE_READY_WIDTH,
                height = TRADE_READY_HEIGHT,
                vOffset = TRADE_READY_HEIGHT * readyProgress,
                textureHeight = TRADE_READY_HEIGHT * READY_PROGRESS_LIMIT
            )

            blitk(
                matrixStack = matrices,
                texture = opposingTradeReadyTopResource,
                x = x + 153,
                y = y + 2,
                width = TRADE_READY_WIDTH,
                height = TRADE_READY_TOP_HEIGHT,
                vOffset = TRADE_READY_TOP_HEIGHT * readyProgress,
                textureHeight = TRADE_READY_TOP_HEIGHT * READY_PROGRESS_LIMIT
            )
        }

        // Render usernames
        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = Minecraft.getInstance().user.name.text().bold(),
            x = x + 57,
            y = y - 10.5,
            centered = true,
            shadow = true
        )

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = traderName.bold(),
            x = x + 237,
            y = y - 10.5,
            centered = true,
            shadow = true
        )

        super.render(context, mouseX, mouseY, delta)

        // Item Tooltip
        if (offeredPokemon != null && !offeredPokemon!!.heldItemNoCopy().isEmpty) {
            val itemX = x + 50
            val itemY = y + 125
            val itemHovered = mouseX.toFloat() in (itemX.toFloat()..(itemX.toFloat() + 16)) && mouseY.toFloat() in (itemY.toFloat()..(itemY.toFloat() + 16))
            if (itemHovered) context.renderTooltip(Minecraft.getInstance().font, offeredPokemon!!.heldItemNoCopy(), mouseX, mouseY)
        }

        if (opposingOfferedPokemon != null && !opposingOfferedPokemon!!.heldItemNoCopy().isEmpty) {
            val itemX = x + 227
            val itemY = y + 125
            val itemHovered = mouseX.toFloat() in (itemX.toFloat()..(itemX.toFloat() + 16)) && mouseY.toFloat() in (itemY.toFloat()..(itemY.toFloat() + 16))
            if (itemHovered) context.renderTooltip(Minecraft.getInstance().font, opposingOfferedPokemon!!.heldItemNoCopy(), mouseX, mouseY)
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        when (keyCode) {
            InputConstants.KEY_ESCAPE -> {
//                playSound(CobblemonSounds.PC_OFF)
                CancelTradePacket().sendToServer()
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun tick() {
        ticksElapsed++
        if (protectiveTicks > 0) {
            protectiveTicks--
        }

        // Calculate select pointer offset
        val delayFactor = 3
        if (ticksElapsed % (2 * delayFactor) == 0) selectPointerOffsetIncrement = !selectPointerOffsetIncrement
        if (ticksElapsed % delayFactor == 0) selectPointerOffsetY += if (selectPointerOffsetIncrement) 1 else -1

        if (ticksElapsed % 6 == 0) readyProgress = if (readyProgress == READY_PROGRESS_LIMIT) 0 else readyProgress + 1
    }

    override fun onClose() {
        CobblemonNetwork.sendToServer(CancelTradePacket())
        super.onClose()
    }

    private fun setOfferedPokemon(pokemon: Pokemon?, isOpposing: Boolean = false) {
        protectiveTicks = 20
        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        if (isOpposing) {
            opposingOfferedPokemon = pokemon
            opposingOfferedPokemonModel = if (pokemon != null) ModelWidget(
                pX = x + 147,
                pY = y + 30,
                pWidth = PORTRAIT_SIZE,
                pHeight = PORTRAIT_SIZE,
                pokemon = pokemon.asRenderablePokemon(),
                baseScale = 2F,
                rotationY = 35F,
                offsetY = -10.0
            ) else null
            trade.acceptedOppositeOffer = false
        } else {
            offeredPokemon = pokemon
            offeredPokemonModel = if (pokemon != null) ModelWidget(
                pX = x + 68,
                pY = y + 30,
                pWidth = PORTRAIT_SIZE,
                pHeight = PORTRAIT_SIZE,
                pokemon = pokemon.asRenderablePokemon(),
                baseScale = 2F,
                rotationY = 325F,
                offsetY = -10.0
            ) else null
        }
    }

    private fun playSound(soundEvent: SoundEvent) {
        Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(soundEvent, 1.0F))
    }

    private fun renderPokemonInfo(pokemon: Pokemon?, isOpposing: Boolean, context: GuiGraphics, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        if (pokemon != null) {
            val matrices = context.pose()
            // Level
            val levelXOffset = if (isOpposing) 117 else 0
            drawScaledText(
                context = context,
                font = CobblemonResources.DEFAULT_LARGE,
                text = lang("ui.lv").bold(),
                x = x + 76 + levelXOffset,
                y = y + 1.5,
                shadow = true
            )

            drawScaledText(
                context = context,
                font = CobblemonResources.DEFAULT_LARGE,
                text = pokemon.level.toString().text().bold(),
                x = x + 89 + levelXOffset,
                y = y + 1.5,
                shadow = true
            )

            // Poké Ball
            val nameXOffset = if (isOpposing) 75 else 0
            val ballResource = cobblemonResource("textures/item/poke_balls/" + pokemon.caughtBall.name.path + ".png")
            blitk(
                matrixStack = context.pose(),
                texture = ballResource,
                x = (x + 73.5 + nameXOffset) / SCALE,
                y = (y + 12) / SCALE,
                width = 16,
                height = 16,
                scale = SCALE
            )

            drawScaledText(
                context = context,
                font = CobblemonResources.DEFAULT_LARGE,
                text = pokemon.getDisplayName().bold(),
                x = x + 82 + nameXOffset,
                y = y + 11.5,
                shadow = true
            )

            if (pokemon.gender != Gender.GENDERLESS) {
                val isMale = pokemon.gender == Gender.MALE
                val textSymbol = if (isMale) "♂".text().bold() else "♀".text().bold()
                drawScaledText(
                    context = context,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = textSymbol,
                    x = x + 139 + nameXOffset,
                    y = y + 11.5,
                    colour = if (isMale) 0x32CBFF else 0xFC5454,
                    shadow = true
                )
            }

            // Held Item
            val heldItem = pokemon.heldItemNoCopy()
            val itemX = x + (if (isOpposing) 227 else 50)
            val itemY = y + 125
            if (!heldItem.isEmpty) {
                val textRenderer = Minecraft.getInstance().font
                context.renderItem(heldItem, itemX, itemY)
                context.renderItemDecorations(textRenderer, heldItem, itemX, itemY)
            }

            // Shiny Icon
            if (pokemon.shiny) {
                blitk(
                    matrixStack = matrices,
                    texture = Summary.iconShinyResource,
                    x = (x + (if (isOpposing) 214.5 else 71.5)) / SCALE,
                    y = (y + 33.5) / SCALE,
                    width = 14,
                    height = 14,
                    scale = SCALE
                )
            }

            blitk(
                matrixStack = matrices,
                texture = if (pokemon.secondaryType != null) typeSpacerDoubleResource else typeSpacerSingleResource,
                x = (x + (if (isOpposing) 153 else 73)) / SCALE,
                y = (y + 113.5) / SCALE,
                width = TYPE_SPACER_WIDTH,
                height = TYPE_SPACER_HEIGHT,
                vOffset = if (isOpposing) TYPE_SPACER_HEIGHT else 0,
                textureHeight = TYPE_SPACER_HEIGHT * 2,
                scale = SCALE
            )

            TypeIcon(
                x = x + (if (isOpposing) 187 else 106),
                y = y + 112,
                type = pokemon.primaryType,
                secondaryType = pokemon.secondaryType,
                doubleCenteredOffset = 5F,
                secondaryOffset = 10F,
                small = true,
                centeredX = true
            ).render(context)

            val labelXOffset = if (isOpposing) 77 else 0

            // Nature
            val natureText = reformatNatureTextIfMinted(pokemon)
            drawScaledText(
                context = context,
                text = natureText,
                x = x + 108 + labelXOffset,
                y = y + 146.5,
                centered = true,
                shadow = true,
                scale = SCALE,
                pMouseX = mouseX,
                pMouseY = mouseY
            )

            // Ability
            drawScaledText(
                context = context,
                text = pokemon.ability.displayName.asTranslated(),
                x = x + 108 + labelXOffset,
                y = y + 163.5,
                centered = true,
                shadow = true,
                scale = SCALE
            )

            // Moves
            val moves = pokemon.moveSet.getMoves()
            for (i in moves.indices) {
                drawScaledText(
                    context = context,
                    text = moves[i].displayName,
                    x = x + 108 + labelXOffset,
                    y = y + 180.5 + (7 * i),
                    centered = true,
                    shadow = true,
                    scale = SCALE
                )
            }

            // IVs
            val ivXOffset = if (isOpposing) 205 else -13
            drawScaledText(
                context = context,
                text = pokemon.ivs.getOrDefault(Stats.HP).toString().text(),
                x = x + 60 + ivXOffset,
                y = y + 155.5,
                scale = SCALE,
                centered = true
            )

            drawScaledText(
                context = context,
                text = pokemon.ivs.getOrDefault(Stats.ATTACK).toString().text(),
                x = x + 60 + ivXOffset,
                y = y + 163.5,
                scale = SCALE,
                centered = true
            )

            drawScaledText(
                context = context,
                text = pokemon.ivs.getOrDefault(Stats.DEFENCE).toString().text(),
                x = x + 60 + ivXOffset,
                y = y + 171.5,
                scale = SCALE,
                centered = true
            )

            drawScaledText(
                context = context,
                text = pokemon.ivs.getOrDefault(Stats.SPECIAL_ATTACK).toString().text(),
                x = x + 60 + ivXOffset,
                y = y + 179.5,
                scale = SCALE,
                centered = true
            )

            drawScaledText(
                context = context,
                text = pokemon.ivs.getOrDefault(Stats.SPECIAL_DEFENCE).toString().text(),
                x = x + 60 + ivXOffset,
                y = y + 187.5,
                scale = SCALE,
                centered = true
            )

            drawScaledText(
                context = context,
                text = pokemon.ivs.getOrDefault(Stats.SPEED).toString().text(),
                x = x + 60 + ivXOffset,
                y = y + 195.5,
                scale = SCALE,
                centered = true
            )

            // EVs
            val evXOffset = if (isOpposing) 221 else 3
            drawScaledText(
                context = context,
                text = pokemon.evs.getOrDefault(Stats.HP).toString().text(),
                x = x + 60 + evXOffset,
                y = y + 155.5,
                scale = SCALE,
                centered = true
            )

            drawScaledText(
                context = context,
                text = pokemon.evs.getOrDefault(Stats.ATTACK).toString().text(),
                x = x + 60 + evXOffset,
                y = y + 163.5,
                scale = SCALE,
                centered = true
            )

            drawScaledText(
                context = context,
                text = pokemon.evs.getOrDefault(Stats.DEFENCE).toString().text(),
                x = x + 60 + evXOffset,
                y = y + 171.5,
                scale = SCALE,
                centered = true
            )

            drawScaledText(
                context = context,
                text = pokemon.evs.getOrDefault(Stats.SPECIAL_ATTACK).toString().text(),
                x = x + 60 + evXOffset,
                y = y + 179.5,
                scale = SCALE,
                centered = true
            )

            drawScaledText(
                context = context,
                text = pokemon.evs.getOrDefault(Stats.SPECIAL_DEFENCE).toString().text(),
                x = x + 60 + evXOffset,
                y = y + 187.5,
                scale = SCALE,
                centered = true
            )

            drawScaledText(
                context = context,
                text = pokemon.evs.getOrDefault(Stats.SPEED).toString().text(),
                x = x + 60 + evXOffset,
                y = y + 195.5,
                scale = SCALE,
                centered = true
            )
        } else {
            blitk(
                matrixStack = context.pose(),
                texture = typeSpacerResource,
                x = (x + (if (isOpposing) 153 else 73)) / SCALE,
                y = (y + 113.5) / SCALE,
                width = TYPE_SPACER_WIDTH,
                height = TYPE_SPACER_HEIGHT,
                vOffset = if (isOpposing) TYPE_SPACER_HEIGHT else 0,
                textureHeight = TYPE_SPACER_HEIGHT * 2,
                scale = SCALE
            )
        }
    }

    private fun renderInfoLabels(context: GuiGraphics, x: Int, y: Int) {
        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = lang("ui.party").bold(),
            x = x + 25.5,
            y = y + 7,
            centered = true
        )

        drawScaledText(
            context = context,
            text = lang("ui.info.nature").bold(),
            x = x + 108,
            y = y + 139.5,
            centered = true,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.info.ability").bold(),
            x = x + 108,
            y = y + 156.5,
            centered = true,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.moves").bold(),
            x = x + 108,
            y = y + 173.5,
            centered = true,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("held_item"),
            x = x + 22.5,
            y = y + 135.5,
            scale = SCALE,
            centered = true
        )

        drawScaledText(
            context = context,
            text = lang("ui.stats.ivs").bold(),
            x = x + 47,
            y = y + 147.5,
            centered = true,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.stats.evs").bold(),
            x = x + 62.5,
            y = y + 147.5,
            centered = true,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.stats.hp"),
            x = x + 9.5,
            y = y + 155.5,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.stats.atk"),
            x = x + 9.5,
            y = y + 163.5,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.stats.def"),
            x = x + 9.5,
            y = y + 171.5,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.stats.sp_atk"),
            x = x + 9.5,
            y = y + 179.5,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.stats.sp_def"),
            x = x + 9.5,
            y = y + 187.5,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.stats.speed"),
            x = x + 9.5,
            y = y + 195.5,
            scale = SCALE
        )

        // Opposing
        drawScaledText(
            context = context,
            text = lang("ui.info.nature").bold(),
            x = x + 185,
            y = y + 139.5,
            centered = true,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.info.ability").bold(),
            x = x + 185,
            y = y + 156.5,
            centered = true,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.moves").bold(),
            x = x + 185,
            y = y + 173.5,
            centered = true,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("held_item"),
            x = x + 270.5,
            y = y + 135.5,
            scale = SCALE,
            centered = true
        )

        drawScaledText(
            context = context,
            text = lang("ui.stats.ivs").bold(),
            x = x + 265,
            y = y + 148,
            centered = true,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.stats.evs").bold(),
            x = x + 280.5,
            y = y + 148,
            centered = true,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.stats.hp"),
            x = x + 227.5,
            y = y + 155.5,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.stats.atk"),
            x = x + 227.5,
            y = y + 163.5,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.stats.def"),
            x = x + 227.5,
            y = y + 171.5,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.stats.sp_atk"),
            x = x + 227.5,
            y = y + 179.5,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.stats.sp_def"),
            x = x + 227.5,
            y = y + 187.5,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.stats.speed"),
            x = x + 227.5,
            y = y + 195.5,
            scale = SCALE
        )
    }
}