package com.example;

import com.google.inject.Provides;
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
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.PostClientTick;
import net.runelite.api.events.PostMenuSort;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Hover Tile Warning",
	description = "Changes mouse cursor when your hovered tile is unclickable or will path you to an unexpected position"
)
public class HoverTileWarning extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientUI clientUI;

	@Inject
	private HoverTileWarningConfig config;

//	@Inject
//	private DebugOverlay overlay;

//	@Inject
//	private OverlayManager overlayManager;

	private Cursor customCursor = null;
	private boolean wasBadHover = false;
	LocalPoint lastHoveredTile = null;
	private boolean walk = false;
	private BufferedImage warnCursor;
	private BufferedImage blockCursor;

	private final String WARN_NAME = "hoverTileWarn";
	private final String BLOCKED_NAME = "hoverTileBlocked";

	@Provides
	HoverTileWarningConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HoverTileWarningConfig.class);
	}

	@Override
	protected void startUp()
	{
//		overlayManager.add(overlay);

		warnCursor = ImageUtil.loadImageResource(HoverTileWarning.class, "/warn cursor.png");
		blockCursor = ImageUtil.loadImageResource(HoverTileWarning.class, "/block cursor.png");

		if (clientUI.getCurrentCursor().getType() == Cursor.DEFAULT_CURSOR){
			customCursor = null;
		}
		else {
			customCursor = clientUI.getCurrentCursor();
		}

	}

	@Override
	protected void shutDown()
	{
//		overlayManager.remove(overlay);

		resetCursor();
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged e){
		if (e.getGameState() != GameState.LOGGED_IN){
			resetCursor();
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged e){
		if (!Objects.equals(e.getGroup(), "customcursor"))
			return;

		if (clientUI.getCurrentCursor().getType() == Cursor.DEFAULT_CURSOR){
			customCursor = null;
		}
		else {
			customCursor = clientUI.getCurrentCursor();
		}
	}

	@Subscribe
	public void onPostClientTick(PostClientTick e)
	{
		if (!config.showUnclickable() && !config.showUnexpected())
		{
			return;
		}

		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

//		boolean isDefaultCursor = (clientUI.getCurrentCursor().getType() == Cursor.DEFAULT_CURSOR);
//		boolean isCustomCursor = (clientUI.getCurrentCursor().getType() == Cursor.CUSTOM_CURSOR
//			&& !List.of(WARN_NAME,BLOCKED_NAME).contains(clientUI.getCurrentCursor().getName()));

		if (!walk){
			wasBadHover = false;
			resetCursor();
			return;
		}

		if (client.getSelectedSceneTile() == null && config.showUnclickable() && !Objects.equals(clientUI.getCurrentCursor().getName(), BLOCKED_NAME))
		{
			clientUI.setCursor(blockCursor, BLOCKED_NAME);
			return;
		}

		if (client.getSelectedSceneTile() == null || !config.showUnexpected())
			return;

		// selected scene tile exists
		if (Objects.equals(clientUI.getCurrentCursor().getName(), BLOCKED_NAME)){
			resetCursor();
		}

		LocalPoint hoveredTile = client.getSelectedSceneTile().getLocalLocation();

		// if we are hovering over the same tile 2 client ticks in a row
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

				clientUI.resetCursor();
				clientUI.setCursor(warnCursor, WARN_NAME);
			}
			else if (!badHover && wasBadHover){
				wasBadHover = false;
				resetCursor();
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

	/*
	* TODO:
	*  right clicking or left click walk on any tile flashes the blocked cursor - why?
	*  warn cursor flickers when moving the mouse quickly: probably not solvable
	*  clicking on lower floors is borked. is this fixable?
	* SOLVED? hovering an upper floor tile close to the edge of clickable area results in buggy behaviour
	* SOLVED: sometimes unclickable tiles show warn cursor instead of blocked: warn cursor thing isnt replaced properly?
	* */
}
