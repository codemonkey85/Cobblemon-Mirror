/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.molang

import com.bedrockk.molang.runtime.MoLangEnvironment
import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.MoParams
import com.bedrockk.molang.runtime.struct.ArrayStruct
import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.struct.VariableStruct
import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.MoValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.dialogue.PlayerDialogueFaceProvider
import com.cobblemon.mod.common.api.dialogue.ReferenceDialogueFaceProvider
import com.cobblemon.mod.common.api.scripting.CobblemonScripts
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.WaveFunctions
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.net.messages.client.effect.RunPosableMoLangPacket
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.effectiveName
import com.cobblemon.mod.common.util.getBooleanOrNull
import com.cobblemon.mod.common.util.getDoubleOrNull
import com.cobblemon.mod.common.util.isInt
import com.cobblemon.mod.common.util.itemRegistry
import com.cobblemon.mod.common.util.worldRegistry
import com.mojang.datafixers.util.Either
import net.minecraft.client.gui.components.tabs.Tab
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.commands.TagCommand
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.dimension.DimensionType

/**
 * Holds a bunch of useful MoLang trickery that can be used or extended in API
 *
 * @author Hiroku
 * @since October 2nd, 2023
 */
object MoLangFunctions {
    val generalFunctions = hashMapOf<String, java.util.function.Function<MoParams, Any>>(
        "is_int" to java.util.function.Function { params -> DoubleValue(params.get<MoValue>(0).asString().isInt()) },
        "is_number" to java.util.function.Function { params -> DoubleValue(params.get<MoValue>(0).asString().toDoubleOrNull() != null) },
        "to_number" to java.util.function.Function { params -> DoubleValue(params.get<MoValue>(0).asString().toDoubleOrNull() ?: 0.0) },
        "to_int" to java.util.function.Function { params -> DoubleValue(params.get<MoValue>(0).asString().toIntOrNull() ?: 0) },
        "to_string" to java.util.function.Function { params -> StringValue(params.get<MoValue>(0).asString()) },
        "do_effect_walks" to java.util.function.Function { _ ->
            DoubleValue(Cobblemon.config.walkingInBattleAnimations)
        },
        "random" to java.util.function.Function { params ->
            val options = mutableListOf<MoValue>()
            var index = 0
            while (params.contains(index)) {
                options.add(params.get(index))
                index++
            }
            return@Function options.random() // Can throw an exception if they specified no args. They'd be idiots though.
        },
        "curve" to java.util.function.Function { params ->
            val curveName = params.getString(0)
            val curve = WaveFunctions.functions[curveName] ?: throw IllegalArgumentException("Unknown curve: $curveName")
            return@Function ObjectValue(curve)
        },
        "array" to java.util.function.Function { params ->
            val values = params.params
            val array = ArrayStruct(hashMapOf())
            values.forEachIndexed { index, moValue -> array.setDirectly("$index", moValue) }
            return@Function array
        },
        "run_script" to java.util.function.Function { params ->
            val runtime = MoLangRuntime()
            runtime.environment.query = params.environment.query
            runtime.environment.variable = params.environment.variable
            runtime.environment.context = params.environment.context
            val script = params.getString(0).asIdentifierDefaultingNamespace()
            CobblemonScripts.run(script, runtime) ?: DoubleValue(0)
        }
    )
    val biomeFunctions = hashMapOf<String, java.util.function.Function<MoParams, Any>>()
    val worldFunctions = hashMapOf<String, java.util.function.Function<MoParams, Any>>()
    val dimensionTypeFunctions = hashMapOf<String, java.util.function.Function<MoParams, Any>>()
    val blockFunctions = hashMapOf<String, java.util.function.Function<MoParams, Any>>()
    val playerFunctions = mutableListOf<(Player) -> HashMap<String, java.util.function.Function<MoParams, Any>>>(
        { player ->
            val map = hashMapOf<String, java.util.function.Function<MoParams, Any>>()
            map.put("username") { _ -> StringValue(player.gameProfile.name) }
            map.put("uuid") { _ -> StringValue(player.gameProfile.id.toString()) }
            map.put("data") { _ -> return@put if (player is ServerPlayer) Cobblemon.molangData.load(player.uuid) else DoubleValue(0) }
            map.put("save_data") { _ -> if (player is ServerPlayer) Cobblemon.molangData.save(player.uuid) else DoubleValue(0) }
            map.put("main_held_item") { _ -> player.level().itemRegistry.wrapAsHolder(player.mainHandItem.item).asMoLangValue(Registries.ITEM) }
            map.put("off_held_item") { _ -> player.level().itemRegistry.wrapAsHolder(player.offhandItem.item).asMoLangValue(Registries.ITEM) }
            map.put("face") { _ -> ObjectValue(PlayerDialogueFaceProvider(player.uuid)) }
            map.put("swing_hand") { _ -> player.swing(player.usedItemHand) }
            map.put("food_level") { _ -> DoubleValue(player.foodData.foodLevel) }
            map.put("saturation_level") { _ -> DoubleValue(player.foodData.saturationLevel) }
            map.put("tell") { params ->
                val message = params.getString(0).text()
                val overlay = params.getBooleanOrNull(1) ?: false
                player.displayClientMessage(message, overlay)
            }
            map.put("teleport") { params ->
                val x = params.getDouble(0)
                val y = params.getDouble(1)
                val z = params.getDouble(2)
                val playParticleOptionss = params.getBooleanOrNull(3) ?: false
                player.randomTeleport(x, y, z, playParticleOptionss)
            }
            map.put("heal") { params ->
                val amount = params.getDoubleOrNull(0) ?: player.maxHealth
                player.heal(amount.toFloat())
            }
            map.put("environment") {
                val environment = MoLangEnvironment()
                environment.query = player.asMoLangValue()
                environment
            }
            map
        }
    )
    val entityFunctions = mutableListOf<(LivingEntity) -> HashMap<String, java.util.function.Function<MoParams, Any>>>(
        { entity ->
            val map = hashMapOf<String, java.util.function.Function<MoParams, Any>>()
            map.put("damage") { params ->
                val amount = params.getDouble(0)
                val source = DamageSource(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(DamageTypes.GENERIC).get())
                entity.hurt(source, amount.toFloat())
            }
            map.put("is_sneaking") { _ -> DoubleValue(entity.isShiftKeyDown) }
            map.put("is_sprinting") { _ -> DoubleValue(entity.isSprinting) }
            map.put("is_flying") { _ -> DoubleValue(entity.isFallFlying) }
            map.put("is_in_water") { _ -> DoubleValue(entity.isUnderWater) }
            map.put("is_touching_water_or_rain") { _ -> DoubleValue(entity.isInWaterRainOrBubble) }
            map.put("is_touching_water") { _ -> DoubleValue(entity.isInWater) }
            map.put("is_in_lava") { _ -> DoubleValue(entity.isInLava) }
            map.put("is_on_fire") { _ -> DoubleValue(entity.isOnFire) }
            map.put("is_invisible") { _ -> DoubleValue(entity.isInvisible) }
            map.put("is_sleeping") { _ -> DoubleValue(entity.isSleeping) }
            map.put("is_riding") { _ -> DoubleValue(entity.isPassenger()) }
            map.put("health") { _ -> DoubleValue(entity.health) }
            map.put("max_health") { _ -> DoubleValue(entity.maxHealth) }
            map.put("name") { _ -> StringValue(entity.effectiveName().string) }
            map.put("yaw") { _ -> DoubleValue(entity.yRot.toDouble()) }
            map.put("pitch") { _ -> DoubleValue(entity.xRot.toDouble()) }
            map.put("x") { _ -> DoubleValue(entity.x) }
            map.put("y") { _ -> DoubleValue(entity.y) }
            map.put("z") { _ -> DoubleValue(entity.z) }
            map.put("velocity_x") { _ -> DoubleValue(entity.deltaMovement.x) }
            map.put("velocity_y") { _ -> DoubleValue(entity.deltaMovement.y) }
            map.put("velocity_z") { _ -> DoubleValue(entity.deltaMovement.z) }
            map.put("horizontal_velocity") { _ -> DoubleValue(entity.deltaMovement.horizontalDistance()) }
            map.put("is_on_ground") { _ -> DoubleValue(entity.onGround()) }
            map.put("world") { _ -> entity.level().worldRegistry.wrapAsHolder(entity.level()).asWorldMoLangValue() }
            map.put("biome") { _ -> entity.level().getBiome(entity.blockPosition()).asBiomeMoLangValue() }
            map
        }
    )
    val npcFunctions = mutableListOf<(NPCEntity) -> HashMap<String, java.util.function.Function<MoParams, Any>>>(
        { npc ->
            val map = hashMapOf<String, java.util.function.Function<MoParams, Any>>()
            map.put("class") { StringValue(npc.npc.resourceIdentifier.toString()) }
            map.put("name") { StringValue(npc.name.string) }
            map.put("face") { ObjectValue(ReferenceDialogueFaceProvider(npc.id)) }
            map.put("in_battle") { DoubleValue(npc.isInBattle()) }
            map.put("run_script_on_client") { params ->
                val world = npc.level()
                if (world is ServerLevel) {
                    val script = params.getString(0)
                    val packet = RunPosableMoLangPacket(npc.id, setOf("q.run_script('$script')"))
                    packet.sendToPlayers(world.players().toList())
                }
                Unit
            }
            map.put("run_script") { params ->
                val script = params.getString(0).asIdentifierDefaultingNamespace()
                val environment = params.environment
                val runtime = MoLangRuntime()
                runtime.environment.query = environment.query
                runtime.environment.variable = environment.variable
                runtime.environment.context = environment.context
                CobblemonScripts.run(script, runtime) ?: DoubleValue(0)
            }
            map.put("environment") { _ -> npc.runtime.environment }
            map
        }
    )

