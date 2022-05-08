package com.nylo;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("nylohighlight")
public interface nyloConfig extends Config
{
	@ConfigSection(name = "Line settings", description = "Highlighted line settings", position = 1, closedByDefault = true)
	String LINE = "settings";

	@Alpha
	@ConfigItem(
			keyName = "meleeColour",
			name = "Melee colour",
			description = "Colour to highlight meleers",
			position = 1,
			section = LINE
	)
	default Color meleeColour(){
		return new Color(149,140,140,255);
	}

	@Alpha
	@ConfigItem(
			keyName = "rangeColour",
			name = "Range colour",
			description = "Colour to highlight rangers",
			position = 2,
			section = LINE
	)
	default Color rangeColour(){
		return new Color(100,132,38,255);
	}

	@Alpha
	@ConfigItem(
			keyName = "mageColour",
			name = "Mage colour",
			description = "Colour to highlight magers",
			position = 3,
			section = LINE
	)
	default Color mageColour(){
		return new Color(62,238,232,255);
	}

	@Alpha
	@ConfigItem(
			keyName = "aggroColour",
			name = "Aggro colour",
			description = "Colour to highlight aggros inside the room",
			position = 4,
			section = LINE
	)
	default Color aggroColour(){
		return Color.RED;
	}
	@ConfigItem(
			keyName = "thickness",
			name = "Thickness",
			description = "Line thickness",
			position = 5,
			section = LINE
	)
	default double thickness(){
		return 1.5;
	}

	@Alpha
	@ConfigItem(
			keyName = "fillOpacity",
			name = "Fill Opacity",
			description = "Fill opacity as % of outline colour",
			position = 6,
			section = LINE
	)
	default int fillOpacity(){
		return 0;
	}


	@ConfigItem(
			keyName = "melee",
			name = "Melee",
			description = "Highlight melee nylos",
			position = 1
	)
	default boolean melee()
	{
		return false;
	}

	@ConfigItem(
			keyName = "range",
			name = "Range",
			description = "Highlight range nylos",
			position = 2
	)
	default boolean range()
	{
		return false;
	}

	@ConfigItem(
			keyName = "mage",
			name = "Mage",
			description = "Highlight mage nylos",
			position = 3
	)
	default boolean mage()
	{
		return false;
	}

	@ConfigItem(
			keyName = "meleeAggro",
			name = "Melee aggros",
			description = "Highlight melee aggros a different colour",
			position = 4
	)
	default boolean meleeAggro()
	{
		return false;
	}

	@ConfigItem(
			keyName = "rangeAggro",
			name = "Range aggros",
			description = "Highlight range aggros a different colour",
			position = 5
	)
	default boolean rangeAggro()
	{
		return false;
	}

	@ConfigItem(
			keyName = "mageAggro",
			name = "Mage aggros",
			description = "Highlight mage aggros a different colour",
			position = 6
	)
	default boolean mageAggro()
	{
		return false;
	}

	@ConfigItem(
			keyName = "meowdy",
			name = "Meowdy",
			description = "",
			position = 7
	)
	default boolean meowdy()
	{
		return false;
	}

	@ConfigItem(
			keyName = "debug",
			name = "Debug",
			description = "",
			position = 8
	)
	default boolean debug()
	{
		return false;
	}
}
