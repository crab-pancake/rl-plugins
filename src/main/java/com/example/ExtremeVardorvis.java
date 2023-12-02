package com.example;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Projectile;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.Varbits;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Extreme Vardorvis"
)
public class ExtremeVardorvis extends Plugin
{
	private static final int BOSS_ROOM_MAP_REGION = 4405;

	private static final int MAGIC_PROJECTILE = 2520;
	private static final int RANGE_PROJECTILE = 2521;

	boolean inBossRoom;
	boolean inFight;
	int bossAttackTimer;
	int nextPossibleOsu;
	int bossHpPercentage;
	boolean bossWasDashing;

	private WorldPoint arenaSWCorner;
//	private final List<Integer> realAxes = new ArrayList<>();
//	private final List<RuneLiteObject> fakeTendrils = new ArrayList<>();
//	private final List<FakeAxe> fakeAxes = new ArrayList<>();

	private boolean headSpawnedThisTick;
	private boolean forceMageHead;
	private RuneLiteObject fakeSpike;

	private int axeSpawnedTicks;

	private int missedPrayers;
	private WorldPoint playerPositionLastTick;

	private final List<PrayerCheck> prayerCheckQueue = new ArrayList<>();

	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;

	@Inject
	private VardConfig config;

	@Override
	protected void startUp()
	{
		reset();
	}

	@Override
	protected void shutDown()
	{
		reset();
	}

	@Provides
	VardConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(VardConfig.class);
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged e){
		if (e.getGameState() == GameState.LOGGED_IN){
			checkInBossRoom();
		}
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned e){
		if (!inFight)
		{
			return;
		}

		// tendril spawn: add to list of axes. change next possible captcha time
//		if (e.getNpc().getId() == NpcID.LARGE_TENDRIL){
//			axeSpawnedTicks = 0;
//
//			// add location of this axe
////			realAxes.add(worldPointToTendrilNum(e.getNpc()));
//
////			while (nextPossibleOsu < 9){
////				nextPossibleOsu += 5;
////			}
//		}

		// head spawn: add to list of head projectiles (note prayer timing for mistake tracker)
		if (e.getNpc().getId() == NpcID.VARDORVIS_HEAD){
			headSpawnedThisTick = true;
			forceMageHead = true;
			prayerCheckQueue.add(new PrayerCheck(client.getTickCount()+3, Varbits.PRAYER_PROTECT_FROM_MISSILES));
		}
	}

