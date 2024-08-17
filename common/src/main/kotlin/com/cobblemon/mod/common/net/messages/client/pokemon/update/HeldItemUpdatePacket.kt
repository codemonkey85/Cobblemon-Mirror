/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readItemStack
import com.cobblemon.mod.common.util.writeItemStack
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.item.ItemStack

class HeldItemUpdatePacket(pokemon: () -> Pokemon, value: ItemStack): SingleUpdatePacket<ItemStack, HeldItemUpdatePacket>(pokemon, value) {

    override val id = ID

    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        buffer.writeItemStack(this.value)
    }

    override fun set(pokemon: Pokemon, value: ItemStack) { pokemon.swapHeldItem(this.value, false) }

    companion object {
        val ID = cobblemonResource("held_item_update")
        fun decode(buffer: RegistryFriendlyByteBuf): HeldItemUpdatePacket {
            val pokemon = decodePokemon(buffer)
            val stack = buffer.readItemStack()
            return HeldItemUpdatePacket(pokemon, stack)
        }
    }

}