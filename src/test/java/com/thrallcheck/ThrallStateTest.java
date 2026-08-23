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
	private static final int FULL_PRAYER = 99;

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
		assertTrue(new ThrallState(true, false, null, null, FULL_PRAYER).wrongSpellbook());
	}

	@Test
	public void bookOnArceuusIsFine()
	{
		assertFalse(new ThrallState(true, true, null, null, FULL_PRAYER).wrongSpellbook());
	}

	@Test
	public void noBookNeverWarnsAboutSpellbook()
	{
		assertFalse(new ThrallState(false, false, null, null, FULL_PRAYER).wrongSpellbook());
	}

	@Test
	public void castsIsTheLimitingRune()
	{
		// greater wants 10 fire, 5 blood, 1 cosmic. blood runs out first
		ThrallState s = new ThrallState(true, true, ThrallTier.GREATER,
			runes(Rune.FIRE, 1000, Rune.BLOOD, 12, Rune.COSMIC, 50), FULL_PRAYER);
		assertEquals(2, s.casts());
		assertTrue(s.runesOk());
	}

	@Test
	public void missingRuneMeansZeroCasts()
	{
		ThrallState s = new ThrallState(true, true, ThrallTier.GREATER,
			runes(Rune.FIRE, 1000, Rune.BLOOD, 100), FULL_PRAYER);
		assertEquals(0, s.casts());
		assertFalse(s.runesOk());
	}

	@Test
	public void infiniteStaffDoesNotCapCasts()
	{
		ThrallState s = new ThrallState(true, true, ThrallTier.GREATER,
			runes(Rune.FIRE, ThrallState.INFINITE, Rune.BLOOD, 50, Rune.COSMIC, 50), FULL_PRAYER);
		assertEquals(10, s.casts());
	}

	@Test
	public void allInfiniteRunesStillCappedByPrayer()
	{
		// 99 prayer / 6 per greater cast = 16, even with unlimited runes
		ThrallState s = new ThrallState(true, true, ThrallTier.GREATER,
			runes(Rune.FIRE, ThrallState.INFINITE, Rune.BLOOD, ThrallState.INFINITE, Rune.COSMIC, ThrallState.INFINITE),
			FULL_PRAYER);
		assertEquals(16, s.casts());
		assertEquals(ThrallState.INFINITE, s.runeCasts());
	}

	@Test
	public void noTierMeansNoCasts()
	{
		assertEquals(0, new ThrallState(true, true, null, runes(Rune.FIRE, 100), FULL_PRAYER).casts());
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

	// prayer. the whole point: plenty of runes and no prayer means you cast nothing

	@Test
	public void noPrayerMeansNoCastsDespiteFullBank()
	{
		ThrallState s = new ThrallState(true, true, ThrallTier.GREATER,
			runes(Rune.FIRE, 10_000, Rune.BLOOD, 10_000, Rune.COSMIC, 10_000), 0);
		assertEquals(0, s.casts());
		assertFalse(s.prayerOk());
		// runes were never the problem
		assertTrue(s.runesOk());
		assertEquals(1000, s.runeCasts());
	}

	@Test
	public void prayerCapsBelowRunes()
	{
		// 13 prayer / 6 = 2 casts, even though the runes cover 1000
		ThrallState s = new ThrallState(true, true, ThrallTier.GREATER,
			runes(Rune.FIRE, 10_000, Rune.BLOOD, 10_000, Rune.COSMIC, 10_000), 13);
		assertEquals(2, s.casts());
		assertTrue(s.prayerOk());
	}

	@Test
	public void exactlyEnoughPrayerForOneCast()
	{
		ThrallState s = new ThrallState(true, true, ThrallTier.GREATER,
			runes(Rune.FIRE, 100, Rune.BLOOD, 100, Rune.COSMIC, 100), 6);
		assertEquals(1, s.casts());
		assertTrue(s.prayerOk());
	}

	@Test
	public void onePrayerShortIsNotOk()
	{
		ThrallState s = new ThrallState(true, true, ThrallTier.GREATER,
			runes(Rune.FIRE, 100, Rune.BLOOD, 100, Rune.COSMIC, 100), 5);
		assertEquals(0, s.casts());
		assertFalse(s.prayerOk());
	}

	@Test
	public void cheaperTiersNeedLessPrayer()
	{
		// 5 prayer casts a lesser (2) but not a greater (6)
		assertEquals(2, ThrallTier.LESSER.getPrayerCost());
		assertEquals(4, ThrallTier.SUPERIOR.getPrayerCost());
		assertEquals(6, ThrallTier.GREATER.getPrayerCost());

		ThrallState lesser = new ThrallState(true, true, ThrallTier.LESSER,
			runes(Rune.AIR, 100, Rune.MIND, 100, Rune.COSMIC, 100), 5);
		assertTrue(lesser.prayerOk());
		assertEquals(2, lesser.casts());
	}
}
