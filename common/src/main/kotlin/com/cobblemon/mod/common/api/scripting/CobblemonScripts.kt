/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.scripting

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.data.DataRegistry
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.messages.client.data.ScriptRegistrySyncPacket
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.endsWith
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import java.io.File
import java.util.concurrent.ExecutionException

object CobblemonScripts : DataRegistry {
    const val MOLANG_EXTENSION = ".molang"
    override val id = cobblemonResource("molang")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<CobblemonScripts>()

    val clientScripts = mutableMapOf<ResourceLocation, ExpressionLike>()
    val scripts = mutableMapOf<ResourceLocation, ExpressionLike>()

    override fun reload(manager: ResourceManager) {
        manager.listResources("molang") { path -> path.endsWith(MOLANG_EXTENSION) }.forEach { (identifier, resource) ->
            resource.open().use { stream ->
                stream.bufferedReader().use { reader ->
                    val resolvedIdentifier = ResourceLocation.fromNamespaceAndPath(identifier.namespace, File(identifier.path).nameWithoutExtension)
                    try {
                        val expression = reader.readText().asExpressionLike()
                        if (identifier.path.startsWith("molang/client/")) {
                            clientScripts[resolvedIdentifier] = expression
                        } else {
                            scripts[resolvedIdentifier] = expression
                        }
                    } catch (exception: Exception) {
                        throw ExecutionException("Error loading MoLang script: $identifier", exception)
                    }
                }
            }
        }

        Cobblemon.LOGGER.info("Loaded ${scripts.size} server scripts and ${clientScripts.size} client scripts")
        observable.emit(this)
    }


    override fun sync(player: ServerPlayer) {
        player.sendPacket(ScriptRegistrySyncPacket(clientScripts.entries))
    }

    fun run(identifier: ResourceLocation, runtime: MoLangRuntime): MoValue? {
        return scripts[identifier]?.resolve(runtime)
    }
}