Adds new forms or variations to existing dex entries. Variations and
forms are both optional. You need to specify the entryId of the
entry you want to change.

For example, if Arbok didn't originally have patterns, you could give it a pattern variation in the Pokédex using:

    {
      "entryId": "cobblemon:arbok",
      "variations": [
        {
          "displayName": "cobblemon.whatever.name",
          "icon": "cobblemon:textures/gui/pokedex/variation/pattern.png",
          "aspects": [
            "snake-pattern-classic",
            "snake-pattern-legacy",
            "snake-pattern-attack",
            "snake-pattern-dark",
            "snake-pattern-elusive",
            "snake-pattern-heart",
            "snake-pattern-speed",
            "snake-pattern-sound"
          ]
        }
      ]
    }

If you used a species addition to add a new form to Arbok called "Venom", you could add that form to the Pokédex using:

    {
      "entryId": "cobblemon:arbok",
      "forms": [
        {
          "displayForm": "Venom",
          "unlockForms": ["Venom"]
        }
      ]
   }
   
If this form was only different in hidden stats and should not be displayed separately to regular Arbok, you will still need to add something to indicate that catching this special form will unlock it in the Pokédex. This can be done by adding the form to the Pokédex with the same <code>displayForm</code> as the original entry, but with your new form in the <code>unlockForms</code> list:

    {
      "entryId": "cobblemon:arbok",
      "forms": [
        {
          "displayForm": "Normal",
          "unlockForms": ["Venom"]
        }
      ]
    }