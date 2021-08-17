/*
 * Copyright (c) 2021, Zoinkwiz <https://github.com/Zoinkwiz>
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
package com.regionchat;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Ably Region Chat",
	description = "Talk to others even if they go to another fishing spot!",
	tags = { "chat" }
)
public class RegionChatPlugin extends Plugin
{
	@Inject
	private AblyManager ablyManager;

	@Inject
	private Client client;

	@Inject
	private RegionChatConfig config;

	@Getter
	private final HashMap<String, ArrayList<String>> previousMessages = new HashMap<>();

	boolean inPvp;

	@Override
	protected void startUp() throws Exception
	{
		ablyManager.startConnection();
	}

	@Override
	protected void shutDown() throws Exception
	{
		ablyManager.closeConnection();
	}

	// TODO: If not logged in, close channel

	@Subscribe
	public void onGameTick(GameTick event)
	{
		LocalPoint currentPos = client.getLocalPlayer().getLocalLocation();
		int regionID = client.getLocalPlayer().getWorldLocation().getRegionID();

		WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, currentPos);

		boolean foundRegion = false;

		for (Region region : Region.values())
		{
			if (region.getZones().stream().anyMatch((zone) -> zone.contains(worldPoint)))
			{
				foundRegion = true;
				String channelName = "";
				channelName += client.getWorld();
				if (region.isInstance())
				{
					channelName +=  ":" + regionID;
				}

				ablyManager.connectToRegion(region, channelName);
			}
		}

		if (!foundRegion)
		{
			ablyManager.disconnectFromRegions();
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged e)
	{
		inPvp = client.getVar(Varbits.PVP_SPEC_ORB) == 1;
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		EnumSet<WorldType> wt = client.getWorldType();

		if (wt.contains(WorldType.BOUNTY) ||
			wt.contains(WorldType.DEADMAN) ||
			wt.contains(WorldType.DEADMAN_TOURNAMENT) ||
			wt.contains(WorldType.PVP) ||
			inPvp
		)
		{
			return;
		}

		String cleanedName = Text.sanitize(event.getName());
		String cleanedMessage = Text.removeTags(event.getMessage());


		if (event.getType() != ChatMessageType.PUBLICCHAT ||
			!cleanedName.equals(client.getLocalPlayer().getName()))
		{
			return;
		}

		ablyManager.updateMessages(cleanedName, cleanedMessage);
		ablyManager.publishMessage(cleanedMessage);
	}

	@Provides
	RegionChatConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RegionChatConfig.class);
	}
}
