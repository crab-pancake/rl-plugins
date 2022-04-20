package com.sbs;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Supplier;

import static com.google.common.base.Predicates.alwaysTrue;

@Slf4j
@PluginDescriptor(
	name = "ice demon sbs",
	enabledByDefault = false
)
public class sbsIceDemonPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private sbsIceDemonConfig config;

	private final ArrayListMultimap<String, Integer> optionIndexes = ArrayListMultimap.create();

	private final Multimap<String, Swap> swaps = LinkedHashMultimap.create();

	public sbsIceDemonPlugin() {
	}

	private boolean IS_ICE_DEMON = true;

	@Provides
	sbsIceDemonConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(sbsIceDemonConfig.class);
	}


	@Override
	protected void startUp() throws Exception
	{
		if (config.debug()){
		}
		setupSwaps();
	}

	@Override
	protected void shutDown() throws Exception
	{
		swaps.clear();
		IS_ICE_DEMON = false;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged a){
		if (a.getGameState() != GameState.LOGGED_IN){
			return;
		}
		if (config.debug()){
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "logged in", null);
		}
		// currently in game: what do?
	}

	void setupSwaps() {
		if (config.debug()){
			log.debug("sbs setting up swaps");
		}
		SpellbookSwapMode book = IS_ICE_DEMON ? SpellbookSwapMode.STANDARD : SpellbookSwapMode.ARCEUUS;

		for (SpellbookSwapMode mode : SpellbookSwapMode.class.getEnumConstants())
		{
			swaps.put("Cast", new Swap(
					alwaysTrue(),
					mode.checkTarget(),
					mode.getOption().toLowerCase(),
					() -> (!mode.checkShift() || !shiftModifier()) & mode == book,
					mode.strict()
			));
		}
	}

	private <T extends Enum<?> & SwapMode> void swapMode(String option, Class<T> mode, Supplier<T> enumGet)
	{
		for (T e : mode.getEnumConstants())
		{
			swaps.put(option, new Swap(
					alwaysTrue(),
					e.checkTarget(),
					e.getOption().toLowerCase(),
					() -> (!e.checkShift() || (e.checkShift() && !shiftModifier())) & e == enumGet.get(),
					e.strict()
			));
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event) {
		if (config.debug()){
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "npc spawned"+event.getNpc().getName(), null);
		}
		if (client.getVarbitValue(Varbits.IN_RAID) == 1 && (event.getNpc().getId() == 7584 ||  event.getNpc().getId() == 7585)) {
			if (config.debug()){
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "dormant ice demon has spawned", null);
			}
			IS_ICE_DEMON = true;
			setupSwaps();
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event) {
		if (config.debug()){
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "npc despawned"+event.getNpc().getName(), null);
		}
		if (client.getVarbitValue(Varbits.IN_RAID) == 1 && event.getNpc().getId() == 7585) {
			if (config.debug()){
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "active ice demon has disappeared", null);
			}
			IS_ICE_DEMON = false;
			setupSwaps();
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged e){
		if (config.debug()){
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "animation changed"+e.getActor().getName(), null);
		}
		if(client.getVarbitValue(Varbits.IN_RAID) == 1 && e.getActor().getName() != null && Objects.equals(e.getActor().getName(), "Ice Demon") && e.getActor().getAnimation() == 67){
			if (config.debug()){
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "ice demon death animation seen", null);
			}
			IS_ICE_DEMON = false;
			setupSwaps();
		}
	}

	private void swapMenuEntry(int index, MenuEntry menuEntry) {
		final int eventId = menuEntry.getIdentifier();
		final MenuAction menuAction = menuEntry.getType();
		final String option = Text.removeTags(menuEntry.getOption()).toLowerCase();
		final String target = Text.removeTags(menuEntry.getTarget()).toLowerCase();

		if (shiftModifier() && (menuAction == MenuAction.ITEM_FIRST_OPTION
				|| menuAction == MenuAction.ITEM_SECOND_OPTION
				|| menuAction == MenuAction.ITEM_THIRD_OPTION
				|| menuAction == MenuAction.ITEM_FOURTH_OPTION
				|| menuAction == MenuAction.ITEM_FIFTH_OPTION
				|| menuAction == MenuAction.ITEM_USE)) {
			// don't perform swaps on items when shift is held; instead prefer the client menu swap, which
			// we may have overwrote
			return;
		}

		Collection<Swap> swaps = this.swaps.get(option);
		for (Swap swap : swaps) {
			if (swap.getTargetPredicate().test(target) && swap.getEnabled().get()) {
				if (swap(swap.getSwappedOption(), target, index, swap.isStrict())) {
					break;
				}
			}
		}
	}

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		// The menu is not rebuilt when it is open, so don't swap or else it will
		// repeatedly swap entries
		if (client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen() || !IS_ICE_DEMON)
		{
			return;
		}

		MenuEntry[] menuEntries = client.getMenuEntries();

		// Build option map for quick lookup in findIndex
		int idx = 0;
		optionIndexes.clear();
		for (MenuEntry entry : menuEntries)
		{
			String option = Text.removeTags(entry.getOption()).toLowerCase();
			optionIndexes.put(option, idx++);
		}

		// Perform swaps
		idx = 0;
		for (MenuEntry entry : menuEntries)
		{
			swapMenuEntry(idx++, entry);
		}
	}

