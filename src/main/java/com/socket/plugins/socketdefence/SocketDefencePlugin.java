/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.inject.Provides
 *  javax.inject.Inject
 *  net.runelite.api.Actor
 *  net.runelite.api.Client
 *  net.runelite.api.IndexDataBase
 *  net.runelite.api.NPC
 *  net.runelite.api.Skill
 *  net.runelite.api.SpritePixels
 *  net.runelite.api.VarPlayer
 *  net.runelite.api.Varbits
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.ActorDeath
 *  net.runelite.api.events.AnimationChanged
 *  net.runelite.api.events.GameTick
 *  net.runelite.api.events.GraphicChanged
 *  net.runelite.api.events.HitsplatApplied
 *  net.runelite.api.events.NpcSpawned
 *  net.runelite.api.events.ScriptPreFired
 *  net.runelite.api.events.VarbitChanged
 *  net.runelite.api.kit.KitType
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.events.ConfigChanged
 *  net.runelite.client.game.SkillIconManager
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.plugins.PluginManager
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 *  net.runelite.client.ui.overlay.infobox.InfoBox
 *  net.runelite.client.ui.overlay.infobox.InfoBoxManager
 *  net.runelite.client.util.ColorUtil
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.socket.plugins.socketdefence;

import com.google.inject.Provides;
import com.socket.org.json.JSONObject;
import com.socket.packet.SSend;
import com.socket.packet.SocketMembersUpdate;
import com.socket.packet.SReceive;
import com.socket.packet.SShutdown;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@PluginDescriptor(name="Socket - Defence", description="Shows defence level for different bosses after specs", tags={"net/runelite/client/plugins/socket", "pvm", "cox", "gwd", "corp", "tob"}, enabledByDefault=false)
public class SocketDefencePlugin
extends Plugin {
    private static final Logger log = LoggerFactory.getLogger(SocketDefencePlugin.class);
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private EventBus eventBus;
    @Inject
    private SkillIconManager skillIconManager;
    @Inject
    private InfoBoxManager infoBoxManager;
    @Inject
    private SocketDefenceConfig config;
    @Inject
    private SocketDefenceOverlay overlay;
    @Inject
    ConfigManager configManager;
    @Inject
    PluginManager pluginManager;
    public ArrayList<String> socketPlayerNames = new ArrayList();
    public String specWep = "";
    public String boss = "";
    public double bossDef = -1.0;
    public DefenceInfoBox box = null;
    private VulnerabilityInfoBox vulnBox = null;
    public SpritePixels vuln = null;
    public boolean vulnHit;
    public boolean isInCm = false;
    public ArrayList<String> bossList = new ArrayList<>(Arrays.asList("Corporeal Beast", "General Graardor", "K'ril Tsutsaroth", "Kalphite Queen", "The Maiden of Sugadinti", "Xarpus", "Great Olm (Left claw)", "Tekton", "Tekton (enraged)", "Callisto"));
    public boolean hmXarpus = false;
    private boolean mirrorMode;

    protected void startUp() throws Exception {
        this.reset();
        this.overlayManager.add(this.overlay);
    }

    protected void shutDown() throws Exception {
        this.reset();
        this.overlayManager.remove(this.overlay);
    }

    protected void reset() {
        this.infoBoxManager.removeInfoBox(this.box);
        this.infoBoxManager.removeInfoBox(this.vulnBox);
        this.boss = "";
        this.bossDef = -1.0;
        this.specWep = "";
        this.box = null;
        this.vulnBox = null;
        this.vuln = null;
        this.vulnHit = false;
        this.isInCm = this.config.cm();
    }

    @Provides
    SocketDefenceConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(SocketDefenceConfig.class);
    }

    @Subscribe
    public void onScriptPreFired(ScriptPreFired scriptPreFired) {
        if (!this.specWep.contains("bgs") && !this.specWep.contains("dwh")) {
            return;
        }
        if (scriptPreFired.getScriptId() == 996) {
            int[] intStack = this.client.getIntStack();
            int intStackSize = this.client.getIntStackSize();
            int widgetId = intStack[intStackSize - 4];
            try {
                this.processXpDrop(widgetId);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void processXpDrop(int widgetId) throws InterruptedException {
        Widget xpdrop = this.client.getWidget(WidgetInfo.TO_GROUP(widgetId), WidgetInfo.TO_CHILD(widgetId));
        if (xpdrop == null) {
            return;
        }
        Widget[] children = xpdrop.getChildren();
        Widget textWidget = children[0];
        String text = textWidget.getText();
        boolean isDamage = false;
        Actor interacted = Objects.requireNonNull(this.client.getLocalPlayer()).getInteracting();
        String targetName = interacted.getName();
        if (!(targetName.contains("Maiden") || targetName.contains("Sotetseg") || targetName.contains("Xarpus"))) {
            return;
        }
        Optional<Plugin> o = this.pluginManager.getPlugins().stream().filter(p -> p.getName().equals("damagedrops")).findAny();
        if (o.isPresent() && this.pluginManager.isPluginEnabled(o.get())) {
            isDamage = this.configManager.getConfiguration("damagedrops", "replaceEXPDrop").equals("true");
        }
        if (text.contains("<")) {
            if (text.contains("<img=11>")) {
                text = text.substring(9);
            }
            if (text.contains("<")) {
                text = text.substring(0, text.indexOf("<"));
            }
        }
        int damage = -1;
        int weaponUsed = Objects.requireNonNull(this.client.getLocalPlayer()).getPlayerComposition().getEquipmentId(KitType.WEAPON);
        if (Arrays.stream(children).skip(1L).filter(Objects::nonNull).mapToInt(Widget::getSpriteId).anyMatch(id -> id == 197 || id == 198 || id == 199)) {
            String name;
            if (this.client.getVarbitValue(4696) == 0) {
                if (this.client.getVarbitValue(4696) == 0) {
                    if (this.client.getVar(VarPlayer.ATTACK_STYLE) == 3) {
                        damage = isDamage ? Integer.parseInt(text) : Integer.parseInt(text);
                    }
                } else {
                    damage = isDamage ? Integer.parseInt(text) : Integer.parseInt(text) / 4;
                }
            } else {
                int n = damage = isDamage ? Integer.parseInt(text) : (int)Math.round((double)Integer.parseInt(text) / 5.3333);
            }
            if ((name = this.client.getLocalPlayer().getInteracting().getName()) == null) {
                return;
            }
            JSONObject data = new JSONObject();
            data.put("boss", name);
            data.put("weapon", this.specWep);
            data.put("hit", damage);
            JSONObject payload = new JSONObject();
            payload.put("socketdefence", data);
            this.eventBus.post(new SSend(payload));
            this.specWep = "";
        }
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (event.getActor() != null && this.client.getLocalPlayer() != null && event.getActor().getName() != null) {
            int animation = event.getActor().getAnimation();
            if (event.getActor().getName().equals(this.client.getLocalPlayer().getName())) {
                this.specWep = animation == 1378 ? "dwh" : (animation == 7642 || animation == 7643 ? "bgs" : (animation == 2890 ? "arclight" : ""));
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        for (NPC n : this.client.getNpcs()) {
            if (n == null || n.getName() == null || !n.getName().equals(this.boss) || !n.isDead() && n.getHealthRatio() != 0) continue;
            JSONObject data = new JSONObject();
            data.put("boss", this.boss);
            data.put("player", this.client.getLocalPlayer().getName());
            JSONObject payload = new JSONObject();
            payload.put("socketdefencebossdead", data);
            this.eventBus.post(new SSend(payload));
            this.reset();
        }
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        if (!(event.getActor() instanceof NPC)) {
            return;
        }
        NPC npc = (NPC)event.getActor();
        if (npc.getName().contains("Maiden") || npc.getName().contains("Sotetseg") || npc.getName().contains("Xarpus")) {
            return;
        }
        if (!this.specWep.equals("") && event.getHitsplat().isMine() && event.getActor() instanceof NPC && event.getActor() != null && event.getActor().getName() != null && this.bossList.contains(event.getActor().getName())) {
            String name = event.getActor().getName().contains("Tekton") ? "Tekton" : event.getActor().getName();
            JSONObject data = new JSONObject();
            data.put("boss", name);
            data.put("weapon", this.specWep);
            data.put("hit", event.getHitsplat().getAmount());
            JSONObject payload = new JSONObject();
            payload.put("socketdefence", data);
            this.eventBus.post(new SSend(payload));
            this.specWep = "";
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        this.hmXarpus = event.getNpc().getId() >= 10770 && event.getNpc().getId() <= 10772;
    }

    @Subscribe
    public void onActorDeath(ActorDeath event) {
        if (event.getActor() instanceof NPC && event.getActor().getName() != null && this.client.getLocalPlayer() != null && (event.getActor().getName().equals(this.boss) || event.getActor().getName().contains("Tekton") && this.boss.equals("Tekton"))) {
            JSONObject data = new JSONObject();
            data.put("boss", this.boss);
            data.put("player", this.client.getLocalPlayer().getName());
            JSONObject payload = new JSONObject();
            payload.put("socketdefencebossdead", data);
            this.eventBus.post(new SSend(payload));
            this.reset();
        }
    }

    @Subscribe
    public void onSReceive(SReceive event) {
        try {
            JSONObject payload = event.getPayload();
            if (payload.has("socketdefence")) {
                JSONObject data = payload.getJSONObject("socketdefence");
                String bossName = data.getString("boss");
                String weapon = data.getString("weapon");
                int hit = data.getInt("hit");
                if ((bossName.equals("Tekton") || bossName.contains("Great Olm")) && this.client.getVar(Varbits.IN_RAID) != 1 || (bossName.contains("The Maiden of Sugadinti") || bossName.contains("Xarpus")) && this.client.getVar(Varbits.THEATRE_OF_BLOOD) != 2) {
                    return;
                }
                if (this.boss.equals("") || this.bossDef == -1.0 || !this.boss.equals(bossName)) {
                    switch (bossName) {
                        case "Corporeal Beast":
                            this.bossDef = 310.0;
                            break;
                        case "General Graardor":
                            this.bossDef = 250.0;
                            break;
                        case "K'ril Tsutsaroth":
                            this.bossDef = 270.0;
                            break;
                        case "Kalphite Queen":
                            this.bossDef = 300.0;
                            break;
                        case "Callisto":
                            this.bossDef = 440.0;
                            break;
                        case "The Maiden of Sugadinti":
                            this.bossDef = 200.0;
                            break;
                        case "Xarpus":
                            this.bossDef = this.hmXarpus ? 200.0 : 250.0;
                            break;
                        case "Great Olm (Left claw)":
                            this.bossDef = 175.0 * (1.0 + 0.01 * (double) (this.client.getVarbitValue(5424) - 1));
                            if (this.isInCm) {
                                this.bossDef *= 1.5;
                            }
                            break;
                        case "Tekton":
                            this.bossDef = 205.0 * (1.0 + 0.01 * (double) (this.client.getVarbitValue(5424) - 1));
                            if (this.isInCm) {
                                this.bossDef *= 1.2;
                            }
                            break;
                    }
                    this.boss = bossName;
                }
                if (weapon.equals("dwh")) {
                    if (hit == 0) {
                        if (this.client.getVar(Varbits.IN_RAID) == 1 && this.boss.equals("Tekton")) {
                            this.bossDef -= this.bossDef * 0.05;
                        }
                    } else {
                        this.bossDef -= this.bossDef * 0.3;
                    }
                } else if (weapon.equals("bgs")) {
                    if (hit == 0) {
                        if (this.client.getVar(Varbits.IN_RAID) == 1 && this.boss.equals("Tekton")) {
                            this.bossDef -= 10.0;
                        }
                    } else {
                        this.bossDef = this.boss.equals("Corporeal Beast") ? (this.bossDef = this.bossDef - (double)(hit * 2)) : (this.bossDef = this.bossDef - (double)hit);
                    }
                } else if (weapon.equals("arclight") && hit > 0) {
                    this.bossDef = this.boss.equals("K'ril Tsutsaroth") ? (this.bossDef = this.bossDef - this.bossDef * 0.1) : (this.bossDef = this.bossDef - this.bossDef * 0.05);
                } else if (weapon.equals("vuln")) {
                    if (this.config.vulnerability()) {
                        this.infoBoxManager.removeInfoBox(this.vulnBox);
                        IndexDataBase sprite = this.client.getIndexSprites();
                        this.vuln = this.client.getSprites(sprite, 56, 0)[0];
                        this.vulnBox = new VulnerabilityInfoBox(this.vuln.toBufferedImage(), this);
                        this.vulnBox.setTooltip(ColorUtil.wrapWithColorTag(this.boss, Color.WHITE));
                        this.infoBoxManager.addInfoBox(this.vulnBox);
                    }
                    this.vulnHit = true;
                    this.bossDef -= this.bossDef * 0.1;
                }
                if (this.bossDef < -1.0) {
                    this.bossDef = 0.0;
                }
                this.infoBoxManager.removeInfoBox(this.box);
                this.box = new DefenceInfoBox(this.skillIconManager.getSkillImage(Skill.DEFENCE), this, Math.round(this.bossDef), this.config);
                this.box.setTooltip(ColorUtil.wrapWithColorTag(this.boss, Color.WHITE));
                this.infoBoxManager.addInfoBox(this.box);
            } else if (payload.has("socketdefencebossdead")) {
                String bossName;
                JSONObject data = payload.getJSONObject("socketdefencebossdead");
                if (this.client.getLocalPlayer() != null && !data.getString("player").equals(this.client.getLocalPlayer().getName()) && (bossName = data.getString("boss")).equals(this.boss)) {
                    this.reset();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    void onSocketMembersUpdate(SocketMembersUpdate event) {
        this.socketPlayerNames.clear();
        this.socketPlayerNames.addAll(event.getMembers());
    }

    @Subscribe
    private void onSShuddown(SShutdown event) {
        this.socketPlayerNames.clear();
    }

    @Subscribe
    private void onVarbitChanged(VarbitChanged event) {
        if (this.client.getVar(Varbits.IN_RAID) != 1 && (this.boss.equals("Tekton") || this.boss.equals("Great Olm (Left claw)")) || this.boss.equals("The Maiden of Sugadinti") && this.getInstanceRegionId() != TobRegions.MAIDEN.getRegionId() || this.boss.equals("Xarpus") && this.getInstanceRegionId() != TobRegions.XARPUS.getRegionId()) {
            this.reset();
        }
    }

    @Subscribe
    public void onGraphicChanged(GraphicChanged event) {
        if (event.getActor().getName() != null && event.getActor().getGraphic() == 169 && this.bossList.contains(event.getActor().getName())) {
            this.boss = event.getActor().getName().contains("Tekton") ? "Tekton" : event.getActor().getName();
            JSONObject data = new JSONObject();
            data.put("boss", this.boss);
            data.put("weapon", "vuln");
            data.put("hit", 0);
            JSONObject payload = new JSONObject();
            payload.put("socketdefence", data);
            this.eventBus.post(new SSend(payload));
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged e) {
        this.isInCm = this.config.cm();
    }

    public int getInstanceRegionId() {
        return WorldPoint.fromLocalInstance(this.client, this.client.getLocalPlayer().getLocalLocation()).getRegionID();
    }

    public enum TobRegions {
        MAIDEN(12613),
        BLOAT(13125),
        NYLOCAS(13122),
        SOTETSEG(13123),
        SOTETSEG_MAZE(13379),
        XARPUS(12612),
        VERZIK(12611);

        private final int regionId;

        TobRegions(int regionId) {
            this.regionId = regionId;
        }

        public int getRegionId() {
            return this.regionId;
        }
    }
}

