# Development notes

Working notes for anyone touching this repo. Not needed to use the plugin.

## Alert style

**Flash** pulses the whole screen. The colour has an opacity slider — turn the A
channel down if it blocks your view. **Banner** draws a bar across the top instead,
same warning without the strobing. **Off** leaves it to the panel and the notification.

Uses its own flash rather than RuneLite's notifier flash, because that one cancels as
soon as you move the mouse. This is a warning about a mistake you're still making, so
it stays up.

## Summon reminder

There's a tick delay (default 5) so a single stray hit doesn't trigger it.

There is no varbit for "do I have a thrall", so it watches the nine Arceuus thrall NPCs
and checks the one it finds is yours — a follower interacting with you — rather than
someone else's standing nearby at a bank or a boss lobby.

## Rune counter

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
checklist the moment you're on Arceuus with the book. Compact and Full pin it either
way. Overlay text size is configurable; 0 uses RuneLite's own overlay font.

## Two rendering traps, already paid for

**The flash must not fill from 0,0.** `OverlayRenderer.safeRender` translates the
graphics origin to the overlay's position before calling render, so a fill at the
origin lands wherever the panel sits and you get a box in the corner. Undo the
translate first.

**Never hardcode a panel width.** `LineComponent` doesn't clip when the text is wider
than the panel, it wraps and squeezes both sides into each other. Measure the text with
FontMetrics and size to fit. An 84px guess drew "Thralls" straight through "spellbook".

## Verified

In-game, on a live client:

- Rune counting: 3592 blood read correctly, picked blood as the limiting rune over
  fire and cosmic, auto-tier resolved to Greater off 79 Magic.
- Prayer: 71 prayer against 37.3k fire / 3587 blood / 7873 cosmic reported 11 casts,
  correctly limited by prayer rather than runes.
- Auto overlay mode: one line normally, full checklist once armed.
- Full-screen flash covers the whole frame.
- Panel sizes itself, no overlap at any label length.
- Banner, opacity slider and summon reminder all checked live.
- Config migration runs once and is logged (`thrallcheck.flash` unset,
  `thrallcheck.migrated` written).

Compiles clean, 20/20 unit tests.
