package com.cobblemon.mod.common.api.pokemon.moves.entry

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.mojang.serialization.Codec
import net.minecraft.core.Holder

interface LearnsetEntry {

    val move: Holder<MoveTemplate>

    val syncToClient: Boolean

    val type: LearnsetEntryType<*>

    companion object {

        @JvmStatic
        val CODEC: Codec<LearnsetEntry> = LearnsetEntryType.REGISTRY
            .byNameCodec()
            .dispatch(LearnsetEntry::type) { it.codec() }

    }

}