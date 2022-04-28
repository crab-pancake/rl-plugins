/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.inject.Provides
 *  javax.inject.Inject
 *  net.runelite.api.Actor
 *  net.runelite.api.ChatMessageType
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.NPC
 *  net.runelite.api.Player
 *  net.runelite.api.Prayer
 *  net.runelite.api.Skill
 *  net.runelite.api.VarPlayer
 *  net.runelite.api.Varbits
 *  net.runelite.api.events.AnimationChanged
 *  net.runelite.api.events.GameTick
 *  net.runelite.api.kit.KitType
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.events.ConfigChanged
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.util.ColorUtil
 */
package com.socket.plugins.playerstatusextended;

import com.google.inject.Provides;

import java.util.ArrayList;
import java.util.Arrays;
import javax.inject.Inject;

import com.socket.org.json.JSONArray;
import com.socket.org.json.JSONObject;
import com.socket.packet.SSend;
import com.socket.packet.SReceive;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.kit.KitType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ColorUtil;

@PluginDescriptor(name="Socket - Player Status Extended", description="Socket extension for displaying player status to members in your party.", tags={"net/runelite/client/plugins/socket"}, enabledByDefault=false)
public class PlayerStatusExtendedPlugin
extends Plugin {
    @Inject
    private Client client;
    @Inject
    private PlayerStatusExtendedConfig config;
    @Inject
    private EventBus eventBus;
    private DeferredCheck deferredCheck;
    private ArrayList<String> exemptPlayer = new ArrayList();

    @Provides
    PlayerStatusExtendedConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(PlayerStatusExtendedConfig.class);
    }

    protected void startUp() throws Exception {
        this.exemptPlayer.clear();
        this.exemptPlayer = new ArrayList();
        if (this.config.ePlayers() != null && this.config.ePlayers().length() > 0) {
            String[] sp0;
            for (String sp1 : sp0 = this.config.ePlayers().split(",")) {
                String sp2;
                if (sp1 == null || (sp2 = sp1.trim()).length() == 0) continue;
                this.exemptPlayer.add(sp2.toLowerCase());
            }
        }
    }

    protected void shutDown() throws Exception {
        this.exemptPlayer.clear();
        this.exemptPlayer = new ArrayList();
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("playerstatusextendedconfig") && event.getKey().equals("exemptPl")) {
            this.exemptPlayer.clear();
            if (this.config.ePlayers() != null && this.config.ePlayers().length() > 0) {
                String[] sp0;
                for (String sp1 : sp0 = this.config.ePlayers().split(",")) {
                    if (sp1 == null) continue;
                    String sp2 = sp1.trim();
                    if (sp2.length() != 0) {
                        this.exemptPlayer.add(sp2.toLowerCase());
                    }
                    System.out.println("Config Changed: " + this.exemptPlayer);
                }
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (this.client.getGameState() == GameState.LOGGED_IN) {
            this.statCheckOnGameTick();
        }
    }

    private void statCheckOnGameTick() {
        if (this.client == null || this.client.getLocalPlayer() == null) {
            return;
        }
        if (this.deferredCheck != null && this.client.getTickCount() == this.deferredCheck.getTick()) {
            this.checkStats();
            this.deferredCheck = null;
        }
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        this.onCheckAnimationChanged(event);
    }

    private boolean nyloSlaveInteracting(NPC target) {
        if (target != null && target.getName() != null && target.getName().toLowerCase().contains("nylocas")) {
            return !target.getName().toLowerCase().contains("vasil");
        }
        return false;
    }

    private boolean otherShitBow(int i) {
        int[] e = new int[]{861, 12788, 22550, 22547};
        for (int i2 : e) {
            if (i2 != i) continue;
            return true;
        }
        return false;
    }

    private void checkStats() {
        int anim = this.deferredCheck.getAnim();
        int hammerBop = 401;
        int godBop = 7045;
        int bow = 426;
        int clawSpec = 7514;
        int clawBop = 393;
        int whip = 1658;
        int chalyBop = 440;
        int chalySpec = 1203;
        int scy = 8056;
        int bggsSpec = 7643;
        int bggsSpec2 = 7642;
        int hammerSpec = 1378;
        int del = 1100;
        int lanceSmack = 8290;
        int lancePoke = 8288;
        int[] hits = new int[]{lancePoke, lanceSmack, clawSpec, clawBop, whip, chalySpec, scy, bggsSpec, bggsSpec2, hammerSpec};
        for (int i : hits) {
            if (anim != i) continue;
            int lvl = this.client.getBoostedSkillLevel(Skill.STRENGTH);
            boolean piety = this.deferredCheck.isPiety();
            boolean is118 = lvl == 118;
            boolean bl = is118;
            if (piety && is118) break;
            String s = "attacked";
            if (i == clawSpec) {
                s = "claw speced";
            } else if (i == chalySpec) {
                s = "chally speced";
            } else if (i == bggsSpec || i == bggsSpec2) {
                s = "bgs speced";
            } else if (i == hammerSpec) {
                s = "hammer speced";
            }
            String s2 = "";
            if (!piety) {
                s2 = !is118 ? " with " + lvl + " strength and without piety." : " without piety.";
            } else if (!is118) {
                s2 = " with " + lvl + " strength.";
            }
            this.flagMesOut("You " + s + s2);
            break;
        }
    }

    private void onCheckAnimationChanged(AnimationChanged event) {
        if (event == null) {
            return;
        }
        if (event.getActor() instanceof Player) {
            Player p = (Player)event.getActor();
            if (p == null) {
                return;
            }
            int anim = p.getAnimation();
            if (p.getPlayerComposition() == null) {
                return;
            }
            int wep = p.getPlayerComposition().getEquipmentId(KitType.WEAPON);
            int hammerBop = 401;
            int godBop = 7045;
            int bow = 426;
            int lanceSmack = 8290;
            int lancePoke = 8288;
            int clawSpec = 7514;
            int clawBop = 393;
            int whip = 1658;
            int chalyBop = 440;
            int chalySpec = 1203;
            int scy = 8056;
            int bggsSspec = 7643;
            int hammerSpec = 1378;
            int trident = 1167;
            int surge = 7855;
            Actor interacting = p.getInteracting();
            NPC target = interacting == null ? null : (NPC)interacting;
            if (p.equals(this.client.getLocalPlayer())) {
                if (anim != 0 && anim != -1) {
                    if (!this.nyloSlaveInteracting(target)) {
                        int style = this.client.getVar(VarPlayer.ATTACK_STYLE);
                        if (anim == scy && style == 2) {
                            String a = "crush";
                            this.flagMesOut("You scythed on " + a + ".");
                        } else if (anim == bow && !this.otherShitBow(wep) && !this.client.isPrayerActive(Prayer.RIGOUR)) {
                            this.flagMesOut("You bowed without rigour active.");
                        } else if (anim == hammerBop && wep == 13576) {
                            this.flagMesOut("You hammer bopped.");
                        } else if (anim == godBop) {
                            this.flagMesOut("You godsword bopped.");
                        } else if (anim == chalyBop) {
                            this.flagMesOut("You chally poked.");
                        }
                    }
                    this.deferredCheck = new DeferredCheck(this.client.getTickCount(), anim, wep, this.client.isPrayerActive(Prayer.PIETY));
                }
            }
        }
    }

    private void flagMesOut(String mes) {
        if (this.client == null || this.client.getLocalPlayer() == null || this.client.getLocalPlayer().getName() == null) {
            return;
        }
        String finalS = mes.toLowerCase().replaceAll("you ", this.client.getLocalPlayer().getName() + " ");
        JSONArray data = new JSONArray();
        JSONObject json$ = new JSONObject();
        json$.put("print", finalS);
        json$.put("sender", this.client.getLocalPlayer().getName());
        int[] mapRegions = this.client.getMapRegions() == null ? new int[0] : this.client.getMapRegions();
        json$.put("mapregion", Arrays.toString(mapRegions));
        json$.put("raidbit", this.client.getVar(Varbits.IN_RAID));
        data.put(json$);
        JSONObject send = new JSONObject();
        send.put("sLeech", data);
        this.eventBus.post(new SSend(send));
    }

    @Subscribe
    public void onSReceive(SReceive event) {
        try {
            JSONObject payload = event.getPayload();
            if (!payload.has("sLeech")) {
                return;
            }
            JSONArray data = payload.getJSONArray("sLeech");
            JSONObject jsonmsg = data.getJSONObject(0);
            String sender = jsonmsg.getString("sender");
            if (this.exemptPlayer.contains(sender.toLowerCase())) {
                return;
            }
            String mapRegion = jsonmsg.getString("mapregion");
            int[] mapRegions = this.regionsFromString(mapRegion);
            boolean inTob = this.inRegion(mapRegions, 12613, 13125, 13123, 12612, 12611, 13122);
            boolean inCox = jsonmsg.getInt("raidbit") == 1;
            boolean bl = inCox;
            if (this.config.where() == PlayerStatusExtendedConfig.Where.TOB && !inTob) {
                return;
            }
            if (this.config.where() == PlayerStatusExtendedConfig.Where.COX && !inCox) {
                return;
            }
            if (this.config.where() == PlayerStatusExtendedConfig.Where.TOB_AND_COX && !inCox && !inTob) {
                return;
            }
            String msg = jsonmsg.getString("print");
            String finalS = ColorUtil.prependColorTag(msg, this.config.col());
            if (this.config.lvlOnly() && !finalS.contains("str")) {
                return;
            }
            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", finalS, "");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int[] regionsFromString(String s) {
        String s1 = s.substring(1).replaceAll("]", "");
        String[] s2 = s1.split(",");
        ArrayList<Integer> o = new ArrayList<Integer>();
        for (String s3 : s2) {
            o.add(Integer.valueOf(s3.trim()));
        }
        return o.stream().mapToInt(i -> i).toArray();
    }

    private boolean inRegion(int[] realR, int ... regions) {
        if (realR != null) {
            for (int i : realR) {
                int[] var6 = regions;
                int var7 = regions.length;
                for (int var8 = 0; var8 < var7; ++var8) {
                    int j = var6[var8];
                    if (i != j) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public static class DeferredCheck {
        private int tick;
        private int anim;
        private int wep;
        private boolean piety;

        public DeferredCheck(int tick, int anim, int wep, boolean piety) {
            this.tick = tick;
            this.anim = anim;
            this.wep = wep;
            this.piety = piety;
        }

        public void setTick(int tick) {
            this.tick = tick;
        }

        public void setAnim(int anim) {
            this.anim = anim;
        }

        public void setWep(int wep) {
            this.wep = wep;
        }

        public void setPiety(boolean piety) {
            this.piety = piety;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof DeferredCheck)) {
                return false;
            }
            DeferredCheck other = (DeferredCheck)o;
            return other.canEqual(this) && this.getTick() == other.getTick() && this.getAnim() == other.getAnim() && this.getWep() == other.getWep() && this.isPiety() == other.isPiety();
        }

        protected boolean canEqual(Object other) {
            return other instanceof DeferredCheck;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            result = result * 59 + this.getTick();
            result = result * 59 + this.getAnim();
            result = result * 59 + this.getWep();
            return result * 59 + (this.isPiety() ? 79 : 97);
        }

        public String toString() {
            return "PlayerStatusExtendedPlugin.DeferredCheck(tick=" + this.getTick() + ", anim=" + this.getAnim() + ", wep=" + this.getWep() + ", piety=" + this.isPiety() + ")";
        }

        public int getTick() {
            return this.tick;
        }

        public int getAnim() {
            return this.anim;
        }

        public int getWep() {
            return this.wep;
        }

        public boolean isPiety() {
            return this.piety;
        }
    }
}

