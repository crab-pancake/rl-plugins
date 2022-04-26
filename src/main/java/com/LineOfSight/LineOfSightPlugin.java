package com.LineOfSight;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.VarClientInt;
import net.runelite.api.events.VarClientIntChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

@PluginDescriptor(name = "Line of Sight", description = "los")

@Slf4j
public class LineOfSightPlugin extends Plugin {

    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private LineOfSightOverlay overlay;
    @Inject
    private LineOfSightConfig config;
    private int asdf = -1;

    @Provides
    LineOfSightConfig providesConfig(final ConfigManager configManager){
        return configManager.getConfig(LineOfSightConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
    }

//    @Subscribe
//    public void onVarClientIntChanged(VarClientIntChanged var){
//        if (asdf != client.getVar(VarClientInt.INVENTORY_TAB)) {
//            asdf = client.getVar(VarClientInt.INVENTORY_TAB);
//            if (overlay.losTiles != null) {
//                System.out.println("player: " + client.getLocalPlayer().getWorldLocation());
//                System.out.println("dest: " + client.getSelectedSceneTile().getWorldLocation());
//                System.out.println(overlay.losTiles.keySet() + "\n");
//            }
//        }
//    }
}


// 1: calc los between player and hovered tile using the inbuilt function Tile.hasLosTo
   // Done kinda: all tiles in los not just hovered
// 2: implement my own line of sight algorithm for use with line of walk
//      2.5 canTravelInDirection for line of walk
// 3: calc line of walk between player and hovered tile.
// 4: calc line of walk between player and interacting