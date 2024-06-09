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
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.Channel;
import io.ably.lib.realtime.ChannelEvent;
import io.ably.lib.realtime.ChannelState;
import io.ably.lib.realtime.CompletionListener;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.ClientOptions;
import io.ably.lib.types.ErrorInfo;
import io.ably.lib.types.Message;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.WorldType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class AblyManager
{
	private final Client client;

	private final Gson gson;

	private final Map<String, String> previousMessages = new HashMap<>();
	public final Map<String, String> previousRealMessages = new HashMap<>();

	private final String CHANNEL_NAME_PREFIX = "regionchat";

	private boolean changingChannels;

	@Inject
	ChatMessageManager chatMessageManager;

	private final RegionChatConfig config;

	private AblyRealtime ablyRealtime;
	private Channel ablyRegionChannel;

	private SpamMessages spamMessages;

	public final String BUBBLE_ICON = "<img=19>";

	@Inject
	public AblyManager(Client client, RegionChatConfig config, Gson gson)
	{
		this.client = client;
		this.config = config;
		this.gson = gson;
		this.spamMessages = new SpamMessages();
	}

	public void startConnection()
	{
		setupAblyInstances();
	}

	public void closeConnection()
	{
		ablyRealtime.close();
		ablyRealtime = null;
		ablyRegionChannel = null;
	}

	public void publishMessage(String message)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		if (ablyRegionChannel == null)
		{
			return;
		}

		if (spamMessages.isSpam(message)) return;

		try
		{
			JsonObject msg = io.ably.lib.util.JsonUtils.object()
				.add("username", client.getLocalPlayer().getName())
				.add("message", message).toJson();

			ablyRegionChannel.publish("event", msg);
		}
		catch (AblyException err)
		{
			System.out.println(err.getMessage());
		}
	}

	private void handleMessage(Message message)
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			handleAblyMessage(message, config.regionChatColour());
		}
	}

	private void handleAblyMessage(Message message, Color color)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		RegionChatMessage msg = gson.fromJson((JsonElement) message.data, RegionChatMessage.class);

		String username = msg.username;
		String receivedMsg = Text.removeTags(msg.message);

		if (spamMessages.isSpam(msg.message)) return;
		if (isInvalidUsername(msg.username)) return;
		if (!tryUpdateMessages(username, receivedMsg)) return;

		final ChatMessageBuilder chatMessageBuilder = new ChatMessageBuilder()
			.append(color, receivedMsg);

		if (username.length() > 12)
		{
			return;
		}
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.PUBLICCHAT)
			.name(BUBBLE_ICON + msg.username)
			.runeLiteFormattedMessage(chatMessageBuilder.build())
			.build());
	}

	public boolean tryUpdateMessages(String name, String message)
	{
		String prevMessage = previousMessages.get(name);
		// If someone is spamming the same message during a session, block it
		if (message.equals(prevMessage))
		{
			return false;
		}

		String lastRealMessage = previousRealMessages.get(name);
		if (message.equals(lastRealMessage))
		{
			return false;
		}

		previousMessages.put(name, message);

		return true;
	}

	private void setupAblyInstances()
	{
		try
		{
			ClientOptions clientOptions = new ClientOptions();
			clientOptions.authUrl = "https://runelite-regionchat.herokuapp.com/token";
			ablyRealtime = new AblyRealtime(clientOptions);
		}
		catch (AblyException e)
		{
			e.printStackTrace();
		}
	}

	public void connectToRegion(Region region, String world)
	{
		String newChannelName = CHANNEL_NAME_PREFIX + ":" + world + ":" + region.getName();

		if (changingChannels)
		{
			return;
		}

		if (ablyRegionChannel != null && ablyRegionChannel.name.equals(newChannelName))
		{
			if (ablyRegionChannel.state == ChannelState.detached)
			{
				subscribeToChannel();
			}

			return;
		}

		changingChannels = true;

		if (ablyRegionChannel == null)
		{
			ablyRegionChannel = ablyRealtime.channels.get(newChannelName);
			setupAlerts(region);
			subscribeToChannel();
			return;
		}

		try
		{
			ablyRegionChannel.unsubscribe();
			ablyRegionChannel.detach(detatchListener(newChannelName, region));
		}
		catch (AblyException err)
		{
			changingChannels = false;
			System.err.println(err.getMessage());
		}
	}

	private void setupAlerts(Region region)
	{
		ablyRegionChannel.on(ChannelEvent.attached, stateChange -> {
			if (!config.shouldShowStateChanges())
			{
				return;
			}

			final ChatMessageBuilder chatMessageBuilder = new ChatMessageBuilder()
				.append("Entered new Region Chat area: " + region.getName());

			chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.ENGINE)
				.runeLiteFormattedMessage(chatMessageBuilder.build())
				.build());
		});

		ablyRegionChannel.on(ChannelEvent.detached, stateChange -> {
			if (!config.shouldShowStateChanges())
			{
				return;
			}

			final ChatMessageBuilder chatMessageBuilder = new ChatMessageBuilder()
				.append("Left Region Chat area: " + region.getName());

			chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.ENGINE)
				.runeLiteFormattedMessage(chatMessageBuilder.build())
				.build());
		});
	}

	public CompletionListener detatchListener(String newChannelName, Region region)
	{
		return new CompletionListener()
		{
			@Override
			public void onSuccess()
			{
				ablyRegionChannel = ablyRealtime.channels.get(newChannelName);
				setupAlerts(region);
				subscribeToChannel();
			}

			@Override
			public void onError(ErrorInfo reason)
			{
				System.err.println(reason.message);
				changingChannels = false;
			}
		};
	}

	public void disconnectFromRegions()
	{
		if (ablyRegionChannel != null && ablyRegionChannel.state == ChannelState.attached)
		{
			try
			{
				ablyRegionChannel.unsubscribe();
				ablyRegionChannel.detach();
			}
			catch(AblyException err)
			{
				System.err.println(err.getMessage());
			}
		}
	}

	private void subscribeToChannel()
	{
		try
		{
			ablyRegionChannel.subscribe(this::handleMessage);
		}
		catch(AblyException err)
		{
			System.err.println(err.getMessage());
		}

		changingChannels = false;
	}

	// Checks for bits someone could insert in to be icons
	// Important in case it's a JMod icon or something
	private boolean isInvalidUsername(String username)
	{
		return username.toLowerCase().contains("<") || username.toLowerCase().startsWith("mod ");
	}

	public void printInfo(MenuEntry menuEntry)
	{
		final ChatMessageBuilder chatMessageBuilder = new ChatMessageBuilder()
			.append("This is a message from the Region Chat Runelite Plugin.");

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.ENGINE)
			.runeLiteFormattedMessage(chatMessageBuilder.build())
			.build());
	}
}
