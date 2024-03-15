package com.cluejuggling;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.grounditems.config.DespawnTimerMode;
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

	private final Map<WorldPoint, Integer> offsetMap = new HashMap<>();

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

		offsetMap.clear();

		for (Map.Entry<GroundItem.GroundItemKey, Timer> itemTimer : plugin.dropTimers.entrySet()){
			final LocalPoint groundPoint = LocalPoint.fromWorld(client, itemTimer.getKey().getLocation());
			if (groundPoint == null || client.getLocalPlayer().getLocalLocation().distanceTo(groundPoint) > 3000){
				continue;
			}
			long despawnTimeMillis = itemTimer.getValue().getEndTime().toEpochMilli() - Instant.now().toEpochMilli();

			final String timerText;
			if (config.timerMode() == DespawnTimerMode.SECONDS)
			{
				timerText = String.format("%.1f", despawnTimeMillis / 1000f);
			}
			else // TICKS
			{
				timerText = String.format("%d", despawnTimeMillis / 600);
			}

			final Point textPoint = Perspective.getCanvasTextLocation(client, graphics, groundPoint, timerText, -30);

			final int offset = offsetMap.compute(itemTimer.getKey().getLocation(), (k, v) -> v != null ? v + 1 : 0);

			final int textY = textPoint.getY() - (15 * offset);

			textComponent.setText(timerText);
			textComponent.setOutline(false);
			textComponent.setColor(despawnTimeMillis < config.notificationTime() / 1000 ? Color.RED : Color.WHITE);
			textComponent.setPosition(new java.awt.Point(textPoint.getX(),textY));
			textComponent.render(graphics);
		}
		return null;
	}
}
