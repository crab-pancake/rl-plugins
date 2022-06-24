/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.openosrs.client.util.PvPUtil
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Client
 *  net.runelite.api.FriendsChatManager
 *  net.runelite.api.FriendsChatMember
 *  net.runelite.api.FriendsChatRank
 *  net.runelite.api.Player
 *  net.runelite.api.clan.ClanChannel
 *  net.runelite.api.clan.ClanChannelMember
 *  net.runelite.api.clan.ClanRank
 *  net.runelite.api.clan.ClanSettings
 *  net.runelite.api.clan.ClanTitle
 */
package com.pvpplayerindicators;

import com.openosrs.client.util.PvPUtil;
import java.awt.Color;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.FriendsChatManager;
import net.runelite.api.FriendsChatMember;
import net.runelite.api.FriendsChatRank;
import net.runelite.api.Player;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.clan.ClanChannelMember;
import net.runelite.api.clan.ClanRank;
import net.runelite.api.clan.ClanSettings;
import net.runelite.api.clan.ClanTitle;
import com.pvpplayerindicators.PvPPlayerIndicatorsConfig;

@Singleton
public class PvPPlayerIndicatorsService {
    private final Client client;
    private final PvPPlayerIndicatorsConfig config;

    @Inject
    private PvPPlayerIndicatorsService(Client client, PvPPlayerIndicatorsConfig config) {
        this.config = config;
        this.client = client;
    }

    public void forEachPlayer(BiConsumer<Player, Color> consumer) {
        if (!(this.config.highlightOwnPlayer() || this.config.drawFriendsChatMemberNames() || this.config.highlightFriends() || this.config.highlightOthers() || this.config.highlightTargets() != PvPPlayerIndicatorsConfig.TargetHighlightMode.OFF || this.config.highlightClanMembers())) {
            return;
        }
        Player localPlayer = this.client.getLocalPlayer();
        for (Player player : this.client.getPlayers()) {
            if (player == null || player.getName() == null) continue;
            boolean isFriendsChatMember = player.isFriendsChatMember();
            boolean isClanMember = player.isClanMember();
            if (player == localPlayer) {
                if (!this.config.highlightOwnPlayer()) continue;
                consumer.accept(player, this.config.getOwnPlayerColor());
                continue;
            }
            if (this.config.highlightFriends() && this.client.isFriended(player.getName(), false)) {
                consumer.accept(player, this.config.getFriendColor());
                continue;
            }
            if (this.config.drawFriendsChatMemberNames() && isFriendsChatMember) {
                consumer.accept(player, this.config.getFriendsChatMemberColor());
                continue;
            }
            if (this.config.highlightTeamMembers() && localPlayer.getTeam() > 0 && localPlayer.getTeam() == player.getTeam()) {
                consumer.accept(player, this.config.getTeamMemberColor());
                continue;
            }
            if (this.config.highlightClanMembers() && isClanMember) {
                consumer.accept(player, this.config.getClanMemberColor());
                continue;
            }
            if (this.config.highlightOthers() && !isFriendsChatMember && !isClanMember) {
                consumer.accept(player, this.config.getOthersColor());
                continue;
            }
            if (this.config.highlightTargets() == PvPPlayerIndicatorsConfig.TargetHighlightMode.OFF || !PvPUtil.isAttackable((Client)this.client, (Player)player) || this.client.isFriended(player.getName(), false) || player.isFriendsChatMember()) continue;
            consumer.accept(player, this.config.getTargetColor());
        }
    }

    ClanTitle getClanTitle(Player player) {
        ClanChannel clanChannel = this.client.getClanChannel();
        ClanSettings clanSettings = this.client.getClanSettings();
        if (clanChannel == null || clanSettings == null) {
            return null;
        }
        ClanChannelMember member = clanChannel.findMember(player.getName());
        if (member == null) {
            return null;
        }
        ClanRank rank = member.getRank();
        return clanSettings.titleForRank(rank);
    }

    FriendsChatRank getFriendsChatRank(Player player) {
        FriendsChatManager friendsChatManager = this.client.getFriendsChatManager();
        if (friendsChatManager == null) {
            return FriendsChatRank.UNRANKED;
        }
        FriendsChatMember friendsChatMember = (FriendsChatMember)friendsChatManager.findByName(player.getName());
        return friendsChatMember != null ? friendsChatMember.getRank() : FriendsChatRank.UNRANKED;
    }
}

