/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Player
 *  net.runelite.api.Point
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.OverlayPriority
 *  net.runelite.client.ui.overlay.OverlayUtil
 */
package com.pvpplayerindicators;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Player;
import net.runelite.api.Point;
import com.pvpplayerindicators.PvPPlayerIndicatorsConfig;
import com.pvpplayerindicators.PvPPlayerIndicatorsService;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
public class PvPPlayerIndicatorsMinimapOverlay
extends Overlay {
    private final PvPPlayerIndicatorsService playerIndicatorsService;
    private final PvPPlayerIndicatorsConfig config;

    @Inject
    private PvPPlayerIndicatorsMinimapOverlay(PvPPlayerIndicatorsConfig config, PvPPlayerIndicatorsService playerIndicatorsService) {
        this.config = config;
        this.playerIndicatorsService = playerIndicatorsService;
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setPriority(OverlayPriority.HIGH);
    }

    public Dimension render(Graphics2D graphics) {
        this.playerIndicatorsService.forEachPlayer((player, color) -> this.renderPlayerOverlay(graphics, (Player)player, (Color)color));
        return null;
    }

    private void renderPlayerOverlay(Graphics2D graphics, Player actor, Color color) {
        Point minimapLocation;
        String name = actor.getName().replace('\u00a0', ' ');
        if (this.config.drawMinimapNames() && (minimapLocation = actor.getMinimapLocation()) != null) {
            OverlayUtil.renderTextLocation((Graphics2D)graphics, (Point)minimapLocation, (String)name, (Color)color);
        }
    }
}

