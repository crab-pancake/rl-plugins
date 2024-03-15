package com.cluejuggling;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.grounditems.config.DespawnTimerMode;

@ConfigGroup("cluescrolljuggling")
public interface ClueScrollJugglingConfig extends Config
{
	@ConfigItem(
		keyName = "notificationTime",
		name = "Notification at (s)",
		description = "Time remaining (seconds) on despawn timer to send notification. Set to 0 to disable the notification.",
		position = 0
	)
	default int notificationTime()
	{
		return 60;
	}

	@ConfigItem(
		keyName = "showOnClue",
		name = "Show Timer on Clue",
		description = "Displays timer on the item",
		position = 1
	)
	default boolean showOnClue()
	{
		return true;
	}

	@ConfigItem(
		keyName = "beginnerTimers",
		name = "Beginner timer",
		description = "Create timers for beginner clues on the ground.",
		position = 2
	)
	default boolean beginnerTimers()
	{
		return true;
	}

	@ConfigItem(
		keyName = "easyTimers",
		name = "Easy timer",
		description = "Create timers for easy clues on the ground.",
		position = 3
	)
	default boolean easyTimers()
	{
		return true;
	}

	@ConfigItem(
		keyName = "mediumTimers",
		name = "Medium timer",
		description = "Create timers for medium clues on the ground.",
		position = 4
	)
	default boolean mediumTimers()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hardTimers",
		name = "Hard timer",
		description = "Create timers for hard clues on the ground.",
		position = 5
	)
	default boolean hardTimers()
	{
		return true;
	}

	@ConfigItem(
		keyName = "eliteTimers",
		name = "Elite timer",
		description = "Create timers for elite clues on the ground.",
		position = 6
	)
	default boolean eliteTimers()
	{
		return true;
	}

	@ConfigItem(
		keyName = "masterTimers",
		name = "Master timer",
		description = "Create timers for master clues on the ground.",
		position = 7
	)
	default boolean masterTimers()
	{
		return true;
	}

	@ConfigItem(
		keyName = "despawnTimerMode",
		name = "Despawn timer mode",
		description = "",
		position = 2
	)
	default DespawnTimerMode timerMode()
	{
		return DespawnTimerMode.TICKS;
	}
}
