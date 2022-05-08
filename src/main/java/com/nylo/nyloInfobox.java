package com.nylo;

import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.infobox.InfoBox;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;

import static java.awt.Color.GRAY;
import static java.awt.Color.GREEN;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;

class nyloInfobox extends InfoBox {
    String attackStyle;
    boolean highlight;
    boolean aggroHighlight;

    @Inject
    nyloInfobox(nyloPlugin plugin, BufferedImage image, boolean highlight, boolean aggroHighlight, String attackStyle) // 0 melee, 1 range, 2 mage
    {
        super(image, plugin);
        this.attackStyle = attackStyle;
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, "Toggle highlight", attackStyle+" nylos"));
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, "Toggle aggro", attackStyle+" nylos"));
        this.highlight = highlight;
        this.aggroHighlight = aggroHighlight;
    }

    @Override
    public String getText()
    {
        return highlight ? "On" : "Off";
    }

    @Override
    public Color getTextColor()
    {
        return highlight ? GREEN : GRAY;
    }

    @Override
    public String getTooltip()
    {
        return attackStyle+" aggros: "+(aggroHighlight ? "on" : "off");
    }
}
