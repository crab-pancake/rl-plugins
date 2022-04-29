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
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import static java.lang.Math.max;
import static java.lang.Math.min;

@PluginDescriptor(name="Six hour", description="6 hr log out")
public class NeverLog extends Plugin {
    @Inject
    private Client client;
    private static final Random random = new Random();
    private long randomDelay;

    protected void startUp() {
        randomDelay = randomDelay();
//        System.out.println("randomDelay: "+randomDelay);
    }

    protected void shutDown() {
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (checkIdleLogout()) {
//            System.out.println("onGameTick "+LocalTime.now());
//            System.out.println("idle ticks: "+min(client.getKeyboardIdleTicks(), client.getMouseIdleTicks()));
            randomDelay = randomDelay();
//            System.out.println("randomDelay: "+randomDelay);
            Executors.newSingleThreadExecutor().submit(this::pressKey);
        }
    }

    private boolean checkIdleLogout() {
        int idleClientTicks = min(client.getKeyboardIdleTicks(), client.getMouseIdleTicks());
        return (long)idleClientTicks >= randomDelay;
    }

    private static long randomDelay() {
        return (long) clamp(Math.round(random.nextGaussian() * 824) + 6731, 2713, 14728);
    }

    private static double clamp(double val, int min, int max) {
        return max(min, Math.min(max, val));
    }

    private void pressKey() {
        KeyEvent keyPress = new KeyEvent(client.getCanvas(), 401, System.currentTimeMillis(), 0, 8);
        client.getCanvas().dispatchEvent(keyPress);
//        System.out.println("pressed "+LocalTime.now());
        double sleeptime = clamp((random.nextGaussian() * 48)+152,12,323);
        try{
        Thread.sleep((long) sleeptime);}
        catch (InterruptedException ignored) {
        }

        KeyEvent keyRelease = new KeyEvent(client.getCanvas(), 402, System.currentTimeMillis(), 0, 8);
        client.getCanvas().dispatchEvent(keyRelease);
//        System.out.println("released "+LocalTime.now());
        KeyEvent keyTyped = new KeyEvent(client.getCanvas(), 400, System.currentTimeMillis(), 0, 8);
        client.getCanvas().dispatchEvent(keyTyped);
//        System.out.println("typed "+LocalTime.now());
    }
}