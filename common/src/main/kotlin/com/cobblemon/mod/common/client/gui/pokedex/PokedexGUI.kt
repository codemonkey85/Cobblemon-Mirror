/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pokedex

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.pokedex.CaughtCount
import com.cobblemon.mod.common.api.pokedex.Dexes
import com.cobblemon.mod.common.api.pokedex.SeenCount
import com.cobblemon.mod.common.api.pokedex.def.PokedexDef
import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry
import com.cobblemon.mod.common.api.pokedex.entry.PokedexForm
import com.cobblemon.mod.common.api.pokedex.filter.EntryFilter
import com.cobblemon.mod.common.api.pokedex.filter.SearchFilter
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.storage.player.client.ClientPokedexManager
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.ClientMoLangFunctions.setupClient
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.BASE_HEIGHT
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.BASE_WIDTH
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.HALF_OVERLAY_WIDTH
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.HEADER_BAR_HEIGHT
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.SCALE
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.TAB_ABILITIES
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.TAB_DESCRIPTION
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.TAB_DROPS
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.TAB_ICON_SIZE
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.TAB_SIZE
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.TAB_STATS
import com.cobblemon.mod.common.client.gui.pokedex.widgets.AbilitiesWidget
import com.cobblemon.mod.common.client.gui.pokedex.widgets.DescriptionWidget
import com.cobblemon.mod.common.client.gui.pokedex.widgets.DropsScrollingWidget
import com.cobblemon.mod.common.client.gui.pokedex.widgets.EntriesScrollingWidget
import com.cobblemon.mod.common.client.gui.pokedex.widgets.PokemonInfoWidget
import com.cobblemon.mod.common.client.gui.pokedex.widgets.SearchWidget
import com.cobblemon.mod.common.client.gui.pokedex.widgets.SizeWidget
import com.cobblemon.mod.common.client.gui.pokedex.widgets.StatsWidget
import com.cobblemon.mod.common.client.pokedex.PokedexTypes
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbility
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent

/**
 * Pokedex GUI
 *
 * @author JPAK
 * @since February 24, 2024
 */
