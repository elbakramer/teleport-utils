package io.github.elbakramer.mc.teleportutils.util;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "teleport-utils")
public class TeleportUtilsModConfig implements ConfigData {

    @ConfigEntry.Category("common")
    @ConfigEntry.Gui.Tooltip
    public double expandAmountForFindingLeashedAnimals = 10.0D;

    @ConfigEntry.Category("common")
    @ConfigEntry.Gui.Tooltip
    public boolean forceRidingWhenRidingBack = true;

    @ConfigEntry.Category("teleport")
    @ConfigEntry.Gui.Tooltip
    public boolean particleEffectsOnDeparture = false;

    @ConfigEntry.Category("teleport")
    @ConfigEntry.Gui.Tooltip
    public boolean particleEffectsOnArrival = false;

    @ConfigEntry.Category("teleport")
    @ConfigEntry.Gui.Tooltip
    public boolean playTeleportSoundOnDeparture = false;

    @ConfigEntry.Category("teleport")
    @ConfigEntry.Gui.Tooltip
    public boolean playTeleportSoundOnArrival = false;

    @ConfigEntry.Category("teleport")
    @ConfigEntry.Gui.Tooltip
    public float playTeleportSoundVolume = 1F;

    @ConfigEntry.Category("teleport")
    @ConfigEntry.Gui.Tooltip
    public float playTeleportSoundPitch = 1F;

    @ConfigEntry.Category("teleport")
    @ConfigEntry.Gui.Tooltip
    public boolean teleportWithPassengers = true;

    @ConfigEntry.Category("teleport")
    @ConfigEntry.Gui.Tooltip
    public boolean teleportWithLeashedAnimals = true;

    @ConfigEntry.Category("teleport")
    @ConfigEntry.Gui.Tooltip
    public boolean teleportWithVehicle = true;

    @ConfigEntry.Category("teleport")
    @ConfigEntry.Gui.Tooltip
    public boolean teleportWithVehicleRecursively = true;

    @ConfigEntry.Category("teleport")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean injectPlayersTeleportDirectly = false;

    @ConfigEntry.Category("portal")
    @ConfigEntry.Gui.Tooltip
    public boolean moveToWorldWithPassengers = true;

    @ConfigEntry.Category("portal")
    @ConfigEntry.Gui.Tooltip
    public boolean moveToWorldWithLeashedAnimals = true;

    @ConfigEntry.Category("portal")
    @ConfigEntry.Gui.Tooltip
    public boolean moveToWorldWithVehicle = true;

    @ConfigEntry.Category("portal")
    @ConfigEntry.Gui.Tooltip
    public boolean moveToWorldWithVehicleRecursively = true;

    @ConfigEntry.Category("portal")
    @ConfigEntry.Gui.Tooltip
    public boolean followTeleportTargetOnMoveToWorld = true;

    @ConfigEntry.Category("portal")
    @ConfigEntry.Gui.Tooltip
    public boolean playerMoveToWorldWithOthersOnNetherPortal = true;

    @ConfigEntry.Category("portal")
    @ConfigEntry.Gui.Tooltip
    public boolean playerMoveToWorldWithOthersOnEndPortal = true;

    @ConfigEntry.Category("portal")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean injectPlayersMoveToWorldDirectly = false;

    @ConfigEntry.Category("command")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.RequiresRestart
    public boolean registerCommand = true;

    @ConfigEntry.Category("command")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.RequiresRestart
    public int commandPermissionLevel = 2;

    @ConfigEntry.Category("common_misc")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.PrefixText
    public boolean dropItemOnDetachLeash = false;

    @ConfigEntry.Category("common_misc")
    @ConfigEntry.Gui.Tooltip
    public boolean sendPacketOnAttachLeash = true;

    @ConfigEntry.Category("common_misc")
    @ConfigEntry.Gui.Tooltip
    public boolean sendPacketOnDetachLeash = true;

    @ConfigEntry.Category("teleport_misc")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.PrefixText
    public boolean forceCopyOnEntityTeleport = false;

    @ConfigEntry.Category("teleport_misc")
    @ConfigEntry.Gui.Tooltip
    public boolean loadChunkOnLivingEntityTeleport = true;

    @ConfigEntry.Category("teleport_misc")
    @ConfigEntry.Gui.Tooltip
    public boolean findGroundOnLivingEntityTeleport = false;

    @ConfigEntry.Category("teleport_misc")
    @ConfigEntry.Gui.Tooltip
    public boolean checkAndRevertOnLivingEntityTeleport = false;

    @ConfigEntry.Category("teleport_misc")
    @ConfigEntry.Gui.Tooltip
    public boolean particleEffectOnLivingEntityTeleport = false;

    @ConfigEntry.Category("teleport_misc")
    @ConfigEntry.Gui.Tooltip
    public boolean forceCopyOnLivingEntityTeleport = false;

    @ConfigEntry.Category("teleport_misc")
    @ConfigEntry.Gui.Tooltip(count = 3)
    public boolean setInTeleportationStateOnPlayerTeleport = true;

    @ConfigEntry.Category("teleport_misc")
    @ConfigEntry.Gui.Tooltip
    public boolean stopRidingOnNonPlayerEntityTeleport = true;

    @ConfigEntry.Category("portal_misc")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.PrefixText
    public boolean resetNetherPortalCooldownOnMoveToWorld = true;

    @ConfigEntry.Category("portal_misc")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean bypassHasVehicleTestOnPlayerEntityCollisionWithNetherPortalBlock = true;

    @ConfigEntry.Category("portal_misc")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean bypassHasPassengersTestOnPlayerEntityCollisionWithNetherPortalBlock = true;

    @ConfigEntry.Category("portal_misc")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean bypassHasVehicleTestOnPlayerTickNetherPortal = true;

    @ConfigEntry.Category("portal_misc")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean bypassHasVehicleTestOnPlayerEntityCollisionWithEndPortalBlock = true;

    @ConfigEntry.Category("portal_misc")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean bypassHasPassengersTestOnPlayerEntityCollisionWithEndPortalBlock = true;

}
