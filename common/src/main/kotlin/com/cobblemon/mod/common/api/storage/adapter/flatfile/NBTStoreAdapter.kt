/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.adapter.flatfile

import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.api.storage.StorePosition
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import java.io.File
import java.util.*
import net.minecraft.core.RegistryAccess

/**
 * A [OneToOneFileStoreAdapter] that can arbitrarily save a single [PokemonStore] into an [CompoundTag] with the
 * help of Minecraft's [NbtIo]. This is arguably the best persistence method for [PokemonStore]s and is absolutely
 * the most efficient [FileStoreAdapter].
 *
 * @author Hiroku
 * @since November 30th, 2021
 */
open class NBTStoreAdapter(
    rootFolder: String,
    useNestedFolders: Boolean,
    folderPerClass: Boolean,
) : OneToOneFileStoreAdapter<CompoundTag>(rootFolder, useNestedFolders, folderPerClass, "dat") {
    override fun <E : StorePosition, T : PokemonStore<E>> serialize(store: T, registryAccess: RegistryAccess) = store.saveToNBT(CompoundTag(), registryAccess)
    override fun save(file: File, serialized: CompoundTag) = NbtIo.writeCompressed(serialized, file.toPath())
    override fun <E, T : PokemonStore<E>> load(file: File, storeClass: Class<out T>, uuid: UUID, registryAccess: RegistryAccess): T? {
        val store = try {
            storeClass.getConstructor(UUID::class.java, UUID::class.java).newInstance(uuid, uuid)
        } catch (exception: NoSuchMethodException) {
            storeClass.getConstructor(UUID::class.java).newInstance(uuid)
        }
        return try {
            val nbt = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap())
            store.loadFromNBT(nbt, registryAccess)
            store
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}