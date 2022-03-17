/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.NPC
 *  net.runelite.api.NPCComposition
 *  net.runelite.api.Point
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.OverlayUtil
 *  net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
 */
package com.highlight;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class SpoonNpcMinimapOverlay extends Overlay {
    private final Client client;
    private final SpoonNpcHighlightPlugin plugin;
    private final SpoonNpcHighlightConfig config;

    @Inject
    private SpoonNpcMinimapOverlay(Client client, SpoonNpcHighlightPlugin plugin, SpoonNpcHighlightConfig config, ModelOutlineRenderer modelOutlineRenderer) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    public Dimension render(Graphics2D graphics) {
        for (NPC npc : client.getNpcs()) {
            if (npc.getName() == null || config.npcMinimapMode() == SpoonNpcHighlightConfig.npcMinimapMode.OFF) continue;

            String name = npc.getName().toLowerCase();
            ArrayList<ArrayList<String>> allLists = new ArrayList<>(Arrays.asList(plugin.tileNames, plugin.trueTileNames, plugin.swTileNames, plugin.hullNames, plugin.areaNames, plugin.outlineNames, plugin.turboNames));
            block1: for (ArrayList<String> strList : allLists) {
                for (String str : strList) {
                    Point minimapLocation;
                    if (!str.equalsIgnoreCase(name) && (!str.contains("*") || !(str.startsWith("*") && str.endsWith("*") && name.contains(str.replace("*", "")) || str.startsWith("*") && name.endsWith(str.replace("*", ""))) && !name.startsWith(str.replace("*", "")))) continue;
                    NPCComposition npcComposition = npc.getTransformedComposition();
                    if (npcComposition == null || !npcComposition.isInteractible() || (minimapLocation = npc.getMinimapLocation()) == null) continue block1;

                    if (config.npcMinimapMode() == SpoonNpcHighlightConfig.npcMinimapMode.DOT || config.npcMinimapMode() == SpoonNpcHighlightConfig.npcMinimapMode.BOTH) {
                        OverlayUtil.renderMinimapLocation(graphics, minimapLocation, config.highlightColor());
                    }
                    if (config.npcMinimapMode() == SpoonNpcHighlightConfig.npcMinimapMode.NAME || config.npcMinimapMode() == SpoonNpcHighlightConfig.npcMinimapMode.BOTH) {
                        OverlayUtil.renderTextLocation(graphics, minimapLocation, name, config.highlightColor());
                    }
                }
            }
        }
        return null;
    }
}