//	@Subscribe
//	private void onNpcDespawned(NpcDespawned e){
//		if (e.getNpc().getId() == 12227){
////			realAxes.clear();
////			fakeAxes.clear();
//		}
//	}

	// detect fight start: entrance rocks become uninteractable (because of the tendrils)
	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned e){
		if (inFight || !inBossRoom)
		{
			return;
		}
		if (e.getGameObject().getId() == 47604){
			// fight started
			inFight = true;
			arenaSWCorner = e.getGameObject().getWorldLocation().dx(5).dy(-16);
		}
	}

	// detect fight end: uninteractable entrance rocks despawn (either replaced by interactable version, or player tele)
	@Subscribe
	private void onGameObjectDespawned(GameObjectDespawned e){
		if (!inFight || !inBossRoom)
		{
			return;
		}

		if (e.getGameObject().getId() == 47604){
			// fight ended
			inFight = false;
			reset();
		}
	}

	// detect boss dying - fight end
	@Subscribe
	private void onActorDeath(ActorDeath e){
		if (!inFight || !inBossRoom || !(e.getActor() instanceof NPC)){
			return;
		}

		if (((NPC) e.getActor()).getId() == NpcID.VARDORVIS){
			inFight = false;
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Missed "+missedPrayers+" prayers that fight.","");
			missedPrayers = 0;
		}
	}

	@Subscribe
	private void onAnimationChanged(AnimationChanged e){
		if (!(inFight && inBossRoom))
		{
			return;
		}
		if (!(e.getActor() instanceof NPC) || ((NPC) e.getActor()).getId() != NpcID.VARDORVIS){
			return;
		}

		if (e.getActor().getAnimation() == 10341){
			bossWasDashing = true;
			return;
		}

		// attack animation
		if (e.getActor().getAnimation() == 10340 ){
			bossAttackTimer = 5;
			prayerCheckQueue.add(new PrayerCheck(client.getTickCount(), Varbits.PRAYER_PROTECT_FROM_MELEE));

			if (config.spikyFloor() && !bossWasDashing)
			{
				clientThread.invoke(this::spawnSpike);
			}
		}

		// captcha start
		if (e.getActor().getAnimation() == 10342){
			// clear all axes, heads, projectiles, tendrils
			fakeSpike.setFinished(true);
			fakeSpike = null;

		}

		bossWasDashing = false;
	}

	@Subscribe
	private void onGameTick(GameTick e){
		if (!inFight || !inBossRoom)
		{
			return;
		}

		for (PrayerCheck prayer : prayerCheckQueue){
			if (prayer.tick == client.getTickCount()){
				if (client.getVarbitValue(prayer.prayerToCheck) != 1){
					// missed prayer!
					if (config.mistakeTracker())
					{
						client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You've been injured and can't use protection prayers!", "");
					}
					missedPrayers++;
				}
				if (prayer.soundId != null)
				{
					client.playSoundEffect(prayer.soundId);
				}
				if (prayer.graphicId != null)
				{
					client.getLocalPlayer().createSpotAnim(99, prayer.graphicId, 80, 0);
				}
			}
		}
		prayerCheckQueue.removeIf(sound -> sound.tick <= client.getTickCount());

//		fakeHeadProjectiles.removeIf(p -> p.getEndCycle() <= client.getGameCycle());

		handleHeads();

//		handleAxes();

		clientThread.invokeAtTickEnd(() -> {
			bossAttackTimer--;
			headSpawnedThisTick = false;
			if (axeSpawnedTicks >= 0)
			{
				axeSpawnedTicks++;
			}
			if (axeSpawnedTicks > 7)
			{
				axeSpawnedTicks = -1;
			}
			playerPositionLastTick = client.getLocalPlayer().getWorldLocation();

			if (client.getVarbitValue(Varbits.BOSS_HEALTH_MAXIMUM) > 0)
			{
				bossHpPercentage = client.getVarbitValue(Varbits.BOSS_HEALTH_CURRENT) * 100 / client.getVarbitValue(Varbits.BOSS_HEALTH_MAXIMUM);
			}
			else {
				bossHpPercentage = 100;
			}
		});
	}

	@Subscribe
	private void onClientTick(ClientTick e){

		if (fakeSpike != null)
		{
			updateSpikeAnim();
		}
	}

	private void checkInBossRoom(){
		inBossRoom = (client.isInInstancedRegion() && Arrays.stream(client.getMapRegions()).anyMatch(i -> i == BOSS_ROOM_MAP_REGION));
	}

	private void spawnSpike(){
		RuneLiteObject spike = client.createRuneLiteObject();
		spike.setModel(client.loadModel(46981));
		spike.setAnimation(client.loadAnimation(10165));

		LocalPoint modifiedLocation = offsetLp(LocalPoint.fromWorld(client, playerPositionLastTick));

		spike.setLocation(modifiedLocation, client.getPlane());
		spike.setDrawFrontTilesFirst(false);

		spike.setActive(true);
		if (fakeSpike != null){
			fakeSpike.setFinished(true);
		}
		fakeSpike = spike;
	}

	private void spawnHead(int projectileId){
		// don't send message for the second head
		if (bossAttackTimer == 5)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ff289d>Vardorvis' head gazes upon you...</col>", "");
		}

		client.playSoundEffect(3539);

//		int west = Math.max(arenaSWCorner.getX(), client.getLocalPlayer().getWorldLocation().getX() - 6);
//		int south = Math.max(arenaSWCorner.getY(), client.getLocalPlayer().getWorldLocation().getX() - 6);
//		int width = Math.min(arenaSWCorner.getX() + 11, client.getLocalPlayer().getWorldLocation().getX() + 6) - west;
//		int height = Math.min(arenaSWCorner.getY() + 11, client.getLocalPlayer().getWorldLocation().getY() + 6) - south;
//
//		WorldArea tempSpawns = new WorldArea(west, south, width, height, client.getPlane());
//
//		List<WorldPoint> validPositions = tempSpawns.toWorldPointList();
		// in future: remove player position and pillar positions
