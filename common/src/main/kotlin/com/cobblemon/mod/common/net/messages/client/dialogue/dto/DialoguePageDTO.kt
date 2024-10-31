/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.dialogue.dto

import com.cobblemon.mod.common.api.dialogue.ActiveDialogue
import com.cobblemon.mod.common.api.dialogue.DialoguePage
import com.cobblemon.mod.common.api.net.Decodable
import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.readList
import com.cobblemon.mod.common.util.readNullable
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.readText
import com.cobblemon.mod.common.util.writeCollection
import com.cobblemon.mod.common.util.writeIdentifier
import com.cobblemon.mod.common.util.writeNullable
import com.cobblemon.mod.common.util.writeString
import com.cobblemon.mod.common.util.writeText
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation

class DialoguePageDTO : Encodable, Decodable {
    var speaker: String? = null
    lateinit var background: ResourceLocation
    var lines: MutableList<MutableComponent> = mutableListOf()
    // Later can include some face data probably
    var clientActions = mutableListOf<String>()

    constructor()
    constructor(dialoguePage: DialoguePage, activeDialogue: ActiveDialogue) {
        this.speaker = dialoguePage.speaker
        this.background = dialoguePage.background ?: activeDialogue.dialogueReference.background
        this.lines = dialoguePage.lines.map { it(activeDialogue) }.toMutableList()
        this.clientActions = dialoguePage.clientActions.map { it.originalString }.toMutableList()
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeNullable(speaker) { _, value -> buffer.writeString(value)}
        buffer.writeIdentifier(background)
        buffer.writeCollection(lines) { _, value -> buffer.writeText(value) }
        buffer.writeInt(clientActions.size)
        clientActions.forEach { buffer.writeString(it) }
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        speaker = buffer.readNullable { buffer.readString() }
        background = buffer.readIdentifier()
        lines = buffer.readList { (it as RegistryFriendlyByteBuf).readText().copy() }.toMutableList()
        val clientActionsSize = buffer.readInt()
        for (i in 0 until clientActionsSize) {
            clientActions.add(buffer.readString())
        }
    }
}