package com.dmgtracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("HitsplatTracker")
public interface HitsplatTrackerConfig extends Config {
    @ConfigItem
    (keyName = "showInfobox", name = "Show infobox", description = "shows infobox overlay of hit statistics", position = 0)
    default boolean showInfobox() {
        return false;
    }
}
