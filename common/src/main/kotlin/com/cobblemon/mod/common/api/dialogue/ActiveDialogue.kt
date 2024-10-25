/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.dialogue

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.struct.MoStruct
import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.MoValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.dialogue.input.ActiveInput
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addStandardFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.scheduling.ServerRealTimeTaskTracker
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.net.messages.client.dialogue.DialogueOpenedPacket
import java.util.UUID
import java.util.concurrent.CompletableFuture
import net.minecraft.server.level.ServerPlayer

/**
 * An active instance of [Dialogue]. Bound to a player. These all need to be created through DialogueManager
 * or things will go to shit.
 *
 * @author Hiroku
 * @since December 27th, 2023
 */
class ActiveDialogue(var playerEntity: ServerPlayer, var dialogueReference: Dialogue) {
    var initialized = false
    val dialogueId = UUID.randomUUID()
    val runtime = MoLangRuntime().setup()
    var npc: NPCEntity? = null
    var currentPage = dialogueReference.pages[0]
    val playerStruct = playerEntity.asMoLangValue()
    var activeInput = ActiveInput(this, currentPage.input)

    val completion = CompletableFuture<Unit>()

    fun initialize() {
        runtime.environment.query.addFunctions(
            mapOf(
                "dialogue" to java.util.function.Function { _ -> toMoLangStruct() },
                "player" to java.util.function.Function { _ -> playerStruct }
            )
        )

        dialogueReference.initializationAction.invoke(this)

        if (!completion.isDone) {
            val packet = DialogueOpenedPacket(this, true)
            playerEntity.sendPacket(packet)
        }
        initialized = true
    }

    val currentPageIndex
        get() = dialogueReference.pages.indexOf(currentPage)

    fun setPage(value: MoValue) {
        val page = if (value is StringValue) {
            dialogueReference.pages.find { it.id == value.value }
                ?: return Cobblemon.LOGGER.error("Dialogue requested page ${value.value} but it doesn't exist")
        } else {
            val pageNum = value.asDouble().toInt()
            if (pageNum < 0 || pageNum >= dialogueReference.pages.size) {
                return Cobblemon.LOGGER.error("Dialogue requested page $pageNum but it doesn't exist")
            } else {
                dialogueReference.pages[pageNum]
            }
        }
        setPage(page)
    }

    fun isActive() = DialogueManager.activeDialogues[playerEntity.uuid] == this

    fun incrementPage() {
        setPage(currentPageIndex + 1)
    }

    fun setPage(page: DialoguePage) {
        currentPage = page
        activeInput = ActiveInput(this, currentPage.input)
        val deadline = currentPage.input.timeout?.duration
        val inputId = activeInput.inputId
        if (deadline != null && deadline > 0) {
            ServerRealTimeTaskTracker.after(seconds = deadline) {
                if (inputId == activeInput.inputId && isActive()) {
                    activeInput.dialogueInput.timeout?.action?.invoke(this, null)
                }
            }
        }
        if (initialized) {
            playerEntity.sendPacket(DialogueOpenedPacket(this, false))
        }
    }

    fun setPage(index: Int) {
        if (index == dialogueReference.pages.size) {
            return close()
        }

        if (index < 0 || index > dialogueReference.pages.size) {
            return Cobblemon.LOGGER.error("Dialogue requested page $index but it doesn't exist")
        }

        setPage(dialogueReference.pages[index])
    }

    fun toMoLangStruct(): MoStruct {
        return QueryStruct(
            hashMapOf(
                "current_page" to java.util.function.Function { _ -> currentPage.toMoLangStruct(this) },
                "current_page_number" to java.util.function.Function { _ -> DoubleValue(currentPageIndex) },
                "next_page" to java.util.function.Function { _ -> incrementPage() },
                "set_page" to java.util.function.Function { args -> setPage(args[0]) },
                "close" to java.util.function.Function { _ -> close() },
                "input" to java.util.function.Function { params -> activeInput.handle(if (params.params.size > 0) params.get<MoValue>(0).asString() else "") },
            )
        ).addStandardFunctions()
    }

    fun close() {
        if (initialized) {
            DialogueManager.stopDialogue(playerEntity)
        }
        completion.complete(Unit)
    }

    fun escape() {
        val action = currentPage.escapeAction ?: dialogueReference.escapeAction
        action(this, null)
    }
}