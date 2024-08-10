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
import com.cobblemon.mod.common.world.gamerules.CobblemonGameRules;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerMixin extends LivingEntity {

    @Shadow public abstract NbtCompound getShoulderEntityLeft();

    @Shadow public abstract NbtCompound getShoulderEntityRight();

    @Shadow public abstract void dropShoulderEntity(NbtCompound entityNbt);

    @Shadow public abstract void setShoulderEntityRight(NbtCompound entityNbt);

    @Shadow public abstract void setShoulderEntityLeft(NbtCompound entityNbt);

    @Shadow public abstract boolean isSpectator();

    @Shadow public abstract boolean giveItemStack(ItemStack stack);

    @Shadow public abstract void sendMessage(Text message, boolean overlay);


    protected PlayerMixin(EntityType<? extends LivingEntity> p_20966_, World p_20967_) {
        super(p_20966_, p_20967_);
    }

    @Inject(method = "dropShoulderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityType;getEntityFromNbt(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/world/World;)Ljava/util/Optional;"), cancellable = true)
    private void cobblemon$removePokemon(NbtCompound nbt, CallbackInfo ci) {
        if (CompoundTagExtensionsKt.isPokemonEntity(nbt)) {
            final UUID uuid = this.getPokemonID(nbt);
            if (this.isShoulderPokemon(this.getShoulderEntityRight())) {
                final UUID uuidRight = this.getPokemonID(this.getShoulderEntityRight());
                if (uuid.equals(uuidRight)) {
                    this.recallPokemon(uuidRight);
                    this.setShoulderEntityRight(new NbtCompound());
                }
            }
            if (this.isShoulderPokemon(this.getShoulderEntityLeft())) {
                final UUID uuidLeft = this.getPokemonID(this.getShoulderEntityLeft());
                if (uuid.equals(uuidLeft)) {
                    this.recallPokemon(uuidLeft);
                    this.setShoulderEntityLeft(new NbtCompound());
                }
            }
            ci.cancel();
        }
    }

    @Inject(
        method = "dropShoulderEntities",
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
        if (this.isSpectator() || this.isDead())
            return;
        if (!this.isShoulderPokemon(this.getShoulderEntityLeft())) {
            this.dropShoulderEntity(this.getShoulderEntityLeft());
            this.setShoulderEntityLeft(new NbtCompound());
        }
        if (!this.isShoulderPokemon(this.getShoulderEntityRight())) {
            this.dropShoulderEntity(this.getShoulderEntityRight());
            this.setShoulderEntityRight(new NbtCompound());
        }
        ci.cancel();
    }

    private UUID getPokemonID(NbtCompound nbt) {
        return nbt.getCompound(DataKeys.POKEMON)
                .getUuid(DataKeys.POKEMON_UUID);
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

    private boolean isShoulderPokemon(NbtCompound nbt) {
        return CompoundTagExtensionsKt.isPokemonEntity(nbt);
    }

    @Inject(
        method = "eatFood",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;getHungerManager()Lnet/minecraft/entity/player/HungerManager;",
            shift = At.Shift.AFTER
        )
    )
    public void onEatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (!getWorld().isClient) {
            if (stack.isIn(CobblemonItemTags.LEAVES_LEFTOVERS) && getWorld().random.nextDouble() < Cobblemon.config.getAppleLeftoversChance()) {
                ItemStack leftovers = new ItemStack(CobblemonItems.LEFTOVERS);
                ServerPlayerEntity player = Objects.requireNonNull(getServer()).getPlayerManager().getPlayer(uuid);
                assert player != null;
                CobblemonEvents.LEFTOVERS_CREATED.postThen(
                    new LeftoversCreatedEvent(player, leftovers),
                    leftoversCreatedEvent -> null,
                    leftoversCreatedEvent -> {
                        if(!player.giveItemStack(leftoversCreatedEvent.getLeftovers())) {
                            var itemPos = player.getRotationVector().multiply(0.5).add(getPos());
                            getWorld().spawnEntity(new ItemEntity(getWorld(), itemPos.getX(), itemPos.getY(), itemPos.getZ(), leftoversCreatedEvent.getLeftovers()));
                        }
                        return null;
                    }
                );
            }
        }
    }

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    public void isInvulnerableTo(DamageSource source, CallbackInfoReturnable<Boolean> ci) {
        if (!getWorld().isClient) {
            ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
            boolean invulnerableInBattle = this.getWorld().getGameRules().getBoolean(CobblemonGameRules.BATTLE_INVULNERABILITY);
            boolean inBattle = Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null;
            if (invulnerableInBattle && inBattle) ci.setReturnValue(true);
        }
    }
}