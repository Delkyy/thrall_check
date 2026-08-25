/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Notification;
import net.runelite.client.config.Range;

@ConfigGroup(ThrallCheckConfig.GROUP)
public interface ThrallCheckConfig extends Config
{
	String GROUP = "thrallcheck";

	@ConfigItem(
		keyName = "tier",
		name = "Thrall tier",
		description = "Which resurrection spell to check runes for. Auto picks the best your Magic level allows.",
		position = 1
	)
	default TierMode tier()
	{
		return TierMode.AUTO;
	}

	@ConfigItem(
		keyName = "alertStyle",
		name = "Alert style",
		description = "How to warn you about the spellbook. Banner draws a bar across the top instead of flashing the whole screen.",
		position = 2
	)
	default AlertStyle alertStyle()
	{
		return AlertStyle.FLASH;
	}

	@Alpha
	@ConfigItem(
		keyName = "flashColor",
		name = "Flash colour",
		description = "Colour of the screen flash. The A slider is the opacity - turn it down if the flash blocks your view.",
		position = 3
	)
	default Color flashColor()
	{
		return new Color(255, 0, 0, 70);
	}

	@ConfigItem(
		keyName = "flashSeconds",
		name = "Flash for (seconds)",
		description = "How long to keep flashing. 0 flashes until you fix the spellbook.",
		position = 4
	)
	@Range(min = 0, max = 30)
	default int flashSeconds()
	{
		return 3;
	}

	@ConfigItem(
		keyName = "notification",
		name = "Notification",
		description = "Fires the usual RuneLite notification when the spellbook is wrong.",
		position = 5
	)
	default Notification notification()
	{
		return Notification.OFF;
	}

	@ConfigItem(
		keyName = "showOverlay",
		name = "Show rune overlay",
		description = "Shows the spellbook state and your thrall rune counts on screen.",
		position = 6
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "overlayMode",
		name = "Overlay mode",
		description = "Auto shows one line normally and the full rune checklist once you're on Arceuus with the book.",
		position = 7
	)
	default OverlayMode overlayMode()
	{
		return OverlayMode.AUTO;
	}

	@ConfigItem(
		keyName = "hideWhenReady",
		name = "Hide overlay when ready",
		description = "Only shows the overlay when something is actually wrong. Ignored while the checklist is up.",
		position = 8
	)
	default boolean hideWhenReady()
	{
		return true;
	}

	@ConfigItem(
		keyName = "remindThrall",
		name = "Remind me to summon",
		description = "Shows a reminder while you're in combat with no thrall out.",
		position = 9
	)
	default boolean remindThrall()
	{
		return true;
	}

	@ConfigItem(
		keyName = "remindDelay",
		name = "Remind after (ticks)",
		description = "How long in combat before the reminder appears. Stops it firing on a single stray hit.",
		position = 10
	)
	@Range(min = 0, max = 50)
	default int remindDelay()
	{
		return 5;
	}

	@ConfigItem(
		keyName = "fontSize",
		name = "Overlay text size",
		description = "Size of the overlay text. 0 uses RuneLite's own overlay font.",
		position = 10
	)
	@Range(min = 0, max = 32)
	default int fontSize()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "warnMissingBook",
		name = "Warn when the book is missing",
		description = "Also warns when you're on Arceuus with no Book of the Dead on you. Noisy if you just teleport with Arceuus.",
		position = 11
	)
	default boolean warnMissingBook()
	{
		return false;
	}

	enum AlertStyle
	{
		/** The whole screen pulses. Loud, impossible to miss. */
		FLASH,
		/** A bar across the top of the viewport. Says the same thing without blinding you. */
		BANNER,
		/** Overlay and notification only. */
		OFF
	}

	enum TierMode
	{
		AUTO,
		LESSER,
		SUPERIOR,
		GREATER
	}

	enum OverlayMode
	{
		/** One line, until you're actually holding the book on arceuus. Then the runes. */
		AUTO,
		/** Always one line, even when armed. */
		COMPACT,
		/** Always the full breakdown. */
		FULL
	}
}
