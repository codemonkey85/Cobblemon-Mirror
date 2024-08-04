/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pokedex.widgets

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.api.pokedex.SpeciesPokedexEntry
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.api.types.ElementalType
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
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.pokedex.DexPokemonData
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.aspects.SHINY_ASPECT
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvent
import net.minecraft.text.MutableComponent
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.joml.Quaternionf
import org.joml.Vector3f
import java.io.FileNotFoundException

class PokemonInfoWidget(val pX: Int, val pY: Int, val updateForm: (FormData) -> (Unit)) : SoundlessWidget(
    pX,
    pY,
    POKEMON_PORTRAIT_WIDTH,
    POKEMON_PORTRAIT_HEIGHT,
    lang("ui.pokedex.pokemon_info"),
) {
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

    var dexPokemonData : DexPokemonData? = null
    var speciesPokedexEntry : SpeciesPokedexEntry? = null
    var speciesName: MutableComponent = Text.translatable("")
    var speciesNumber: MutableComponent = "0000".text()
    var formsList: MutableList<String> = mutableListOf("normal")
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
            shiny = !shiny
            updateAspects()
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
        if (dexPokemonData == null || renderablePokemon == null) return

        val hasKnowledge = speciesPokedexEntry != null
        val species = dexPokemonData!!.species

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

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = speciesName.bold(),
            x = pX + 26,
            y = pY + 1,
            colour = 0x606B6E
        )

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

        if (hasKnowledge) {
            context.enableScissor(
                pX + 1,
                pY + portraitStartY,
                pX + POKEMON_PORTRAIT_WIDTH + 1,
                pY + portraitStartY + POKEMON_PORTRAIT_HEIGHT
            )

            matrices.push()
            matrices.translate(
                pX.toDouble() + (POKEMON_PORTRAIT_WIDTH.toDouble() + 2)/2,
                pY.toDouble() + portraitStartY - 12,
                0.0
            )
            matrices.scale(scaleAmount, scaleAmount, scaleAmount)
            val rotationVector = Vector3f(13F, rotationY, 0F)

            drawProfilePokemon(
                renderablePokemon =  renderablePokemon!!,
                poseType = poseList[selectedPoseIndex],
                matrixStack =  matrices,
                partialTicks = delta,
                rotation = Quaternionf().fromEulerXYZDegrees(rotationVector),
                state = state
            )

            matrices.pop()
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
            if (!species!!.implemented) {
                drawScaledTextJustifiedRight(
                    context = context,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = Text.translatable("cobblemon.ui.pokedex.info.unimplemented").bold(),
                    x = pX + 136,
                    y = pY + 15,
                    shadow = true
                )
            }
        }

        // Render
        if (species != null && isSelectedPokemonOwned()) {
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
            matrices.push()
            matrices.translate(0.0, 0.0, 100.0)

            if (gender != Gender.GENDERLESS) genderButton.render(context, mouseX, mouseY, delta)
            shinyButton.render(context, mouseX, mouseY, delta)

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

            // Forms
            if(formsList.size > 1 && formsList.size > selectedFormIndex) {
                formLeftButton.render(context,mouseX, mouseY, delta)
                formRightButton.render(context,mouseX, mouseY, delta)

                drawScaledTextJustifiedRight(
                    context = context,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = Text.translatable("cobblemon.ui.pokedex.info.form.${formsList[selectedFormIndex]}").bold(),
                    x = pX + 136,
                    y = pY + 15,
                    shadow = true
                )
            }
            matrices.pop()
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
    }

    fun setPokemon(dexPokemonData : DexPokemonData, speciesPokedexEntry: SpeciesPokedexEntry?) {
        this.speciesPokedexEntry = speciesPokedexEntry
        this.dexPokemonData = dexPokemonData

        val species = dexPokemonData.species

        if (species != null) {
            var pokemonNumber = species.nationalPokedexNumber.toString()
            while (pokemonNumber.length < 4) pokemonNumber = "0$pokemonNumber"
            this.speciesNumber = pokemonNumber.text()

            this.maleRatio = species.maleRatio
            if (maleRatio != -1F) {
                this.gender = if (maleRatio > 0) Gender.MALE else Gender.FEMALE
                genderButton.active = isSelectedPokemonOwned()
            } else {
                this.gender = Gender.GENDERLESS
                genderButton.active = false
            }

            if (shiny && !isSelectedPokemonOwned()) shiny = false
            this.shinyButton.active = isSelectedPokemonOwned()

            this.speciesName =
                if (speciesPokedexEntry != null) species.translatedName
                else Text.translatable("cobblemon.ui.pokedex.unknown")

            if (dexPokemonData.forms.size > 0) {
                formsList = dexPokemonData.forms
                formLeftButton.active = true
                formRightButton.active = true
            } else {
                formsList = mutableListOf("normal")
                formLeftButton.active = false
                formRightButton.active = false
            }
            selectedFormIndex = 0

            updateAspects()
        }
    }

    private fun setType(species: Species, form: String) {
        val formData = species.getForm(setOf(form))
        type = arrayOf(formData.primaryType, formData.secondaryType)
    }

    private fun playCry() {
        state.addFirstAnimation(setOf("cry"))
    }

    private fun switchForm(nextIndex: Boolean) {
        if (nextIndex) {
            if (selectedFormIndex < formsList.lastIndex) selectedFormIndex++
            else selectedFormIndex = 0
        } else {
            if (selectedFormIndex > 0) selectedFormIndex--
            else selectedFormIndex = formsList.lastIndex
        }
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
        genderButton.resource = if (gender == Gender.FEMALE) buttonGenderFemale else buttonGenderMale
        shinyButton.resource = if (shiny) buttonShiny else buttonNone

        val species = dexPokemonData?.species
        if (species != null) {
            val aspects = mutableSetOf<String>()

            val form = formsList[selectedFormIndex]
            setType(species, form)
            aspects.add(form)

            if (shiny) aspects.add(SHINY_ASPECT.aspect)

            if (gender == Gender.FEMALE) {
                aspects.add("female")
            } else if (gender == Gender.MALE) {
                aspects.add("male")
            }

            renderablePokemon = RenderablePokemon(species, aspects)

            val formData = species.getForm(setOf(form))
            updateForm.invoke(formData)
        }
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
        return speciesPokedexEntry?.highestDiscoveryLevel() == PokedexEntryProgress.CAUGHT
    }

    fun playSound(soundEvent: SoundEvent) {
        Minecraft.getInstance().soundManager.play(PositionedSoundInstance.master(soundEvent, 1.0F))
    }
}