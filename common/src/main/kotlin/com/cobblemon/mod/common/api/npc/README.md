# Cobbled NPCs

NPC's are AI-driven entities that are built on the same identifier-aspects rendering platform
as Cobblemon's Pokémon entities. Every NPC in the world is based on an [NPC class](#npc-classes).

## NPC Classes

An NPC class is a datapacked JSON (inside the <code>npcs</code> folder) that defines many properties of an NPC in a way that's reusable
across any number of individual NPC entities. The possible properties are explained in [NPC Properties](#npc-properties)

## NPC Class Presets

For many properties or sets of properties of an NPC class, there may be logical grouping of settings.
As an example, many types of trainer NPC classes could share the logic of challenging nearby players
that they are able to see. This could become tedious if the same AI script and range config needed to
be defined on every NPC class. Not only is it tedious, but if there was some tweak that was made in the
underlying logic of that AI, it would need to be updated in every single NPC class JSON that uses it.

The solution to this is NPC Class Presets, a datapack folder (<code>npc_presets</code>) with JSON files that have an almost identical
format to [NPC Classes](#npc-classes), but with every property being nullable. An NPC class can reference
any number of presets and the properties of those presets will be merged into the NPC class as it loads.

## NPC Properties
    {
      "hitbox": "player",
      "resourceIdentifier": "cobblemon:bug_catcher",
      "presets": [
        "cobblemon:npc-standard"
      ],
      "variations": {
        "dirt": [
          "clean",
          "slightly_dirty",
          "dirty",
          "filthy"
        ],
        "net": {
          "type": "weighted",
          "options": [
            {
              "aspect": "green-net",
              "weight": 10
            },
            {
              "aspect": "blue-net",
              "weight": 5
            },
            {
              "aspect": "red-net",
              "weight": 1
            }
          ]
        }
      },
      "config": [
        {
          "variableName": "challenge_distance",
          "displayName": "cobblemon.npc.challenge_distance",
          "description": "cobblemon.npc.challenge_distance.description",
          "type": "number",
          "default": "5"
        }
      ],
      "names": [
        "Vera",
        "Hiro",
        "John Cobblemon"
      ],
      "interaction": {
        "type": "dialogue",
        "dialogue": "cobblemon:npc-example"
      },
      "battleConfiguration": {
        "canChallenge": true
      },
      "canDespawn": false,
      "skill": 5,
      "party": {
        "type": "simple",
        "pokemon": [
          "spiritomb level=1 moves=splash,firepunch,flamethrower,swift",
          "porygonz level=1 speed_ev=252",
          "togekiss level=1 nature=timid",
          "lucario level=1 held_item=cobblemon:focus_sash",
          "milotic level=1 ability=marvelscale ",
          "garchomp level=1 hp_iv=31 attack_iv=31"
        ]
      }
    }

### hitbox
The hitbox of the NPC, relevant to combat and pathing. This can be defined as an object 
or as a shortcut string that is predefined in the code. The only currently hardcoded
hitbox name is "player", which is the standard Steve Minecraft hitbox and is the same as
this fully defined hitbox:
    
    "hitbox": {
      "width": 0.6,
      "height": 1.8
    }

### resourceIdentifier
An optional property representing the 'identifier' that will be used for rendering the NPC.
In a way, this is the same as the "species" of a Pokémon. It is this value combined with
whatever aspects have been defined for the NPC that completely handles the appearance of the
NPC. This identifier is referenced in the variation JSONs on the client.

If left blank, it will fallback to the identifier used for the NPC class itself. The reason why
this is useful is that it allows several NPC classes to appear the same despite having differences
in AI, parties, interactions, etc.

### presets
A list of strings representing the identifiers of [NPC Class Presets](#npc-class-presets) that form the foundation of
this NPC class. Any property defined in this class will override the same property in the presets.

### variations
A map of variations that generate the aspects that control the appearance of the NPC. The keys of this
map should represent the kind of variation that is being defined, and the values are some kind of [NPC Variation](#npc-variation).

Variations can be defined in presets and flow through to the NPC class, but they can also be defined
in the NPC class. If the same name is used in both a preset and an NPC class, that which is on the NPC
class will override that from the preset. If the variation names are different, they will be merged together.

#### NPC Variation
An NPC variation is some way in which aspects can be generated for the NPC. The types of variation can
be added using the API, but the type most anticipated is the weighted choice aspects.

This can be defined as a list of strings, in which case the aspect will be chosen randomly from the list
with uniform distribution.

    "dirt": [
      "clean",
      "slightly_dirty",
      "dirty",
      "filthy"
    ]

The weighted variation can also be defined more fully, which is necessary if you want some options to be
more likely than others.

    "net": {
      "type": "weighted",
      "options": [
        {
          "aspect": "green-net",
          "weight": 10
        },
        {
          "aspect": "blue-net",
          "weight": 5
        },
        {
          "aspect": "red-net",
          "weight": 1
        }
      ]
    }

In this example, the "net" value will be "green-net" most of the time, 10 times out of 16. The "blue-net"
aspect will be chosen 5 times out of 16, and the "red-net" aspect will be chosen 1 time out of 16. 

When using a type of NPC variation that is not "weighted", the value of "type" must specify it and the
other values (instead of "options") will be in whatever format is dictated by that type.

### config
A list of NPC config values that are predefined as being necessary for this class. An NPC config 
variable's value is used in the MoLang environment of the NPC and can be accessed in any MoLang 
environment that has the NPC. 

This class property is a way to present clear options to the user if configuring an NPC in-game using
the NPC Edit command. This does not mean that NPC variables can only be created when they were defined
in the class, but it makes it easier for the user to know what variables should be set and will set
the initial values when an NPC is initialized.

An example situation of using this is if there is an NPC class preset that defines an AI script for
challenging a player when they come within a certain range. Inside that class preset, the AI script may
need to know at what distance the player will be engaged. This can be defined in the NPC class preset as
a config variable, and therefore any NPC class that relies on this preset will have that config variable
and present it to users when they setup an NPC of that class.

    "config": [
      {
        "variableName": "challenge_distance",
        "displayName": "cobblemon.npc.challenge_distance",
        "description": "cobblemon.npc.challenge_distance.description",
        "type": "number",
        "default": "5"
      }
    ]

The "type" of variable is one of: `string`, `number`, or `boolean`. The type is used to preset a more
appropriate input field for the user when they are setting up the NPC.

### names
A list of strings that represent the names that the NPC can have. The name of the NPC is chosen randomly
when initialized. These can either be simple names or translation keys so provide localization support.

### interaction
An object that defines how the NPC will handle being interacted with by players. The type of interaction
is defined by the "type" property, and different types of interaction have different properties.

#### dialogue
The "dialogue" interaction type is a simple way to have an NPC say something when interacted with. The
"dialogue" property should be the identifier of a datapacked dialogue JSON file. The dialogue that is
created will have `c.npc` as a contextual property in any script that occurs as part of that dialogue,
allowing the dialogue to work with the NPC (including the NPC config values, such as 
`c.npc.config.challenge_distance`).

    "interaction": {
      "type": "dialogue",
      "dialogue": "cobblemon:npc-example"
    }

#### script
The "script" interaction type is a way to have an NPC run a predefined script when interacted with. The
script field is an identifier of a datapacked MoLang script file (in the `scripts` folder).

    "interaction": {
      "type": "script", 
      "script": "cobblemon:npc-example"
    }

#### custom_script
The "custom_script" interaction type is a way to have an NPC run the MoLang script defined specifically
in this file. The script property can be a single string or an array of strings to do multiline MoLang scripts.

    "interaction": {
      "type": "custom_script",
      "script": "c.player.tell('My name is ' + c.npc.name);"
    }

### battleConfiguration
I don't really know whether this is going to stay tbh

### canDespawn
Whether or not regular entity despawning logic applies. By default, NPCs will despawn over time. If the value
of this property is set to false, the NPC will never despawn over time.

### skill
The skill level when this NPC is used in battle. This is a number from 1 to 5, with 5 being most difficult
to beat. This does not control their Pokémon's levels or moves, but it does control how well they use them.

### party
A party provider which generates the party of Pokémon that this NPC will have when in battle. The type of 
party is defined by the "type", and different types of party have different properties. An NPC party is
intended to be initialized when an NPC is spawned and uses a 'seed level'. When spawned using the world
spawning system, it will use a seed level that is based on the player's party's average level. When
spawned using the NPC command, the seed level can be defined in the command and is otherwise set to 1.

A party provider produces either 'static' or 'dynamic' parties. A static party is one that is generated
when the NPC is initialized and will not change. A dynamic party is one that may be configured using the
seed level but can be different any time the NPC is challenged.

A dynamic party prevents the NPC from doing several actions, such as using healing machines, having
persistent health of their party that changes between battles, and keeping their Pokémon out of their
balls. This is because the NPC's party is not stored in the world.

#### simple
The "simple" party type is a way to define a party of Pokémon in a simple list format. Each string in the
list should be a set of Pokémon properties, as used in commands and spawn files.

This party provider type is considered 'static'. Any level that is not mentioned in the Pokémon properties
will be set to the seed level.

    "party": {
      "type": "simple",
      "pokemon": [
        "spiritomb moves=splash,firepunch,flamethrower,swift",
        "porygonz speed_ev=252",
        "togekiss nature=timid",
        "lucario held_item=cobblemon:focus_sash",
        "milotic ability=marvelscale ",
        "garchomp level=100 hp_iv=31 attack_iv=31"
      ]
    }

#### dynamic
To be implemented
