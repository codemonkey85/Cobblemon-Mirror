/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.item.LeftoversCreatedEvent;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PartyStore;
import com.cobblemon.mod.common.api.tags.CobblemonItemTags;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.CompoundTagExtensionsKt;
import com.cobblemon.mod.common.util.DataKeys;
import net.minecraft.world.entity.item.ItemEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    @Shadow public abstract CompoundTag getShoulderEntityLeft();

    @Shadow public abstract CompoundTag getShoulderEntityRight();

    @Shadow public abstract void respawnEntityOnShoulder(CompoundTag entityNbt);

    @Shadow public abstract void setShoulderEntityRight(CompoundTag entityNbt);

    @Shadow public abstract void setShoulderEntityLeft(CompoundTag entityNbt);

    @Shadow public abstract boolean isSpectator();

    @Shadow public abstract boolean addItem(ItemStack stack);

    @Shadow public abstract void displayClientMessage(Component message, boolean overlay);


    protected PlayerMixin(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
        super(p_20966_, p_20967_);
    }

    @Inject(method = "respawnEntityOnShoulder", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/Level;)Ljava/util/Optional;"), cancellable = true)
    private void cobblemon$removePokemon(CompoundTag nbt, CallbackInfo ci) {
        if (CompoundTagExtensionsKt.isPokemonEntity(nbt)) {
            final UUID uuid = this.getPokemonID(nbt);
            if (this.isShoulderPokemon(this.getShoulderEntityRight())) {
                final UUID uuidRight = this.getPokemonID(this.getShoulderEntityRight());
                if (uuid.equals(uuidRight)) {
                    this.recallPokemon(uuidRight);
                    this.setShoulderEntityRight(new CompoundTag());
                }
            }
            if (this.isShoulderPokemon(this.getShoulderEntityLeft())) {
                final UUID uuidLeft = this.getPokemonID(this.getShoulderEntityLeft());
                if (uuid.equals(uuidLeft)) {
                    this.recallPokemon(uuidLeft);
                    this.setShoulderEntityLeft(new CompoundTag());
                }
            }
            ci.cancel();
        }
    }

    @Inject(
        method = "removeEntitiesOnShoulder",
        at = @At(
            value = "JUMP",
            opcode = Opcodes.IFGE,
            ordinal = 0,
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    private void cobblemon$preventPokemonDropping(CallbackInfo ci) {
        // We want to allow both of these to forcefully remove the entities
        if (this.isSpectator() || this.isDeadOrDying())
            return;
        if (!this.isShoulderPokemon(this.getShoulderEntityLeft())) {
            this.respawnEntityOnShoulder(this.getShoulderEntityLeft());
            this.setShoulderEntityLeft(new CompoundTag());
        }
        if (!this.isShoulderPokemon(this.getShoulderEntityRight())) {
            this.respawnEntityOnShoulder(this.getShoulderEntityRight());
            this.setShoulderEntityRight(new CompoundTag());
        }
        ci.cancel();
    }

    private UUID getPokemonID(CompoundTag nbt) {
        return nbt.getCompound(DataKeys.POKEMON)
                .getUUID(DataKeys.POKEMON_UUID);
    }

    private void recallPokemon(UUID uuid) {
        // We need to do this cause the Entity doesn't store a reference to its storage
        try {
            final PartyStore party = Cobblemon.INSTANCE.getStorage().getParty(this.uuid);
            for (Pokemon pokemon : party) {
                if (pokemon.getUuid().equals(uuid)) {
                    pokemon.recall();
                }
            }
        } catch (NoPokemonStoreException ignored) {}
    }

    private boolean isShoulderPokemon(CompoundTag nbt) {
        return CompoundTagExtensionsKt.isPokemonEntity(nbt);
    }

    @Inject(
        method = "eat",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getFoodData()Lnet/minecraft/world/food/FoodData;",
            shift = At.Shift.AFTER
        )
    )
    public void onEatFood(Level world, ItemStack stack, FoodProperties foodComponent, CallbackInfoReturnable<ItemStack> cir) {
        if (!level().isClientSide) {
            if (stack.is(CobblemonItemTags.LEAVES_LEFTOVERS) && level().random.nextDouble() < Cobblemon.config.getAppleLeftoversChance()) {
                ItemStack leftovers = new ItemStack(CobblemonItems.LEFTOVERS);
                ServerPlayer player = Objects.requireNonNull(getServer()).getPlayerList().getPlayer(uuid);
                assert player != null;
                CobblemonEvents.LEFTOVERS_CREATED.postThen(
                    new LeftoversCreatedEvent(player, leftovers),
                    leftoversCreatedEvent -> null,
                    leftoversCreatedEvent -> {
                        if(!player.addItem(leftoversCreatedEvent.getLeftovers())) {
                            var itemPos = player.getLookAngle().scale(0.5f).add(position());
                            level().addFreshEntity(new ItemEntity(level(), itemPos.x(), itemPos.y(), itemPos.z(), leftoversCreatedEvent.getLeftovers()));
                        }
                        return null;
                    }
                );
            }
        }
    }
}