    val battleFunctions = mutableListOf<(PokemonBattle) -> HashMap<String, java.util.function.Function<MoParams, Any>>>(
        { battle ->
            val map = hashMapOf<String, java.util.function.Function<MoParams, Any>>()
            map.put("battle_id") { StringValue(battle.battleId.toString()) }
            map.put("is_pvn") { DoubleValue(battle.isPvN) }
            map.put("is_pvp") { DoubleValue(battle.isPvP) }
            map.put("is_pvw") { DoubleValue(battle.isPvW) }
            map.put("battle_type") { StringValue(battle.format.toString()) }
            map.put("environment") { battle.runtime.environment }
            map
        }
    )

    fun Holder<Biome>.asBiomeMoLangValue() = asMoLangValue(Registries.BIOME).addFunctions(biomeFunctions)
    fun Holder<Level>.asWorldMoLangValue() = asMoLangValue(Registries.DIMENSION).addFunctions(worldFunctions)
    fun Holder<Block>.asBlockMoLangValue() = asMoLangValue(Registries.BLOCK).addFunctions(blockFunctions)
    fun Holder<DimensionType>.asDimensionTypeMoLangValue() = asMoLangValue(Registries.DIMENSION_TYPE).addFunctions(dimensionTypeFunctions)
    fun Player.asMoLangValue(): ObjectValue<Player> {
        val value = ObjectValue(
            obj = this,
            stringify = { it.effectiveName().string }
        )
        value.addFunctions(entityFunctions.flatMap { it(this).entries.map { it.key to it.value } }.toMap())
        value.addFunctions(playerFunctions.flatMap { it(this).entries.map { it.key to it.value } }.toMap())
        return value
    }

