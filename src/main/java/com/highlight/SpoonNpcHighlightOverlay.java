// 
// Decompiled by Procyon v0.5.36
// 

package com.highlight;

import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.Polygon;
import net.runelite.api.NPCComposition;

import java.time.Instant;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.api.Point;

import java.util.Objects;
import java.util.Random;
import net.runelite.api.coords.LocalPoint;
import java.awt.Shape;
import net.runelite.api.Perspective;
import java.awt.Color;
import net.runelite.api.NPC;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;

public class SpoonNpcHighlightOverlay extends Overlay
{
    private final Client client;
    private final SpoonNpcHighlightPlugin plugin;
    private final SpoonNpcHighlightConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;
    
    @Inject
    private SpoonNpcHighlightOverlay(final Client client, final SpoonNpcHighlightPlugin plugin, final SpoonNpcHighlightConfig config, final ModelOutlineRenderer modelOutlineRenderer) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setPriority(OverlayPriority.HIGH);
        this.setLayer(OverlayLayer.ABOVE_SCENE);
    }
    
    public Dimension render(final Graphics2D graphics) {
        for (final NPC npc : this.client.getNpcs()) {
            if (!npc.isDead() || this.config.ignoreDeadNpcs().contains(Objects.requireNonNull(npc.getName()).toLowerCase())) {
                boolean foundNpc = false;
                Color outlineColor = this.config.highlightColor();
                Color fillColor = this.config.fillColor();
                Color areaFill = this.config.areaFill();
                if (npc.getInteracting() != null && this.client.getLocalPlayer() != null && npc.getInteracting().equals(this.client.getLocalPlayer()) && this.config.interactingHighlight() != SpoonNpcHighlightConfig.interactingHighlightMode.OFF) {
                    outlineColor = this.config.interactingColor();
                    fillColor = new Color(this.config.interactingColor().getRed(), this.config.interactingColor().getGreen(), this.config.interactingColor().getBlue(), this.config.fillColor().getAlpha());
                }
                final NPCComposition npcComposition = npc.getTransformedComposition();
                if (npcComposition == null) {
                    continue;
                }
                if (this.config.tileHighlight()) {
                    if (this.plugin.tileIds.size() > 0 && this.plugin.tileIds.contains(npc.getId())) {
                        final int size = npcComposition.getSize();
                        final LocalPoint lp = npc.getLocalLocation();
                        if (lp != null) {
                            final Polygon tilePoly = Perspective.getCanvasTileAreaPoly(this.client, lp, size);
                            if (tilePoly != null) {
                                this.renderPoly(graphics, outlineColor, fillColor, tilePoly, this.config.highlightThiCC());
                                foundNpc = true;
                            }
                        }
                    }
                    else if (npc.getName() != null) {
                        final String name = npc.getName().toLowerCase();
                        for (final String str : this.plugin.tileNames) {
                            if (str.equalsIgnoreCase(name) || (str.contains("*") && ((str.startsWith("*") && str.endsWith("*") && name.contains(str.replace("*", ""))) || (str.startsWith("*") && name.endsWith(str.replace("*", ""))) || name.startsWith(str.replace("*", ""))))) {
                                final int size2 = npcComposition.getSize();
                                final LocalPoint lp2 = npc.getLocalLocation();
                                if (lp2 == null) {
                                    continue;
                                }
                                final Polygon tilePoly2 = Perspective.getCanvasTileAreaPoly(this.client, lp2, size2);
                                if (tilePoly2 == null) {
                                    continue;
                                }
                                this.renderPoly(graphics, outlineColor, fillColor, tilePoly2, this.config.highlightThiCC());
                                foundNpc = true;
                            }
                        }
                    }
                }
                if (this.config.trueTileHighlight()) {
                    if (this.plugin.trueTileIds.size() > 0 && this.plugin.trueTileIds.contains(npc.getId())) {
                        final int size = npcComposition.getSize();
                        LocalPoint lp = LocalPoint.fromWorld(this.client, npc.getWorldLocation());
                        if (lp != null) {
                            lp = new LocalPoint(lp.getX() + size * 128 / 2 - 64, lp.getY() + size * 128 / 2 - 64);
                            final Polygon tilePoly = Perspective.getCanvasTileAreaPoly(this.client, lp, size);
                            if (tilePoly != null) {
                                this.renderPoly(graphics, outlineColor, fillColor, tilePoly, this.config.highlightThiCC());
                                foundNpc = true;
                            }
                        }
                    }
                    else if (npc.getName() != null) {
                        final String name = npc.getName().toLowerCase();
                        for (final String str : this.plugin.trueTileNames) {
                            if (str.equalsIgnoreCase(name) || (str.contains("*") && ((str.startsWith("*") && str.endsWith("*") && name.contains(str.replace("*", ""))) || (str.startsWith("*") && name.endsWith(str.replace("*", ""))) || name.startsWith(str.replace("*", ""))))) {
                                final int size2 = npcComposition.getSize();
                                LocalPoint lp2 = LocalPoint.fromWorld(this.client, npc.getWorldLocation());
                                if (lp2 == null) {
                                    continue;
                                }
                                lp2 = new LocalPoint(lp2.getX() + size2 * 128 / 2 - 64, lp2.getY() + size2 * 128 / 2 - 64);
                                final Polygon tilePoly2 = Perspective.getCanvasTileAreaPoly(this.client, lp2, size2);
                                if (tilePoly2 == null) {
                                    continue;
                                }
                                this.renderPoly(graphics, outlineColor, fillColor, tilePoly2, this.config.highlightThiCC());
                                foundNpc = true;
                            }
                        }
                    }
                }
                if (this.config.swTileHighlight()) {
                    if (this.plugin.swTileIds.size() > 0 && this.plugin.swTileIds.contains(npc.getId())) {
                        final int size = npcComposition.getSize();
                        final LocalPoint lp = npc.getLocalLocation();
                        if (lp != null) {
                            final int x = lp.getX() - (size - 1) * 128 / 2;
                            final int y = lp.getY() - (size - 1) * 128 / 2;
                            final Polygon tilePoly3 = Perspective.getCanvasTilePoly(this.client, new LocalPoint(x, y));
                            if (tilePoly3 != null) {
                                this.renderPoly(graphics, outlineColor, fillColor, tilePoly3, this.config.highlightThiCC());
                                foundNpc = true;
                            }
                        }
                    }
                    else if (npc.getName() != null) {
                        final String name = npc.getName().toLowerCase();
                        for (final String str : this.plugin.swTileNames) {
                            if (str.equalsIgnoreCase(name) || (str.contains("*") && ((str.startsWith("*") && str.endsWith("*") && name.contains(str.replace("*", ""))) || (str.startsWith("*") && name.endsWith(str.replace("*", ""))) || name.startsWith(str.replace("*", ""))))) {
                                final int size2 = npcComposition.getSize();
                                final LocalPoint lp2 = npc.getLocalLocation();
                                if (lp2 == null) {
                                    continue;
                                }
                                final int x2 = lp2.getX() - (size2 - 1) * 128 / 2;
                                final int y2 = lp2.getY() - (size2 - 1) * 128 / 2;
                                final Polygon tilePoly4 = Perspective.getCanvasTilePoly(this.client, new LocalPoint(x2, y2));
                                if (tilePoly4 == null) {
                                    continue;
                                }
                                this.renderPoly(graphics, outlineColor, fillColor, tilePoly4, this.config.highlightThiCC());
                                foundNpc = true;
                            }
                        }
                    }
                }
                if (this.config.hullHighlight()) {
                    if (this.plugin.hullIds.size() > 0 && this.plugin.hullIds.contains(npc.getId())) {
                        final Shape objectClickbox = npc.getConvexHull();
                        if (objectClickbox != null) {
                            this.renderPoly(graphics, outlineColor, fillColor, objectClickbox, this.config.highlightThiCC());
                            foundNpc = true;
                        }
                    }
                    else if (npc.getName() != null) {
                        final String name = npc.getName().toLowerCase();
                        for (final String str : this.plugin.hullNames) {
                            if (str.equalsIgnoreCase(name) || (str.contains("*") && ((str.startsWith("*") && str.endsWith("*") && name.contains(str.replace("*", ""))) || (str.startsWith("*") && name.endsWith(str.replace("*", ""))) || name.startsWith(str.replace("*", ""))))) {
                                final Shape objectClickbox2 = npc.getConvexHull();
                                if (objectClickbox2 == null) {
                                    continue;
                                }
                                this.renderPoly(graphics, outlineColor, fillColor, objectClickbox2, this.config.highlightThiCC());
                                foundNpc = true;
                            }
                        }
                    }
                }
                if (this.config.areaHighlight()) {
                    if (this.plugin.areaIds.size() > 0 && this.plugin.areaIds.contains(npc.getId())) {
                        graphics.setColor(areaFill);
                        graphics.fill(npc.getConvexHull());
                        foundNpc = true;
                    }
                    else if (npc.getName() != null) {
                        final String name = npc.getName().toLowerCase();
                        for (final String str : this.plugin.areaNames) {
                            if (str.equalsIgnoreCase(name) || (str.contains("*") && ((str.startsWith("*") && str.endsWith("*") && name.contains(str.replace("*", ""))) || (str.startsWith("*") && name.endsWith(str.replace("*", ""))) || name.startsWith(str.replace("*", ""))))) {
                                graphics.setColor(areaFill);
                                graphics.fill(npc.getConvexHull());
                                foundNpc = true;
                            }
                        }
                    }
                }
                if (this.config.outlineHighlight()) {
                    if (this.plugin.outlineIds.size() > 0 && this.plugin.outlineIds.contains(npc.getId())) {
                        this.modelOutlineRenderer.drawOutline(npc, this.config.outlineThiCC(), outlineColor, this.config.outlineFeather());
                        foundNpc = true;
                    }
                    else if (npc.getName() != null) {
                        final String name = npc.getName().toLowerCase();
                        for (final String str : this.plugin.outlineNames) {
                            if (str.equalsIgnoreCase(name) || (str.contains("*") && ((str.startsWith("*") && str.endsWith("*") && name.contains(str.replace("*", ""))) || (str.startsWith("*") && name.endsWith(str.replace("*", ""))) || name.startsWith(str.replace("*", ""))))) {
                                this.modelOutlineRenderer.drawOutline(npc, this.config.outlineThiCC(), outlineColor, this.config.outlineFeather());
                                foundNpc = true;
                            }
                        }
                    }
                }
                if (this.config.turboHighlight()) {
                    boolean found = false;
                    Color raveColor = Color.WHITE;
                    Color oColor = Color.WHITE;
                    Color fColor = Color.WHITE;
                    if (this.plugin.turboIds.size() > 0 && this.plugin.turboIds.contains(npc.getId())) {
                        raveColor = this.plugin.turboColors.get(this.plugin.turboIds.indexOf(npc.getId()) + this.plugin.turboNames.size());
                        oColor = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), new Random().nextInt(254) + 1);
                        fColor = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), new Random().nextInt(254) + 1);
                        found = true;
                        foundNpc = true;
                    }
                    else if (npc.getName() != null) {
                        final String name2 = npc.getName().toLowerCase();
                        int index = 0;
                        for (final String str2 : this.plugin.turboNames) {
                            if (str2.equalsIgnoreCase(name2) || (str2.contains("*") && ((str2.startsWith("*") && str2.endsWith("*") && name2.contains(str2.replace("*", ""))) || (str2.startsWith("*") && name2.endsWith(str2.replace("*", ""))) || name2.startsWith(str2.replace("*", ""))))) {
                                raveColor = this.plugin.turboColors.get(index);
                                oColor = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), new Random().nextInt(254) + 1);
                                fColor = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), new Random().nextInt(254) + 1);
                                found = true;
                                foundNpc = true;
                            }
                            ++index;
                        }
                    }
                    if (found) {
                        final int rng = this.plugin.turboModeStyle;
                        if (rng == 0) {
                            final int size3 = npcComposition.getSize();
                            final LocalPoint lp3 = npc.getLocalLocation();
                            if (lp3 != null) {
                                final Polygon tilePoly4 = Perspective.getCanvasTileAreaPoly(this.client, lp3, size3);
                                if (tilePoly4 != null) {
                                    this.renderPoly(graphics, oColor, fColor, tilePoly4, this.plugin.turboTileWidth);
                                }
                            }
                        }
                        else if (rng == 1) {
                            final int size3 = npcComposition.getSize();
                            LocalPoint lp3 = LocalPoint.fromWorld(this.client, npc.getWorldLocation());
                            if (lp3 != null) {
                                lp3 = new LocalPoint(lp3.getX() + size3 * 128 / 2 - 64, lp3.getY() + size3 * 128 / 2 - 64);
                                final Polygon tilePoly4 = Perspective.getCanvasTileAreaPoly(this.client, lp3, size3);
                                if (tilePoly4 != null) {
                                    this.renderPoly(graphics, oColor, fColor, tilePoly4, this.plugin.turboTileWidth);
                                }
                            }
                        }
                        else if (rng == 2) {
                            final int size3 = npcComposition.getSize();
                            final LocalPoint lp3 = npc.getLocalLocation();
                            if (lp3 != null) {
                                final int x3 = lp3.getX() - (size3 - 1) * 128 / 2;
                                final int y3 = lp3.getY() - (size3 - 1) * 128 / 2;
                                final Polygon tilePoly5 = Perspective.getCanvasTilePoly(this.client, new LocalPoint(x3, y3));
                                if (tilePoly5 != null) {
                                    this.renderPoly(graphics, oColor, fColor, tilePoly5, this.plugin.turboTileWidth);
                                }
                            }
                        }
                        else if (rng == 3) {
                            final Shape objectClickbox3 = npc.getConvexHull();
                            if (objectClickbox3 != null) {
                                this.renderPoly(graphics, oColor, fColor, objectClickbox3, this.plugin.turboTileWidth);
                            }
                        }
                        else if (rng == 4) {
                            graphics.setColor(fColor);
                            graphics.fill(npc.getConvexHull());
                        }
                        else {
                            this.modelOutlineRenderer.drawOutline(npc, this.plugin.turboTileWidth, oColor, this.plugin.turboOutlineFeather);
                        }
                    }
                }
                if (!foundNpc || this.config.interactingHighlight() != SpoonNpcHighlightConfig.interactingHighlightMode.BOTH || npc.getInteracting() == null || this.client.getLocalPlayer() == null || npc.getInteracting().getName() == null || !npc.getInteracting().getName().equals(this.client.getLocalPlayer().getName())) {
                    continue;
                }
                final String text = npc.getInteracting().getName();
                final Point textLoc = npc.getCanvasTextLocation(graphics, text, npc.getLogicalHeight() + 20);
                if (textLoc == null) {
                    continue;
                }
                final Point pointShadow = new Point(textLoc.getX() + 1, textLoc.getY() + 1);
                OverlayUtil.renderTextLocation(graphics, pointShadow, text, Color.BLACK);
                OverlayUtil.renderTextLocation(graphics, textLoc, text, this.config.interactingColor());
            }
        }
        if (this.config.respawnTimer() != SpoonNpcHighlightConfig.respawnTimerMode.OFF) {
            for (final NpcSpawn n : this.plugin.npcSpawns) {
                if (n.spawnPoint != null && n.respawnTime != -1 && n.dead) {
                    final LocalPoint lp4 = LocalPoint.fromWorld(this.client, n.spawnPoint);
                    if (lp4 == null) {
                        continue;
                    }
                    Color raveColor2 = Color.WHITE;
                    if (config.respawnTimerBox()) {
                        final Polygon tilePoly6 = Perspective.getCanvasTileAreaPoly(this.client, lp4, n.size);
                        if (tilePoly6 != null) {
                            if (this.plugin.turboIds.contains(n.id)) {
                                raveColor2 = this.plugin.turboColors.get(this.plugin.turboIds.indexOf(n.id) + this.plugin.turboNames.size());
                                final Color oColor2 = new Color(raveColor2.getRed(), raveColor2.getGreen(), raveColor2.getBlue(), new Random().nextInt(254) + 1);
                                final Color fColor2 = new Color(raveColor2.getRed(), raveColor2.getGreen(), raveColor2.getBlue(), new Random().nextInt(254) + 1);
                                this.renderPoly(graphics, oColor2, fColor2, tilePoly6, this.plugin.turboTileWidth);
                            } else if (n.name != null) {
                                int index2 = 0;
                                boolean foundTurbo = false;
                                for (final String str : this.plugin.turboNames) {
                                    if (str.equalsIgnoreCase(n.name) || (str.contains("*") && ((str.startsWith("*") && str.endsWith("*") && n.name.contains(str.replace("*", ""))) || (str.startsWith("*") && n.name.endsWith(str.replace("*", ""))) || n.name.startsWith(str.replace("*", ""))))) {
                                        raveColor2 = this.plugin.turboColors.get(index2);
                                        final Color oColor3 = new Color(raveColor2.getRed(), raveColor2.getGreen(), raveColor2.getBlue(), new Random().nextInt(254) + 1);
                                        final Color fColor3 = new Color(raveColor2.getRed(), raveColor2.getGreen(), raveColor2.getBlue(), new Random().nextInt(254) + 1);
                                        this.renderPoly(graphics, oColor3, fColor3, tilePoly6, this.plugin.turboTileWidth);
                                        foundTurbo = true;
                                        break;
                                    }
                                    ++index2;
                                }
                                if (!foundTurbo) {
                                    this.renderPoly(graphics, this.config.highlightColor(), this.config.fillColor(), tilePoly6, this.config.highlightThiCC());
                                }
                            }
                        }
                    }
                    String text2;
                    if (this.config.respawnTimer() == SpoonNpcHighlightConfig.respawnTimerMode.SECONDS) {
                        final Instant now = Instant.now();
                        final double baseTick = (n.respawnTime - (this.client.getTickCount() - n.diedOnTick)) * 0.6;
                        final double sinceLast = (now.toEpochMilli() - this.plugin.lastTickUpdate.toEpochMilli()) / 1000.0;
                        final double timeLeft = Math.max(0.0, baseTick - sinceLast);
                        text2 = String.valueOf(timeLeft);
                        if (text2.contains(".")) {
                            text2 = text2.substring(0, text2.indexOf(".") + 2);
                        }
                    }
                    else {
                        text2 = String.valueOf(n.respawnTime - (this.client.getTickCount() - n.diedOnTick));
                    }
                    final Point textLoc2 = Perspective.getCanvasTextLocation(this.client, graphics, lp4, text2, 0);
                    if (textLoc2 == null) {
                        continue;
                    }
                    final Point pointShadow2 = new Point(textLoc2.getX() + 1, textLoc2.getY() + 1);
                    OverlayUtil.renderTextLocation(graphics, pointShadow2, text2, Color.BLACK);
                    if (raveColor2 != Color.WHITE) {
                        OverlayUtil.renderTextLocation(graphics, textLoc2, text2, new Color(raveColor2.getRed(), raveColor2.getGreen(), raveColor2.getBlue(), new Random().nextInt(205) + 50));
                    }
                    else {
                        OverlayUtil.renderTextLocation(graphics, textLoc2, text2, this.config.highlightColor());
                    }
                }
            }
        }
        return null;
    }
    
    private void renderPoly(final Graphics2D graphics, final Color outlineColor, final Color fillColor, final Shape polygon, final double width) {
        if (polygon != null) {
            if (this.config.antiAlias()) {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            else {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }
            graphics.setColor(outlineColor);
            graphics.setStroke(new BasicStroke((float)width));
            graphics.draw(polygon);
            graphics.setColor(fillColor);
            graphics.fill(polygon);
        }
    }

    public static String to_mmss(final int ticks) {
        final int m = ticks / 100;
        final int s = (ticks - m * 100) * 6 / 10;
        final String timeStr = m + ((s < 10) ? ":0" : ":") + s;
        return String.valueOf((ticks - ticks / 100 * 100) * 6 / 10);
    }
}
