/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.OverlayPriority
 */
package com.pvpplayerindicators;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import javax.inject.Inject;
import com.pvpplayerindicators.PvPPlayerIndicatorsConfig;
import com.pvpplayerindicators.PvPPlayerIndicatorsService;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class PvPPlayerIndicatorsTileOverlay
extends Overlay {
    private final PvPPlayerIndicatorsService playerIndicatorsService;
    private final PvPPlayerIndicatorsConfig config;

    @Inject
    private PvPPlayerIndicatorsTileOverlay(PvPPlayerIndicatorsConfig config, PvPPlayerIndicatorsService playerIndicatorsService) {
        this.config = config;
        this.playerIndicatorsService = playerIndicatorsService;
        this.setLayer(OverlayLayer.ABOVE_SCENE);
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setPriority(OverlayPriority.MED);
    }

    public Dimension render(Graphics2D graphics) {
        if (!this.config.drawTiles()) {
            return null;
        }
        this.playerIndicatorsService.forEachPlayer((player, color) -> {
            Polygon poly = player.getCanvasTilePoly();
            if (poly != null) {
                PvPPlayerIndicatorsTileOverlay.renderPolygon(graphics, poly, color);
            }
        });
        return null;
    }

    public static void renderPolygon(Graphics2D graphics, Shape poly, Color color) {
        graphics.setColor(color);
        Stroke originalStroke = graphics.getStroke();
        graphics.setStroke(new BasicStroke(2.0f));
        graphics.draw(poly);
        graphics.setColor(new Color(0, 0, 0, 0));
        graphics.fill(poly);
        graphics.setStroke(originalStroke);
    }
}

