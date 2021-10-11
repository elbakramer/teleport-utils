package io.github.elbakramer.mc.teleportutils.util;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import me.shedaniel.autoconfig.AutoConfig;

import io.github.elbakramer.mc.teleportutils.TeleportUtilsMod;
import io.github.elbakramer.mc.teleportutils.mixin.ServerPlayerEntityAccessor;

public class TeleportUtils {

    private static final Logger LOGGER = TeleportUtilsMod.LOGGER;

    public static void spawnPortalParticleEffectAroundEntity(Entity entity) {
        World world = entity.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            serverWorld.sendEntityStatus(entity, (byte) 46);
        }
    }

    public static Entity entityTeleport(Entity entity, TeleportTarget target) {
        entity.teleport(target.position.getX(), target.position.getY(), target.position.getZ());
        entity.setYaw(target.yaw);
        entity.setPitch(target.pitch);
        entity.setVelocity(target.velocity);
        return entity;
    }

    public static Entity entityTeleport(Entity entity, TeleportTarget target, ServerWorld targetWorld,
            boolean forceCopy) {
        World world = entity.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            if (targetWorld == null) {
                targetWorld = serverWorld;
            }
            if (forceCopy || targetWorld != serverWorld) {
                Entity newEntity = entity.getType().create(targetWorld);
                if (newEntity != null) {
                    newEntity.copyFrom(entity);
                    newEntity.refreshPositionAndAngles(target.position.getX(), target.position.getY(),
                            target.position.getZ(), target.yaw, target.pitch);
                    newEntity.setVelocity(target.velocity);
                    targetWorld.onDimensionChanged(newEntity);
                }
                entity.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
                entity = newEntity;
            } else {
                entity = entityTeleport(entity, target);
            }
        }
        return entity;
    }

    public static Entity entityTeleport(Entity entity, TeleportTarget target, ServerWorld targetWorld) {
        TeleportUtilsModConfig config = AutoConfig.getConfigHolder(TeleportUtilsModConfig.class).getConfig();
        return entityTeleport(entity, target, targetWorld, config.forceCopyOnEntityTeleport);
    }

    public static boolean livingEntityTeleport(LivingEntity entity, TeleportTarget target, boolean particleEffects,
            boolean loadChunk, boolean findGround, boolean checkAndRevert) {
        double d = entity.getX();
        double e = entity.getY();
        double f = entity.getZ();
        double g = target.position.getY();

        double x = target.position.getX();
        double y = target.position.getY();
        double z = target.position.getZ();

        BlockPos blockPos = new BlockPos(x, y, z);
        World world = entity.getEntityWorld();

        boolean foundGround = false;
        boolean successfulTeleport = false;
        boolean successful = false;

        if (loadChunk && world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            ChunkPos chunkPos = new ChunkPos(blockPos);
            serverWorld.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 0, entity.getId());
            world.getChunk(chunkPos.x, chunkPos.z);
        }

        if (findGround && world.isChunkLoaded(blockPos)) {
            while (!foundGround && blockPos.getY() > world.getBottomY()) {
                BlockPos blockPos2 = blockPos.down();
                BlockState blockState = world.getBlockState(blockPos2);
                if (blockState.getMaterial().blocksMovement()) {
                    foundGround = true;
                } else {
                    --g;
                    blockPos = blockPos2;
                }
            }
        }

        if (!findGround || foundGround) {
            entity.requestTeleport(x, g, z);
            if (world.isSpaceEmpty(entity) && !world.containsFluid(entity.getBoundingBox())) {
                successfulTeleport = true;
            }
        }

        if (checkAndRevert && !successfulTeleport) {
            entity.requestTeleport(d, e, f);
            successful = false;
        } else {
            if (particleEffects) {
                world.sendEntityStatus(entity, (byte) 46);
            }
            if (entity instanceof PathAwareEntity) {
                ((PathAwareEntity) entity).getNavigation().stop();
            }
            successful = true;
        }

        if (successful) {
            entity.setYaw(target.yaw);
            entity.setPitch(target.pitch);
            entity.setVelocity(target.velocity);
        }

        return successful;
    }

    public static LivingEntity livingEntityTeleport(LivingEntity entity, TeleportTarget target,
            boolean particleEffects) {
        TeleportUtilsModConfig config = AutoConfig.getConfigHolder(TeleportUtilsModConfig.class).getConfig();
        boolean successful = livingEntityTeleport(entity, target, particleEffects,
                config.loadChunkOnLivingEntityTeleport, config.findGroundOnLivingEntityTeleport,
                config.checkAndRevertOnLivingEntityTeleport);
        if (!successful) {
            LOGGER.warn("[TeleportUtils] Failed to teleport LivingEntity: {}", entity.getName().getString());
        }
        return entity;
    }

    public static LivingEntity livingEntityTeleport(LivingEntity entity, TeleportTarget target) {
        TeleportUtilsModConfig config = AutoConfig.getConfigHolder(TeleportUtilsModConfig.class).getConfig();
        return livingEntityTeleport(entity, target, config.particleEffectOnLivingEntityTeleport);
    }

    public static LivingEntity livingEntityTeleport(LivingEntity entity, TeleportTarget target, ServerWorld targetWorld,
            boolean forceCopy) {
        World world = entity.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            if (targetWorld == null) {
                targetWorld = serverWorld;
            }
            if (forceCopy || targetWorld != serverWorld) {
                entity = (LivingEntity) entityTeleport(entity, target, targetWorld, forceCopy);
                if (entity instanceof PathAwareEntity) {
                    ((PathAwareEntity) entity).getNavigation().stop();
                }
            } else {
                entity = livingEntityTeleport(entity, target);
            }
        }
        return entity;
    }

    public static LivingEntity livingEntityTeleport(LivingEntity entity, TeleportTarget target,
            ServerWorld targetWorld) {
        TeleportUtilsModConfig config = AutoConfig.getConfigHolder(TeleportUtilsModConfig.class).getConfig();
        return livingEntityTeleport(entity, target, targetWorld, config.forceCopyOnLivingEntityTeleport);
    }

    public static ServerPlayerEntity serverPlayerEntityTeleport(ServerPlayerEntity player, TeleportTarget target,
            ServerWorld targetWorld, boolean setInTeleportationState) {
        ServerPlayerEntityAccessor playerAccessor = (ServerPlayerEntityAccessor) player;
        if (setInTeleportationState) {
            playerAccessor.setInTeleportationState(true);
        }
        player.teleport(targetWorld, target.position.getX(), target.position.getY(), target.position.getZ(), target.yaw,
                target.pitch);
        player.setVelocity(target.velocity);
        return player;
    }

    public static ServerPlayerEntity serverPlayerEntityTeleport(ServerPlayerEntity player, TeleportTarget target,
            ServerWorld targetWorld) {
        TeleportUtilsModConfig config = AutoConfig.getConfigHolder(TeleportUtilsModConfig.class).getConfig();
        return serverPlayerEntityTeleport(player, target, targetWorld, config.setInTeleportationStateOnPlayerTeleport);
    }

    @Nullable
    public static Entity teleportEntity(Entity entity, TeleportTarget target, ServerWorld targetWorld,
            boolean particleEffectsOnDeparture, boolean particleEffectsOnArrival) {
        World world = entity.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            if (targetWorld == null) {
                targetWorld = serverWorld;
            }
            if (particleEffectsOnDeparture) {
                spawnPortalParticleEffectAroundEntity(entity);
            }
            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) entity;
                entity = serverPlayerEntityTeleport(player, target, targetWorld);
            } else {
                TeleportUtilsModConfig config = AutoConfig.getConfigHolder(TeleportUtilsModConfig.class).getConfig();
                if (config.stopRidingOnNonPlayerEntityTeleport) {
                    entity.stopRiding();
                }
                if (entity instanceof LivingEntity) {
                    LivingEntity animal = (LivingEntity) entity;
                    entity = livingEntityTeleport(animal, target, targetWorld);
                } else {
                    entity = entityTeleport(entity, target, targetWorld);
                }
            }
            if (entity != null && particleEffectsOnArrival) {
                spawnPortalParticleEffectAroundEntity(entity);
            }
        }
        return entity;
    }

    @Nullable
    public static Entity teleportEntity(Entity entity, TeleportTarget target, ServerWorld targetWorld,
            boolean particleEffects) {
        return teleportEntity(entity, target, targetWorld, particleEffects, particleEffects);
    }

    @Nullable
    public static Entity teleportEntity(Entity entity, TeleportTarget target, ServerWorld targetWorld) {
        TeleportUtilsModConfig config = AutoConfig.getConfigHolder(TeleportUtilsModConfig.class).getConfig();
        return teleportEntity(entity, target, targetWorld, config.particleEffectsOnDeparture,
                config.particleEffectsOnArrival);
    }

    public static boolean startRiding(Entity passenger, Entity vehicle, boolean forceRiding) {
        boolean successfulRide = passenger.startRiding(vehicle, forceRiding);
        return successfulRide;
    }

    public static List<MobEntity> findLeashedAnimals(Entity entity, double expandAmount) {
        return entity.getEntityWorld().getEntitiesByClass(MobEntity.class,
                new Box(entity.getBlockPos()).expand(expandAmount), e -> entity.equals(e.getHoldingEntity()));
    }

    public static List<MobEntity> findLeashedAnimals(Entity entity) {
        TeleportUtilsModConfig config = AutoConfig.getConfigHolder(TeleportUtilsModConfig.class).getConfig();
        return findLeashedAnimals(entity, config.expandAmountForFindingLeashedAnimals);
    }

    @Nullable
    public static Entity teleportEntityWithItsPassengersAndLeashedAnimalsRecursively(Entity entity,
            TeleportTarget target, ServerWorld targetWorld, boolean particleEffectsOnDeparture,
            boolean particleEffectsOnArrival, boolean withPassengers, boolean forceRiding, boolean withLeashedAnimals,
            double expandAmount) {
        List<Entity> passengers = entity.getPassengerList();
        List<MobEntity> leashed = findLeashedAnimals(entity, expandAmount);

        String entityType = "entity";

        boolean hadPassengers = entity.hasPassengers();
        boolean hadLeashedAnimals = !leashed.isEmpty();

        if (hadPassengers) {
            LOGGER.info("[TeleportUtils] Removing all passengers before teleporting: {}", entity.getName().getString());
            entity.removeAllPassengers();
            entityType = "vehicle";
        }

        LOGGER.info("[TeleportUtils] Teleporting {}: {}", entityType, entity.getName().getString());
        Entity oldEntity = entity;
        entity = teleportEntity(entity, target, targetWorld, particleEffectsOnDeparture, particleEffectsOnArrival);

        if (entity != null) {
            LOGGER.info("[TeleportUtils] {} teleported successsfully: {}",
                    entityType.substring(0, 1).toUpperCase() + entityType.substring(1), entity.getName().getString());
            if (hadPassengers && withPassengers) {
                LOGGER.info("[TeleportUtils] Teleporting passengers...");
                for (Entity passenger : passengers) {
                    LOGGER.info("[TeleportUtils] Teleporting single passenger: {} (passenger of {})",
                            passenger.getName().getString(), entity.getName().getString());
                    Entity oldPassenger = passenger;
                    passenger = teleportEntityWithItsPassengersAndLeashedAnimalsRecursively(passenger, target,
                            targetWorld, particleEffectsOnDeparture, particleEffectsOnArrival, withPassengers,
                            forceRiding, withLeashedAnimals, expandAmount);
                    if (passenger != null) {
                        LOGGER.info(
                                "[TeleportUtils] Passenger teleported successfully, riding back: {} (passenger of {})",
                                passenger.getName().getString(), entity.getName().getString());
                        boolean successfulRide = startRiding(passenger, entity, forceRiding);
                        if (!successfulRide) {
                            LOGGER.warn("[TeleportUtils] Failed to ride back: {} (passenger of {})",
                                    passenger.getName().getString(), entity.getName().getString());
                        } else {
                            LOGGER.info("[TeleportUtils] Passenger rided back successfully: {} (passenger of {})",
                                    passenger.getName().getString(), entity.getName().getString());
                        }
                    } else {
                        LOGGER.warn("[TeleportUtils] Failed to teleport a passenger: {} (passenger of {})",
                                oldPassenger.getName().getString(), entity.getName().getString());
                    }
                }
            }
            if (hadLeashedAnimals && withLeashedAnimals) {
                LOGGER.info("[TeleportUtils] Teleporting leashed animals...");
                for (MobEntity animal : leashed) {
                    LOGGER.info("[TeleportUtils] Teleporting single leashed animal: {} (animal of {})",
                            animal.getName().getString(), entity.getName().getString());
                    MobEntity oldAnimal = animal;
                    animal = (MobEntity) teleportEntityWithItsPassengersAndLeashedAnimalsRecursively(animal, target,
                            targetWorld, particleEffectsOnDeparture, particleEffectsOnArrival, withPassengers,
                            forceRiding, withLeashedAnimals, expandAmount);
                    if (animal != null) {
                        LOGGER.info("[TeleportUtils] Animal teleported successfully: {} (animal of {})",
                                animal.getName().getString(), entity.getName().getString());
                    } else {
                        LOGGER.warn("[TeleportUtils] Failed to teleport an animal: {} (animal of {})",
                                oldAnimal.getName().getString(), entity.getName().getString());
                    }
                }
            }
        } else {
            LOGGER.warn("[TeleportUtils] Failed to teleport {}: {}", entityType, oldEntity.getName().getString());
        }

        return entity;
    }

    @Nullable
    public static Entity teleportEntityWithItsPassengersLeashedAnimalsAndVehiclesRecursively(Entity entity,
            TeleportTarget target, ServerWorld targetWorld, boolean particleEffectsOnDeparture,
            boolean particleEffectsOnArrival, boolean withPassengers, boolean forceRiding, boolean withLeashedAnimals,
            double expandAmount, boolean withVehicle, boolean withVehicleRecursively) {
        if (!withVehicle) {
            entity.stopRiding();
        }
        if (entity.hasVehicle()) {
            Entity startingVehicle;
            if (withVehicleRecursively) {
                startingVehicle = entity.getRootVehicle();
            } else {
                startingVehicle = entity.getVehicle();
                startingVehicle.stopRiding();
            }
            LOGGER.info("[TeleportUtils] Teleporting {} starting from vehicle: {}", entity.getName().getString(),
                    startingVehicle.getName().getString());
            startingVehicle = teleportEntityWithItsPassengersAndLeashedAnimalsRecursively(startingVehicle, target,
                    targetWorld, particleEffectsOnDeparture, particleEffectsOnArrival, withPassengers, forceRiding,
                    withLeashedAnimals, expandAmount);
        } else {
            entity = teleportEntityWithItsPassengersAndLeashedAnimalsRecursively(entity, target, targetWorld,
                    particleEffectsOnDeparture, particleEffectsOnArrival, withPassengers, forceRiding,
                    withLeashedAnimals, expandAmount);
        }
        return entity;
    }

    @Nullable
    public static Entity teleportEntityWithItsPassengersLeashedAnimalsAndVehiclesRecursively(Entity entity,
            TeleportTarget target, ServerWorld targetWorld) {
        TeleportUtilsModConfig config = AutoConfig.getConfigHolder(TeleportUtilsModConfig.class).getConfig();
        return teleportEntityWithItsPassengersLeashedAnimalsAndVehiclesRecursively(entity, target, targetWorld,
                config.particleEffectsOnDeparture, config.particleEffectsOnArrival, config.teleportWithPassengers,
                config.forceRidingWhenRidingBack, config.teleportWithLeashedAnimals,
                config.expandAmountForFindingLeashedAnimals, config.teleportWithVehicleRecursively,
                config.teleportWithVehicleRecursively);
    }

}
