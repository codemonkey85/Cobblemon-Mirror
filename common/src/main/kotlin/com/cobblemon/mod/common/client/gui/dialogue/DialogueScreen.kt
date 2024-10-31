/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.dialogue

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.MoParams
import com.cobblemon.mod.common.api.dialogue.ArtificialDialogueFaceProvider
import com.cobblemon.mod.common.api.dialogue.PlayerDialogueFaceProvider
import com.cobblemon.mod.common.api.dialogue.ReferenceDialogueFaceProvider
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.client.ClientMoLangFunctions.setupClient
import com.cobblemon.mod.common.client.gui.dialogue.widgets.DialogueBox
import com.cobblemon.mod.common.client.gui.dialogue.widgets.DialoguePortraitWidget
import com.cobblemon.mod.common.client.gui.dialogue.widgets.DialogueNameWidget
import com.cobblemon.mod.common.client.gui.dialogue.widgets.DialogueOptionWidget
import com.cobblemon.mod.common.client.gui.dialogue.widgets.DialogueTextInputWidget
import com.cobblemon.mod.common.client.gui.dialogue.widgets.DialogueTimerWidget
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.net.messages.client.dialogue.dto.DialogueDTO
import com.cobblemon.mod.common.net.messages.client.dialogue.dto.DialogueInputDTO
import com.cobblemon.mod.common.net.messages.server.dialogue.EscapeDialoguePacket
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.asExpressions
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.resolve
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen

class DialogueScreen(var dialogueDTO: DialogueDTO) : Screen("gui.dialogue".asTranslated()) {
    val speakers = dialogueDTO.speakers?.mapNotNull { (key, value) ->
        val name = value.name
        when (val face = value.face) {
            is ArtificialDialogueFaceProvider -> key to DialogueRenderableSpeaker(name, ArtificialRenderableFace(face.modelType, face.identifier, face.aspects, face.isLeftSide))
            is PlayerDialogueFaceProvider -> key to DialogueRenderableSpeaker(name, PlayerRenderableFace(face.playerId, face.isLeftSide))
            is ReferenceDialogueFaceProvider -> {
                key to DialogueRenderableSpeaker(
                    name = name,
                    face = ReferenceRenderableFace(Minecraft.getInstance().level?.getEntity(face.entityId) as? PosableEntity ?: return@mapNotNull null, face.isLeftSide)
                )
            }
            else -> key to DialogueRenderableSpeaker(name, null)
        }
    }?.toMap() ?: emptyMap()
    val runtime = MoLangRuntime().setup().setupClient()

    // After they do something, the GUI will wait for the server to update the dialogue in some way
    var waitingForServerUpdate = false

    val dialogueId = dialogueDTO.dialogueId
    var remainingSeconds = dialogueDTO.dialogueInput.deadline
    lateinit var dialogueTimerWidget: DialogueTimerWidget
    lateinit var dialogueTextInputWidget: DialogueTextInputWidget
    lateinit var dialogueBox: DialogueBox
    lateinit var dialogueOptionWidgets: List<DialogueOptionWidget>
    lateinit var dialogueNameWidget: DialogueNameWidget
    lateinit var dialoguePortraitWidget: DialoguePortraitWidget

    val scaledWidth
        get() = this.minecraft!!.window.guiScaledWidth
    val scaledHeight
        get() = this.minecraft!!.window.guiScaledHeight

    companion object {
        const val BOX_WIDTH = 196
        const val BOX_HEIGHT = 74

        const val PORTRAIT_WIDTH = 38
        const val PORTRAIT_HEIGHT = 36

        private const val BAR_WIDTH = 186
        private const val BAR_HEIGHT = 4

        private const val OPTION_HEIGHT = 21
        private const val OPTION_WIDTH_NARROW = 96
        private const val OPTION_WIDTH_WIDE = 196

        private const val NAME_WIDTH = 196
        private const val NAME_HEIGHT = 17

        private const val TEXT_INPUT_WIDTH = 196
        private const val TEXT_INPUT_HEIGHT = 21

        private const val OPTION_HORIZONTAL_SPACING = 4
        private const val OPTION_VERTICAL_SPACING = 4

        private val buttonResource = cobblemonResource("textures/gui/dialogue/dialogue_button.png")
        private val buttonFullWidthResource = cobblemonResource("textures/gui/dialogue/dialogue_button_full.png")

        val dialogueMolangFunctions = mutableListOf<(DialogueScreen) -> HashMap<String, java.util.function.Function<MoParams, Any>>>(
            { dialogueScreen ->
                return@mutableListOf hashMapOf(
                    "face" to java.util.function.Function { Unit },
                )
            }
        // idk something maybe, stuff for accessing the current 'face' to modify it?
        )
    }

