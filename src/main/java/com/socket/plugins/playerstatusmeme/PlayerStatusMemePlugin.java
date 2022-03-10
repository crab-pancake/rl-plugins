/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.events.ClientTick
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package com.socket.plugins.playerstatusmeme;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ClientTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(name="Rat Jam", description="Ask Boris to explain why", tags={"spoon", "boris"}, enabledByDefault=false)
public class PlayerStatusMemePlugin
extends Plugin {
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private PlayerStatusMemeOverlay overlay;
    public int ratJamFrame = 1;

    protected void startUp() {
        overlayManager.add(overlay);
    }

    protected void shutDown() {
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        if (client.getGameState() == GameState.LOGGED_IN) {
            ++ratJamFrame;
            if (ratJamFrame >= 35) {
                ratJamFrame = 1;
            }
        }
    }
}

