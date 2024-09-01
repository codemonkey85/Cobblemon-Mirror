/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.cobblemon.mod.common.Cobblemon
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.ResourceLocationException
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

fun ResourceLocation.extractTo(directory : File) {
    val stream = Cobblemon::class.java.getResourceAsStream(String.format("/assets/%s/%s", namespace, path))
        ?: throw Exception("Could not read $this")
    Files.copy(stream, directory.toPath(), StandardCopyOption.REPLACE_EXISTING)
    stream.close()
}

/**
 * Creates an identifier from this string, if a namespace is not present the default [namespace] will be used.
 *
 * @param namespace The namespace that will default if none is present, defaults to [Cobblemon.MODID].
 */
fun String.asIdentifierDefaultingNamespace(namespace: String = Cobblemon.MODID): ResourceLocation {
    val id = this.lowercase()
    return if (id.contains(":")) ResourceLocation.fromNamespaceAndPath(id.substringBefore(":"), id.substringAfter(":")) else ResourceLocation.fromNamespaceAndPath(namespace, id)
}

/**
 * Attempts to parse an [ResourceLocation] from the [StringReader].
 * Unlike [ResourceLocation.fromCommandInput] this will default the namespace to the given [namespace] if non-present.
 * This is useful for when we want to automatically assign an identifier to our mod.
 *
 * @throws CommandSyntaxException If the raw data is not a valid identifier.
 *
 * @param namespace The [ResourceLocation.namespace] being assigned if none is present.
 * @return The parsed [ResourceLocation].
 */
fun StringReader.asIdentifierDefaultingNamespace(namespace: String = Cobblemon.MODID): ResourceLocation {
    val start = this.cursor
    while (this.canRead() && ResourceLocation.isAllowedInResourceLocation(this.peek())) {
        this.skip()
    }
    val raw = this.string.substring(start, this.cursor)
    try {
        return raw.asIdentifierDefaultingNamespace(namespace)
    } catch (e: ResourceLocationException) {
        this.cursor = start
        throw SimpleCommandExceptionType(Component.translatable("argument.id.invalid")).createWithContext(this)
    }
}