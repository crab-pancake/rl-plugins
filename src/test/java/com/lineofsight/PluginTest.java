package com.lineofsight;

import com.mplayerindicators.MPlayerIndicatorsPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

@SuppressWarnings("unchecked")
public class PluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(MPlayerIndicatorsPlugin.class);
		RuneLite.main(args);
	}
}