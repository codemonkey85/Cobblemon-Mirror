/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.api.storage.pc.PCPosition
import com.cobblemon.mod.common.net.IntSize
import com.google.common.collect.Lists
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import io.netty.handler.codec.EncoderException
import net.minecraft.nbt.EndTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.Tag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.Utf8String
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.codec.StreamEncoder
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.item.ItemStack
import java.io.IOException
import java.util.*

fun RegistryFriendlyByteBuf.readItemStack(): ItemStack {
    return ItemStack.OPTIONAL_STREAM_CODEC.decode(this)
}

fun RegistryFriendlyByteBuf.writeItemStack(itemStack: ItemStack) { // TODO (techdaan): why was itemStack optional?
    ItemStack.OPTIONAL_STREAM_CODEC.encode(this, itemStack)
}

fun RegistryFriendlyByteBuf.readText(): Component {
    return ComponentSerialization.STREAM_CODEC.decode(this)
}

fun RegistryFriendlyByteBuf.writeText(text: Component) { // TODO (techdaan): why was text optional?
    ComponentSerialization.STREAM_CODEC.encode(this, text)
}

fun ByteBuf.readEntityDimensions(): EntityDimensions {
    val width = readFloat()
    val height = readFloat()
    val isFixed = this.readBoolean()
    return if (isFixed) {
        EntityDimensions.fixed(width, height)
    }
    else {
        EntityDimensions.scalable(width, height)
    }
}


fun <T> ByteBuf.writeCollection(collection: Collection<T> , writer: (ByteBuf, T) -> Unit) {
    this.writeInt(collection.size)
    collection.forEach {
        writer(this, it)
    }
}

fun <T> ByteBuf.writeNullable(obj: T?, writer: (ByteBuf, T) -> Unit) {
    this.writeBoolean(obj == null)
    obj?.let {
        writer(this, it)
    }
}

fun <T> ByteBuf.writeNullable(obj: T?, writer: StreamEncoder<ByteBuf, T>) {
    writeNullable(obj) { buf, otherObj ->
        writer.encode(buf, otherObj)
    }
}

fun ByteBuf.writeString(string: String): ByteBuf {
    Utf8String.write(this, string, 32767)
    return this
}

fun <T> RegistryFriendlyByteBuf.readList(reader: (FriendlyByteBuf) -> T): List<T> = readCollection(Lists::newArrayListWithCapacity, reader)

fun ByteBuf.readString(): String {
    return Utf8String.read(this, 32767)
}

fun <T> ByteBuf.readNullable(reader: (ByteBuf) -> T): T? {
    val isPresent = this.readBoolean()
    if (isPresent) {
        return reader.invoke(this)
    }
    return null
}

fun <K, V> ByteBuf.writeMap(map: Map<K, V>, keyWriter: (ByteBuf, K) -> Unit, valueWriter: (ByteBuf, V) -> Unit) {
    this.writeInt(map.size)
    map.forEach { (key, value) ->
        keyWriter(this, key)
        valueWriter(this, value)
    }
}

fun <K, V> ByteBuf.readMap(keyReader: (ByteBuf) -> K, valueReader: (ByteBuf) -> V): Map<K, V> {
    val map = mutableMapOf<K, V>()
    val numElements = readInt()
    repeat(numElements) {
        val key = keyReader.invoke(this)
        val value = valueReader.invoke(this)
        map[key] = value
    }
    return map
}


fun ByteBuf.readIdentifier(): ResourceLocation {
    val str = this.readString()
    //If this is null we should be using writeNullable anyway
    return ResourceLocation.tryParse(str)!!
}

fun ByteBuf.writeIdentifier(id: ResourceLocation) {
    writeString(id.toString())
}

fun ByteBuf.writePartyPosition(partyPosition: PartyPosition) {
    writeSizedInt(IntSize.U_SHORT, partyPosition.slot)
}

fun ByteBuf.readPartyPosition() = PartyPosition(readSizedInt(IntSize.U_SHORT))

fun ByteBuf.writePCPosition(pcPosition: PCPosition) {
    writeSizedInt(IntSize.U_SHORT, pcPosition.box)
    writeSizedInt(IntSize.U_BYTE, pcPosition.slot)
}

fun ByteBuf.readPCPosition() = PCPosition(readSizedInt(IntSize.U_SHORT), readSizedInt(IntSize.U_BYTE))

fun ByteBuf.writeUUID(uuid: UUID) {
    writeLong(uuid.mostSignificantBits)
    writeLong(uuid.leastSignificantBits)
}

fun ByteBuf.readUUID() = UUID(readLong(), readLong())

fun ByteBuf.writeEnumConstant(value: Enum<*>) {
    this.writeInt(value.ordinal)
}

fun <T : Enum<T>> ByteBuf.readEnumConstant(clazz: Class<T>): T {
    return clazz.getEnumConstants()[this.readInt()]
}

fun ByteBuf.writeNbt(nbt: Tag) {
    if (nbt == null) {
        NbtIo.writeAnyTag(EndTag.INSTANCE, ByteBufOutputStream(this))
    }

    try {
        NbtIo.writeAnyTag(nbt, ByteBufOutputStream(this))
    } catch (var3: IOException) {
        val iOException = var3
        throw EncoderException(iOException)
    }
}

fun ByteBuf.readNbt(): Tag? {
    val iOException: IOException
    val nbtElement: Tag
    try {
        nbtElement = NbtIo.read(ByteBufInputStream(this), NbtAccounter.create(2097152L))
        if (nbtElement.type == EndTag.TYPE) { // TODO (techdaan): ensure this works
            return null
        }
    } catch (var4: IOException) {
        iOException = var4
        throw EncoderException(iOException)
    }

    try {
        return nbtElement
    } catch (var3: IOException) {
        iOException = var3
        throw EncoderException(iOException)
    }
}

fun <E : Enum<E>> ByteBuf.writeEnumSet(enumSet: EnumSet<E>, type: Class<E>) {
    val enums: Array<E> = type.enumConstants as Array<E>
    val bitSet = BitSet(enums.size)

    for (i in enums.indices) {
        bitSet[i] = enumSet.contains(enums[i])
    }

    this.writeBitSet(bitSet, enums.size)
}

fun <E : Enum<E>> ByteBuf.readEnumSet(type: Class<E>): EnumSet<E> {
    val enums: Array<E> = type.enumConstants
    val bitSet = this.readBitSet(enums.size)
    val enumSet = EnumSet.noneOf(type)

    for (i in enums.indices) {
        if (bitSet[i]) {
            enumSet.add(enums[i])
        }
    }

    return enumSet
}

fun ByteBuf.writeBitSet(bitSet: BitSet, size: Int) {
    if (bitSet.length() > size) {
        val var10002 = bitSet.length()
        throw EncoderException("BitSet is larger than expected size ($var10002>$size)")
    } else {
        val bs = bitSet.toByteArray()
        this.writeBytes(bs.copyOf(Mth.positiveCeilDiv(size, 8)))
    }
}

fun ByteBuf.readBitSet(size: Int): BitSet {
    val bs = ByteArray(Mth.positiveCeilDiv(size, 8))
    this.readBytes(bs)
    return BitSet.valueOf(bs)
}

//fun