package com.cobblemon.mod.common.api.pokemon.moves.entry.variant

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.pokemon.moves.entry.LearnsetEntry
import com.cobblemon.mod.common.api.pokemon.moves.entry.LearnsetEntryType
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder

class TutorLearnsetEntry(override val move: Holder<MoveTemplate>) : LearnsetEntry {

    override val type: LearnsetEntryType<*> = LearnsetEntryType.TUTOR

    override val syncToClient: Boolean = false

    companion object {
        @JvmStatic
        val CODEC: MapCodec<TutorLearnsetEntry> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                MoveTemplate.CODEC.fieldOf("move").forGetter(TutorLearnsetEntry::move)
            ).apply(instance, ::TutorLearnsetEntry)
        }
    }

}