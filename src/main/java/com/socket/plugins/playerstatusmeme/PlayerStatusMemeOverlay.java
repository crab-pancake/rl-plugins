/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.Perspective
 *  net.runelite.api.Point
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.ui.overlay.OverlayPanel
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.OverlayPriority
 *  net.runelite.client.util.ImageUtil
 */
package com.socket.plugins.playerstatusmeme;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.util.ImageUtil;

public class PlayerStatusMemeOverlay
extends OverlayPanel {
    private final Client client;
    private final PlayerStatusMemePlugin plugin;

    @Inject
    private PlayerStatusMemeOverlay(Client client, PlayerStatusMemePlugin plugin) {
        this.client = client;
        this.plugin = plugin;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setPriority(OverlayPriority.HIGH);
        this.setLayer(OverlayLayer.ABOVE_SCENE);
    }

    public Dimension render(Graphics2D graphics) {
        Point base;
        if (this.client.getLocalPlayer() != null && (base = Perspective.localToCanvas(this.client, this.client.getLocalPlayer().getLocalLocation(), this.client.getPlane(), this.client.getLocalPlayer().getLogicalHeight() / 2 - 10)) != null) {
            base = new Point(0, 0);
            BufferedImage icon = ImageUtil.loadImageResource(PlayerStatusMemePlugin.class, this.plugin.ratJamFrame + ".png");
            if (icon != null) {
                graphics.drawImage(icon, base.getX(), base.getY(), this.client.getCanvasWidth(), this.client.getCanvasHeight(), null);
            }
        }
        return super.render(graphics);
    }
}

