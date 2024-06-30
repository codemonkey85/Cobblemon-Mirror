/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc.configuration

import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf

class NPCBehaviourConfiguration {
    var canBeHurt = true

    fun encode(buffer: PacketByteBuf) {

    }

    fun decode(buffer: PacketByteBuf) {

    }

    fun saveToNBT(nbt: NbtCompound) {

    }

    fun loadFromNBT(nbt: NbtCompound) {

    }
}