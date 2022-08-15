package net.runelite.client.plugins.vorkathPlayer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.basicapi.BasicApiPlugin;
import net.runelite.client.plugins.basicapi.utils.BankingUtilsTest;
import net.runelite.client.plugins.basicapi.utils.Inventory;
import net.runelite.client.plugins.basicapi.utils.PrayerUtils;
import net.runelite.client.plugins.iutils.*;
import net.runelite.client.plugins.iutils.api.SpellBook;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.game.iNPC;
import net.runelite.client.plugins.iutils.game.iObject;
import net.runelite.client.plugins.iutils.scripts.ReflectBreakHandler;
import net.runelite.client.plugins.iutils.scripts.iScript;
import net.runelite.client.plugins.iutils.ui.Chatbox;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static net.runelite.api.GraphicID.VORKATH_BOMB_AOE;
import static net.runelite.api.GraphicID.VORKATH_ICE;
import static net.runelite.api.ObjectID.ACID_POOL_32000;
import static net.runelite.client.plugins.vorkathPlayer.VorkathPlayerStates.*;

@Extension
@PluginDependency(BasicApiPlugin.class)
@PluginDependency(iUtils.class)
@PluginDescriptor(
		name = "Vorkath Player",
		description = "Automatic Vorkath",
		tags = {"Vorkath"}
)
@Slf4j
public class VorkathPlayerPlugin extends iScript {

	@Inject
	private VorkathPlayerConfig config;

	@Inject
	private Client client;

	@Inject
	private Chatbox chatbox;

	@Inject
	private iUtils utils;

	@Inject
	private NPCUtils npcUtils;

	@Inject
	private PlayerUtils playerUtils;

	@Inject
	private WalkUtils walkUtils;

	@Inject
	private ObjectUtils objectUtils;

	@Inject
	private Game game;

	@Inject
	private CalculationUtils calc;

	@Inject
	private PrayerUtils prayerUtils;

	@Inject
	private BasicApiPlugin basicApi;

	@Inject
	private BankingUtilsTest bankUtils;

	@Inject
	private Inventory inventory;

	@Inject
	private ReflectBreakHandler chinBreakHandler;

	private LegacyMenuEntry targetMenu;

	public HashMap<Integer, Integer> inventoryItems;
	public HashMap<Integer, Integer> itemValues;

	List<String> includedItems;
	List<String> excludedItems;


	private boolean startPlugin = false;
	private int timeout;
	private boolean isAcid;
	private boolean isFireball;
	private boolean isMinion;
	private long sleepLength;
	private boolean hasSpecced;
	private boolean rechargeHelm;
	private boolean gotPet;
	private WorldPoint fireballPoint;
	private List<Integer> regions;
	public List<WorldPoint> acidSpots;
	public List<WorldPoint> acidFreePath;
	public List<WorldPoint> safeVorkathTiles;
	public WorldPoint safeWooxTile;
	private LocalPoint meleeBaseTile;
	private LocalPoint rangeBaseTile;
	public WorldArea fremmyArea;
	public List<TileItem> items;
	public boolean hasDied;
	public Collection<Integer> serpItems;


	private int DIAMOND_SET;
	private int RUBY_SET;

	private String oldState = "";

	private int getSpecId(){
		return config.useSpec().getItemId();
	}

	private int getMainhandId(){
		return config.mainhand();
	}

	private int getOffhandId(){
		return config.offhand();
	}

	private int getStaffId(){
		return config.staffID();
	}

	private int getFoodId(){
		return config.food().getId();
	}

	private int getMinDoses(){
		return config.minDoses();
	}

	private int getWalkMethod(){
		return config.walkMethod().getId();
	}

	public VorkathPlayerPlugin() {
		regions = Arrays.asList(7513, 7514, 7769, 7770, 8025, 8026);
		meleeBaseTile = new LocalPoint(6208, 7744);
		rangeBaseTile = new LocalPoint(6208, 7104);
		fremmyArea = new WorldArea(new WorldPoint(2613, 3645, 0), new WorldPoint(2693, 3716, 0));
		serpItems = Set.of(ItemID.ZULRAHS_SCALES, ItemID.SERPENTINE_HELM_UNCHARGED, ItemID.SERPENTINE_HELM);
		acidSpots = new ArrayList<>();
		acidFreePath = new ArrayList<>();
		safeVorkathTiles = new ArrayList<>();
		itemValues = new HashMap<>();
		items = new ArrayList<>();
		includedItems  = new ArrayList<>();
		excludedItems  = new ArrayList<>();
		safeWooxTile = null;
		fireballPoint = null;
		isMinion = false;
		isAcid = false;
		isFireball = false;
		rechargeHelm = false;
		gotPet = false;
		timeout = 0;
	}

