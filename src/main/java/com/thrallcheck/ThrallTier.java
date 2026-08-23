/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import java.util.EnumMap;
import java.util.Map;
import lombok.Getter;

/**
 * The three resurrection tiers. Costs are off the wiki (Arceuus spellbook, resurrection
 * spells) and are per cast. Prayer cost is not checked here, only runes.
 */
@Getter
enum ThrallTier
{
	LESSER("Lesser", 38, cost(10, Rune.AIR, 5, Rune.MIND, 1, Rune.COSMIC)),
	SUPERIOR("Superior", 57, cost(10, Rune.EARTH, 5, Rune.DEATH, 1, Rune.COSMIC)),
	GREATER("Greater", 76, cost(10, Rune.FIRE, 5, Rune.BLOOD, 1, Rune.COSMIC));

	private final String name;
	private final int magicLevel;
	private final Map<Rune, Integer> cost;

	ThrallTier(String name, int magicLevel, Map<Rune, Integer> cost)
	{
		this.name = name;
		this.magicLevel = magicLevel;
		this.cost = cost;
	}

	private static Map<Rune, Integer> cost(int a, Rune ra, int b, Rune rb, int c, Rune rc)
	{
		Map<Rune, Integer> m = new EnumMap<>(Rune.class);
		m.put(ra, a);
		m.put(rb, b);
		m.put(rc, c);
		return m;
	}

	/** Best tier the level allows, or null below 38. */
	static ThrallTier bestFor(int magicLevel)
	{
		if (magicLevel >= GREATER.magicLevel)
		{
			return GREATER;
		}
		if (magicLevel >= SUPERIOR.magicLevel)
		{
			return SUPERIOR;
		}
		return magicLevel >= LESSER.magicLevel ? LESSER : null;
	}
}
