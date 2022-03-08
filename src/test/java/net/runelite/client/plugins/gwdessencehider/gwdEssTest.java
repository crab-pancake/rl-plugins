package net.runelite.client.plugins.gwdessencehider;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import net.runelite.client.plugins.gwdessencehider.GwdEssenceHiderPlugin;

public class gwdEssTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(GwdEssenceHiderPlugin.class);
		RuneLite.main(args);
	}
}