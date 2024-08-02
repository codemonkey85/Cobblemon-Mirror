/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.evolution.progress

import com.cobblemon.mod.common.pokemon.evolution.controller.ServerEvolutionController
import com.cobblemon.mod.common.pokemon.evolution.progress.*
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.serialization.Codec
import com.mojang.serialization.Lifecycle
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import kotlin.jvm.Throws

object EvolutionProgressTypes {

    private val REGISTRY: Registry<EvolutionProgressType<*>> = MappedRegistry(
        ResourceKey.createRegistryKey(cobblemonResource("evolution_progress")), Lifecycle.stable()
    )

    @JvmStatic
    val DAMAGE_TAKEN = this.registerType(DamageTakenEvolutionProgress.ID, EvolutionProgressType(DamageTakenEvolutionProgress.CODEC))
    @JvmStatic
    val DEFEAT = this.registerType(DefeatEvolutionProgress.ID, EvolutionProgressType(DefeatEvolutionProgress.CODEC))
    @JvmStatic
    val LAST_BATTLE_CRITICAL_HITS = this.registerType(LastBattleCriticalHitsEvolutionProgress.ID, EvolutionProgressType(LastBattleCriticalHitsEvolutionProgress.CODEC))
    @JvmStatic
    val RECOIL = this.registerType(RecoilEvolutionProgress.ID, EvolutionProgressType(RecoilEvolutionProgress.CODEC))
    @JvmStatic
    val USE_MOVE = this.registerType(UseMoveEvolutionProgress.ID, EvolutionProgressType(UseMoveEvolutionProgress.CODEC))

    /**
     * Registers an evolution progress.
     *
     * @param T The type of the [EvolutionProgress].
     * @param id The [Identifier].
     * @param type The [EvolutionProgressType] record.
     * @return The registered [EvolutionProgressType] of type [T].
     *
     * @throws [IllegalStateException] if [Registry.register] fails.
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    fun <T : EvolutionProgress<*>> registerType(id: ResourceLocation, type: EvolutionProgressType<T>): EvolutionProgressType<T> {
        return Registry.register(REGISTRY, id, type)
    }

    /**
     * Creates a codec instance of this registry.
     *
     * @return The generated [Codec] of [EvolutionProgress].
     */
    fun codec(): Codec<EvolutionProgress<*>> = REGISTRY.byNameCodec().dispatch(
        ServerEvolutionController.ID_KEY,
        { it.type() },
        { it.codec }
    )

}