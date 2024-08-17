/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.effects

import com.cobblemon.mod.common.api.pokemon.effect.ShoulderEffect
import com.cobblemon.mod.common.mixin.accessor.StatusEffectInstanceAccessor
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class PotionBaseEffect(
    val effect: MobEffect,
    val amplifier: Int,
    val ambient: Boolean,
    val showParticles: Boolean,
    val showIcon: Boolean
) : ShoulderEffect {

    override fun applyEffect(pokemon: Pokemon, player: ServerPlayer, isLeft: Boolean) {
        val effect = player.getEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect))
        // We handle part of our own type.
        if (effect is ShoulderStatusEffectInstance && effect.amplifier >= this.amplifier) {
            // If the effect is the same strength simply add another source for it.
            if (effect.amplifier == this.amplifier) {
                effect.shoulderSources.add(pokemon.uuid)
            }
            // When the effect is weaker just don't use it, it will automatically be used if the stronger is removed from shoulder.
            // Both cases want to stop execution here.
            return
        }
        // Let vanilla handle it, it will attempt to upgrade our custom impl which handles both upgrades from vanilla or our own.
        player.addEffect(this.createStatus(pokemon))
    }

    override fun removeEffect(pokemon: Pokemon, player: ServerPlayer, isLeft: Boolean) {
        val effect = player.getEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect)) as? ShoulderStatusEffectInstance ?: return
        if (effect.amplifier == this.amplifier && effect.ambient == this.ambient && effect.isVisible() == this.showParticles && effect.showIcon() == this.showIcon) {
            effect.shoulderSources.remove(pokemon.uuid)
        }
    }

    // Note on the -1 on level, we do this because we want to be user-friendly and not make them think about indexes :)
    private fun createStatus(pokemon: Pokemon): ShoulderStatusEffectInstance = ShoulderStatusEffectInstance(this.effect, this.amplifier, this.ambient, this.showParticles, this.showIcon, pokemon)

    /*
     Long term we need to do this differently. There is a restriction in minecraft that for a StatusEffect, i.e. SLOW_FALLING,
     only one status instance can exist. If someone puts Pokémon on their shoulder for slow fall, and then they take the potion,
     they SHOULD have the effect even after they take their Pokémon off the shoulder since the potion would still be in effect,
     but it won't have even been added because of the uniqueness constraint. Unclear how best to solve this.
     */
    class ShoulderStatusEffectInstance(
        effect: MobEffect,
        amplifier: Int,
        ambient: Boolean,
        showParticles: Boolean,
        showIcon: Boolean,
        startingPokemon: Pokemon
    ) : MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), -1, amplifier, ambient, showParticles, showIcon) {

        internal val shoulderSources: MutableSet<UUID> = hashSetOf(startingPokemon.uuid)
        private var upgrade: MobEffectInstance? = null

        fun save(nbt: CompoundTag): CompoundTag {
            /**
             * No need for this operation.
             * [StatusEffectInstance.fromNbt] tosses it out immediately if the ID is invalid.
             */
            // super.writeNbt(nbt)
            /*
             * StatusEffectInstance isn't really made to be extended. If they have the status when they log out,
             * it will be saved and loaded and any of our custom properties and subclassing doesn't mean shit.
             *
             * The way this effect works is that it's constantly checking if the Pokémon is where it should be. This
             * can only happen if it is this subclass, so we have the pokemonId variable. We must not allow Minecraft
             * to load this back in. We can manually handle this shit, so the goal is to stop Minecraft from doing it
             * for us.
             *
             * Putting the ID as some impossible number means that when reading from NBT statically, it doesn't find a
             * match for the effect ID, so it doesn't bother. This is what we want because if the effect needs to
             * continue (like the Pokémon is still on the shoulder) then we handle that in our login handler - no need
             * for Minecraft to get in our way.
             *
             * - Hiro
             */
            nbt.putInt("Id", -999)
            return nbt
        }

        override fun isInfiniteDuration(): Boolean = this.shoulderSources.isNotEmpty()

        override fun update(that: MobEffectInstance): Boolean {
            if (that.amplifier > this.amplifier) {
                // We handle our own upgrading, don't use the vanilla system.
                if (that is ShoulderStatusEffectInstance) {
                    this.upgradeFrom(that)
                    return true
                }
                this.upgrade = that
                return true
            }
            return false
        }

        override fun tick(entity: LivingEntity, overwriteCallback: Runnable): Boolean {
            if (this.effect.value().shouldApplyEffectTickThisTick(entity.tickCount, this.amplifier)) {
                this.onEffectStarted(entity)
            }
            this.upgrade?.let {
                if (--it.duration == 0) {
                    this.upgrade = null
                    overwriteCallback.run()
                }
            }
            return this.shoulderSources.isNotEmpty()
        }

        override fun onEffectStarted(entity: LivingEntity) {
            this.effect.value().applyEffectTick(entity, this.upgrade?.amplifier ?: this.amplifier)
        }

        @Suppress("CAST_NEVER_SUCCEEDS")
        private fun upgradeFrom(other: ShoulderStatusEffectInstance) {
            this.shoulderSources.clear()
            this.shoulderSources += other.shoulderSources
            this.ambient = other.ambient
            val accessor = this as StatusEffectInstanceAccessor
            accessor.setAmplifier(other.amplifier)
            accessor.setShowIcon(other.showIcon())
            accessor.setShowParticles(other.isVisible)
        }

    }

}