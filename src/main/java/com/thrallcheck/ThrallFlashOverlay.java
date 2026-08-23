/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * The screen flash, on its own overlay so it can't get tangled up with the panel.
 *
 * OverlayRenderer translates the graphics origin to wherever the overlay sits before it
 * calls render, so filling from 0,0 gives you a rectangle in the corner and not a full
 * screen. Took me way too long to spot that. Undo the translate first, fill, put it back.
 */
class ThrallFlashOverlay extends Overlay
{
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
		// 20 cycles on, 20 off. same cadence the client's own notifier flash uses
		if (!plugin.shouldFlash() || client.getGameCycle() % 40 >= 20)
		{
			return null;
		}

		Rectangle b = getBounds();
		Color prev = graphics.getColor();

		graphics.translate(-b.x, -b.y);
		graphics.setColor(config.flashColor());
		graphics.fill(new Rectangle(client.getCanvasWidth(), client.getCanvasHeight()));
		graphics.translate(b.x, b.y);

		graphics.setColor(prev);
		return null;
	}
}
