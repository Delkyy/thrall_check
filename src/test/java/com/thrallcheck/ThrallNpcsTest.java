/*
 * Copyright (c) 2026, Delkyy
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.thrallcheck;

import net.runelite.api.gameval.NpcID;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * The reminder is only useful if it fires for a thrall that's actually gone. A wrong id
 * here means it either nags you while a thrall is out, or stays silent when none is.
 */
public class ThrallNpcsTest
{
	@Test
	public void everyArceuusThrallCounts()
	{
		// three types, three tiers. missing any one means the reminder nags while that
		// thrall is standing right there.
		int[] all = {
			NpcID.ARCEUUS_THRALL_GHOST_LESSER,
			NpcID.ARCEUUS_THRALL_GHOST_SUPERIOR,
			NpcID.ARCEUUS_THRALL_GHOST_GREATER,
			NpcID.ARCEUUS_THRALL_SKELETON_LESSER,
			NpcID.ARCEUUS_THRALL_SKELETON_SUPERIOR,
			NpcID.ARCEUUS_THRALL_SKELETON_GREATER,
			NpcID.ARCEUUS_THRALL_ZOMBIE_LESSER,
			NpcID.ARCEUUS_THRALL_ZOMBIE_SUPERIOR,
			NpcID.ARCEUUS_THRALL_ZOMBIE_GREATER,
		};

		for (int id : all)
		{
			assertTrue("id " + id + " should be a thrall", ThrallNpcs.isThrall(id));
		}
	}

	@Test
	public void otherFollowersAreNotThralls()
	{
		// the imp thralls are a different spell entirely, and the toa baboon isn't ours
		assertFalse(ThrallNpcs.isThrall(NpcID.THRALL_IMP_MAGIC_GREATER));
		assertFalse(ThrallNpcs.isThrall(NpcID.TOA_PATH_APMEKEN_BABOON_THRALL));
		assertFalse(ThrallNpcs.isThrall(0));
		assertFalse(ThrallNpcs.isThrall(-1));
	}
}
