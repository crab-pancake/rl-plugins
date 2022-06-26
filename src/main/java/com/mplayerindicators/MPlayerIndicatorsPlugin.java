/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.mplayerindicators;

import com.google.inject.Provides;
import lombok.Value;
import net.runelite.api.*;
import net.runelite.api.clan.ClanTitle;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.runelite.api.FriendsChatRank.UNRANKED;
import static net.runelite.api.MenuAction.*;

@PluginDescriptor(
	name = "[M] Player Indicators",
	description = "Also highlights offline friends",
	tags = {"highlight", "minimap", "overlay", "players"}
)
public class MPlayerIndicatorsPlugin extends Plugin
{
	private static final String TRADING_WITH_TEXT = "Trading with: ";
	private static final Pattern WILDERNESS_LEVEL_PATTERN = Pattern.compile("^Level: (\\d+)$");

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private MPlayerIndicatorsConfig config;

	@Inject
	private MPlayerIndicatorsOverlay overlay;

	@Inject
	private MPlayerIndicatorsTileOverlay tileOverlay;

	@Inject
	private MPlayerIndicatorsMinimapOverlay minimapOverlay;

	@Inject
	private MPlayerIndicatorsService MPlayerIndicatorsService;

	@Inject
	private Client client;

	@Inject
	private ChatIconManager chatIconManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private Notifier notifier;

	boolean isPVP;
	int lastPlayedTick = -1;

	@Provides
	MPlayerIndicatorsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MPlayerIndicatorsConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		isPVP = false;
		overlayManager.add(overlay);
		overlayManager.add(tileOverlay);
		overlayManager.add(minimapOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		overlayManager.remove(tileOverlay);
		overlayManager.remove(minimapOverlay);
		lastPlayedTick = -1;
	}

	@Subscribe
	private void onPlayerSpawned(PlayerSpawned event)
	{
		if (!config.pvpAlertSound() || !isPVP) return;

		Player player = event.getPlayer();
		if (player == client.getLocalPlayer()) return;

		// later: check what player is wearing also?
		if (config.pvpAlertSound()
				&& !client.isFriended(player.getName(),false)
				&& isAttackable(client, player)) {
			if (lastPlayedTick < client.getTickCount()) { // play max once per tick
				client.playSoundEffect(3924, config.playerAlertSoundVolume());
				lastPlayedTick = client.getTickCount();
				}
			notifier.notify("Attackable player spawned!");
			client.addChatMessage(ChatMessageType.CONSOLE,"", "Player spawned: "+player.getName()+" ("+player.getCombatLevel()+")","");
		}
	}

	@Subscribe
	private void onVarbitChanged(VarbitChanged event)
	{
		checkPVP(client);
	}

	@Subscribe
	private void onWorldChanged(WorldChanged event)
	{
		checkPVP(client);
	}

	boolean isAttackable(Client client, Player player)
	{
		final Widget wildernessLevelWidget = client.getWidget(WidgetInfo.PVP_WILDERNESS_LEVEL);
			// TODO: make wildy level a separate int that updates on widget changed, so it doesnt get rerun every time a player spawns? probably not that expensive rn tbh
		if (wildernessLevelWidget == null && !WorldType.isPvpWorld(client.getWorldType()))
		{
			return false;
		}

		int wildyLvl = 0;

		if (wildernessLevelWidget != null)
		{
			final String wildernessLevelText = wildernessLevelWidget.getText();
			final Matcher m = WILDERNESS_LEVEL_PATTERN.matcher(wildernessLevelText);
			if (m.matches()) {
				wildyLvl = Integer.parseInt(m.group(1)) + 1; // add +/- 1 level margin for safety
			}
		}

		final int combatLvlRange = wildyLvl + (WorldType.isPvpWorld(client.getWorldType()) ? 15 : 0);
		final int combatLevel = client.getLocalPlayer().getCombatLevel();

		final int minLevel = Math.max(3, combatLevel - combatLvlRange);
		final int maxLevel = Math.min(126, combatLevel + combatLvlRange);

		return player.getCombatLevel() >= minLevel && player.getCombatLevel() <= maxLevel;
	}

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		if (client.isMenuOpen())
		{
			return;
		}

		MenuEntry[] menuEntries = client.getMenuEntries();

