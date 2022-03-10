package com.socket.plugins.specs;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
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

	@ConfigItem(keyName="vulnerability", name="Show Vulnerability", description="Displays an infobox when you successfully land vulnerability", position=3)
	default boolean vulnerability() {
		return true;
	}
}
