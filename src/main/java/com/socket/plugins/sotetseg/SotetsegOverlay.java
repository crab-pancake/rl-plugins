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
package com.socket.plugins.sotetseg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class SotetsegOverlay
extends Overlay {
    private final Client client;
    private final SotetsegPlugin plugin;
    private final SotetsegConfig config;

    @Inject
    private SotetsegOverlay(Client client, SotetsegPlugin plugin, SotetsegConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setPriority(OverlayPriority.HIGH);
        this.setLayer(OverlayLayer.ABOVE_SCENE);
    }

    public Dimension render(Graphics2D graphics) {
        if (plugin.isSotetsegActive() || config.showTestOverlay()) {
            Set<WorldPoint> tiles;
            if (config.showTestOverlay()) {
                tiles = new HashSet();
                for (int i = 0; i < 5; ++i) {
                    try {
                        WorldPoint worldPoint = client.getLocalPlayer().getWorldLocation();
                        WorldPoint wp = new WorldPoint(worldPoint.getX(), worldPoint.getY() + i, worldPoint.getPlane());
                        tiles.add(wp);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            } else {
                tiles = plugin.getMazePings();
            }
            for (WorldPoint worldPoint : tiles) {
                Polygon poly;
                LocalPoint localPoint = LocalPoint.fromWorld(this.client, worldPoint);
                if (localPoint == null || (poly = Perspective.getCanvasTilePoly(client, localPoint)) == null || config.streamerMode()) continue;
                int outlineAlpha = config.getTileOutlineSize() > 0 ? 255 : 0;
                Color color = new Color(config.getTileOutline().getRed(), config.getTileOutline().getGreen(), config.getTileOutline().getBlue(), outlineAlpha);
                graphics.setColor(color);
                Stroke originalStroke = graphics.getStroke();
                graphics.setStroke(new BasicStroke(config.getTileOutlineSize()));
                graphics.draw(poly);
                Color fill = config.getTileColor();
                int alpha = Math.min(Math.max(config.getTileTransparency(), 0), 255);
                Color realFill = new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), alpha);
                graphics.setColor(realFill);
                graphics.fill(poly);
                graphics.setStroke(originalStroke);
            }
        }
        return null;
    }
}