class PokedexGUI private constructor(
    val type: PokedexTypes,
    val initSpecies: ResourceLocation?
): Screen(Component.translatable("cobblemon.ui.pokedex.title")) {
    var oldDragPosX = 0.0
    var canDragRender = false

    private var filteredPokedex: Collection<PokedexDef> = mutableListOf()
    private var seenCount = "0000"
    private var ownedCount = "0000"

    val runtime = MoLangRuntime().setupClient().setup().also {
        it.environment.query.addFunction("get_pokedex") { CobblemonClient.clientPokedexData.struct }
    }

    private var selectedEntry: PokedexEntry? = null
    private var selectedForm: PokedexForm? = null

    private var availableRegions = emptyList<ResourceLocation>()
    private var selectedRegionIndex = 0

    private lateinit var scrollScreen: EntriesScrollingWidget
    private lateinit var pokemonInfoWidget: PokemonInfoWidget
    private lateinit var searchWidget: SearchWidget
    private lateinit var regionSelectWidgetUp: ScaledButton
    private lateinit var regionSelectWidgetDown: ScaledButton

    private val tabButtons: MutableList<ScaledButton> = mutableListOf()

    lateinit var tabInfoElement: GuiEventListener
    var tabInfoIndex = TAB_DESCRIPTION

    override fun renderBlurredBackground(delta: Float) {}
    override fun renderMenuBackground(context: GuiGraphics) {}

    public override fun init() {
        super.init()
        clearWidgets()

        val pokedex = CobblemonClient.clientPokedexData

        availableRegions = Dexes.dexEntryMap.keys.toList()
        selectedRegionIndex = 0

        val ownedAmount = pokedex.getDexCalculatedValue(cobblemonResource("national"),  CaughtCount)
        ownedCount = ownedAmount.toString()
        while (ownedCount.length < 4) ownedCount = "0$ownedCount"

        seenCount = pokedex.getDexCalculatedValue(cobblemonResource("national"),  SeenCount).toString()
        while (seenCount.length < 4) seenCount = "0$seenCount"

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        //Info Widget
        if (::pokemonInfoWidget.isInitialized) removeWidget(pokemonInfoWidget)
        pokemonInfoWidget = PokemonInfoWidget(x + 180, y + 28) { newForm -> updateSelectedForm(newForm) }
        addRenderableWidget(pokemonInfoWidget)

        setUpTabs()

        //Tab Info Widget
        displaytabInfoElement(tabInfoIndex, false)

        if (::searchWidget.isInitialized) removeWidget(searchWidget)
        searchWidget = SearchWidget(x + 26, y + 28, HALF_OVERLAY_WIDTH, HEADER_BAR_HEIGHT, update = ::updateFilters)
        addRenderableWidget(searchWidget)

        if (::regionSelectWidgetUp.isInitialized) removeWidget(regionSelectWidgetUp)
        regionSelectWidgetUp = ScaledButton(
            buttonX = (x + 95).toFloat(),
            buttonY = (y + 14.5).toFloat(),
            buttonWidth = 8,
            buttonHeight = 6,
            scale = SCALE,
            resource = arrowUpIcon,
            clickAction = { updatePokedexRegion(false) }
        )
        addRenderableWidget(regionSelectWidgetUp)

        if (::regionSelectWidgetDown.isInitialized) removeWidget(regionSelectWidgetDown)
        regionSelectWidgetDown = ScaledButton(
            buttonX = (x + 95).toFloat(),
            buttonY = (y + 19.5).toFloat(),
            buttonWidth = 8,
            buttonHeight = 6,
            scale = SCALE,
            resource = arrowDownIcon,
            clickAction = { updatePokedexRegion(true) }
        )

        addRenderableWidget(regionSelectWidgetDown)

        updateFilters(true)
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val matrices = context.pose()
        renderBackground(context, mouseX, mouseY, delta)

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        // Render Base Resource
        blitk(
            matrixStack = matrices,
            texture = type.getTexturePath(),
            x = x, y = y,
            width = BASE_WIDTH,
            height = BASE_HEIGHT
        )

        blitk(
            matrixStack = matrices,
            texture = screenBackground,
            x = x, y = y,
            width = BASE_WIDTH,
            height = BASE_HEIGHT
        )

        // Region
        blitk(
            matrixStack = matrices,
            texture = globeIcon,
            x = (x + 26) / SCALE,
            y = (y + 15) / SCALE,
            width = 14,
            height = 14,
            scale = SCALE
        )

        // Region label
        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = Component.translatable("cobblemon.ui.pokedex.region.${availableRegions[selectedRegionIndex].path}").bold(),
            x = x + 36,
            y = y + 14,
            shadow = true
        )


        // Seen icon
        blitk(
            matrixStack = matrices,
            texture = caughtSeenIcon,
            x = (x + 252) / SCALE,
            y = (y + 15) / SCALE,
            width = 14,
            height = 14,
            vOffset = 0,
            textureHeight = 28,
            scale = SCALE
        )

        // Caught icon
        blitk(
            matrixStack = matrices,
            texture = caughtSeenIcon,
            x = (x + 290) / SCALE,
            y = (y + 15) / SCALE,
            width = 14,
            height = 14,
            vOffset = 14,
            textureHeight = 28,
            scale = SCALE
        )

        // Seen
        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = seenCount.text().bold(),
            x = x + 262,
            y = y + 14,
            shadow = true
        )

        // Owned
        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = ownedCount.text().bold(),
            x = x + 300,
            y = y + 14,
            shadow = true
        )

        // Show selected tab pointer if selected Pokémon has tab info to be shown
        if (selectedEntry?.let { selectedForm in CobblemonClient.clientPokedexData.getCaughtForms(it) } == true) {
            // Tab arrow
            blitk(
                matrixStack = matrices,
                texture = tabSelectArrow,
                x = (x + 198 + (25 * tabInfoIndex)) / SCALE,
                // (x + 191.5 + (22 * tabInfoIndex)) / SCALE for 6 tabs
                y = (y + 177) / SCALE,
                width = 12,
                height = 6,
                scale = SCALE
            )
        }

        super.render(context, mouseX, mouseY, delta)
    }

    override fun onClose() {
        playSound(CobblemonSounds.POKEDEX_CLOSE)
        super.onClose()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val canDisplayEntry = true //selectedForm?.unlockForms

        if (::pokemonInfoWidget.isInitialized
            && pokemonInfoWidget.isWithinPortraitSpace(mouseX, mouseY)
            && canDisplayEntry == true
        ) {
            canDragRender = true
            isDragging = true
            oldDragPosX = mouseX
            playSound(CobblemonSounds.POKEDEX_CLICK_SHORT)
        }
        return try {
            super.mouseClicked(mouseX, mouseY, button)
        } catch(_: ConcurrentModificationException) {
            false
        }
    }

    override fun mouseReleased(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        if (canDragRender) canDragRender = false
        if (isDragging) isDragging = false
        return super.mouseReleased(pMouseX, pMouseY, pButton)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (isDragging && canDragRender) {
            val dragOffsetY = (oldDragPosX - mouseX).toFloat()
            pokemonInfoWidget.rotationY = (((pokemonInfoWidget.rotationY + dragOffsetY) % 360 + 360) % 360)
        }
        oldDragPosX = mouseX
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun tick() {
        if (::pokemonInfoWidget.isInitialized) pokemonInfoWidget.tick()
    }

    fun updatePokedexRegion(nextIndex: Boolean) {
        if (nextIndex) {
            if (selectedRegionIndex < availableRegions.lastIndex) selectedRegionIndex++
            else selectedRegionIndex = 0
        } else {
            if (selectedRegionIndex > 0) selectedRegionIndex--
            else selectedRegionIndex = availableRegions.lastIndex
        }
        updateFilters()
    }

    fun updateFilters(init: Boolean = false) {
        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        filteredPokedex = listOfNotNull(Dexes.dexEntryMap[availableRegions[selectedRegionIndex]])

        // Scroll Screen
        if (::scrollScreen.isInitialized) removeWidget(scrollScreen)
        scrollScreen = EntriesScrollingWidget(x + 26, y + 39) { setSelectedEntry(it) }
        var entries = filteredPokedex
            .flatMap { it.getEntries() }

        for (filter in getFilters()) {
            entries = entries.filter { filter.test(it) }
        }

        scrollScreen.createEntries(entries)
        addRenderableWidget(scrollScreen)

        if (entries.isNotEmpty()) {
            if (init && initSpecies != null) {
                var entry = entries.firstOrNull { it.speciesId == initSpecies }
                if (entry == null) {
                    entry = entries.first()
                }
                setSelectedEntry(entry)
                scrollScreen.scrollAmount = (entries.indexOf(entry).toDouble() / entries.size.toDouble()) * scrollScreen.maxScroll
            } else {
                setSelectedEntry(entries.first())
            }
        }
    }

    fun getFilters(): Collection<EntryFilter> {
        val filters: MutableList<EntryFilter> = mutableListOf()

        filters.add(SearchFilter(CobblemonClient.clientPokedexData, searchWidget.value))

        return filters
    }

    fun setSelectedEntry(newSelectedEntry: PokedexEntry) {
        selectedEntry = newSelectedEntry
        selectedForm = CobblemonClient.clientPokedexData.getEncounteredForms(newSelectedEntry).firstOrNull()

        pokemonInfoWidget.setDexEntry(selectedEntry!!)
        displaytabInfoElement(tabInfoIndex)
    }

    fun setUpTabs() {
        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        if (tabButtons.isNotEmpty()) tabButtons.clear()

        for (i in tabIcons.indices) {
            tabButtons.add(ScaledButton(
                x + 197F + (i * 25F), // x + 190.5F + (i * 22F) for 6 tabs
                y + 181.5F,
                TAB_ICON_SIZE,
                TAB_ICON_SIZE,
                resource = tabIcons[i],
                clickAction = { if (canSelectTab(i)) displaytabInfoElement(i) }
            ))
        }

        for (tab in tabButtons) addRenderableWidget(tab)
    }

    fun displaytabInfoElement(tabIndex: Int, update: Boolean = true) {
        val showActiveTab = selectedEntry?.let { selectedForm in CobblemonClient.clientPokedexData.getCaughtForms(it) } == true
        if (tabButtons.isNotEmpty() && tabButtons.size > tabIndex) {
            tabButtons.forEachIndexed { index, tab -> tab.isWidgetActive = showActiveTab && index == tabIndex
            }
        }

        if (tabInfoIndex == TAB_ABILITIES && tabInfoElement is AbilitiesWidget) {
            removeWidget((tabInfoElement as AbilitiesWidget).leftButton)
            removeWidget((tabInfoElement as AbilitiesWidget).rightButton)
        }

        tabInfoIndex = tabIndex
        if (::tabInfoElement.isInitialized) removeWidget(tabInfoElement)

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        when (tabIndex) {
            TAB_DESCRIPTION -> {
                tabInfoElement = DescriptionWidget( x + 180, y + 135)
            }
            TAB_ABILITIES -> {
                tabInfoElement = AbilitiesWidget( x + 180, y + 135)
            }
            TAB_SIZE -> {
                tabInfoElement = SizeWidget( x + 180, y + 135)
            }
            TAB_STATS -> {
                tabInfoElement = StatsWidget( x + 180, y + 135)
            }
            TAB_DROPS -> {
                tabInfoElement = DropsScrollingWidget(x + 189, y + 135)
            }
        }
        val element = tabInfoElement
        if (element is Renderable && element is NarratableEntry) {
            addRenderableWidget(element)
        }
        if (update) updateTabInfoElement()
    }

    fun updateTabInfoElement() {
        val species = selectedEntry?.speciesId?.let { PokemonSpecies.getByIdentifier(it) }
        val formName = selectedForm?.displayForm
        val canDisplay = selectedEntry?.let { selectedForm in CobblemonClient.clientPokedexData.getCaughtForms(it) } == true
        val textToShowInDescription = mutableListOf<String>()

        if (canDisplay && species != null) {
            val form = species.forms.find { it.name.equals(formName, ignoreCase = true) } ?: species.standardForm
            when (tabInfoIndex) {
                TAB_DESCRIPTION -> {
                    textToShowInDescription.addAll(species.pokedex)
                    (tabInfoElement as DescriptionWidget).showPlaceholder = false
                }
                TAB_ABILITIES -> {
                    (tabInfoElement as AbilitiesWidget).abilitiesList = form.abilities.sortedBy { it is HiddenAbility }.map { ability -> ability.template }
                    (tabInfoElement as AbilitiesWidget).selectedAbilitiesIndex = 0
                    (tabInfoElement as AbilitiesWidget).setAbility()
                    (tabInfoElement as AbilitiesWidget).scrollAmount = 0.0

                    if ((tabInfoElement as AbilitiesWidget).abilitiesList.size > 1) {
                        addRenderableWidget((tabInfoElement as AbilitiesWidget).leftButton)
                        addRenderableWidget((tabInfoElement as AbilitiesWidget).rightButton)
                    }
                }
                TAB_SIZE -> {
                    if (::pokemonInfoWidget.isInitialized && pokemonInfoWidget.renderablePokemon != null) {
                        (tabInfoElement as SizeWidget).pokemonHeight = form.height
                        (tabInfoElement as SizeWidget).weight = form.weight
                        (tabInfoElement as SizeWidget).baseScale = form.baseScale
                        (tabInfoElement as SizeWidget).renderablePokemon = pokemonInfoWidget.renderablePokemon!!
                    }
                }
                TAB_STATS -> {
                    (tabInfoElement as StatsWidget).baseStats = form.baseStats
                }
                TAB_DROPS -> {
                    (tabInfoElement as DropsScrollingWidget).dropTable = form.drops
                    (tabInfoElement as DropsScrollingWidget).setEntries()
                }
//                TAB_MOVES -> {
//                    form.moves.getLevelUpMovesUpTo(100)
//                }
            }
        } else {
            if (tabInfoIndex != TAB_DESCRIPTION) displaytabInfoElement(TAB_DESCRIPTION)
            (tabInfoElement as DescriptionWidget).showPlaceholder = true
        }

        when (tabInfoIndex) {
            TAB_DESCRIPTION -> {
                (tabInfoElement as DescriptionWidget).setText(textToShowInDescription)
                (tabInfoElement as DescriptionWidget).scrollAmount = 0.0
            }
        }
    }

    fun updateSelectedForm(newForm: PokedexForm) {
        selectedForm = newForm
        displaytabInfoElement(tabInfoIndex)
    }

    fun canSelectTab(tabIndex: Int): Boolean {
        val selectedForm = this.selectedForm ?: return false
        val selectedEntry = this.selectedEntry ?: return false
        val encounteredForm = selectedForm in CobblemonClient.clientPokedexData.getEncounteredForms(selectedEntry)
        return encounteredForm && (tabIndex != tabInfoIndex)
    }

    override fun isPauseScreen(): Boolean = false

    fun playSound(soundEvent: SoundEvent) {
        Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(soundEvent, 1.0F))
    }

    companion object {
        private val screenBackground = cobblemonResource("textures/gui/pokedex/pokedex_screen.png")

        private val globeIcon = cobblemonResource("textures/gui/pokedex/globe_icon.png")
        private val caughtSeenIcon = cobblemonResource("textures/gui/pokedex/caught_seen_icon.png")

        private val arrowUpIcon = cobblemonResource("textures/gui/pokedex/arrow_up.png")
        private val arrowDownIcon = cobblemonResource("textures/gui/pokedex/arrow_down.png")


        private val tabSelectArrow = cobblemonResource("textures/gui/pokedex/select_arrow.png")
        private val tabIcons = arrayOf(
            cobblemonResource("textures/gui/pokedex/tab_info.png"),
            cobblemonResource("textures/gui/pokedex/tab_abilities.png"),
            cobblemonResource("textures/gui/pokedex/tab_size.png"),
            cobblemonResource("textures/gui/pokedex/tab_stats.png"),
            cobblemonResource("textures/gui/pokedex/tab_drops.png")
        )

        /**
         * Attempts to open this screen for a client.
         */
        fun open(pokedex: ClientPokedexManager, type: PokedexTypes, species: ResourceLocation? = null) {
            val mc = Minecraft.getInstance()
            val screen = PokedexGUI(type, species)
            mc.setScreen(screen)
        }
    }
}