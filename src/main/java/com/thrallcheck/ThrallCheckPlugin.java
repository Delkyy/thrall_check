/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import com.google.inject.Provides;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
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
	description = "Flashes the screen if you have the Book of the Dead on the wrong spellbook, and tracks your thrall runes and prayer",
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
	private ThrallFlashOverlay flashOverlay;

	@Inject
	private Notifier notifier;

	@Inject
	private ConfigManager configManager;

	@Getter
	private ThrallState state = new ThrallState(false, false, null, null, 0);

	private boolean warned;
	private Instant wrongSince;

	/** Thralls we can currently see. Spawn/despawn keeps this honest across a hop. */
	private final Set<NPC> thralls = new HashSet<>();

	/** Ticks since we were last in combat. Counts up so a brief gap doesn't reset it. */
	private int ticksInCombat;
	private int ticksSinceCombat = Integer.MAX_VALUE;

	@Provides
	ThrallCheckConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ThrallCheckConfig.class);
	}

	@Override
	protected void startUp()
	{
		migrate();
		overlayManager.add(overlay);
		overlayManager.add(flashOverlay);
	}

	/**
	 * The old boolean "flash" key became the three-way "alertStyle".
	 *
	 * Renaming a key on a live plugin silently throws away what people already set, so
	 * anyone who had turned flashing off keeps it off instead of getting a faceful of
	 * red on the next login.
	 */
	private void migrate()
	{
		if (configManager.getConfiguration(ThrallCheckConfig.GROUP, "migrated") != null)
		{
			return;
		}

		String flash = configManager.getConfiguration(ThrallCheckConfig.GROUP, "flash");
		if ("false".equals(flash))
		{
			configManager.setConfiguration(ThrallCheckConfig.GROUP, "alertStyle",
				ThrallCheckConfig.AlertStyle.OFF);
		}
		configManager.unsetConfiguration(ThrallCheckConfig.GROUP, "flash");
		configManager.setConfiguration(ThrallCheckConfig.GROUP, "migrated", "1");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlayManager.remove(flashOverlay);
		state = new ThrallState(false, false, null, null, 0);
		warned = false;
		wrongSince = null;
		thralls.clear();
		ticksInCombat = 0;
		ticksSinceCombat = Integer.MAX_VALUE;
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		if (ThrallNpcs.isThrall(npc.getId()))
		{
			thralls.add(npc);
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		thralls.remove(event.getNpc());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		// npcs don't despawn cleanly across a hop or a loading screen, so the set would
		// keep a thrall that isn't there and the reminder would never fire
		if (event.getGameState() == GameState.LOADING
			|| event.getGameState() == GameState.HOPPING
			|| event.getGameState() == GameState.LOGIN_SCREEN)
		{
			thralls.clear();
			ticksInCombat = 0;
			ticksSinceCombat = Integer.MAX_VALUE;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		state = build();
		trackCombat();

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

		// boosted, not real. that's your current points, which is what a cast spends
		return new ThrallState(book, arceuus, tier(), countRunes(inv, worn),
			client.getBoostedSkillLevel(Skill.PRAYER));
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

	/**
	 * Are we fighting, and is a thrall out?
	 *
	 * "In combat" is you interacting with an npc that's interacting back, which is the
	 * same test the idle notifier uses. A monster you clicked once but walked away from
	 * doesn't count, and neither does standing next to something fighting someone else.
	 */
	private void trackCombat()
	{
		Player me = client.getLocalPlayer();
		boolean fighting = false;

		if (me != null)
		{
			Actor target = me.getInteracting();
			fighting = target instanceof NPC && !target.isDead()
				&& target.getInteracting() == me;
		}

		if (fighting)
		{
			ticksInCombat++;
			ticksSinceCombat = 0;
		}
		else
		{
			// a couple of ticks of grace so swapping targets doesn't reset the counter
			if (ticksSinceCombat < Integer.MAX_VALUE)
			{
				ticksSinceCombat++;
			}
			if (ticksSinceCombat > 3)
			{
				ticksInCombat = 0;
			}
		}
	}

	/** True while you're fighting with nothing summoned. */
	boolean needsThrall()
	{
		return config.remindThrall()
			&& ticksInCombat >= config.remindDelay()
			&& ticksSinceCombat <= 3
			&& !hasThrall();
	}

	/**
	 * Is one of the thralls on screen actually ours?
	 *
	 * A thrall follows its owner, so isFollower() plus interacting with us is the same
	 * ownership test the entity hider uses for pets. Without it someone else's thrall
	 * standing nearby would silence your reminder.
	 */
	private boolean hasThrall()
	{
		Player me = client.getLocalPlayer();
		if (me == null)
		{
			return false;
		}

		for (NPC npc : thralls)
		{
			NPCComposition comp = npc.getComposition();
			if (comp != null && comp.isFollower() && npc.getInteracting() == me)
			{
				return true;
			}
		}
		return false;
	}

	/** True while the screen should be flashing. */
	boolean shouldFlash()
	{
		if (config.alertStyle() != ThrallCheckConfig.AlertStyle.FLASH
			|| !state.wrongSpellbook() || wrongSince == null)
		{
			return false;
		}

		int secs = config.flashSeconds();
		return secs <= 0 || Duration.between(wrongSince, Instant.now()).getSeconds() < secs;
	}

	/** True while the banner should be drawn instead of a flash. */
	boolean shouldBanner()
	{
		return config.alertStyle() == ThrallCheckConfig.AlertStyle.BANNER
			&& needsWarning();
	}

	/** What the banner should say. */
	String bannerText()
	{
		return state.wrongSpellbook()
			? "Wrong spellbook for thralls"
			: "No Book of the Dead";
	}
}
