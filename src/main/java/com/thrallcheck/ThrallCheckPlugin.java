/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import com.google.inject.Provides;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Thrall Check",
	description = "Warns when you're carrying the Book of the Dead on the wrong spellbook, and tracks thrall runes",
	tags = {"thrall", "arceuus", "spellbook", "book of the dead", "resurrect", "runes", "magic"}
)
public class ThrallCheckPlugin extends Plugin
{
	// spellbook varbit, 0 standard / 1 ancient / 2 lunar / 3 arceuus
	private static final int ARCEUUS = 3;

	private static final int POUCH_SLOTS = 6;
	private static final int[] POUCH_TYPE = {
		VarbitID.RUNE_POUCH_TYPE_1, VarbitID.RUNE_POUCH_TYPE_2, VarbitID.RUNE_POUCH_TYPE_3,
		VarbitID.RUNE_POUCH_TYPE_4, VarbitID.RUNE_POUCH_TYPE_5, VarbitID.RUNE_POUCH_TYPE_6
	};
	private static final int[] POUCH_QTY = {
		VarbitID.RUNE_POUCH_QUANTITY_1, VarbitID.RUNE_POUCH_QUANTITY_2, VarbitID.RUNE_POUCH_QUANTITY_3,
		VarbitID.RUNE_POUCH_QUANTITY_4, VarbitID.RUNE_POUCH_QUANTITY_5, VarbitID.RUNE_POUCH_QUANTITY_6
	};

	@Inject
	private Client client;

	@Inject
	private ThrallCheckConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ThrallCheckOverlay overlay;

	@Inject
	private Notifier notifier;

	@Getter
	private ThrallState state = new ThrallState(false, false, null, null);

	private boolean warned;
	private Instant wrongSince;

	@Provides
	ThrallCheckConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ThrallCheckConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		state = new ThrallState(false, false, null, null);
		warned = false;
		wrongSince = null;
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		state = build();

		if (!needsWarning())
		{
			warned = false;
			wrongSince = null;
			return;
		}

		if (wrongSince == null)
		{
			wrongSince = Instant.now();
		}

		if (!warned)
		{
			warned = true;
			notifier.notify(config.notification(), state.wrongSpellbook()
				? "Book of the Dead on the wrong spellbook"
				: "No Book of the Dead - thralls won't cast");
		}
	}

	private boolean needsWarning()
	{
		if (state.wrongSpellbook())
		{
			return true;
		}
		return config.warnMissingBook() && state.isOnArceuus() && !state.isHasBook();
	}

	private ThrallState build()
	{
		ItemContainer inv = client.getItemContainer(InventoryID.INV);
		ItemContainer worn = client.getItemContainer(InventoryID.WORN);

		boolean book = (inv != null && inv.contains(ItemID.BOOK_OF_THE_DEAD))
			|| equippedId(worn, EquipmentInventorySlot.SHIELD) == ItemID.BOOK_OF_THE_DEAD
			|| equippedId(worn, EquipmentInventorySlot.WEAPON) == ItemID.BOOK_OF_THE_DEAD;

		boolean arceuus = client.getVarbitValue(VarbitID.SPELLBOOK) == ARCEUUS;

		return new ThrallState(book, arceuus, tier(), countRunes(inv, worn));
	}

	private ThrallTier tier()
	{
		switch (config.tier())
		{
			case LESSER:
				return ThrallTier.LESSER;
			case SUPERIOR:
				return ThrallTier.SUPERIOR;
			case GREATER:
				return ThrallTier.GREATER;
			default:
				return ThrallTier.bestFor(client.getRealSkillLevel(Skill.MAGIC));
		}
	}

	private Map<Rune, Integer> countRunes(ItemContainer inv, ItemContainer worn)
	{
		Map<Rune, Integer> have = new EnumMap<>(Rune.class);

		if (inv != null)
		{
			for (Item item : inv.getItems())
			{
				Rune[] runes = Rune.paidBy(item.getId());
				if (runes != null)
				{
					for (Rune r : runes)
					{
						add(have, r, item.getQuantity());
					}
				}
			}
		}

		addPouch(have);

		// staves and tomes last so INFINITE isn't clobbered by a loose rune count
		if (worn != null)
		{
			for (EquipmentInventorySlot slot : new EquipmentInventorySlot[]{EquipmentInventorySlot.WEAPON, EquipmentInventorySlot.SHIELD})
			{
				int id = equippedId(worn, slot);
				if (id < 0)
				{
					continue;
				}

				Rune[] staff = InfiniteRunes.fromStaff(id);
				if (staff != null)
				{
					for (Rune r : staff)
					{
						have.put(r, ThrallState.INFINITE);
					}
				}

				Rune tome = InfiniteRunes.fromTome(client, id);
				if (tome != null)
				{
					have.put(tome, ThrallState.INFINITE);
				}
			}
		}

		return have;
	}

	private void addPouch(Map<Rune, Integer> have)
	{
		EnumComposition pouchEnum = client.getEnum(EnumID.RUNEPOUCH_RUNE);
		if (pouchEnum == null)
		{
			return;
		}

		for (int i = 0; i < POUCH_SLOTS; i++)
		{
			int type = client.getVarbitValue(POUCH_TYPE[i]);
			int qty = client.getVarbitValue(POUCH_QTY[i]);
			if (type == 0 || qty <= 0)
			{
				continue;
			}

			Rune[] runes = Rune.paidBy(pouchEnum.getIntValue(type));
			if (runes == null)
			{
				continue;
			}

			for (Rune r : runes)
			{
				add(have, r, qty);
			}
		}
	}

	private static void add(Map<Rune, Integer> have, Rune r, int qty)
	{
		int now = have.getOrDefault(r, 0);
		if (now == ThrallState.INFINITE)
		{
			return;
		}
		have.put(r, now + qty);
	}

	private static int equippedId(ItemContainer worn, EquipmentInventorySlot slot)
	{
		if (worn == null)
		{
			return -1;
		}
		Item item = worn.getItem(slot.getSlotIdx());
		return item == null ? -1 : item.getId();
	}

	/** True while the screen should be flashing. */
	boolean shouldFlash()
	{
		if (!config.flash() || !state.wrongSpellbook() || wrongSince == null)
		{
			return false;
		}

		int secs = config.flashSeconds();
		return secs <= 0 || Duration.between(wrongSince, Instant.now()).getSeconds() < secs;
	}
}
