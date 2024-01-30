package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.tms.ObtainMethod
import com.cobblemon.mod.common.api.tms.ObtainMethodAdapter
import com.cobblemon.mod.common.tms.obtain.ImpossibleObtainMethod
import com.cobblemon.mod.common.tms.obtain.PlayerHasAdvancementObtainMethod
import com.cobblemon.mod.common.tms.obtain.PokemonHasMoveObtainMethod
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import net.minecraft.util.Identifier
import java.lang.reflect.Type
import kotlin.reflect.KClass

object CobblemonObtainMethodAdapter : ObtainMethodAdapter {
    private const val VARIANT = "variant"
    private val types = hashMapOf<String, KClass<out ObtainMethod>>()

    init {
        this.register(PokemonHasMoveObtainMethod::class, PokemonHasMoveObtainMethod.ID)
        this.register(ImpossibleObtainMethod::class, ImpossibleObtainMethod.ID)
        this.register(PlayerHasAdvancementObtainMethod::class, PlayerHasAdvancementObtainMethod.ID)
    }

    override fun register(type: KClass<out ObtainMethod>, identifier: Identifier) {
        val existing = this.types.put(identifier.toString(), type)
        if (existing != null) {
            Cobblemon.LOGGER.debug("Replaced {} under ID {} with {} in the {}", existing::class.qualifiedName, identifier.toString(), type.qualifiedName, this::class.qualifiedName)
        }
    }

    override fun deserialize(jElement: JsonElement, type: Type, context: JsonDeserializationContext): ObtainMethod {
        val json = jElement.asJsonObject
        val variant = json.get(VARIANT).asString.lowercase()
        val registeredType = this.types[variant] ?: throw IllegalArgumentException("Cannot resolve type for variant $variant")
        return context.deserialize(json, registeredType.java)
    }

    override fun serialize(method: ObtainMethod, type: Type, context: JsonSerializationContext): JsonElement {
        val json = context.serialize(method).asJsonObject
        val variant = this.types.entries.find { it.value == method::class }?.key ?: throw IllegalArgumentException("Cannot resolve variant for type ${method::class.qualifiedName}")
        json.addProperty(VARIANT, variant)
        return json
    }
}