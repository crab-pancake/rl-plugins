package com.example;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import static net.runelite.client.ui.overlay.OverlayPosition.*;
import net.runelite.client.ui.overlay.OverlayUtil;

public class TileOverlay extends Overlay
{
    HoverTileWarning plugin;
    Client client;

	final Color clear = new Color(0,0,0,0);

	TileOverlay(Client client, HoverTileWarning plugin) {
        this.plugin = plugin;
        this.client = client;
        setPosition(DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);

    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (plugin.badHover){
			if (client.getSelectedSceneTile() != null)
			{
				final LocalPoint dest = client.getSelectedSceneTile().getLocalLocation();
				if (dest != null)
				{
					final Polygon poly = Perspective.getCanvasTileAreaPoly(client, dest, 1);
					if (poly != null)
					{
						OverlayUtil.renderPolygon(graphics, poly, Color.ORANGE, clear, new BasicStroke((float) (double) 2));
					}
				}
			}
		}
        return null;
    }

}
