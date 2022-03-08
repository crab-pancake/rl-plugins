package com.tob;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("example")
public interface tobConfig extends Config
{
	@ConfigSection(name = "Line settings", description = "Highlighted line settings", position = 1, closedByDefault = true)
	public static final String line = "settings";

	@ConfigItem(
			keyName = "thickness",
			name = "Thickness",
			description = "Line thickness",
			section = line
	)
	default double thickness(){
		return 2.0;
	}

	@Alpha
	@ConfigItem(
			keyName = "rangeColour",
			name = "Range colour",
			description = "Colour to highlight rangers",
			section = line
	)
	default Color rangeColour(){
		return Color.GREEN;
	}

	@Alpha
	@ConfigItem(
			keyName = "mageColour",
			name = "Mage colour",
			description = "Colour to highlight magers",
			section = line
	)
	default Color mageColour(){
		return Color.BLUE;
	}

	@Alpha
	@ConfigItem(
			keyName = "meleeColour",
			name = "Melee colour",
			description = "Colour to highlight meleers",
			section = line
	)
	default Color meleeColour(){
		return Color.LIGHT_GRAY;
	}

	@Alpha
	@ConfigItem(
			keyName = "interactingColour",
			name = "Interacting colour",
			description = "Colour to highlight things currently hitting you",
			section = line
	)
	default Color interactingColor(){
		return Color.LIGHT_GRAY;
	}

	@ConfigItem(
			keyName = "range",
			name = "Range",
			description = "Highlight range nylos"
	)
	default boolean range()
	{
		return false;
	}

	@ConfigItem(
			keyName = "mage",
			name = "Mage",
			description = "Highlight mage nylos"
	)
	default boolean mage()
	{
		return false;
	}

	@ConfigItem(
			keyName = "melee",
			name = "Melee",
			description = "Highlight melee nylos"
	)
	default boolean melee()
	{
		return false;
	}
}
