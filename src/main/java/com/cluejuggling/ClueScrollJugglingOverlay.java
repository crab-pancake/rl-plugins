package com.cluejuggling;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.infobox.Timer;

public class ClueScrollJugglingOverlay extends Overlay
{
	private final Client client;
	private final ClueScrollJugglingPlugin plugin;
	private final ClueScrollJugginglingConfig config;

	@Inject
	private ClueScrollJugglingOverlay(Client client, ClueScrollJugglingPlugin plugin, ClueScrollJugginglingConfig config)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
//		for (Map<GroundItem.GroundItemKey, Timer> item : ClueScrollJugglingPlugin.dropTimers.keySet()){
//			final LocalPoint groundPoint = LocalPoint.fromWorld(client, item.getLocation());
//			if (groundPoint == null || localLocation.distanceTo(groundPoint) > 3000){
//				continue;
//			}
//			final String timerText = String.format(" - %.1f", despawnTimeMillis / 1000f);
//		}
		return null;
	}
}
