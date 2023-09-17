package com.chatfilterupdater;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

	private String regexBefore;

	@Inject
	private OkHttpClient httpClient;

	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

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

			client.refreshChat();
		}
	}

	private String fetchPatternsFromGitHub()
	{
		Request request = new Request.Builder()
				.url(provideConfig(configManager).filterURL())
				.build();

		try (Response response = httpClient.newCall(request).execute())
		{
			if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

			return response.body().string();
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
