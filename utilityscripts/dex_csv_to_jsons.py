import csv
import json

dexes = {}

with open('pokedex.csv', newline='') as csvfile:
    data_reader = csv.reader(csvfile, delimiter=',', quotechar='|')

    for row in data_reader:
        if(row[0] == 'pokedex'): continue

        dex_name = row[0]
        if dex_name not in dexes:
            dexes[dex_name] = {}

        dex = dexes[dex_name]

        identifier = row[1]
        form = row[2]
        category = row[3]
        tags = row[4]
        invisible_until_found = row[5]
        visual_number = row[6]
        skip_auto_numbering = row[7]

        pokemon = {}
        if identifier in dex:
            pokemon = dex[identifier]
        pokemon['identifier'] = identifier

        if 'forms' not in pokemon:
            pokemon['forms'] = []
        pokemon['forms'].append(form)

        if 'category' not in pokemon:
            pokemon['category'] = category

        if 'tags' not in pokemon:
            pokemon['tags'] = []
        for tag in tags:
            pokemon['tags'].append(tag)
        
        if 'invisible_until_found' not in pokemon:
            pokemon['invisible_until_found'] = False
        if invisible_until_found != '' and bool(invisible_until_found):
            pokemon['invisible_until_found'] = True
        
        if 'visual_number' not in pokemon:
            pokemon['visual_number'] = ''
        if(visual_number != '' and pokemon['visual_number'] == ''):
            pokemon['visual_number'] = visual_number

        if 'skip_auto_numbering' not in pokemon:
            pokemon['skip_auto_numbering'] = False
        if skip_auto_numbering != '' and bool(skip_auto_numbering):
            pokemon['skip_auto_numbering'] = True
        

        dex[identifier] = pokemon

for dex_name in dexes.keys():
    dex = dexes[dex_name]
    json_dict = {'identifier':f'cobblemon:{dex_name}'}
    pokemon_list = []
    for entry in dex.values():
        
        json_entry = {'identifier':entry['identifier']}
        
        if not (len(entry['forms']) == 1 and entry['forms'][0] == 'normal'):
            json_entry['forms'] = entry['forms']
        
        if entry['category'] != '':
            json_entry['category'] = entry['category']
        
        if len(entry['tags']) != 0:
            json_entry['tags'] = entry['tags']
        
        if entry['invisible_until_found'] == True:
            json_entry['invisible_until_found'] = entry['invisible_until_found']
        
        if entry['visual_number'] != '':
            json_entry['visual_number'] = entry['visual_number']
        
        if entry['skip_auto_numbering'] == True:
            json_entry['skip_auto_numbering'] = entry['skip_auto_numbering']

        pokemon_list.append(json_entry)
    
    json_dict['pokemon_list'] = pokemon_list

    with open(f'{dex_name}.json', "w") as outfile:
        outfile.write(json.dumps(json_dict, indent=4))