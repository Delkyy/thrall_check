/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class ThrallCheckOverlay extends OverlayPanel
{
	private static final Color GOOD = new Color(0, 200, 83);
	private static final Color BAD = new Color(255, 80, 80);

	// gap between label and value. LineComponent squeezes and wraps both sides when the
	// panel is narrower than left+right, which is what turned "Thralls spellbook" into
	// overlapping mush. always measure, never hardcode a width
	private static final int GAP = 14;

	private final ThrallCheckPlugin plugin;
	private final ThrallCheckConfig config;

	@Inject
	ThrallCheckOverlay(ThrallCheckPlugin plugin, ThrallCheckConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	/** One row of the panel, held as data so the width can be measured before drawing. */
	private static class Row
	{
		final String left;
		final String right;
		final Color color;

		Row(String left, String right, Color color)
		{
			this.left = left;
			this.right = right;
			this.color = color;
		}
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showOverlay())
		{
			return null;
		}

		ThrallState state = plugin.getState();

		// nothing worth saying if you're nowhere near casting a thrall
		if (!state.isHasBook() && !state.isOnArceuus())
		{
			return null;
		}

		// book in hand and on arceuus. this is the moment the rune checklist is worth
		// having, so it wins over compact and over hide-when-ready
		boolean armed = state.isHasBook() && state.isOnArceuus() && config.checklist();

		boolean ok = !state.wrongSpellbook() && state.isHasBook() && state.runesOk();
		if (ok && config.hideWhenReady() && !armed)
		{
			return null;
		}

		boolean full = armed || !config.compact();
		List<Row> rows = full ? fullRows(state, armed) : compactRows(state);

		FontMetrics fm = graphics.getFontMetrics();
		int width = 0;
		for (Row row : rows)
		{
			width = Math.max(width, fm.stringWidth(row.left) + GAP + fm.stringWidth(row.right));
		}
		panelComponent.setPreferredSize(new Dimension(width, 0));

		if (full)
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("Thralls")
				.color(ok ? GOOD : BAD)
				.build());
		}

		for (Row row : rows)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left(row.left)
				.right(row.right)
				.rightColor(row.color)
				.build());
		}

		return super.render(graphics);
	}

	/** One line: what's wrong, or how many casts you're carrying. */
	private List<Row> compactRows(ThrallState state)
	{
		List<Row> rows = new ArrayList<>();

		if (state.wrongSpellbook())
		{
			rows.add(new Row("Thralls", "not Arceuus", BAD));
		}
		else if (!state.isHasBook())
		{
			rows.add(new Row("Thralls", "no book", BAD));
		}
		else if (state.getTier() == null)
		{
			rows.add(new Row("Thralls", "38 mage", BAD));
		}
		else
		{
			int casts = state.casts();
			rows.add(new Row("Thralls",
				casts == ThrallState.INFINITE ? "\u221e" : casts + " casts",
				casts > 0 ? GOOD : BAD));
		}

		return rows;
	}

	private List<Row> fullRows(ThrallState state, boolean armed)
	{
		List<Row> rows = new ArrayList<>();

		// when you're armed both of these are green by definition, so they're just noise.
		// show the runes and nothing else
		if (!armed)
		{
			rows.add(new Row("Book", state.isHasBook() ? "yes" : "missing", state.isHasBook() ? GOOD : BAD));
			rows.add(new Row("Spellbook", state.isOnArceuus() ? "Arceuus" : "wrong", state.isOnArceuus() ? GOOD : BAD));
		}

		ThrallTier tier = state.getTier();
		if (tier == null)
		{
			rows.add(new Row("Runes", "38 mage", BAD));
			return rows;
		}

		for (Map.Entry<Rune, Integer> need : tier.getCost().entrySet())
		{
			int held = state.getHave().getOrDefault(need.getKey(), 0);
			boolean enough = held >= need.getValue();
			rows.add(new Row(
				need.getKey().getName(),
				held == ThrallState.INFINITE ? "\u221e" : shorten(held) + "/" + need.getValue(),
				enough ? GOOD : BAD));
		}

		int casts = state.casts();
		rows.add(new Row(tier.getName() + " casts",
			casts == ThrallState.INFINITE ? "\u221e" : String.valueOf(casts),
			casts > 0 ? GOOD : BAD));

		return rows;
	}

	/** 37265 reads as 37.3k. nobody needs the exact count of a rune they have thousands of. */
	private static String shorten(int n)
	{
		if (n >= 100_000)
		{
			return n / 1000 + "k";
		}
		if (n >= 10_000)
		{
			return String.format("%.1fk", n / 1000f);
		}
		return String.valueOf(n);
	}
}
