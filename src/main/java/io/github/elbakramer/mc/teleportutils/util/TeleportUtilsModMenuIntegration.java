package io.github.elbakramer.mc.teleportutils.util;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class TeleportUtilsModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> TeleportUtilsModConfig.getConfigScreen(parent).get();
    }

}
