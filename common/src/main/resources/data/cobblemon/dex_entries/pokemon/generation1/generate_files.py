import re

# List of original 151 Pokémon names prefixed with "cobblemon:" and starting with lowercase letters
pokemon_list = [
    "cobblemon:bulbasaur", "cobblemon:ivysaur", "cobblemon:venusaur", "cobblemon:charmander",
    "cobblemon:charmeleon", "cobblemon:charizard", "cobblemon:squirtle", "cobblemon:wartortle",
    "cobblemon:blastoise", "cobblemon:caterpie", "cobblemon:metapod", "cobblemon:butterfree",
    "cobblemon:weedle", "cobblemon:kakuna", "cobblemon:beedrill", "cobblemon:pidgey",
    "cobblemon:pidgeotto", "cobblemon:pidgeot", "cobblemon:rattata", "cobblemon:raticate",
    "cobblemon:spearow", "cobblemon:fearow", "cobblemon:ekans", "cobblemon:arbok",
    "cobblemon:pikachu", "cobblemon:raichu", "cobblemon:sandshrew", "cobblemon:sandslash",
    "cobblemon:nidoran♀", "cobblemon:nidorina", "cobblemon:nidoqueen", "cobblemon:nidoran♂",
    "cobblemon:nidorino", "cobblemon:nidoking", "cobblemon:clefairy", "cobblemon:clefable",
    "cobblemon:vulpix", "cobblemon:ninetales", "cobblemon:jigglypuff", "cobblemon:wigglytuff",
    "cobblemon:zubat", "cobblemon:golbat", "cobblemon:oddish", "cobblemon:gloom",
    "cobblemon:vileplume", "cobblemon:paras", "cobblemon:parasect", "cobblemon:venonat",
    "cobblemon:venomoth", "cobblemon:diglett", "cobblemon:dugtrio", "cobblemon:meowth",
    "cobblemon:persian", "cobblemon:psyduck", "cobblemon:golduck", "cobblemon:mankey",
    "cobblemon:primeape", "cobblemon:growlithe", "cobblemon:arcanine", "cobblemon:poliwag",
    "cobblemon:poliwhirl", "cobblemon:poliwrath", "cobblemon:abra", "cobblemon:kadabra",
    "cobblemon:alakazam", "cobblemon:machop", "cobblemon:machoke", "cobblemon:machamp",
    "cobblemon:bellsprout", "cobblemon:weepinbell", "cobblemon:victreebel", "cobblemon:tentacool",
    "cobblemon:tentacruel", "cobblemon:geodude", "cobblemon:graveler", "cobblemon:golem",
    "cobblemon:ponyta", "cobblemon:rapidash", "cobblemon:slowpoke", "cobblemon:slowbro",
    "cobblemon:magnemite", "cobblemon:magneton", "cobblemon:farfetch'd", "cobblemon:doduo",
    "cobblemon:dodrio", "cobblemon:seel", "cobblemon:dewgong", "cobblemon:grimer",
    "cobblemon:muk", "cobblemon:shellder", "cobblemon:cloyster", "cobblemon:gastly",
    "cobblemon:haunter", "cobblemon:gengar", "cobblemon:onix", "cobblemon:drowzee",
    "cobblemon:hypno", "cobblemon:krabby", "cobblemon:kingler", "cobblemon:voltorb",
    "cobblemon:electrode", "cobblemon:exeggcute", "cobblemon:exeggutor", "cobblemon:cubone",
    "cobblemon:marowak", "cobblemon:hitmonlee", "cobblemon:hitmonchan", "cobblemon:lickitung",
    "cobblemon:koffing", "cobblemon:weezing", "cobblemon:rhyhorn", "cobblemon:rhydon",
    "cobblemon:chansey", "cobblemon:tangela", "cobblemon:kangaskhan", "cobblemon:horsea",
    "cobblemon:seadra", "cobblemon:goldeen", "cobblemon:seaking", "cobblemon:staryu",
    "cobblemon:starmie", "cobblemon:mr. mime", "cobblemon:scyther", "cobblemon:jynx",
    "cobblemon:electabuzz", "cobblemon:magmar", "cobblemon:pinsir", "cobblemon:tauros",
    "cobblemon:magikarp", "cobblemon:gyarados", "cobblemon:lapras", "cobblemon:ditto",
    "cobblemon:eevee", "cobblemon:vaporeon", "cobblemon:jolteon", "cobblemon:flareon",
    "cobblemon:porygon", "cobblemon:omanyte", "cobblemon:omastar", "cobblemon:kabuto",
    "cobblemon:kabutops", "cobblemon:aerodactyl", "cobblemon:snorlax", "cobblemon:articuno",
    "cobblemon:zapdos", "cobblemon:moltres", "cobblemon:dratini", "cobblemon:dragonair",
    "cobblemon:dragonite", "cobblemon:mewtwo", "cobblemon:mew"
]

# Function to remove non-ASCII characters from a string
def remove_non_ascii(s):
    return re.sub(r'[^\x00-\x7F]', '', s)

# Clean the Pokémon names in the list by removing non-ASCII characters
cleaned_pokemon_list = [f"cobblemon:{remove_non_ascii(pokemon.split(':')[1])}" for pokemon in pokemon_list]

# Base string where "abra" will be replaced
base_string = """
{
  "id": "cobblemon:abra",
  "entryId": "cobblemon:abra",
  "displayEntry": 0,
  "variations": [
    {
      "type": "cobblemon:basic_pokedex_variation",
      "aspects": "",
      "conditions": ["q.get_dex_key('cobblemon.pokedex.abra.knowledge.normal') == 'CAUGHT'"]
    }
  ]
}
"""

# Iterate through the cleaned list and replace "abra" with each Pokémon
for pokemon in cleaned_pokemon_list:
    pokemon_name = pokemon.split(":")[1]
    replaced_string = base_string.replace("abra", pokemon_name)
    
    # Save the output to a file named {pokemon_name}.json
    with open(f"{pokemon_name}.json", "w") as file:
        file.write(replaced_string)
