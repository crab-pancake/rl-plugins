package com.ticktimestamp;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Tick Timestamp",
	description = "Display a game client tick count as a timestamp on game chat messages.<br>" +
		"Useful for knowing if you are on pace when skilling or timing actions.",
	tags = {"tick", "count", "timestamp"}
)
public class TickTimestampPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private TickTimestampConfig config;

	private int lastTickCount;
	private int loginClientTick = 0;
//	private boolean ready = true;

	@Provides
	TickTimestampConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TickTimestampConfig.class);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		ChatMessageType type = event.getType();
		if (!ChatMessageType.GAMEMESSAGE.equals(type) && !ChatMessageType.SPAM.equals(type))
		{
			return;
		}

		int timestamp = config.deltaTick() ? (client.getTickCount() - lastTickCount) : (client.getTickCount() - loginClientTick + 1);
		lastTickCount = client.getTickCount();

		event.getMessageNode().setValue(timestamp + ": " + event.getMessageNode().getValue());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();

		switch (state)
		{
			case LOGGING_IN:
			case HOPPING:
			case CONNECTION_LOST:
//				ready = true;  // logged out and should reset once logged in
				loginClientTick = client.getTickCount();
				break;
		}
	}
}
