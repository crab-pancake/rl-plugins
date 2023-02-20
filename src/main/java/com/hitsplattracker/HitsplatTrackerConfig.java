package com.hitsplattracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("HitsplatTracker")
public interface HitsplatTrackerConfig extends Config {
    @ConfigItem
    (keyName = "showInfobox", name = "Show infobox", description = "shows infobox overlay of hit statistics", position = 0)
    default boolean showInfobox() {
        return true;
    }
    @ConfigItem
    (keyName = "target", name = "Tracked target/s", description = "Track hitsplats dealt or received or both?", position = 1)
    default Target target(){
        return Target.BOTH;
    }

    enum Target{
        DEALT,
        RECEIVED,
        BOTH;
    }

}
