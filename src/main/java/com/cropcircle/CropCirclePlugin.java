package com.cropcircle;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Crop Circle tracker",
	enabledByDefault = false
)
public class CropCirclePlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private CropCircleOverlay cropCircleOverlay;
	@Inject
	private CropCircleConfig config;
	@Getter
	private int currentUTCTime;
	@Getter
	private int rotationTime;

	private final List<Integer> fairyregions = Arrays.asList(11829,10288,12339,11826,12598,5177,6967,15148,8757,11318,9782,11058,14638,11573,12595,10548,12852,10044);
	// doric, yanille, draynor, rimmington, cooks guild, battlefront, hosidius, harmony, gwenith, catherby, gnome, brimhaven, mosleharmless, taverley, lumbridge windmill, ardy, champ guild, miscellania

	public CropCirclePlugin() {
	}

	@Provides
	CropCircleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CropCircleConfig.class);
	}

	public final LocalTime getUTCTime() {
		return LocalTime.now(ZoneId.of("UTC"));
	}

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
		this.overlayManager.add(this.cropCircleOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		this.overlayManager.remove(this.cropCircleOverlay);
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			if (this.config.everywhereOverlay())
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Meowdy there " + config.greeting(), null);
			}
			else
			{
				this.reset();
			}
		}
	}

	@Schedule(
			period = 500L,
			unit = ChronoUnit.MILLIS
	)
	public void updateTime() {
		if (this.client.getGameState() == GameState.LOGGED_IN) {
			this.currentUTCTime = this.getUTCTime().getHour() * 60 + this.getUTCTime().getMinute();
			this.rotationTime = this.currentUTCTime % 15;
		}
	}

	public boolean inCropCircleArea(){
		for (int region : fairyregions)
		{
			if (ArrayUtils.contains(this.client.getMapRegions(), region)){
				return true;
			}
		}
		return false;
	}

	public boolean inRegion(int region) {
		return ArrayUtils.contains(this.client.getMapRegions(), region);
	}

	private void reset() {
		this.currentUTCTime = -1;
		this.rotationTime = -1;
	}
}
