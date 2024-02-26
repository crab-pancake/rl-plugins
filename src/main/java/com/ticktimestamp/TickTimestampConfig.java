package com.ticktimestamp;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("ticktimestamp")
public interface TickTimestampConfig extends Config
{
	@ConfigItem(
		keyName = "chatmessages",
		name = "Chat messages",
		description = "Display timestamp on public and private chats"
	)
	default boolean chatMessage()
	{
		return false;
	}

	@ConfigItem(
		keyName = "opaqueTimestamp",
		name = "Timestamps (opaque)",
		position = 1,
		description = "Colour of Timestamps from the Timestamps plugin (opaque)"
	)
	Color opaqueTimestamp();

	@ConfigItem(
		keyName = "transparentTimestamp",
		name = "Timestamps (transparent)",
		position = 2,
		description = "Colour of Timestamps from the Timestamps plugin (transparent)"
	)
	Color transparentTimestamp();
}
