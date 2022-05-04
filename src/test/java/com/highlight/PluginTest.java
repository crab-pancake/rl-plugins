package com.highlight;

import com.neverlog.NeverLog;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

@SuppressWarnings("unchecked")
public class PluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SpoonNpcHighlightPlugin.class);
		RuneLite.main(args);
	}
}