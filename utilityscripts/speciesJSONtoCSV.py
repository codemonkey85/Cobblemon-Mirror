import os
import json
import csv

def find_json_files(directory):
    """Recursively find all JSON files in the given directory and subdirectories."""
    json_files = []
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.json'):
                json_files.append(os.path.join(root, file))
    return json_files


def process_behavior_field(entry):
    """Process the behavior field to create 'behavior - x - y' entries."""
    behavior_data = entry.pop('behaviour', None)
    if behavior_data:
        for key, value in behavior_data.items():
            if isinstance(value, dict):
                # Process nested dictionary
                for subkey, subvalue in value.items():
                    entry['behaviour - {} - {}'.format(key, subkey)] = json.dumps(subvalue)
            else:
                # Directly assign the value if not a dictionary
                entry['behaviour - {}'.format(key)] = json.dumps(value)


def process_pokemon_entry(entry, pokemon_data, base_pokemon_name, is_base_form=True):
    """Process a single Pokémon entry, extracting relevant fields."""
    # Format the name column
    if is_base_form:
        entry['name'] = base_pokemon_name
    else:
        form_name = entry.get('name', '')
        entry['name'] = f"{base_pokemon_name} [{form_name}]"

    # Process behavior field
    process_behavior_field(entry)

    pokemon_data.append(entry)

def parse_pokemon_data(json_file):
    """Parse the JSON file and extract Pokémon data, including forms."""
    with open(json_file, 'r', encoding='utf-8-sig') as file:
        data = json.load(file)
        pokemon_data = []
        pokemon_name = data.get('name', 'Unknown')  # Default to 'Unknown' if no name is present

        # Process base Pokémon entry
        process_pokemon_entry(data, pokemon_data, pokemon_name)

        # Handling forms
        if "forms" in data:
            for form in data["forms"]:
                form_data = data.copy()
                form_data.update(form)  # Merge form data with the base Pokémon data
                process_pokemon_entry(form_data, pokemon_data, pokemon_name, is_base_form=False)

    return pokemon_data


def write_to_csv(pokemon_data, csv_file_path):
    """Write the Pokémon data to a CSV file."""
    fieldnames = set()
    for entry in pokemon_data:
        fieldnames.update(entry.keys())

    # Exclude 'drops' and 'forms' fields
    fieldnames.discard('drops')
    fieldnames.discard('forms')

    # Sort fieldnames with specified columns first
    priority_fields = ['nationalPokedexNumber', 'name', 'baseStats', 'primaryType', 'secondaryType']
    sorted_fieldnames = sorted(fieldnames - set(priority_fields))
    sorted_fieldnames = priority_fields + sorted_fieldnames

    with open(csv_file_path, 'w', newline='', encoding='utf-8') as csvfile:
        writer = csv.DictWriter(csvfile, fieldnames=sorted_fieldnames)
        writer.writeheader()
        for entry in pokemon_data:
            # Remove 'drops' and 'forms' data from each entry
            entry.pop('drops', None)
            entry.pop('forms', None)
            writer.writerow(entry)


def log_unhandled_fields(pokemon_data, log_file_path):
    """Log any unhandled fields in the Pokémon data."""
    unhandled_fields = set()
    known_fields = set()  # Add known fields here

    for entry in pokemon_data:
        for key in entry.keys():
            if key not in known_fields:
                unhandled_fields.add(key)

    with open(log_file_path, 'w', encoding='utf-8') as logfile:
        for field in sorted(unhandled_fields):
            logfile.write(f"{field}\n")


def main(data_dir, csv_out_path, log_out_path):
    """Main function to process JSON files and output CSV and log files."""
    json_files = find_json_files(data_dir)
    all_pokemon_data = []

    # Process each JSON file
    for json_file in json_files:
        pokemon_data = parse_pokemon_data(json_file)
        all_pokemon_data.extend(pokemon_data)

    # Write the compiled data to CSV
    write_to_csv(all_pokemon_data, csv_out_path)

    # Log unhandled fields
    log_unhandled_fields(all_pokemon_data, log_out_path)
    print("Processing complete. Data written to CSV and unhandled fields logged.")


# Example usage (with placeholder paths)
directory = '../common/src/main/resources/data/cobblemon/species'  # Directory containing JSON files
csv_output_path = "generatedSpeciesData.csv"  # Desired CSV output file path
log_output_path = "speciesReport.txt"  # Desired log file path

if __name__ == "__main__":
    main(directory, csv_output_path, log_output_path)
