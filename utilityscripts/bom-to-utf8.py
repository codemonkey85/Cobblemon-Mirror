import os
import codecs

# Specify the directory you want to convert JSON files in
directory = '../common/src/main/resources/data/cobblemon/species/generation1'

# Get a list of all files in the directory
files = os.listdir(directory)

# Loop through each file
for filename in files:
    # Check if the file is a JSON file
    if filename.endswith('.json'):
        # Construct the full file path
        filepath = os.path.join(directory, filename)
        
        # Open the file with 'utf-8-sig' encoding and read its contents
        with codecs.open(filepath, 'r', 'utf-8-sig') as file:
            contents = file.read()
        
        # Write the contents back to the file with 'utf-8' encoding
        with codecs.open(filepath, 'w', 'utf-8') as file:
            file.write(contents)
print('Done');
