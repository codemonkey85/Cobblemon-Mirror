### Additions
- Pokémon are now animated when seen in any GUI that isn't the party GUI.
- Quirk animations can now occur for Pokémon that are shoulder mounted.

### Changes
- Improved the performance of display cases that contain Pokémon photos.

### Fixes
- Fixed some Pokémon photos in display cases flashing if the Pokémon has gender differences.
- Fixed Bidoof and Jigglypuff sleep animations stopping after some time, causing them to be 'asleep' while T-posing menacingly.
- Fixed entities not changing poses unless you're looking at them.
- Fixed cries not playing on send-out if the Pokémon is off-screen.

### Developer
- Rebuilt large swaths of the model animation code to simplify it.
- Renamed a bunch of things from %Poseable% to %Posable% because spelling.
- Renamed StatelessAnimation to PoseAnimation.
- Renamed StatefulAnimation to ActiveAnimation.
- Documented the animation system.

### Data Pack & Resource Pack Creators
- Added MoLang compatibility in the isVisible property for transformed parts.
- Added q.has_aspect('some_aspect') function to animations, posers, and entity particle effects.
- Added support for conditional pose animations.