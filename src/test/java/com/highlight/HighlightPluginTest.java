package com.highlight;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class HighlightPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SpoonNpcHighlightPlugin.class);
		RuneLite.main(args);
	}
}