		for (MenuEntry entry : menuEntries)
		{
			MenuAction type = entry.getType();

			if (type == WALK
				|| type == WIDGET_TARGET_ON_PLAYER
				|| type == ITEM_USE_ON_PLAYER
				|| type == PLAYER_FIRST_OPTION
				|| type == PLAYER_SECOND_OPTION
				|| type == PLAYER_THIRD_OPTION
				|| type == PLAYER_FOURTH_OPTION
				|| type == PLAYER_FIFTH_OPTION
				|| type == PLAYER_SIXTH_OPTION
				|| type == PLAYER_SEVENTH_OPTION
				|| type == PLAYER_EIGTH_OPTION
				|| type == RUNELITE_PLAYER)
			{
				Player[] players = client.getCachedPlayers();
				Player player = null;

				int identifier = entry.getIdentifier();

				// 'Walk here' identifiers are offset by 1 because the default
				// identifier for this option is 0, which is also a player index.
				if (type == WALK)
				{
					identifier--;
				}

				if (identifier >= 0 && identifier < players.length)
				{
					player = players[identifier];
				}

				if (player == null)
				{
					continue;
				}

				Decorations decorations = getDecorations(player);

				if (decorations == null)
				{
					continue;
				}

				String oldTarget = entry.getTarget();
				String newTarget = decorateTarget(oldTarget, decorations);

				entry.setTarget(newTarget);
			}
		}
	}

	private Decorations getDecorations(Player player)
	{
		int image = -1;
		Color color = null;

		if (client.isFriended(player.getName(),false) && config.highlightFriends())
		{
			color = config.getFriendColor();
		}
		else if (player.isFriendsChatMember() && config.highlightFriendsChat())
		{
			color = config.getFriendsChatMemberColor();

			if (config.showFriendsChatRanks())
			{
				FriendsChatRank rank = MPlayerIndicatorsService.getFriendsChatRank(player);
				if (rank != UNRANKED)
				{
					image = chatIconManager.getIconNumber(rank);
				}
			}
		}
		else if (player.getTeam() > 0 && client.getLocalPlayer().getTeam() == player.getTeam() && config.highlightTeamMembers())
		{
			color = config.getTeamMemberColor();
		}
		else if (player.isClanMember() && config.highlightClanMembers())
		{
			color = config.getClanMemberColor();

			if (config.showClanChatRanks())
			{
				ClanTitle clanTitle = MPlayerIndicatorsService.getClanTitle(player);
				if (clanTitle != null)
				{
					image = chatIconManager.getIconNumber(clanTitle);
				}
			}
		}
		else if (!player.isFriendsChatMember() && !player.isClanMember() && config.highlightOthers())
		{
			color = config.getOthersColor();
		}

		if (image == -1 && color == null)
		{
			return null;
		}

		return new Decorations(image, color);
	}

	private String decorateTarget(String oldTarget, Decorations decorations)
	{
		String newTarget = oldTarget;

		if (decorations.getColor() != null && config.colorPlayerMenu())
		{
			// strip out existing <col...
			int idx = oldTarget.indexOf('>');
			if (idx != -1)
			{
				newTarget = oldTarget.substring(idx + 1);
			}

			newTarget = ColorUtil.prependColorTag(newTarget, decorations.getColor());
		}

		if (decorations.getImage() != -1)
		{
			newTarget = "<img=" + decorations.getImage() + ">" + newTarget;
		}

		return newTarget;
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (event.getScriptId() == ScriptID.TRADE_MAIN_INIT)
		{
			clientThread.invokeLater(() ->
			{
				Widget tradeTitle = client.getWidget(WidgetInfo.TRADE_WINDOW_HEADER);
				String header = tradeTitle.getText();
				String playerName = header.substring(TRADING_WITH_TEXT.length());

				Player targetPlayer = findPlayer(playerName);
				if (targetPlayer == null)
				{
					return;
				}

				Decorations playerColor = getDecorations(targetPlayer);
				if (playerColor != null)
				{
					tradeTitle.setText(TRADING_WITH_TEXT + ColorUtil.wrapWithColorTag(playerName, playerColor.color));
				}
			});
		}
	}

	private Player findPlayer(String name)
	{
		for (Player player : client.getPlayers())
		{
			if (player.getName().equals(name))
			{
				return player;
			}
		}
		return null;
	}

	@Value
	private static class Decorations
	{
		int image;
		Color color;
	}

	public void checkPVP(Client client)
	{
		clientThread.invokeLater(() -> {
			isPVP = client.getVarbitValue(5963) == 1 || WorldType.isPvpWorld(client.getWorldType());
		});
	}
}
