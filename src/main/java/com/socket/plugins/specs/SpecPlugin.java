package com.socket.plugins.specs;

import com.google.inject.Provides;
import com.socket.org.json.JSONObject;
import com.socket.packet.SocketBroadcastPacket;
import com.socket.packet.SocketReceivePacket;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.ws.PartyService;
import net.runelite.client.ws.WSClient;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@PluginDescriptor(
		name = "<html><font color=#b20e0e>[M] spec counter",
		description = "Track specs over socket",
		tags = {"combat", "npcs", "overlay"},
		enabledByDefault = true
)
public class SpecPlugin extends Plugin
{
	private int currentWorld = -1;
	private int specialPercentage = -1;
	private int specialHitpointsExperience = -1;
	private int specialHitpointsGained = -1;
	private boolean specialUsed = false;
	private double modifier = 2d;

	private SpecialWeapon specialWeapon;
	private final Set<Integer> interactedNpcIds = new HashSet<>();
	private final SpecCounter[] specCounter = new SpecCounter[SpecialWeapon.values().length];

	private Actor lastSpecTarget;
	private int lastSpecTick;
//	private long specialExperience = -1L;
//	private long magicExperience = -1L;
	private int minSpecHit;

	private VulnerabilityInfoBox vulnBox = null;
	public SpritePixels vuln = null;

	private String identifier = "m-specs";

	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private WSClient wsClient;
	@Inject
	private PartyService party;
	@Inject
	private InfoBoxManager infoBoxManager;
	@Inject
	private ItemManager itemManager;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private EventBus eventBus;
	@Inject
	private SpecOverlay overlay;
	@Inject
	private SpecConfig config;

