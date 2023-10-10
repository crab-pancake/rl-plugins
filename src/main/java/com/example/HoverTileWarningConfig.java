package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("hoverTileWarning")
public interface HoverTileWarningConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "showUnclickable",
		name = "Show unclickable",
		description = "Change mouse cursor when hovered tile is unclickable"
	)
	default boolean showUnclickable()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = "showUnexpected",
		name = "Show unexpected pathing",
		description = "Change cursor when hovered tile will path to a different place than the cursor"
	)
	default boolean showUnexpected()
	{
		return true;
	}
}
