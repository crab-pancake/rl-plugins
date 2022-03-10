package com.highlight;

import com.socket.plugins.specs.SpecPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

@SuppressWarnings("unchecked")
public class HighlightPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SpecPlugin.class);
		RuneLite.main(args);
	}
}