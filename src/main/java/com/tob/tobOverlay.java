package com.tob;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.*;

public class tobOverlay extends Overlay {
    private final Client client;
    private final tobPlugin plugin;
    private final tobConfig config;

    @Inject
    private tobOverlay(final Client client, final tobPlugin plugin, final tobConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setPriority(OverlayPriority.HIGH);
        this.setLayer(OverlayLayer.ABOVE_SCENE);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.plugin.inNylo()) {
            for (final NPC npc : this.client.getNpcs()) {
                if (!npc.isDead()) {
//                    if (npc.getInteracting() != null && this.client.getLocalPlayer() != null && npc.getInteracting().equals(this.client.getLocalPlayer()) && this.config.interactingHighlight() != SpoonNpcHighlightConfig.interactingHighlightMode.OFF) {
//                        outlineColor = this.config.interactingColor();
//                        fillColor = new Color(this.config.interactingColor().getRed(), this.config.interactingColor().getGreen(), this.config.interactingColor().getBlue(), this.config.fillColor().getAlpha());
//                    }
                    final NPCComposition npcComposition = npc.getTransformedComposition();
                    if (npcComposition == null) {
                        continue;
                    }
                    if (this.config.range() && this.plugin.rangeIds.contains(npc.getId())) {
                        final int size = npcComposition.getSize();
                        final LocalPoint lp = npc.getLocalLocation();
                        if (lp != null) {
                            final Polygon tilePoly = Perspective.getCanvasTileAreaPoly(this.client, lp, size);
                            if (tilePoly != null) {
                                this.renderPoly(graphics, config.rangeColour(), new Color(0,0,0,0), tilePoly, this.config.thickness());
                            }
                        }
                    }
                    else if (this.config.mage() && this.plugin.mageIds.contains(npc.getId())) {
                        final int size = npcComposition.getSize();
                        final LocalPoint lp = npc.getLocalLocation();
                        if (lp != null) {
                            final Polygon tilePoly = Perspective.getCanvasTileAreaPoly(this.client, lp, size);
                            if (tilePoly != null) {
                                this.renderPoly(graphics, config.mageColour(), new Color(0,0,0,0), tilePoly, this.config.thickness());
                            }
                        }
                    }
                    else if (this.config.melee() && this.plugin.meleeIds.contains(npc.getId())) {
                        final int size = npcComposition.getSize();
                        final LocalPoint lp = npc.getLocalLocation();
                        if (lp != null) {
                            final Polygon tilePoly = Perspective.getCanvasTileAreaPoly(this.client, lp, size);
                            if (tilePoly != null) {
                                this.renderPoly(graphics, config.meleeColour(), new Color(0,0,0,0), tilePoly, this.config.thickness());
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void renderPoly(final Graphics2D graphics, final Color outlineColor, final Color fillColor, final Shape polygon, final double width) {
        if (polygon != null) {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            graphics.setColor(outlineColor);
            graphics.setStroke(new BasicStroke((float)width));
            graphics.draw(polygon);
            graphics.setColor(fillColor);
            graphics.fill(polygon);
        }
    }
}
