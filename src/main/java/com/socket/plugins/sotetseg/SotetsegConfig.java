/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.Range
 */
package com.socket.plugins.sotetseg;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(value="Socket Sotetseg Config")
public interface SotetsegConfig
extends Config {
    @ConfigItem(position=0, keyName="getTileColor", name="Tile Color", description="The color of the tiles.")
    default Color getTileColor() {
        return Color.GREEN;
    }

    @Range(min=0, max=255)
    @ConfigItem(position=1, keyName="getTileTransparency", name="Tile Transparency", description="The color transparency of the tiles. Ranges from 0 to 255, inclusive.")
    default int getTileTransparency() {
        return 50;
    }

    @ConfigItem(position=2, keyName="getTileOutline", name="Tile Outline Color", description="The color of the outline of the tiles.")
    default Color getTileOutline() {
        return Color.GREEN;
    }

    @Range(min=0, max=5)
    @ConfigItem(position=3, keyName="getTileOutlineSize", name="Tile Outline Size", description="The size of the outline of the tiles.")
    default int getTileOutlineSize() {
        return 1;
    }

    @ConfigItem(position=4, keyName="streamerMode", name="Streamer Mode", description="Send Maze Info to team but don't display maze overlay on your screen.")
    default boolean streamerMode() {
        return false;
    }

    @ConfigItem(position=5, keyName="testOverlay", name="Show Test Tiles", description="Shows test tiles to allow you to change your tile outline settings")
    default boolean showTestOverlay() {
        return false;
    }

    @ConfigItem(position=6, keyName="warnBall", name="Warns if invisible ball is sent", description="Warns you if the ball was sent while you were chosen since it's invisible otherwise")
    default boolean warnBall() {
        return true;
    }

    @ConfigItem(position=7, keyName="trueMaze", name="Maze True Tile", description="Shows your true tile location only when the maze is active")
    default boolean trueMaze() {
        return true;
    }

    @ConfigItem(position=8, keyName="trueMazeColor", name="Maze True Tile Color", description="Color for the maze true tile")
    default Color trueMazeColor() {
        return Color.RED;
    }

    @Range(min=1, max=5)
    @ConfigItem(position=9, keyName="trueMazeThicc", name="Maze True Tile Width", description="Width for the maze true location tile")
    default int trueMazeThicc() {
        return 2;
    }

    @ConfigItem(position=10, keyName="antiAlias", name="Maze True Tile Anti-Aliasing", description="Turns on anti-aliasing for the tiles. Makes them more smoother.")
    default boolean antiAlias() {
        return false;
    }
}

