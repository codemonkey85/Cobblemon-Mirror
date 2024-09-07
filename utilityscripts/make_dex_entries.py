import os
import json

# Define source and destination directories
source_dir = '../common/src/main/resources/data/cobblemon/species'
destination_dir = '../common/src/main/resources/data/cobblemon/dex_entries/pokemon'

# List of predefined folders to check
folders_to_check = ['generation1', 'generation2', 'generation3', 'generation4', 'generation5', 'generation6', 'generation7', 'generation8', 'generation9']

# Predefined JSON format function
def create_json_content(file_name):
    file_name = file_name.replace('.json', '')
    return {
      "id": f"cobblemon:{file_name}",
      "speciesId": f"cobblemon:{file_name}",
      "displayAspects": [],
      "conditionAspects": [],
      "forms": [],
      "variations": []
    }

# Function to ensure the destination directory exists
def ensure_dir_exists(path):
    if not os.path.exists(path):
        os.makedirs(path)

# Process each predefined folder
for folder in folders_to_check:
    source_folder_path = os.path.join(source_dir, folder)
    dest_folder_path = os.path.join(destination_dir, folder)
    #ensure_dir_exists(dest_folder_path)

    # List all files in the current folder
    for file in os.listdir(source_folder_path):
        file_path = os.path.join(source_folder_path, file)

        # Create new JSON content
        json_content = create_json_content(file)

        # Write JSON content to new file in destination directory
        dest_file_path = os.path.join(dest_folder_path, f"{os.path.splitext(file)[0]}.json")
        with open(dest_file_path, 'w') as json_file:
            json.dump(json_content, json_file, indent=2)

print("JSON files created successfully.")