	@Provides
	VorkathPlayerConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(VorkathPlayerConfig.class);
	}

	@Override
	protected void startUp() {
		log.info("Vorkath Player Started");
		chinBreakHandler.registerPlugin(this);
	}

	@Override
	protected void shutDown() {
		log.info("Vorkath Player Stopped");
		chinBreakHandler.unregisterPlugin(this);
	}

	@Override
	protected void onStart() {
		chinBreakHandler.startPlugin(this);
	}

	@Override
	protected void onStop() {
		inventoryItems.clear();
		acidFreePath .clear();
		safeVorkathTiles.clear();;
		acidFreePath.clear();
		startPlugin = false;
		hasDied = false;
		timeout = 0;
		isMinion = false;
		isAcid = false;
		isFireball = false;
		fireballPoint = null;
		safeWooxTile = null;
		hasSpecced = false;
		rechargeHelm = false;
		gotPet = false;
		chinBreakHandler.stopPlugin(this);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if(gameStateChanged.getGameState() == GameState.LOADING)
			timeout+=3;

		if(gameStateChanged.getGameState() == GameState.LOGIN_SCREEN && hasDied){
			stop();
		}
	}

	@Override
	protected void loop() {
		game.tick();
	}

	@Subscribe
	public void onGameTick(GameTick event){
		if(!startPlugin || chinBreakHandler.isBreakActive(this))
			return;

		final Player player = client.getLocalPlayer();
		final LocalPoint playerLocal = player.getLocalLocation();
		final WorldPoint baseTile = config.useRange() ? WorldPoint.fromLocal(client, rangeBaseTile) : WorldPoint.fromLocal(client, meleeBaseTile);
		final iNPC vorkathAlive = game.npcs().withId(NpcID.VORKATH_8061).nearest();
		final iNPC vorkathAsleep = game.npcs().withId(NpcID.VORKATH_8059).nearest();

		if(!isAcid()){
			isAcid = false;
			safeWooxTile = null;
			acidFreePath.clear();
			acidSpots.clear();
		}

		if(!isAtVorkath()){
			isMinion = false;
			isAcid = false;
			isFireball = false;
			hasSpecced = false;
			safeVorkathTiles.clear();
		}

		if(gotPet){
			if(!isInPOH() || isAtVorkath()){
				teleToPoH();
			}else{
				stop();
			}
			return;
		}

		if(hasDied){
			if(prayerUtils.isQuickPrayerActive()){
				prayerUtils.toggleQuickPrayer(false, sleepDelay());
			}else{
				basicApi.logout();
			}
			return;
		}

		if(timeout > 0){
			if(isAcid()){
				if (playerUtils.isRunEnabled()) {
					toggleRun(false, calc.getRandomIntBetweenRange(0, 30));
				}

				if(prayerUtils.isQuickPrayerActive()){
					prayerUtils.toggleQuickPrayer(false, calc.getRandomIntBetweenRange(100,150));
					if(config.useRange()) {
						prayerUtils.togglePrayer(true, Prayer.RIGOUR, calc.getRandomIntBetweenRange(100,150));
					}
					else {
						prayerUtils.togglePrayer(true, Prayer.PIETY, calc.getRandomIntBetweenRange(100,150));
					}
				}
			}
			--timeout;
			return;
		}

		if(config.debug())
			game.sendGameMessage("State: " + String.valueOf(getState()));

		//IMPORTANT CHECKS MOVED OUTSIDE OTHER LOGIC
		switch (getState()){
			case HANDLE_BREAK:
				chinBreakHandler.startBreak(this);
				break;
			case LOW_RUNES:
				game.sendGameMessage("PROBLEM: Incorrect / low runes. Please use Law/Chaos/Dust and have more than 100 of each IN THE RUNE POUCH LOL. Please don't come to discord and ask why this isnt working");
				stop();
				break;
			case SPELLBOOK_STOP:
				game.sendGameMessage("PROBLEM: Incorrect spellbook. Please switch to the normal spellbook. You can donate the 100k you would've lost to me if you want");
				stop();
				break;
		}

		if(isAtVorkath()){
			createSafetiles();
			switch (getState()){
				case EAT_FOOD:
					inventory.interactWithItem(getFoodId(), 0, "Eat");
					if(isVorkathAsleep())
						timeout+=3;
					break;
				case DRINK_ANTIVENOM:
					inventory.interactWithItem(inventory.getFirst(config.antivenom().getIds()).getId(), sleepDelay(), "Drink");
					timeout+=1;
					break;
				case DRINK_ANTIFIRE:
					inventory.interactWithItem(inventory.getFirst(config.antifire().getIds()).getId(), sleepDelay(), "Drink");
					timeout+=1;
					break;
				case DRINK_RESTORE:
					inventory.interactWithItem(inventory.getFirst(config.prayer().getIds()).getId(), sleepDelay(), "Drink");
					timeout+=1;
					break;
				case DRINK_BOOST:
					inventory.interactWithItem(inventory.getFirst(config.boostPotion().getIds()).getId(), sleepDelay(), "Drink");
					timeout+=1;
					break;
				case TOGGLE_RUN:
					toggleRun(false, sleepDelay());
					break;
				case PRAYER_ON:
					if(!prayerUtils.isQuickPrayerActive() && prayerUtils.getRemainingPoints() > 0)
						prayerUtils.toggleQuickPrayer(true, sleepDelay());
//					if(isSpecActive()) {
//						// toggle on piety for the spec attack
//						if(prayerUtils.getRemainingPoints() > 0) {
//							prayerUtils.togglePrayer(true, PIETY, sleepDelay());
//						}
//					}
					break;
				case PRAYER_OFF:
					if(prayerUtils.isQuickPrayerActive())
						prayerUtils.toggleQuickPrayer(false, sleepDelay());
//					if(isAcid() && prayerUtils.getRemainingPoints() > 0) {
//						if(config.useRange()) {
//							prayerUtils.togglePrayer(true, Prayer.RIGOUR, sleepDelay());
//						}
//						else {
//							prayerUtils.togglePrayer(true, Prayer.PIETY, sleepDelay());
//						}
//					}
					break;
				case EQUIP_MH:
					inventory.interactWithItem(getMainhandId(), sleepDelay(), "Wield");
//					if(config.useRange()) {
//						prayerUtils.togglePrayer(true, Prayer.RIGOUR, sleepDelay());
//					}
//					else {
//						prayerUtils.togglePrayer(true, Prayer.PIETY, sleepDelay());
//					}
					break;
				case EQUIP_OH:
					inventory.interactWithItem(getOffhandId(), sleepDelay(), "Wield", "Equip", "Wear");
					break;
				case EQUIP_SPEC:
					if(config.useSpec().getHands() == 2 && inventory.getFreeSlots() < 1){
						if(inventory.getFirst(config.food().getId()) != null){
							if(config.debug()){
								game.sendGameMessage("DEBUG: Eating food for spec");
							}
							inventory.interactWithItem(getFoodId(), 0, "Eat");
							return;
						}
					}

					inventory.interactWithItem(getSpecId(), sleepDelay(), "Wield");
					break;
				case EQUIP_STAFF:
					inventory.interactWithItem(getStaffId(), 0, "Wield");
					break;
				case TOGGLE_SPEC:
					toggleSpec();
					break;
				case POKE_VORKATH:
					actionNPC(vorkathAsleep.id(), MenuAction.NPC_FIRST_OPTION, sleepDelay());
					break;
				case LOOT_VORKATH:
					if(config.debug())
						game.sendGameMessage("Looting Item: " + client.getItemComposition(getLoot().getId()).getName() + " x" + getLoot().getQuantity() + " for a value of: " + (itemValues.containsKey(getLoot().getId()) ? itemValues.get(getLoot().getId()) * getLoot().getQuantity() : "Couldn't find price :("));
					if(!inventory.isFull() || (client.getItemComposition(getLoot().getId()).isStackable() && inventory.contains(getLoot().getId()))){
						if(!playerUtils.isMoving())
							lootItem(getLoot());
					}else{
						if(config.lootPrio() && inventory.contains(getFoodId())){
							if(config.debug())
								game.sendGameMessage("Eating food to make room for loot");
							inventory.interactWithItem(getFoodId(), sleepDelay(), "Eat");
							timeout+=1;
							return;
						}

						if((!hasFoodForKill() || !hasPrayerForKill() || !hasVenomForKill()) && itemToDrop(getLoot()) != null){
							if(config.debug())
								game.sendGameMessage("End of trip detected, prioritizing loot over inventory item: " + client.getItemComposition(itemToDrop(getLoot()).getId()).getName());
							prioritizeLoot();
							timeout+=1;
						}
					}

					break;
				case DISTANCE_CHECK:
					walkUtils.sceneWalk(baseTile, 0, sleepDelay());
					break;
				case SWITCH_DIAMOND:
					inventory.interactWithItem(DIAMOND_SET, sleepDelay(), "Wield");
					break;
				case SWITCH_RUBY:
					inventory.interactWithItem(RUBY_SET, sleepDelay(), "Wield");
					break;
				case ACID_WALK:
					if(config.eatWoox() && shouldEat()){
						inventory.interactWithItem(getFoodId(), calc.getRandomIntBetweenRange(120, 160), "Eat");
					}

					if(getWalkMethod() == 1) return;
					if(getWalkMethod() == 2){
						if(!acidSpots.isEmpty()){
							if(acidFreePath.isEmpty()){
								calculateAcidFreePath();
							}

							WorldPoint firstTile;
							WorldPoint lastTile;
							if(!acidFreePath.isEmpty()){
								firstTile = acidFreePath.get(0);
							}else{
								return;
							}

							if(acidFreePath.size() > 5){
								lastTile = acidFreePath.get(4);
							}else{
								lastTile = acidFreePath.get(acidFreePath.size() - 1);
							}
							log.info("First tile: " + firstTile);
							log.info("Last Tile: " + lastTile);
							log.info("Actual length: " + (firstTile.getX() != lastTile.getX() ? Math.abs(firstTile.getX() - lastTile.getX()) : Math.abs(firstTile.getY() - lastTile.getY())));
							LocalPoint localDestination = client.getLocalDestinationLocation();
							WorldPoint worldDestination = null;
							if(localDestination != null)
								worldDestination = WorldPoint.fromLocal(client, localDestination);

							if(acidFreePath.contains(player.getWorldLocation())){
								if(player.getWorldLocation().equals(firstTile)){
									walkUtils.sceneWalk(lastTile, 0, calc.getRandomIntBetweenRange(20, 40));
									return;
								}
								if(player.getWorldLocation().equals(lastTile)){
									walkUtils.sceneWalk(firstTile, 0, calc.getRandomIntBetweenRange(20, 40));
									return;
								}
							}else if(!player.isMoving() || (worldDestination == null || (!worldDestination.equals(firstTile) && !worldDestination.equals(lastTile)))){
								walkUtils.sceneWalk(lastTile, 0, 0);
							}

						}
					}
					else {
						Collections.sort(safeVorkathTiles, Comparator.comparingInt(o -> o.distanceTo(player.getWorldLocation())));

						if (safeWooxTile == null) {
							for (int i = 0; i < safeVorkathTiles.size(); i++) {
								WorldPoint temp = safeVorkathTiles.get(i);
								WorldPoint temp2 = new WorldPoint(temp.getX(), temp.getY() - 1 , temp.getPlane());
								if (!acidSpots.contains(temp) && !acidSpots.contains(temp2)) {
									safeWooxTile = temp2;
									break;
								}
							}
						}

						if(safeWooxTile != null){
							if(player.getWorldLocation().equals(safeWooxTile)){
								actionNPC(vorkathAlive.id(), MenuAction.NPC_SECOND_OPTION, calc.getRandomIntBetweenRange(0, 40));
							}else{
								LocalPoint lp = LocalPoint.fromWorld(client, safeWooxTile);
								if(lp != null){
									if(basicApi.useInvokes()){
										walkUtils.walkTile(lp.getSceneX(), lp.getSceneY());
									}else{
										walkUtils.sceneWalk(lp, 0, 0);
									}
								}else{
									log.info("Local point is a null");
								}
							}
						}
					}

					break;
				case KILL_MINION:
					NPC iceMinion = npcUtils.findNearestNpc(NpcID.ZOMBIFIED_SPAWN_8063);

					if(player.getInteracting() != null && player.getInteracting().getName().equalsIgnoreCase("Vorkath")){
						if(inventory.contains(getFoodId()) && game.modifiedLevel(Skill.HITPOINTS) <= game.baseLevel(Skill.HITPOINTS) - 25){
							inventory.interactWithItem(getFoodId(), sleepDelay(), "Eat");
						}else{
							walkUtils.sceneWalk(playerLocal, 0, sleepDelay());
						}
						return;
					}

					if(prayerUtils.isQuickPrayerActive()){
						prayerUtils.toggleQuickPrayer(false, sleepDelay());
						return;
					}

					if(iceMinion != null && player.getInteracting() == null) {
						attackMinion();
					}
					break;
				case DODGE_FIREBALL:
					LocalPoint bomb = LocalPoint.fromWorld(client, fireballPoint);
					LocalPoint dodgeRight = new LocalPoint(bomb.getX() + 256, bomb.getY()); //Local point is 1/128th of a tile. 256 = 2 tiles
					LocalPoint dodgeLeft = new LocalPoint(bomb.getX() - 256, bomb.getY());
					LocalPoint dodgeReset = new LocalPoint(6208, 7872);

					if(isFireball && !player.getWorldLocation().equals(fireballPoint)){
						if(player.getWorldLocation().distanceTo(fireballPoint) >= 2 && vorkathAlive != null){
							actionNPC(vorkathAlive.id(), MenuAction.NPC_SECOND_OPTION, sleepDelay());
						}
						fireballPoint = null;
						isFireball = false;
						return;
					}
					if(playerLocal.getY() > 7872){
						walkUtils.sceneWalk(dodgeReset, 0, 0);
						isFireball = false;
						timeout+=2;
						return;
					}
					if (playerLocal.getX() < 6208) {
						walkUtils.sceneWalk(dodgeRight, 0, 0);
					} else {
						walkUtils.sceneWalk(dodgeLeft, 0, 0);
					}
					break;
				case RETALIATE:
					if(vorkathAlive != null){
//						if(isSpecActive() && prayerUtils.getRemainingPoints() > 0) {
//							// toggle on piety for the spec attack
//							prayerUtils.togglePrayer(true, Prayer.PIETY, sleepDelay());
//						}
						actionNPC(vorkathAlive.id(), MenuAction.NPC_SECOND_OPTION, sleepDelay());
					}
					break;
				case TELEPORT_TO_POH:
					teleToPoH();
					break;
			}
		}
		else if (isInPOH()){
			if(prayerUtils.isQuickPrayerActive()){
				prayerUtils.toggleQuickPrayer(false, sleepDelay());
				return;
			}

			switch (getState()){
				case TOGGLE_RUN:
					toggleRun(false, sleepDelay());
					break;
				case USE_ALTAR:
					GameObject altar = new GameObjectQuery().filter(a -> a.getName().contains("Altar") && Arrays.stream(a.getActions()).anyMatch(b -> b.contains("Pray"))).result(client).first();

					if(altar != null && !player.isMoving())
						actionObject(altar.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, null);

					break;
				case USE_POOL:
					//GameObject pool = new GameObjectQuery().filter(a -> a.getName().toLowerCase().contains("pool") && Arrays.stream(a.getActions()).anyMatch(b -> b != null && b.contains("Drink"))).result(client).nearestTo(client.getLocalPlayer());
					iObject pool = game.objects().filter(a -> a.name().toLowerCase().contains("pool")).withAction("Drink").nearest();

					if(pool != null && !player.isMoving())
						actionObject(pool.id(), MenuAction.GAME_OBJECT_FIRST_OPTION, null);

					timeout+=1;
					break;
				case USE_PORTAL:
					/*GameObject portal = new GameObjectQuery().filter(a -> {
						return Arrays.stream(a.getActions()).anyMatch(b -> b != null && b.equalsIgnoreCase("Lunar Isle")) || (a.getName().contains("Lunar Isle") && Arrays.stream(a.getActions()).anyMatch(c -> c != null && c.contains("Enter")));
					}).result(client).nearestTo(client.getLocalPlayer());
					 */
					iObject portal1 = game.objects().filter(a -> a.actions().contains("Lunar Isle") || (a.name().contains("Lunar Isle") && a.actions().contains("Enter"))).nearest();
					if(portal1 != null && !player.isMoving()) {
						if (config.debug())
							game.sendGameMessage("Portal isn't null, attempting to use");
						actionObject(portal1.id(), MenuAction.GAME_OBJECT_FIRST_OPTION, null);
					}else if(portal1 == null){
						game.sendGameMessage("Severe error: Can't find portal. Attemping to teleport to reload instance. If that doesn't fix it, please stop and move your portal closer.");
						if(client.getGameState() != GameState.LOADING){
							if(optionExists("Cast teleport anyway")){
								chooseOption("Cast teleport anyway");
								timeout += 9;
							}else{
								teleToPoH();
								timeout += 1;
							}
						}
					}

					timeout+=1;
					break;
				case TIMEOUT:
					break;
			}
		}
		else if(isNearBank()){
			if(prayerUtils.isQuickPrayerActive()){
				prayerUtils.toggleQuickPrayer(false, sleepDelay());
				return;
			}

			switch(getState()){
				case TOGGLE_RUN:
					toggleRun(false, sleepDelay());
					break;
				case RECHARGE_HELM:
					if(isItemEquipped(ItemID.SERPENTINE_HELM)){
						rechargeHelm = false;
						break;
					}
					if(inventory.contains(ItemID.SERPENTINE_HELM)){
						inventory.interactWithItem(ItemID.SERPENTINE_HELM, 0, "Wear");
						timeout+=1;
					}
					if(inventory.containsExcept(serpItems)){
						if(!bankUtils.isOpen()){
							openBank();
						}else{
							bankUtils.depositAll();
						}
					}else{
						if(!inventory.contains(ItemID.ZULRAHS_SCALES)){
							if(!bankUtils.isOpen()){
								openBank();
							}else{
								if(!bankUtils.contains(client.getItemComposition(ItemID.ZULRAHS_SCALES).getName())){
									game.sendGameMessage("Ran out of zulrah scales, stopping");
									stop();
								}
								bankUtils.withdrawAllItem(ItemID.ZULRAHS_SCALES);
							}
						}else{
							if(!inventory.contains(ItemID.SERPENTINE_HELM_UNCHARGED)){
								if(isItemEquipped(ItemID.SERPENTINE_HELM_UNCHARGED)){
									game.equipment().withId(ItemID.SERPENTINE_HELM_UNCHARGED).unequip();
								}
							}else{
								if(bankUtils.isOpen()){
									bankUtils.close();
									return;
								}
								inventory.useItemOnItem(ItemID.SERPENTINE_HELM_UNCHARGED, ItemID.ZULRAHS_SCALES, sleepDelay());
								timeout+=1;
							}
						}
					}
					break;
				case FIX_GEAR:
					if(inventory.getFreeSlots() < 6) {
						if (bankUtils.isOpen()) {
							bankUtils.depositAll();
							timeout += 1;
						} else {
							openBank();
						}
						return;
					}

					if(getSpecId() != -1 && !isItemEquipped(getSpecId())){
						withdrawUse(false, new HashMap<Integer, Integer>() {{
							put(getSpecId(), 1);
						}}, "Wield");
						timeout=1;
						return;
					}

					if(getSpecId() == -1 && (!isItemEquipped(getMainhandId()) || !isItemEquipped(getOffhandId()))){
						withdrawUse(false, new HashMap<Integer, Integer>() {{
							if(!isItemEquipped(getMainhandId()))
								put(getMainhandId(), 1);
							if(getOffhandId() != 0 && !isItemEquipped(getOffhandId()))
								put(getOffhandId(), 1);
						}}, "Wield");
						timeout=1;
						return;
					}
					if(config.useRange() && config.useSwitches() && !playerUtils.isItemEquipped(Set.of(RUBY_SET))){
						withdrawUse(false, new HashMap<Integer, Integer>() {{
							put(RUBY_SET, -1);
						}}, "Wield");
					}
					break;
				case OVEREAT:
					if(inventory.getFreeSlots() < 8) {
						if(config.debug())
							game.sendGameMessage("Free slots is less than 8, depositing inventory");
						if (bankUtils.isOpen()) {
							bankUtils.depositAll();
							timeout += 1;
						} else {
							openBank();
						}
						return;
					}
					if((game.modifiedLevel(Skill.HITPOINTS) < game.baseLevel(Skill.HITPOINTS) || game.modifiedLevel(Skill.PRAYER) < game.baseLevel(Skill.PRAYER))) {
						withdrawUse(true, new HashMap<Integer, Integer>() {{
							if(game.modifiedLevel(Skill.HITPOINTS) < game.baseLevel(Skill.HITPOINTS))
								put(getFoodId(), 4);
							if(game.modifiedLevel(Skill.PRAYER) < game.baseLevel(Skill.PRAYER))
								put(config.prayer().getDose4(), 1);
						}},"Eat", "Drink");
						timeout+=1;
						return;
					}

					if(getFoodId() == ItemID.ANGLERFISH && game.modifiedLevel(Skill.HITPOINTS) <= game.baseLevel(Skill.HITPOINTS)){
						withdrawUse(false, new HashMap<Integer, Integer>() {{
							put(getFoodId(), 1);
						}},"Eat");
					}

					timeout+=1;
					break;
				case WITHDRAW_INVENTORY:
					if(inventory.containsExcept(inventoryItems.keySet())){
						if(config.debug())
							game.sendGameMessage("Found bad items, clearing inventory");
						if(bankUtils.isOpen()){
							bankUtils.depositAll();
							timeout+=1;
						}else{
							openBank();
						}
						return;
					}

					if(!bankUtils.isOpen()){
						openBank();
						return;
					}

					for(int id : inventoryItems.keySet()){

						String name = client.getItemComposition(id).getName();
						int amount = inventoryItems.get(id);


						if((!bankUtils.contains(id) && !inventory.contains(id)) || ((bankUtils.getQuantity(id) == -1 ? 0 : bankUtils.getQuantity(id)) + inventory.getCount(false,id) < amount) || (name.contains("bolts") && bankUtils.getQuantity(id) + inventory.getCount(true, id) < 50)){
							game.sendGameMessage("Failed: Couldn't withdraw item: " + name + " for the amount: " + (name.contains("bolts") ? "50" : amount));
							stop();
							return;
						}

						boolean var = name.contains("bolts") ? inventory.contains(id) : inventory.getCount(false, id) == amount;

						if(var)
							continue;

						if(config.debug())
							game.sendGameMessage("Banking for item: " + name + " : " + amount);

						if(!name.contains("bolts") && inventory.getCount(false, id) > amount) {
							bankUtils.depositAll();
							return;
						}

						if(name.contains("bolts")){
							bankUtils.withdrawAllItem(id);
						}else{
							bankUtils.withdrawItemAmount(id, amount - inventory.getCount(false, id));
						}
						timeout += inventoryItems.get(id) == 1 ? 0 : 4;
						return;
					}
					break;
				case LEAVE_BANK:
					switch(config.rellekkaTeleport().getOption()){
						case 0:
							GameObject badBooth = objectUtils.getGameObjectAtWorldPoint(new WorldPoint(2098, 3920, 0));
							if(chatboxIsOpen()){
								continueChat();
							}else
							if(badBooth != null && !player.isMoving()){
								actionObject(badBooth.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION, badBooth.getWorldLocation());
								timeout+=2;
							}
							break;
						case 1:
							inventory.interactWithItem(ItemID.FREMENNIK_SEA_BOOTS_4, sleepDelay(), "Teleport");
							timeout+=9;
							break;
						case 29712:
							GameObject returnOrb = objectUtils.findNearestGameObject(29712);

							if(!chatboxIsOpen()){
								if(returnOrb != null && !player.isMoving()){
									actionObject(returnOrb.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, null);
								}
							}else{
								continueChat();
								timeout+=16;
							}
					}
					break;
			}
		}
		else {
			if(prayerUtils.isQuickPrayerActive()){
				prayerUtils.toggleQuickPrayer(false, sleepDelay());
				return;
			}

			switch (getState()){
				case TELEPORT_TO_POH:
					teleToPoH();
					break;
				case USE_BOAT:
					GameObject boat = new GameObjectQuery().idEquals(29917).result(client).first();
					if(boat != null){
						actionObject(boat.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, null);
					}else if(!player.isMoving()){
						walkUtils.sceneWalk(new WorldPoint(2641, 3686, 0), 2, sleepDelay());
					}
					break;
				case USE_OBSTACLE:
					actionObject(31990, MenuAction.GAME_OBJECT_FIRST_OPTION, null);
					break;
			}
		}

	}

	public void withdrawUse(boolean consumeableOverride, HashMap<Integer, Integer> set, String... actions){

		List<Integer> temp = new ArrayList<>(set.keySet()){{
			removeIf(a -> equipment.isEquipped(a));
		}};

		Collection<Integer> temp1 = new ArrayList<>(temp);

		if(inventory.containsAllOf(temp) || (consumeableOverride && (game.modifiedLevel(Skill.HITPOINTS) >= game.baseLevel(Skill.HITPOINTS) || inventory.contains(getFoodId())) && (game.modifiedLevel(Skill.PRAYER) >= game.baseLevel(Skill.PRAYER) || inventory.contains(a -> client.getItemComposition(a.getId()).getName().contains("Prayer potion"))))){
			if(bankUtils.isOpen()){
				bankUtils.close();
				return;
			}
			if(consumeableOverride && inventory.contains(a -> client.getItemComposition(a.getId()).getName().contains("Prayer")) && client.getBoostedSkillLevel(Skill.PRAYER) < client.getRealSkillLevel(Skill.PRAYER)){
				inventory.interactWithItem(inventory.getFirst(a -> client.getItemComposition(a.getId()).getName().contains("Prayer")).getId(), sleepDelay(), actions);
				return;
			}
			inventory.interactWithItem(temp1.stream().mapToInt(Integer::intValue).toArray(), sleepDelay(), actions);
		}else{
			withdrawList(set);
		}
	}

	boolean chatboxIsOpen() {
		return chatbox.chatState() == Chatbox.ChatState.NPC_CHAT || chatbox.chatState() == Chatbox.ChatState.PLAYER_CHAT || chatbox.chatState() == Chatbox.ChatState.OPTIONS_CHAT;
	}

	public void withdrawList(HashMap<Integer, Integer> list){
		if(bankUtils.isOpen()){
			for(int id : list.keySet()){
				if(!inventory.contains(id)){
					if(config.debug())
						game.sendGameMessage("Banking for: " + client.getItemComposition(id).getName() + " : ID: " + id + " : Amount: " + list.get(id));

					int amount = list.get(id);
					boolean value = amount == -1 ? bankUtils.contains(id) : bankUtils.contains(id, amount);

					if(value){
						if(amount == -1) {
							bankUtils.withdrawAllItem(id);
						}else {
							bankUtils.withdrawItemAmount(id, amount);
							timeout += 1;
						}
					}else{
						game.sendGameMessage("Failed to find item: " + client.getItemComposition(id).getName());
						stop();
					}
				}else{
					continue;
				}
				return;
			}
		}else{
			openBank();
		}
	}

	@Subscribe
	private void onChatMessage(ChatMessage ev){
		if(!startPlugin || ev.getType() != ChatMessageType.GAMEMESSAGE) return;

		String message = ev.getMessage();

		String deathMessage = "Oh dear, you are dead!";
		String serpHelm = "Your serpentine helm has run out of";
		if(message.contains("You have a funny feeling")) {
			gotPet = true;
			teleToPoH();
			return;
		}
		if(message.equalsIgnoreCase(deathMessage)){
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			Date date = new Date();
			game.sendGameMessage("Died at: " + format.format(date));
			hasDied = true;
		}
		if(message.contains(serpHelm)){
			teleToPoH();
			rechargeHelm = true;
		}
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("VorkathPlayerConfig"))
			return;

		if (configButtonClicked.getKey().equalsIgnoreCase("startVorkath")) {
			if (!startPlugin) {
				game.sendGameMessage("Vorkath Started, please wait a moment for item values to be cached");
				startPlugin = true;
				includedItems = Arrays.asList(config.includedItems().toLowerCase().split("\\s*,\\s*"));
				excludedItems = Arrays.asList(config.excludedItems().toLowerCase().split("\\s*,\\s*"));
				inventoryItems = new HashMap<>();
				initInventoryItems();
				start();
			} else {
				game.sendGameMessage("Vorkath stopped");
				startPlugin = false;
				stop();
			}
		}
	}

	@Subscribe
	private void onAnimationChanged(AnimationChanged event) {
		if(!startPlugin || event == null) return;

		final Player player = client.getLocalPlayer();

		final WorldPoint playerLocation = player.getWorldLocation();
		final LocalPoint playerLocalPoint = player.getLocalLocation();
		final Actor actor = event.getActor();

		if (actor == player) {
			if (actor.getAnimation() == 7642 || actor.getAnimation() == 1378 || actor.getAnimation() == 7514)  { //bgs dwh claws
				hasSpecced = true;
			}
		}
		if(actor.getAnimation() == 7949){ //death animation
			hasSpecced = false;
			isMinion = false;
			isAcid = false;
		}
		if(actor.getAnimation() == 7889){
			timeout+=4;
			isMinion = false;
		}
		if(actor.getAnimation() == 7957 && actor.getName().equalsIgnoreCase("Vorkath")){
			timeout=1;
		}
	}

	@Subscribe
	private void onProjectileSpawned(ProjectileSpawned event) {
		if(client.getLocalPlayer() == null) return;

		final Player player = client.getLocalPlayer();

		final Projectile projectile = event.getProjectile();

		final WorldPoint playerWorldLocation = player.getWorldLocation();

		final LocalPoint playerLocalLocation = player.getLocalLocation();

		if (projectile.getId() == VORKATH_BOMB_AOE){
			fireballPoint = player.getWorldLocation();
			isFireball = true;
		}

		if (projectile.getId() == VORKATH_ICE) {
			isMinion = true;
		}

		if(projectile.getId() == 1483)
			isAcid = true;
	}

	@Subscribe
	private void onProjectileMoved(final ProjectileMoved event) {
		Projectile projectile = event.getProjectile();
		LocalPoint position = event.getPosition();
		WorldPoint.fromLocal(
				client,
				position);
		if(client.getLocalPlayer() != null) {
			LocalPoint fromWorld = LocalPoint.fromWorld(client, client.getLocalPlayer().getWorldLocation());

			if (projectile.getId() == 1483) {
				addAcidSpot(WorldPoint.fromLocal(client, position));
			}
		}
	}

	/* Functions below */

	private VorkathPlayerStates getState(){

		Player player = client.getLocalPlayer();

		if(SpellBook.getCurrentSpellBook(game) != SpellBook.Type.STANDARD){
			return SPELLBOOK_STOP;
		}

		if(hasLowRunes()){
			return LOW_RUNES;
		}

		if(!isAtVorkath() && chinBreakHandler.shouldBreak(this)){
			return HANDLE_BREAK;
		}

		if(isAtVorkath()) {
			iNPC vorkathAlive = game.npcs().withId(NpcID.VORKATH_8061).nearest();
			final WorldPoint baseTile = config.useRange() ? WorldPoint.fromLocal(client, rangeBaseTile) : WorldPoint.fromLocal(client, meleeBaseTile);

			if (player.getAnimation() == 839) //Climbing over rock
				return TIMEOUT;


			if(isAcid)
				return ACID_WALK;

			if(prayerUtils.isQuickPrayerActive()){
				if(isVorkathAsleep() || (vorkathAlive != null && vorkathAlive.isDead()) || isMinion || isAcid){
					return PRAYER_OFF;
				}
			}

			if(!prayerUtils.isQuickPrayerActive() && prayerUtils.getRemainingPoints() > 0){
				if(!isAcid && !isMinion){
					if(isWakingUp() || (vorkathAlive != null && !vorkathAlive.isDead())){
						return PRAYER_ON;
					}
				}
			}

			if(isFireball)
				return DODGE_FIREBALL;

			if(isMinion){
				if(config.useStaff() && !isItemEquipped(getStaffId()))
					return EQUIP_STAFF;
				return KILL_MINION;
			}

			if(shouldEat()){
				if(!isVorkathAsleep() && inventory.getFirst(config.food().getId()) == null && game.modifiedLevel(Skill.HITPOINTS) <= config.eatAt() && vorkathAlive != null && !vorkathAlive.isDead())
					return TELEPORT_TO_POH;
				if(inventory.contains(getFoodId()))
					return EAT_FOOD;
			}

			if(shouldDrinkVenom()){
				if(!isVorkathAsleep() && !inventory.contains(config.antivenom().getIds()) && vorkathAlive != null && !vorkathAlive.isDead() && (calculateHealth(vorkathAlive) > 75))
					return TELEPORT_TO_POH;
				if(inventory.contains(config.antivenom().getIds()))
					return DRINK_ANTIVENOM;
			}

			if(shouldDrinkAntifire()){
				if(!isVorkathAsleep() && !inventory.contains(config.antifire().getIds()) && vorkathAlive != null && !vorkathAlive.isDead())
					return TELEPORT_TO_POH;
				if(inventory.contains(config.antifire().getIds()))
					return DRINK_ANTIFIRE;
			}

			if(shouldDrinkBoost()){
				if(inventory.contains(config.boostPotion().getIds())){
					if (client.getItemComposition(config.boostPotion().getDose4()).getName().contains("Divine")) {
						if (game.modifiedLevel(Skill.HITPOINTS) > 48 && inventory.getFirst(config.food().getId()) != null) {
							return DRINK_BOOST;
						}
					} else {
						return DRINK_BOOST;
					}
				}
			}

			if(shouldDrinkRestore()){
				if(!isVorkathAsleep() && !inventory.contains(config.prayer().getIds()) && (vorkathAlive != null && !vorkathAlive.isDead()) && prayerUtils.getRemainingPoints() == 0)
					return TELEPORT_TO_POH;
				if(inventory.contains(config.prayer().getIds()))
					return DRINK_RESTORE;
			}

			if(!playerUtils.isRunEnabled() && !isAcid())
				return TOGGLE_RUN;

			if((vorkathAlive != null || isWakingUp())
					&& !isAcid
					&& !isMinion
					&& !isFireball
					&& (!canSpec() || config.useRange())
					&& !shouldLoot()
					&& !player.isMoving()
					&& baseTile.distanceTo(player.getWorldLocation()) >= 4)
				return DISTANCE_CHECK;

			if(canSpec() && (isWakingUp() || (vorkathAlive != null && !vorkathAlive.isDead() && (calculateHealth(vorkathAlive) == -1 || calculateHealth(vorkathAlive) >= 750)))){
				if(isItemEquipped(getSpecId())){
					//if(isSpecActive() && (player.getInteracting() == null || !player.getInteracting().getName().equalsIgnoreCase("Vorkath"))){
					prayerUtils.togglePrayer(true, Prayer.PIETY, calc.getRandomIntBetweenRange(100,150));
					if(isSpecActive()){
						return RETALIATE;
					}else{
						return TOGGLE_SPEC;
					}
				}else{
					return EQUIP_SPEC;
				}
			}
			if(config.useSwitches() && (vorkathAlive == null || vorkathAlive.isDead()) && playerUtils.isItemEquipped(Set.of(DIAMOND_SET)) && inventory.contains(RUBY_SET))
				return SWITCH_RUBY;

			if(config.useSwitches() && vorkathAlive != null && !vorkathAlive.isDead()
					&& playerUtils.isItemEquipped(Set.of(RUBY_SET))
					&& inventory.contains(DIAMOND_SET)
					&& calculateHealth(vorkathAlive) > 0
					&& calculateHealth(vorkathAlive) < 260
					&& vorkathAlive.animation() != 7960
					&& vorkathAlive.animation() != 7957)
				return SWITCH_DIAMOND;

			if(!config.useStaff() || (config.useStaff() && !isMinion)){
				if(vorkathAlive != null && !isItemEquipped(getMainhandId()) && inventory.contains(getMainhandId()))
					if(config.useRange()) {
						prayerUtils.togglePrayer(true, Prayer.RIGOUR, calc.getRandomIntBetweenRange(100,150));
					}
					return EQUIP_MH;
				if(vorkathAlive != null && getOffhandId() != 0 && !isItemEquipped(getOffhandId()) && inventory.contains(getOffhandId()))
					return EQUIP_OH;
			}

			if(player.getInteracting() == null && vorkathAlive != null && !vorkathAlive.isDead()
					&& !isMinion
					&& !isFireball
					&& !isAcid)
				return RETALIATE;

			if(shouldLoot()){
				return LOOT_VORKATH;
			}

			if(isVorkathAsleep() && !shouldLoot()
					&& !player.isMoving()
					&& game.modifiedLevel(Skill.HITPOINTS) >= game.baseLevel(Skill.HITPOINTS) - 20
					&& hasFoodForKill()
					&& hasPrayerForKill()
					&& hasVenomForKill()){
				return POKE_VORKATH;
			}

			if(isVorkathAsleep() && !shouldLoot() && (!hasFoodForKill() || !hasVenomForKill() || !hasPrayerForKill()))
				return TELEPORT_TO_POH;
		}

		if(isInPOH()) {
			GameObject pool = new GameObjectQuery().filter(a -> a.getName().toLowerCase().contains("pool") && Arrays.stream(a.getActions()).anyMatch(b -> b != null && b.contains("Drink"))).result(client).first();

			if(!playerUtils.isRunEnabled()) return TOGGLE_RUN;
			if(prayerUtils.isQuickPrayerActive()) return PRAYER_OFF;
			if(config.useAltar() && game.modifiedLevel(Skill.PRAYER) < game.baseLevel(Skill.PRAYER))
				return USE_ALTAR;

			if (config.usePool() && pool != null &&
					((pool.getName().toLowerCase().contains("ornate") && game.modifiedLevel(Skill.HITPOINTS) < game.baseLevel(Skill.HITPOINTS))
					|| ((!pool.getName().toLowerCase().contains("restoration") && !pool.getName().toLowerCase().contains("revitalisation")) && game.modifiedLevel(Skill.PRAYER) < game.baseLevel(Skill.PRAYER))
					|| (getSpecialPercent() < 100))) {
				return USE_POOL;
			}

			return USE_PORTAL;
		}

		if(isNearBank()){
			if(rechargeHelm)
				return RECHARGE_HELM;

			if(!isGeared())
				return FIX_GEAR;

			if(shouldEatAtBank())
				return OVEREAT;

			if(!checkItems())
				return WITHDRAW_INVENTORY;

			return LEAVE_BANK;
		}

		if(shouldUseBoat() && !player.isMoving()){
			return USE_BOAT;
		}

		if(shouldUseObstacle() && !player.isMoving() && player.getAnimation() == -1){
			return USE_OBSTACLE;
		}

		if(!isNearBank() && !isAtVorkath() && !isInPOH() && !shouldUseObstacle() && !shouldUseBoat()){
			return TELEPORT_TO_POH;
		}

		return TIMEOUT;
	}

	public void createSafetiles(){
		if(isAtVorkath()){
			if(safeVorkathTiles.size() > 8) safeVorkathTiles.clear();
			LocalPoint southWest = getWalkMethod() == 3 ? new LocalPoint(5824, 7872) : getWalkMethod() == 4 ? new LocalPoint(5824, 7104) : new LocalPoint(5824, 7360);
			WorldPoint base = WorldPoint.fromLocal(client, southWest);
			for(int i = 0; i < 7; i++){
				safeVorkathTiles.add(new WorldPoint(base.getX() + i, base.getY(), base.getPlane()));
			}
		}else if(!isAtVorkath() && !safeVorkathTiles.isEmpty()){
			safeVorkathTiles.clear();
		}
	}

	private boolean isInPOH() {
		return Arrays.stream(client.getMapRegions()).anyMatch(regions::contains);
	}

	public boolean isAtVorkath(){
		iNPC vorkath = game.npcs().withName("Vorkath").nearest();
		return client.isInInstancedRegion() && vorkath != null;
	}

	public boolean isSpecActive(){
		return game.varp(VarPlayer.SPECIAL_ATTACK_ENABLED.getId()) == 1;
	}

	public boolean shouldDrinkBoost(){
		if(config.boostPotion().getDose4() == -1) return false;

		Skill skill = config.boostPotion().getSkill();
		return game.modifiedLevel(skill) <= (game.baseLevel(skill) + config.boostLevel());
	}

	public boolean shouldDrinkVenom() {
		return !isItemEquipped(ItemID.SERPENTINE_HELM) && game.varp(VarPlayer.POISON.getId()) > 0;
	}

	public boolean shouldEat(){
		return (client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.eatAt()) || (isAtVorkath() && isVorkathAsleep() && game.modifiedLevel(Skill.HITPOINTS) <= game.baseLevel(Skill.HITPOINTS) - 20);
	}

	public boolean shouldDrinkAntifire(){
		return config.antifire().name().toLowerCase().contains("super") ? game.varb(6101) == 0 : game.varb(3981) == 0;
	}

	public boolean shouldDrinkRestore(){
		return prayerUtils.getRemainingPoints() <= config.restoreAt();
	}

	public boolean isNearBank(){
		iNPC goodBanker = game.npcs().withName("'Bird's-Eye' Jack").nearest();
		return goodBanker != null;
	}

	public boolean isItemEquipped(int id){
		return game.equipment().withId(id).exists();
	}

	private void addAcidSpot(WorldPoint worldPoint) {
		if (!acidSpots.contains(worldPoint))
			acidSpots.add(worldPoint);
	}

	public boolean shouldLoot(){
		if(!isVorkathAsleep() || getLoot() == null) return false;

		if(getLoot().getId() == ItemID.SUPERIOR_DRAGON_BONES){
			if(inventory.isFull() && config.lootBonesIfRoom() && hasPrayerForKill() && hasFoodForKill() && hasVenomForKill()) return false;
		}
		if(config.lootPrio()){
			if(inventory.isFull() && (inventory.contains(getFoodId()) || itemToDrop(getLoot()) != null)) return true;
		}
		if(getSpecId() != -1 && config.useSpec().getHands() == 2 && getSpecialPercent() >= config.useSpec().getSpecAmt() && inventory.getFreeSlots() == 1 && hasPrayerForKill() && hasFoodForKill() && hasVenomForKill())
			return false;

		return (!inventory.isFull()) || ((!hasFoodForKill() || !hasPrayerForKill() || !hasVenomForKill()) && itemToDrop(getLoot()) != null) || (client.getItemComposition(getLoot().getId()).isStackable() && inventory.contains(getLoot().getId()));
	}

	public TileItem getLoot() {
		Set<TileItem> items = iUtils.tileItems;

		if (items.isEmpty())
			return null;

		List<TileItem> filtered = items.stream().filter(a -> {
			int value = 0;
			String name = "";
			if (itemValues.containsKey(a.getId())) {
				value = itemValues.get(a.getId()) * a.getQuantity();
			} else {
				itemValues.put(a.getId(), a.getId() == ItemID.VORKATHS_HEAD_21907 ? 75000 : utils.getItemPrice(a.getId(), true));
			}
			name = client.getItemComposition(a.getId()).getName().toLowerCase();
			return (config.excludedItems().isBlank() || excludedItems.stream().noneMatch(name::contains)) && (includedItems.stream().anyMatch(name::contains) || value >= config.lootValue() || (a.getId() == ItemID.BLUE_DRAGONHIDE + 1) || (config.lootBones() && a.getId() == ItemID.SUPERIOR_DRAGON_BONES));

		}).sorted(Comparator.comparingInt(b -> itemValues.get(b.getId()) * b.getQuantity())).collect(Collectors.toList());

		Collections.reverse(filtered);

		if(!filtered.isEmpty()){
			if(filtered.get(0).getId() == ItemID.SUPERIOR_DRAGON_BONES)
				return filtered.stream().filter(a -> a.getId() == ItemID.SUPERIOR_DRAGON_BONES).sorted(Comparator.comparingInt(o -> o.getTile().getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()))).findFirst().get();
			if(filtered.get(0).getId() == ItemID.BLUE_DRAGONHIDE)
				return filtered.stream().filter(a -> a.getId() == ItemID.BLUE_DRAGONHIDE).sorted(Comparator.comparingInt(o -> o.getTile().getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()))).findFirst().get();

			return filtered.get(0);
		}else{
			if(!items.isEmpty() && !inventory.isFull() && (!hasFoodForKill() || !hasVenomForKill() || !hasPrayerForKill())){
				List<TileItem> remains = items.stream().filter(a -> {
					String name = client.getItemComposition(a.getId()).getName().toLowerCase();
					return (config.excludedItems().isBlank() || excludedItems.stream().noneMatch(name::contains));
				}).sorted(Comparator.comparingInt(b -> itemValues.get(b.getId()) * b.getQuantity())).collect(Collectors.toList());
				Collections.reverse(remains);
				if(!remains.isEmpty())
					return remains.get(0);
			}
		}
		return null;
	}

	public boolean isVorkathAsleep(){
		iNPC vorkathAsleep = game.npcs().withId(NpcID.VORKATH_8059).first();
		return isAtVorkath() && vorkathAsleep != null;
	}

	public boolean isWakingUp(){
		NPC vorkathWaking = npcUtils.findNearestNpc(NpcID.VORKATH_8058);
		return isAtVorkath() && vorkathWaking != null;
	}

	public boolean isAcid(){
		GameObject pool = objectUtils.findNearestGameObject(ACID_POOL_32000);
		NPC vorkath = npcUtils.findNearestNpc(NpcID.VORKATH_8061);
		return pool != null || (vorkath != null && vorkath.getAnimation() == 7957);
	}

	private int calculateHealth(iNPC target) {
		if (target == null || target.name() == null)
		{
			return -1;
		}

		final int healthScale = target.getHealthScale();
		final int healthRatio = target.getHealthRatio();
		final int maxHealth = 750;

		if (healthRatio < 0 || healthScale <= 0)
		{
			return -1;
		}

		return (int)((maxHealth * healthRatio / healthScale) + 0.5f);
	}

	private void calculateAcidFreePath() {
		acidFreePath.clear();

		Player player = client.getLocalPlayer();
		NPC vorkath = npcUtils.findNearestNpc(NpcID.VORKATH_8061);

		if (vorkath == null)
		{
			return;
		}

		final int[][][] directions = {
				{
						{0, 1}, {0, -1} // Positive and negative Y
				},
				{
						{1, 0}, {-1, 0} // Positive and negative X
				}
		};

		List<WorldPoint> bestPath = new ArrayList<>();
		double bestClicksRequired = 99;

		final WorldPoint playerLoc = client.getLocalPlayer().getWorldLocation();
		final WorldPoint vorkLoc = vorkath.getWorldLocation();
		final int maxX = vorkLoc.getX() + 14;
		final int minX = vorkLoc.getX() - 8;
		final int maxY = vorkLoc.getY() - 1;
		final int minY = vorkLoc.getY() - 8;

		// Attempt to search an acid free path, beginning at a location
		// adjacent to the player's location (including diagonals)
		for (int x = -1; x < 2; x++)
		{
			for (int y = -1; y < 2; y++)
			{
				final WorldPoint baseLocation = new WorldPoint(playerLoc.getX() + x,
						playerLoc.getY() + y, playerLoc.getPlane());

				if (acidSpots.contains(baseLocation) || baseLocation.getY() < minY || baseLocation.getY() > maxY)
				{
					continue;
				}

				// Search in X and Y direction
				for (int d = 0; d < directions.length; d++)
				{
					// Calculate the clicks required to start walking on the path
					double currentClicksRequired = Math.abs(x) + Math.abs(y);
					if (currentClicksRequired < 2)
					{
						currentClicksRequired += Math.abs(y * directions[d][0][0]) + Math.abs(x * directions[d][0][1]);
					}
					if (d == 0)
					{
						// Prioritize a path in the X direction (sideways)
						currentClicksRequired += 0.5;
					}

					List<WorldPoint> currentPath = new ArrayList<>();
					currentPath.add(baseLocation);

					// Positive X (first iteration) or positive Y (second iteration)
					for (int i = 1; i < 25; i++)
					{
						final WorldPoint testingLocation = new WorldPoint(baseLocation.getX() + i * directions[d][0][0],
								baseLocation.getY() + i * directions[d][0][1], baseLocation.getPlane());

						if (acidSpots.contains(testingLocation) || testingLocation.getY() < minY || testingLocation.getY() > maxY
								|| testingLocation.getX() < minX || testingLocation.getX() > maxX)
						{
							break;
						}

						currentPath.add(testingLocation);
					}

					// Negative X (first iteration) or positive Y (second iteration)
					for (int i = 1; i < 25; i++)
					{
						final WorldPoint testingLocation = new WorldPoint(baseLocation.getX() + i * directions[d][1][0],
								baseLocation.getY() + i * directions[d][1][1], baseLocation.getPlane());

						if (acidSpots.contains(testingLocation) || testingLocation.getY() < minY || testingLocation.getY() > maxY
								|| testingLocation.getX() < minX || testingLocation.getX() > maxX)
						{
							break;
						}

						currentPath.add(testingLocation);
					}

					if (currentPath.size() >= 5 && currentClicksRequired < bestClicksRequired
							|| (currentClicksRequired == bestClicksRequired && currentPath.size() > bestPath.size()))
					{
						bestPath = currentPath;
						bestClicksRequired = currentClicksRequired;
					}
				}
			}
		}

		if (bestClicksRequired != 99)
		{
			acidFreePath = bestPath;
		}
	}

	private long sleepDelay() {
		sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

	private boolean hasFoodForKill(){
		return inventory.getFirst(config.food().getId()) != null && inventory.getCount(false, config.food().getId()) >= config.minFood();
	}

	private boolean hasPrayerForKill(){
		return getPotionDoses(config.prayer().getDose4()) >= getMinDoses() || game.modifiedLevel(Skill.PRAYER) > 70;
	}

	private boolean hasVenomForKill(){
		if(config.antivenom().getDose4() == ItemID.SERPENTINE_HELM) return true;
		return inventory.contains(config.antivenom().getIds()) || game.varp(VarPlayer.IS_POISONED.getId()) < -40;
	}

	private int getSpecialPercent(){
		return game.varp(VarPlayer.SPECIAL_ATTACK_PERCENT.getId()) / 10;
	}

	private boolean canSpec(){
		return getSpecId() != -1 && !hasSpecced && getSpecialPercent() >= config.useSpec().getSpecAmt();
	}

	private void toggleSpec(){
		Widget widget = client.getWidget(WidgetInfo.MINIMAP_SPEC_CLICKBOX);
		if(widget != null && canSpec()){
			targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP, -1, WidgetInfo.MINIMAP_SPEC_CLICKBOX.getId(), false);
			if (basicApi.useInvokes())
				utils.doInvokeMsTime(targetMenu, sleepDelay());
			else
				utils.doActionMsTime(targetMenu, widget.getBounds(), sleepDelay());
//			prayerUtils.togglePrayer(true, Prayer.PIETY, sleepDelay());
		}
		return;
	}

	private void toggleRun(boolean override, long value){
		Widget runOrb = client.getWidget(WidgetInfo.MINIMAP_TOGGLE_RUN_ORB);
		if(runOrb == null) return;
		targetMenu = new LegacyMenuEntry("Toggle Run", "", 1, MenuAction.CC_OP, -1, 10485783, false);
		if(basicApi.useInvokes())
			utils.doInvokeMsTime(targetMenu, override ? 0 : value);
		else
			utils.doActionMsTime(targetMenu, runOrb.getCanvasLocation(), override ? 0 : value);
	}

	private void actionNPC(int id, MenuAction action, long delay) {
		NPC target = npcUtils.findNearestNpc(id);
		if (target != null) {
			targetMenu = new LegacyMenuEntry("", "", target.getIndex(), action, target.getIndex(), 0, false);
			if (basicApi.useInvokes())
				utils.doInvokeMsTime(targetMenu, delay);
			else
				utils.doNpcActionMsTime(target, action.getId(), delay);
		}
		return;
	}

	private void actionObject(int id, MenuAction action, WorldPoint point) {
		GameObject obj = point == null ? objectUtils.findNearestGameObject(id) : objectUtils.getGameObjectAtWorldPoint(point);
		if (obj != null) {
			targetMenu = new LegacyMenuEntry("", "", obj.getId(), action, obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY(), false);
			if (basicApi.useInvokes())
				utils.doInvokeMsTime(targetMenu, sleepDelay());
			else
				utils.doGameObjectActionMsTime(obj, action.getId(), sleepDelay());
		}
		return;
	}

	public void attackMinion(){
		NPC iceMinion = npcUtils.findNearestNpc(NpcID.ZOMBIFIED_SPAWN_8063);
		Widget undead = client.getWidget(WidgetInfo.SPELL_CRUMBLE_UNDEAD);
		if(undead == null){
			game.sendGameMessage("Fatal error: Spell is null");
		}

		if(iceMinion != null) {
			LegacyMenuEntry entry = new LegacyMenuEntry("", "", iceMinion.getIndex(), MenuAction.WIDGET_TARGET_ON_NPC.getId(), 0, 0, false);
			utils.oneClickCastSpell(WidgetInfo.SPELL_CRUMBLE_UNDEAD, entry, iceMinion.getConvexHull().getBounds(), sleepDelay());
		}
		return;
	}

	public void openBank(){
		GameObject booth = objectUtils.findNearestGameObjectMenuWithin(new WorldPoint(2099, 3920, 0), 0, "Bank");
		if(booth != null && !playerUtils.isMoving())
			actionObject(booth.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION, new WorldPoint(2099, 3920, 0));
		return;
	}

	private void teleToPoH() {
		switch(config.houseTele().getId()){
			case ItemID.CONSTRUCT_CAPET:
			case ItemID.CONSTRUCT_CAPE:
			case ItemID.TELEPORT_TO_HOUSE:
				if(inventory.contains(config.houseTele().getId()))
					inventory.interactWithItem(config.houseTele().getId(), sleepDelay(), "Tele to POH", "Break");
				break;
			case -1:
				Widget widget = client.getWidget(WidgetInfo.SPELL_TELEPORT_TO_HOUSE);
				if (widget != null)
					utils.oneClickCastSpell(WidgetInfo.SPELL_TELEPORT_TO_HOUSE, new LegacyMenuEntry("", "", 1 , MenuAction.CC_OP, -1, widget.getId(), false), sleepDelay());
				break;
		}
	}

	public void initInventoryItems(){
		if(getSpecId() != -1){
			if(getMainhandId() != -1) {
				inventoryItems.put(getMainhandId(), 1);
			}
			if(config.useSpec().getHands() == 2){
				if(getOffhandId() != 0){
					inventoryItems.put(getOffhandId(), 1);
				}
			}
		}
		if(config.useStaff()){
			inventoryItems.put(config.staffID(), 1);
		}

		if(config.antivenom().getDose4() != ItemID.SERPENTINE_HELM){
			inventoryItems.put(config.antivenom().getDose4(), config.venomAmount());
		}

		if(config.useRange() && config.useSwitches()){
			if(config.useDragonBolts()){
				RUBY_SET = ItemID.RUBY_DRAGON_BOLTS_E;
				DIAMOND_SET = ItemID.DIAMOND_DRAGON_BOLTS_E;

				inventoryItems.put(ItemID.DIAMOND_DRAGON_BOLTS_E, -1);
			}else{
				RUBY_SET = ItemID.RUBY_BOLTS_E;
				DIAMOND_SET = ItemID.DIAMOND_BOLTS_E;

				inventoryItems.put(ItemID.DIAMOND_BOLTS_E, -1);
			}
		}

		if(config.houseTele().getId() != -1){
			inventoryItems.put(config.houseTele().getId(), 1);
		}
		if(config.rellekkaTeleport().getOption() == 1){
			inventoryItems.put(ItemID.FREMENNIK_SEA_BOOTS_4, 1);
		}

		if(config.boostPotion().getDose4() != -1){
			inventoryItems.put(config.boostPotion().getDose4(), 1);
		}

		inventoryItems.put(config.antifire().getDose4(), 1);
		inventoryItems.put(config.pouchID(), 1);
		inventoryItems.put(config.prayer().getDose4(), config.prayerAmount());

		inventoryItems.put(getFoodId(), config.withdrawFood());
	}

	public boolean checkItems(){
		for(int id : inventoryItems.keySet()){
			if(id == ItemID.DIAMOND_DRAGON_BOLTS_E || id == ItemID.DIAMOND_BOLTS_E){
				if(inventory.getCount(true, id) < 50)
					return false;
				continue;
			}
			if(inventory.getCount(false, id) != inventoryItems.get(id)){
				return false;
			}
		}
		return true;
	}

	public boolean isGeared(){
		if(getSpecId() != -1 && !isItemEquipped(getSpecId())) return false;
		if(getSpecId() == -1 && ((getMainhandId() != -1 && !isItemEquipped(getMainhandId())) || (getOffhandId() != 0 && !isItemEquipped(getOffhandId())))) return false;
		if(config.useRange() && config.useSwitches() && !playerUtils.isItemEquipped(Set.of(RUBY_SET))) return false;

		return true;
	}

	public boolean shouldEatAtBank(){
		return (game.modifiedLevel(Skill.PRAYER) < game.baseLevel(Skill.PRAYER)) || (game.modifiedLevel(Skill.HITPOINTS) < game.baseLevel(Skill.HITPOINTS)) || (getFoodId() == ItemID.ANGLERFISH && game.modifiedLevel(Skill.HITPOINTS) <= game.baseLevel(Skill.HITPOINTS));
	}

	private void continueChat() {
		targetMenu = null;
		Rectangle bounds = null;

		if (chatbox.chatState() == Chatbox.ChatState.NPC_CHAT) {
			targetMenu = new LegacyMenuEntry("Continue", "", 0, MenuAction.WIDGET_CONTINUE, -1, client.getWidget(231, 5).getId(), false);
			bounds = client.getWidget(231, 5).getBounds();
		}
		if (chatbox.chatState() == Chatbox.ChatState.PLAYER_CHAT) {
			targetMenu = new LegacyMenuEntry("Continue", "", 0, MenuAction.WIDGET_CONTINUE, -1, client.getWidget(217, 5).getId(), false);
			bounds = client.getWidget(217, 5).getBounds();
		}
		if (chatbox.chatState() == Chatbox.ChatState.OPTIONS_CHAT) {
			chooseOption("Yes");
			return;
		}

		if(basicApi.useInvokes())
			utils.doInvokeMsTime(targetMenu, (int) sleepDelay());
		else
			utils.doActionMsTime(targetMenu, bounds, (int) sleepDelay());
	}

	public boolean hasLowRunes(){
		if(!inventory.contains(config.pouchID())) return false;

		int law = inventory.runePouchQuanitity(ItemID.LAW_RUNE);
		int dustEarth = inventory.runePouchContains(ItemID.DUST_RUNE, -1) ? inventory.runePouchQuanitity(ItemID.DUST_RUNE) : inventory.runePouchQuanitity(ItemID.EARTH_RUNE);
		int chaos = inventory.runePouchQuanitity(ItemID.CHAOS_RUNE);

		return law < 50 || dustEarth < 25 || chaos < 50;
	}

	private void lootItem(TileItem item) {
		if (item != null) {
			LegacyMenuEntry entry = new LegacyMenuEntry("", "", item.getId(), MenuAction.GROUND_ITEM_THIRD_OPTION, item.getTile().getSceneLocation().getX(), item.getTile().getSceneLocation().getY(), false);
			if(basicApi.useInvokes())
				utils.doInvokeMsTime(entry, sleepDelay());
			else
				utils.doActionMsTime(entry, item.getTile().getSceneLocation(), sleepDelay());
		}
	}

	public boolean shouldUseObstacle(){
		return !client.isInInstancedRegion() && objectUtils.findNearestGameObject(31990) != null;
	}

	public boolean shouldUseBoat(){
		GameObject bigBoat = objectUtils.findNearestGameObject(4391);
		return client.getLocalPlayer() != null && fremmyArea.contains(client.getLocalPlayer().getWorldLocation());
	}

	public void prioritizeLoot(){
		WidgetItem itemToDrop = itemToDrop(getLoot());
		if(itemToDrop != null){
			String name = client.getItemComposition(itemToDrop.getId()).getName();
			if((name.contains("(1)") && !name.contains("Prayer")) || (!config.usePool() && !config.useAltar() && name.contains("Prayer"))){
				inventory.interactWithItem(itemToDrop.getId(), sleepDelay(), "Drink");
				return;
			}
			if(itemToDrop.getId() == getFoodId()){
				inventory.interactWithItem(itemToDrop.getId(), sleepDelay(), "Eat");
				return;
			}
			inventory.interactWithItem(itemToDrop.getId(), sleepDelay(), "Drop");
		}
	}

	private WidgetItem itemToDrop(TileItem loot){
		int lootValue = loot.getId() == ItemID.VORKATHS_HEAD ? 75000 : itemValues.get(loot.getId()) * loot.getQuantity();

		for(WidgetItem item : inventory.getAll()){
			if(item == null || client.getItemComposition(item.getId()).getName().equalsIgnoreCase(client.getItemComposition(loot.getId()).getName()) || !client.getItemComposition(item.getId()).isTradeable() || item.getId() == config.houseTele().getId()) continue;

			if(itemValues.containsKey(item.getId())){
				if((itemValues.get(item.getId()) * item.getQuantity()) < (lootValue - 1000)){
					return item;
				}
			}else{
				itemValues.put(item.getId(), utils.getItemPrice(item.getId(), true));
			}
		}
		return null;
	}

	public void chooseOption(String part) {
		for (var i = 0; i < game.widget(219, 1).items().size(); i++) {
			if (game.widget(219, 1, i).text() != null && game.widget(219, 1, i).text().contains(part)) {
				game.widget(219, 1, i).select();
				return;
			}
		}
	}

	public boolean optionExists(String part) {
		if(game.widget(219, 1) == null) return false;
		for (var i = 0; i < game.widget(219, 1).items().size(); i++) {
			if (game.widget(219, 1, i).text() != null && game.widget(219, 1, i).text().contains(part))
				return true;
		}
		return false;
	}
	public int getPotionDoses(int anyDoseId) {
		String partial = client.getItemComposition(anyDoseId).getName().substring(0, 11);
		int count = 0;
		for(WidgetItem item : inventory.getAll(a -> client.getItemComposition(a.getId()).getName().contains(partial))){
			String name = client.getItemComposition(item.getId()).getName();
			count += Integer.parseInt(name.substring(name.indexOf("(") + 1, name.indexOf(")")));
		}
		return count;
	}

}
