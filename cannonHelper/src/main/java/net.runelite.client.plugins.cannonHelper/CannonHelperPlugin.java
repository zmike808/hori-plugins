package net.runelite.client.plugins.cannonHelper;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.basicapi.BasicApiPlugin;
import net.runelite.client.plugins.basicapi.utils.Inventory;
import net.runelite.client.plugins.basicapi.utils.PrayerUtils;
import net.runelite.client.plugins.iutils.CalculationUtils;
import net.runelite.client.plugins.iutils.WalkUtils;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.game.iObject;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.plugins.iutils.scene.Position;
import net.runelite.client.plugins.iutils.scripts.iScript;
import org.pf4j.Extension;

import javax.inject.Inject;

@Extension
@PluginDependency(BasicApiPlugin.class)
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "Cannon Helper",
	description = "Assists with cannon functions",
	tags = {"Vorkath"}
)
@Slf4j
public class CannonHelperPlugin extends iScript {

	@Inject
	private CannonHelperConfig config;

	@Inject
	private Client client;

	@Inject
	private Inventory inventory;

	@Inject
	private PrayerUtils prayerUtils;

	@Inject
	private WalkUtils walkUtils;

	@Inject
	private Game game;

	@Inject
	private CalculationUtils calc;

	private boolean startPlugin;

	private LocalPoint cannonLocation;
	private LocalPoint safeSpot;
	private long sleepLength;

	private int nextCount;

	public CannonHelperPlugin() {
		startPlugin = false;
		cannonLocation = null;
		safeSpot = null;
	}

	@Provides
	CannonHelperConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(CannonHelperConfig.class);
	}

	@Override
	protected void startUp() {
		log.info("Cannon Helper Started");
	}

	@Override
	protected void shutDown() {

	}

	@Override
	protected void onStart() {

	}

	@Override
	protected void onStop() {
		startPlugin = false;
		cannonLocation = null;
		safeSpot = null;
	}

	@Override
	protected void loop() {
		if(client.getGameState() != GameState.LOGGED_IN || !startPlugin || client.getLocalPlayer() == null) return;

		iObject cannon = game.objects().withPosition(new Position(WorldPoint.fromLocal(client, cannonLocation))).withName("Dwarf multicannon", "Broken multicannon").nearest();

		Player player = client.getLocalPlayer();

		if(!inventory.contains(ItemID.CANNONBALL) && !inventory.contains(ItemID.GRANITE_CANNONBALL)){
			game.sendGameMessage("Stopping plugin: Can't find cannonballs!");
			stop();
		}
		if(cannon == null){
			game.sendGameMessage("Stopping plugin: Can't find cannon");
			stop();
		}

		if(cannon.name().equalsIgnoreCase("Broken multicannon")){
			cannon.interact("Repair");
			game.waitUntil(() -> !cannon.name().equalsIgnoreCase("Broken multicannon"), 10);
		}else{
			if(!isRunning()){
				cannon.interact("Fire");
				game.waitUntil(() -> isRunning(), 5);
			}else{
				if(getBalls() <= nextCount){
					cannon.interact("Fire");
					if(game.waitUntil(() -> getBalls() >= 25, 7)){
						nextCount = calc.getRandomIntBetweenRange(config.minimumBalls(), config.maximumBalls());
					}
				}
			}
		}

		if(safeSpot != null){
			if(!player.getLocalLocation().equals(safeSpot) && isRunning() && !player.isMoving())
				walkUtils.sceneWalk(safeSpot, 0, sleepDelay());
		}

		if(config.restorePray() && shouldDrinkRestore() &&  inventory.contains(config.prayer().getIds())){
			inventory.interactWithItem(inventory.getFirst(config.prayer().getIds()).getId(), sleepDelay(), "Drink");
		}
		game.tick();
	}

	public boolean shouldDrinkRestore(){
		return prayerUtils.getRemainingPoints() <= 25;
	}

	@Subscribe
	public void onGameTick(GameTick event){
		if(client.getGameState() != GameState.LOGGED_IN || !startPlugin || client.getLocalPlayer() == null) return;

	}

	@Subscribe
	private void onChatMessage(ChatMessage event){
		String message = event.getMessage();
		if(message.isEmpty() || event.getType() != ChatMessageType.GAMEMESSAGE) return;
		if(message.equalsIgnoreCase("That isn't your cannon!")){
			game.sendGameMessage("Interacted with incorrect cannon, stopping.");
			stop();
		}
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("CannonHelperConfig") || client.getGameState() != GameState.LOGGED_IN|| client.getLocalPlayer() == null)
			return;

		if (configButtonClicked.getKey().equalsIgnoreCase("setTile")) {
			LocalPoint playerLocalLoc = client.getLocalPlayer().getLocalLocation();
			cannonLocation = playerLocalLoc;
			game.sendGameMessage("Cannon locations set!");
			game.sendGameMessage("Local Location: " + cannonLocation);
		}

		if (configButtonClicked.getKey().equalsIgnoreCase("setSafeTile")) {
			safeSpot = client.getLocalPlayer().getLocalLocation();
			game.sendGameMessage("Safespot location set!");
			game.sendGameMessage("Local Location: " + safeSpot);
		}

		if (configButtonClicked.getKey().equalsIgnoreCase("startHelper")) {
			if(cannonLocation == null){
				game.sendGameMessage("Set cannon tile location before starting please!");
				return;
			}
			if(config.minimumBalls() > config.maximumBalls()){
				game.sendGameMessage("Why is the minimum balls greater than the maximum balls?");
				return;
			}
			if (!startPlugin) {
				game.sendGameMessage("Cannon Helper Started");
				startPlugin = true;
				nextCount = calc.getRandomIntBetweenRange(config.minimumBalls(), config.maximumBalls());
				start();
			} else {
				game.sendGameMessage("Cannon Helper stopped");
				stop();
			}
		}

	}

	private int getBalls(){
		return game.varp(3);
	}

	private boolean isRunning(){
		return game.varp(1) > 0;
	}
	private long sleepDelay() {
		sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

}
