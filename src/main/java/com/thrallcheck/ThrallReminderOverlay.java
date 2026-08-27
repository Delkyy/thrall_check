/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import java.awt.Color;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

/**
 * "Summon a thrall", on its own movable overlay.
 *
 * This used to be drawn straight onto the canvas by ThrallFlashOverlay, which sits at
 * OverlayPosition.DYNAMIC so the full-screen flash can cover the whole viewport.
 * DYNAMIC overlays are explicitly excluded from RuneLite's drag-to-move handling
 * (OverlayRenderer only offers it for other positions), so the reminder was stuck at a
 * fixed spot with no way to relocate it. A real OverlayPanel at TOP_CENTER gets the
 * normal move/reset/hide menu for free, the same as every other RuneLite overlay.
 */
class ThrallReminderOverlay extends OverlayPanel
{
	private static final Color FG = new Color(0x9d, 0x6b, 0xd9);

	private final ThrallCheckPlugin plugin;

	@Inject
	ThrallReminderOverlay(ThrallCheckPlugin plugin)
	{
		this.plugin = plugin;
		setPosition(OverlayPosition.TOP_CENTER);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public java.awt.Dimension render(java.awt.Graphics2D graphics)
	{
		if (!plugin.needsThrall())
		{
			return null;
		}

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Summon a thrall")
			.leftColor(FG)
			.build());

		return super.render(graphics);
	}
}
