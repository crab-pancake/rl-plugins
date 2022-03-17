// 
// Decompiled by Procyon v0.5.36
// 

package com.highlight;

import net.runelite.api.NPCComposition;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import java.util.ArrayList;

public class NpcSpawn
{
    public int index;
    public String name;
    public int id;
    public int size;
    public int diedOnTick;
    public int respawnTime;
    public ArrayList<WorldPoint> spawnLocations;
    public WorldPoint spawnPoint;
    public boolean dead;
    
    NpcSpawn(final NPC npc) {
        this.name = npc.getName();
        this.id = npc.getId();
        this.index = npc.getIndex();
        this.spawnLocations = new ArrayList<>();
        this.respawnTime = -1;
        this.diedOnTick = -1;
        this.spawnPoint = null;
        this.dead = true;
        final NPCComposition composition = npc.getTransformedComposition();
        if (composition != null) {
            this.size = composition.getSize();
        }
    }
}
