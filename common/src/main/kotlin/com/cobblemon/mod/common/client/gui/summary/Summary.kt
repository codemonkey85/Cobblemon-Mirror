/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.moves.MoveSet
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies.species
import com.cobblemon.mod.common.api.reactive.Observable.Companion.emitWhile
import com.cobblemon.mod.common.api.reactive.ObservableSubscription
import com.cobblemon.mod.common.api.scheduling.Schedulable
import com.cobblemon.mod.common.api.scheduling.SchedulingTracker
import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.ExitButton
import com.cobblemon.mod.common.client.gui.TypeIcon
import com.cobblemon.mod.common.client.gui.summary.widgets.EvolutionSelectScreen
import com.cobblemon.mod.common.client.gui.summary.widgets.ModelWidget
import com.cobblemon.mod.common.client.gui.summary.widgets.NicknameEntryWidget
import com.cobblemon.mod.common.client.gui.summary.widgets.PartyWidget
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.SummaryTab
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.info.InfoWidget
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves.MoveSwapScreen
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves.MovesWidget
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.stats.StatWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.net.messages.server.storage.party.MovePartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.party.SwapPartyPokemonPacket
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.InputUtil
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text

/**
 * The screen responsible for displaying various information regarding a Pokémon team.
 *
 * @property party The party that will be displayed.
 * @property editable Whether you shall be able to edit Pokémon with operations such as reordering their move set.
 *
 * @param selection The index the [party] will have as the base [selectedPokemon].
 */
class Summary private constructor(party: Collection<Pokemon?>, private val editable: Boolean, private val selection: Int): Screen(Text.translatable("cobblemon.ui.summary.title")), Schedulable {

    companion object {
        const val BASE_WIDTH = 331
        const val BASE_HEIGHT = 161
        private const val PORTRAIT_SIZE = 66
        private const val SCALE = 0.5F

        // Main Screen Index
        private const val INFO = 0
        private const val MOVES = 1
        private const val STATS = 2

        // Side Screen Index
        const val PARTY = 0
        const val MOVE_SWAP = 1
        const val EVOLVE = 2

        // Resources
        private val baseResource = cobblemonResource("textures/gui/summary/summary_base.png")
        private val portraitBackgroundResource = cobblemonResource("textures/gui/summary/portrait_background.png")
        private val typeSpacerResource = cobblemonResource("textures/gui/summary/type_spacer.png")
        private val typeSpacerDoubleResource = cobblemonResource("textures/gui/summary/type_spacer_double.png")
        private val sideSpacerResource = cobblemonResource("textures/gui/summary/summary_side_spacer.png")
        private val evolveButtonResource = cobblemonResource("textures/gui/summary/summary_evolve_button.png")
        val iconShinyResource = cobblemonResource("textures/gui/summary/icon_shiny.png")

        /**
         * Attempts to open this screen for a client.
         * If an exception is thrown this screen will not open.
         *
         * @param party The party to be displayed.
         * @param editable Whether you shall be able to edit Pokémon with operations such as reordering their move set.
         * @param selection The index to start as the selected party member, based on the [party].
         *
         * @throws IllegalArgumentException If the [party] is empty or contains more than 6 members.
         * @throws IndexOutOfBoundsException If the [selection] is not a possible index of [party].
         */
        fun open(party: Collection<Pokemon?>, editable: Boolean = true, selection: Int = 0) {
            val mc = MinecraftClient.getInstance()
            val screen = Summary(party, editable, selection)
            mc.setScreen(screen)
        }
    }

    override val schedulingTracker = SchedulingTracker()

    internal lateinit var selectedPokemon: Pokemon
    private lateinit var mainScreen: ClickableWidget
    lateinit var sideScreen: Element
    private lateinit var modelWidget: ModelWidget
    private lateinit var nicknameEntryWidget: NicknameEntryWidget
    private val summaryTabs = mutableListOf<SummaryTab>()
    private var mainScreenIndex = INFO
    var sideScreenIndex = PARTY
    private val party = ArrayList(party)

