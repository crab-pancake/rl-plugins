package com.highlight;

import com.mplayerindicators.PlayerIndicatorsConfig;
import com.mplayerindicators.PlayerIndicatorsPlugin;
import com.neverlog.NeverLog;
import com.nylo.nyloPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

@SuppressWarnings("unchecked")
public class PluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PlayerIndicatorsPlugin.class);
		RuneLite.main(args);
	}
}