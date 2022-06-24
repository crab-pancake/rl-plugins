/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.inject.Provides
 *  com.openosrs.client.util.PvPUtil
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.FriendsChatRank
 *  net.runelite.api.MenuAction
 *  net.runelite.api.MenuEntry
 *  net.runelite.api.Player
 *  net.runelite.api.clan.ClanTitle
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.events.ClientTick
 *  net.runelite.api.events.GameObjectSpawned
 *  net.runelite.api.events.PlayerSpawned
 *  net.runelite.api.events.ScriptPostFired
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 *  net.runelite.client.callback.ClientThread
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.game.ChatIconManager
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 *  net.runelite.client.util.ColorUtil
 *  org.pf4j.Extension
 */
package com.pvpplayerindicators;

import com.google.inject.Provides;
import java.awt.Color;
import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.api.clan.ClanTitle;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;

@PluginDescriptor(name="<html><font color=#25c550>[S] Player Indicators", description="Highlight players on-screen and/or on the minimap", tags={"highlight", "minimap", "overlay", "players"}, conflicts={"Player Indicators"})
public class PvPPlayerIndicatorsPlugin
extends Plugin {
    private static final String TRADING_WITH_TEXT = "Trading with: ";
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private PvPPlayerIndicatorsConfig config;
    @Inject
    private PvPPlayerIndicatorsOverlay playerIndicatorsOverlay;
    @Inject
    private PvPPlayerIndicatorsTileOverlay playerIndicatorsTileOverlay;
    @Inject
    private PvPPlayerIndicatorsTrueTile playerIndicatorsTrueTile;
    @Inject
    private PvPPlayerIndicatorsHullOverlay playerIndicatorsHullOverlay;
    @Inject
    private PvPPlayerIndicatorsMinimapOverlay playerIndicatorsMinimapOverlay;
    @Inject
    private PvPTargetHighlightOverlay targetHighlightOverlay;
    @Inject
    private PvPPlayerIndicatorsService playerIndicatorsService;
    @Inject
    private Client client;
    @Inject
    private ChatIconManager chatIconManager;
    @Inject
    private ClientThread clientThread;
    private LocalPoint GELocation = null;

    @Provides
    PvPPlayerIndicatorsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PvPPlayerIndicatorsConfig.class);
    }

    protected void startUp() throws Exception {
        this.overlayManager.add(this.playerIndicatorsOverlay);
        this.overlayManager.add(this.playerIndicatorsTileOverlay);
        this.overlayManager.add(this.playerIndicatorsTrueTile);
        this.overlayManager.add(this.playerIndicatorsHullOverlay);
        this.overlayManager.add(this.playerIndicatorsMinimapOverlay);
    }

    protected void shutDown() throws Exception {
        this.overlayManager.remove(this.playerIndicatorsOverlay);
        this.overlayManager.remove(this.playerIndicatorsTileOverlay);
        this.overlayManager.remove(this.playerIndicatorsTrueTile);
        this.overlayManager.remove(this.playerIndicatorsHullOverlay);
        this.overlayManager.remove(this.playerIndicatorsMinimapOverlay);
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        if (event.getGameObject().getId() == 10063) {
            this.GELocation = event.getTile().getLocalLocation();
        }
    }

    @Subscribe
    private void onPlayerSpawned(PlayerSpawned event) {
        Player player;
        if (this.config.playerAlertSound() && !(player = event.getPlayer()).getName().equalsIgnoreCase(this.client.getLocalPlayer().getName()) && isAttackable((Client)this.client, (Player)player) && !player.isFriendsChatMember() && !player.isFriend() && isAttackable((Client)this.client, (Player)this.client.getLocalPlayer())) {
            LocalPoint playerloc;
            if (this.GELocation != null && (playerloc = this.client.getLocalPlayer().getLocalLocation()).distanceTo(this.GELocation) < 3000) {
                System.out.println(playerloc.distanceTo(this.GELocation));
                return;
            }
            this.client.playSoundEffect(3924, this.config.playerAlertSoundVolume());
        }
    }

    @Subscribe
    public void onClientTick(ClientTick clientTick) {
        if (this.client.isMenuOpen()) {
            return;
        }
        MenuEntry[] menuEntries = this.client.getMenuEntries();
        boolean modified = false;
        for (MenuEntry entry : menuEntries) {
            Decorations decorations;
            int type = entry.getType().getId();
            if (type >= 2000) {
                type -= 2000;
            }
            if (type != MenuAction.WALK.getId() && type != MenuAction.WIDGET_TARGET_ON_PLAYER.getId() && type != MenuAction.ITEM_USE_ON_PLAYER.getId() && type != MenuAction.PLAYER_FIRST_OPTION.getId() && type != MenuAction.PLAYER_SECOND_OPTION.getId() && type != MenuAction.PLAYER_THIRD_OPTION.getId() && type != MenuAction.PLAYER_FOURTH_OPTION.getId() && type != MenuAction.PLAYER_FIFTH_OPTION.getId() && type != MenuAction.PLAYER_SIXTH_OPTION.getId() && type != MenuAction.PLAYER_SEVENTH_OPTION.getId() && type != MenuAction.PLAYER_EIGTH_OPTION.getId() && type != MenuAction.RUNELITE_PLAYER.getId()) continue;
            Player[] players = this.client.getCachedPlayers();
            Player player = null;
            int identifier = entry.getIdentifier();
            if (type == MenuAction.WALK.getId()) {
                --identifier;
            }
            if (identifier >= 0 && identifier < players.length) {
                player = players[identifier];
            }
            if (player == null || (decorations = this.getDecorations(player)) == null) continue;
            String oldTarget = entry.getTarget();
            String newTarget = this.decorateTarget(oldTarget, decorations);
            entry.setTarget(newTarget);
            modified = true;
        }
        if (modified) {
            this.client.setMenuEntries(menuEntries);
        }
    }

    private Decorations getDecorations(Player player) {
        int image = -1;
        Color color = null;
        if (this.config.highlightFriends() && this.client.isFriended(player.getName(), false)) {
            color = this.config.getFriendColor();
        } else if (this.config.drawFriendsChatMemberNames() && player.isFriendsChatMember()) {
            color = this.config.getFriendsChatMemberColor();
            FriendsChatRank rank = this.playerIndicatorsService.getFriendsChatRank(player);
            if (rank != FriendsChatRank.UNRANKED) {
                image = this.chatIconManager.getIconNumber(rank);
            }
        } else if (this.config.highlightTeamMembers() && player.getTeam() > 0 && this.client.getLocalPlayer().getTeam() == player.getTeam()) {
            color = this.config.getTeamMemberColor();
        } else if (player.isClanMember() && this.config.highlightClanMembers()) {
            ClanTitle clanTitle;
            color = this.config.getClanMemberColor();
            if (this.config.showClanChatRanks() && (clanTitle = this.playerIndicatorsService.getClanTitle(player)) != null) {
                image = this.chatIconManager.getIconNumber(clanTitle);
            }
        } else if (this.config.highlightOthers() && !player.isFriendsChatMember() && !player.isClanMember()) {
            color = this.config.getOthersColor();
        } else if (isAttackable((Client)this.client, (Player)player) && !player.isFriendsChatMember() && !player.isFriend()) {
            color = this.config.getTargetColor();
        }
        if (image == -1 && color == null) {
            return null;
        }
        return new Decorations(image, color);
    }

    private String decorateTarget(String oldTarget, Decorations decorations) {
        String newTarget = oldTarget;
        if (decorations.getColor() != null && this.config.colorPlayerMenu()) {
            int idx = oldTarget.indexOf(62);
            if (idx != -1) {
                newTarget = oldTarget.substring(idx + 1);
            }
            newTarget = ColorUtil.prependColorTag(newTarget, decorations.getColor());
        }
        if (decorations.getImage() != -1 && this.config.showFriendsChatRanks()) {
            newTarget = "<img=" + decorations.getImage() + ">" + newTarget;
        }
        return newTarget;
    }

    private Player findPlayer(String name) {
        for (Player player : this.client.getPlayers()) {
            if (!player.getName().equals(name)) continue;
            return player;
        }
        return null;
    }

    private boolean isAttackable(Client client, Player player) {
        return true;
    }

    private static String combatAttackRange(final int combatLevel, final int wildernessLevel)
    {
        return Math.max(3, combatLevel - wildernessLevel) + "-" + Math.min(Experience.MAX_COMBAT_LEVEL, combatLevel + wildernessLevel);
    }

    private static final class Decorations {
        private final int image;
        private final Color color;

        public Decorations(int image, Color color) {
            this.image = image;
            this.color = color;
        }

        public int getImage() {
            return this.image;
        }

        public Color getColor() {
            return this.color;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Decorations)) {
                return false;
            }
            Decorations other = (Decorations)o;
            if (this.getImage() != other.getImage()) {
                return false;
            }
            Color this$color = this.getColor();
            Color other$color = other.getColor();
            return !(this$color == null ? other$color != null : !((Object)this$color).equals(other$color));
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            result = result * 59 + this.getImage();
            Color $color = this.getColor();
            result = result * 59 + ($color == null ? 43 : ((Object)$color).hashCode());
            return result;
        }

        public String toString() {
            return "PvPPlayerIndicatorsPlugin.Decorations(image=" + this.getImage() + ", color=" + this.getColor() + ")";
        }
    }
}

