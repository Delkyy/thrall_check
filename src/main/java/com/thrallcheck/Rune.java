/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import net.runelite.api.gameval.ItemID;

/**
 * The four runes a resurrection spell can ask for, plus every item that pays for them.
 */
@Getter
enum Rune
{
	AIR("Air"),
	EARTH("Earth"),
	FIRE("Fire"),
	MIND("Mind"),
	DEATH("Death"),
	BLOOD("Blood"),
	COSMIC("Cosmic");

	private final String name;

	Rune(String name)
	{
		this.name = name;
	}

	// item id -> what it pays for. combination runes pay for both halves, and a sunfire
	// is just a fire rune with a hat on
	private static final Map<Integer, Rune[]> ITEMS = new HashMap<>();

	static
	{
		ITEMS.put(ItemID.AIRRUNE, new Rune[]{AIR});
		ITEMS.put(ItemID.EARTHRUNE, new Rune[]{EARTH});
		ITEMS.put(ItemID.FIRERUNE, new Rune[]{FIRE});
		ITEMS.put(ItemID.MINDRUNE, new Rune[]{MIND});
		ITEMS.put(ItemID.DEATHRUNE, new Rune[]{DEATH});
		ITEMS.put(ItemID.BLOODRUNE, new Rune[]{BLOOD});
		ITEMS.put(ItemID.COSMICRUNE, new Rune[]{COSMIC});
		ITEMS.put(ItemID.SUNFIRERUNE, new Rune[]{FIRE});

		ITEMS.put(ItemID.MISTRUNE, new Rune[]{AIR});
		ITEMS.put(ItemID.DUSTRUNE, new Rune[]{AIR, EARTH});
		ITEMS.put(ItemID.SMOKERUNE, new Rune[]{AIR, FIRE});
		ITEMS.put(ItemID.MUDRUNE, new Rune[]{EARTH});
		ITEMS.put(ItemID.LAVARUNE, new Rune[]{EARTH, FIRE});
		ITEMS.put(ItemID.STEAMRUNE, new Rune[]{FIRE});
	}

	static Rune[] paidBy(int itemId)
	{
		return ITEMS.get(itemId);
	}
}
