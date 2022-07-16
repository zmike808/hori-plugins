package net.runelite.client.plugins.basicapi;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.TileObject;
import net.runelite.api.events.*;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.basicapi.utils.BankUtils;
import net.runelite.client.plugins.basicapi.utils.BankingUtilsTest;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.iUtils;
import org.pf4j.Extension;
import net.runelite.api.*;
import net.runelite.api.events.*;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "BasicApi",
	description = "Basic API",
	tags = {"api"}
)
@Slf4j
public class BasicApiPlugin extends Plugin {

	@Inject
	private BasicApiConfig config;

	@Inject
	private Game game;

	@Inject
	private Client client;

	@Inject
	private BankingUtilsTest bankingUtilsTest;

	@Inject
	private BankUtils bankUtils;

	public BasicApiPlugin() {
	}

	@Provides
	BasicApiConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(BasicApiConfig.class);
	}

	@Override
	protected void startUp() {
		log.info("Starting BasicAPI");
	}

	@Override
	protected void shutDown() {
		log.info("Stopping BasicAPI");
	}

	public boolean logout() {
		if (game.widget(182, 8) != null) {
			game.widget(182, 8).interact("Logout");
		} else {
			game.widget(WidgetInfo.WORLD_SWITCHER_LOGOUT_BUTTON).interact("Logout");
		}
		return game.client.getGameState() == GameState.LOGIN_SCREEN;
	}

	@Getter(AccessLevel.PUBLIC)
	private final static Set<TileObject> objects = new HashSet<>();

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() != GameState.LOGGED_IN && gameStateChanged.getGameState() != GameState.CONNECTION_LOST)
		{
			objects.clear();
		}
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned wallObjectSpawned)
	{
		objects.add(wallObjectSpawned.getWallObject());
	}

//	@Subscribe
//	public void onWallObjectChanged(WallObjectChanged wallObjectChanged)
//	{
//		objects.remove(wallObjectChanged.getPrevious());
//		objects.add(wallObjectChanged.getWallObject());
//	}

	@Subscribe
	public void onWallObjectDespawned(WallObjectDespawned wallObjectDespawned)
	{
		objects.remove(wallObjectDespawned.getWallObject());
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		objects.add(gameObjectSpawned.getGameObject());
	}

//	@Subscribe
//	public void onGameObjectChanged(GameObjectChanged gameObjectChanged)
//	{
//		objects.remove(gameObjectChanged.getPrevious());
//		objects.add(gameObjectChanged.getGameObject());
//	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned)
	{
		objects.remove(gameObjectDespawned.getGameObject());
	}

	@Subscribe
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned decorativeObjectSpawned)
	{
		objects.add(decorativeObjectSpawned.getDecorativeObject());
	}

//	@Subscribe
//	public void onDecorativeObjectChanged(DecorativeObjectChanged decorativeObjectChanged)
//	{
//		objects.remove(decorativeObjectChanged.getPrevious());
//		objects.add(decorativeObjectChanged.getDecorativeObject());
//	}

	@Subscribe
	public void onDecorativeObjectDespawned(DecorativeObjectDespawned decorativeObjectDespawned)
	{
		objects.remove(decorativeObjectDespawned.getDecorativeObject());
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned groundObjectSpawned)
	{
		objects.add(groundObjectSpawned.getGroundObject());
	}

	@Subscribe
	public void onGroundObjectChanged(GroundObjectChanged groundObjectChanged)
	{
		objects.remove(groundObjectChanged.getPrevious());
		objects.add(groundObjectChanged.getGroundObject());
	}

	@Subscribe
	public void onGroundObjectDespawned(GroundObjectDespawned groundObjectDespawned)
	{
		objects.remove(groundObjectDespawned.getGroundObject());
	}

	public boolean useInvokes(){
		return config.invokes();
	}
}
