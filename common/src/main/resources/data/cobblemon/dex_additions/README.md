Adds entries to existing dexes.

When adding to a simple dex, the entries are a list of Pokédex entries.

For example, you could add Arbok's entry to the Johto dex using:

    {
      "dexId": "cobblemon:johto",
      "entries": [
        "cobblemon:arbok"
      ]
    }


When adding to an aggregate dex, the entries are a list of Pokédexes.

For example, you could add a custom Pokédex called yourmod:customdex to the National dex using:

    {
      "dexId": "cobblemon:national",
      "entries": [
        "yourmod:customdex"
      ]
    }