//		validPositions.removeIf(w -> w.getX() == client.getLocalPlayer().getWorldLocation().getX() && w.getY() == client.getLocalPlayer().getWorldLocation().getY());
//		WorldPoint spawnPos = validPositions.get(ThreadLocalRandom.current().nextInt(validPositions.size()));

		WorldPoint spawnPos = client.getLocalPlayer().getWorldLocation().dx(ThreadLocalRandom.current().nextInt(-3,4)).dy(ThreadLocalRandom.current().nextInt(-3,4));

		RuneLiteObject head = client.createRuneLiteObject();

		head.setModel(client.loadModel(49301));
		head.setAnimation(client.loadAnimation(10348));
		head.setLocation(LocalPoint.fromWorld(client,spawnPos), client.getPlane());
		int angle = (int) (Math.atan2(spawnPos.getX() - client.getLocalPlayer().getWorldLocation().getX(), spawnPos.getY() - client.getLocalPlayer().getWorldLocation().getY()) * 1024 / Math.PI);
		while (angle < 0){
			angle += 2048;
		}
		head.setOrientation(angle);
		head.setActive(true);

		createHeadProjectile(client, projectileId, head);

		int prayerToCheck;
		int spotAnim;

		if (projectileId == MAGIC_PROJECTILE){
			prayerToCheck = Varbits.PRAYER_PROTECT_FROM_MAGIC;
			spotAnim = 2551;
		}
//		else if (projectileId == RANGE_PROJECTILE){
//			prayerToCheck = Varbits.PRAYER_PROTECT_FROM_MISSILES;
//			spotAnim = 2552;
//		}
		else { // TODO: add more prayers here!
			prayerToCheck = Varbits.PRAYER_PROTECT_FROM_MISSILES;
			spotAnim = 2552;
		}

		prayerCheckQueue.add(new PrayerCheck(client.getTickCount() + 3, prayerToCheck, 4015, spotAnim));
	}

	private void handleHeads(){
		if (!config.moreHeads() || headSpawnedThisTick || bossAttackTimer == 3)
			return;

		switch (bossAttackTimer){
			case 5:
				if (ThreadLocalRandom.current().nextBoolean() && bossHpPercentage < 90)
				{
					boolean whichProjectile = ThreadLocalRandom.current().nextBoolean();
					spawnHead(whichProjectile ? MAGIC_PROJECTILE : RANGE_PROJECTILE);

					forceMageHead = !whichProjectile; // chaos heads: ThreadLocalRandom.current().nextBoolean()
				}
				return;
			case 4:
				if (bossHpPercentage > 50)
					return;
				spawnHead(forceMageHead ? MAGIC_PROJECTILE : RANGE_PROJECTILE);
			default:
		}
	}

	private void updateSpikeAnim()
	{
		if (Objects.requireNonNull(fakeSpike.getAnimation()).getId() == 10165){
			if (fakeSpike.getAnimationFrame() == fakeSpike.getAnimation().getNumFrames() - 1){
				fakeSpike.setModel(client.loadModel(49294));
				fakeSpike.setAnimation(client.loadAnimation(10356));
			}
		}
		else if (Objects.requireNonNull(fakeSpike.getAnimation()).getId() == 10356){
			if (fakeSpike.getAnimationFrame() == 2){
				client.playSoundEffect(7154, fakeSpike.getLocation().getSceneX(), fakeSpike.getLocation().getSceneY(), 10);

				// put below in onGameTick?
				WorldPoint spikeLocation = WorldPoint.fromLocal(client, fakeSpike.getLocation());
				if (client.getLocalPlayer().getWorldLocation().getX() == spikeLocation.getX() && client.getLocalPlayer().getWorldLocation().getY() == spikeLocation.getY()){
					client.addChatMessage(ChatMessageType.GAMEMESSAGE,"","Stepped on a spike! Ow!","");
				}
			}
			else if (fakeSpike.getAnimationFrame() == 40){
				client.playSoundEffect(7109, fakeSpike.getLocation().getSceneX(), fakeSpike.getLocation().getSceneY(), 10);
			}

			if (fakeSpike.finished()){
				fakeSpike = null;
			}
		}
	}

	private LocalPoint offsetLp(LocalPoint lp){
		int cameraAngle = client.getCameraYaw();
		if (cameraAngle < 2048 /16){
			return new LocalPoint(lp.getX(), lp.getY() + 1);
		}
		else if (cameraAngle < 2048 * 3/16){
			return new LocalPoint(lp.getX() + 1, lp.getY() + 1);
		}
		else if (cameraAngle < 2048 * 5/16){
			return new LocalPoint(lp.getX() + 1, lp.getY());
		}
		else if (cameraAngle < 2048 * 7/16){
			return new LocalPoint(lp.getX() + 1, lp.getY() - 1);
		}
		else if (cameraAngle < 2048 * 9/16){
			return new LocalPoint(lp.getX(), lp.getY() - 1);
		}
		else if (cameraAngle < 2048 * 11/16){
			return new LocalPoint(lp.getX() - 1, lp.getY() - 1);
		}
		else if (cameraAngle < 2048 * 13/16){
			return new LocalPoint(lp.getX() - 1, lp.getY());
		}
		else if (cameraAngle < 2048 * 15/16){
			return new LocalPoint(lp.getX() - 1, lp.getY() + 1);
		}
		else
		{
			return new LocalPoint(lp.getX(), lp.getY() + 1);
		}
	}

