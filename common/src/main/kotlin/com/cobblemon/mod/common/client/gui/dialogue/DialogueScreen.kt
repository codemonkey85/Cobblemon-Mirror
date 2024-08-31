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
import com.cobblemon.mod.common.client.gui.dialogue.widgets.DialogueFaceWidget
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
            is ArtificialDialogueFaceProvider -> key to DialogueRenderableSpeaker(name, ArtificialRenderableFace(face.modelType, face.identifier, face.aspects))
            is PlayerDialogueFaceProvider -> key to DialogueRenderableSpeaker(name, PlayerRenderableFace(face.playerId))
            is ReferenceDialogueFaceProvider -> {
                key to DialogueRenderableSpeaker(
                    name = name,
                    face = ReferenceRenderableFace(Minecraft.getInstance().level?.getEntity(face.entityId) as? PosableEntity ?: return@mapNotNull null)
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
    lateinit var dialogueFaceWidget: DialogueFaceWidget

    val scaledWidth
        get() = this.minecraft!!.window.guiScaledWidth
    val scaledHeight
        get() = this.minecraft!!.window.guiScaledHeight

    companion object {
        private const val BOX_WIDTH = 169
        private const val BOX_HEIGHT = 47

        private const val BAR_WIDTH = 169
        private const val BAR_HEIGHT = 13

        private const val OPTION_HEIGHT = 24
        private const val OPTION_WIDTH_NARROW = 92
        private const val OPTION_WIDTH_WIDE = 162

        private const val NAME_WIDTH = 120
        private const val NAME_HEIGHT = 15

        private const val TEXT_INPUT_WIDTH = 160
        private const val TEXT_INPUT_HEIGHT = 16

        private const val OPTION_HORIZONTAL_SPACING = 12
        private const val OPTION_VERTICAL_SPACING = 1

        private const val FACE_WIDTH = 38
        private const val FACE_HEIGHT = 36

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

        runtime.environment
            .query
            .addFunctions(
                dialogueMolangFunctions
                    .flatMap { it(this@DialogueScreen).entries }
                    .associate { it.key to it.value }
            )

        dialogueDTO.currentPageDTO.clientActions.forEach { runtime.resolve(it.asExpressionLike()) }
        val centerX = scaledWidth / 2F
        val boxMinY = (scaledHeight / 2F - BOX_HEIGHT / 2F) - 10
        val boxMaxY = boxMinY + BOX_HEIGHT
        dialogueTimerWidget = DialogueTimerWidget(
            dialogueScreen = this,
            x = (centerX - BAR_WIDTH / 2F).toInt(),
            y = (boxMaxY + 4).toInt(),
            width = BAR_WIDTH,
            height = BAR_HEIGHT
        )
        dialogueTextInputWidget = DialogueTextInputWidget(
            dialogueScreen = this,
            x = (centerX - TEXT_INPUT_WIDTH / 2F).toInt(),
            y = (boxMaxY + 16).toInt(),
            width = TEXT_INPUT_WIDTH,
            height = TEXT_INPUT_HEIGHT
        )
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
            y = (boxMinY - NAME_HEIGHT).toInt(),
            width = NAME_WIDTH,
            height = NAME_HEIGHT,
            text = name
        )
        dialogueFaceWidget = DialogueFaceWidget(
            dialogueScreen = this,
            x = (centerX - BOX_WIDTH / 2F - FACE_WIDTH).toInt(),
            y = boxMinY.toInt(),
            width = FACE_WIDTH,
            height = FACE_HEIGHT
        )

        val optionCount = dialogueDTO.dialogueInput.options.size
        val optionStartY = boxMaxY + 18
        val vertical = dialogueDTO.dialogueInput.vertical
        val horizontalSpacing = if (vertical) 0 else (OPTION_HORIZONTAL_SPACING + OPTION_WIDTH_WIDE / 2)
        val verticalSpacing = if (vertical) OPTION_VERTICAL_SPACING + OPTION_HEIGHT else 0
        val totalWidth = (optionCount - 1) * horizontalSpacing
        val optionStartX = centerX - totalWidth / 2F

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
                texture = if (vertical) {
                    cobblemonResource("textures/gui/dialogue/dialogue_button_wide.png")
                } else {
                    cobblemonResource("textures/gui/dialogue/dialogue_button_narrow.png")
                },
                overlayTexture = if (vertical) {
                    cobblemonResource("textures/gui/dialogue/dialogue_button_wide_overlay.png")
                } else {
                    cobblemonResource("textures/gui/dialogue/dialogue_button_narrow_overlay.png")
                }
            )
        }

        addRenderableOnly(dialogueTimerWidget)
        addRenderableWidget(dialogueTextInputWidget)
        addRenderableWidget(dialogueBox)
        dialogueOptionWidgets.forEach { addRenderableWidget(it) }
        addRenderableOnly(dialogueNameWidget)
        addRenderableOnly(dialogueFaceWidget)

        if (dialogueDTO.dialogueInput.inputType == DialogueInputDTO.InputType.TEXT) {
            //focusOn(dialogueTextInputWidget)
        }

        dialogueDTO.currentPageDTO.clientActions.flatMap(String::asExpressions).resolve(runtime)
    }

    override fun render(drawContext: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        remainingSeconds -= delta / 20F
        dialogueTimerWidget.ratio = if (remainingSeconds <= 0) -1F else remainingSeconds / dialogueDTO.dialogueInput.deadline
        super.render(drawContext, mouseX, mouseY, delta)
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