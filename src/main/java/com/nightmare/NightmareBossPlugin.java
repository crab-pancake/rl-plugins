package com.nightmare;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.ArrayList;
import java.util.List;

@PluginDescriptor(
	name = "Nightmare",
	description = "meowdy"
)
public class NightmareBossPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NightmareBossConfig config;

	@Inject
	private NightmareBossOverlay overlay;

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

		clientThread.invokeLater(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				dreaming = isHealthbarActive();
			}
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
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

//			if (config.debug()) {
//				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", String.valueOf(graphicsObject.getLocation()), null);
//			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		GameState gameState = gameStateChanged.getGameState();
		if (gameState == GameState.LOADING)
		{
			hands.clear();
			if (config.debug()) {
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Gamestate changed, cleared list of hands", null);
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		dreaming = isHealthbarActive();
	}

	@Provides
	NightmareBossConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NightmareBossConfig.class);
	}

	private boolean isHealthbarActive()
	{
		if (config.debug()) {
			return true;
		}
		int npcId = client.getVar(VarPlayer.HP_HUD_NPC_ID);
		return (npcId >= 9416 && npcId <= 9424) || (npcId >= 11153 && npcId <= 11155);
	}
}