	@Provides
    SpecConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SpecConfig.class);
	}

	@Override
	protected void startUp()
	{
		reset();
		wsClient.registerMessage(SpecCounterUpdate.class);
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		reset();
		wsClient.unregisterMessage(SpecCounterUpdate.class);
		overlayManager.remove(overlay);
		removeCounters();
	}

	protected void reset(){
		currentWorld = -1;
		specialPercentage = -1;
		lastSpecTarget = null;
		lastSpecTick = -1;
		interactedNpcIds.clear();
		specialUsed = false;
//		specialExperience = -1L;
//		magicExperience = -1L;
		minSpecHit = -1;
		this.vulnBox = null;
		this.vuln = null;
		removeCounters();
		identifier = config.identifier() ? "special-extended" : "m-specs";
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			if (currentWorld == -1)
			{
				currentWorld = client.getWorld();
			}
			else if (currentWorld != client.getWorld())
			{
				currentWorld = client.getWorld();
				removeCounters();
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event){
		if (event.getKey().equals("identifier")){
			identifier = config.identifier() ? "special-extended" : "m-specs";
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged interactingChanged) {
		Actor source = interactingChanged.getSource();
		Actor target = interactingChanged.getTarget();
		if (lastSpecTick != client.getTickCount() || source != client.getLocalPlayer() || target == null) {
			return;
		}
		lastSpecTarget = target;
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int specialPercentage = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);
		if (this.specialPercentage == -1 || specialPercentage >= this.specialPercentage) {
			this.specialPercentage = specialPercentage;
			return;
		}


		this.specialPercentage = specialPercentage;
		specialWeapon = usedSpecialWeapon();

		lastSpecTarget = Objects.requireNonNull(client.getLocalPlayer()).getInteracting();
		lastSpecTick = client.getTickCount();
		specialHitpointsExperience = client.getSkillExperience(Skill.HITPOINTS);
		specialHitpointsGained = -1;
//		specialExperience = this.client.getOverallExperience();
//		magicExperience = this.client.getSkillExperience(Skill.MAGIC);

		specialUsed = true;
		minSpecHit = -1;
	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged event) {
		if (event.getActor().getName() != null && event.getActor().getGraphic() == 169) {
			updateCounter("", SpecialWeapon.VULNERABILITY,"",0);
			lastSpecTarget = event.getActor();

			JSONObject data = new JSONObject();
			data.put("target", ((NPC)event.getActor()).getId());
			data.put("weapon", "vuln");
			data.put("hit", 0);
			JSONObject payload = new JSONObject();
			payload.put(identifier, data);
			this.eventBus.post(new SocketBroadcastPacket(payload));
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		if (specialUsed && statChanged.getSkill() == Skill.HITPOINTS)
		{
			specialHitpointsGained = statChanged.getXp() - specialHitpointsExperience;
		}
	}

	@Subscribe
	public void onFakeXpDrop(FakeXpDrop fakeXpDrop)
	{
		if (specialUsed && fakeXpDrop.getSkill() == Skill.HITPOINTS)
		{
			specialHitpointsGained = fakeXpDrop.getXp();
		}
	}

	@Subscribe
	private void onGameTick(GameTick tick)
	{
		if (client.getGameState() != GameState.LOGGED_IN) return;

		if (lastSpecTick != -1 && lastSpecTick < client.getTickCount() + 10){
			// reset: last spec occurred too long ago
			specialHitpointsGained = 0;
			lastSpecTarget = null;
			specialUsed = false;
			minSpecHit = -1;
			lastSpecTick = -1;
		}

		if (specialUsed && lastSpecTarget != null && lastSpecTarget instanceof NPC)
		{
			int deltaExperience = specialHitpointsGained;  // TODO: check how this works with fake xp drops

			if (deltaExperience > 0)
			{
				if (specialWeapon != null) {
					minSpecHit = getHitFromXp(specialWeapon, deltaExperience);  // returns 1 if non dmg based spec

					if (config.guessDawnbringer() && specialWeapon == SpecialWeapon.DAWNBRINGER) {
						minSpecHit *= 1.9;
					}
					else if (specialWeapon.isDamage()){
						return;
					}

					if (!interactedNpcIds.contains(((NPC) lastSpecTarget).getId())) {
						removeCounters();
						addInteracting(((NPC) lastSpecTarget).getId());
					}

					String pName = client.getLocalPlayer().getName();
					updateCounter(pName, specialWeapon, pName, minSpecHit);

					if (!party.getMembers().isEmpty())
					{
						final SpecCounterUpdate specCounterUpdate = new SpecCounterUpdate(((NPC) lastSpecTarget).getId(), specialWeapon, minSpecHit);
						specCounterUpdate.setMemberId(party.getLocalMember().getMemberId());
						wsClient.send(specCounterUpdate);
					}
					socketSend(pName, ((NPC)lastSpecTarget).getId(), specialWeapon, minSpecHit);

					// reset
					specialHitpointsGained = 0;
					lastSpecTarget = null;
					specialUsed = false;
					minSpecHit = -1;
					lastSpecTick = -1;
				}

			}
			else { // no exp gained: hit splashed
				specialHitpointsGained = 0;
				lastSpecTarget = null;
				specialUsed = false;
				minSpecHit = -1;
				lastSpecTick = -1;
			}
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied) {
		if (minSpecHit == -1) return; // means either no spec or no hp exp was gained so spec missed
		// is this necessary? already gets reset in onGameTick. maybe depends on order things are processed

		Actor target = hitsplatApplied.getActor();
		Hitsplat hitsplat = hitsplatApplied.getHitsplat();
//		Hitsplat.HitsplatType hitsplatType = hitsplat.getHitsplatType();
		if (!hitsplat.isMine() || target == client.getLocalPlayer() || hitsplat.getAmount() < minSpecHit || hitsplat.getAmount() > minSpecHit * 2.5) {
			// hitsplat is not mine or hitsplat was on me or hitsplat is too big/small to be the spec
			return;
		}
		if (lastSpecTarget != null && lastSpecTarget != target) {
			return;
		}
		boolean wasSpec = lastSpecTarget != null;  // always true?

		// reset
		specialHitpointsGained = 0;
		lastSpecTarget = null;
		specialUsed = false;
		minSpecHit = -1;
		lastSpecTick = -1;
//		specialExperience = -1L;
//		magicExperience = -1L;
		if (!(target instanceof NPC)) {
			return;
		}
		NPC npc = (NPC)target;
		int interactingId = npc.getId();
		if (!interactedNpcIds.contains(interactingId)) {
			removeCounters();
			addInteracting(interactingId);
		}
		if (wasSpec && specialWeapon != null) {
			if (!specialWeapon.isDamage()){
				log.debug("non dmg based spec weapon processed hitsplat: check.");
			}
			int hit = getHit(specialWeapon, hitsplat);
			log.debug("Special attack target: id: {} - target: {} - weapon: {} - amount: {}", interactingId, target.getName(), specialWeapon, hit);
			String pName = Objects.requireNonNull(client.getLocalPlayer()).getName();
			updateCounter(pName, specialWeapon, pName, hit);

			if (!party.getMembers().isEmpty())
			{
				final SpecCounterUpdate specCounterUpdate = new SpecCounterUpdate(interactingId, specialWeapon, minSpecHit);
				specCounterUpdate.setMemberId(party.getLocalMember().getMemberId());
				wsClient.send(specCounterUpdate);
			}

			socketSend(pName, interactingId, specialWeapon, hit);
		}
	}

	private void socketSend(String playerName, int interactingId, SpecialWeapon specialWeapon, int hit){
		JSONObject data = new JSONObject();
		data.put("player", playerName);
		data.put("target", interactingId);
		data.put("weapon", specialWeapon.ordinal());
		data.put("hit", hit);

		JSONObject payload = new JSONObject();
		payload.put(identifier, data);
		eventBus.post(new SocketBroadcastPacket(payload));
	}

	private int checkInteracting()  // is this better than last target? probably, since multi form bosses are included
	{
		Player localPlayer = client.getLocalPlayer();
		Actor interacting = localPlayer.getInteracting();

		if (interacting instanceof NPC)
		{
			NPC npc = (NPC) interacting;
			NPCComposition composition = npc.getComposition();
			int interactingId = npc.getId();

			if (!ArrayUtils.contains(composition.getActions(), "Attack"))
			{
				// Skip over non attackable npcs so that eg. talking to bankers doesn't reset
				// the counters.
				return -1;
			}

			if (!interactedNpcIds.contains(interactingId))
			{
				removeCounters();
				addInteracting(interactingId);
			}

			return interactingId;
		}

		return -1;
	}

	@Subscribe
	public void onSocketReceivePacket(SocketReceivePacket event) {
		try {
			if (client.getGameState() != GameState.LOGGED_IN) {
				return;
			}
			JSONObject payload = event.getPayload();
			if (payload.has(identifier)) {
				String pName = client.getLocalPlayer().getName();
				JSONObject data = payload.getJSONObject(identifier);
				if (data.getString("player").equals(pName)) {
					return;
				}
				clientThread.invoke(() -> {
					SpecialWeapon weapon = SpecialWeapon.values()[data.getInt("weapon")];
					String attacker = data.getString("player");
					int targetId = data.getInt("target");
					if (!interactedNpcIds.contains(targetId)) {
						removeCounters();
						addInteracting(targetId);
					}
					updateCounter(attacker, weapon, attacker, data.getInt("hit"));
				});
			} else if (payload.has(identifier+"-bossdead")) {
				removeCounters();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addInteracting(int npcId)
	{
		modifier = 1d;
		interactedNpcIds.add(npcId);

		// Add alternate forms of bosses
		final Boss boss = Boss.getBoss(npcId);
		if (boss != null)
		{
//				modifier = boss.getModifier(); // TODO: readd multipliers to make min dmg threshold more accurate?
			interactedNpcIds.addAll(boss.getIds());
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (isSotetseg(npcDespawned.getNpc().getId()) && client.getLocalPlayer().getWorldLocation().getPlane() == 3) {
			return;
		}
		NPC actor = npcDespawned.getNpc();
		if (lastSpecTarget == actor) {
			lastSpecTarget = null;
		}
		if (actor.isDead() && this.interactedNpcIds.contains(actor.getId())) {
			removeCounters();
			JSONObject payload = new JSONObject();
			payload.put(identifier+"-bossdead", "dead");
			eventBus.post(new SocketBroadcastPacket(payload));
		}
	}

	@Subscribe
	public void onSpecCounterUpdate(SpecCounterUpdate event)
	{
		if (party.getLocalMember().getMemberId().equals(event.getMemberId()))
		{
			return;
		}

		String name = party.getMemberById(event.getMemberId()).getName();
		if (name == null)
		{
			return;
		}

		clientThread.invoke(() ->
		{
			// If not interacting with any npcs currently, add to interacting list
			if (interactedNpcIds.isEmpty())
			{
				addInteracting(event.getNpcId());
			}

			// Otherwise we only add the count if it is against a npc we are already tracking
			if (interactedNpcIds.contains(event.getNpcId()))
			{
				updateCounter(name, event.getWeapon(), name, event.getHit());
			}
		});
	}

	private SpecialWeapon usedSpecialWeapon() {
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment == null) {
			return null;
		}
		Item[] items = equipment.getItems();
		int weaponIdx = EquipmentInventorySlot.WEAPON.getSlotIdx();
		if (weaponIdx >= items.length) {
			return null;
		}
		Item weapon = items[weaponIdx];
		for (SpecialWeapon specialWeapon : SpecialWeapon.values()) {
			if (specialWeapon.getItemID() == weapon.getId()) {
				return specialWeapon;
			}
		}
		return null;
	}

	private void updateCounter(String player, SpecialWeapon specialWeapon, String name, int hit) {
		if (specialWeapon == SpecialWeapon.VULNERABILITY) {
			if (config.vulnerability()) {
				infoBoxManager.removeInfoBox(vulnBox);
				IndexDataBase sprite = client.getIndexSprites();
				vuln = Objects.requireNonNull(client.getSprites(sprite, 56, 0))[0];
				vulnBox = new VulnerabilityInfoBox(vuln.toBufferedImage(), this);
				infoBoxManager.addInfoBox(vulnBox);
			}
		}
		else{
			if (specialWeapon == SpecialWeapon.BANDOS_GODSWORD_OR) {
				specialWeapon = SpecialWeapon.BANDOS_GODSWORD;
			}
			else if (specialWeapon == SpecialWeapon.DARKLIGHT) {
				specialWeapon = SpecialWeapon.ARCLIGHT;
			}
			else if (specialWeapon == SpecialWeapon.BONE_DAGGER_P || specialWeapon == SpecialWeapon.BONE_DAGGER_PP || specialWeapon == SpecialWeapon.BONE_DAGGER_S){
				specialWeapon = SpecialWeapon.BONE_DAGGER;
			}
			SpecCounter counter = specCounter[specialWeapon.ordinal()];
			AsyncBufferedImage image = itemManager.getImage(specialWeapon.getItemID());
			overlay.addOverlay(player, new SpecialIcon(image, Integer.toString(hit), System.currentTimeMillis()));

			if (counter == null) {
				counter = new SpecCounter(image, this, hit, specialWeapon);
				infoBoxManager.addInfoBox(counter);
				specCounter[specialWeapon.ordinal()] = counter;
			}
			else {
				counter.addHits(hit);
			}
			// If in a party, add hit to partySpecs for the infobox tooltip. TODO: do this for vuln?
			Map<String, Integer> partySpecs = counter.getPartySpecs();
			if (!party.getMembers().isEmpty()) {
				if (partySpecs.containsKey(name)) {
					partySpecs.put(name, hit + partySpecs.get(name));
				}
				else {
					partySpecs.put(name, hit);
				}
			}
		}
	}

	private void removeCounters() {
		interactedNpcIds.clear();
		for (int i = 0; i < specCounter.length; ++i){
			SpecCounter counter = specCounter[i];
			if (counter != null){
				infoBoxManager.removeInfoBox(counter);
				specCounter[i] = null;
				infoBoxManager.removeInfoBox(vulnBox);
			}
		}
	}

	private int getHit(SpecialWeapon specialWeapon, Hitsplat hitsplat) {
		return specialWeapon.isDamage() ? hitsplat.getAmount() : 1;
	}

	private int getHitFromXp(SpecialWeapon specialWeapon, int deltaHpExp)
	{
		double modifierBase = 1/2d;
		double damageOutput = (deltaHpExp * modifierBase) / 1.3333d;

		return specialWeapon.isDamage()? (int) Math.round(damageOutput) : 1;
	}

	public boolean isSotetseg(int id) {
		return Boss.getBoss(id) == Boss.SOTETSEG;
	}
}