package com.dmgtracker;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.LineComponent;

import java.awt.*;
import java.util.List;

public class HitsplatTrackerOverlay extends OverlayPanel {
    private final Client client;
    private final HitsplatTrackerPlugin plugin;
    private HitsplatTrackerConfig config;

    private HitsplatTrackerOverlay(Client client, HitsplatTrackerPlugin plugin){
        this.client = client;
        this.plugin = plugin;
    }

    @Override
    public Dimension render(Graphics2D graphics){
        List<LayoutableRenderableEntity> elems = panelComponent.getChildren();
        elems.clear();
        elems.add(LineComponent.builder().leftColor(Color.white).left("Total hitsplats: ").rightColor(Color.white).right(Integer.toString(plugin.totalHits)).build());
        elems.add(LineComponent.builder().leftColor(Color.white).left("Largest hitsplat: ").rightColor(Color.white).right(Integer.toString(plugin.biggestHit)).build());
        elems.add(LineComponent.builder().leftColor(Color.white).left("Accurate hits: ").rightColor(Color.white).right(Integer.toString(plugin.accurateHits)).build());
        elems.add(LineComponent.builder().leftColor(Color.white).left("Missed hits: ").rightColor(Color.white).right(Integer.toString(plugin.misses)).build());
        elems.add(LineComponent.builder().leftColor(Color.white).left("Approximate accuracy: ").rightColor(Color.white).right(String.format("%05d",(plugin.accurateHits / plugin.totalHits))).build());

        return super.render(graphics);
    }
}
