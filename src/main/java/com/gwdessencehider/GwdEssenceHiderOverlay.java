// 
// Decompiled by Procyon v0.5.36
// 

package com.gwdessencehider;

import java.awt.Color;
import net.runelite.client.ui.overlay.components.LineComponent;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;

class GwdEssenceHiderOverlay extends OverlayPanel
{
    private final Client client;
    private final GwdEssenceHiderPlugin plugin;
    private final GwdEssenceHiderConfig config;
    
    @Inject
    private GwdEssenceHiderOverlay(final Client client, final GwdEssenceHiderPlugin plugin, final GwdEssenceHiderConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.setPosition(OverlayPosition.TOP_LEFT);
        this.setLayer(OverlayLayer.ABOVE_SCENE);
        this.setPriority(OverlayPriority.MED);
    }
    
    public Dimension render(final Graphics2D graphics) {
        if (this.plugin.gwdWidget) {
            this.panelComponent.getChildren().clear();
            if (this.plugin.armaKc > 0) {
                this.panelComponent.getChildren().add(LineComponent.builder().leftColor(this.config.textColor() ? this.config.armaColor() : this.config.defaultColor()).left((this.config.godMode() == GwdEssenceHiderConfig.GodMode.FULL_NAME) ? "Armadyl: " : "Arma: ").rightColor(this.config.textColor() ? this.config.armaColor() : this.config.defaultColor()).right(String.valueOf(this.plugin.armaKc)).build());
            }
            if (this.plugin.bandosKc > 0) {
                this.panelComponent.getChildren().add(LineComponent.builder().leftColor(this.config.textColor() ? this.config.bandosColor() : this.config.defaultColor()).left("Bandos: ").rightColor(this.config.textColor() ? this.config.bandosColor() : this.config.defaultColor()).right(String.valueOf(this.plugin.bandosKc)).build());
            }
            if (this.plugin.saraKc > 0) {
                this.panelComponent.getChildren().add(LineComponent.builder().leftColor(this.config.textColor() ? this.config.saraColor() : this.config.defaultColor()).left((this.config.godMode() == GwdEssenceHiderConfig.GodMode.FULL_NAME) ? "Saradomin: " : "Sara: ").rightColor(this.config.textColor() ? this.config.saraColor() : this.config.defaultColor()).right(String.valueOf(this.plugin.saraKc)).build());
            }
            if (this.plugin.zammyKc > 0) {
                this.panelComponent.getChildren().add(LineComponent.builder().leftColor(this.config.textColor() ? this.config.zammyColor() : this.config.defaultColor()).left((this.config.godMode() == GwdEssenceHiderConfig.GodMode.FULL_NAME) ? "Zamorak: " : "Zammy: ").rightColor(this.config.textColor() ? this.config.zammyColor() : this.config.defaultColor()).right(String.valueOf(this.plugin.zammyKc)).build());
            }
            if (this.plugin.nexKc > 0) {
                this.panelComponent.getChildren().add(LineComponent.builder().leftColor(Color.MAGENTA).left("Nex: ").rightColor(Color.MAGENTA).right(String.valueOf(this.plugin.nexKc)).build());
            }
            this.panelComponent.setPreferredSize(new Dimension(graphics.getFontMetrics().stringWidth("Saradomin:   ") + 40, 0));
            return super.render(graphics);
        }
        return null;
    }
}
