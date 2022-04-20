/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.events.GameTick
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 */
package com.neverlog;

import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import static java.lang.Math.max;

@PluginDescriptor(name="Six hour", description="6 hr log out")
public class NeverLog extends Plugin {
    @Inject
    private Client client;
    private final Random random = new Random();
    private long randomDelay;

    protected void startUp() {
        randomDelay = randomDelay();
    }

    protected void shutDown() {
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (checkIdleLogout()) {
            randomDelay = randomDelay();
            Executors.newSingleThreadExecutor().submit(this::pressKey);
        }
    }

    private boolean checkIdleLogout() {
        int idleClientTicks = max(client.getKeyboardIdleTicks(), client.getMouseIdleTicks());
        return (long)idleClientTicks >= randomDelay;
    }

    private long randomDelay() {
        return (long)NeverLog.clamp(Math.round(random.nextGaussian() * 8400.0));
    }

    private static double clamp(double val) {
        return max(1.0, Math.min(13000.0, val));
    }

    private void pressKey() {
        KeyEvent keyPress = new KeyEvent(client.getCanvas(), 401, System.currentTimeMillis(), 0, 8);
        client.getCanvas().dispatchEvent(keyPress);
        KeyEvent keyRelease = new KeyEvent(client.getCanvas(), 402, System.currentTimeMillis(), 0, 8);
        client.getCanvas().dispatchEvent(keyRelease);
        KeyEvent keyTyped = new KeyEvent(client.getCanvas(), 400, System.currentTimeMillis(), 0, 8);
        client.getCanvas().dispatchEvent(keyTyped);
    }
}

