package com.cropcircle;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface CropCircleConfig extends Config
{
	@ConfigItem(
		keyName = "greeting",
		name = "Welcome Greeting",
		description = "The message to show to the user when they login"
	)
	default String greeting()
	{
		return "Hello";
	}

	@ConfigItem(
			keyName = "everywhereOverlay",
			name = "Overlay everywhere",
			description = "Overlay shows everywhere?"
	)
	default boolean everywhereOverlay()
	{
		return false;
	}
}
