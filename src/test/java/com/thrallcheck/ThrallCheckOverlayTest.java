/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import java.util.EnumMap;
import java.util.Map;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * The show/hide logic used to live inline inside render(), which needs a live
 * Graphics2D and can't be unit tested. Pulled out as static methods so these two
 * reported bugs can be pinned directly:
 *   - box never hid once every requirement was met
 *   - AUTO mode showed only the spellbook problem and hid a rune shortfall behind it
 */
public class ThrallCheckOverlayTest
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
	public void readyStateHidesWhenHideWhenReadyIsOn()
	{
		// book, arceuus, full runes, full prayer - nothing left to warn about
		ThrallState ready = new ThrallState(true, true, ThrallTier.GREATER,
			runes(Rune.FIRE, 100, Rune.BLOOD, 100, Rune.COSMIC, 100), 99);
		assertTrue(ThrallCheckOverlay.isOk(ready));
		assertTrue("should hide once everything is fine",
			ThrallCheckOverlay.shouldHide(true, true));
	}

	@Test
	public void readyStateStaysUpWhenHideWhenReadyIsOff()
	{
		assertFalse(ThrallCheckOverlay.shouldHide(true, false));
	}

	@Test
	public void notReadyNeverHidesRegardlessOfSetting()
	{
		assertFalse(ThrallCheckOverlay.shouldHide(false, true));
		assertFalse(ThrallCheckOverlay.shouldHide(false, false));
	}

	@Test
	public void autoModeShowsFullChecklistWheneverAnythingIsWrong()
	{
		// wrong spellbook AND short on runes at once - both reasons must show, not
		// just the spellbook one. this is the bug: the old rule only went full once
		// you were ALREADY correct on book+spellbook, hiding the rune shortfall
		// behind the spellbook warning until you fixed that first.
		assertTrue("should show everything, not just the first problem found",
			ThrallCheckOverlay.showFullChecklist(ThrallCheckConfig.OverlayMode.AUTO, false));
	}

	@Test
	public void autoModeStaysCompactWhenEverythingIsFine()
	{
		assertFalse(ThrallCheckOverlay.showFullChecklist(ThrallCheckConfig.OverlayMode.AUTO, true));
	}

	@Test
	public void compactModeNeverGoesFull()
	{
		assertFalse(ThrallCheckOverlay.showFullChecklist(ThrallCheckConfig.OverlayMode.COMPACT, false));
		assertFalse(ThrallCheckOverlay.showFullChecklist(ThrallCheckConfig.OverlayMode.COMPACT, true));
	}

	@Test
	public void fullModeAlwaysGoesFull()
	{
		assertTrue(ThrallCheckOverlay.showFullChecklist(ThrallCheckConfig.OverlayMode.FULL, false));
		assertTrue(ThrallCheckOverlay.showFullChecklist(ThrallCheckConfig.OverlayMode.FULL, true));
	}
}
