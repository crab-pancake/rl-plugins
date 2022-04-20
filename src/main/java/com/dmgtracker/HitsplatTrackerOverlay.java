package com.dmgtracker;

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
        if (config.target() != HitsplatTrackerConfig.Target.RECEIVED) {
            elems.add(LineComponent.builder().left("Total hits dealt: ").right(Integer.toString(plugin.totalDealt)).build());
            elems.add(LineComponent.builder().left("Largest hit dealt: ").right(Integer.toString(plugin.biggestDealt)).build());
            elems.add(LineComponent.builder().left("Accurate dealt: ").right(Integer.toString(plugin.accurateDealt)).build());
            elems.add(LineComponent.builder().left("Misses dealt: ").right(Integer.toString(plugin.missesDealt)).build());
            if (plugin.totalDealt > 0) {
                float accuracy;
                if (plugin.attackStyle == CASTING || plugin.attackStyle == DEFENSIVE_CASTING) {
                    accuracy = (float) plugin.accurateDealt / plugin.totalDealt;
                } else {
                    accuracy = (plugin.accurateDealt + (float) plugin.missesDealt / (plugin.biggestDealt + 1)) / plugin.totalDealt;
                }
                elems.add(LineComponent.builder().left("Approximate accuracy: ").right(String.format("%.4f", (accuracy))).build());
            }
        }
        if (config.target() != HitsplatTrackerConfig.Target.DEALT){
            elems.add(LineComponent.builder().left("Total hits received: ").right(Integer.toString(plugin.totalReceived)).build());
            elems.add(LineComponent.builder().left("Largest hit received: ").right(Integer.toString(plugin.biggestReceived)).build());
            elems.add(LineComponent.builder().left("Accurate received: ").right(Integer.toString(plugin.accurateReceived)).build());
            elems.add(LineComponent.builder().left("Misses received: ").right(Integer.toString(plugin.missesReceived)).build());
        }

        return super.render(graphics);
    }
}
