package com.highlight;

import com.LineOfSight.LineOfSightPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

@SuppressWarnings("unchecked")
public class PluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(LineOfSightPlugin.class);
		RuneLite.main(args);
	}
}