package io.github.elbakramer.mc.teleportutils.util;

import java.util.function.Supplier;

import net.minecraft.client.gui.screen.Screen;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

public class TeleportUtilsModConfigManager {

    public static void register() {
        AutoConfig.register(TeleportUtilsModConfig.class, JanksonConfigSerializer::new);
    }

    public static TeleportUtilsModConfig getConfig() {
        return AutoConfig.getConfigHolder(TeleportUtilsModConfig.class).getConfig();
    }

    public static Supplier<Screen> getConfigScreen(Screen parent) {
        return AutoConfig.getConfigScreen(TeleportUtilsModConfig.class, parent);
    }

}
