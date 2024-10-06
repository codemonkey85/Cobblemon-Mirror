/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

{
    use(battle, pokemon, itemId, data) {
        var healthRatio = parseFloat(data[0]);
        if (pokemon.fainted) {
			if (pokemon.position < pokemon.side.active.length) {
				battle.queue.addChoice({
					choice: 'instaswitch',
					pokemon: pokemon,
					target: pokemon,
				});
			}
            pokemon.fainted = false;
            pokemon.side.pokemonLeft++;
            pokemon.faintQueued = false;
            pokemon.subFainted = false;
            pokemon.hp = 1;
            pokemon.cureStatus(true);
            pokemon.sethp(Math.floor(healthRatio * pokemon.maxhp));
            battle.add('-heal', pokemon, pokemon.getHealth, '[from] bagitem: ' + itemId);
        }
    }
}