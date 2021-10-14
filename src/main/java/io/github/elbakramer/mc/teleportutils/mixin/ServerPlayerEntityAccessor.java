package io.github.elbakramer.mc.teleportutils.mixin;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

@Mixin(ServerPlayerEntity.class)
public interface ServerPlayerEntityAccessor {

    @Accessor("inTeleportationState")
    public void setInTeleportationState(boolean inTeleportationState);

    @Nullable
    @Invoker("getTeleportTarget")
    public TeleportTarget invokeGetTeleportTarget(ServerWorld destination);

    @Accessor("seenCredits")
    public boolean getSeenCredits();

    @Accessor("seenCredits")
    public void setSeenCredits(boolean seenCredits);

    @Accessor("enteredNetherPos")
    public void setEnteredNetherPos(Vec3d enteredNetherPos);

    @Invoker("createEndSpawnPlatform")
    public void invokeCreateEndSpawnPlatform(ServerWorld world, BlockPos centerPos);

    @Invoker("worldChanged")
    public void invokeWorldChanged(ServerWorld origin);

    @Accessor("syncedExperience")
    public void setSyncedExperience(int syncedExperience);

    @Accessor("syncedHealth")
    public void setSyncedHealth(float syncedHealth);

    @Accessor("syncedFoodLevel")
    public void setSyncedFoodLevel(int syncedFoodLevel);

}
