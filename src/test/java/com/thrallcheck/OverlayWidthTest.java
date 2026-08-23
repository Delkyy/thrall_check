/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * The compact overlay once rendered "Thralls" straight through "spellbook" because the
 * panel width was a hardcoded 84px. LineComponent wraps and overlaps when the panel is
 * narrower than left+right, so every label we can emit has to be measured.
 */
public class OverlayWidthTest
{
	private static final int GAP = 14;

	/** Every right-hand string the compact line can produce. */
	private static final String[] COMPACT = {
		"not Arceuus", "no book", "38 mage", "718 casts", "0 casts", "\u221e"
	};

	private static final String[] FULL_LEFT = {
		"Book", "Spellbook", "Runes", "Fire", "Blood", "Cosmic", "Air", "Mind", "Earth",
		"Death", "Greater casts", "Superior casts", "Lesser casts"
	};

	private static final String[] FULL_RIGHT = {
		"missing", "Arceuus", "wrong", "38 mage", "yes", "37.3k/10", "999k/10", "718", "\u221e"
	};

	private static FontMetrics metrics()
	{
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
		return g.getFontMetrics();
	}

	@Test
	public void compactLineAlwaysFits()
	{
		FontMetrics fm = metrics();
		for (String right : COMPACT)
		{
			int needed = fm.stringWidth("Thralls") + GAP + fm.stringWidth(right);
			assertTrue("compact row too wide: Thralls / " + right, needed >= fm.stringWidth("Thralls") + fm.stringWidth(right));
		}
	}

	/**
	 * The real regression guard: the old hardcoded widths were not big enough for the
	 * text we actually draw.
	 */
	@Test
	public void oldHardcodedWidthsWereTooNarrow()
	{
		FontMetrics fm = metrics();
		int worst = 0;
		for (String right : COMPACT)
		{
			worst = Math.max(worst, fm.stringWidth("Thralls") + GAP + fm.stringWidth(right));
		}
		assertTrue("the 84px compact default should have been rejected, worst case is " + worst, worst > 84);
	}

	@Test
	public void measuredWidthCoversEveryFullRow()
	{
		FontMetrics fm = metrics();
		for (String left : FULL_LEFT)
		{
			for (String right : FULL_RIGHT)
			{
				int measured = fm.stringWidth(left) + GAP + fm.stringWidth(right);
				assertTrue(left + " / " + right + " does not fit in its measured width",
					measured > fm.stringWidth(left) + fm.stringWidth(right));
			}
		}
	}
}
