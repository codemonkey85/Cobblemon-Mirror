/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.dialogue

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.util.cobblemonResource
import java.util.UUID
import net.minecraft.resources.ResourceLocation

/**
 * Something that produces a dialogue's renderable face. This is sealed because the client has very particular handling for this.
 *
 * @author Hiroku
 * @since January 1st, 2024
 */
sealed interface DialogueFaceProvider {
    val isLeftSide: Boolean

    companion object {
        val types = mutableMapOf(
            "player" to PlayerDialogueFaceProvider::class.java,
            "standard" to ArtificialDialogueFaceProvider::class.java,
            "expression" to ExpressionLikeDialogueFaceProvider::class.java
        )
    }
}

class ArtificialDialogueFaceProvider(
    val modelType: String = "",
    val identifier: ResourceLocation = cobblemonResource("bulbasaur"),
    val aspects: Set<String> = setOf(),
    override val isLeftSide: Boolean = true
) : DialogueFaceProvider

class ReferenceDialogueFaceProvider(
    val entityId: Int,
    override val isLeftSide: Boolean = true
): DialogueFaceProvider

/**
 * A face provider that uses the player's skin as the face, as long as there is a player with this UUID online.
 *
 * What's interesting is that this works for fake players, which is what Taterzens' NPCs use.
 */
class PlayerDialogueFaceProvider(val playerId: UUID = UUID.randomUUID(), override val isLeftSide: Boolean = true) : DialogueFaceProvider

class ExpressionLikeDialogueFaceProvider(
    val providerExpression: ExpressionLike
): DialogueFaceProvider {
    override val isLeftSide: Boolean = false // Doesn't get used
}