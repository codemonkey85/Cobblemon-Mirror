import json

"""
HOW TO USE

In the same directory as this file, place the following:
- A copy of the language file you would like to write to
- The sounds.json file
- An empty "new_sounds.json" file

Replace the string on line 19 with the name of your lang file.
Replace the variable on line 20 with the string the subtitles will default to. Type "[POKEMON]" where you would like the species name to appear.

Copy only the section of the language file that has the subtitles and add it to the end of the original language file.

If this breaks, ping @whatsy.json on discord
"""

lang_file = "en_us.json"
default_string = "[POKEMON] cries"

with open('sounds.json') as f:
    sounds_data = json.load(f)
    print("Loaded sounds.json")
    cries = []
    for i in sounds_data:
        if ".cry" in i:
            cries.append(i)

    print(str(len(cries)) + " cries found.")

    file = open('new_sounds.json')
    new = json.load(file)

    for i in cries:
        seperated = i.split(".")
        pokemon = seperated[1]

        file = open('new_sounds.json')

        sounds = new[i]["sounds"]

        new[i] = {
            "subtitle": "cobblemon.subtitle." + pokemon + ".cry",
            "sounds": sounds
        }

        print("Wrote subtitle for '" + i + "'.")

    file.close()
    file = open('new_sounds.json', "w")
    file.write(json.dumps(new, indent=2))
    file.close()

    print("Succesfully wrote to new_sounds.json")

with open(lang_file) as f:
    lang_data = json.load(f)
    print("Loaded " + lang_file)

    species = []
    species_names = []
    index = 0

    for i in lang_data:
        if ".name" in i and ".species" in i:
            species.append(i.split(".")[2])
            species_names.append(lang_data[i])

    print("Loaded " + str(len(species)) + " species IDs and names.")

    for s in species:
        string = default_string.replace("[POKEMON]", species_names[index])
        lang_data["cobblemon.subtitle." + s + ".cry"] = string
        index += 1

    file = open(lang_file, "w")
    file.write(json.dumps(lang_data, indent=2))
    file.close()
    print("Succesfully wrote to " + lang_file)


