package com.nylo;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.*;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.awt.*;

public class nyloOverlay extends Overlay {
    private final Client client;
    private final nyloPlugin plugin;
    private final nyloConfig config;

    @Inject
    private nyloOverlay(final Client client, final nyloPlugin plugin, final nyloConfig config) {
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
                    final NPCComposition npcComposition = npc.getTransformedComposition();
                    if (npcComposition == null) {
                        continue;
                    }
                    Color colour;
                    if ((this.plugin.meleeAggro && this.plugin.aggro_meleeIds.contains(npc.getId()))
                            ||(this.plugin.rangeAggro && this.plugin.aggro_rangeIds.contains(npc.getId()))
                            ||(this.plugin.mageAggro && this.plugin.aggro_mageIds.contains(npc.getId()))){
                        colour = config.aggroColour();
                    }
                    else if (this.plugin.meleeHighlight && (this.plugin.meleeIds.contains(npc.getId()) || this.plugin.aggro_meleeIds.contains(npc.getId()))) {
                        colour = config.meleeColour();
                    }
                    else if (this.plugin.rangeHighlight && (this.plugin.rangeIds.contains(npc.getId()) || this.plugin.aggro_rangeIds.contains(npc.getId()))) {
                        colour = config.rangeColour();
                    }
                    else if (this.plugin.mageHighlight && (this.plugin.mageIds.contains(npc.getId()) || this.plugin.aggro_mageIds.contains(npc.getId()))) {
                        colour = config.mageColour();
                    }
                    else continue;

                    final int size = npcComposition.getSize();
                    final LocalPoint lp = npc.getLocalLocation();
                    Color fill = new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), colour.getAlpha() * config.fillOpacity());
                    if (lp != null) {

                        final Polygon tilePoly = Perspective.getCanvasTileAreaPoly(this.client, lp, size);
                        if (tilePoly != null) {
                            this.renderPoly(graphics, colour, fill, tilePoly, this.config.thickness());
                        }
                    }
                }
            }
        }

        if (this.config.meowdy()){
            if (this.client.getLocalPlayer() != null && ArrayUtils.contains(this.client.getMapRegions(), 12611)){
                for (final NPC npc : this.client.getNpcs()) {
                    if (this.plugin.verzikNylos.contains(npc.getId())) {
                        final NPCComposition npcComposition = npc.getTransformedComposition();
                        if (npcComposition == null) {
                            continue;
                        }
                        boolean targeted = false;
                        if (npc.getInteracting() != null && this.client.getLocalPlayer() != null) {
                            if (npc.getInteracting().equals(this.client.getLocalPlayer())){
                                targeted = true;
                                final int size = npcComposition.getSize();
                                final LocalPoint lp = npc.getLocalLocation();
                                if (lp != null) {
                                    final Polygon tilePoly = Perspective.getCanvasTileAreaPoly(this.client, lp, size);
                                    if (tilePoly != null) {
                                        this.renderPoly(graphics, this.config.aggroColour(), new Color(0,0,0,0), tilePoly, this.config.thickness());
                                    }
                                }
                            }
                        }
                        final String text = npc.getInteracting().getName();
                        final Point textLoc = npc.getCanvasTextLocation(graphics, text, npc.getLogicalHeight() + 20);
                        if (textLoc == null) {
                            continue;
                        }
                        final Point pointShadow = new Point(textLoc.getX() + 1, textLoc.getY() + 1);
                        OverlayUtil.renderTextLocation(graphics, pointShadow, text, Color.BLACK);
                        OverlayUtil.renderTextLocation(graphics, textLoc, text, targeted ? this.config.aggroColour() : Color.WHITE);
                    }
                }
            }
        }

        return null;
    }

    private void renderPoly(final Graphics2D graphics, final Color outlineColor, final Color fillColor, final Shape polygon, final double width) {
        if (polygon != null) {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(outlineColor);
            graphics.setStroke(new BasicStroke((float) width));
            graphics.draw(polygon);
            graphics.setColor(fillColor);
            graphics.fill(polygon);
        }
    }
}
