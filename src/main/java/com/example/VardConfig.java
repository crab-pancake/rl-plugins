package com.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("vardorvis")
public interface VardConfig extends Config
{
	@ConfigItem(
		keyName = "moreAxes",
		name = "More Axes",
		description = "Spawns more axes (+10 invo)",
		hidden = true
	)
	default boolean moreAxes()
	{
		return false;
	}

	@ConfigItem(
		keyName = "funkyAxes",
		name = "Funky Axes",
		description = "Spawns axes in funky directions (+15 invo)",
		hidden = true
	)
	default boolean funkyAxes()
	{
		return false;
	}

	@ConfigItem(
		keyName = "moreHeads",
		name = "More Heads",
		description = "Spawns more heads"
	)
	default boolean moreHeads()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hydraHeads",
		name = "Hydra Heads",
		description = "Spawns all the heads. Also adds cHaOs (requires more heads)",
		hidden = true
	)
	default boolean hydraHeads()
	{
		return false;
	}

	@ConfigItem(
		keyName = "spikyFloor",
		name = "Spiky Floor",
		description = "Boss spawns spikes under you"
	)
	default boolean spikyFloor()
	{
		return false;
	}

	@ConfigItem(
		keyName = "mistakeTracker",
		name = "Mistake Tracker",
		description = "Adds chat messages when you make mistakes"
	)
	default boolean mistakeTracker()
	{
		return false;
	}
}