    /**
     * Initializes the Summary Screen
     */
    override fun init() {
        super.init()
        if (this.party.isEmpty()) {
            throw IllegalArgumentException("Summary UI cannot display zero Pokemon")
        }
        if (this.party.size > 6) {
            throw IllegalArgumentException("Summary UI cannot display more than six Pokemon")
        }
        val idealSelected = this.party[selection]
        if (idealSelected == null) {
            this.selectedPokemon = this.party.first { it != null }!!
        } else {
            this.selectedPokemon = idealSelected
        }
        this.listenToMoveSet()

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        displayMainScreen(mainScreenIndex)
        displaySideScreen(PARTY)

        // Evolve Button
        addDrawableChild(
                SummaryButton(
                        buttonX = x + 12F,
                        buttonY = y + 145F,
                        buttonWidth = 54,
                        buttonHeight = 15,
                        clickAction = {
                            momentarily {
                                displaySideScreen(if (sideScreenIndex == EVOLVE) PARTY else EVOLVE)
                            }
                        },
                        text = lang("ui.evolve"),
                        resource = evolveButtonResource,
                        renderRequirement = { selectedPokemon.evolutionProxy.client().isNotEmpty() },
                        clickRequirement = { selectedPokemon.evolutionProxy.client().isNotEmpty() }
                )
        )

        // Init Tabs
        summaryTabs.clear()
        summaryTabs.add(
                SummaryTab(
                        pX = x + 78,
                        pY = y - 1,
                        label = lang("ui.info")
                ) {
                    if (mainScreenIndex != INFO) {
                        displayMainScreen(INFO)
                        playSound(CobblemonSounds.GUI_CLICK)
                    }
                }
        )

        summaryTabs.add(
                SummaryTab(
                        pX = x + 119,
                        pY = y - 1,
                        label = lang("ui.moves")
                ) {
                    if (mainScreenIndex != MOVES) {
                        displayMainScreen(MOVES)
                        playSound(CobblemonSounds.GUI_CLICK)
                    }
                }
        )

        summaryTabs.add(
                SummaryTab(
                        pX = x + 160,
                        pY = y - 1,
                        label = lang("ui.stats")
                ) {
                    if (mainScreenIndex != STATS) {
                        displayMainScreen(STATS)
                        playSound(CobblemonSounds.GUI_CLICK)
                    }
                }
        )

        summaryTabs[mainScreenIndex].toggleTab()
        summaryTabs.forEach { addDrawableChild(it) }

        // Add Exit Button
        addDrawableChild(
                ExitButton(
                        pX = x + 302,
                        pY = y + 145
                ) {
                    playSound(CobblemonSounds.GUI_CLICK)
                    MinecraftClient.getInstance().setScreen(null)
                }
        )

        // Add Nickname Entry
        nicknameEntryWidget = NicknameEntryWidget(
                selectedPokemon,
                x = x + 12,
                y = (y + 14.5).toInt(),
                width = 50,
                height = 10,
                isParty = true,
                lang("ui.nickname")
        )
        focused = nicknameEntryWidget
        nicknameEntryWidget.isFocused = false
        addDrawableChild(nicknameEntryWidget)

        // Add Model Preview
        modelWidget = ModelWidget(
                pX = x + 6,
                pY = y + 32,
                pWidth = PORTRAIT_SIZE,
                pHeight = PORTRAIT_SIZE,
                pokemon = selectedPokemon.asRenderablePokemon(),
                baseScale = 2F,
                rotationY = 325F,
                offsetY = -10.0
        )
        addDrawable(this.modelWidget)
    }

    fun swapPartySlot(sourceIndex: Int, targetIndex: Int) {
        if (sourceIndex >= this.party.size || targetIndex >= this.party.size) {
            return
        }

        val sourcePokemon = this.party.getOrNull(sourceIndex)

        if (sourcePokemon != null) {
            val targetPokemon = this.party.getOrNull(targetIndex)

            val sourcePosition = PartyPosition(sourceIndex)
            val targetPosition = PartyPosition(targetIndex)

            val packet = targetPokemon?.let { SwapPartyPokemonPacket(it.uuid, targetPosition, sourcePokemon.uuid, sourcePosition) }
                    ?: MovePartyPokemonPacket(sourcePokemon.uuid, sourcePosition, targetPosition)
            packet.sendToServer()

            // Update change in UI
            this.party[targetIndex] = sourcePokemon
            this.party[sourceIndex] = targetPokemon
            displaySideScreen(PARTY)
            (sideScreen as PartyWidget).enableSwap()
        }
    }

