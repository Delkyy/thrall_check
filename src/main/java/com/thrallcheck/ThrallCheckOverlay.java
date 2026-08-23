/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class ThrallCheckOverlay extends OverlayPanel
{
	private static final Color GOOD = new Color(0, 200, 83);
	private static final Color BAD = new Color(255, 80, 80);

	private final Client client;
	private final ThrallCheckPlugin plugin;
	private final ThrallCheckConfig config;

	@Inject
	ThrallCheckOverlay(Client client, ThrallCheckPlugin plugin, ThrallCheckConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		// above widgets or the flash paints under the chatbox and you barely see it
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(PRIORITY_HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		flash(graphics);

		if (!config.showOverlay())
		{
			return null;
		}

		ThrallState state = plugin.getState();

		// nothing to say if you're not anywhere near casting a thrall
		if (!state.isHasBook() && !state.isOnArceuus())
		{
			return null;
		}

		boolean ok = !state.wrongSpellbook() && state.isHasBook() && state.runesOk();
		if (ok && config.hideWhenReady())
		{
			return null;
		}

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
				.right(held == ThrallState.INFINITE ? "\u221e" : held + "/" + need.getValue())
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

	/**
	 * Own flash rather than the Notifier's, because that one cancels itself the moment
	 * you touch the mouse. This is a warning about a mistake you're still making.
	 */
	private void flash(Graphics2D graphics)
	{
		if (!plugin.shouldFlash() || client.getGameCycle() % 40 >= 20)
		{
			return;
		}

		Color prev = graphics.getColor();
		graphics.setColor(config.flashColor());
		graphics.fill(new Rectangle(client.getCanvas().getSize()));
		graphics.setColor(prev);
	}
}
