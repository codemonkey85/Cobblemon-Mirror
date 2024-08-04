/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.dialogue.dto

import com.cobblemon.mod.common.api.dialogue.DialogueFaceProvider
import net.minecraft.network.chat.MutableComponent

class DialogueSpeakerDTO(
    val name: MutableComponent? = null,
    val face: DialogueFaceProvider?
)