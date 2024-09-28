import csv
import json
import os

# special thanks to Rosemary for writing this script - JPAK

# ignore "shiny" and "female" aspects
# every pokemon should have a form with an empty string

# get species, aspects, generation
# for each generation grab national dex number
# hash map of dex number as key, to generation

species_directory = 'species'

generations = {}

for generation in os.listdir(species_directory):
    generation_directory = os.path.join(species_directory, generation)
    if os.path.isdir(generation_directory):
        for species_filename in os.listdir(generation_directory):
            species_path = os.path.join(generation_directory, species_filename)
            species_file = open(species_path, "r", encoding="utf-8")
            try:
                species_json = json.loads(species_file.read())
                nat_dex_num = species_json["nationalPokedexNumber"]
                generations[nat_dex_num] = generation
            except:
                print(species_filename)

resolvers_directory = 'resolvers'

def is_valid_aspect(aspect):
    return not ("female" in aspect or "shiny" in aspect)

num_digits = 4
csv_data = []

for pokemon_foldername in os.listdir(resolvers_directory):
    nat_dex_num = int(pokemon_foldername[0:num_digits])
    if nat_dex_num not in generations:
        continue
    generation = generations[nat_dex_num]
    pokemon_directory = os.path.join(resolvers_directory, pokemon_foldername)
    if os.path.isdir(pokemon_directory):
        aspects_set = {}
        aspects_set["normal"] = False

        for pokemon_filename in os.listdir(pokemon_directory):
            pokemon_path = os.path.join(pokemon_directory, pokemon_filename)
            pokemon_file = open(pokemon_path, "r", encoding="utf-8")
            pokemon_json = json.loads(pokemon_file.read())

            species = pokemon_json["species"]

            variations = pokemon_json["variations"]
            for variation in variations:
                aspects = variation["aspects"]
                for aspect in aspects:
                    if (is_valid_aspect(aspect)):
                        aspects_set[aspect] = False

        for aspect in aspects_set.keys():
            csv_data.append([generation, species, aspect])

with open('pokemon_data.csv', 'w', newline='') as csvfile:
    pokemon_writer = csv.writer(csvfile, delimiter=' ',
                            quotechar='|', quoting=csv.QUOTE_MINIMAL)
    
    pokemon_writer.writerow(["pokedex", "identifier", "form"])
    for row in csv_data:
        pokemon_writer.writerow(row)