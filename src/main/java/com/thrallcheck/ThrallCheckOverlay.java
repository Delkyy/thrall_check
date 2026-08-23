/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
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

	// the full panel needs room for "37265/10", the compact one never does
	private static final int WIDE = 129;
	private static final int NARROW = 84;

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

		boolean ok = !state.wrongSpellbook() && state.isHasBook() && state.runesOk();
		if (ok && config.hideWhenReady())
		{
			return null;
		}

		panelComponent.setPreferredSize(new Dimension(config.compact() ? NARROW : WIDE, 0));

		if (config.compact())
		{
			return compact(graphics, state, ok);
		}
		return full(graphics, state, ok);
	}

	/** One line. Says what's wrong, or how many casts you're carrying. */
	private Dimension compact(Graphics2D graphics, ThrallState state, boolean ok)
	{
		String text;
		Color color;

		if (state.wrongSpellbook())
		{
			text = "spellbook";
			color = BAD;
		}
		else if (!state.isHasBook())
		{
			text = "no book";
			color = BAD;
		}
		else if (state.getTier() == null)
		{
			text = "38 mage";
			color = BAD;
		}
		else
		{
			int casts = state.casts();
			text = casts == ThrallState.INFINITE ? "\u221e" : String.valueOf(casts);
			color = casts > 0 ? GOOD : BAD;
		}

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Thralls")
			.right(text)
			.rightColor(color)
			.build());

		return super.render(graphics);
	}

	private Dimension full(Graphics2D graphics, ThrallState state, boolean ok)
	{
		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Thralls")
			.color(ok ? GOOD : BAD)
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Book")
			.right(state.isHasBook() ? "yes" : "missing")
			.rightColor(state.isHasBook() ? GOOD : BAD)
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Spellbook")
			.right(state.isOnArceuus() ? "Arceuus" : "wrong")
			.rightColor(state.isOnArceuus() ? GOOD : BAD)
			.build());

		ThrallTier tier = state.getTier();
		if (tier == null)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Runes")
				.right("need 38 mage")
				.rightColor(BAD)
				.build());
			return super.render(graphics);
		}

		for (Map.Entry<Rune, Integer> need : tier.getCost().entrySet())
		{
			int held = state.getHave().getOrDefault(need.getKey(), 0);
			boolean enough = held >= need.getValue();
			panelComponent.getChildren().add(LineComponent.builder()
				.left(need.getKey().getName())
				.right(held == ThrallState.INFINITE ? "\u221e" : shorten(held) + "/" + need.getValue())
				.rightColor(enough ? GOOD : BAD)
				.build());
		}

		int casts = state.casts();
		panelComponent.getChildren().add(LineComponent.builder()
			.left(tier.getName() + " casts")
			.right(casts == ThrallState.INFINITE ? "\u221e" : String.valueOf(casts))
			.rightColor(casts > 0 ? GOOD : BAD)
			.build());

		return super.render(graphics);
	}

	/** 37265 reads as 37k. nobody needs the exact count of a rune they have thousands of. */
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