//	private void handleAxes(){
//		switch (axeSpawnedTicks){
//			case 0:
//				spawnTendrils();
//				break;
//			case 1:
//			case 2:
//				break;
//			case 3:
//				for (FakeAxe i : fakeAxes){
//					i.axe.setActive(true);
//				}
////				break;
//				// TODO: when tendrils finish, do they get cleared from the list? most likely not
//			case 4:
//
//			case 5:
//			case 6:
//			case 7:
//				updateAxeMoveTarget();
//				break;
//			default:
//				for (FakeAxe i : fakeAxes){
//					i.axe.setActive(false);
//				}
//				fakeAxes.clear();
//		}
//	}
//	private void spawnTendrils(){
//		fakeTendrils.clear();
//		fakeAxes.clear();
//
//		// in future: eventually change 7 to 15 if i add horizontal axes
//		List<Integer> possibleAxeSpawns = IntStream.range(0, 8).boxed().collect(Collectors.toList());
//
//		possibleAxeSpawns.removeAll(realAxes);
//
//		client.playSoundEffect(7078);
//
//		for (int i = 0; i < 8; i++)
//		{
//			if (possibleAxeSpawns.isEmpty())
//			{
//				return;
//			}
//
//			int spot = possibleAxeSpawns.get(ThreadLocalRandom.current().nextInt(possibleAxeSpawns.size()));
//
//			WorldPoint spawnPos = tendrilNumToWorldPoint(spot);
//			assert spawnPos != null;
//
//			int orientation = tendrilNumToOrientation(spot);
//
//			RuneLiteObject tendril = client.createRuneLiteObject();
//
//			tendril.setModel(client.loadModel(49300));
//			tendril.setAnimation(client.loadAnimation(10364));
//			tendril.setLocation(LocalPoint.fromWorld(client, spawnPos), client.getPlane());
//			tendril.setRadius(384);
//			tendril.setOrientation(orientation);
//			tendril.setDrawFrontTilesFirst(false);
//			tendril.setActive(true);
//
//			fakeTendrils.add(tendril);
//
//			possibleAxeSpawns.remove((Object) spot);
//
//			// spawn axe here, give axe the same orientation as this tendril
//			RuneLiteObject axe = client.createRuneLiteObject();
//
//			axe.setModel(client.loadModel(49304));
//			axe.setAnimation(client.loadAnimation(10366));
//			axe.setShouldLoop(true);
//			axe.setRadius(384);
//			axe.setOrientation(orientation);
//			axe.setDrawFrontTilesFirst(false);
//			axe.setLocation(LocalPoint.fromWorld(client, spawnPos), client.getPlane());
//
//			FakeAxe fakeAxe = new FakeAxe(axe, arenaSWCorner, spot);
//			fakeAxes.add(fakeAxe);
//		}
//	}
//	private void updateAxeMoveTarget(){
//		for (FakeAxe i : fakeAxes){
//			i.nextPoint = i.path.get(axeSpawnedTicks - 3);
//			LocalPoint target = LocalPoint.fromWorld(client, i.nextPoint);
//
//			assert target != null;
//			i.stepX = (target.getX() - i.axe.getLocation().getX()) / 30;
//			i.stepY = (target.getY() - i.axe.getLocation().getY()) / 30;
//		}
//	}
//	private int worldPointToTendrilNum(NPC tendril){
//		// 5 6 7
//		// 3   4
//		// 0 1 2
//
//		if (arenaSWCorner == null){
//			log.warn("trying to find tendril position but we don't have the rock location");
//			return -1;
//		}
//		int x = tendril.getWorldLocation().getX() - arenaSWCorner.getX();
//		int y = tendril.getWorldLocation().getY() - arenaSWCorner.getY();
//
//		// had way too much fun trying to minimise this. going to be annoying to debug later
//		if (x == 5){
//			return 3 + y/10;
//		}
//		else {
//			return x/5 + y/2;
//		}
//	}
//	private WorldPoint tendrilNumToWorldPoint(int i){
//		if (arenaSWCorner == null){
//			log.warn("trying to find axe position but we don't have the rock location");
//			return null;
//		}
//
//		if (i < 3){
//			return arenaSWCorner.dx(5*i);
//		}
//		else if (i < 5){
//			return arenaSWCorner.dy(5).dx(10*(i-3));
//		}
//		else if (i < 8){
//			return arenaSWCorner.dy(10).dx(5*(i-5));
//		}
//		// in future: add funky axe spots
//		else return null;
//	}
//	private int tendrilNumToOrientation(int i){
//		// in future: add straight axes
//		switch (i){
//			case 7:
//				return 256;
//			case 4:
//				return 512;
//			case 2:
//				return 768;
//			case 1:
//				return 1024;
//			case 0:
//				return 1280;
//			case 3:
//				return 1536;
//			case 5:
//				return 1792;
//			case 6:
//			default:
//				return 0;
//		}
//	}

	private static void createHeadProjectile(Client client, int projectileID, RuneLiteObject head)
	{
		int tileHeight = client.getTileHeights()[0]
		[head.getLocation().getSceneX()]
		[head.getLocation().getSceneY()];

		LocalPoint lp = head.getLocation();

		Projectile proj = client.createProjectile(projectileID,
			client.getPlane(),
			lp.getX(),
			lp.getY(),
			tileHeight - 100, // z coordinate
			client.getGameCycle() + 15,  // start cycle
			client.getGameCycle() + 75,  // end cycle
			8, // slope ???
			80, // start height
			80, // end height
			client.getLocalPlayer(),
			client.getLocalPlayer().getLocalLocation().getX(),
			client.getLocalPlayer().getLocalLocation().getY()
		);
		client.getProjectiles()
			.addLast(proj);
	}

	private void reset(){
		arenaSWCorner = null;
		inFight = false;
		bossAttackTimer = -1;
		nextPossibleOsu = -1;
		headSpawnedThisTick = false;
		axeSpawnedTicks = -1;
		missedPrayers = 0;
	}

