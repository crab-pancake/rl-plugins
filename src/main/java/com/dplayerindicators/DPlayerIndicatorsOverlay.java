/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.FriendsChatMember
 *  net.runelite.api.FriendsChatRank
 *  net.runelite.api.Player
 *  net.runelite.api.Point
 *  net.runelite.api.clan.ClanChannel
 *  net.runelite.api.clan.ClanChannelMember
 *  net.runelite.api.clan.ClanRank
 *  net.runelite.api.clan.ClanSettings
 *  net.runelite.client.game.ChatIconManager
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.OverlayPriority
 *  net.runelite.client.ui.overlay.OverlayUtil
 *  net.runelite.client.util.Text
 */
package com.dplayerindicators;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.FriendsChatRank;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.clan.ClanChannelMember;
import net.runelite.api.clan.ClanRank;
import net.runelite.api.clan.ClanSettings;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.Text;

public class DPlayerIndicatorsOverlay
extends Overlay {
    private Client client;
    private DPlayerIndicatorsConfig config;
    private ChatIconManager chatIconManager;

    @Inject
    public DPlayerIndicatorsOverlay(Client client, DPlayerIndicatorsPlugin plugin, DPlayerIndicatorsConfig config, ChatIconManager cim) {
        super(plugin);
        this.client = client;
        this.config = config;
        this.chatIconManager = cim;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setPriority(OverlayPriority.MED);
    }

    public Dimension render(Graphics2D g) {
        for (int i = 0; i < 2048; ++i) {
            boolean on_other_team;
            Player p = this.client.getCachedPlayers()[i];
            if (p == null) continue;
            boolean on_same_team = this.client.getLocalPlayer().getTeam() > 0 && this.client.getLocalPlayer().getTeam() == p.getTeam();
            boolean bl = on_other_team = this.client.getLocalPlayer().getTeam() > 0 && this.client.getLocalPlayer().getTeam() != p.getTeam();
            if (p == this.client.getLocalPlayer()) {
                if (!this.config.highlightOwnPlayer()) continue;
                this.render_player(g, p, this.config.getOwnPlayerColor());
                continue;
            }
            if (this.config.highlightFriends() && this.client.isFriended(p.getName(), false)) {
                this.render_player(g, p, this.config.getFriendColor());
                continue;
            }
            if (this.config.drawFriendsChatMemberNames() && p.isFriendsChatMember()) {
                this.render_player(g, p, this.config.getFriendsChatMemberColor());
                continue;
            }
            if (this.config.highlightTeamMembers() && on_same_team) {
                this.render_player(g, p, this.config.getTeamMemberColor());
                continue;
            }
            if (this.config.highlightOpposingTeamMembers() && on_other_team) {
                this.render_player(g, p, this.config.getOpposingTeamMemberColor());
                continue;
            }
            if (this.config.highlightClanMembers() && p.isClanMember()) {
                this.render_player(g, p, this.config.getClanMemberColor());
                continue;
            }
            if (!this.config.highlightOthers()) continue;
            this.render_player(g, p, this.config.getOthersColor());
        }
        return null;
    }

    private void render_player(Graphics2D g, Player p, Color c) {
        int zOffset = p.getLogicalHeight() + 40;
        String name = Text.sanitize(p.getName());
        Point textLocation = p.getCanvasTextLocation(g, name, zOffset);
        if (textLocation == null) {
            return;
        }
        BufferedImage image = null;
        if (this.config.showFriendsChatRanks() && p.isFriendsChatMember()) {
            FriendsChatRank rank = this.client.getFriendsChatManager().findByName(name).getRank();
            if (rank != FriendsChatRank.UNRANKED) {
                image = this.chatIconManager.getRankImage(rank);
            }
        } else if (this.config.showClanChatRanks() && p.isClanMember()) {
            ClanChannelMember member;
            ClanChannel clanChannel = this.client.getClanChannel();
            ClanSettings clanSettings = this.client.getClanSettings();
            if (clanChannel != null && clanSettings != null && (member = clanChannel.findMember(p.getName())) != null) {
                ClanRank rank = member.getRank();
                image = this.chatIconManager.getRankImage(clanSettings.titleForRank(rank));
            }
        }
        if (image != null) {
            int imageWidth = image.getWidth();
            int imageTextMargin = imageWidth / 2;
            int imageNegativeMargin = imageWidth / 2;
            int textHeight = g.getFontMetrics().getHeight() - g.getFontMetrics().getMaxDescent();
            Point imageLocation = new Point(textLocation.getX() - imageNegativeMargin - 1, textLocation.getY() - textHeight / 2 - image.getHeight() / 2);
            OverlayUtil.renderImageLocation(g, imageLocation, image);
            textLocation = new Point(textLocation.getX() + imageTextMargin, textLocation.getY());
        }
        OverlayUtil.renderTextLocation(g, textLocation, name, c);
    }
}

