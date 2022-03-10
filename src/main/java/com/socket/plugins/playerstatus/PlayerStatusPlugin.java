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
 *  net.runelite.api.Skill
 *  net.runelite.api.VarPlayer
 *  net.runelite.api.Varbits
 *  net.runelite.api.events.ActorDeath
 *  net.runelite.api.events.ChatMessage
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.api.events.GameTick
 *  net.runelite.api.events.GraphicChanged
 *  net.runelite.api.events.MenuOptionClicked
 *  net.runelite.api.events.VarbitChanged
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.events.ConfigChanged
 *  net.runelite.client.game.ItemManager
 *  net.runelite.client.game.SpriteManager
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.socket.plugins.playerstatus;

import com.google.inject.Provides;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.inject.Inject;

import com.socket.plugins.playerstatus.marker.IndicatorMarker;
import com.socket.plugins.playerstatus.marker.TimerMarker;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import com.socket.org.json.JSONObject;
import com.socket.packet.SocketBroadcastPacket;
import com.socket.packet.SocketPlayerLeave;
import com.socket.packet.SocketReceivePacket;
import com.socket.plugins.playerstatus.gametimer.GameIndicator;
import com.socket.plugins.playerstatus.gametimer.GameTimer;
import com.socket.plugins.playerstatus.marker.AbstractMarker;
import net.runelite.client.ui.overlay.OverlayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(name="Socket - Player Status", description="Socket extension for displaying player status to members in your party.", tags={"net/runelite/client/plugins/socket", "server", "discord", "connection", "broadcast", "player", "status", "venge", "vengeance"}, enabledByDefault=true)
public class PlayerStatusPlugin
extends Plugin {
    private static final Logger log = LoggerFactory.getLogger(PlayerStatusPlugin.class);
    @Inject
    private Client client;
    @Inject
    private EventBus eventBus;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ItemManager itemManager;
    @Inject
    private SpriteManager spriteManager;
    @Inject
    private PlayerStatusOverlay overlay;
    @Inject
    private PlayerSidebarOverlay sidebar;
    @Inject
    private PlayerStatusConfig config;
    private Map<String, List<AbstractMarker>> statusEffects = new HashMap<>();
    private Map<String, PlayerStatus> partyStatus = new TreeMap<>();
    private int lastRaidVarb;
    private int lastVengCooldownVarb;
    private int lastIsVengeancedVarb;
    private int lastRefresh;
    public ArrayList<String> playerNames = new ArrayList();
    private List<String> whiteList = new ArrayList<>();

    @Provides
    PlayerStatusConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(PlayerStatusConfig.class);
    }

    public List<String> getWhiteList() {
        return this.whiteList;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void startUp() {
        this.lastRaidVarb = -1;
        this.lastRefresh = 0;
        synchronized (statusEffects) {
            this.statusEffects.clear();
        }
        synchronized (partyStatus) {
            this.partyStatus.clear();
        }
        this.overlayManager.add(this.overlay);
        this.overlayManager.add(this.sidebar);
        if (!this.config.specXferList().equals("")) {
            this.playerNames.clear();
            for (String name : this.config.specXferList().split(",")) {
                if (name.trim().equals("")) continue;
                this.playerNames.add(name.trim().toLowerCase());
            }
        }
        if (!this.config.showPlayerWhiteList().equals("")) {
            this.whiteList.clear();
            for (String name : this.config.showPlayerWhiteList().split(",")) {
                if (name.trim().equals("")) continue;
                this.whiteList.add(name.trim().toLowerCase());
            }
        }
    }

    protected void shutDown() {
        this.overlayManager.remove(this.overlay);
        this.overlayManager.remove(this.sidebar);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        block4: {
            block5: {
                if (!event.getGroup().equals("Socket Player Status Config v3")) break block4;
                if (!event.getKey().equals("specXferList")) break block5;
                if (this.config.specXferList().equals("")) break block4;
                this.playerNames.clear();
                for (String name : this.config.specXferList().split(",")) {
                    if (name.trim().equals("")) continue;
                    this.playerNames.add(name.trim().toLowerCase());
                }
                break block4;
            }
            if (event.getKey().equals("showPlayerWhiteList")) {
                this.whiteList.clear();
                if (!this.config.showPlayerWhiteList().equals("")) {
                    for (String name : this.config.showPlayerWhiteList().split(",")) {
                        if (name.trim().equals("")) continue;
                        this.whiteList.add(name.trim().toLowerCase());
                    }
                }
            }
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        int raidVarb = this.client.getVar(Varbits.IN_RAID);
        int vengCooldownVarb = this.client.getVar(Varbits.VENGEANCE_COOLDOWN);
        int isVengeancedVarb = this.client.getVar(Varbits.VENGEANCE_ACTIVE);
        if (this.lastRaidVarb != raidVarb) {
            this.removeGameTimer(GameTimer.OVERLOAD_RAID);
            this.removeGameTimer(GameTimer.PRAYER_ENHANCE);
            this.lastRaidVarb = raidVarb;
        }
        if (this.lastVengCooldownVarb != vengCooldownVarb) {
            if (vengCooldownVarb == 1) {
                this.createGameTimer(GameTimer.VENGEANCE);
            } else {
                this.removeGameTimer(GameTimer.VENGEANCE);
            }
            this.lastVengCooldownVarb = vengCooldownVarb;
        }
        if (this.lastIsVengeancedVarb != isVengeancedVarb) {
            if (isVengeancedVarb == 1) {
                this.createGameIndicator(GameIndicator.VENGEANCE_ACTIVE);
            } else {
                this.removeGameIndicator(GameIndicator.VENGEANCE_ACTIVE);
            }
            this.lastIsVengeancedVarb = isVengeancedVarb;
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().contains("Drink") && (event.getId() == 12635 || event.getId() == 12633)) {
            this.createGameTimer(GameTimer.STAMINA);
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.SPAM && event.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }
        if (event.getMessage().equals("You drink some of your stamina potion.") || event.getMessage().equals("You have received a shared dose of stamina potion.")) {
            this.createGameTimer(GameTimer.STAMINA);
        }
        if (event.getMessage().equals("<col=8f4808>Your stamina potion has expired.</col>")) {
            this.removeGameTimer(GameTimer.STAMINA);
        }
        if (event.getMessage().startsWith("You drink some of your") && event.getMessage().contains("overload")) {
            if (this.client.getVar(Varbits.IN_RAID) == 1) {
                this.createGameTimer(GameTimer.OVERLOAD_RAID);
            } else {
                this.createGameTimer(GameTimer.OVERLOAD);
            }
        }
        if (event.getMessage().startsWith("You drink some of your") && event.getMessage().contains("prayer enhance")) {
            this.createGameTimer(GameTimer.PRAYER_ENHANCE);
        }
        if (event.getMessage().equals("<col=ef1020>Your imbued heart has regained its magical power.</col>")) {
            this.removeGameTimer(GameTimer.IMBUED_HEART);
        }
        if (event.getMessage().equals("You drink some of your stamina potion.") || event.getMessage().equals("You have received a shared dose of stamina potion.")) {
            this.createGameTimer(GameTimer.STAMINA);
        }
        if (event.getMessage().contains("You drink some of your divine")) {
            if (event.getMessage().contains("divine ranging")) {
                this.createGameTimer(GameTimer.DIVINE_RANGE);
            } else if (event.getMessage().contains("divine bastion")) {
                this.createGameTimer(GameTimer.DIVINE_BASTION);
            } else if (event.getMessage().contains("divine combat")) {
                this.createGameTimer(GameTimer.DIVINE_SCB);
            } else if (event.getMessage().contains("divine super attack")) {
                this.createGameTimer(GameTimer.DIVINE_ATTACK);
            } else if (event.getMessage().contains("divine super strength")) {
                this.createGameTimer(GameTimer.DIVINE_STRENGTH);
            }
        }
    }

    @Subscribe
    public void onGraphicChanged(GraphicChanged event) {
        Actor actor = event.getActor();
        if (actor != this.client.getLocalPlayer()) {
            return;
        }
        if (actor.getGraphic() == GameTimer.IMBUED_HEART.getGraphicId().intValue()) {
            this.createGameTimer(GameTimer.IMBUED_HEART);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Subscribe
    public void onActorDeath(ActorDeath event) {
        Map<String, List<AbstractMarker>> map;
        if (event.getActor() != this.client.getLocalPlayer()) {
            return;
        }
        Map<String, List<AbstractMarker>> map2 = map = this.statusEffects;
        synchronized (map2) {
            List<AbstractMarker> activeEffects = this.statusEffects.get(null);
            if (activeEffects == null) {
                return;
            }
            for (AbstractMarker marker : new ArrayList<>(activeEffects)) {
                TimerMarker timer;
                if (!(marker instanceof TimerMarker) || !(timer = (TimerMarker)marker).getTimer().isRemovedOnDeath()) continue;
                activeEffects.remove(marker);
            }
            if (activeEffects.isEmpty()) {
                this.statusEffects.remove(null);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        switch (event.getGameState()) {
            case HOPPING: 
            case LOGIN_SCREEN: 
            case LOGIN_SCREEN_AUTHENTICATOR: {
                synchronized (statusEffects) {
                    for (String s : new ArrayList<>(this.statusEffects.keySet())) {
                        if (s == null) continue;
                        this.statusEffects.remove(s);
                    }
                }
                synchronized (partyStatus) {
                    this.partyStatus.clear();
                    break;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Subscribe
    public void onGameTick(GameTick event) {
        PlayerStatus status;
        Map<String, PlayerStatus> map;
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        int currentHealth = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        int currentPrayer = this.client.getBoostedSkillLevel(Skill.PRAYER);
        int maxHealth = this.client.getRealSkillLevel(Skill.HITPOINTS);
        int maxPrayer = this.client.getRealSkillLevel(Skill.PRAYER);
        int specialAttack = this.client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10;
        int runEnergy = this.client.getEnergy();
        String name = this.client.getLocalPlayer().getName();
        Map<String, PlayerStatus> map2 = map = this.partyStatus;
        synchronized (map2) {
            status = this.partyStatus.get(name);
            if (status == null) {
                status = new PlayerStatus(currentHealth, maxHealth, currentPrayer, maxPrayer, runEnergy, specialAttack);
                this.partyStatus.put(name, status);
            } else {
                status.setHealth(currentHealth);
                status.setMaxHealth(maxHealth);
                status.setPrayer(currentPrayer);
                status.setMaxPrayer(maxPrayer);
                status.setRun(runEnergy);
                status.setSpecial(specialAttack);
            }
            if (this.config.showSpecXfer() == PlayerStatusConfig.xferIconMode.ALL || this.config.showSpecXfer() == PlayerStatusConfig.xferIconMode.LIST && this.playerNames.contains(name.toLowerCase())) {
                if (specialAttack <= this.config.specThreshold()) {
                    this.createGameIndicator(GameIndicator.SPEC_XFER);
                } else {
                    this.removeGameIndicator(GameIndicator.SPEC_XFER);
                }
            }
        }
        ++this.lastRefresh;
        if (this.lastRefresh >= Math.max(1, this.config.getStatsRefreshRate())) {
            JSONObject packet = new JSONObject();
            packet.put("name", name);
            packet.put("player-stats", status.toJSON());
            this.eventBus.post(new SocketBroadcastPacket(packet));
            this.lastRefresh = 0;
        }
    }

    private void sortMarkers(List<AbstractMarker> markers) {
        markers.sort(new Comparator<AbstractMarker>(){

            @Override
            public int compare(AbstractMarker o1, AbstractMarker o2) {
                return Integer.compare(this.getMarkerOrdinal(o1), this.getMarkerOrdinal(o2));
            }

            private int getMarkerOrdinal(AbstractMarker marker) {
                if (marker == null) {
                    return -1;
                }
                if (marker instanceof IndicatorMarker) {
                    return ((IndicatorMarker)marker).getIndicator().ordinal();
                }
                if (marker instanceof TimerMarker) {
                    return ((TimerMarker)marker).getTimer().ordinal();
                }
                return -1;
            }
        });
    }

    private void createGameTimer(GameTimer timer) {
        this.createGameTimer(timer, null);
        JSONObject packet = new JSONObject();
        packet.put("player-status-game-add", this.client.getLocalPlayer().getName());
        packet.put("effect-name", timer.name());
        this.eventBus.post(new SocketBroadcastPacket(packet));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void createGameTimer(GameTimer timer, String name) {
        Map<String, List<AbstractMarker>> map;
        TimerMarker marker = new TimerMarker(timer, System.currentTimeMillis());
        switch (timer.getImageType()) {
            case SPRITE: {
                marker.setBaseImage(this.spriteManager.getSprite(timer.getImageId(), 0));
                break;
            }
            case ITEM: {
                marker.setBaseImage(this.itemManager.getImage(timer.getImageId()));
            }
        }
        this.removeGameTimer(timer, name);
        Map<String, List<AbstractMarker>> map2 = map = this.statusEffects;
        synchronized (map2) {
            List<AbstractMarker> activeEffects = this.statusEffects.get(name);
            if (activeEffects == null) {
                activeEffects = new ArrayList<>();
                this.statusEffects.put(name, activeEffects);
            }
            activeEffects.add(marker);
            this.sortMarkers(activeEffects);
        }
    }

    private void removeGameTimer(GameTimer timer) {
        this.removeGameTimer(timer, null);
        if (this.client.getLocalPlayer() == null) {
            return;
        }
        JSONObject packet = new JSONObject();
        packet.put("player-status-game-remove", this.client.getLocalPlayer().getName());
        packet.put("effect-name", timer.name());
        this.eventBus.post(new SocketBroadcastPacket(packet));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void removeGameTimer(GameTimer timer, String name) {
        Map<String, List<AbstractMarker>> map;
        Map<String, List<AbstractMarker>> map2 = map = this.statusEffects;
        synchronized (map2) {
            List<AbstractMarker> activeEffects = this.statusEffects.get(name);
            if (activeEffects == null) {
                return;
            }
            for (AbstractMarker marker : new ArrayList<>(activeEffects)) {
                TimerMarker instance;
                if (!(marker instanceof TimerMarker) || (instance = (TimerMarker)marker).getTimer() != timer) continue;
                activeEffects.remove(marker);
            }
            if (activeEffects.isEmpty()) {
                this.statusEffects.remove(name);
            }
        }
    }

    private void createGameIndicator(GameIndicator gameIndicator) {
        this.createGameIndicator(gameIndicator, null);
        if (this.client.getLocalPlayer() == null) {
            return;
        }
        JSONObject packet = new JSONObject();
        packet.put("player-status-indicator-add", this.client.getLocalPlayer().getName());
        packet.put("effect-name", gameIndicator.name());
        this.eventBus.post(new SocketBroadcastPacket(packet));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void createGameIndicator(GameIndicator gameIndicator, String name) {
        Map<String, List<AbstractMarker>> map;
        IndicatorMarker marker = new IndicatorMarker(gameIndicator);
        switch (gameIndicator.getImageType()) {
            case SPRITE: {
                marker.setBaseImage(this.spriteManager.getSprite(gameIndicator.getImageId(), 0));
                break;
            }
            case ITEM: {
                marker.setBaseImage(this.itemManager.getImage(gameIndicator.getImageId()));
            }
        }
        this.removeGameIndicator(gameIndicator, name);
        Map<String, List<AbstractMarker>> map2 = map = this.statusEffects;
        synchronized (map2) {
            List<AbstractMarker> activeEffects = this.statusEffects.get(name);
            if (activeEffects == null) {
                activeEffects = new ArrayList<>();
                this.statusEffects.put(name, activeEffects);
            }
            activeEffects.add(marker);
            this.sortMarkers(activeEffects);
        }
    }

    private void removeGameIndicator(GameIndicator indicator) {
        this.removeGameIndicator(indicator, null);
        JSONObject packet = new JSONObject();
        packet.put("player-status-indicator-remove", this.client.getLocalPlayer().getName());
        packet.put("effect-name", indicator.name());
        this.eventBus.post(new SocketBroadcastPacket(packet));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void removeGameIndicator(GameIndicator indicator, String name) {
        Map<String, List<AbstractMarker>> map;
        Map<String, List<AbstractMarker>> map2 = map = this.statusEffects;
        synchronized (map2) {
            List<AbstractMarker> activeEffects = this.statusEffects.get(name);
            if (activeEffects == null) {
                return;
            }
            for (AbstractMarker marker : new ArrayList<>(activeEffects)) {
                IndicatorMarker instance;
                if (!(marker instanceof IndicatorMarker) || (instance = (IndicatorMarker)marker).getIndicator() != indicator) continue;
                activeEffects.remove(marker);
            }
            if (activeEffects.isEmpty()) {
                this.statusEffects.remove(name);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Subscribe
    public void onSocketReceivePacket(SocketReceivePacket event) {
        block20: {
            try {
                JSONObject payload = event.getPayload();
                String localName = this.client.getLocalPlayer().getName();
                if (payload.has("player-stats")) {
                    Map<String, PlayerStatus> map;
                    String targetName = payload.getString("name");
                    if (targetName.equals(localName)) {
                        return;
                    }
                    JSONObject statusJson = payload.getJSONObject("player-stats");
                    Map<String, PlayerStatus> map2 = map = this.partyStatus;
                    synchronized (map2) {
                        PlayerStatus status = this.partyStatus.get(targetName);
                        if (status == null) {
                            status = PlayerStatus.fromJSON(statusJson);
                            this.partyStatus.put(targetName, status);
                        } else {
                            status.parseJSON(statusJson);
                        }
                        break block20;
                    }
                }
                if (payload.has("player-status-game-add")) {
                    String targetName = payload.getString("player-status-game-add");
                    if (targetName.equals(localName)) {
                        return;
                    }
                    String effectName = payload.getString("effect-name");
                    GameTimer timer = GameTimer.valueOf(effectName);
                    this.createGameTimer(timer, targetName);
                } else if (payload.has("player-status-game-remove")) {
                    String targetName = payload.getString("player-status-game-remove");
                    if (targetName.equals(localName)) {
                        return;
                    }
                    String effectName = payload.getString("effect-name");
                    GameTimer timer = GameTimer.valueOf(effectName);
                    this.removeGameTimer(timer, targetName);
                } else if (payload.has("player-status-indicator-add")) {
                    String targetName = payload.getString("player-status-indicator-add");
                    if (targetName.equals(localName)) {
                        return;
                    }
                    String effectName = payload.getString("effect-name");
                    GameIndicator indicator = GameIndicator.valueOf(effectName);
                    this.createGameIndicator(indicator, targetName);
                } else if (payload.has("player-status-indicator-remove")) {
                    String targetName = payload.getString("player-status-indicator-remove");
                    if (targetName.equals(localName)) {
                        return;
                    }
                    String effectName = payload.getString("effect-name");
                    GameIndicator indicator = GameIndicator.valueOf(effectName);
                    this.removeGameIndicator(indicator, targetName);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Subscribe
    public void onSocketPlayerLeave(SocketPlayerLeave event) {
        String target = event.getPlayerName();
        synchronized (statusEffects) {
            this.statusEffects.remove(target);
        }
        synchronized (partyStatus) {
            partyStatus.remove(target);
        }
    }

    public Map<String, List<AbstractMarker>> getStatusEffects() {
        return statusEffects;
    }

    public Map<String, PlayerStatus> getPartyStatus() {
        return partyStatus;
    }
}

