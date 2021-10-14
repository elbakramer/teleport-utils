package io.github.elbakramer.mc.teleportutils.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("inNetherPortal")
    public boolean getInNetherPortal();

    @Accessor("inNetherPortal")
    public void setInNetherPortal(boolean inNetherPortal);

    @Accessor("lastNetherPortalPosition")
    public BlockPos getLastNetherPortalPosition();

    @Accessor("lastNetherPortalPosition")
    public void setLastNetherPortalPosition(BlockPos pos);

    @Nullable
    @Invoker("getTeleportTarget")
    public TeleportTarget invokeGetTeleportTarget(ServerWorld destination);

    @Invoker("removeFromDimension")
    public void invokeRemoveFromDimension();

    @Invoker("unsetRemoved")
    public void invokeUnsetRemoved();

    @Invoker("setRotation")
    public void invokeSetRotation(float yaw, float pitch);

}
