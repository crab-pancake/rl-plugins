package com.dmgtracker;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

import static com.dmgtracker.AttackStyle.*;

@PluginDescriptor(
        name = "Hitsplat Tracker",
        description = "Track hitsplats. No handling of thralls/cannon/venge/recoil",
        enabledByDefault = false
)
@Slf4j
public class HitsplatTrackerPlugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;

    private HitsplatTrackerOverlay overlay;
    private HitsplatTrackerConfig config;

    private final ArrayList<Hitsplat_> hits = new ArrayList<>();

    private HitsplatTrackerWriter writer;

    private String filename;
    private String username;

    private String hitsplatEffects;

    int biggestHit = -1;
    int misses = 0;
    int accurateHits = 0;
    int totalHits = 0;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");

    private int attackStyleVarbit = -1;
    private int equippedWeaponTypeVarbit = -1;
    private int castingModeVarbit = -1;
    private AttackStyle attackStyle;

    protected void startUp(){
        writer = new HitsplatTrackerWriter();

        if (config.showInfobox()){
            overlayManager.add(overlay);
        }
    }

    @Override
    protected void shutDown(){
        overlayManager.remove(overlay);
        writeHits();
    }

    @Provides
    HitsplatTrackerConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(HitsplatTrackerConfig.class);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged){
        if (configChanged.getKey().equals("showInfobox")){
            if (config.showInfobox()){
                overlayManager.add(overlay);
            }
            else
            {
                overlayManager.remove(overlay);
            }
        }
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied hitsplatApplied){
        Actor target = hitsplatApplied.getActor();
        Hitsplat hitsplat = hitsplatApplied.getHitsplat();
        if (!hitsplat.isMine() || target == client.getLocalPlayer()){
            return;
        }
        int amount = hitsplat.getAmount();
        if (amount > biggestHit){
            biggestHit = amount;
            client.addChatMessage(ChatMessageType.ENGINE,"HitTracker","new biggest hitsplat: " + biggestHit, "HitTracker");
        }
        hits.add(new Hitsplat_(amount, hitsplatEffects));
        if (amount > 0){
            accurateHits += 1;
        }
        else if (amount == 0){
            if (attackStyle == CASTING || attackStyle == DEFENSIVE_CASTING){
                accurateHits += 1;
            }
            else {
                misses += 1;
            }
        }
        hitsplatEffects = "";
    }

    @Subscribe
    public void onGraphicChanged(GraphicChanged graphicChanged){
        if (graphicChanged.getActor().getName() != null){
            // ahrims head
            if (graphicChanged.getActor().getGraphic() == 400){
                hitsplatEffects += "ahrims ";
            }
            //TODO: add opal/pearl/diamond/dragonstone/onyx bolt effects, check if they linger till next attack.
            // keris? gadderhammer?
            //

            else if (graphicChanged.getActor().getGraphic() == 85){ // TODO: CHECK THIS IS CORRECT
                hits.add(new Hitsplat_(-1, "splash"));
            }
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        username = Objects.requireNonNull(client.getLocalPlayer()).getName();

        int currentAttackStyleVarbit = client.getVar(VarPlayer.ATTACK_STYLE);
        int currentEquippedWeaponTypeVarbit = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);
        int currentCastingModeVarbit = client.getVarbitValue(Varbits.DEFENSIVE_CASTING_MODE);

        if (attackStyleVarbit != currentAttackStyleVarbit || equippedWeaponTypeVarbit != currentEquippedWeaponTypeVarbit || castingModeVarbit != currentCastingModeVarbit)
        {
            client.addChatMessage(ChatMessageType.ENGINE,"HitTracker","Weapon or attack style changed" + biggestHit, "HitTracker");
            writeHits();

            attackStyleVarbit = currentAttackStyleVarbit;
            equippedWeaponTypeVarbit = currentEquippedWeaponTypeVarbit;
            castingModeVarbit = currentCastingModeVarbit;
            updateAttackStyle(equippedWeaponTypeVarbit, attackStyleVarbit,
                    castingModeVarbit);
        }
    }

    private void updateAttackStyle(int equippedWeaponType, int attackStyleIndex, int castingMode)
    {
        AttackStyle[] attackStyles = WeaponType.getWeaponType(equippedWeaponType).getAttackStyles();
        if (attackStyleIndex < attackStyles.length)
        {
            attackStyle = attackStyles[attackStyleIndex];
            if (attackStyle == null)
            {
                attackStyle = OTHER;
            }
            else if ((attackStyle == CASTING) && (castingMode == 1))
            {
                attackStyle = DEFENSIVE_CASTING;
            }
        }
    }

    private void writeHits(){
        if (hits.size() > 0) {
            for (Hitsplat_ hit : hits) {
                writer.toFile(username, filename, hit.amount + ", " + hit.effects);
            }
            writer.toFile(username, filename, "total hits, " + totalHits);
            writer.toFile(username, filename, "accurate, " + accurateHits);
            writer.toFile(username, filename, "missed, " + misses);
            writer.toFile(username, filename, "attack style, " + attackStyle.toString());
            writer.toFile(username, filename, "weapon type, " + WeaponType.getWeaponType(equippedWeaponTypeVarbit).toString());
        }
        filename = formatter.format(LocalDateTime.now());
    }

    static class Hitsplat_ {
        int amount;
        String effects;

        public Hitsplat_(int amount, String hitsplatEffects) {
            this.amount = amount;
            this.effects = hitsplatEffects;
        }
    }
}
