/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Client
 *  net.runelite.api.FriendsChatRank
 *  net.runelite.api.Player
 *  net.runelite.api.Point
 *  net.runelite.api.WorldType
 *  net.runelite.api.clan.ClanTitle
 *  net.runelite.client.game.ChatIconManager
 *  net.runelite.client.game.ItemManager
 *  net.runelite.client.hiscore.HiscoreEndpoint
 *  net.runelite.client.hiscore.HiscoreManager
 *  net.runelite.client.hiscore.HiscoreResult
 *  net.runelite.client.hiscore.HiscoreSkill
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.OverlayPriority
 *  net.runelite.client.ui.overlay.OverlayUtil
 *  net.runelite.client.util.ImageUtil
 *  net.runelite.client.util.Text
 */
package com.pvpplayerindicators;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.FriendsChatRank;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.WorldType;
import net.runelite.api.clan.ClanTitle;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.hiscore.HiscoreEndpoint;
import net.runelite.client.hiscore.HiscoreManager;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import com.pvpplayerindicators.PvPPlayerIndicatorsConfig;
import com.pvpplayerindicators.PvPPlayerIndicatorsPlugin;
import com.pvpplayerindicators.PvPPlayerIndicatorsService;
import com.pvpplayerindicators.PvPPlayerNameLocation;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

@Singleton
public class PvPPlayerIndicatorsOverlay
extends Overlay {
    private final PvPPlayerIndicatorsService playerIndicatorsService;
    private final PvPPlayerIndicatorsConfig config;
    private final ChatIconManager chatIconManager;
    private final HiscoreManager hiscoreManager;
    private final BufferedImage agilityIcon = ImageUtil.loadImageResource(PvPPlayerIndicatorsPlugin.class, "agility.png");
    private final BufferedImage noAgilityIcon = ImageUtil.loadImageResource(PvPPlayerIndicatorsPlugin.class, "no-agility.png");
    @Inject
    private Client client;
    @Inject
    private ItemManager itemManager;

    @Inject
    private PvPPlayerIndicatorsOverlay(PvPPlayerIndicatorsConfig config, PvPPlayerIndicatorsService playerIndicatorsService, ChatIconManager chatIconManager, HiscoreManager hiscoreManager) {
        this.config = config;
        this.playerIndicatorsService = playerIndicatorsService;
        this.chatIconManager = chatIconManager;
        this.hiscoreManager = hiscoreManager;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setPriority(OverlayPriority.MED);
    }

    public Dimension render(Graphics2D graphics) {
        this.playerIndicatorsService.forEachPlayer((player, color) -> this.renderPlayerOverlay(graphics, player, color));
        return null;
    }

    private void renderPlayerOverlay(Graphics2D graphics, Player actor, Color color) {
        HiscoreResult hiscoreResult;
        ClanTitle clanTitle;
        int zOffset;
        PvPPlayerNameLocation drawPlayerNamesConfig = this.config.playerNamePosition();
        if (drawPlayerNamesConfig == PvPPlayerNameLocation.DISABLED) {
            return;
        }
        switch (drawPlayerNamesConfig) {
            case MODEL_CENTER: 
            case MODEL_RIGHT: {
                zOffset = actor.getLogicalHeight() / 2;
                break;
            }
            default: {
                zOffset = actor.getLogicalHeight() + 40;
            }
        }
        Object name = Text.sanitize(actor.getName());
        Point textLocation = actor.getCanvasTextLocation(graphics, (String)name, zOffset);
        if (drawPlayerNamesConfig == PvPPlayerNameLocation.MODEL_RIGHT) {
            textLocation = actor.getCanvasTextLocation(graphics, "", zOffset);
            if (textLocation == null) {
                return;
            }
            textLocation = new Point(textLocation.getX() + 10, textLocation.getY());
        }
        if (textLocation == null) {
            return;
        }
        BufferedImage rankImage = null;
        if (actor.isFriendsChatMember() && this.config.drawFriendsChatMemberNames() && this.config.showFriendsChatRanks()) {
            FriendsChatRank rank = this.playerIndicatorsService.getFriendsChatRank(actor);
            if (rank != FriendsChatRank.UNRANKED) {
                rankImage = this.chatIconManager.getRankImage(rank);
            }
        } else if (actor.isClanMember() && this.config.highlightClanMembers() && this.config.showClanChatRanks() && (clanTitle = this.playerIndicatorsService.getClanTitle(actor)) != null) {
            rankImage = this.chatIconManager.getRankImage(clanTitle);
        }
        if (rankImage != null) {
            int imageNegativeMargin;
            int imageTextMargin;
            int imageWidth = rankImage.getWidth();
            if (drawPlayerNamesConfig == PvPPlayerNameLocation.MODEL_RIGHT) {
                imageTextMargin = imageWidth;
                imageNegativeMargin = 0;
            } else {
                imageTextMargin = imageWidth / 2;
                imageNegativeMargin = imageWidth / 2;
            }
            int textHeight = graphics.getFontMetrics().getHeight() - graphics.getFontMetrics().getMaxDescent();
            Point imageLocation = new Point(textLocation.getX() - imageNegativeMargin - 1, textLocation.getY() - textHeight / 2 - rankImage.getHeight() / 2);
            OverlayUtil.renderImageLocation(graphics, imageLocation, rankImage);
            textLocation = new Point(textLocation.getX() + imageTextMargin, textLocation.getY());
        }
        if (this.config.showCombatLevel()) {
            name = name + " (" + actor.getCombatLevel() + ")";
        }
        OverlayUtil.renderTextLocation(graphics, textLocation, (String)name, color);
    }

    private boolean checkWildy() {
        return this.client.getVarbitValue(5963) == 1 || WorldType.isPvpWorld(this.client.getWorldType());
    }
}

