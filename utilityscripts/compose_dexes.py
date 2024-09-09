import os
import json

# List of folders to process
folder_prefix = '../common/src/main/resources/data/cobblemon/dex_entries/pokemon/'
species_folder = '../common/src/main/resources/data/cobblemon/species/'

folders = [
    'kanto',
    'johto',
    'hoenn',
    'sinnoh',
    'unova',
    'kalos',
    'alola',
    'galar',
    'hisui',
    'paldea'
]

def read_json(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        return json.load(file)

sort_order = 1
dex_numbers = {}
for folder in folders:
    entries = []
    for root, dirs, files in os.walk(species_folder):
        for file in files:
            species = read_json(os.path.join(root, file))
            dex_numbers[file.replace('.json', '')] = species['nationalPokedexNumber']

    for root, dirs, files in os.walk(folder_prefix + folder):
        for file in files:
            if file.endswith('.json'):
                # Strip the file extension and prefix with "cobblemon:"
                entry_name = f"cobblemon:{os.path.splitext(file)[0]}"
                entries.append(entry_name)

    # Create the dictionary in the required format
    pokedex_data = {
        "type": "cobblemon:simple_pokedex_def",
        "id": f"cobblemon:{os.path.basename(folder)}",
        "sortOrder": sort_order,
        "entries": sorted(entries, key=lambda entry: dex_numbers[entry.split(':')[1].split('-')[0]])
    }

    sort_order += 1

    # Write the dictionary to a new JSON file
    output_file = os.path.join(folder_prefix + folder, f"{os.path.basename(folder)}.json")
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(pokedex_data, f, ensure_ascii=False, indent=2)

print('Done!')