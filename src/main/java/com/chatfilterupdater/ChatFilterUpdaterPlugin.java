package com.chatfilterupdater;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

@PluginDescriptor(
		name = "Chat Filter Updater",
		description = "Automatically updates the chat filter regex patterns from a URL",
		tags = {"chat", "filter", "update", "spam", "github"}
)
public class ChatFilterUpdaterPlugin extends Plugin
{

	private static final String defaultURL = "https://raw.githubusercontent.com/IamReallyOverrated/Runelite_ChatFilter/master/Chatfilter";

	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	private String regexBefore;

	@Provides
	ChatFilterUpdaterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ChatFilterUpdaterConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		if(provideConfig(configManager).filterURL().isEmpty()){
			configManager.setConfiguration("chatfilterupdater", "filterURL", defaultURL);
		}

		if(!fetchPatternsFromGitHub().isBlank()) {
			setChatFilterRegex(fetchPatternsFromGitHub());

			//Refresh chat after config change to reflect current rules
			client.refreshChat();
		}
	}

	private void setChatFilterRegex(String regex){
		regexBefore = configManager.getConfiguration("chatfilter", "filteredRegex");
		configManager.setConfiguration("chatfilter", "filteredRegex", regex);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!"chatfilterupdater".equals(event.getGroup()))
		{
			return;
		}

		if(!fetchPatternsFromGitHub().isBlank()) {
			setChatFilterRegex(fetchPatternsFromGitHub());

			//Refresh chat after config change to reflect current rules
			client.refreshChat();
		}
	}

	private String fetchPatternsFromGitHub()
	{
		try
		{
			if(provideConfig(configManager).filterURL().isEmpty()){
				return "";
			}
			URL url = new URL(provideConfig(configManager).filterURL());
			try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8")))
			{
				return in.lines().collect(Collectors.joining("\n"));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}


	@Override
	protected void shutDown() throws Exception
	{
		configManager.setConfiguration("chatfilter", "filteredRegex", regexBefore);
	}

}
