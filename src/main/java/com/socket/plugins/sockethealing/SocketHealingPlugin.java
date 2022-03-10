/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.inject.Provides
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.MenuEntry
 *  net.runelite.api.Skill
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.api.events.GameTick
 *  net.runelite.api.events.MenuEntryAdded
 *  net.runelite.client.callback.ClientThread
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.events.ConfigChanged
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDependency
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 *  net.runelite.client.util.ColorUtil
 *  net.runelite.client.util.Text
 */
package com.socket.plugins.sockethealing;

import com.google.inject.Provides;
import java.awt.Color;
import java.util.*;
import javax.inject.Inject;

import com.socket.SocketPlugin;
import com.socket.org.json.JSONObject;
import com.socket.packet.SocketBroadcastPacket;
import com.socket.packet.SocketPlayerLeave;
import com.socket.packet.SocketReceivePacket;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

@PluginDescriptor(name="Socket - Healing", description="Displays health overlays for socket party members. <br> Created by: A wild animal with a keyboard <br> Modified by: SpoonLite", enabledByDefault=false)
@PluginDependency(SocketPlugin.class)
public class SocketHealingPlugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private SocketHealingOverlay socketHealingOverlay;
    @Inject
    private SocketHealingConfig config;
    @Inject
    private SocketPlugin socketPlugin;
    @Inject
    private ClientThread clientThread;
    @Inject
    private EventBus eventBus;
    private Map<String, SocketHealingPlayer> partyMembers = new TreeMap();
    private int lastRefresh;
    public ArrayList<String> playerNames = new ArrayList();
    private boolean mirrorMode;

    @Provides
    SocketHealingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SocketHealingConfig.class);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void startUp() {
        this.overlayManager.add(this.socketHealingOverlay);
        this.lastRefresh = 0;
        Map<String, SocketHealingPlayer> map = this.partyMembers;
        synchronized(map) {
            this.partyMembers.clear();
        }
    }

    protected void shutDown() {
        this.overlayManager.remove(this.socketHealingOverlay);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged e) {
        if (!this.config.hpPlayerNames().equals("")) {
            this.playerNames.clear();
            String[] arrayOfString = this.config.hpPlayerNames().split(",");
            int i = arrayOfString.length;

            for(byte b = 0; b < i; ++b) {
                String str = arrayOfString[b];
                if (!"".equals(str = str.trim())) {
                    this.playerNames.add(str.toLowerCase());
                }
            }
        }

    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING) {
            Map<String, SocketHealingPlayer> map = this.partyMembers;
            synchronized(map) {
                this.partyMembers.clear();
            }
        }

    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (this.client.getGameState() == GameState.LOGGED_IN) {
            int currentHealth = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
            String name = this.client.getLocalPlayer().getName();
            Map<String, SocketHealingPlayer> map = this.partyMembers;
            SocketHealingPlayer playerHealth;
            synchronized(map) {
                playerHealth = this.partyMembers.get(name);
                if (playerHealth == null) {
                    playerHealth = new SocketHealingPlayer(name, currentHealth);
                    this.partyMembers.put(name, playerHealth);
                } else {
                    playerHealth.setHealth(currentHealth);
                }
            }

            ++this.lastRefresh;
            if (this.lastRefresh >= Math.max(1, this.config.refreshRate())) {
                JSONObject packet = new JSONObject();
                packet.put("name", name);
                packet.put("player-health", playerHealth.toJSON());
                this.eventBus.post(new SocketBroadcastPacket(packet));
                this.lastRefresh = 0;
            }
        }

    }

    @Subscribe
    public void onSocketReceivePacket(SocketReceivePacket event) {
        try {
            JSONObject payload = event.getPayload();
            String localName = this.client.getLocalPlayer().getName();
            if (payload.has("player-health")) {
                String targetName = payload.getString("name");
                if (targetName.equals(localName)) {
                    return;
                }

                JSONObject statusJSON = payload.getJSONObject("player-health");
                Map<String, SocketHealingPlayer> map = this.partyMembers;
                synchronized(map) {
                    SocketHealingPlayer playerHealth = this.partyMembers.get(targetName);
                    if (playerHealth == null) {
                        playerHealth = SocketHealingPlayer.fromJSON(statusJSON);
                        this.partyMembers.put(targetName, playerHealth);
                    } else {
                        playerHealth.parseJSON(statusJSON);
                    }
                }
            }
        } catch (Exception var11) {
            var11.printStackTrace();
        }

    }

    @Subscribe
    public void onSocketPlayerLeave(SocketPlayerLeave event) {
        String target = event.getPlayerName();
        Map<String, SocketHealingPlayer> map = this.partyMembers;
        synchronized(map) {
            this.partyMembers.remove(target);
        }
    }

    public Map<String, SocketHealingPlayer> getPartyMembers() {
        return this.partyMembers;
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (this.config.hpMenu()) {
            int type = event.getType();
            if (type >= 2000) {
                type -= 2000;
            }

            Color color = Color.GREEN;
            String target = event.getTarget().replaceAll("[^A-Za-z0-9-()<>=]", " ");

            for (String playerName : this.getPartyMembers().keySet()) {
                if (Text.removeTags(target).toLowerCase().contains(playerName.toLowerCase() + "  (level-")) {
                    SocketHealingPlayer player = this.getPartyMembers().get(playerName);
                    MenuEntry[] menuEntries = this.client.getMenuEntries();
                    MenuEntry menuEntry = menuEntries[menuEntries.length - 1];
                    int playerHealth = player.getHealth();
                    if (playerHealth > this.config.greenZone()) {
                        color = this.config.greenZoneColor();
                    }

                    if (playerHealth <= this.config.greenZone() && playerHealth > this.config.orangeZone()) {
                        color = this.config.orangeZoneColor();
                    }

                    if (playerHealth <= this.config.orangeZone()) {
                        color = this.config.redZoneColor();
                    }

                    String hpAdded = ColorUtil.prependColorTag(" - " + playerHealth, color);
                    menuEntry.setTarget(event.getTarget() + hpAdded);
                    this.client.setMenuEntries(menuEntries);
                }
            }
        }

    }
}
