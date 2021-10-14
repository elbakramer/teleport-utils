package io.github.elbakramer.mc.teleportutils.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import io.github.elbakramer.mc.teleportutils.util.TeleportUtils;
import io.github.elbakramer.mc.teleportutils.util.TeleportUtilsModConfig;
import io.github.elbakramer.mc.teleportutils.util.TeleportUtilsModConfigManager;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

    @Redirect(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hasVehicle()Z"))
    private boolean bypassHasVehicleTestOnPlayerEntityCollisionWithEndPortalBlock(Entity entity) {
        if (entity instanceof PlayerEntity) {
            TeleportUtilsModConfig config = TeleportUtilsModConfigManager.getConfig();
            if (config.playerMoveToWorldWithOthersOnEndPortal
                    && config.bypassHasVehicleTestOnPlayerEntityCollisionWithEndPortalBlock) {
                return false;
            }
        }
        return entity.hasVehicle();
    }

    @Redirect(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hasPassengers()Z"))
    private boolean bypassHasPassengersTestOnPlayerEntityCollisionWithEndPortalBlock(Entity entity) {
        if (entity instanceof PlayerEntity) {
            TeleportUtilsModConfig config = TeleportUtilsModConfigManager.getConfig();
            if (config.playerMoveToWorldWithOthersOnEndPortal
                    && config.bypassHasPassengersTestOnPlayerEntityCollisionWithEndPortalBlock) {
                return false;
            }
        }
        return entity.hasPassengers();
    }

    @Nullable
    @Redirect(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;"))
    public Entity playerMoveToWorldWithOthersOnEndPortal(Entity entity, ServerWorld destination) {
        if (entity instanceof PlayerEntity) {
            TeleportUtilsModConfig config = TeleportUtilsModConfigManager.getConfig();
            if (!config.injectPlayersMoveToWorldDirectly && config.playerMoveToWorldWithOthersOnEndPortal) {
                return TeleportUtils.moveToWorldWithItsPassengersLeashedAnimalsAndVehiclesRecursively(entity,
                        destination);
            }
        }
        return entity.moveToWorld(destination);
    }

}
