package io.github.elbakramer.mc.teleportutils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

import io.github.elbakramer.mc.teleportutils.util.TeleportUtilsModConfig;
import io.github.elbakramer.mc.teleportutils.command.TeleportWithCommand;

public class TeleportUtilsMod implements ModInitializer {

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger("TeleportUtils");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		AutoConfig.register(TeleportUtilsModConfig.class, JanksonConfigSerializer::new);
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			TeleportWithCommand.register(dispatcher);
		});
		LOGGER.info("[TeleportUtils] Mod Initialized.");
	}

}
