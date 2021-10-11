package io.github.elbakramer.mc.teleportutils.util;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "teleport-utils")
public class TeleportUtilsModConfig implements ConfigData {

    @ConfigEntry.Category("main")
    @ConfigEntry.Gui.Tooltip
    public boolean particleEffectsOnDeparture = true;

    @ConfigEntry.Category("main")
    @ConfigEntry.Gui.Tooltip
    public boolean particleEffectsOnArrival = true;

    @ConfigEntry.Category("main")
    @ConfigEntry.Gui.Tooltip
    public boolean teleportWithPassengers = true;

    @ConfigEntry.Category("main")
    @ConfigEntry.Gui.Tooltip
    public boolean forceRidingWhenRidingBack = true;

    @ConfigEntry.Category("main")
    @ConfigEntry.Gui.Tooltip
    public boolean teleportWithLeashedAnimals = true;

    @ConfigEntry.Category("main")
    @ConfigEntry.Gui.Tooltip
    public double expandAmountForFindingLeashedAnimals = 10.0D;

    @ConfigEntry.Category("main")
    @ConfigEntry.Gui.Tooltip
    public boolean teleportWithVehicle = true;

    @ConfigEntry.Category("main")
    @ConfigEntry.Gui.Tooltip
    public boolean teleportWithVehicleRecursively = true;

    @ConfigEntry.Category("command")
    @ConfigEntry.Gui.Tooltip
    public boolean registerCommand = true;

    @ConfigEntry.Category("command")
    @ConfigEntry.Gui.Tooltip
    public int commandPermissionLevel = 2;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.PrefixText
    public boolean forceCopyOnEntityTeleport = false;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    public boolean loadChunkOnLivingEntityTeleport = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    public boolean findGroundOnLivingEntityTeleport = false;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    public boolean checkAndRevertOnLivingEntityTeleport = false;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    public boolean particleEffectOnLivingEntityTeleport = false;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    public boolean forceCopyOnLivingEntityTeleport = false;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip(count = 3)
    public boolean setInTeleportationStateOnPlayerTeleport = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    public boolean stopRidingOnNonPlayerEntityTeleport = true;

}
