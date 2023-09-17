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
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private String regexBefore;

	private final Logger logger = LoggerFactory.getLogger(ChatFilterUpdaterPlugin.class);

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
		updateChatFilter();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!"chatfilterupdater".equals(event.getGroup()))
		{
			return;
		}

		updateChatFilter();
	}

	private void setChatFilterRegex(String regex){
		regexBefore = configManager.getConfiguration("chatfilter", "filteredRegex");
		configManager.setConfiguration("chatfilter", "filteredRegex", regex);
	}

	private void updateChatFilter()
	{
		String patterns = fetchPatternsFromGitHub();
		if (patterns != null && !patterns.isBlank())
		{
			setChatFilterRegex(patterns);
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
			if (!response.isSuccessful()) {
				throw new IOException("Unexpected response code: " + response.code() + ", body: " + response.body().string());
			}

			ResponseBody responseBody = response.body();
			if (responseBody != null) {
				return responseBody.string();
			} else {
				logger.error("Response body is null");
				return null;
			}

		}
		catch (IOException e)
		{
			logger.error("Error fetching patterns from GitHub: " + e.getMessage());
			return null;
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		configManager.setConfiguration("chatfilter", "filteredRegex", regexBefore);
	}

}
