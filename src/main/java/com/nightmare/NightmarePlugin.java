package com.nightmare;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.GraphicsObject;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j

@PluginDescriptor(
	name = "Nightmare",
	description = "meowdy"
)
public class NightmarePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NightmareConfig config;

	@Inject
	private NightmareOverlay overlay;

	@Inject
	private ClientThread clientThread;

	@Getter
	private final List<GraphicsObject> hands = new ArrayList<>();

	@Getter
	private boolean dreaming;

	@Getter
	private int lastHandTime = 0;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		hands.clear();
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		hands.clear();
	}

	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated graphicsObjectCreated)
	{
		if (dreaming)
		{
			GraphicsObject graphicsObject = graphicsObjectCreated.getGraphicsObject();
			if (graphicsObject.getId() == 1767)
			{
				if (graphicsObject.getStartCycle() > lastHandTime + 10){
					lastHandTime = graphicsObject.getStartCycle();
					hands.clear();
				}
				hands.add(graphicsObject);
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		GameState gameState = gameStateChanged.getGameState();
		if (gameState == GameState.LOADING)
		{
			hands.clear();
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{

		if (event.getVarbitId() == VarPlayer.HP_HUD_NPC_ID.getId()){
			return;
		}
		int npcId = event.getVarpId();
		dreaming = (npcId >= 9416 && npcId <= 9424) || (npcId >= 11153 && npcId <= 11155);
	}

	@Provides
	NightmareConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NightmareConfig.class);
	}
}