    /**
     * Switches the selected PKM
     */
    fun switchSelection(newSelection: Int) {
        this.party.getOrNull(newSelection)?.let { this.selectedPokemon = it }
        moveSetSubscription?.unsubscribe()
        listenToMoveSet()
        displayMainScreen(mainScreenIndex)
        children().find { it is EvolutionSelectScreen }?.let(this::remove)
        if (this::modelWidget.isInitialized) {
            this.modelWidget.pokemon = selectedPokemon.asRenderablePokemon()
        }
        if (this::nicknameEntryWidget.isInitialized) {
            this.nicknameEntryWidget.setSelectedPokemon(selectedPokemon)
        }
    }

    private var moveSetSubscription: ObservableSubscription<MoveSet>? = null

    /**
     * Start observing the MoveSet of the current PKM for changes
     */
    private fun listenToMoveSet() {
        moveSetSubscription = selectedPokemon.moveSet.observable
                .pipe(emitWhile { isOpen() })
                .subscribe {
                    if (mainScreen is MovesWidget)
                        displayMainScreen(MOVES)
                }
    }

    /**
     * Returns if this Screen is open or not
     */
    private fun isOpen() = MinecraftClient.getInstance().currentScreen == this

    /**
     * Switch center screen
     */
    private fun displayMainScreen(screen: Int) {
        // Get stat tab index if currently displaying stat screen
        val subIndex = if (mainScreenIndex == STATS && mainScreen is StatWidget) (mainScreen as StatWidget).statTabIndex else 0

        mainScreenIndex = screen
        if (::mainScreen.isInitialized) remove(mainScreen)
        if (sideScreenIndex == MOVE_SWAP) displaySideScreen(PARTY)

        summaryTabs.forEachIndexed { index, item ->
            if (index == screen) item.toggleTab() else item.toggleTab(false)
        }

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        when (screen) {
            INFO -> {
                mainScreen = InfoWidget(
                        pX = x + 77,
                        pY = y + 12,
                        pokemon = this.selectedPokemon
                )
            }

            MOVES -> {
                mainScreen = MovesWidget(
                        pX = x + 77,
                        pY = y + 12,
                        summary = this
                )
            }

            STATS -> {
                mainScreen = StatWidget(
                        pX = x + 77,
                        pY = y + 12,
                        pokemon = this.selectedPokemon,
                        tabIndex = subIndex
                )
            }
        }
        addDrawableChild(mainScreen)
    }

