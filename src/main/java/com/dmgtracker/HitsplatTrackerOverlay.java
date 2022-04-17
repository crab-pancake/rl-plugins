package com.dmgtracker;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;

import static com.dmgtracker.AttackStyle.CASTING;
import static com.dmgtracker.AttackStyle.DEFENSIVE_CASTING;

public class HitsplatTrackerOverlay extends OverlayPanel {
    private final HitsplatTrackerPlugin plugin;
    private final HitsplatTrackerConfig config;

    @Inject
    private HitsplatTrackerOverlay(HitsplatTrackerPlugin plugin, HitsplatTrackerConfig config){
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.MED);
        this.plugin = plugin;
        this.config = config;
//        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, "Test", "test"));
    }

    @Override
    public Dimension render(Graphics2D graphics){
        if (!config.showInfobox()){
            return null;
        }
        List<LayoutableRenderableEntity> elems = panelComponent.getChildren();
        elems.clear();
        elems.add(LineComponent.builder().left("Total hitsplats: ").right(Integer.toString(plugin.totalHits)).build());
        elems.add(LineComponent.builder().left("Largest hitsplat: ").right(Integer.toString(plugin.biggestHit)).build());
        elems.add(LineComponent.builder().left("Accurate hits: ").right(Integer.toString(plugin.accurateHits)).build());
        elems.add(LineComponent.builder().left("Missed hits: ").right(Integer.toString(plugin.misses)).build());
        if (plugin.totalHits > 0) {
            float accuracy;
            if (plugin.attackStyle == CASTING || plugin.attackStyle == DEFENSIVE_CASTING){
                accuracy = (float)plugin.accurateHits / plugin.totalHits;
            }
            else {
                accuracy = (plugin.accurateHits + (float) plugin.misses / (plugin.biggestHit + 1)) / plugin.totalHits;
            }
            elems.add(LineComponent.builder().left("Approximate accuracy: ").right(String.format("%.4f", (accuracy))).build());
        }

        return super.render(graphics);
    }
}
