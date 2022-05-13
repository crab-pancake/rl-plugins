package com.nylo;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@PluginDescriptor(
	name = "<html><font color=#b20e0e>[M] Nylo highlights",
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
	@Inject
	SkillIconManager skillIconManager;
	@Inject
	InfoBoxManager infoBoxManager;

	BufferedImage meleeIcon;
	BufferedImage rangeIcon;
	BufferedImage mageIcon;

	nyloInfobox meleeInfoBox;
	nyloInfobox mageInfoBox;
	nyloInfobox rangeInfoBox;

	boolean meleeHighlight;
	boolean rangeHighlight;
	boolean mageHighlight;
	boolean meleeAggro;
	boolean rangeAggro;
	boolean mageAggro;
	boolean wasInNylo;

	final int NYLO_ROOM = 13122;
	final List<Integer> meleeIds = Arrays.asList(8342,8345,10791,10794);
	final List<Integer> aggro_meleeIds = Arrays.asList(8348,8351,10797,10800);
	final List<Integer> rangeIds = Arrays.asList(8343,8346,10792,10795);
	final List<Integer> aggro_rangeIds = Arrays.asList(8349,8352,10798,10801);
	final List<Integer> mageIds = Arrays.asList(8344,8347,10793,10796);
	final List<Integer> aggro_mageIds = Arrays.asList(8350,8353,10799,10802);
	final List<Integer> verzikNylos = Arrays.asList(8381,8382,8383,10858,10859,10860);

	@Provides
	nyloConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(nyloConfig.class);
	}

	@Override
	public void startUp(){
		meleeIcon = skillIconManager.getSkillImage(Skill.ATTACK);
		rangeIcon = skillIconManager.getSkillImage(Skill.RANGED);
		mageIcon = skillIconManager.getSkillImage(Skill.MAGIC);

		meleeHighlight = config.melee();
		rangeHighlight = config.range();
		mageHighlight = config.mage();
		meleeAggro = config.meleeAggro();
		rangeAggro = config.rangeAggro();
		mageAggro = config.mageAggro();

		overlayManager.add(nyloOverlay);
	}

	@Override
	public void shutDown(){
		overlayManager.remove(nyloOverlay);
		infoBoxManager.removeInfoBox(meleeInfoBox);
		infoBoxManager.removeInfoBox(rangeInfoBox);
		infoBoxManager.removeInfoBox(mageInfoBox);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged){
		if (Objects.equals(configChanged.getGroup(), "nylohighlight")){
			if (Objects.equals(configChanged.getKey(), "debug")){
				updateInfoBoxes(-2);
				return;
			}
			int changed = -1;
			boolean newval = Boolean.parseBoolean(configChanged.getNewValue());
			switch (configChanged.getKey()){
				case ("melee"):
					meleeHighlight = newval;
					changed = 0;
					break;
				case ("meleeAggro"):
					meleeAggro = newval;
					changed = 0;
					break;
				case ("range"):
					rangeHighlight = newval;
					changed = 1;
					break;
				case ("rangeAggro"):
					rangeAggro = newval;
					changed = 1;
					break;
				case ("mage"):
					mageHighlight = newval;
					changed = 2;
					break;
				case ("mageAggro"):
					mageAggro = newval;
					changed = 2;
					break;
			}
			if (changed != -1 && this.inNylo()){
				updateInfoBoxes(changed);
			}
		}
	}

	@Subscribe
	public void onOverlayMenuClicked(OverlayMenuClicked event){
		OverlayMenuEntry entry = event.getEntry();
		if (entry.getMenuAction() == MenuAction.RUNELITE_OVERLAY_CONFIG) {
			int changed = -1;
			if (Objects.equals(entry.getTarget(), "Melee nylos")) {
				if (entry.getOption().equals("Toggle highlight")) {
						meleeHighlight = !meleeHighlight;
						changed = 0;
					} else if (event.getEntry().getOption().equals("Toggle aggro")) {
						meleeAggro = !meleeAggro;
						changed = 0;
					}
			}
			else if (Objects.equals(entry.getTarget(), "Range nylos")) {
				if (entry.getOption().equals("Toggle highlight")) {
					rangeHighlight = !rangeHighlight;
					changed = 1;
				} else if (event.getEntry().getOption().equals("Toggle aggro")) {
					rangeAggro = !rangeAggro;
					changed = 1;
				}
			}
			else if (Objects.equals(entry.getTarget(), "Mage nylos")) {
				if (entry.getOption().equals("Toggle highlight")) {
					mageHighlight = !mageHighlight;
					changed = 2;
				} else if (event.getEntry().getOption().equals("Toggle aggro")) {
					mageAggro = !mageAggro;
					changed = 2;
				}
			}
			if (changed != -1 && this.inNylo()) {
				updateInfoBoxes(changed);
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event){
		if (event.getGameState() == GameState.LOGGED_IN) {
			if (this.inNylo()) {
				updateInfoBoxes(-2);
			}
			else {
				infoBoxManager.removeInfoBox(meleeInfoBox);
				infoBoxManager.removeInfoBox(rangeInfoBox);
				infoBoxManager.removeInfoBox(mageInfoBox);
			}
		}
	}

	private void updateInfoBoxes(int i){
		infoBoxManager.removeInfoBox(meleeInfoBox);
		infoBoxManager.removeInfoBox(rangeInfoBox);
		infoBoxManager.removeInfoBox(mageInfoBox);

		switch (i){
			case -2:
				meleeInfoBox = new nyloInfobox(this, meleeIcon, meleeHighlight, meleeAggro, "Melee");
				rangeInfoBox = new nyloInfobox(this, rangeIcon, rangeHighlight, rangeAggro, "Range");
				mageInfoBox = new nyloInfobox(this, mageIcon, mageHighlight, mageAggro, "Mage");
				break;
			case 0:
				meleeInfoBox = new nyloInfobox(this, meleeIcon, meleeHighlight, meleeAggro, "Melee");
				break;
			case 1:
				rangeInfoBox = new nyloInfobox(this, rangeIcon, rangeHighlight, rangeAggro, "Range");
				break;
			case 2:
				mageInfoBox = new nyloInfobox(this, mageIcon, mageHighlight, mageAggro, "Mage");
				break;
		}

		infoBoxManager.addInfoBox(meleeInfoBox);
		infoBoxManager.addInfoBox(rangeInfoBox);
		infoBoxManager.addInfoBox(mageInfoBox);
	}

	public boolean inNylo(){
		if (config.debug()) return true;
		return client.getLocalPlayer() != null && ArrayUtils.contains(client.getMapRegions(), NYLO_ROOM);
	}
}
