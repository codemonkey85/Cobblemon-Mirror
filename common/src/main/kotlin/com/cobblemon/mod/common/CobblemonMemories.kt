/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.api.dialogue.ActiveDialogue
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.ListCodec
import com.mojang.serialization.codecs.PrimitiveCodec
import net.minecraft.core.UUIDUtil
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import java.util.Optional

object CobblemonMemories {
    val memories = mutableMapOf<String, MemoryModuleType<*>>()

    val BATTLING_POKEMON = register("battling_pokemon", ListCodec(UUIDUtil.CODEC, 0, 31))
    val NPC_BATTLING = register("npc_battling", PrimitiveCodec.BOOL)
    val DIALOGUES = register<List<ActiveDialogue>>("npc_dialogues")
    val ACTIVE_ACTION_EFFECT = register<ActionEffectContext>("active_action_effect")

    fun <U> register(id: String, codec: Codec<U>): MemoryModuleType<U> {
        val memoryModule = MemoryModuleType(Optional.of(codec))
        memories[id] = memoryModule
        return memoryModule
    }

    fun <U> register(id: String): MemoryModuleType<U> {
        val memoryModule = MemoryModuleType<U>(Optional.empty())
        memories[id] = memoryModule
        return memoryModule
    }
}