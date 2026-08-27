/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import java.util.HashSet;
import java.util.Set;
import net.runelite.api.gameval.NpcID;

/**
 * The thrall npc ids, so we can tell whether one is out.
 *
 * There is no "do I have a thrall" varbit - checked VarbitID and the only thrall entries
 * are a combat achievement, a league override and the sigil. So the answer has to come
 * from the npcs actually standing next to you.
 */
final class ThrallNpcs
{
	private static final Set<Integer> IDS = new HashSet<>();

	static
	{
		// the arceuus spellbook ones: ghost, skeleton and zombie in three tiers
		IDS.add(NpcID.ARCEUUS_THRALL_GHOST_LESSER);
		IDS.add(NpcID.ARCEUUS_THRALL_GHOST_SUPERIOR);
		IDS.add(NpcID.ARCEUUS_THRALL_GHOST_GREATER);
		IDS.add(NpcID.ARCEUUS_THRALL_SKELETON_LESSER);
		IDS.add(NpcID.ARCEUUS_THRALL_SKELETON_SUPERIOR);
		IDS.add(NpcID.ARCEUUS_THRALL_SKELETON_GREATER);
		IDS.add(NpcID.ARCEUUS_THRALL_ZOMBIE_LESSER);
		IDS.add(NpcID.ARCEUUS_THRALL_ZOMBIE_SUPERIOR);
		IDS.add(NpcID.ARCEUUS_THRALL_ZOMBIE_GREATER);

		// the Impish whistle cosmetic override reskins a normal thrall as an imp -
		// same spell, same tier, different model. missing these is what let a real
		// thrall go undetected and the reminder fire while one was standing right
		// there. checked: "The impish whistle can be used to unlock a cosmetic
		// override for thralls, giving them the appearances of imps."
		// (oldschool.runescape.wiki/w/Thrall)
		IDS.add(NpcID.THRALL_IMP_MAGIC_LESSER);
		IDS.add(NpcID.THRALL_IMP_MAGIC_SUPERIOR);
		IDS.add(NpcID.THRALL_IMP_MAGIC_GREATER);
		IDS.add(NpcID.THRALL_IMP_RANGED_LESSER);
		IDS.add(NpcID.THRALL_IMP_RANGED_SUPERIOR);
		IDS.add(NpcID.THRALL_IMP_RANGED_GREATER);
		IDS.add(NpcID.THRALL_IMP_MELEE_LESSER);
		IDS.add(NpcID.THRALL_IMP_MELEE_SUPERIOR);
		IDS.add(NpcID.THRALL_IMP_MELEE_GREATER);
	}

	private ThrallNpcs()
	{
	}

	static boolean isThrall(int npcId)
	{
		return IDS.contains(npcId);
	}
}
