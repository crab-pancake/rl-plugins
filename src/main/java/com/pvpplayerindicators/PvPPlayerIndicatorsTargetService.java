/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.openosrs.client.util.PvPUtil
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Client
 *  net.runelite.api.Player
 */
package com.pvpplayerindicators;

import com.openosrs.client.util.PvPUtil;
import java.awt.Color;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Player;
import com.pvpplayerindicators.PvPPlayerIndicatorsConfig;
import com.pvpplayerindicators.PvPPlayerIndicatorsPlugin;

@Singleton
public class PvPPlayerIndicatorsTargetService {
    private final Client client;
    private final PvPPlayerIndicatorsConfig config;
    private final PvPPlayerIndicatorsPlugin plugin;

    @Inject
    private PvPPlayerIndicatorsTargetService(Client client, PvPPlayerIndicatorsPlugin plugin, PvPPlayerIndicatorsConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    public void forEachPlayer(BiConsumer<Player, Color> consumer) {
        if (this.config.highlightTargets() == PvPPlayerIndicatorsConfig.TargetHighlightMode.OFF) {
            return;
        }
        Player localPlayer = this.client.getLocalPlayer();
        for (Player player : this.client.getPlayers()) {
            if (player == null || player.getName() == null || !PvPUtil.isAttackable((Client)this.client, (Player)player) || this.client.isFriended(player.getName(), false) || player.isFriendsChatMember() || player.getName().equals(localPlayer.getName())) continue;
            consumer.accept(player, this.config.getTargetColor());
        }
    }
}

