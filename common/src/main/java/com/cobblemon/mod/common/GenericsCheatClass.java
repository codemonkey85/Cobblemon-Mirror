package com.cobblemon.mod.common;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;

import java.util.Collection;

public class GenericsCheatClass {
    // For some reason, doing this in the PokemonEntity class in Kotlin gives confusing generic errors I could not resolve.
    public static Brain.Profile<PokemonEntity> createPokemonBrain(Collection<MemoryModuleType<?>> memories, Collection<SensorType<? extends Sensor<? super PokemonEntity>>> sensors) {
        return Brain.createProfile(memories, sensors);
    }
}
