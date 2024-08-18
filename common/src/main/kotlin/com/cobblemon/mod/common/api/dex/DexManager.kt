package com.cobblemon.mod.common.api.dex

import com.cobblemon.mod.common.api.events.battles.BattleStartedPostEvent
import com.cobblemon.mod.common.api.events.pokedex.scanning.PokemonScannedEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.api.events.pokemon.TradeCompletedEvent
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionCompleteEvent
import com.cobblemon.mod.common.api.events.starter.StarterChosenEvent
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.api.storage.player.InstancedPlayerData
import com.cobblemon.mod.common.api.storage.player.client.ClientDexManager
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Species
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.UUID

class DexManager(
    override val uuid: UUID,
    override val entries: MutableMap<String, String>
) : AbstractDexManager(), InstancedPlayerData {

    fun grantedWithCommand(species: Species, form: FormData) {
        gainedCaughtStatus(species, form)
        entries[getCaptureMethodKeyForSpecies(species.resourceIdentifier)] = "grant"
    }

    fun removedWithCommand(species: Species, form: FormData) {
        entries.remove(getKnowledgeKeyForSpecies(species.resourceIdentifier))
        entries.remove(getCaptureMethodKeyForSpecies(species.resourceIdentifier))
    }

    fun pokemonCaught(event: PokemonCapturedEvent) {
        gainedCaughtStatus(event.pokemon.species, event.pokemon.form)
        entries[getCaptureMethodKeyForSpecies(event.pokemon.species.resourceIdentifier)] = "caught"
    }

    fun pokemonEvolved(event: EvolutionCompleteEvent) {
        gainedCaughtStatus(event.pokemon.species, event.pokemon.form)
        entries[getCaptureMethodKeyForSpecies(event.pokemon.species.resourceIdentifier)] = "evolved"
    }

    fun pokemonTraded(event: TradeCompletedEvent) {
        val recievedPokemon = if (event.tradeParticipant1.uuid == uuid) event.tradeParticipant2Pokemon else event.tradeParticipant1Pokemon
        gainedCaughtStatus(recievedPokemon.species, recievedPokemon.form)
        entries[getCaptureMethodKeyForSpecies(recievedPokemon.species.resourceIdentifier)] = "traded"
    }

    fun onStarterChosen(event: StarterChosenEvent) {
        gainedCaughtStatus(event.pokemon.species, event.pokemon.form)
        entries[getCaptureMethodKeyForSpecies(event.pokemon.species.resourceIdentifier)] = "starter"
    }

    fun pokemonScanned(event: PokemonScannedEvent) {
        gainedSeenStatus(event.pokemon.species, event.pokemon.form)
        entries[getCaptureMethodKeyForSpecies(event.pokemon.species.resourceIdentifier)] = "scanned"
    }

    fun battleStart(event: BattleStartedPostEvent) {
        event.battle.actors.forEach {
            if (uuid !in it.getPlayerUUIDs()) {
                if (it is PokemonBattleActor) {
                    val pokemon = it.pokemon.originalPokemon
                    gainedSeenStatus(pokemon.species, pokemon.form)
                    entries[getCaptureMethodKeyForSpecies(pokemon.species.resourceIdentifier)] = "scanned"
                }
                //Ideally we would not trigger the seen stuff on unseen pokemon but I don't think we currently have a way to listen to
                //Sendout events in battle (though I have not looked that hard!)
                else if (it is PlayerBattleActor) {
                    it.pokemonList.forEach { mon ->
                        val pokemon = mon.originalPokemon
                        gainedSeenStatus(pokemon.species, pokemon.form)
                        entries[getCaptureMethodKeyForSpecies(pokemon.species.resourceIdentifier)] = "scanned"
                    }
                }
            }
        }
    }

    fun gainedSeenStatus(species: Species, form: FormData) {
        val curKnowledge = getKnowledgeForSpecies(species.resourceIdentifier)
        if (curKnowledge == PokedexEntryProgress.NONE) {
            entries[NUM_SEEN_KEY] = (entries[NUM_SEEN_KEY]?.toInt()?.plus(1)).toString()
        }
    }

    fun gainedCaughtStatus(species: Species, form: FormData) {
        val curKnowledge = getKnowledgeForSpecies(species.resourceIdentifier)
        if (curKnowledge == PokedexEntryProgress.ENCOUNTERED) {
            entries[NUM_SEEN_KEY] = (entries[NUM_SEEN_KEY]?.toInt()?.minus(1)).toString()
        }
        if (curKnowledge != PokedexEntryProgress.CAUGHT) {
            entries[NUM_CAUGHT_KEY] = (entries[NUM_SEEN_KEY]?.toInt()?.plus(1)).toString()
        }
        entries[getKnowledgeKeyForSpecies(species.resourceIdentifier)] = PokedexEntryProgress.CAUGHT.serializedName
    }

    companion object {
        val CODEC = RecordCodecBuilder.create<DexManager> { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("uuid").forGetter { it.uuid.toString() },
                Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("entries").forGetter { it.entries }
            ).apply(instance) { uuid, map ->
                DexManager(UUID.fromString(uuid), map)
            }
        }
    }

    override fun toClientData() = ClientDexManager(entries, false)
}