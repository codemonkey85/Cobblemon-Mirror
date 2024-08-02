import re

# Define the path to the PokemonModelRepository.kt file
repository_file = '../common/src/main/kotlin/com/cobblemon/mod/common/client/render/models/blockbench/repository/PokemonModelRepository.kt'

# Initialize an empty list to store the Pokemon names
pokemon_names_in_repository = []

# Open the PokemonModelRepository.kt file
with open(repository_file, 'r') as file:
    # Read the file
    content = file.read()
    # Find all the Pokemon names mentioned in the inbuilt function
    pokemon_names_in_repository = re.findall(r'inbuilt\("(.*?)"', content)

# Print the Pokemon names
print(pokemon_names_in_repository)
