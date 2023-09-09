package com.chatfilterupdater;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ChatFilterUpdaterPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ChatFilterUpdaterPlugin.class);
		RuneLite.main(args);
	}
}