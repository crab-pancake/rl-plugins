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

import net.runelite.api.*;
import net.runelite.api.clan.*;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.function.BiConsumer;

@Singleton
public class MPlayerIndicatorsService
{
	private final Client client;
	private final MPlayerIndicatorsConfig config;
	private final MPlayerIndicatorsPlugin plugin;

	@Inject
	private MPlayerIndicatorsService(Client client, MPlayerIndicatorsConfig config, MPlayerIndicatorsPlugin plugin)
	{
		this.config = config;
		this.client = client;
		this.plugin = plugin;
	}

	public void forEachPlayer(final BiConsumer<Player, Color> consumer)
	{
		if (!config.highlightOwnPlayer() && !config.highlightFriendsChat()
			&& !config.highlightFriends() && !config.highlightOthers()
			&& !config.highlightClanMembers() && !config.pvpHighlight())
		{
			return;
		}

		boolean pvpHighlight = plugin.isPVP && config.pvpHighlight();

		final Player localPlayer = client.getLocalPlayer();

		for (Player player : client.getPlayers())
		{
			if (player == null || player.getName() == null)
			{
				continue;
			}

			boolean isFriendsChatMember = player.isFriendsChatMember();
			boolean isClanMember = player.isClanMember();

			if (player == localPlayer)
			{
				if (config.highlightOwnPlayer())
				{
					consumer.accept(player, config.getOwnPlayerColor());
				}
			}
			else if ((config.highlightFriends() || pvpHighlight) && client.isFriended(player.getName(),false))
			{
				consumer.accept(player, config.getFriendColor());
			}
			else if ((config.highlightFriendsChat() || pvpHighlight) && isFriendsChatMember)
			{
				consumer.accept(player, config.getFriendsChatMemberColor());
			}
			else if ((config.highlightTeamMembers() || pvpHighlight) && localPlayer.getTeam() > 0 && localPlayer.getTeam() == player.getTeam())
			{
				consumer.accept(player, config.getTeamMemberColor());
			}
			else if ((config.highlightClanMembers() || pvpHighlight) && isClanMember)
			{
				consumer.accept(player, config.getClanMemberColor());
			}
			else if ((config.highlightOthers() || pvpHighlight) && !isFriendsChatMember && !isClanMember)
			{
				if (pvpHighlight && plugin.isAttackable(client, player))
				{
					consumer.accept(player, config.attackableColour());
				}
				else consumer.accept(player, config.getOthersColor());
			}
		}
	}

	ClanTitle getClanTitle(Player player)
	{
		ClanChannel clanChannel = client.getClanChannel();
		ClanSettings clanSettings = client.getClanSettings();
		if (clanChannel == null || clanSettings == null)
		{
			return null;
		}

		ClanChannelMember member = clanChannel.findMember(player.getName());
		if (member == null)
		{
			return null;
		}

		ClanRank rank = member.getRank();
		return clanSettings.titleForRank(rank);
	}

	FriendsChatRank getFriendsChatRank(Player player)
	{
		final FriendsChatManager friendsChatManager = client.getFriendsChatManager();
		if (friendsChatManager == null)
		{
			return FriendsChatRank.UNRANKED;
		}

		FriendsChatMember friendsChatMember = friendsChatManager.findByName(Text.removeTags(player.getName()));
		return friendsChatMember != null ? friendsChatMember.getRank() : FriendsChatRank.UNRANKED;
	}
}
