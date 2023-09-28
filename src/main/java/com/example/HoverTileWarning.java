package com.example;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.PostClientTick;
import net.runelite.api.events.PostMenuSort;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Hover Tile Warning"
)
public class HoverTileWarning extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientUI clientUI;

	private Cursor customCursor = null;
	private boolean wasBadHover = false;
	private LocalPoint lastHoveredTile = null;
	private boolean walk = false;
	private BufferedImage warnCursor;
	private BufferedImage blockCursor;

	private final String WARN_NAME = "hoverTileWarn";
	private final String BLOCKED_NAME = "hoverTileBlocked";

	@Override
	protected void startUp()
	{
		warnCursor = ImageUtil.loadImageResource(HoverTileWarning.class, "/warn cursor.png");
		blockCursor = ImageUtil.loadImageResource(HoverTileWarning.class, "/block cursor.png");
	}

	@Override
	protected void shutDown()
	{
		resetCursor();
	}

	@Subscribe
	public void onPostClientTick(PostClientTick e)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		boolean isDefaultCursor = (clientUI.getCurrentCursor().getType() == Cursor.DEFAULT_CURSOR);
		boolean isCustomCursor = (clientUI.getCurrentCursor().getType() == Cursor.CUSTOM_CURSOR
			&& !List.of(WARN_NAME,BLOCKED_NAME).contains(clientUI.getCurrentCursor().getName()));

		if (!walk){
			wasBadHover = false;
			resetCursor();
			return;
		}

		if (client.getSelectedSceneTile() == null)
		{
			if (isDefaultCursor)
			{
				customCursor = null;
				clientUI.setCursor(blockCursor, BLOCKED_NAME);
			}
			else if (isCustomCursor && !Objects.equals(clientUI.getCurrentCursor().getName(), BLOCKED_NAME))
			{
				if (!List.of(WARN_NAME,BLOCKED_NAME).contains(clientUI.getCurrentCursor().getName()))
				{
					customCursor = clientUI.getCurrentCursor();
				}
				clientUI.setCursor(blockCursor, BLOCKED_NAME);
			}
			return;
		}
		else if (Objects.equals(clientUI.getCurrentCursor().getName(), BLOCKED_NAME)){
			resetCursor();
		}

		LocalPoint hoveredTile = client.getSelectedSceneTile().getLocalLocation();

		if (hoveredTile == null)
		{
			return;
		}

		if (lastHoveredTile != null && lastHoveredTile.getX() == hoveredTile.getX() && lastHoveredTile.getY() == hoveredTile.getY())
		{
			final Polygon poly = Perspective.getCanvasTilePoly(client, hoveredTile);

			if (poly == null)
			{
				return;
			}

			boolean badHover = !poly.contains(new Point(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY()));

			if (badHover && !wasBadHover)
			{
				wasBadHover = true;

				if (isDefaultCursor)
				{
					customCursor = null;
					clientUI.setCursor(warnCursor, WARN_NAME);
				}
				else if (isCustomCursor && !Objects.equals(clientUI.getCurrentCursor().getName(), WARN_NAME))
				{
					if (!List.of(WARN_NAME,BLOCKED_NAME).contains(clientUI.getCurrentCursor().getName()))
					{
						customCursor = clientUI.getCurrentCursor();
					}
					clientUI.setCursor(warnCursor, WARN_NAME);
				}
			}
		}
		else if (lastHoveredTile != null){
			wasBadHover = false;
			resetCursor();
		}

		lastHoveredTile = hoveredTile;
	}

	@Subscribe(priority=-1)
	public void onPostMenuSort(PostMenuSort e){
		walk = client.getMenuEntries()[client.getMenuEntries().length - 1].getOption().equalsIgnoreCase("walk here");
	}

	private void resetCursor(){
		if (!List.of(WARN_NAME,BLOCKED_NAME).contains(clientUI.getCurrentCursor().getName()))
		{
			return;
		}

		if (customCursor != null)
		{
			clientUI.setCursor(customCursor);
		}
		else
		{
			clientUI.resetCursor();
		}
	}
}
