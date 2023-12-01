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
		description = "Spawns more axes (+10 invo)"
	)
	default boolean moreAxes()
	{
		return true;
	}

	@ConfigItem(
		keyName = "funkyAxes",
		name = "Funky Axes",
		description = "Spawns axes in funky directions (+15 invo)"
	)
	default boolean funkyAxes()
	{
		return true;
	}

	@ConfigItem(
		keyName = "moreHeads",
		name = "More Heads",
		description = "Spawns more heads (+10 invo)"
	)
	default boolean moreHeads()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hydraHeads",
		name = "Hydra Heads",
		description = "Spawns all the heads. Also adds cHaOs (+30 invo)"
	)
	default boolean hydraHeads()
	{
		return true;
	}

	@ConfigItem(
		keyName = "style",
		name = "Projectile Style",
		description = "Style of Vardorvis head projectile"
	)
	default Style style()
	{
		return Style.CoX;
	}

	@Getter
	@AllArgsConstructor
	enum Style
	{
		Inferno(1378, 1380),
		CoX(1343, 1341),
		ToB(1607, 1606),
		ToA(2241, 2224),
		Default(2521,2520);

		private final int range;
		private final int magic;
	}
}
