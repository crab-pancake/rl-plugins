package com.LineOfSight;

import net.runelite.api.Client;
import net.runelite.api.Tile;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;


public class LineOfSightPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private LineOfSightOverlay overlay;

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
    }

    private void lineOfSight(Tile origin, Tile dest){
        origin.getWorldLocation().getPlane(); // if different planes then return false
        // find which side of tile to use
            // if deltaX == deltaY go e/w first then n/s
        // get midpoint of this side
        // draw straight line between this and the appropriate side of origin tile
        // store all tiles it passes through
            // do appropriate handling for tile corners
    }
}
// 1: calc los between player and hovered tile using the inbuilt function Tile.hasLosTo
// 2: implement my own line of sight algorithm for use with line of walk
// 3: calc line of walk between player and hovered tile.
// 4: calc line of walk between player and interacting