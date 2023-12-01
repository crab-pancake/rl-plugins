package com.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

public class DebugOverlay extends OverlayPanel
{
	private final ExtremeVardorvis plugin;
	private final VardConfig config;

	@Inject
	private DebugOverlay(ExtremeVardorvis plugin, VardConfig config){
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		setPriority(OverlayPriority.LOW);
		this.plugin = plugin;
		this.config = config;
	}


	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().add(LineComponent.builder()
			.left("In fight:")
			.right(String.valueOf(plugin.inFight)).rightColor(plugin.inFight ? Color.GREEN : Color.RED)
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Next boss hit: ")
			.right(String.valueOf(plugin.bossAttackTimer))
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Next osu: ")
			.right(String.valueOf(plugin.nextPossibleOsu))
			.build());

		return super.render(graphics);
	}
}
