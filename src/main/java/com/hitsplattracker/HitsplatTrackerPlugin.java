package com.hitsplattracker;

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

import static com.hitsplattracker.AttackStyle.*;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import static com.hitsplattracker.HitsplatTrackerConfig.Target;

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

    private final ArrayList<Hitsplat_> hitsDealt = new ArrayList<>();
    private final ArrayList<Hitsplat_> hitsReceived = new ArrayList<>();

    private HitsplatTrackerWriter writer;

    private String hitsplatEffects;

    int biggestDealt = 0;
    int missesDealt = 0;
    int accurateDealt = 0;
    int totalDealt = 0;

    int biggestReceived = 0;
    int missesReceived = 0;
    int accurateReceived = 0;
    int totalReceived = 0;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");

    private int attackStyleVarbit = -1;
    private int equippedWeaponTypeVarbit = -1;
    private int castingModeVarbit = -1;
    private int lastChangedWeapon = -1;
    private String target;
    AttackStyle attackStyle;

    @Provides
    HitsplatTrackerConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(HitsplatTrackerConfig.class);
    }

    @Override
    protected void startUp() throws Exception{
        reset(Target.BOTH, true);
        writer = new HitsplatTrackerWriter();
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown(){
        overlayManager.remove(overlay);
        writeHits(Target.BOTH);
    }

    @Subscribe
    public void onOverlayMenuClicked(OverlayMenuClicked overlayMenuClicked){
        OverlayMenuEntry overlayMenuEntry = overlayMenuClicked.getEntry();
        if (overlayMenuEntry.getMenuAction() == MenuAction.RUNELITE_OVERLAY
                && overlayMenuClicked.getOverlay() == overlay
                && overlayMenuClicked.getEntry().getOption().equals(OPTION_CONFIGURE)) {
            client.addChatMessage(ChatMessageType.ENGINE,"hitTracker","clicked overlay option","hitTracker");
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged){
        if (configChanged.getGroup().equals("HitsplatTracker")) {
            if (configChanged.getKey().equals("showInfobox")) {
                if (config.showInfobox()) {
                    overlayManager.add(overlay);
                } else {
                    overlayManager.remove(overlay);
                }
            }
            else if (configChanged.getKey().equals("target")) {
                if (config.target() == Target.RECEIVED){
                    writeHits(Target.DEALT);
                }
                if (config.target() == Target.DEALT){
                    writeHits(Target.RECEIVED);
                }
            }
        }
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied hitsplatApplied){
        Actor target = hitsplatApplied.getActor();
        Hitsplat hitsplat = hitsplatApplied.getHitsplat();
        if (!hitsplat.isMine() || target.getName() == null){
            return;
        }
        if (target == client.getLocalPlayer()){
            if (config.target() == HitsplatTrackerConfig.Target.DEALT){
                return;
            }
            totalReceived += 1;

            int amount = hitsplat.getAmount();
            if (amount > biggestReceived){
                biggestReceived = amount;
                client.addChatMessage(ChatMessageType.ENGINE,"HitTracker","new biggest hit received: " + biggestReceived, "HitTracker");
            }
            hitsReceived.add(new Hitsplat_(amount, hitsplatEffects));
            if (amount > 0){
                accurateReceived += 1;
            }
            else {
                (missesReceived) += 1;
            }
        }
        else {
            if (config.target() == HitsplatTrackerConfig.Target.RECEIVED) {
                return;
            }
            if (!Objects.equals(target.getName(), this.target)){

                // Player dealt hitsplat to a new mob, write hits then change target.
                writeHits(Target.BOTH);
                this.target = target.getName();
            }
            totalDealt += 1;

            int amount = hitsplat.getAmount();
            if (amount > biggestDealt){
                biggestDealt = amount;
                client.addChatMessage(ChatMessageType.ENGINE,"HitTracker","new biggest hit dealt: " + biggestDealt, "HitTracker");
            }
            hitsDealt.add(new Hitsplat_(amount, hitsplatEffects));
            if (amount > 0){
                accurateDealt += 1;
            }
            else if (amount == 0){
                if (attackStyle == CASTING || attackStyle == DEFENSIVE_CASTING){
                    accurateDealt += 1;
                }
                else {
                    missesDealt += 1;
                }
            }
        }
        hitsplatEffects = "";
    }

    @Subscribe
    public void onGraphicChanged(GraphicChanged graphicChanged){
        if (graphicChanged.getActor().getName() != null && Objects.equals(graphicChanged.getActor().getName(), target)){
            // ahrims head
            if (graphicChanged.getActor().getGraphic() == 400){
                hitsplatEffects += "ahrims ";
            }
            // TODO: add opal/pearl/diamond/dragonstone/onyx bolt effects
            // TODO: check spotanim frame to figure out if this graphic is lingering from a previous attack
            // keris? gadderhammer?
            //

            else if (graphicChanged.getActor().getGraphic() == 85){
                hitsDealt.add(new Hitsplat_(-1, "splash"));
                missesDealt += 1;
                totalDealt += 1;
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
                //TODO: figure out why this runs 3 times. 1 for each varbit that is changed probably, how to fix?
//                log.info("weapon or attack style changed");
                writeHits(Target.DEALT);
                lastChangedWeapon = client.getTickCount();
            }
            // TODO: check if below stuff can go inside the above check with some print tests
            attackStyleVarbit = currentAttackStyleVarbit;
            equippedWeaponTypeVarbit = currentEquippedWeaponTypeVarbit;
            castingModeVarbit = currentCastingModeVarbit;
            updateAttackStyle(equippedWeaponTypeVarbit, attackStyleVarbit,
                    castingModeVarbit);
        }
    }

    private void reset(HitsplatTrackerConfig.Target target, boolean varbits){
        if (target != Target.RECEIVED){
            hitsDealt.clear();
            biggestDealt = -1;
            totalDealt = 0;
            accurateDealt = 0;
            missesDealt = 0;
            hitsplatEffects = "";
        }
        if (target != Target.DEALT) {
            hitsReceived.clear();
            biggestReceived = -1;
            totalReceived = 0;
            accurateReceived = 0;
            missesReceived = 0;
        }

        if (varbits){
            attackStyleVarbit = -1;
            equippedWeaponTypeVarbit = -1;
            castingModeVarbit = -1;
            lastChangedWeapon = -1;
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

    private void writeHits(Target target){
//        log.info("writing hits");
        String filename = formatter.format(LocalDateTime.now());

        if (target != Target.DEALT){ // received or both
            if (hitsReceived.size() > 0) {
                writer.toFile(filename + " R", ToCSV(Target.RECEIVED));
            }
        }
        if (target != Target.RECEIVED){ // dealt or both
            if (hitsDealt.size() > 0) {
                filename += " " + this.target;
                writer.toFile(filename + " D", ToCSV(Target.DEALT));
            }
        }
        reset(target, false);
    }

    public String ToCSV(Target target)
    {
        if (target == Target.BOTH){
            // TODO: make this handle properly
            return null;
        }
        StringBuilder csv = new StringBuilder();
        csv.append("amount,effects");
        csv.append("\n");

        if (target == Target.DEALT) {
            for (Hitsplat_ hit : hitsDealt) {
                csv.append(hit.amount).append(",").append(hit.effects).append("\n");
            }
            csv.append("total hits,").append(totalDealt).append("\n");
            csv.append("accurate,").append(accurateDealt).append("\n");
            csv.append("misses,").append(missesDealt).append("\n");
            csv.append("attack style,").append(attackStyle).append("\n");
            csv.append("weapon type,").append(WeaponType.getWeaponType(equippedWeaponTypeVarbit).toString()).append("\n");
            csv.append("target,").append(this.target);
        }
        if (target == Target.RECEIVED) {
            for (Hitsplat_ hit : hitsReceived) {
                csv.append(hit.amount).append(",").append(hit.effects).append("\n");
            }
            csv.append("total hits,").append(totalReceived).append("\n");
            csv.append("accurate,").append(accurateReceived).append("\n");
            csv.append("misses,").append(missesReceived).append("\n");
        }
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