    override fun renderBackground(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {}

    override fun renderBlurredBackground(delta: Float) {}

    override fun init() {
        super.init()

        runtime.environment.query
        .addFunctions(
            dialogueMolangFunctions
                .flatMap { it(this@DialogueScreen).entries }
                .associate { it.key to it.value }
        )

        dialogueDTO.currentPageDTO.clientActions.forEach { runtime.resolve(it.asExpressionLike()) }
        val centerX = scaledWidth / 2F
        val boxMinY = (scaledHeight / 2F) - (BOX_HEIGHT / 2F) - 30
        val boxMaxY = boxMinY + BOX_HEIGHT

        dialogueBox = DialogueBox(
            dialogueScreen = this,
            messages = dialogueDTO.currentPageDTO.lines,
            listX = (centerX - BOX_WIDTH / 2F).toInt(),
            listY = boxMinY.toInt(),
            frameWidth = BOX_WIDTH,
            height = BOX_HEIGHT
        )

        val name = speakers[dialogueDTO.currentPageDTO.speaker]?.name
        dialogueNameWidget = DialogueNameWidget(
            x = (centerX - BOX_WIDTH / 2F).toInt(),
            y = (boxMinY - NAME_HEIGHT + 1).toInt(),
            width = NAME_WIDTH,
            height = NAME_HEIGHT,
            text = name
        )

        dialoguePortraitWidget = DialoguePortraitWidget(
            dialogueScreen = this,
            x = (centerX - BOX_WIDTH / 2F - PORTRAIT_WIDTH).toInt(),
            y = (boxMinY - NAME_HEIGHT + 1).toInt(),
            width = PORTRAIT_WIDTH,
            height = PORTRAIT_HEIGHT
        )

        dialogueTimerWidget = DialogueTimerWidget(
            dialogueScreen = this,
            x = (centerX - BAR_WIDTH / 2F).toInt(),
            y = (boxMaxY - 1).toInt(),
            width = BAR_WIDTH,
            height = BAR_HEIGHT
        )

        dialogueTextInputWidget = DialogueTextInputWidget(
            dialogueScreen = this,
            x = (centerX - BOX_WIDTH / 2F).toInt(),
            y = (boxMaxY + 7).toInt(),
            width = TEXT_INPUT_WIDTH,
            height = TEXT_INPUT_HEIGHT
        )

        val optionCount = dialogueDTO.dialogueInput.options.size
        val vertical = dialogueDTO.dialogueInput.vertical
        val horizontalSpacing = if (vertical) 0 else (OPTION_HORIZONTAL_SPACING + OPTION_WIDTH_NARROW)
        val verticalSpacing = if (vertical) OPTION_VERTICAL_SPACING + OPTION_HEIGHT else 0
        val totalWidth = (optionCount - 1) * horizontalSpacing
        val optionStartX = centerX - (totalWidth / 2F)
        val optionStartY = boxMaxY + 7

        dialogueOptionWidgets = dialogueDTO.dialogueInput.options.mapIndexed { index, option ->
            val x = optionStartX + (index * horizontalSpacing)
            val y = optionStartY + (index * verticalSpacing)

            DialogueOptionWidget(
                dialogueScreen = this,
                text = option.text,
                value = option.value,
                selectable = option.selectable,
                x = x.toInt() - (if (vertical) OPTION_WIDTH_WIDE / 2 else OPTION_WIDTH_NARROW / 2),
                y = y.toInt(),
                width = if (vertical) OPTION_WIDTH_WIDE else OPTION_WIDTH_NARROW,
                height = OPTION_HEIGHT,
                texture = if (vertical) buttonFullWidthResource else buttonResource
            )
        }

        addRenderableOnly(dialogueTimerWidget)
        addRenderableWidget(dialogueTextInputWidget)
        addRenderableWidget(dialogueBox)
        dialogueOptionWidgets.forEach { addRenderableWidget(it) }
        addRenderableOnly(dialogueNameWidget)
        addRenderableOnly(dialoguePortraitWidget)

        if (dialogueDTO.dialogueInput.inputType == DialogueInputDTO.InputType.TEXT) {
            //focusOn(dialogueTextInputWidget)
        }

        dialogueDTO.currentPageDTO.clientActions.flatMap(String::asExpressions).resolve(runtime)
    }

    override fun render(GuiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        remainingSeconds -= delta / 20F
        dialogueTimerWidget.ratio = if (remainingSeconds <= 0) -1F else remainingSeconds / dialogueDTO.dialogueInput.deadline
        super.render(GuiGraphics, mouseX, mouseY, delta)
    }

    override fun isPauseScreen() = false

    fun update(dialogueDTO: DialogueDTO) {
        this.dialogueDTO = dialogueDTO
        this.remainingSeconds = dialogueDTO.dialogueInput.deadline
        waitingForServerUpdate = false
        rebuildWidgets()
    }

    fun sendToServer(packet: NetworkPacket<*>) {
        packet.sendToServer()
        waitingForServerUpdate = true
    }

    override fun onClose() {
        EscapeDialoguePacket().sendToServer()
    }
}