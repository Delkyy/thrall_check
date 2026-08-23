/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import java.util.EnumMap;
import java.util.Map;
import lombok.Getter;

/**
 * What the plugin worked out this tick. Immutable snapshot, built fresh each time,
 * because caching this and getting it stale is exactly the bug I don't want.
 */
@Getter
class ThrallState
{
	static final int INFINITE = Integer.MAX_VALUE;

	private final boolean hasBook;
	private final boolean onArceuus;
	private final ThrallTier tier;
	private final Map<Rune, Integer> have;

	ThrallState(boolean hasBook, boolean onArceuus, ThrallTier tier, Map<Rune, Integer> have)
	{
		this.hasBook = hasBook;
		this.onArceuus = onArceuus;
		this.tier = tier;
		this.have = have == null ? new EnumMap<>(Rune.class) : have;
	}

	boolean wrongSpellbook()
	{
		return hasBook && !onArceuus;
	}

	/** How many casts the runes on you cover, capped so an infinite staff doesn't overflow. */
	int casts()
	{
		if (tier == null)
		{
			return 0;
		}

		int casts = INFINITE;
		for (Map.Entry<Rune, Integer> need : tier.getCost().entrySet())
		{
			int held = have.getOrDefault(need.getKey(), 0);
			if (held == INFINITE)
			{
				continue;
			}
			casts = Math.min(casts, held / need.getValue());
		}
		return casts;
	}

	boolean runesOk()
	{
		return casts() > 0;
	}
}