    /**
     * Switch right screen
     */
    fun displaySideScreen(screen: Int, move: Move? = null) {
        sideScreenIndex = screen
        if (::sideScreen.isInitialized) remove(sideScreen)

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        when (screen) {
            PARTY -> {
                sideScreen = PartyWidget(
                        pX = x + 216,
                        pY = y + 24,
                        isParty = selectedPokemon in CobblemonClient.storage.myParty,
                        summary = this,
                        partyList = this.party
                )
            }

            MOVE_SWAP -> {
                val movesWidget = mainScreen
                if (movesWidget is MovesWidget && move != null) {
                    sideScreen = MoveSwapScreen(
                            x + 216,
                            y + 22,
                            movesWidget = movesWidget,
                            replacedMove = move
                    ).also { switchPane ->
                        val pokemon = selectedPokemon
                        pokemon.allAccessibleMoves
                                .filter { template -> pokemon.moveSet.none { it.template == template } }
                                .map { template ->
                                    val benched = pokemon.benchedMoves.find { it.moveTemplate == template }
                                    MoveSwapScreen.MoveSlot(switchPane, template, benched?.ppRaisedStages
                                            ?: 0, pokemon)
                                }
                                .forEach { switchPane.addEntry(it) }
                    }
                }
            }

            EVOLVE -> {
                sideScreen = EvolutionSelectScreen(
                        x + 216,
                        y + 22,
                        pokemon = selectedPokemon
                )
            }
        }
        val element = sideScreen
        if (element is Drawable && element is Selectable) {
            addDrawableChild(element)
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        //this.renderBackground(context)

        schedulingTracker.update(delta / 20F)

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2
        val matrices = context.matrices

        // Render Portrait Background
        blitk(
                matrixStack = matrices,
                texture = portraitBackgroundResource,
                x = x + 6,
                y = y + 32,
                width = PORTRAIT_SIZE,
                height = PORTRAIT_SIZE
        )

        //modelWidget.render(context, pMouseX, pMouseY, pPartialTicks)

        // Render Base Resource
        blitk(
                matrixStack = matrices,
                texture = baseResource,
                x = x,
                y = y,
                width = BASE_WIDTH,
                height = BASE_HEIGHT
        )

        // Status
        val status = selectedPokemon.status?.status
        if (selectedPokemon.isFainted() || status != null) {
            val statusName = if (selectedPokemon.isFainted()) "fnt" else status?.showdownName
            blitk(
                    matrixStack = matrices,
                    texture = cobblemonResource("textures/gui/battle/battle_status_$statusName.png"),
                    x = x + 34,
                    y = y + 4,
                    height = 7,
                    width = 39,
                    uOffset = 35,
                    textureWidth = 74
            )

            blitk(
                    matrixStack = matrices,
                    texture = cobblemonResource("textures/gui/summary/status_trim.png"),
                    x = x + 34,
                    y = y + 5,
                    height = 6,
                    width = 3
            )

            drawScaledText(
                    context = context,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = lang("ui.status.$statusName").bold(),
                    x = x + 39,
                    y = y + 3
            )
        }

        // Poké Ball
        val ballResource = cobblemonResource("textures/item/poke_balls/" + selectedPokemon.caughtBall.name.path + ".png")
        blitk(
                matrixStack = matrices,
                texture = ballResource,
                x = (x + 3.5) / SCALE,
                y = (y + 15) / SCALE,
                width = 16,
                height = 16,
                scale = SCALE
        )

        if (selectedPokemon.gender != Gender.GENDERLESS) {
            val isMale = selectedPokemon.gender == Gender.MALE
            val textSymbol = if (isMale) "♂".text().bold() else "♀".text().bold()
            drawScaledText(
                    context = context,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = textSymbol,
                    x = x + 69, // 64 when tag icon is implemented
                    y = y + 14.5,
                    colour = if (isMale) 0x32CBFF else 0xFC5454,
                    shadow = true
            )
        }

        drawScaledText(
                context = context,
                font = CobblemonResources.DEFAULT_LARGE,
                text = lang("ui.lv").bold(),
                x = x + 6,
                y = y + 4.5,
                shadow = true
        )

        drawScaledText(
                context = context,
                font = CobblemonResources.DEFAULT_LARGE,
                text = selectedPokemon.level.toString().text().bold(),
                x = x + 19,
                y = y + 4.5,
                shadow = true
        )

        // Shiny Icon
        if (selectedPokemon.shiny) {
            blitk(
                    matrixStack = matrices,
                    texture = iconShinyResource,
                    x = (x + 62.5) / SCALE,
                    y = (y + 33.5) / SCALE,
                    width = 16,
                    height = 16,
                    scale = SCALE
            )
        }

        // Type Icon(s)
        blitk(
                matrixStack = matrices,
                texture = if (selectedPokemon.secondaryType != null) typeSpacerDoubleResource else typeSpacerResource,
                x = (x + 5.5) / SCALE,
                y = (y + 126) / SCALE,
                width = 134,
                height = 24,
                scale = SCALE
        )

        // Held Item
        val heldItem = selectedPokemon.heldItemNoCopy()
        val itemX = x + 3
        val itemY = y + 104
        if (!heldItem.isEmpty) {
            context.drawItem(heldItem, itemX, itemY)
            context.drawItemInSlot(MinecraftClient.getInstance().textRenderer, heldItem, itemX, itemY)
        }

        drawScaledText(
                context = context,
                text = lang("held_item"),
                x = x + 27,
                y = y + 114.5,
                scale = SCALE
        )

        TypeIcon(
                x = x + 39,
                y = y + 123,
                type = selectedPokemon.primaryType,
                secondaryType = selectedPokemon.secondaryType,
                centeredX = true
        ).render(context)

        blitk(
                matrixStack = matrices,
                texture = sideSpacerResource,
                x = (x + 217) / SCALE,
                y = (y + 141) / SCALE,
                width = 144,
                height = 14,
                scale = SCALE
        )

        // Render all added Widgets
        super.render(context, mouseX, mouseY, delta)

        // Render Item Tooltip
        if (!heldItem.isEmpty) {
            val itemHovered = mouseX.toFloat() in (itemX.toFloat()..(itemX.toFloat() + 16)) && mouseY.toFloat() in (itemY.toFloat()..(itemY.toFloat() + 16))
            if (itemHovered) context.drawItemTooltip(MinecraftClient.getInstance().textRenderer, heldItem, mouseX, mouseY)
        }
    }

    /**
     * Whether this Screen should pause the Game in SinglePlayer
     */
    override fun shouldPause(): Boolean = false

    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        return children().any { it.mouseScrolled(mouseX, mouseY, amount) }
    }

