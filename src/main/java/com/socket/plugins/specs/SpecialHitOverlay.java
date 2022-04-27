/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.Perspective
 *  net.runelite.api.Player
 *  net.runelite.api.Point
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.OverlayPriority
 *  net.runelite.client.ui.overlay.OverlayUtil
 */
package com.socket.plugins.specs;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SpecialHitOverlay extends Overlay {
    private final Client client;
    private final SpecConfig config;
    private final Map<String, ArrayList<SpecialIcon>> drawings = new HashMap<>();

    @Inject
    private SpecialHitOverlay(Client client, SpecPlugin plugin, SpecConfig config) {
        this.client = client;
        this.config = config;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setPriority(OverlayPriority.HIGH);
        this.setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    public void addOverlay(String player, SpecialIcon icon) {
        if (config.showHitOverlay()) {
            ArrayList<SpecialIcon> icons = new ArrayList<>();
            if (drawings.containsKey(player)) {
                icons = drawings.get(player);
            }
            icons.add(icon);
            drawings.put(player, icons);
        }
    }

    public Dimension render(Graphics2D graphics) {
        ArrayList<String> removePlayers = new ArrayList<>();
        HashMap<String, LocalPoint> locations = new HashMap<>();
        for (Player player : client.getPlayers()) {
            locations.put(player.getName(), player.getLocalLocation());
        }
        for (String playerName : drawings.keySet()) {
            LocalPoint center = locations.get(playerName);
            if (center != null) {
                ArrayList<SpecialIcon> icons = drawings.get(playerName);
                ArrayList<SpecialIcon> removeIcons = new ArrayList<>();
                int currentHeight = 200;
                for (int i = icons.size() - 1; i >= 0; --i) {
                    SpecialIcon icon = icons.get(i);
                    long elapsedTime = System.currentTimeMillis() - icon.getStartTime();
                    int fadeDelay = Math.max(config.getFadeDelay(), 1);
                    long timeRemaining = (long)fadeDelay - elapsedTime;
                    if (timeRemaining <= 0L) {
                        removeIcons.add(icon);
                        continue;
                    }
                    float opacity = (float)timeRemaining / (float)fadeDelay;
                    float thresh = Math.min(opacity + 0.2f, 1.0f);
                    graphics.setComposite(AlphaComposite.getInstance(3, thresh));
                    int maxHeight = Math.max(config.getMaxHeight(), 1);
                    int updatedHeight = maxHeight - (int)((float)maxHeight * thresh);
                    Point drawPoint = Perspective.getCanvasImageLocation(client, center, icon.getImage(), currentHeight + updatedHeight);
                    graphics.drawImage(icon.getImage(), drawPoint.getX(), drawPoint.getY(), null);
                    if (icon.getText() != null) {
                        Point textPoint = Perspective.getCanvasTextLocation(client, graphics, center, icon.getText(), currentHeight + updatedHeight);
                        graphics.setFont(new Font("Arial", 1, 16));
                        Point canvasCenterPoint = new Point(textPoint.getX(), textPoint.getY());
                        Point canvasCenterPointShadow = new Point(textPoint.getX() + 1, textPoint.getY() + 1);
                        OverlayUtil.renderTextLocation(graphics, canvasCenterPointShadow, icon.getText(), Color.BLACK);
                        OverlayUtil.renderTextLocation(graphics, canvasCenterPoint, icon.getText(), Color.WHITE);
                    }
                    currentHeight += icon.getImage().getHeight() * 2;
                }
                for (SpecialIcon icon : removeIcons) {
                    icons.remove(icon);
                }
                if (!icons.isEmpty()) continue;
                removePlayers.add(playerName);
                continue;
            }
            removePlayers.add(playerName);
        }
        for (String playerName : removePlayers) {
            drawings.remove(playerName);
        }
        return null;
    }
}

