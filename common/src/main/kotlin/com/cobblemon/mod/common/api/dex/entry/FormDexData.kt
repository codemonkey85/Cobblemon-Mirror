package com.cobblemon.mod.common.api.dex.entry

import com.bedrockk.molang.MoLang
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.ListExpression
import com.cobblemon.mod.common.api.net.Decodable
import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

class FormDexData() : ExtraDexData(), Decodable, Encodable {
    override val type = ID
    var aspectString = ""
    val condition = mutableListOf<ExpressionLike>()

    companion object {
        val ID = cobblemonResource("form_dex_data")
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        aspectString = buffer.readString()
        val numToRead = buffer.readInt()
        for (i in 0 until numToRead) {
            val expressions = MoLang.createParser(buffer.readString()).parse()
            condition.add(ListExpression(expressions))
        }
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(aspectString)
        buffer.writeInt(condition.size)
        condition.forEach {
            buffer.writeString(it.toString())
        }
    }
}