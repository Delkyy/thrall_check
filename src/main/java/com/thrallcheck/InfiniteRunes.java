/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import java.util.HashMap;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;

/**
 * Equipped gear that pays a rune cost for you. Only the elements thralls actually use
 * are listed, so no kodai or bryophyta here on purpose - water and nature buy you
 * nothing on a resurrection spell.
 */
final class InfiniteRunes
{
	private static final Map<Integer, Rune[]> STAVES = new HashMap<>();

	static
	{
		put(Rune.AIR, ItemID.STAFF_OF_AIR, ItemID.AIR_BATTLESTAFF, ItemID.MYSTIC_AIR_STAFF, ItemID.DRAMEN_STAFF_AIR);
		put(Rune.EARTH, ItemID.STAFF_OF_EARTH, ItemID.EARTH_BATTLESTAFF, ItemID.MYSTIC_EARTH_STAFF);
		put(Rune.FIRE, ItemID.STAFF_OF_FIRE, ItemID.FIRE_BATTLESTAFF, ItemID.MYSTIC_FIRE_STAFF, ItemID.DRAMEN_STAFF_FIRE,
			ItemID.TWINFLAME_STAFF);

		// combination staves. mist/mud/steam also give water, which no thrall wants
		put(Rune.AIR, ItemID.MIST_BATTLESTAFF, ItemID.MYSTIC_MIST_BATTLESTAFF);
		put(Rune.EARTH, ItemID.MUD_BATTLESTAFF, ItemID.MYSTIC_MUD_STAFF);
		put(Rune.FIRE, ItemID.STEAM_BATTLESTAFF, ItemID.MYSTIC_STEAM_BATTLESTAFF,
			ItemID.STEAM_BATTLESTAFF_PRETTY, ItemID.MYSTIC_STEAM_BATTLESTAFF_PRETTY);

		putBoth(Rune.AIR, Rune.EARTH, ItemID.DUST_BATTLESTAFF, ItemID.MYSTIC_DUST_BATTLESTAFF);
		putBoth(Rune.AIR, Rune.FIRE, ItemID.SMOKE_BATTLESTAFF, ItemID.MYSTIC_SMOKE_BATTLESTAFF);
		putBoth(Rune.EARTH, Rune.FIRE, ItemID.LAVA_BATTLESTAFF, ItemID.MYSTIC_LAVA_STAFF,
			ItemID.LAVA_BATTLESTAFF_PRETTY, ItemID.MYSTIC_LAVA_STAFF_PRETTY);
	}

	private static void put(Rune r, int... ids)
	{
		for (int id : ids)
		{
			STAVES.put(id, new Rune[]{r});
		}
	}

	private static void putBoth(Rune a, Rune b, int... ids)
	{
		for (int id : ids)
		{
			STAVES.put(id, new Rune[]{a, b});
		}
	}

	private InfiniteRunes()
	{
	}

	static Rune[] fromStaff(int itemId)
	{
		return STAVES.get(itemId);
	}

	/**
	 * Tomes only supply runes while they hold a charge, so the item id alone is a lie.
	 * An empty tome of fire is a shield and nothing else.
	 */
	static Rune fromTome(Client client, int itemId)
	{
		if (itemId == ItemID.TOME_OF_FIRE && client.getVarbitValue(VarbitID.CHARGES_TOME_OF_FIRE_QUANTITY) > 0)
		{
			return Rune.FIRE;
		}
		if (itemId == ItemID.TOME_OF_EARTH && client.getVarbitValue(VarbitID.CHARGES_TOME_OF_EARTH_QUANTITY) > 0)
		{
			return Rune.EARTH;
		}
		return null;
	}
}
