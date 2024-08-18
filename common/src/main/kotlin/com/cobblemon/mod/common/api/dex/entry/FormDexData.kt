package com.cobblemon.mod.common.api.dex.entry

import com.bedrockk.molang.MoLang
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.ListExpression
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

class FormDexData(
    val aspects: String,
    val conditions: List<ExpressionLike>
) : ExtraDexData() {
    override val type = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(aspects)
        buffer.writeInt(conditions.size)
        conditions.forEach {
            buffer.writeString(it.toString())
        }
    }

    companion object {
        val ID = cobblemonResource("form_dex_data")

        fun decode(buffer: RegistryFriendlyByteBuf): FormDexData {
            val aspectString = buffer.readString()
            val numToRead = buffer.readInt()
            val conditions = mutableListOf<ExpressionLike>()
            for (i in 0 until numToRead) {
                val expressions = MoLang.createParser(buffer.readString()).parse()
                conditions.add(ListExpression(expressions))
            }
            return FormDexData(aspectString, conditions)
        }
    }
}