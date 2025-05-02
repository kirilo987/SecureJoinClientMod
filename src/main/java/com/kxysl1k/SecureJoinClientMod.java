package com.kxysl1k;

import com.kxysl1k.util.SystemInfoCollector;
import com.kxysl1k.util.FileProtector;
import com.kxysl1k.network.DHKeyExchangeHandler;
import com.kxysl1k.network.ModDataSender;
import com.kxysl1k.anti.AntiCheatMonitor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SecureJoinClientMod implements ClientModInitializer {
	public static final String MOD_ID = "securejoin";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing SecureJoinClientMod");

		// Collect system information and encrypt it into a config file
		SystemInfoCollector collector = new SystemInfoCollector();
		collector.collectAndEncrypt();

		// Start file protection
		FileProtector protector = new FileProtector(collector.getConfigPath());
		protector.startProtection();

		// Start the anti-cheat monitor
		AntiCheatMonitor.getInstance().start();

		// Network initialization
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			DHKeyExchangeHandler.init();
			ModDataSender.sendData();
		});

		// On game exit
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			protector.stopProtection();
			collector.deleteConfig();
			LOGGER.info("SecureJoinClientMod shutdown complete");
		});

		LOGGER.info("SecureJoinClientMod initialized successfully");
	}
}