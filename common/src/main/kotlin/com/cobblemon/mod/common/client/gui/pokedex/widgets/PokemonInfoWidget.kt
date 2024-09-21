/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pokedex.widgets

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.api.pokedex.entry.PokedexCosmeticVariation
import com.cobblemon.mod.common.api.pokedex.entry.PokedexForm
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.client.ClientMoLangFunctions.setupClient
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.TypeIcon
import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.HALF_OVERLAY_HEIGHT
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.HALF_OVERLAY_WIDTH
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.POKEMON_PORTRAIT_HEIGHT
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.POKEMON_PORTRAIT_WIDTH
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.PORTRAIT_POKE_BALL_HEIGHT
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.PORTRAIT_POKE_BALL_WIDTH
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.SCALE
import com.cobblemon.mod.common.client.gui.pokedex.ScaledButton
import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.drawScaledTextJustifiedRight
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.aspects.SHINY_ASPECT
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.util.FastColor
import org.joml.Quaternionf
import org.joml.Vector3f
import java.io.FileNotFoundException

class PokemonInfoWidget(val pX: Int, val pY: Int, val updateForm: (PokedexForm) -> (Unit)) : SoundlessWidget(
    pX,
    pY,
    POKEMON_PORTRAIT_WIDTH,
    POKEMON_PORTRAIT_HEIGHT,
    lang("ui.pokedex.pokemon_info"),
) {
    var currentEntry : PokedexEntry? = null

    var speciesName: MutableComponent = Component.translatable("")
    var speciesNumber: MutableComponent = "0000".text()

    val runtime = MoLangRuntime().setupClient().setup().also {
        it.environment.query.addFunction("get_pokedex") { CobblemonClient.clientPokedexData.struct }
    }

    var visibleForms = mutableListOf<PokedexForm>()
    var selectedFormIndex: Int = 0

    var type: Array<ElementalType?> = arrayOf(null, null)
    var shiny = false
    var maleRatio = -1F
    var gender: Gender = Gender.GENDERLESS

    var renderablePokemon : RenderablePokemon? = null

    var poseList: Array<PoseType> = arrayOf(PoseType.PROFILE, PoseType.WALK, PoseType.SLEEP)
    var selectedPoseIndex: Int = 0

    var state = FloatingState()
    var rotationY = 30F

    var ticksElapsed = 0
    var pokeBallBackgroundFrame = 0

    class VariationButtonWrapper(
        val parent: PokemonInfoWidget,
        val x: Float,
        val y: Float,
    ) {
        var variation = PokedexCosmeticVariation()

        var buttonStateIndex = 0
        private val button = ScaledButton(
            x,
            y,
            20,
            20,
            buttonNone,
            clickAction = { click() }
        )

        fun getWidget() = button

        fun show(variation: PokedexCosmeticVariation) {
            this.variation = variation
            button.resource = variation.icon
            buttonStateIndex = 0
            button.visible = true
            button.active = getPossibleAspects().size > 1
        }

        fun getPossibleAspects() = variation.aspects.filter { it == "" || it in (CobblemonClient.clientPokedexData.getSpeciesRecord(parent.currentEntry?.speciesId ?: return@filter false)?.getAspects() ?: emptySet()) }

        fun getMaxStateIndex() = getPossibleAspects().size - 1

        fun getAspect() = getPossibleAspects().elementAtOrNull(buttonStateIndex)

        fun hide() {
            button.visible = false
        }

        fun isVisible() = button.visible

        fun click() {
            buttonStateIndex++
            if (buttonStateIndex > getMaxStateIndex()) buttonStateIndex = 0
            parent.updateAspects()
        }
    }

    val possibleVariationButtonPositions = mutableListOf<Pair<Float, Float>>(
        114F to 27F,
        102F to 27F,
        90F to 27F,
        78F to 27F,
        66F to 27F,
        54F to 27F,
        42F to 27F,
        30F to 27F,
    )

    val variationButtons = mutableListOf<VariationButtonWrapper>()

    private val genderButton: ScaledButton = ScaledButton(
        pX + 114F,
        pY + 27F,
        20,
        20,
        resource = buttonGenderMale,
        clickAction = {
            if (maleRatio > 0 && maleRatio < 1) gender = if (gender == Gender.MALE) Gender.FEMALE else Gender.MALE
            updateAspects()
        }
    ).apply { addWidget(this) }

    private val shinyButton: ScaledButton = ScaledButton(
        (pX + 126F),
        (pY + 27F),
        20,
        20,
        buttonNone,
        clickAction = {
            val currentEntry = currentEntry ?: return@ScaledButton
            val currentForm = currentEntry.forms[selectedFormIndex]
            val seenShinyStates = CobblemonClient.clientPokedexData.getSeenShinyStates(currentEntry, currentForm)
            if (seenShinyStates.size > 1) {
                shiny = !shiny
                updateAspects()
            }
        }
    ).apply { addWidget(this) }

    private val cryButton: ScaledButton = ScaledButton(
        pX + 115F,
        pY + 83F,
        12,
        12,
        buttonCryArrow,
        silent = true,
        clickAction = { playCry() }
    ).apply { addWidget(this) }

    private val formLeftButton: ScaledButton = ScaledButton(
        pX + 18F,
        pY + 55.5F,
        10,
        16,
        arrowFormLeft,
        clickAction = { switchForm(false) }
    ).apply { addWidget(this) }

    private val formRightButton: ScaledButton = ScaledButton(
        pX + 116F,
        pY + 55.5F,
        10,
        16,
        arrowFormRight,
        clickAction = { switchForm(true) }
    ).apply { addWidget(this) }

    private val animationLeftButton: ScaledButton = ScaledButton(
        pX + 3.5F,
        pY + 83F,
        12,
        12,
        buttonAnimationArrowLeft,
        clickAction = { switchPose(false) }
    ).apply { addWidget(this) }

    private val animationRightButton: ScaledButton = ScaledButton(
        pX + 18.5F,
        pY + 83F,
        12,
        12,
        buttonAnimationArrowRight,
        clickAction = { switchPose(true) }
    ).apply { addWidget(this) }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val currentEntry = this.currentEntry ?: return

        val hasKnowledge = CobblemonClient.clientPokedexData.getKnowledgeForSpecies(currentEntry.speciesId) != PokedexEntryProgress.NONE
        val species = currentEntry.speciesId.let { PokemonSpecies.getByIdentifier(it) } ?: return

        val matrices = context.pose()

        blitk(
            matrixStack = matrices,
            texture = backgroundOverlay,
            x = pX, y = pY,
            width = HALF_OVERLAY_WIDTH,
            height = HALF_OVERLAY_HEIGHT
        )

        blitk(
            matrixStack = matrices,
            texture = pokeBallOverlay,
            x = pX + 15,
            y = pY + 25,
            width = PORTRAIT_POKE_BALL_WIDTH,
            height = PORTRAIT_POKE_BALL_HEIGHT,
            vOffset = (pokeBallBackgroundFrame * 109) + 20,
            textureHeight = 1744,
        )

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = speciesNumber.bold(),
            x = pX + 3,
            y = pY + 1,
            shadow = true
        )

        if (hasKnowledge) {
            drawScaledText(
                context = context,
                font = CobblemonResources.DEFAULT_LARGE,
                text = speciesName.bold(),
                x = pX + 26,
                y = pY + 1,
                colour = 0x606B6E
            )
        }

        // Caught icon
        if (isSelectedPokemonOwned()) {
            blitk(
                matrixStack = matrices,
                texture = caughtIcon,
                x = (pX + 129) / SCALE,
                y = (pY + 2) / SCALE,
                width = 14,
                height = 14,
                scale = SCALE
            )
        }

        // Platform
        blitk(
            matrixStack = matrices,
            texture = platformBase,
            x = pX + 13,
            y = pY + 69,
            width = 113,
            height = 24,
            textureHeight = 30
        )

        val platformType = getPlatformResource()
        if (platformType != null && isSelectedPokemonOwned()) {
            blitk(
                matrixStack = matrices,
                texture = platformType,
                x = pX + 13,
                y = pY + 66,
                width = 113,
                height = 27,
                textureHeight = 30
            )
        }

        blitk(
            matrixStack = matrices,
            texture = platformShadow,
            x = (pX + 47) / SCALE,
            y = (pY + 76.5F) / SCALE,
            width = 90,
            height = 20,
            scale = SCALE
        )

        if (hasKnowledge && renderablePokemon != null) {
            context.enableScissor(
                pX + 1,
                pY + portraitStartY,
                pX + POKEMON_PORTRAIT_WIDTH + 1,
                pY + portraitStartY + POKEMON_PORTRAIT_HEIGHT
            )

            matrices.pushPose()
            matrices.translate(
                pX.toDouble() + (POKEMON_PORTRAIT_WIDTH.toDouble() + 2)/2,
                pY.toDouble() + portraitStartY - 12,
                0.0
            )
            matrices.scale(scaleAmount, scaleAmount, scaleAmount)
            val rotationVector = Vector3f(13F, rotationY, 0F)

            drawProfilePokemon(
                renderablePokemon = renderablePokemon!!,
                poseType = poseList[selectedPoseIndex],
                matrixStack =  matrices,
                partialTicks = delta,
                rotation = Quaternionf().fromEulerXYZDegrees(rotationVector),
                state = state
            )

            matrices.popPose()
            context.disableScissor()
        } else {
            // Render question mark
            blitk(
                matrixStack = matrices,
                texture = platformUnknown,
                x = pX + 50.5,
                y = pY + 39,
                width = 39,
                height = 45
            )

            // Render unimplemented label
            if (!species.implemented) {
                drawScaledTextJustifiedRight(
                    context = context,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = lang("ui.pokedex.info.unimplemented").bold(),
                    x = pX + 136,
                    y = pY + 15,
                    shadow = true
                )
            }
        }

        // Render
        if (isSelectedPokemonOwned()) {
            val primaryType = type[0]
            val secondaryType = type[1]
            blitk(
                matrixStack = matrices,
                texture = if (secondaryType != null) typeBarDouble else typeBar,
                x = pX,
                y = pY + 14,
                width = HALF_OVERLAY_WIDTH,
                height = 25
            )

            if (primaryType != null) {
                TypeIcon(
                    x = pX + 3,
                    y = pY + 17,
                    type = primaryType,
                    secondaryType = secondaryType,
                ).render(context)
            }

            // Ensure elements are not hidden behind Pokémon render
            matrices.pushPose()
            matrices.translate(0.0, 0.0, 100.0)

            if (gender != Gender.GENDERLESS) {
                genderButton.render(context, mouseX, mouseY, delta)
            }

            shinyButton.render(context, mouseX, mouseY, delta)

            variationButtons.forEach { it.getWidget().render(context, mouseX, mouseY, delta) }

            // Cry
            blitk(
                matrixStack = matrices,
                texture = buttonCryBase,
                x = (pX + 114) / SCALE,
                y = (pY + 81) / SCALE,
                width = 44,
                height = 20,
                scale = SCALE
            )

            cryButton.render(context,mouseX, mouseY, delta)

            // Animation
            blitk(
                matrixStack = matrices,
                texture = buttonAnimationBase,
                x = (pX + 3) / SCALE,
                y = (pY + 81) / SCALE,
                width = 44,
                height = 20,
                scale = SCALE
            )

            animationLeftButton.render(context,mouseX, mouseY, delta)
            animationRightButton.render(context,mouseX, mouseY, delta)

            val showableForms = CobblemonClient.clientPokedexData.getEncounteredForms(currentEntry)
            // Forms
            if (showableForms.size > 1 && showableForms.size > selectedFormIndex) {
                formLeftButton.render(context,mouseX, mouseY, delta)
                formRightButton.render(context,mouseX, mouseY, delta)

                val form = showableForms[selectedFormIndex]
                drawScaledTextJustifiedRight(
                    context = context,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = lang("ui.pokedex.info.form.${form.displayForm.lowercase()}").bold(),
                    x = pX + 136,
                    y = pY + 15,
                    shadow = true
                )
            }
            matrices.popPose()
        } else {
            blitk(
                matrixStack = matrices,
                texture = typeBar,
                x = pX,
                y = pY + 14,
                width = HALF_OVERLAY_WIDTH,
                height = 25
            )
        }
        variationButtons.forEach {
            if(it.isVisible() && it.getWidget().isButtonHovered(mouseX, mouseY)) {
                context.fill(pX + 26, pY + 81, pX + 113, pY + 91, FastColor.ARGB32.color(150, 58, 150, 182))
                drawScaledText(context, CobblemonResources.DEFAULT_LARGE,
                    it.variation.displayName.asTranslated().bold(), pX + 70, pY + 82, scale = 0.9F, centered = true, shadow = true)
            }
        }
    }

    fun setDexEntry(pokedexEntry : PokedexEntry) {
        this.currentEntry = pokedexEntry
        this.renderablePokemon = null
        selectedPoseIndex = 0

        val species = PokemonSpecies.getByIdentifier(pokedexEntry.speciesId)
        val forms = pokedexEntry.forms

        if (species != null) {
            this.visibleForms = CobblemonClient.clientPokedexData.getEncounteredForms(pokedexEntry).toMutableList()
            this.speciesNumber = species.nationalPokedexNumber.toString().padStart(4, '0').text()
            this.speciesName = species.translatedName

            if (forms.isNotEmpty()) {
                formLeftButton.active = true
                formRightButton.active = true
            } else {
                formLeftButton.active = false
                formRightButton.active = false
            }
            selectedFormIndex = 0

            setupButtons(pokedexEntry, forms[selectedFormIndex])

            if (visibleForms.isNotEmpty()) {
                updateAspects()
            } else {
                type = arrayOf(null, null)
            }
        }
    }

    private fun setupButtons(pokedexEntry: PokedexEntry, pokedexForm: PokedexForm) {
        val species = PokemonSpecies.getByIdentifier(pokedexEntry.speciesId) ?: return
        variationButtons.forEach { removeWidget(it.getWidget()) }
        variationButtons.clear()

        val form = species.forms.find { it.name.equals(pokedexForm.displayForm, ignoreCase = true) } ?: species.standardForm
        maleRatio = form.maleRatio

        val seenGenders = CobblemonClient.clientPokedexData.getSeenGenders(pokedexEntry, pokedexForm)
        if (seenGenders.isEmpty()) {
            genderButton.visible = false
            genderButton.active = false
        } else {
            gender = seenGenders.first()
            genderButton.visible = true
            genderButton.active = seenGenders.size > 1
        }

        if (CobblemonClient.clientPokedexData.getHighestKnowledgeFor(pokedexEntry) == PokedexEntryProgress.NONE) {
            return
        }

        val startPosition = if (species.maleRatio == -1F) 0 else 1
        pokedexEntry.variations.forEachIndexed { index, variation ->
            val pos = possibleVariationButtonPositions[index + startPosition]
            val button = VariationButtonWrapper(this, pX + pos.first, pY + pos.second)
            button.show(variation)
            addWidget(button.getWidget())
            variationButtons.add(button)
        }

        val seenShinyStates = CobblemonClient.clientPokedexData.getSeenShinyStates(pokedexEntry, pokedexForm)
        shiny = seenShinyStates.contains("shiny")
        shinyButton.resource = if (shiny) buttonShiny else buttonNone
        shinyButton.active = seenShinyStates.size > 1
    }

    private fun updateType(species: Species, form: FormData) {
        type = arrayOf(form.primaryType, form.secondaryType)
    }

    private fun playCry() {
        state.addFirstAnimation(setOf("cry"))
    }

    private fun switchForm(nextIndex: Boolean) {
        if (nextIndex) {
            if (selectedFormIndex < visibleForms.lastIndex) selectedFormIndex++
            else selectedFormIndex = 0
        } else {
            if (selectedFormIndex > 0) selectedFormIndex--
            else selectedFormIndex = visibleForms.lastIndex
        }

        setupButtons(currentEntry!!, visibleForms[selectedFormIndex])
        updateAspects()
    }

    private fun switchPose(nextIndex: Boolean) {
        if (nextIndex) {
            if (selectedPoseIndex < poseList.lastIndex) selectedPoseIndex++
            else selectedPoseIndex = 0
        } else {
            if (selectedPoseIndex > 0) selectedPoseIndex--
            else selectedPoseIndex = poseList.lastIndex
        }
        updateAspects()
    }

    fun updateAspects() {
        if (visibleForms.isEmpty()) {
            return
        }

        genderButton.resource = if (gender == Gender.FEMALE) buttonGenderFemale else buttonGenderMale
        shinyButton.resource = if (shiny) buttonShiny else buttonNone

        val species = currentEntry?.speciesId?.let { PokemonSpecies.getByIdentifier(it) }
        if (species != null) {
            val formName = visibleForms[selectedFormIndex].displayForm
            val form = species.forms.find { it.name.equals(formName, ignoreCase = true) } ?: species.standardForm

            updateType(species, form)

            val aspects = mutableSetOf<String>()
            if (shiny) aspects.add(SHINY_ASPECT.aspect)

            if (gender == Gender.FEMALE) {
                aspects.add("female")
            } else if (gender == Gender.MALE) {
                aspects.add("male")
            }

            aspects.addAll(form.aspects)

            aspects.addAll(variationButtons.filter { it.isVisible() }.mapNotNull { it.getAspect() })

            renderablePokemon = RenderablePokemon(species, aspects).also { recalculatePoses(it) }

            updateForm.invoke(visibleForms[selectedFormIndex])
        }
    }

    fun recalculatePoses(renderablePokemon: RenderablePokemon) {
        val state = FloatingState()
        state.currentAspects = renderablePokemon.aspects
        val poser = PokemonModelRepository.getPoser(renderablePokemon.species.resourceIdentifier, renderablePokemon.aspects)
        state.currentModel = poser
        this.poseList = poser.poses
            .map { it.value.poseTypes.first() }
            .toSet()
            .filterNot { it in PoseType.SHOULDER_POSES } // Those don't play so goodly ykwim
            .sortedBy { it.ordinal }
            .toTypedArray()
    }

    fun getPlatformResource(): ResourceLocation? {
        val primaryType = type[0]
        if (primaryType != null) {
            return try {
                cobblemonResource("textures/gui/pokedex/platform_base_${primaryType.name}.png")
            } catch (error: FileNotFoundException) {
                null
            }
        }
        return null
    }

    fun tick() {
        ticksElapsed++

        // Calculate animation frame
        val delay = 3
        if (ticksElapsed % delay == 0) pokeBallBackgroundFrame++
        if (pokeBallBackgroundFrame == 16) pokeBallBackgroundFrame = 0
    }

    fun isWithinPortraitSpace(mouseX: Double, mouseY: Double): Boolean =
        mouseX.toInt() in pX + 15..(pX + 15 + PORTRAIT_POKE_BALL_WIDTH)
        && mouseY.toInt() in pY + 25..(pY + 25 + PORTRAIT_POKE_BALL_HEIGHT)

    private fun isSelectedPokemonOwned(): Boolean {
        return currentEntry?.let { CobblemonClient.clientPokedexData.getKnowledgeForSpecies(it.speciesId) } == PokedexEntryProgress.CAUGHT
    }

    fun playSound(soundEvent: SoundEvent) {
        Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(soundEvent, 1.0F))
    }

    companion object {
        val scaleAmount = 2F
        val portraitStartY = 25

        private val backgroundOverlay = cobblemonResource("textures/gui/pokedex/pokedex_screen_info_overlay.png")
        private val pokeBallOverlay = cobblemonResource("textures/gui/pokedex/pokedex_screen_poke_ball.png")

        private val platformUnknown = cobblemonResource("textures/gui/pokedex/platform_unknown.png")
        private val platformBase = cobblemonResource("textures/gui/pokedex/platform_base.png")
        private val platformShadow = cobblemonResource("textures/gui/pokedex/platform_shadow.png")

        private val arrowFormLeft = cobblemonResource("textures/gui/pokedex/forms_arrow_left.png")
        private val arrowFormRight = cobblemonResource("textures/gui/pokedex/forms_arrow_right.png")

        private val caughtIcon = cobblemonResource("textures/gui/pokedex/caught_icon.png")
        private val typeBar = cobblemonResource("textures/gui/pokedex/type_bar.png")
        private val typeBarDouble = cobblemonResource("textures/gui/pokedex/type_bar_double.png")

        private val buttonCryBase = cobblemonResource("textures/gui/pokedex/button_sound.png")
        private val buttonCryArrow = cobblemonResource("textures/gui/pokedex/button_sound_arrow.png")
        private val buttonAnimationBase = cobblemonResource("textures/gui/pokedex/button_animation.png")
        private val buttonAnimationArrowLeft = cobblemonResource("textures/gui/pokedex/button_animation_arrow_left.png")
        private val buttonAnimationArrowRight = cobblemonResource("textures/gui/pokedex/button_animation_arrow_right.png")
        private val buttonGenderMale = cobblemonResource("textures/gui/pokedex/button_male.png")
        private val buttonGenderFemale = cobblemonResource("textures/gui/pokedex/button_female.png")
        private val buttonNone = cobblemonResource("textures/gui/pokedex/button_none.png")
        private val buttonShiny = cobblemonResource("textures/gui/pokedex/button_shiny.png")
    }
}