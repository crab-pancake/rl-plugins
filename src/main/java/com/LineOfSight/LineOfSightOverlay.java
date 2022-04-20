package com.LineOfSight;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Tile;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;
import java.util.Set;

public class LineOfSightOverlay extends Overlay {
    private final Client client;

    @Inject
    private LineOfSightOverlay(Client client, LineOfSightPlugin plugin){
        this.client = client;
    }

    @Override
    public Dimension render(Graphics2D graphics){
        return null;
    }

    private void renderMovementInfo(Graphics2D graphics, Tile tile)
    {
        Polygon poly = Perspective.getCanvasTilePoly(client, tile.getLocalLocation());

        if (poly == null || !poly.contains(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY()))
        {
            return;
        }

        if (client.getCollisionMaps() != null)
        {
            int[][] flags = client.getCollisionMaps()[client.getPlane()].getFlags();
            int data = flags[tile.getSceneLocation().getX()][tile.getSceneLocation().getY()];

            Set<MovementFlag> movementFlags = MovementFlag.getSetFlags(data);

            if (movementFlags.isEmpty())
            {
            }
            else
            {
            }

            OverlayUtil.renderPolygon(graphics, poly, Color.BLUE);
        }
    }
}
