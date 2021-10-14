package io.github.elbakramer.mc.teleportutils.mixin;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

import io.github.elbakramer.mc.teleportutils.util.TeleportUtils;
import io.github.elbakramer.mc.teleportutils.util.TeleportUtilsModConfig;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "teleport", at = @At("HEAD"), cancellable = true)
    private void injectToTeleportMethodDirectly(ServerWorld targetWorld, double x, double y, double z, float yaw,
            float pitch, CallbackInfo ci) {
        TeleportUtilsModConfig config = TeleportUtilsModConfig.getConfig();
        if (config.injectPlayersTeleportDirectly) {
            if (!TeleportUtils.isRecursive()) {
                ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
                Vec3d position = new Vec3d(x, y, z);
                Vec3d velocity = self.getVelocity();
                TeleportTarget target = new TeleportTarget(position, velocity, yaw, pitch);
                TeleportUtils.teleportEntityWithItsPassengersLeashedAnimalsAndVehiclesRecursively(self, target,
                        targetWorld);
                ci.cancel();
            }
        }
    }

    @Nullable
    @Inject(method = "moveToWorld", at = @At("HEAD"), cancellable = true)
    private void injectToMoveToWorldMethodDirectly(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        TeleportUtilsModConfig config = TeleportUtilsModConfig.getConfig();
        if (config.injectPlayersMoveToWorldDirectly) {
            if (!TeleportUtils.isRecursive()) {
                ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
                Entity entity = TeleportUtils.moveToWorldWithItsPassengersLeashedAnimalsAndVehiclesRecursively(self,
                        destination);
                cir.setReturnValue(entity);
            }
        }
    }

}
