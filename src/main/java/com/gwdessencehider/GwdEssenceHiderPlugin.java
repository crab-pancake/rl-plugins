// 
// Decompiled by Procyon v0.5.36
// 

package com.gwdessencehider;

import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.api.events.WidgetLoaded;
import java.util.Objects;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.callback.ClientThread;
import javax.inject.Inject;
import net.runelite.api.Client;

@PluginDescriptor(name = "[S] Gwd Essence", description = "Removes the new essence counter and replaces it with a better one", tags = { "combat", "spoon", "pve", "pvm", "bosses", "gwd" })
public class GwdEssenceHiderPlugin extends Plugin
{
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private GwdEssenceHiderOverlay overlay;
    public boolean gwdWidget;
    public int armaKc;
    public int bandosKc;
    public int saraKc;
    public int zammyKc;
    public int nexKc;
    
    public GwdEssenceHiderPlugin() {
        armaKc = 0;
        bandosKc = 0;
        saraKc = 0;
        zammyKc = 0;
        nexKc = 0;
    }
    
    @Provides
    GwdEssenceHiderConfig getConfig(final ConfigManager configManager) {
        return configManager.getConfig(GwdEssenceHiderConfig.class);
    }
    
    protected void startUp() throws Exception {
        gwdWidget = (client.getWidget(26607621) != null);
        overlayManager.add(overlay);
    }
    
    protected void shutDown() throws Exception {
        gwdWidget = false;
        overlayManager.remove(overlay);
        if (client.getWidget(26607621) != null) {
            Objects.requireNonNull(client.getWidget(26607621)).setHidden(false);
        }
    }
    
    public void reset() {
        armaKc = 0;
        bandosKc = 0;
        saraKc = 0;
        zammyKc = 0;
        nexKc = 0;
    }
    
    @Subscribe
    public void onWidgetLoaded(final WidgetLoaded event) {
        if (event.getGroupId() == 406 && !gwdWidget) {
            gwdWidget = (client.getWidget(26607621) != null);
            setKc();
        }
    }
    
    @Subscribe
    public void onGameTick(final GameTick event) {
        if (gwdWidget) {
            final Widget widget = client.getWidget(26607621);
            if (widget != null) {
                if (!widget.isHidden()) {
                    widget.setHidden(true);
                    setKc();
                }
            }
            else {
                gwdWidget = false;
                reset();
            }
        }
    }
    
    @Subscribe
    public void onVarbitChanged(final VarbitChanged event) {
        if (gwdWidget) {
            setKc();
        }
    }
    
    public void setKc() {
        armaKc = client.getVarbitValue(3973);
        bandosKc = client.getVarbitValue(3975);
        saraKc = client.getVarbitValue(3972);
        zammyKc = client.getVarbitValue(3976);
        nexKc = client.getVarbitValue(13080);
    }
}
