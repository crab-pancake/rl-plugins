/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.inject.Provides
 *  javax.inject.Inject
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package com.dplayerindicators;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.playerindicators.PlayerIndicatorsMinimapOverlay;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(name="[D] Player Indicators", description="")
public class DPlayerIndicatorsPlugin extends Plugin {
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private DPlayerIndicatorsOverlay overlay;
    @Inject
    private PlayerIndicatorsMinimapOverlay minimapOverlay;

    protected void startUp() throws Exception {
        this.overlayManager.add(this.overlay);
        this.overlayManager.add(this.minimapOverlay);
    }

    protected void shutDown() throws Exception {
        this.overlayManager.remove(this.overlay);
        this.overlayManager.remove(this.minimapOverlay);
    }

    @Provides
    DPlayerIndicatorsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DPlayerIndicatorsConfig.class);
    }
}

