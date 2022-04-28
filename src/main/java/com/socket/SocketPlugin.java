/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.inject.Provides
 *  javax.inject.Inject
 *  net.runelite.api.ChatMessageType
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.api.events.GameTick
 *  net.runelite.client.callback.ClientThread
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.events.ConfigChanged
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.socket;

import com.google.inject.Provides;
import com.socket.hash.AES256;
import com.socket.org.json.JSONObject;
import com.socket.packet.*;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.PrintWriter;

@PluginDescriptor(name="Socket", description="Socket connection for broadcasting messages across clients.", tags={"src/socket/socket", "server", "discord", "connection", "broadcast"}, enabledByDefault=false)
public class SocketPlugin
extends Plugin {
    private static final Logger log = LoggerFactory.getLogger(SocketPlugin.class);
    public static final String CONFIG_VERSION = "Socket Plugin v2.1.0";
    public static final String PASSWORD_SALT = "$P@_/gKR`y:mv)6K";
    @Inject
    private Client client;
    @Inject
    private EventBus eventBus;
    @Inject
    private ClientThread clientThread;
    @Inject
    private SocketConfig config;
    private long nextConnection;
    private SocketConnection connection = null;

    @Provides
    SocketConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(SocketConfig.class);
    }

    protected void startUp() {
        this.nextConnection = 0L;
        this.eventBus.register(SReceive.class);
        this.eventBus.register(SSend.class);
        this.eventBus.register(SJoin.class);
        this.eventBus.register(SLeave.class);
        this.eventBus.register(SStartup.class);
        this.eventBus.register(SShutdown.class);
        this.eventBus.post(new SStartup());
    }

    protected void shutDown() {
        this.eventBus.post(new SShutdown());
        this.eventBus.unregister(SReceive.class);
        this.eventBus.unregister(SSend.class);
        this.eventBus.unregister(SJoin.class);
        this.eventBus.unregister(SLeave.class);
        this.eventBus.unregister(SStartup.class);
        this.eventBus.unregister(SShutdown.class);
        if (this.connection != null) {
            this.connection.terminate(true);
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (this.client.getGameState() == GameState.LOGGED_IN) {
            SocketState state;
            if (this.connection != null && ((state = this.connection.getState()) == SocketState.CONNECTING || state == SocketState.CONNECTED)) {
                return;
            }
            if (System.currentTimeMillis() >= this.nextConnection) {
                this.nextConnection = System.currentTimeMillis() + 30000L;
                this.connection = new SocketConnection(this, this.client.getLocalPlayer().getName());
                new Thread(this.connection).start();
            }
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (this.config.disableChatMessages()) {
            return;
        }
        if (event.getGroup().equals(CONFIG_VERSION)) {
            this.clientThread.invoke(() -> this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=b4281e>Configuration changed. Please restart the plugin to see updates.", null));
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGIN_SCREEN && this.connection != null) {
            this.connection.terminate(false);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Subscribe
    public void onSSend(SSend packet) {
        try {
            PrintWriter outputStream;
            if (this.connection == null || this.connection.getState() != SocketState.CONNECTED) {
                return;
            }
            String data = packet.getPayload().toString();
            log.debug("Deploying packet from client: {}", data);
            String secret = this.config.getPassword() + PASSWORD_SALT;
            JSONObject payload = new JSONObject();
            payload.put("header", "BROADCAST");
            payload.put("payload", AES256.encrypt(secret, data));
            PrintWriter printWriter = outputStream = this.connection.getOutputStream();
            synchronized (printWriter) {
                outputStream.println(payload);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error("An error has occurred while trying to broadcast a packet.", e);
        }
    }

    public Client getClient() {
        return this.client;
    }

    public EventBus getEventBus() {
        return this.eventBus;
    }

    public ClientThread getClientThread() {
        return this.clientThread;
    }

    public SocketConfig getConfig() {
        return this.config;
    }

    public long getNextConnection() {
        return this.nextConnection;
    }

    public void setNextConnection(long nextConnection) {
        this.nextConnection = nextConnection;
    }
}

