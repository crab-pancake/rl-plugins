package com.BetterGodwarsOverlay;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class BetterGodwarsOverlayOverlay extends OverlayPanel
{

	private final Client client;
	private final BetterGodwarsOverlayConfig config;

	@Inject
	private BetterGodwarsOverlayOverlay(BetterGodwarsOverlayPlugin plugin, Client client, BetterGodwarsOverlayConfig config)
	{
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		setPriority(OverlayPriority.LOW);
		this.client = client;
		this.config = config;

		getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Godwars Overlay"));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		boolean[] hideGods = {config.hideArmadyl(), config.hideBandos(), config.hideSaradomin(), config.hideZamorak(), config.hideAncient()};
		int i = 0;
		//hide original overlay
		final Widget godwars = client.getWidget(WidgetInfo.GWD_KC);
		if (godwars != null)
		{
			godwars.setHidden(true);

			for (BetterGodwarsOverlayGods god : BetterGodwarsOverlayGods.values())
			{
				final int killcount = client.getVarbitValue(god.getKillCountVarbit().getId());

				if (!hideGods[i] && (killcount != 0 || !config.hideZero()))
				{
					panelComponent.getChildren().add(LineComponent.builder()
						.left(config.shortGodNames() ? god.getName().substring(0, 2) : god.getName())
						.leftColor(config.godNameColor())
						.right(Integer.toString(killcount))
						.rightColor(killcount >= config.highlightOnKC() ? config.highlightOnKCColor() : Color.WHITE)
						.build());
				}
				i++;
			}
		}
		return super.render(graphics);
	}
}
