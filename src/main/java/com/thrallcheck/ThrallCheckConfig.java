/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import java.awt.Color;
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
		keyName = "flash",
		name = "Flash on wrong spellbook",
		description = "Flashes the screen while you're holding the Book of the Dead on the wrong spellbook.",
		position = 2
	)
	default boolean flash()
	{
		return true;
	}

	@ConfigItem(
		keyName = "flashColor",
		name = "Flash colour",
		description = "Colour of the screen flash.",
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
		keyName = "compact",
		name = "Compact overlay",
		description = "One line instead of the full breakdown. Turn off if you want the per-rune counts.",
		position = 7
	)
	default boolean compact()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hideWhenReady",
		name = "Hide overlay when ready",
		description = "Only shows the overlay when something is actually wrong.",
		position = 8
	)
	default boolean hideWhenReady()
	{
		return false;
	}

	@ConfigItem(
		keyName = "warnMissingBook",
		name = "Warn when the book is missing",
		description = "Also warns when you're on Arceuus with no Book of the Dead on you. Noisy if you just teleport with Arceuus.",
		position = 9
	)
	default boolean warnMissingBook()
	{
		return false;
	}

	enum TierMode
	{
		AUTO,
		LESSER,
		SUPERIOR,
		GREATER
	}
}
