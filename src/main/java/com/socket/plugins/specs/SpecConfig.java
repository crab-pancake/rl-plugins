package com.socket.plugins.specs;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("mspeccounter")
public interface SpecConfig extends Config
{
	@ConfigItem(position=0, keyName="showHitOverlay", name="Hit Overlay", description="Show the special attack overlay.")
	default boolean showHitOverlay() {
		return true;
	}

	@ConfigItem(position=1, keyName="getFadeDelay", name="Fade Delay", description="Delay, in milliseconds, until the icon disappears.")
	default int getFadeDelay() {
		return 5000;
	}

	@ConfigItem(position=2, keyName="getMaxHeight", name="Travel Height", description="Maximum height, in pixels, for the icon to travel.")
	default int getMaxHeight() {
		return 200;
	}

	@ConfigItem(position=3, keyName="guessDawnbringer", name="Guess Dawnbringer Hit", description="Guess Dawnbringer based on XP drop. Provides faster results.")
	default boolean guessDawnbringer() {
		return true;
	}

	@ConfigItem(position=4, keyName="vulnerability", name="Show Vulnerability", description="Displays an infobox when you successfully land vulnerability")
	default boolean vulnerability() {
		return true;
	}

	@ConfigItem(position=5, keyName = "identifier", name = "Shared socket identifier", description = "Share socket spec data with spec counter? Restart plugin to change")
	default boolean identifier()
	{
		return true;
	}

	@ConfigItem(keyName = "debug", name = "Debug", description = "Log debug text", hidden = true)
	default boolean debug()
	{
		return true;
	}
}
