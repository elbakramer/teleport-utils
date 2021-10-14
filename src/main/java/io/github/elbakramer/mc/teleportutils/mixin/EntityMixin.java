package io.github.elbakramer.mc.teleportutils.mixin;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import io.github.elbakramer.mc.teleportutils.util.TeleportUtils;
import io.github.elbakramer.mc.teleportutils.util.TeleportUtilsModConfig;

@Mixin(Entity.class)
public class EntityMixin {

    @Redirect(method = "tickNetherPortal", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hasVehicle()Z"))
    private boolean bypassHasVehicleTestOnPlayerTickNetherPortal(Entity entity) {
        if (entity instanceof PlayerEntity) {
            TeleportUtilsModConfig config = TeleportUtilsModConfig.getConfig();
            if (config.playerMoveToWorldWithOthersOnNetherPortal
                    && config.bypassHasVehicleTestOnPlayerTickNetherPortal) {
                return false;
            }
        }
        return entity.hasVehicle();
    }

    @Nullable
    @Redirect(method = "tickNetherPortal", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;"))
    public Entity playerMoveToWorldWithOthersOnNetherPortal(Entity entity, ServerWorld destination) {
        if (entity instanceof PlayerEntity) {
            TeleportUtilsModConfig config = TeleportUtilsModConfig.getConfig();
            if (!config.injectPlayersMoveToWorldDirectly && config.playerMoveToWorldWithOthersOnNetherPortal) {
                return TeleportUtils.moveToWorldWithItsPassengersLeashedAnimalsAndVehiclesRecursively(entity,
                        destination);
            }
        }
        return entity.moveToWorld(destination);
    }

}
