/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.ConfigSection
 */
package com.dplayerindicators;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(value="combatcounter")
public interface DPlayerIndicatorsConfig
extends Config {
    @ConfigSection(name="Highlight Options", description="Toggle highlighted players by type (self, friends, etc.) and choose their highlight colors", position=99)
    public static final String highlightSection = "section";

    @ConfigItem(position=0, keyName="drawOwnName", name="Highlight own player", description="Configures whether or not your own player should be highlighted", section="section")
    default public boolean highlightOwnPlayer() {
        return false;
    }

    @ConfigItem(position=1, keyName="ownNameColor", name="Own player color", description="Color of your own player", section="section")
    default public Color getOwnPlayerColor() {
        return new Color(0, 184, 212);
    }

    @ConfigItem(position=2, keyName="drawFriendNames", name="Highlight friends", description="Configures whether or not friends should be highlighted", section="section")
    default public boolean highlightFriends() {
        return true;
    }

    @ConfigItem(position=3, keyName="friendNameColor", name="Friend color", description="Color of friend names", section="section")
    default public Color getFriendColor() {
        return new Color(0, 200, 83);
    }

    @ConfigItem(position=4, keyName="drawClanMemberNames", name="Highlight friends chat members", description="Configures if friends chat members should be highlighted", section="section")
    default public boolean drawFriendsChatMemberNames() {
        return true;
    }

    @ConfigItem(position=5, keyName="clanMemberColor", name="Friends chat member color", description="Color of friends chat members", section="section")
    default public Color getFriendsChatMemberColor() {
        return new Color(170, 0, 255);
    }

    @ConfigItem(position=6, keyName="drawTeamMemberNames", name="Highlight team members", description="Configures whether or not team members should be highlighted", section="section")
    default public boolean highlightTeamMembers() {
        return true;
    }

    @ConfigItem(position=7, keyName="teamMemberColor", name="Team member color", description="Color of team members", section="section")
    default public Color getTeamMemberColor() {
        return new Color(19, 110, 247);
    }

    @ConfigItem(position=8, keyName="drawOpposingTeamMemberNames", name="Highlight opposing team members", description="Configures whether or not opposing team members should be highlighted", section="section")
    default public boolean highlightOpposingTeamMembers() {
        return true;
    }

    @ConfigItem(position=9, keyName="opposingTeamMemberColor", name="Opposing team member color", description="Color of opposing team members", section="section")
    default public Color getOpposingTeamMemberColor() {
        return Color.RED.darker();
    }

    @ConfigItem(position=10, keyName="drawClanChatMemberNames", name="Highlight clan members", description="Configures whether or not clan members should be highlighted", section="section")
    default public boolean highlightClanMembers() {
        return true;
    }

    @ConfigItem(position=11, keyName="clanChatMemberColor", name="Clan member", description="Color of clan members", section="section")
    default public Color getClanMemberColor() {
        return new Color(36, 15, 171);
    }

    @ConfigItem(position=12, keyName="drawNonClanMemberNames", name="Highlight others", description="Configures whether or not other players should be highlighted", section="section")
    default public boolean highlightOthers() {
        return false;
    }

    @ConfigItem(position=13, keyName="nonClanMemberColor", name="Others color", description="Color of other players names", section="section")
    default public Color getOthersColor() {
        return Color.RED;
    }

    @ConfigItem(position=14, keyName="drawPlayerTiles", name="Draw tiles under players", description="Configures whether or not tiles under highlighted players should be drawn")
    default public boolean drawTiles() {
        return false;
    }

    @ConfigItem(position=15, keyName="drawMinimapNames", name="Draw names on minimap", description="Configures whether or not minimap names for players with rendered names should be drawn")
    default public boolean drawMinimapNames() {
        return false;
    }

    @ConfigItem(position=16, keyName="colorPlayerMenu", name="Colorize player menu", description="Color right click menu for players")
    default public boolean colorPlayerMenu() {
        return true;
    }

    @ConfigItem(position=17, keyName="clanMenuIcons", name="Show friends chat ranks", description="Add friends chat rank to right click menu and next to player names")
    default public boolean showFriendsChatRanks() {
        return true;
    }

    @ConfigItem(position=18, keyName="clanchatMenuIcons", name="Show clan chat ranks", description="Add clan chat rank to right click menu and next to player names")
    default public boolean showClanChatRanks() {
        return true;
    }
}