//	private void moveAxes(){
//		for (RuneLiteObject tendril : fakeTendrils){
//			if (Objects.requireNonNull(tendril.getAnimation()).getId() == 10364)
//			{
//				if (tendril.getAnimationFrame() == tendril.getAnimation().getNumFrames() - 1)
//				{
//					tendril.setAnimation(client.loadAnimation(10365));
//				}
//			}
//			else if (tendril.getAnimation().getId() == 10365)
//			{
//				if (tendril.getAnimationFrame() == tendril.getAnimation().getNumFrames() - 1)
//				{
//					tendril.setModel(client.loadModel(49302));
//					tendril.setAnimation(client.loadAnimation(10336));
//				}
//			}
//		}
//		for (FakeAxe fakeAxe : fakeAxes){
//			if (fakeAxe.axe.isActive()){
//				LocalPoint newPos = new LocalPoint(fakeAxe.axe.getLocation().getX() + fakeAxe.stepX, fakeAxe.axe.getLocation().getY() + fakeAxe.stepY);
//				fakeAxe.axe.setLocation(newPos, client.getPlane());
//			}
//		}
//	}

//	private void debugtest(){
//		if (client.getGameState() != GameState.LOGGED_IN)
//			return;
//
//		clientThread.invokeLater(this::spawnSpike);
//	}
////	@Subscribe
//	private void onVarClientStrChanged(VarClientStrChanged e){
//		if (e.getIndex() != VarClientStr.CHATBOX_TYPED_TEXT)
//			return;
//
//		debugtest();
//	}
}
