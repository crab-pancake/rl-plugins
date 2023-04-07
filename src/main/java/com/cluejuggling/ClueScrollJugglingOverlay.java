package com.cluejuggling;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Instant;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TextComponent;
import net.runelite.client.ui.overlay.infobox.Timer;

public class ClueScrollJugglingOverlay extends Overlay
{
	private final Client client;
	private final ClueScrollJugglingPlugin plugin;
	private final ClueScrollJugglingConfig config;
	private final TextComponent textComponent = new TextComponent();

	@Inject
	private ClueScrollJugglingOverlay(Client client, ClueScrollJugglingPlugin plugin, ClueScrollJugglingConfig config)
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
		if (!config.showOnClue()) return null;
		for (Map.Entry<GroundItem.GroundItemKey, Timer> itemTimer : plugin.dropTimers.entrySet()){
			final LocalPoint groundPoint = LocalPoint.fromWorld(client, itemTimer.getKey().getLocation());
			if (groundPoint == null || client.getLocalPlayer().getLocalLocation().distanceTo(groundPoint) > 3000){
				continue;
			}
			long despawnTimeMillis = itemTimer.getValue().getEndTime().toEpochMilli() - Instant.now().toEpochMilli();

			final String timerText = String.format("%d", despawnTimeMillis / 600);

			final Point textPoint = Perspective.getCanvasTextLocation(client, graphics, groundPoint, timerText, -30);

			textComponent.setText(timerText);
			textComponent.setOutline(false);

			if (textPoint != null){
				textComponent.setPosition(new java.awt.Point(textPoint.getX(),textPoint.getY()));
				textComponent.render(graphics);
			}
		}
		return null;
	}
}
