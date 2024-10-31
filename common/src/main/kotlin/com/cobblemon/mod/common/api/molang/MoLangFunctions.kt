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
import com.cobblemon.mod.common.CobblemonActivities
import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.dialogue.PlayerDialogueFaceProvider
import com.cobblemon.mod.common.api.dialogue.ReferenceDialogueFaceProvider
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.cobblemon.mod.common.api.moves.animations.ActionEffects
import com.cobblemon.mod.common.api.moves.animations.NPCProvider
import com.cobblemon.mod.common.api.scripting.CobblemonScripts
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.WaveFunctions
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.net.messages.client.effect.RunPosableMoLangPacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.*
import com.mojang.datafixers.util.Either
import java.util.UUID
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.dimension.DimensionType
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

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
        },
        "run_command" to java.util.function.Function { params ->
            val command = params.getString(0)
            server()?.let {
                return@Function DoubleValue(it.commands.dispatcher.execute(command, it.createCommandSourceStack()))
            } ?: return@Function DoubleValue.ZERO
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
            map.put("face") { params -> ObjectValue(PlayerDialogueFaceProvider(player.uuid, params.getBooleanOrNull(0) != false)) }
            map.put("swing_hand") { _ -> player.swing(player.usedItemHand) }
            map.put("food_level") { _ -> DoubleValue(player.foodData.foodLevel) }
            map.put("saturation_level") { _ -> DoubleValue(player.foodData.saturationLevel) }
//            map.put("has_permission") { params -> DoubleValue(Cobblemon.permissionValidator.hasPermission(player, Permission)) }
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
            if (player is ServerPlayer) {
                map.put("is_party_at_full_health") { _ ->
                    DoubleValue(player.party().none(Pokemon::canBeHealed)) }
                map.put("can_heal_at_healer") { params ->
                    val pos = params.get<ArrayStruct>(0).asBlockPos()
                    val healer = player.level().getBlockEntity(pos, CobblemonBlockEntities.HEALING_MACHINE).orElse(null) ?: return@put DoubleValue.ZERO
                    val party = player.party()
                    return@put DoubleValue(healer.canHeal(party))
                }
                map.put("put_pokemon_in_healer") { params ->
                    val healer = player.level()
                        .getBlockEntity(params.get<ArrayStruct>(0).asBlockPos(), CobblemonBlockEntities.HEALING_MACHINE)
                        .orElse(null) ?: return@put DoubleValue.ZERO
                    val party = player.party()
                    if (healer.canHeal(party)) {
                        healer.activate(player.uuid, party)
                        return@put DoubleValue.ONE
                    } else {
                        return@put DoubleValue.ZERO
                    }
                }
            }
            map
        }
    )
    val entityFunctions = mutableListOf<(LivingEntity) -> HashMap<String, java.util.function.Function<MoParams, Any>>>(
        { entity ->
            val map = hashMapOf<String, java.util.function.Function<MoParams, Any>>()
            map.put("uuid") { _ -> StringValue(entity.uuid.toString()) }
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
            map.put("is_healer_in_use") { params ->
                val pos = params.get<ArrayStruct>(0).asBlockPos()
                val healer = entity.level().getBlockEntity(pos, CobblemonBlockEntities.HEALING_MACHINE).orElse(null) ?: return@put DoubleValue.ONE
                return@put DoubleValue(healer.isInUse)
            }
            map.put("find_nearby_block") { params ->
                val type = params.getString(0).asIdentifierDefaultingNamespace(namespace = "minecraft")
                val isTag = type.path.startsWith("#")
                val range = params.getDoubleOrNull(1) ?: 10
                val blockPos = entity.level().getBlockStatesWithPos(AABB.ofSize(entity.position(), range.toDouble(), range.toDouble(), range.toDouble()))
                    .filter { it.first.blockHolder.let { if (isTag) it.`is`(TagKey.create(Registries.BLOCK, type)) else it.`is`(type) } }
                    .minByOrNull { it.second.distSqr(entity.blockPosition()) }
                    ?.second
                if (blockPos != null) {
                    return@put ArrayStruct(mapOf("0" to DoubleValue(blockPos.x), "1" to DoubleValue(blockPos.y), "2" to DoubleValue(blockPos.z)))
                } else {
                    return@put DoubleValue.ZERO
                }
            }
            map
        }
    )
    val npcFunctions = mutableListOf<(NPCEntity) -> HashMap<String, java.util.function.Function<MoParams, Any>>>(
        { npc ->
            val map = hashMapOf<String, java.util.function.Function<MoParams, Any>>()
            map.put("class") { StringValue(npc.npc.id.toString()) }
            map.put("name") { StringValue(npc.name.string) }
            map.put("face") { params -> ObjectValue(ReferenceDialogueFaceProvider(npc.id, params.getBooleanOrNull(0) != false)) }
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
                val runtime = MoLangRuntime()
                runtime.environment.cloneFrom(params.environment)
                CobblemonScripts.run(script, runtime) ?: DoubleValue(0)
            }
            map.put("run_action_effect") { params ->
                val runtime = MoLangRuntime().setup()
                runtime.environment.cloneFrom(params.environment)
                runtime.withNPCValue(value = npc)
                val actionEffect = ActionEffects.actionEffects[params.getString(0).asIdentifierDefaultingNamespace()]
                if (actionEffect != null) {
                    val context = ActionEffectContext(
                        actionEffect = actionEffect,
                        providers = mutableListOf(NPCProvider(npc)),
                        runtime = runtime
                    )
                    npc.actionEffect = context
                    npc.brain.setMemory(CobblemonMemories.ACTIVE_ACTION_EFFECT, context)
                    npc.brain.setActiveActivityIfPossible(CobblemonActivities.NPC_ACTION_EFFECT)
                    actionEffect.run(context).thenRun {
                        val npcActionEffect = npc.brain.getMemory(CobblemonMemories.ACTIVE_ACTION_EFFECT).orElse(null)
                        if (npcActionEffect == context && npc.brain.isActive(CobblemonActivities.NPC_ACTION_EFFECT)) {
                            npc.brain.eraseMemory(CobblemonMemories.ACTIVE_ACTION_EFFECT)
                            npc.actionEffect = null
                        }
                    }

                    return@put DoubleValue(1)
                }
                return@put DoubleValue(0)
            }
            map.put("put_pokemon_in_healer") { params ->
                val healer = npc.level().getBlockEntity(params.get<ArrayStruct>(0).asBlockPos(), CobblemonBlockEntities.HEALING_MACHINE).orElse(null) ?: return@put DoubleValue.ZERO
                val party = npc.staticParty ?: return@put DoubleValue.ZERO
                if (healer.canHeal(party)) {
                    healer.activate(npc.uuid, party)
                    return@put DoubleValue.ONE
                } else {
                    return@put DoubleValue.ZERO
                }
            }
            map.put("look_at_position") { params ->
                val pos = Vec3(params.getDouble(0), params.getDouble(1), params.getDouble(2))
                npc.lookAt(EntityAnchorArgument.Anchor.EYES, pos)
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
            map.put("get_actor") { params ->
                val uuid = UUID.fromString(params.getString(0))
                val actor = battle.actors.find { it.uuid == uuid } ?: return@put DoubleValue.ZERO
                return@put actor.struct
            }
            map
        }
    )

    val partyFunctions = mutableListOf<(PartyStore) -> HashMap<String, java.util.function.Function<MoParams, Any>>>(
        { party ->
            val map = hashMapOf<String, java.util.function.Function<MoParams, Any>>()
            map.put("get_pokemon") { params ->
                val index = params.getInt(0)
                val pokemon = party.get(index) ?: return@put DoubleValue.ZERO
                val struct = VariableStruct()
                pokemon.writeVariables(struct)
                return@put struct
            }
            map.put("healing_remainder_percent") { _ -> DoubleValue(party.getHealingRemainderPercent()) }

            return@mutableListOf map
        })

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