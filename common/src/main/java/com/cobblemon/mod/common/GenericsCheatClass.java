/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common;

import com.cobblemon.mod.common.entity.npc.NPCEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;

import java.util.Collection;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

public class GenericsCheatClass {
    // For some reason, doing this in the PokemonEntity class in Kotlin gives confusing generic errors I could not resolve.
    public static Brain.Profile<PokemonEntity> createPokemonBrain(Collection<MemoryModuleType<?>> memories, Collection<SensorType<? extends Sensor<? super PokemonEntity>>> sensors) {
        return Brain.createProfile(memories, sensors);
    }
    // For some reason, doing this in the NPCEntity class in Kotlin gives confusing generic errors I could not resolve.
    public static Brain.Provider<NPCEntity> createNPCBrain(Collection<MemoryModuleType<?>> memories, Collection<SensorType<? extends Sensor<? super NPCEntity>>> sensors) {
        return Brain.provider(memories, sensors);
    }
}

