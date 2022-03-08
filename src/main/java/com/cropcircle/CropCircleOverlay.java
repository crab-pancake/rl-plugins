package com.cropcircle;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;

public class CropCircleOverlay extends OverlayPanel {
    private final CropCirclePlugin plugin;
    private final CropCircleConfig config;

    @Inject
    private CropCircleOverlay(CropCirclePlugin plugin, CropCircleConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.setPriority(OverlayPriority.HIGH);
        this.setPosition(OverlayPosition.TOP_LEFT);
    }

    public Dimension render(Graphics2D graphics) {
        boolean inArea = this.plugin.inCropCircleArea();
        if (inArea || this.config.everywhereOverlay()) {
            this.panelComponent.setPreferredSize(new Dimension(150, 0));
            this.panelComponent.getChildren().add(LineComponent.builder().left("UTC Time:").leftColor(Color.WHITE).right(Integer.toString(this.plugin.getCurrentUTCTime())).rightColor(Color.GREEN).build());
            this.panelComponent.getChildren().add(LineComponent.builder().left("Rotation time:").leftColor(Color.WHITE).right(Integer.toString(this.plugin.getRotationTime())).rightColor(Color.GREEN).build());
            this.panelComponent.getChildren().add(LineComponent.builder().left("In crop circle area?").leftColor(Color.WHITE).right(Boolean.toString(inArea)).rightColor(inArea?Color.GREEN:Color.RED).build());
            return super.render(graphics);
        } else {
            return null;
        }
    }
}
