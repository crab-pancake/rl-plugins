// 
// Decompiled by Procyon v0.5.36
// 

package net.runelite.client.plugins.gwdessencehider;

import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.api.events.WidgetLoaded;
import java.util.Objects;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.callback.ClientThread;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.Plugin;

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
        this.armaKc = 0;
        this.bandosKc = 0;
        this.saraKc = 0;
        this.zammyKc = 0;
        this.nexKc = 0;
    }
    
    @Provides
    GwdEssenceHiderConfig getConfig(final ConfigManager configManager) {
        return (GwdEssenceHiderConfig)configManager.getConfig((Class)GwdEssenceHiderConfig.class);
    }
    
    protected void startUp() throws Exception {
        this.gwdWidget = (this.client.getWidget(26607621) != null);
        this.overlayManager.add((Overlay)this.overlay);
    }
    
    protected void shutDown() throws Exception {
        this.gwdWidget = false;
        this.overlayManager.remove((Overlay)this.overlay);
        if (this.client.getWidget(26607621) != null) {
            Objects.requireNonNull(this.client.getWidget(26607621)).setHidden(false);
        }
    }
    
    public void reset() {
        this.armaKc = 0;
        this.bandosKc = 0;
        this.saraKc = 0;
        this.zammyKc = 0;
        this.nexKc = 0;
    }
    
    @Subscribe
    public void onWidgetLoaded(final WidgetLoaded event) {
        if (event.getGroupId() == 406 && !this.gwdWidget) {
            this.gwdWidget = (this.client.getWidget(26607621) != null);
            this.setKc();
        }
    }
    
    @Subscribe
    public void onGameTick(final GameTick event) {
        if (this.gwdWidget) {
            final Widget widget = this.client.getWidget(26607621);
            if (widget != null) {
                if (!widget.isHidden()) {
                    widget.setHidden(true);
                    this.setKc();
                }
            }
            else {
                this.gwdWidget = false;
                this.reset();
            }
        }
    }
    
    @Subscribe
    public void onVarbitChanged(final VarbitChanged event) {
        if (this.gwdWidget) {
            this.setKc();
        }
    }
    
    public void setKc() {
        this.armaKc = this.client.getVarbitValue(3973);
        this.bandosKc = this.client.getVarbitValue(3975);
        this.saraKc = this.client.getVarbitValue(3972);
        this.zammyKc = this.client.getVarbitValue(3976);
    }
}
