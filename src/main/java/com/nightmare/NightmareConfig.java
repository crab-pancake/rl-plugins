package com.nightmare;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("example")
public interface NightmareConfig extends Config
{
	@Alpha
	@ConfigItem(
			position = 0,
			keyName = "warningColour",
			name = "Warning colour",
			description = "Colour of the hand outline if underneath you"
	)
	default Color warningColour()
	{
		return Color.RED;
	}

	@Alpha
	@ConfigItem(
			position = 1,
			keyName = "outlineColour",
			name = "Outline colour",
			description = "Colour of the hand outline otherwise"
	)
	default Color colour()
	{
		return Color.ORANGE;
	}

	@ConfigItem(
			position = 2,
			keyName = "width",
			name = "Outline Width",
			description = "Width of the outline"
	)
	default double width()
	{
		return 1;
	}

	@ConfigItem(
			position = 3,
			keyName = "outlineFeather",
			name = "Outline feather",
			description = "Specify between 0-4 how much of the model outline should be faded"
	)
	@Range(
			min = 0,
			max = 3
	)
	default int outlineFeather()
	{
		return 0;
	}

	@ConfigItem(
			position = 4,
			keyName = "drawDistance",
			name = "Draw distance",
			description = "How far to highlight hands. 0 to highlight all"
	)
	@Range(
			min = 0,
			max = 15
	)
	default int drawDistance()
	{
		return 4;
	}

	@ConfigItem(
			position = 5,
			keyName = "debug",
			name = "Debug info",
			description = "Debug info logged to chatbox"
	)
	default boolean debug()
	{
		return false;
	}

}
