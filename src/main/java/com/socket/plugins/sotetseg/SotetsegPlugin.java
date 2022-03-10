/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.inject.Provides
 *  javax.inject.Inject
 *  net.runelite.api.ChatMessageType
 *  net.runelite.api.Client
 *  net.runelite.api.GroundObject
 *  net.runelite.api.NPC
 *  net.runelite.api.Player
 *  net.runelite.api.Point
 *  net.runelite.api.Tile
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.GameTick
 *  net.runelite.api.events.GroundObjectSpawned
 *  net.runelite.api.events.NpcDespawned
 *  net.runelite.api.events.NpcSpawned
 *  net.runelite.api.events.ProjectileMoved
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.socket.plugins.sotetseg;

import com.google.inject.Provides;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import com.socket.org.json.JSONArray;
import com.socket.org.json.JSONObject;
import com.socket.packet.SocketBroadcastPacket;
import com.socket.packet.SocketReceivePacket;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(name="Socket - Sotetseg", description="Extended plugin handler for Sotetseg in the Theatre of Blood.", tags={"net/runelite/client/plugins/socket", "server", "discord", "connection", "broadcast", "sotetseg", "theatre", "tob"}, enabledByDefault=true)
public class SotetsegPlugin
extends Plugin {
    private static final Logger log = LoggerFactory.getLogger(SotetsegPlugin.class);
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private EventBus eventBus;
    @Inject
    private SotetsegConfig config;
    @Inject
    private SotetsegOverlay overlay;
    @Inject
    private MazeTrueTileOverlay mazeOverlay;
    private boolean sotetsegActive;
    private NPC sotetsegNPC;
    public LinkedHashSet<Point> redTiles;
    public Set<WorldPoint> mazePings;
    private int dispatchCount;
    private boolean wasInUnderworld;
    private int overworldRegionID;
    private int underworldRegionID;
    private int ballTick = 0;
    private boolean mirrorMode;

    @Provides
    SotetsegConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(SotetsegConfig.class);
    }

    protected void startUp() {
        this.sotetsegActive = false;
        this.sotetsegNPC = null;
        this.redTiles = new LinkedHashSet();
        this.mazePings = Collections.synchronizedSet(new HashSet());
        this.dispatchCount = 5;
        this.wasInUnderworld = false;
        this.overworldRegionID = -1;
        this.underworldRegionID = -1;
        this.overlayManager.add(this.overlay);
        this.overlayManager.add(this.mazeOverlay);
    }

    protected void shutDown() {
        this.overlayManager.remove(this.overlay);
        this.overlayManager.remove(this.mazeOverlay);
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved event) {
        if (event.getProjectile().getId() == 1604 && event.getProjectile().getEndCycle() - event.getProjectile().getStartCycle() == event.getProjectile().getRemainingCycles()) {
            JSONObject data = new JSONObject();
            data.put("sotetseg-extended-ball", "");
            this.eventBus.post(new SocketBroadcastPacket(data));
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        NPC npc = event.getNpc();
        switch (npc.getId()) {
            case 8387: 
            case 8388: 
            case 10864: 
            case 10865: 
            case 10867: 
            case 10868: {
                this.sotetsegActive = true;
                this.sotetsegNPC = npc;
            }
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        NPC npc = event.getNpc();
        switch (npc.getId()) {
            case 8387: 
            case 8388: 
            case 10864: 
            case 10865: 
            case 10867: 
            case 10868: {
                if (this.client.getPlane() == 3) break;
                this.sotetsegActive = false;
                this.sotetsegNPC = null;
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (this.sotetsegActive) {
            Player player = this.client.getLocalPlayer();
            if (this.sotetsegNPC != null && (this.sotetsegNPC.getId() == 8388 || this.sotetsegNPC.getId() == 10868 || this.sotetsegNPC.getId() == 10865)) {
                this.redTiles.clear();
                this.mazePings.clear();
                this.dispatchCount = 5;
                if (this.isInOverWorld()) {
                    this.wasInUnderworld = false;
                    if (player != null && player.getWorldLocation() != null) {
                        WorldPoint wp = player.getWorldLocation();
                        this.overworldRegionID = wp.getRegionID();
                    }
                }
            }
            if (!this.redTiles.isEmpty() && this.wasInUnderworld && this.dispatchCount > 0) {
                this.underworldRegionID = player.getWorldLocation().getRegionID();
                --this.dispatchCount;
                JSONArray data = new JSONArray();
                JSONArray dataUnder = new JSONArray();
                for (Point p : this.redTiles) {
                    WorldPoint wp = this.translateMazePoint(p);
                    JSONObject jsonwp = new JSONObject();
                    jsonwp.put("x", wp.getX());
                    jsonwp.put("y", wp.getY());
                    jsonwp.put("plane", wp.getPlane());
                    data.put(jsonwp);
                    JSONObject jsonunder = new JSONObject();
                    WorldPoint wp2 = this.translateUnderWorldPoint(p);
                    jsonunder.put("x", wp2.getX());
                    jsonunder.put("y", wp2.getY());
                    jsonunder.put("plane", wp2.getPlane());
                    dataUnder.put(jsonunder);
                }
                JSONObject payload = new JSONObject();
                payload.put("sotetseg-extended", data);
                JSONObject payloadUnder = new JSONObject();
                payloadUnder.put("sotetseg-extended", dataUnder);
                this.eventBus.post(new SocketBroadcastPacket(payload));
                this.eventBus.post(new SocketBroadcastPacket(payloadUnder));
            }
        }
    }

    @Subscribe
    public void onSocketReceivePacket(SocketReceivePacket event) {
        try {
            JSONObject payload = event.getPayload();
            if (!payload.has("sotetseg-extended") && !payload.has("sotetseg-extended-ball")) {
                return;
            }
            if (payload.has("sotetseg-extended-ball")) {
                if (this.client.getLocalPlayer().getWorldLocation().getPlane() == 3 && this.ballTick != this.client.getTickCount()) {
                    this.ballTick = this.client.getTickCount();
                    if (this.config.warnBall()) {
                        this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ff0000>Ball thrown while in underworld", "");
                    }
                }
                return;
            }
            JSONArray data = payload.getJSONArray("sotetseg-extended");
            for (int i = 0; i < data.length(); ++i) {
                JSONObject jsonwp = data.getJSONObject(i);
                int x = jsonwp.getInt("x");
                int y = jsonwp.getInt("y");
                int plane = jsonwp.getInt("plane");
                WorldPoint wp = new WorldPoint(x, y, plane);
                this.mazePings.add(wp);
            }
        }
        catch (Exception var10) {
            var10.printStackTrace();
        }
    }

    @Subscribe
    public void onGroundObjectSpawned(GroundObjectSpawned event) {
        GroundObject o;
        if (this.sotetsegActive && ((o = event.getGroundObject()).getId() > 41749 && o.getId() < 41754 || o.getId() == 33035)) {
            Tile t = event.getTile();
            WorldPoint p = WorldPoint.fromLocal(this.client, t.getLocalLocation());
            Point point = new Point(p.getRegionX(), p.getRegionY());
            if (this.isInOverWorld()) {
                this.redTiles.add(new Point(point.getX() - 9, point.getY() - 22));
            }
            if (this.isInUnderWorld()) {
                this.redTiles.add(new Point(point.getX() - 42, point.getY() - 31));
                this.wasInUnderworld = true;
            }
        }
    }

    private boolean isInOverWorld() {
        return this.client.getMapRegions().length > 0 && this.client.getMapRegions()[0] == 13123;
    }

    private boolean isInUnderWorld() {
        return this.client.getMapRegions().length > 0 && this.client.getMapRegions()[0] == 13379;
    }

    private WorldPoint translateMazePoint(Point mazePoint) {
        Player p = this.client.getLocalPlayer();
        if (this.overworldRegionID == -1 && p != null) {
            WorldPoint wp = p.getWorldLocation();
            return WorldPoint.fromRegion(wp.getRegionID(), mazePoint.getX() + 9, mazePoint.getY() + 22, 0);
        }
        return WorldPoint.fromRegion(this.overworldRegionID, mazePoint.getX() + 9, mazePoint.getY() + 22, 0);
    }

    private WorldPoint translateUnderWorldPoint(Point mazePoint) {
        return WorldPoint.fromRegion(this.underworldRegionID, mazePoint.getX() + 42, mazePoint.getY() + 31, 3);
    }

    public boolean isSotetsegActive() {
        return this.sotetsegActive;
    }

    public Set<WorldPoint> getMazePings() {
        return this.mazePings;
    }
}

