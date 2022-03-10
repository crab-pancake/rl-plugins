/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.MenuAction
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.ui.overlay.OverlayMenuEntry
 *  net.runelite.client.ui.overlay.OverlayPanel
 *  net.runelite.client.ui.overlay.components.ComponentConstants
 *  net.runelite.client.util.ColorUtil
 *  net.runelite.client.util.QuantityFormatter
 *  net.runelite.client.ws.PartyService
 */
package com.socket.plugins.socketDPS;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.util.Map;
import javax.inject.Inject;

import com.socket.plugins.socketDPS.table.SocketTableAlignment;
import com.socket.plugins.socketDPS.table.SocketTableComponent;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.QuantityFormatter;
import net.runelite.client.ws.PartyService;

class SocketDpsOverlay
extends OverlayPanel {
    private static final DecimalFormat DPS_FORMAT = new DecimalFormat("#0.0");
    private static final int PANEL_WIDTH_OFFSET = 10;
    static final OverlayMenuEntry RESET_ENTRY = new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY, "Reset", "DPS counter");
    private final SocketDpsCounterPlugin socketDpsCounterPlugin;
    private final SocketDpsConfig socketDpsConfig;
    private final Client client;

    @Inject
    SocketDpsOverlay(SocketDpsCounterPlugin socketDpsCounterPlugin, SocketDpsConfig socketDpsConfig, PartyService partyService, Client client) {
        super(socketDpsCounterPlugin);
        this.socketDpsCounterPlugin = socketDpsCounterPlugin;
        this.socketDpsConfig = socketDpsConfig;
        this.client = client;
        this.getMenuEntries().add(RESET_ENTRY);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.socketDpsConfig.displayOverlay() && !this.socketDpsCounterPlugin.getMembers().isEmpty() && this.client.getLocalPlayer() != null) {
            Map<String, Integer> dpsMembers = this.socketDpsCounterPlugin.getMembers();
            this.panelComponent.getChildren().clear();
            int tot = 0;
            String localName = this.client.getLocalPlayer().getName();
            if (dpsMembers.containsKey("Total")) {
                tot = dpsMembers.get("Total");
                dpsMembers.remove("Total");
            }
            SocketTableComponent tableComponent = new SocketTableComponent();
            tableComponent.setColumnAlignments(SocketTableAlignment.LEFT, SocketTableAlignment.RIGHT);
            int maxWidth = 129;
            dpsMembers.forEach((k, v) -> {
                String right = QuantityFormatter.formatNumber(v.intValue());
                if (k.equalsIgnoreCase(this.client.getLocalPlayer().getName()) && this.socketDpsConfig.highlightSelf()) {
                    tableComponent.addRow(ColorUtil.prependColorTag(k, Color.green), ColorUtil.prependColorTag(right, Color.green));
                } else if (this.socketDpsConfig.highlightOtherPlayer() && this.socketDpsCounterPlugin.getHighlights().contains(k.toLowerCase())) {
                    tableComponent.addRow(ColorUtil.prependColorTag(k, this.socketDpsConfig.getHighlightColor()), ColorUtil.prependColorTag(right, this.socketDpsConfig.getHighlightColor()));
                } else {
                    tableComponent.addRow(ColorUtil.prependColorTag(k, Color.white), ColorUtil.prependColorTag(right, Color.white));
                }
            });
            this.panelComponent.setPreferredSize(new Dimension(maxWidth + 10, 0));
            dpsMembers.put("Total", tot);
            if (localName != null && dpsMembers.containsKey(localName) && tot > dpsMembers.get(localName) && this.socketDpsConfig.showTotal()) {
                tableComponent.addRow(ColorUtil.prependColorTag("Total", Color.red), ColorUtil.prependColorTag(dpsMembers.get("Total").toString(), Color.red));
            }
            if (!tableComponent.isEmpty()) {
                this.panelComponent.getChildren().add(tableComponent);
            }
            if (this.socketDpsConfig.backgroundStyle() == SocketDpsConfig.backgroundMode.HIDE) {
                this.panelComponent.setBackgroundColor(null);
            } else if (this.socketDpsConfig.backgroundStyle() == SocketDpsConfig.backgroundMode.STANDARD) {
                this.panelComponent.setBackgroundColor(ComponentConstants.STANDARD_BACKGROUND_COLOR);
            } else if (this.socketDpsConfig.backgroundStyle() == SocketDpsConfig.backgroundMode.CUSTOM) {
                this.panelComponent.setBackgroundColor(new Color(this.socketDpsConfig.backgroundColor().getRed(), this.socketDpsConfig.backgroundColor().getGreen(), this.socketDpsConfig.backgroundColor().getBlue(), this.socketDpsConfig.backgroundColor().getAlpha()));
            }
            return this.panelComponent.render(graphics);
        }
        return null;
    }
}

