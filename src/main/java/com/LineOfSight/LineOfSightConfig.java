package com.LineOfSight;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("LineOfSight")
public interface LineOfSightConfig extends Config {
    @ConfigItem(keyName = "lineOfSight", name = "Line of Sight", description = "Show all tiles you have los to")
    default boolean lineOfSight() {
        return false;
    }
    @ConfigItem(keyName = "hovered", name = "Hovered tile", description = "Show tiles in line to hovered tile")
    default boolean hoveredTile() {
        return true;
    }
    @ConfigItem(keyName = "interacting", name = "Interacting", description = "Show line to target you are interacting with")
    default boolean interacting() {
        return true;
    }
    @ConfigItem(keyName = "mode", name = "Mode", description = "Line of sight or line of walk?")
    default LineOfSightMode mode() {
        return LineOfSightMode.SIGHT;
    }
    @ConfigItem(keyName = "origin", name = "Origin", description = "Line origin from player or target?")
    default Origin origin() {
        return Origin.PLAYER;
    }


    enum LineOfSightMode{
        SIGHT,
        WALK;
    }
    enum Origin {
        PLAYER,
        TARGET;
    }
}
