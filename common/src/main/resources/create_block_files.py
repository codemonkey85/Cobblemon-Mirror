import os
import json

dir = os.getcwd()
assets_dir = dir + "/assets/cobblemon"
data_dir = dir + "/data/cobblemon"

id = input("Enter the ID for this block: ")
print("------------------------")

blockstate_template = {
  "variants": {
    "": {
      "model": "cobblemon:block/{}".format(id)
    }
  }
}
print("Generated blockstate template")

block_model_template = {
  "parent": "block/cube_all",
  "textures": {
    "all": "cobblemon:block/{}".format(id)
  }
}
print("Generated block model template")

item_model_template = {
  "parent": "cobblemon:block/{}".format(id)
}
print("Generated item model template")

loot_table_template = {
  "type": "minecraft:block",
  "pools": [{
    "rolls": 1,
    "entries": [{
      "type": "minecraft:item",
      "name": "cobblemon:{}".format(id)
    }],
    "conditions": [{
      "condition": "minecraft:survives_explosion"
    }]
  }]
}
print("Generated loot table template")
print("------------------------")

with open(assets_dir + "/blockstates/{}.json".format(id), "x") as f:
  json.dump(blockstate_template, f, indent=3)
print("Created blockstate file")

with open(assets_dir + "/models/block/{}.json".format(id), "x") as f:
  json.dump(block_model_template, f, indent=3)
print("Created block model file")

with open(assets_dir + "/models/item/{}.json".format(id), "x") as f:
  json.dump(item_model_template, f, indent=3)
print("Created item model file")

with open(data_dir + "/loot_tables/blocks/{}.json".format(id), "x") as f:
  json.dump(loot_table_template, f, indent=3)
print("Created loot table file")
print("------------------------")

print("Succesfully created block: {}".format(id))
print("You still need to:")
print("  - Create recipe file")
print("  - Add block and item definitions in CobblemonBlocks and CobblemonItems respectively")
print("  - Add the block to a creative tab")
print("  - Add textures")
print("  - Add lang")