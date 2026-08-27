/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * The screen-wide alerts: the flash, and the spellbook banner.
 *
 * Both live here because they're drawn against the whole viewport at OverlayPosition
 * .DYNAMIC, which is what a full-screen flash needs. The summon reminder used to be
 * here too but DYNAMIC overlays can't be dragged (see ThrallReminderOverlay), so it
 * moved to its own movable panel.
 *
 * OverlayRenderer translates the graphics origin to wherever the overlay sits before it
 * calls render, so filling from 0,0 gives you a rectangle in the corner and not a full
 * screen. Took me way too long to spot that. Undo the translate first, fill, put it back.
 */
class ThrallFlashOverlay extends Overlay
{
	private static final Color BANNER_BG = new Color(0, 0, 0, 170);

	private final Client client;
	private final ThrallCheckPlugin plugin;
	private final ThrallCheckConfig config;

	@Inject
	ThrallFlashOverlay(Client client, ThrallCheckPlugin plugin, ThrallCheckConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(PRIORITY_HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Rectangle b = getBounds();
		Color prev = graphics.getColor();

		// everything here is drawn in viewport space, so undo the overlay's offset once
		graphics.translate(-b.x, -b.y);

		flash(graphics);

		if (plugin.shouldBanner())
		{
			banner(graphics, plugin.bannerText(), config.flashColor(), BANNER_BG);
		}

		graphics.translate(b.x, b.y);
		graphics.setColor(prev);
		return null;
	}

	private void flash(Graphics2D graphics)
	{
		// 20 cycles on, 20 off. same cadence the client's own notifier flash uses
		if (!plugin.shouldFlash() || client.getGameCycle() % 40 >= 20)
		{
			return;
		}

		graphics.setColor(config.flashColor());
		graphics.fill(new Rectangle(client.getCanvasWidth(), client.getCanvasHeight()));
	}

	/**
	 * A bar across the top of the viewport.
	 *
	 * Centred on the canvas rather than the overlay's own bounds, and sized from real
	 * FontMetrics - a hardcoded width is what broke the panel the first time.
	 */
	private void banner(Graphics2D graphics, String text, Color fg, Color bg)
	{
		FontMetrics fm = graphics.getFontMetrics();
		int pad = 8;
		int w = fm.stringWidth(text) + pad * 2;
		int h = fm.getHeight() + pad;
		int x = (client.getCanvasWidth() - w) / 2;
		int top = 6;

		graphics.setColor(bg);
		graphics.fillRect(x, top, w, h);
		graphics.setColor(fg);
		graphics.drawRect(x, top, w, h);
		graphics.drawString(text, x + pad, top + fm.getAscent() + pad / 2);
	}
}
