package com.nylo;

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
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Nylo highlights",
	enabledByDefault = false
)
public class nyloPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private nyloOverlay nyloOverlay;
	@Inject
	private nyloConfig config;
	private nyloPanel panel;
	private NavigationButton navButton;

	final int NYLO_ROOM = 13122;
	final List<Integer> meleeIds = Arrays.asList(8342,8345,10791,10794);
	final List<Integer> aggro_meleeIds = Arrays.asList(8348,8351,10797,10800);
	final List<Integer> rangeIds = Arrays.asList(8343,8346,10792,10795);
	final List<Integer> aggro_rangeIds = Arrays.asList(8349,8352,10798,10801);
	final List<Integer> mageIds = Arrays.asList(8344,8347,10793,10796);
	final List<Integer> aggro_mageIds = Arrays.asList(8350,8353,10799,10802);
	final List<Integer> verzikNylos = Arrays.asList(8381,8382,8383,10858,10859,10860);

	public nyloPlugin() {
	}

	@Provides
	nyloConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(nyloConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(nyloOverlay);
		// TODO: add infobox and use onInfoboxMenuClicked to toggle wave nylos (also aggros?) don't need panel in this case
//		this.panel = this.injector.getInstance(tobPanel.class);
//		BufferedImage icon = ImageUtil.loadImageResource(this.getClass(), "/nylo.png");
//		navButton = NavigationButton.builder().tooltip("Nylo").icon(icon).priority(3).panel(this.panel).build();
//		if (this.inNylo()) {
//			this.clientToolbar.addNavigation(this.navButton);
//		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(nyloOverlay);
//		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{

	}

	public boolean inNylo() {
		return client.getMapRegions() != null && ArrayUtils.contains(client.getMapRegions(), NYLO_ROOM);
	}
}