    fun NPCEntity.asMoLangValue(): ObjectValue<NPCEntity> {
        val value = ObjectValue(
            obj = this,
            stringify = { it.name.string }
        )
        value.addFunctions(entityFunctions.flatMap { it(this).entries.map { it.key to it.value } }.toMap())
        value.addFunctions(npcFunctions.flatMap { it(this).entries.map { it.key to it.value } }.toMap())
        return value
    }

    fun PokemonBattle.asMoLangValue(): ObjectValue<PokemonBattle> {
        val value = ObjectValue(
            obj = this,
            stringify = { it.battleId.toString() }
        )
        value.addFunctions(battleFunctions.flatMap { it(this).entries.map { it.key to it.value } }.toMap())
        return value
    }

    fun <T> Holder<T>.asMoLangValue(key: ResourceKey<Registry<T>>): ObjectValue<Holder<T>> {
        val value = ObjectValue(
            obj = this,
            stringify = { it.unwrapKey().get().location().toString() }
        )
        value.functions.put("is_in") {
            val tag = TagKey.create(key, ResourceLocation.parse(it.getString(0).replace("#", "")))
            return@put DoubleValue(if (value.obj.`is`(tag)) 1.0 else 0.0)
        }
        value.functions.put("is_of") {
            val identifier = ResourceLocation.parse(it.getString(0))
            return@put DoubleValue(if (value.obj.`is`(identifier)) 1.0 else 0.0)
        }
        return value
    }

    fun QueryStruct.addStandardFunctions(): QueryStruct {
        functions.putAll(generalFunctions)
        return this
    }

    fun <T : QueryStruct> T.addFunctions(functions: Map<String, java.util.function.Function<MoParams, Any>>): T {
        this.functions.putAll(functions)
        return this
    }

    fun MoLangRuntime.setup(): MoLangRuntime {
        environment.query.addStandardFunctions()
        return this
    }

    fun writeMoValueToNBT(value: MoValue): Tag? {
        return when (value) {
            is DoubleValue -> DoubleTag.valueOf(value.value)
            is StringValue -> StringTag.valueOf(value.value)
            is ArrayStruct -> {
                val list = value.map.values
                val nbtList = ListTag()
                list.mapNotNull(::writeMoValueToNBT).forEach(nbtList::add)
                nbtList
            }
            is VariableStruct -> {
                val nbt = CompoundTag()
                value.map.forEach { (key, value) ->
                    val element = writeMoValueToNBT(value) ?: return@forEach
                    nbt.put(key, element)
                }
                nbt
            }
            else -> null
        }
    }

    fun readMoValueFromNBT(nbt: Tag): MoValue {
        return when (nbt) {
            is DoubleTag -> DoubleValue(nbt.asDouble)
            is StringTag -> StringValue(nbt.asString)
            is ListTag -> {
                val array = ArrayStruct(hashMapOf())
                var index = 0
                nbt.forEach { element ->
                    val value = readMoValueFromNBT(element)
                    array.setDirectly("$index", value)
                    index++
                }
                array
            }
            is CompoundTag -> {
                val variable = VariableStruct(hashMapOf())
                nbt.allKeys.toList().forEach { key ->
                    val value = readMoValueFromNBT(nbt[key]!!)
                    variable.map[key] = value
                }
                variable
            }
            else -> null
        } ?: throw IllegalArgumentException("Invalid NBT element type: ${nbt.type}")
    }
}

fun Either<ResourceLocation, ExpressionLike>.runScript(runtime: MoLangRuntime) = map({ CobblemonScripts.run(it, runtime) }, { it.resolve(runtime) })