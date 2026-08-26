# Thrall Check

You get to the boss, click the thrall spell, and nothing happens — you were on the
wrong spellbook the whole time. This plugin catches that before the fight starts.

- **Spellbook alert.** The Book of the Dead only works on Arceuus. Warns you if it
  isn't, either as a full-screen flash or a banner across the top — your choice.
- **Summon reminder.** Fighting with no thrall out gets you a reminder to summon one.
- **Rune counter.** Shows the cost of one resurrection cast (Lesser/Superior/Greater)
  against what you're actually holding — inventory, rune pouch, elemental staves, and a
  charged tome of fire or earth. Counts prayer too, since a cast spends both.

## Build

```bash
JAVA_HOME="/c/Program Files/Java/jdk-17" ./gradlew build
JAVA_HOME="/c/Program Files/Java/jdk-17" ./gradlew run
```

Developer notes (verification log, rendering gotchas) are in `DEVELOPMENT.md`.
