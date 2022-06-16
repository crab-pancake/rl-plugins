// 
// Decompiled by Procyon v0.5.36
// 

package com.highlight;

import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.Polygon;

import net.runelite.api.*;

import java.time.Instant;
import net.runelite.client.ui.overlay.OverlayUtil;

import java.util.*;

import net.runelite.api.coords.LocalPoint;
import java.awt.Shape;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.client.ui.overlay.Overlay;

public class SpoonNpcHighlightOverlay extends Overlay
{
    private final Client client;
    private final SpoonNpcHighlightPlugin plugin;
    private final SpoonNpcHighlightConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private final Random random;
    private final List<Integer> instances = Arrays.asList(
            5536, //hydra
            9033, //kbd
            9116, //kraken
            11346,11347,11602,11603, //gwd
            13972 //kq
            );

    @Inject
    private SpoonNpcHighlightOverlay(final Client client, final SpoonNpcHighlightPlugin plugin, final SpoonNpcHighlightConfig config, final ModelOutlineRenderer modelOutlineRenderer) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setPriority(OverlayPriority.HIGH);
        this.setLayer(OverlayLayer.ABOVE_SCENE);
        random = new Random();
    }
    
    public Dimension render(final Graphics2D graphics) {
        for (final NPC npc : client.getNpcs()) {
            if (npc.getName() == null) continue;

            String text;
            Point textLoc;
            Shape objectClickbox;
            Polygon tilePoly;
            LocalPoint lp;
            int size;
            NPCComposition npcComposition;

            if (!npc.isDead() || plugin.ignoreDeadExclusionList.contains(npc.getName().toLowerCase())) {
                boolean foundNpc = false;
                Color outlineColor = config.highlightColor();
                Color fillColor = config.fillColor();
                if (npc.getInteracting() != null && client.getLocalPlayer() != null && npc.getInteracting().equals(client.getLocalPlayer()) && config.interactingHighlight() != SpoonNpcHighlightConfig.interactingHighlightMode.OFF) {
                    outlineColor = config.interactingColor();
                    fillColor = new Color(config.interactingColor().getRed(), config.interactingColor().getGreen(), config.interactingColor().getBlue(), config.fillColor().getAlpha());
                }
                npcComposition = npc.getTransformedComposition();
                if (npcComposition == null) {
                    continue;
                }
                if (config.tileHighlight() && plugin.checkSpecificList(plugin.tileNames, plugin.tileIds, npc)) {
                    size = npcComposition.getSize();
                    lp = npc.getLocalLocation();
                    if (lp != null && (tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size)) != null) {
                        renderPoly(graphics, outlineColor, fillColor, tilePoly, config.highlightThiCC());
                        foundNpc = true;
                    }
                }

                if (config.trueTileHighlight() && plugin.checkSpecificList(plugin.trueTileNames, plugin.trueTileIds, npc)) {
                    size = npcComposition.getSize();
                    lp = LocalPoint.fromWorld(client, npc.getWorldLocation());
                    if (lp != null && (tilePoly = Perspective.getCanvasTileAreaPoly(client, new LocalPoint(lp.getX() + size * 128 / 2 - 64, lp.getY() + size * 128 / 2 - 64), size)) != null) {
                        renderPoly(graphics, outlineColor, fillColor, tilePoly, config.highlightThiCC());
                        foundNpc = true;
                    }
                }
                if (config.swTileHighlight() && plugin.checkSpecificList(plugin.swTileNames, plugin.swTileIds, npc)) {
                    Polygon tilePoly2;
                    size = npcComposition.getSize();
                    lp = npc.getLocalLocation();
                    if (lp != null && (tilePoly2 = Perspective.getCanvasTilePoly(client, new LocalPoint(lp.getX() - (size - 1) * 128 / 2, lp.getY() - (size - 1) * 128 / 2))) != null) {
                        renderPoly(graphics, outlineColor, fillColor, tilePoly2, config.highlightThiCC());
                        foundNpc = true;
                    }
                }
                if (config.hullHighlight() && plugin.checkSpecificList(plugin.hullNames, plugin.hullIds, npc) && (objectClickbox = npc.getConvexHull()) != null) {
                    renderPoly(graphics, outlineColor, fillColor, objectClickbox, config.highlightThiCC());
                    foundNpc = true;
                }
                if (config.areaHighlight() && plugin.checkSpecificList(plugin.areaNames, plugin.areaIds, npc)) {
                    Color areaFill = config.areaFill();
                    graphics.setColor(areaFill);
                    graphics.fill(npc.getConvexHull());
                    foundNpc = true;
                }
                if (config.outlineHighlight() && plugin.checkSpecificList(plugin.outlineNames, plugin.outlineIds, npc)) {
                    modelOutlineRenderer.drawOutline(npc, config.outlineThiCC(), outlineColor, config.outlineFeather());
                    foundNpc = true;
                }
                if (config.turboHighlight() && plugin.checkSpecificList(plugin.turboNames, plugin.turboIds, npc)) {
                    lp = npc.getLocalLocation();
                    size = npcComposition.getSize();
                    tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
                    foundNpc = true;
                    Color raveColor = plugin.turboIds.contains(npc.getId()) ? plugin.turboColors.get(plugin.turboIds.indexOf(npc.getId()) + plugin.turboNames.size()) : Color.WHITE;
                    Color oColor = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), random.nextInt(254) + 1);
                    Color fColor = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), random.nextInt(254) + 1);
                    if (raveColor == Color.WHITE && npc.getName() != null) {
                        String name = npc.getName().toLowerCase();
                        int index = 0;
                        for (String str : plugin.turboNames) {
                            if (str.equalsIgnoreCase(name) || str.contains("*") && (str.startsWith("*") && str.endsWith("*") && name.contains(str.replace("*", "")) || str.startsWith("*") && name.endsWith(str.replace("*", "")) || name.startsWith(str.replace("*", "")))) {
                                raveColor = plugin.turboColors.get(index);
                                oColor = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), random.nextInt(254) + 1);
                                fColor = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), random.nextInt(254) + 1);
                                break;
                            }
                            ++index;
                        }
                    }
                    if (plugin.turboModeStyle == SpoonNpcHighlightConfig.tagStyleMode.TILE) {
                        if (tilePoly != null) {
                            renderPoly(graphics, oColor, fColor, tilePoly, plugin.turboTileWidth);
                        }
                    } else if (plugin.turboModeStyle == SpoonNpcHighlightConfig.tagStyleMode.TRUE_TILE) {
                        lp = LocalPoint.fromWorld(client, npc.getWorldLocation());
                        if (lp != null && (tilePoly = Perspective.getCanvasTileAreaPoly(client, new LocalPoint(lp.getX() + size * 128 / 2 - 64, lp.getY() + size * 128 / 2 - 64), size)) != null) {
                            renderPoly(graphics, oColor, fColor, tilePoly, plugin.turboTileWidth);
                        }
                    } else if (plugin.turboModeStyle == SpoonNpcHighlightConfig.tagStyleMode.SW_TILE) {
                        int y = lp.getY() - (size - 1) * 128 / 2;
                        int x = lp.getX() - (size - 1) * 128 / 2;
                        tilePoly = Perspective.getCanvasTilePoly(client, new LocalPoint(x, y));
                        if (tilePoly != null) {
                            renderPoly(graphics, oColor, fColor, tilePoly, plugin.turboTileWidth);
                        }
                    } else if (plugin.turboModeStyle == SpoonNpcHighlightConfig.tagStyleMode.AREA) {
                        objectClickbox = npc.getConvexHull();
                        if (objectClickbox != null) {
                            renderPoly(graphics, oColor, fColor, objectClickbox, plugin.turboTileWidth);
                        }
                    } else if (plugin.turboModeStyle == SpoonNpcHighlightConfig.tagStyleMode.HULL) {
                        graphics.setColor(fColor);
                        graphics.fill(npc.getConvexHull());
                    } else {  // outline
                        modelOutlineRenderer.drawOutline(npc, plugin.turboTileWidth, oColor, plugin.turboOutlineFeather);
                    }
                }
                if (foundNpc && config.interactingHighlight() == SpoonNpcHighlightConfig.interactingHighlightMode.BOTH && npc.getInteracting() != null && client.getLocalPlayer() != null && npc.getInteracting().getName() != null && npc.getInteracting().getName().equals(client.getLocalPlayer().getName())) {
                    String text2 = npc.getInteracting().getName();
                    textLoc = npc.getCanvasTextLocation(graphics, text2, npc.getLogicalHeight() + 20);
                    if (textLoc == null) continue;
                    Point pointShadow = new Point(textLoc.getX() + 1, textLoc.getY() + 1);
                    OverlayUtil.renderTextLocation(graphics, pointShadow, text2, Color.BLACK);
                    OverlayUtil.renderTextLocation(graphics, textLoc, text2, config.interactingColor());
                    continue;
                }
                if (plugin.namesToDisplay.size() <= 0 || npc.getName() == null || !plugin.namesToDisplay.contains(npc.getName().toLowerCase()) || (textLoc = npc.getCanvasTextLocation(graphics, text = npc.getName(), npc.getLogicalHeight() + 20)) == null) continue;
                Point pointShadow = new Point(textLoc.getX() + 1, textLoc.getY() + 1);
                OverlayUtil.renderTextLocation(graphics, pointShadow, text, Color.BLACK);
                OverlayUtil.renderTextLocation(graphics, textLoc, text, config.highlightColor());
            }
        }
        if (config.respawnTimer() != SpoonNpcHighlightConfig.respawnTimerMode.OFF) {
            if (client.getVarbitValue(Varbits.IN_RAID) != 1 &&
                    (!client.isInInstancedRegion() || Arrays.stream(client.getMapRegions()).anyMatch(instances::contains))) {
                // instances::contains is same as r -> instances.contains(r)
                for (NpcSpawn n : plugin.npcSpawns) {
                    Point textLoc;
                    String text;
                    LocalPoint lp = null;
                    if (n.respawnTime != -1 && n.dead) {
                        if (n.spawnPoint != null) {
                            lp = LocalPoint.fromWorld(client, n.spawnPoint.getX(), n.spawnPoint.getY());

                        } else if (n.spawnLocations.size() > 0) {
                            // temporarily use last spawn location as respawn tiles
                            lp = LocalPoint.fromWorld(client, n.spawnLocations.get(n.spawnLocations.size() - 1));
                        }
                        if (lp == null) {
                            continue;
                        }

                        Color raveColor = plugin.turboIds.contains(n.id) ? plugin.turboColors.get(plugin.turboIds.indexOf(n.id) + plugin.turboNames.size()) : Color.WHITE;
                        LocalPoint centerLp = new LocalPoint(lp.getX() + 128 * (n.size - 1) / 2, lp.getY() + 128 * (n.size - 1) / 2);
                        if (config.respawnTimerBox()) {
                            Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, centerLp, n.size);
                            if (tilePoly != null) {
                                Color oColor = raveColor != Color.WHITE ? new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), random.nextInt(254) + 1) : config.highlightColor();
                                Color fColor = raveColor != Color.WHITE ? new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), random.nextInt(254) + 1) : config.fillColor();
                                if (raveColor == Color.WHITE) {
                                    int index = 0;
                                    for (String str : plugin.turboNames) {
                                        if (str.equalsIgnoreCase(n.name) || str.contains("*") && (str.startsWith("*") && str.endsWith("*") && n.name.contains(str.replace("*", "")) || str.startsWith("*") && n.name.endsWith(str.replace("*", "")) || n.name.startsWith(str.replace("*", "")))) {
                                            raveColor = plugin.turboColors.get(index);
                                            oColor = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), random.nextInt(254) + 1);
                                            fColor = new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), random.nextInt(254) + 1);
                                            break;
                                        }
                                        ++index;
                                    }
                                }
                                renderPoly(graphics, oColor, fColor, tilePoly, raveColor != Color.WHITE ? (double) plugin.turboTileWidth : config.highlightThiCC());
                            }
                        }

                        // check if timer is too high
                        if ((client.getTickCount() - n.diedOnTick) < n.respawnTime){
                            text = "?";
                        }
                        else if (config.respawnTimer() == SpoonNpcHighlightConfig.respawnTimerMode.SECONDS) {
                            Instant now = Instant.now();
                            double sinceLast = (double) (now.toEpochMilli() - plugin.lastTickUpdate.toEpochMilli()) / 1000.0;
                            double baseTick = (double) (n.respawnTime - (client.getTickCount() - n.diedOnTick)) * 0.6;
                            double timeLeft = Math.max(0.0, baseTick - sinceLast);
                            text = String.valueOf(timeLeft);
                            if (text.contains(".")) {
                                text = text.substring(0, text.indexOf(".") + 2);
                            }
                        } else {
                            text = String.valueOf(n.respawnTime - (client.getTickCount() - n.diedOnTick));
                        }

                        textLoc = Perspective.getCanvasTextLocation(client, graphics, centerLp, text, 0);
                        if (textLoc != null) {
                            Point pointShadow = new Point(textLoc.getX() + 1, textLoc.getY() + 1);
                            OverlayUtil.renderTextLocation(graphics, pointShadow, text, Color.BLACK);
                            if (raveColor != Color.WHITE) {
                                OverlayUtil.renderTextLocation(graphics, textLoc, text, new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), random.nextInt(205) + 50));
                            }
                            else {
                                OverlayUtil.renderTextLocation(graphics, textLoc, text, config.respawnTimerColor());
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
            if (config.antiAlias()) {
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
