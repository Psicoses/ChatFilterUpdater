package com.chatfilterupdater;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("chatfilterupdater")
public interface ChatFilterUpdaterConfig extends Config
{
	@ConfigItem(
			keyName = "filterURL",
			name = "Filter URL",
			description = "URL of the filter regex to pull from",
			position = 0
	)
	default String filterURL() {
		return "https://raw.githubusercontent.com/IamReallyOverrated/Runelite_ChatFilter/master/Chatfilter";
	}
}