    /*
    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return children().any { it.mouseClicked(mouseX, mouseY, button) }
    }
     */

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (sideScreenIndex == MOVE_SWAP || sideScreenIndex == EVOLVE) sideScreen.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if ((keyCode == InputUtil.GLFW_KEY_ENTER || keyCode == InputUtil.GLFW_KEY_KP_ENTER)
            && this::nicknameEntryWidget.isInitialized
            && this.nicknameEntryWidget.isFocused
        ) {
            this.focused = null
        }
        if (Cobblemon.config.enableDebugKeys) {
            val model = PokemonModelRepository.getPoser(selectedPokemon.species.resourceIdentifier, selectedPokemon.aspects)
            if (keyCode == InputUtil.GLFW_KEY_UP) {
                model.profileTranslation = model.profileTranslation.add(0.0, -0.01, 0.0)
            }
            if (keyCode == InputUtil.GLFW_KEY_DOWN) {
                model.profileTranslation = model.profileTranslation.add(0.0, 0.01, 0.0)
            }
            if (keyCode == InputUtil.GLFW_KEY_LEFT) {
                model.profileTranslation = model.profileTranslation.add(-0.01, 0.0, 0.0)
            }
            if (keyCode == InputUtil.GLFW_KEY_RIGHT) {
                model.profileTranslation = model.profileTranslation.add(0.01, 0.0, 0.0)
            }
            if (keyCode == InputUtil.GLFW_KEY_EQUAL) {
                model.profileScale += 0.01F
            }
            if (keyCode == InputUtil.GLFW_KEY_MINUS) {
                model.profileScale -= 0.01F
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    fun playSound(soundEvent: SoundEvent) {
        MinecraftClient.getInstance().soundManager.play(PositionedSoundInstance.master(soundEvent, 1.0F))
    }

    override fun close() {
        if (Cobblemon.config.enableDebugKeys) {
            val model = PokemonModelRepository.getPoser(selectedPokemon.species.resourceIdentifier, selectedPokemon.aspects)
            MinecraftClient.getInstance().player?.sendMessage(Text.of("Profile Translation: ${model.profileTranslation}"))
            MinecraftClient.getInstance().player?.sendMessage(Text.of("Profile Scale: ${model.profileScale}"))
            Cobblemon.LOGGER.info("override var profileTranslation = Vec3d(${model.profileTranslation.x}, ${model.profileTranslation.y}, ${model.profileTranslation.z})")
            Cobblemon.LOGGER.info("override var profileScale = ${model.profileScale}F")
        }
        super.close()
    }
}
