package com.ticktimestamp;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MessageNode;
import net.runelite.api.ScriptID;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ColorUtil;

@PluginDescriptor(
	name = "Tick Timestamp",
	description = "Display a game client tick count as a timestamp on game chat messages",
	tags = {"tick", "count", "timestamp"},
	conflicts = "Chat Timestamps"
)
public class TickTimestampPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private TickTimestampConfig config;

	private int loginGameTick = 0;

	private final Set<ChatMessageType> timestampedTypes = new HashSet<>(Arrays.asList(ChatMessageType.CONSOLE,ChatMessageType.SPAM,ChatMessageType.GAMEMESSAGE));

	@Provides
	TickTimestampConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TickTimestampConfig.class);
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged e){
		switch(e.getGameState())
		{
			case LOGGING_IN:
			case HOPPING:
				loginGameTick = client.getTickCount();
				break;
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("ticktimestamp"))
		{
			switch (event.getKey())
			{
				case "opaqueTimestamp":
				case "transparentTimestamp":
					clientThread.invokeLater(() -> client.runScript(ScriptID.SPLITPM_CHANGED));
					break;
				case "chatmessages":
					updateTypes();
			}
		}
	}

	@Subscribe
	private void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
		if (!"chatMessageBuilding".equals(event.getEventName()))
		{
			return;
		}

		int uid = client.getIntStack()[client.getIntStackSize() - 1];
		final MessageNode messageNode = client.getMessages().get(uid);
		assert messageNode != null : "chat message build for unknown message";

		// rebuilt per message. if just added, it will have epoch time: big number
		if (messageNode.getTimestamp() >> 24 > 1){
			messageNode.setTimestamp(client.getTickCount());
		}

		String timestamp = "";

		if (timestampedTypes.contains(messageNode.getType())){
			timestamp = getTimestampString(messageNode);
		}

		client.getStringStack()[client.getStringStackSize() - 1] = timestamp;
	}

	private Color getTimestampColour()
	{
		boolean isChatboxTransparent = client.isResized() && client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) == 1;

		return isChatboxTransparent ? config.transparentTimestamp() : config.opaqueTimestamp();
	}

	private String getTimestampString(MessageNode msg){
		String timestamp = String.valueOf((msg.getTimestamp() - loginGameTick + 1));

		Color timestampColour = getTimestampColour();
		if (timestampColour != null)
		{
			timestamp = ColorUtil.wrapWithColorTag(timestamp, timestampColour);
		}

		return "["+timestamp + "]";
	}

	private void updateTypes(){
		if (config.chatMessage()){
			timestampedTypes.addAll(Arrays.asList(ChatMessageType.PUBLICCHAT, ChatMessageType.PRIVATECHAT, ChatMessageType.PRIVATECHATOUT,
				ChatMessageType.CLAN_CHAT, ChatMessageType.CLAN_GUEST_CHAT));
		}
		else {
			Arrays.asList(ChatMessageType.PUBLICCHAT,ChatMessageType.PRIVATECHAT,ChatMessageType.PRIVATECHATOUT,
				ChatMessageType.CLAN_CHAT,ChatMessageType.CLAN_GUEST_CHAT).forEach(timestampedTypes::remove);
		}
	}
}
