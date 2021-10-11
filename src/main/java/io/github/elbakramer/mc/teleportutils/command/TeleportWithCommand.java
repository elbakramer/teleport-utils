package io.github.elbakramer.mc.teleportutils.command;

import java.util.Collection;
import java.util.Collections;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.*;
import net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

import me.shedaniel.autoconfig.AutoConfig;
import io.github.elbakramer.mc.teleportutils.util.TeleportUtils;
import io.github.elbakramer.mc.teleportutils.util.TeleportUtilsModConfig;

import static net.minecraft.server.command.CommandManager.*;

public final class TeleportWithCommand implements Command<ServerCommandSource> {

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Vec3d targetLocation;

        try {
            targetLocation = Vec3ArgumentType.getVec3(context, "location");
        } catch (IllegalArgumentException e1) {
            try {
                targetLocation = EntityArgumentType.getEntity(context, "destination").getPos();
            } catch (IllegalArgumentException e2) {
                throw e2;
            }
        }

        Collection<? extends Entity> targetEntities;

        try {
            targetEntities = EntityArgumentType.getEntities(context, "targets");
        } catch (IllegalArgumentException e) {
            targetEntities = Collections.singleton(context.getSource().getEntityOrThrow());
        }

        PosArgument targetRotation = null;
        Vec3d facingLocation = null;
        Entity facingEntity = null;
        EntityAnchor facingAnchor = null;

        try {
            targetRotation = RotationArgumentType.getRotation(context, "rotation");
        } catch (IllegalArgumentException e1) {
            try {
                facingLocation = Vec3ArgumentType.getVec3(context, "facingLocation");
            } catch (IllegalArgumentException e2) {
                try {
                    facingEntity = EntityArgumentType.getEntity(context, "facingEntity");
                } catch (IllegalArgumentException e3) {
                }
                if (facingEntity != null) {
                    try {
                        facingAnchor = EntityAnchorArgumentType.getEntityAnchor(context, "facingAnchor");
                    } catch (IllegalArgumentException e4) {
                    }
                }
            }
        }

        float yaw = 0.0f;
        float pitch = 0.0f;

        for (Entity entity : targetEntities) {
            if (targetRotation != null) {
                Vec2f rot = targetRotation.toAbsoluteRotation(context.getSource());
                yaw = rot.x;
                pitch = rot.y;
            } else if (facingLocation != null || facingEntity != null) {
                Vec3d facingFromLocation = targetLocation;
                if (facingLocation == null) {
                    if (facingAnchor == null) {
                        facingAnchor = EntityAnchor.EYES;
                    }
                    if (facingAnchor == EntityAnchor.EYES) {
                        facingLocation = facingEntity.getEyePos();
                    } else {
                        facingLocation = facingEntity.getPos();
                    }
                }
                Vec3d rot = facingFromLocation.relativize(facingLocation).normalize();
                yaw = (float) MathHelper.wrapDegrees(MathHelper.atan2(-rot.x, rot.z) * MathHelper.DEGREES_PER_RADIAN);
                pitch = (float) MathHelper.wrapDegrees(MathHelper.atan2(new Vec3d(rot.z, 0.0D, rot.z).length(), -rot.y)
                        * MathHelper.DEGREES_PER_RADIAN);
            } else {
                yaw = entity.getPitch();
                pitch = entity.getYaw();
            }

            Vec3d targetVelocity = entity.getVelocity();
            TeleportTarget target = new TeleportTarget(targetLocation, targetVelocity, yaw, pitch);
            entity = TeleportUtils.teleportEntityWithItsPassengersLeashedAnimalsAndVehiclesRecursively(entity, target,
                    null);
        }

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        TeleportWithCommand command = new TeleportWithCommand();
        TeleportUtilsModConfig config = AutoConfig.getConfigHolder(TeleportUtilsModConfig.class).getConfig();

        LiteralArgumentBuilder<ServerCommandSource> teleportWith = literal("teleportWith");

        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> destination = argument("destination",
                EntityArgumentType.entity());
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> targets = argument("targets",
                EntityArgumentType.entities());
        RequiredArgumentBuilder<ServerCommandSource, PosArgument> location = argument("location",
                Vec3ArgumentType.vec3());
        RequiredArgumentBuilder<ServerCommandSource, PosArgument> rotation = argument("rotation",
                RotationArgumentType.rotation());
        LiteralArgumentBuilder<ServerCommandSource> facing = literal("facing");
        RequiredArgumentBuilder<ServerCommandSource, PosArgument> facingLocation = argument("facingLocation",
                Vec3ArgumentType.vec3());
        LiteralArgumentBuilder<ServerCommandSource> entity = literal("entity");
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> facingEntity = argument("facingEntity",
                EntityArgumentType.entity());
        RequiredArgumentBuilder<ServerCommandSource, EntityAnchor> facingAnchor = argument("facingAnchor",
                EntityAnchorArgumentType.entityAnchor());

        // @formatter:off
        LiteralArgumentBuilder<ServerCommandSource> builder = teleportWith
            .requires(source -> source.hasPermissionLevel(config.commandPermissionLevel))
            .then(destination.executes(command))
            .then(location.executes(command))
            .then(targets
                .then(destination.executes(command))
                .then(location
                    .then(rotation.executes(command))
                    .then(facing
                        .then(facingLocation.executes(command))
                        .then(entity.then(facingEntity)
                            .then(facingAnchor.executes(command))
                            .executes(command)
                        )
                    )
                    .executes(command)
                )
            );
        // @formatter:on

        LiteralCommandNode<ServerCommandSource> node = dispatcher.register(builder);

        LiteralArgumentBuilder<ServerCommandSource> tpw = literal("tpw");
        LiteralArgumentBuilder<ServerCommandSource> aliasBuilder = tpw.redirect(node);
        dispatcher.register(aliasBuilder);
    }

}