package com.cobblemon.mod.common.api.pokedex

import com.cobblemon.mod.common.api.storage.player.InstancedPlayerData
import com.cobblemon.mod.common.api.storage.player.client.ClientPokedexManager
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.UUID

class PokedexManager(
    override val uuid: UUID,
    override val entries: MutableMap<String, String>
) : AbstractPokedexManager(), InstancedPlayerData {

    fun gainedSeenStatus(species: Species, form: FormData) {
        val curKnowledge = getKnowledgeForSpecies(species.resourceIdentifier)
        if (curKnowledge == PokedexEntryProgress.NONE) {
            entries[NUM_SEEN_KEY] = (entries[NUM_SEEN_KEY]?.toInt()?.plus(1)).toString()
        }
    }

    fun gainedCaughtStatus(pokemon: Pokemon) {
        val curKnowledge = getKnowledgeForSpecies(pokemon.species.resourceIdentifier)
        if (curKnowledge == PokedexEntryProgress.ENCOUNTERED) {
            val numSeenValue = entries[NUM_SEEN_KEY] ?: "0"
            entries[NUM_SEEN_KEY] = (numSeenValue.toInt().minus(1)).toString()
        }
        if (curKnowledge != PokedexEntryProgress.CAUGHT) {
            val numCaughtValue = entries[NUM_CAUGHT_KEY] ?: "0"
            entries[NUM_CAUGHT_KEY] = (numCaughtValue.toInt().plus(1)).toString()
        }

        val speciesId = pokemon.species.resourceIdentifier
        val formName = pokemon.form.formOnlyShowdownId()

        entries[getKnowledgeKeyForSpecies(speciesId)] = PokedexEntryProgress.CAUGHT.serializedName
        entries[getKnowledgeKeyForForm(speciesId, formName)] = PokedexEntryProgress.CAUGHT.serializedName
    }

    companion object {
        val CODEC = RecordCodecBuilder.create<PokedexManager> { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("uuid").forGetter { it.uuid.toString() },
                Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("entries").forGetter { it.entries }
            ).apply(instance) { uuid, map ->
                //Codec stuff seems to deserialize to an immutable map, so we have to convert it to mutable explicitly
                PokedexManager(UUID.fromString(uuid), map.toMutableMap())
            }
        }
    }

    override fun toClientData() = ClientPokedexManager(entries, false)
}