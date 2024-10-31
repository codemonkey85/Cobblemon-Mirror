import os
import json

# Define input and output directories
input_dir = '../common/src/main/resources/data/cobblemon/species'
output_dir = '../common/src/main/resources/data/cobblemon/dex_entries/pokemon/{generation}'

def read_json(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        return json.load(file)

def write_json(data, file_path):
    with open(file_path, 'w', encoding='utf-8') as file:
        json.dump(data, file, indent=2)

def process_species_json(file_name, species_json):
    file_name = file_name.replace('.json', '')
    base_entry = {
        "id": f"cobblemon:{file_name}",
        "speciesId": f"cobblemon:{file_name}",
        "displayAspects": [],
        "conditionAspects": [],
        "forms": [],
        "variations": [],
        "dex": species_json['nationalPokedexNumber']
    }

    special_forms = ["Galar", "Alola", "Hisui"]
    additional_entries = []

    if 'forms' in species_json:
        normal_form = {"displayForm": "Normal", "unlockForms": ["Normal"]}
        for form in species_json['forms']:
            form_name = form['name']
            named_form = f"-{form_name.lower()}ian".replace('lolaian', 'lolan').replace('isuiian', 'isuian')
            if form_name in special_forms:
                additional_entry = base_entry.copy()
                additional_entry['dex'] = species_json['nationalPokedexNumber']
                additional_entry['id'] += named_form
                additional_entry['forms'] = [{"displayForm": form_name, "unlockForms": [form_name]}]
                additional_entries.append(additional_entry)
            else:
                normal_form['unlockForms'].append(form_name)
        base_entry['forms'].append(normal_form)
    else:
        base_entry['forms'].append({"displayForm": "Normal", "unlockForms": ["Normal"]})

    return [base_entry] + additional_entries

def main():
    for root, _, files in os.walk(input_dir):
        for file_name in files:
            if file_name.endswith('.json'):
                species_json = read_json(os.path.join(root, file_name))
                entries = process_species_json(file_name, species_json)
                for entry in entries:
                    output_file_name = f"{entry['id'].split(':')[1]}.json"
                    dex = entry['dex']
                    del entry['dex']
                    is_alolan = 'lolan' in entry['id']
                    is_galarian = 'alarian' in entry['id']
                    is_hisuian = 'isuian' in entry['id']
                    is_original_form = not is_alolan and not is_galarian and not is_hisuian

                    # Ehhhh use region rather than gen, galarian and hisuian are both gen8

                    folder = 'paldea'
                    if dex <= 151 and is_original_form:
                        folder = 'kanto'
                    elif dex <= 251 and is_original_form:
                        folder = 'johto'
                    elif dex <= 386 and is_original_form:
                        folder = 'hoenn'
                    elif dex <= 493 and is_original_form:
                        folder = 'sinnoh'
                    elif dex <= 649 and is_original_form:
                        folder = 'unova'
                    elif dex <= 721 and is_original_form:
                        folder = 'kalos'
                    elif (dex <= 809 and is_original_form) or is_alolan:
                        folder = 'alola'
                    elif (dex <= 905 and is_original_form) or is_galarian:
                        folder = 'galar'
                    elif dex <= 905 and not is_original_form and is_hisuian:
                        folder = 'hisui'
                    write_json(entry, os.path.join(output_dir.format(generation=folder), output_file_name))

if __name__ == "__main__":
    main()