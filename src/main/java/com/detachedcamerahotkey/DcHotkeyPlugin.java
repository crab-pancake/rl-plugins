/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.inject.Provides
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.events.ConfigChanged
 *  net.runelite.client.input.KeyListener
 *  net.runelite.client.input.KeyManager
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.util.HotkeyListener
 *  org.pf4j.Extension
 */
package com.detachedcamerahotkey;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;

@PluginDescriptor(name="[S] Detached Camera Hotkey",
        description="Hotkey to enable/disable the detached camera. Not made by me",
        tags={"hotkey", "detached", "camera"},
        enabledByDefault=false)
public class DcHotkeyPlugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    private KeyManager keyManager;
    @Inject
    private DcHotkeyConfig config;
    private boolean toggled = false;
    private final HotkeyListener masterSwitch = new HotkeyListener(() -> this.config.getDCHotkey()){

        public void hotkeyPressed() {
            DcHotkeyPlugin.this.toggled = !DcHotkeyPlugin.this.toggled;
            DcHotkeyPlugin.this.client.setOculusOrbState(DcHotkeyPlugin.this.toggled ? 1 : 0);
            if (DcHotkeyPlugin.this.toggled) {
                DcHotkeyPlugin.this.client.setOculusOrbNormalSpeed(DcHotkeyPlugin.this.config.getDCSpeed());
            }
        }
    };

    @Provides
    DcHotkeyConfig provideConfig(ConfigManager cm) {
        return cm.getConfig(DcHotkeyConfig.class);
    }

    protected void startUp() {
        this.keyManager.registerKeyListener(this.masterSwitch);
    }

    protected void shutDown() {
        this.keyManager.unregisterKeyListener(this.masterSwitch);
        this.toggled = false;
        this.client.setOculusOrbState(0);
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged e) {
        if (e.getGroup().equals("detachedcamerahotkey") && e.getKey().equals("dcSpeed") && this.toggled) {
            this.client.setOculusOrbNormalSpeed(this.config.getDCSpeed());
        }
    }
}

