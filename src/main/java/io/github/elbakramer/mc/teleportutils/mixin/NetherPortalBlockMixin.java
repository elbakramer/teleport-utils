package io.github.elbakramer.mc.teleportutils.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import io.github.elbakramer.mc.teleportutils.util.TeleportUtils;
import io.github.elbakramer.mc.teleportutils.util.TeleportUtilsModConfig;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {

    @Redirect(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hasVehicle()Z"))
    private boolean bypassHasVehicleTestOnPlayerEntityCollisionWithNetherPortalBlock(Entity entity) {
        if (entity instanceof PlayerEntity) {
            TeleportUtilsModConfig config = TeleportUtilsModConfig.getConfig();
            if (config.playerMoveToWorldWithOthersOnNetherPortal
                    && config.bypassHasVehicleTestOnPlayerEntityCollisionWithNetherPortalBlock) {
                return false;
            }
        }
        return entity.hasVehicle();
    }

    @Redirect(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hasPassengers()Z"))
    private boolean bypassHasPassengersTestOnPlayerEntityCollisionWithNetherPortalBlock(Entity entity) {
        if (entity instanceof PlayerEntity) {
            TeleportUtilsModConfig config = TeleportUtilsModConfig.getConfig();
            if (config.playerMoveToWorldWithOthersOnNetherPortal
                    && config.bypassHasPassengersTestOnPlayerEntityCollisionWithNetherPortalBlock) {
                return false;
            }
        }
        return entity.hasPassengers();
    }

    @Redirect(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setInNetherPortal(Lnet/minecraft/util/math/BlockPos;)V"))
    private void propagateLastNetherPortalPositionOnPlayerCollisionWithNetherPortalBlock(Entity entity, BlockPos pos) {
        entity.setInNetherPortal(pos);
        if (entity instanceof PlayerEntity) {
            TeleportUtilsModConfig config = TeleportUtilsModConfig.getConfig();
            if (config.playerMoveToWorldWithOthersOnNetherPortal) {
                Entity startingEntity = entity;
                if (entity.hasVehicle() && config.moveToWorldWithVehicle) {
                    if (config.moveToWorldWithVehicleRecursively) {
                        startingEntity = entity.getRootVehicle();
                    } else {
                        startingEntity = entity.getVehicle();
                    }
                }
                TeleportUtils.propagateLastNetherPortalPositionRecursively(startingEntity, pos);
            }
        }
    }

}
