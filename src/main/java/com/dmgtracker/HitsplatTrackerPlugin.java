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
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

import static com.dmgtracker.AttackStyle.*;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

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

    @Inject
    private HitsplatTrackerOverlay overlay;

    @Inject
    private HitsplatTrackerConfig config;

    private final ArrayList<Hitsplat_> hits = new ArrayList<>();

    private HitsplatTrackerWriter writer;

    private String filename;

    private String hitsplatEffects;

    int biggestHit = -1;
    int misses = 0;
    int accurateHits = 0;
    int totalHits = 0;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");

    private int attackStyleVarbit = -1;
    private int equippedWeaponTypeVarbit = -1;
    private int castingModeVarbit = -1;
    private int lastChangedWeapon = -1;
    AttackStyle attackStyle;

    @Provides
    HitsplatTrackerConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(HitsplatTrackerConfig.class);
    }

    @Override
    protected void startUp() throws Exception{
        writer = new HitsplatTrackerWriter();
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown(){
        overlayManager.remove(overlay);
        writeHits();
    }

    @Subscribe
    public void onOverlayMenuClicked(OverlayMenuClicked overlayMenuClicked){
        OverlayMenuEntry overlayMenuEntry = overlayMenuClicked.getEntry();
        if (overlayMenuEntry.getMenuAction() == MenuAction.RUNELITE_OVERLAY
                && overlayMenuClicked.getOverlay() == overlay
                && overlayMenuClicked.getEntry().getOption().equals(OPTION_CONFIGURE)) {
            client.addChatMessage(ChatMessageType.ENGINE,"hittracker","clicked overlay option","hittracker");
        }
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
                misses += 1;
            }
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        int currentAttackStyleVarbit = client.getVar(VarPlayer.ATTACK_STYLE);
        int currentEquippedWeaponTypeVarbit = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);
        int currentCastingModeVarbit = client.getVarbitValue(Varbits.DEFENSIVE_CASTING_MODE);

        if (attackStyleVarbit != currentAttackStyleVarbit || equippedWeaponTypeVarbit != currentEquippedWeaponTypeVarbit || castingModeVarbit != currentCastingModeVarbit)
        {
            if (lastChangedWeapon != client.getTickCount()) {
                client.addChatMessage(ChatMessageType.ENGINE, "HitTracker", "Weapon or attack style changed", "HitTracker");
                lastChangedWeapon = client.getTickCount();
                writeHits();
            }
            // TODO: check if below stuff can go inside the above check with some print tests
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
            writer.toFile(filename, ToCSV());
            hits.clear();
        }
        filename = formatter.format(LocalDateTime.now());
    }

    public String ToCSV()
    {
        StringBuilder csv = new StringBuilder();
        csv.append("amount,effects");
        csv.append("\n");

        for (Hitsplat_ hit : hits)
        {
            csv.append("").append(hit.amount).append(",").append(hit.effects).append("\n");
        }
        csv.append("total hits,").append(totalHits).append("\n");
        csv.append("accurate,").append(accurateHits).append("\n");
        csv.append("misses,").append(misses).append("\n");
        csv.append("attack style,").append(attackStyle).append("\n");
        csv.append("weapon type,").append(WeaponType.getWeaponType(equippedWeaponTypeVarbit).toString()).append("\n");
        return csv.toString();
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
