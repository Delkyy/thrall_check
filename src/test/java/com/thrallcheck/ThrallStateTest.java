/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import java.util.EnumMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ThrallStateTest
{
	private static Map<Rune, Integer> runes(Object... pairs)
	{
		Map<Rune, Integer> m = new EnumMap<>(Rune.class);
		for (int i = 0; i < pairs.length; i += 2)
		{
			m.put((Rune) pairs[i], (Integer) pairs[i + 1]);
		}
		return m;
	}

	@Test
	public void bookOnStandardIsWrong()
	{
		assertTrue(new ThrallState(true, false, null, null).wrongSpellbook());
	}

	@Test
	public void bookOnArceuusIsFine()
	{
		assertFalse(new ThrallState(true, true, null, null).wrongSpellbook());
	}

	@Test
	public void noBookNeverWarnsAboutSpellbook()
	{
		assertFalse(new ThrallState(false, false, null, null).wrongSpellbook());
	}

	@Test
	public void castsIsTheLimitingRune()
	{
		// greater wants 10 fire, 5 blood, 1 cosmic. blood runs out first
		ThrallState s = new ThrallState(true, true, ThrallTier.GREATER,
			runes(Rune.FIRE, 1000, Rune.BLOOD, 12, Rune.COSMIC, 50));
		assertEquals(2, s.casts());
		assertTrue(s.runesOk());
	}

	@Test
	public void missingRuneMeansZeroCasts()
	{
		ThrallState s = new ThrallState(true, true, ThrallTier.GREATER,
			runes(Rune.FIRE, 1000, Rune.BLOOD, 100));
		assertEquals(0, s.casts());
		assertFalse(s.runesOk());
	}

	@Test
	public void infiniteStaffDoesNotCapCasts()
	{
		ThrallState s = new ThrallState(true, true, ThrallTier.GREATER,
			runes(Rune.FIRE, ThrallState.INFINITE, Rune.BLOOD, 50, Rune.COSMIC, 50));
		assertEquals(10, s.casts());
	}

	@Test
	public void allInfiniteIsInfinite()
	{
		ThrallState s = new ThrallState(true, true, ThrallTier.GREATER,
			runes(Rune.FIRE, ThrallState.INFINITE, Rune.BLOOD, ThrallState.INFINITE, Rune.COSMIC, ThrallState.INFINITE));
		assertEquals(ThrallState.INFINITE, s.casts());
	}

	@Test
	public void noTierMeansNoCasts()
	{
		assertEquals(0, new ThrallState(true, true, null, runes(Rune.FIRE, 100)).casts());
	}

	@Test
	public void tierPicksHighestUnlocked()
	{
		assertNull(ThrallTier.bestFor(37));
		assertEquals(ThrallTier.LESSER, ThrallTier.bestFor(38));
		assertEquals(ThrallTier.LESSER, ThrallTier.bestFor(56));
		assertEquals(ThrallTier.SUPERIOR, ThrallTier.bestFor(57));
		assertEquals(ThrallTier.GREATER, ThrallTier.bestFor(76));
		assertEquals(ThrallTier.GREATER, ThrallTier.bestFor(99));
	}

	@Test
	public void everyTierCostsThreeRunes()
	{
		for (ThrallTier tier : ThrallTier.values())
		{
			assertEquals(tier.getName(), 3, tier.getCost().size());
			assertEquals(tier.getName(), Integer.valueOf(1), tier.getCost().get(Rune.COSMIC));
		}
	}
}
