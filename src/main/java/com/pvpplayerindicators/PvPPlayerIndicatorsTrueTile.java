/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.Perspective
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.coords.WorldPoint
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
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import com.pvpplayerindicators.PvPPlayerIndicatorsConfig;
import com.pvpplayerindicators.PvPPlayerIndicatorsService;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class PvPPlayerIndicatorsTrueTile
extends Overlay {
    private final PvPPlayerIndicatorsService playerIndicatorsService;
    private final PvPPlayerIndicatorsConfig config;
    private final Client client;

    @Inject
    private PvPPlayerIndicatorsTrueTile(PvPPlayerIndicatorsConfig config, PvPPlayerIndicatorsService playerIndicatorsService, Client client) {
        this.config = config;
        this.playerIndicatorsService = playerIndicatorsService;
        this.client = client;
        this.setLayer(OverlayLayer.ABOVE_SCENE);
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setPriority(OverlayPriority.MED);
    }

    public Dimension render(Graphics2D graphics) {
        if (!this.config.drawTL()) {
            return null;
        }
        this.playerIndicatorsService.forEachPlayer((player, color) -> {
            LocalPoint playerPosLocal;
            WorldPoint playerPos = player.getWorldLocation();
            if (playerPos != null && (playerPosLocal = LocalPoint.fromWorld((Client)this.client, (WorldPoint)playerPos)) != null) {
                this.renderTile(graphics, playerPosLocal, (Color)color);
            }
        });
        return null;
    }

    private void renderTile(Graphics2D graphics, LocalPoint dest, Color color) {
        if (dest == null) {
            return;
        }
        Polygon poly = Perspective.getCanvasTilePoly((Client)this.client, (LocalPoint)dest);
        if (poly == null) {
            return;
        }
        PvPPlayerIndicatorsTrueTile.renderPolygon(graphics, poly, color);
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

