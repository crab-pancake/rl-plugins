package com.tob;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Small tob stuff",
	enabledByDefault = false
)
public class tobPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private tobOverlay tobOverlay;
	@Inject
	private tobConfig config;
	private tobPanel panel;
	private NavigationButton navButton;

	final int NYLO_ROOM = 13122;
	final List<Integer> rangeIds = Arrays.asList(8343,8349,8382);
	final List<Integer> mageIds = Arrays.asList(8344,8350,8383);
	final List<Integer> meleeIds = Arrays.asList(8342,8348,8381);

	public tobPlugin() {
	}

	@Provides
	tobConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(tobConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		this.overlayManager.add(tobOverlay);
		this.panel = this.injector.getInstance(tobPanel.class);
		BufferedImage icon = ImageUtil.loadImageResource(this.getClass(), "/nylo.png");
		this.navButton = NavigationButton.builder().tooltip("Nylo").icon(icon).priority(3).panel(this.panel).build();
		if (this.inNylo()) {
			this.clientToolbar.addNavigation(this.navButton);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		this.overlayManager.remove(this.tobOverlay);
		this.clientToolbar.removeNavigation(this.navButton);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{

	}

	public boolean inNylo() {
		return this.client.getMapRegions() != null && ArrayUtils.contains(this.client.getMapRegions(), NYLO_ROOM);
	}
}
