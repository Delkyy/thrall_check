# Thrall Check

RuneLite plugin. Two jobs:

1. You're carrying the Book of the Dead and you're **not** on Arceuus. Screen flashes.
2. Tells you whether you've actually got the runes for a thrall cast, before the fight
   instead of during it.

## Spellbook flash

The book only does anything on Arceuus. Carry it anywhere else and the plugin flashes
the game frame until you switch back, or for however many seconds you set.

It uses its own flash instead of RuneLite's notifier flash, because that one cancels
the second you move the mouse. This is a warning about a mistake you are still making,
so it stays up.

## Rune counter

Panel shows the cost of one resurrection cast and what you're holding:

| Tier | Magic | Prayer | Runes |
|---|---|---|---|
| Lesser | 38 | 2 | 10 air, 5 mind, 1 cosmic |
| Superior | 57 | 4 | 10 earth, 5 death, 1 cosmic |
| Greater | 76 | 6 | 10 fire, 5 blood, 1 cosmic |

Counts inventory, rune pouch, combination runes, sunfire runes, elemental and
combination staves, and a charged tome of fire or earth. Tier defaults to the best your
Magic level allows.

Prayer counts too. A cast spends prayer points, so full runes and no prayer is still
zero casts, and the cast count is whichever of the two runs out first.

Overlay mode picks how much you see. Auto is one line normally, and swaps to the rune
checklist the moment you're on Arceuus with the book. Compact and Full pin it either way.

Overlay text size is configurable. 0 uses RuneLite's own overlay font.

## Build

Gradle needs JDK 22 or older here. The default JDK on this box is 25 and both Gradle and
Lombok fall over on it with errors that look like code faults.

```bash
JAVA_HOME="/c/Program Files/Java/jdk-17" ./gradlew build
JAVA_HOME="/c/Program Files/Java/jdk-17" ./gradlew run
```

## Verified

In-game, on a live client:

- **Rune counting works.** 3592 blood read correctly, picked blood as the limiting rune
  over fire and cosmic, and auto-tier resolved to Greater off 79 Magic.
- **Full-screen flash works.** Covers the whole frame now.
- **Panel sizes itself.** No overlap at any label length.

Compiles clean, 18/18 unit tests.

Not yet seen in-game: the rune checklist appearing on its own when the book and Arceuus
are both up, and the prayer tracking.

## Two traps, already paid for

**The flash must not fill from 0,0.** `OverlayRenderer.safeRender` translates the
graphics origin to the overlay's position before calling render, so a fill at the origin
lands wherever the panel sits and you get a box in the corner. Undo the translate first.

**Never hardcode a panel width.** `LineComponent` doesn't clip when the text is wider
than the panel, it wraps and squeezes both sides into each other. Measure the text with
FontMetrics and size to fit. An 84px guess drew "Thralls" straight through "spellbook".
