package com.sbs;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("sbsicedemon")
public interface sbsConfig extends Config
{
	@ConfigItem(
			keyName = "debug",
			name = "debug",
			description = "debug text"
	)
	default boolean debug()
	{
		return false;
	}
}
