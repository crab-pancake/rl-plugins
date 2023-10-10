package com.example;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

public class DebugOverlay extends OverlayPanel
{
	private final Client client;
	private final HoverTileWarning plugin;
	private final ClientUI clientUI;

	@Inject
	private DebugOverlay(Client client, HoverTileWarning plugin, ClientUI clientUI)
	{
		super(plugin);
		setPosition(OverlayPosition.BOTTOM_LEFT);
		this.client = client;
		this.plugin = plugin;
		this.clientUI = clientUI;
		setPreferredSize(new Dimension(300,0));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().add(LineComponent.builder()
			.left("Current:")
			.right(client.getSelectedSceneTile() == null ? "null" : client.getSelectedSceneTile().getWorldLocation().toString())
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Last:")
			.right(plugin.lastHoveredTile == null ? "null" : WorldPoint.fromLocal(client, plugin.lastHoveredTile).toString())
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left("name")
			.right(clientUI.getCurrentCursor().getName())
			.build());

		return super.render(graphics);
	}
}