//	@Subscribe
//	public void onGameTick(GameTick gameTick) {
//		if (this.client.getVar(Varbits.IN_RAID) == 0) {
//			try {
//				if (IS_ICE_DEMON) {
//					IS_ICE_DEMON = false;
//				}
//				this.shutDown();
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
//		return;
//		}
//		;
//	}

	private boolean swap(String option, String target, int index, boolean strict)
	{
		MenuEntry[] menuEntries = client.getMenuEntries();

		// find option to swap with
		int optionIdx = findIndex(menuEntries, index, option, target, strict);

		if (optionIdx >= 0)
		{
			swap(optionIndexes, menuEntries, optionIdx, index);
			return true;
		}

		return false;
	}

	private int findIndex(MenuEntry[] entries, int limit, String option, String target, boolean strict)
	{
		if (strict)
		{
			List<Integer> indexes = optionIndexes.get(option);

			// We want the last index which matches the target, as that is what is top-most
			// on the menu
			for (int i = indexes.size() - 1; i >= 0; --i)
			{
				int idx = indexes.get(i);
				MenuEntry entry = entries[idx];
				String entryTarget = Text.removeTags(entry.getTarget()).toLowerCase();

				// Limit to the last index which is prior to the current entry
				if (idx < limit && entryTarget.equals(target))
				{
					return idx;
				}
			}
		}
		else
		{
			// Without strict matching we have to iterate all entries up to the current limit...
			for (int i = limit - 1; i >= 0; i--)
			{
				MenuEntry entry = entries[i];
				String entryOption = Text.removeTags(entry.getOption()).toLowerCase();
				String entryTarget = Text.removeTags(entry.getTarget()).toLowerCase();

				if (entryOption.contains(option.toLowerCase()) && entryTarget.equals(target))
				{
					return i;
				}
			}

		}

		return -1;
	}

	private void swap(ArrayListMultimap<String, Integer> optionIndexes, MenuEntry[] entries, int index1, int index2)
	{
		MenuEntry entry1 = entries[index1],
				entry2 = entries[index2];

		entries[index1] = entry2;
		entries[index2] = entry1;

		client.setMenuEntries(entries);

		// Update optionIndexes
		String option1 = Text.removeTags(entry1.getOption()).toLowerCase(),
				option2 = Text.removeTags(entry2.getOption()).toLowerCase();

		List<Integer> list1 = optionIndexes.get(option1),
				list2 = optionIndexes.get(option2);

		// call remove(Object) instead of remove(int)
		list1.remove((Integer) index1);
		list2.remove((Integer) index2);

		sortedInsert(list1, index2);
		sortedInsert(list2, index1);
	}

	private static <T extends Comparable<? super T>> void sortedInsert(List<T> list, T value) // NOPMD: UnusedPrivateMethod: false positive
	{
		int idx = Collections.binarySearch(list, value);
		list.add(idx < 0 ? -idx - 1 : idx, value);
	}

	private boolean shiftModifier() {
		return this.client.isKeyPressed(81);
	}

}
