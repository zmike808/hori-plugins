package net.runelite.client.plugins.basicapi;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.iutils.*;
import net.runelite.client.plugins.iutils.PrayerUtils;
import net.runelite.client.plugins.iutils.game.Game;
import org.pf4j.Extension;

import javax.inject.Inject;

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
	private PrayerUtils prayerUtils;


	@Inject
	private Client client;

	@Inject
	private InventoryUtils invUtils;

	@Inject
	private WalkUtils walkUtils;

	@Inject
	private NPCUtils npcUtils;

	@Inject
	private CalculationUtils calc;

	@Inject
	private iUtils utils;

	@Inject
	private Game game;


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

}
