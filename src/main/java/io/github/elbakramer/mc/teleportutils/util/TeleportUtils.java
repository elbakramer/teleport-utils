package io.github.elbakramer.mc.teleportutils.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.source.BiomeAccess;

import io.github.elbakramer.mc.teleportutils.TeleportUtilsMod;
import io.github.elbakramer.mc.teleportutils.mixin.EntityAccessor;
import io.github.elbakramer.mc.teleportutils.mixin.ServerPlayerEntityAccessor;

public class TeleportUtils {

    private TeleportUtils() {
    }

    private static final Logger LOGGER = TeleportUtilsMod.LOGGER;
    private static TeleportUtilsModConfig config = TeleportUtilsModConfigManager.getConfig();;

    public static boolean isRecursive() {
        final StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        boolean foundInTrace = IntStream.range(2, trace.length)
                .filter(i -> trace[i].getClassName().equals(TeleportUtils.class.getName())).findAny().isPresent();
        return foundInTrace;
    }

    public static void spawnPortalParticleEffectAroundEntity(Entity entity) {
        World world = entity.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            serverWorld.sendEntityStatus(entity, (byte) 46);
        }
    }

    public static Entity entityTeleport(Entity entity, TeleportTarget target) {
        World world = entity.getEntityWorld();
        if (world instanceof ServerWorld) {
            entity.teleport(target.position.getX(), target.position.getY(), target.position.getZ());
            entity.setYaw(target.yaw);
            entity.setPitch(target.pitch);
            entity.setVelocity(target.velocity);
        }
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
                serverWorld.resetIdleTimeout();
                targetWorld.resetIdleTimeout();
                entity = newEntity;
            } else {
                entity = entityTeleport(entity, target);
            }
        }
        return entity;
    }

    public static Entity entityTeleport(Entity entity, TeleportTarget target, ServerWorld targetWorld) {
        return entityTeleport(entity, target, targetWorld, config.forceCopyOnEntityTeleport);
    }

    public static void stopNavigation(Entity entity) {
        if (entity instanceof PathAwareEntity) {
            ((PathAwareEntity) entity).getNavigation().stop();
        }
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
            stopNavigation(entity);
            successful = true;
        }

        if (successful && world instanceof ServerWorld) {
            entity.setYaw(target.yaw);
            entity.setPitch(target.pitch);
            entity.setVelocity(target.velocity);
        }

        return successful;
    }

    public static LivingEntity livingEntityTeleport(LivingEntity entity, TeleportTarget target,
            boolean particleEffects) {
        boolean successful = livingEntityTeleport(entity, target, particleEffects,
                config.loadChunkOnLivingEntityTeleport, config.findGroundOnLivingEntityTeleport,
                config.checkAndRevertOnLivingEntityTeleport);
        if (!successful) {
            LOGGER.warn("[TeleportUtils] Failed to teleport LivingEntity: {}", entity.getName().getString());
        }
        return entity;
    }

    public static LivingEntity livingEntityTeleport(LivingEntity entity, TeleportTarget target) {
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
                stopNavigation(entity);
            } else {
                entity = livingEntityTeleport(entity, target);
            }
        }
        return entity;
    }

    public static LivingEntity livingEntityTeleport(LivingEntity entity, TeleportTarget target,
            ServerWorld targetWorld) {
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
                if (config.stopRidingOnNonPlayerEntityTeleport) {
                    stopRiding(entity);
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
        return teleportEntity(entity, target, targetWorld, config.particleEffectsOnDeparture,
                config.particleEffectsOnArrival);
    }

    public static boolean startRiding(Entity passenger, Entity vehicle, boolean forceRiding) {
        return passenger.startRiding(vehicle, forceRiding);
    }

    public static boolean startRiding(Entity passenger, Entity vehicle) {
        return startRiding(passenger, vehicle, config.forceRidingWhenRidingBack);
    }

    public static void stopRiding(Entity passenger) {
        passenger.stopRiding();
    }

    public static void attachLeash(MobEntity entity, Entity holdingEntity, boolean sendPacket) {
        entity.attachLeash(holdingEntity, sendPacket);
    }

    public static void attachLeash(MobEntity entity, Entity holdingEntity) {
        attachLeash(entity, holdingEntity, config.sendPacketOnAttachLeash);
    }

    public static void detachLeash(MobEntity entity, boolean sendPacket, boolean dropItem) {
        entity.detachLeash(sendPacket, dropItem);
    }

    public static void detachLeash(MobEntity entity) {
        detachLeash(entity, config.sendPacketOnDetachLeash, config.dropItemOnDetachLeash);
    }

    public static List<MobEntity> findLeashedAnimals(Entity entity, double expandAmount) {
        return entity.getEntityWorld().getEntitiesByClass(MobEntity.class,
                new Box(entity.getBlockPos()).expand(expandAmount), e -> entity.equals(e.getHoldingEntity()));
    }

    public static List<MobEntity> findLeashedAnimals(Entity entity) {
        return findLeashedAnimals(entity, config.expandAmountForFindingLeashedAnimals);
    }

    @Nullable
    public static Entity teleportEntityWithItsPassengersAndLeashedAnimalsRecursively(Entity entity,
            TeleportTarget target, ServerWorld targetWorld, boolean particleEffectsOnDeparture,
            boolean particleEffectsOnArrival, boolean withPassengers, boolean forceRiding, boolean withLeashedAnimals,
            double expandAmount, boolean sendPacketOnDetachLeash, boolean dropItemOnDetachLeash,
            boolean sendPacketOnAttachLeash) {
        List<Entity> passengers = entity.getPassengerList();
        List<MobEntity> leashedAnimals = findLeashedAnimals(entity, expandAmount);

        String entityName = entity.getName().getString();
        String entityType = "entity";

        boolean hadPassengers = entity.hasPassengers();
        boolean hadLeashedAnimals = !leashedAnimals.isEmpty();

        boolean detachAndAttachLeashes = targetWorld != null && targetWorld != entity.getEntityWorld();

        if (hadPassengers) {
            LOGGER.info("[TeleportUtils] Removing all passengers before teleporting {}", entityName);
            entity.removeAllPassengers();
            entityType = "vehicle";
        }

        if (hadLeashedAnimals && withLeashedAnimals && detachAndAttachLeashes) {
            leashedAnimals.forEach(e -> detachLeash(e, sendPacketOnDetachLeash, dropItemOnDetachLeash));
        }

        LOGGER.info("[TeleportUtils] Teleporting {} {}", entityType, entityName);
        Entity oldEntity = entity;
        entity = teleportEntity(entity, target, targetWorld, particleEffectsOnDeparture, particleEffectsOnArrival);

        if (entity != null) {
            LOGGER.info("[TeleportUtils] {} {} teleported successsfully",
                    entityType.substring(0, 1).toUpperCase() + entityType.substring(1), entity.getName().getString());
            if (hadPassengers && withPassengers) {
                LOGGER.info("[TeleportUtils] Teleporting passengers...");
                for (Entity passenger : passengers) {
                    String passengerName = passenger.getName().getString();
                    LOGGER.info("[TeleportUtils] Teleporting single passenger {} (passenger of {})", passengerName,
                            entityName);
                    Entity oldPassenger = passenger;
                    passenger = teleportEntityWithItsPassengersAndLeashedAnimalsRecursively(passenger, target,
                            targetWorld, particleEffectsOnDeparture, particleEffectsOnArrival, withPassengers,
                            forceRiding, withLeashedAnimals, expandAmount, sendPacketOnDetachLeash,
                            dropItemOnDetachLeash, sendPacketOnAttachLeash);
                    if (passenger != null) {
                        LOGGER.info("[TeleportUtils] Passenger {} teleported successfully, riding {} back",
                                passengerName, entityName);
                        boolean successfulRide = startRiding(passenger, entity, forceRiding);
                        if (!successfulRide) {
                            LOGGER.warn("[TeleportUtils] Passenger {} failed to ride {} back", passengerName,
                                    entityName);
                        } else {
                            LOGGER.info("[TeleportUtils] Passenger {} rided {} back successfully", passengerName,
                                    entityName);
                        }
                    } else {
                        String oldPassengerName = oldPassenger.getName().getString();
                        LOGGER.warn("[TeleportUtils] Failed to teleport a passenger {} (passenger of {})",
                                oldPassengerName, entityName);
                    }
                }
            }
            if (hadLeashedAnimals && withLeashedAnimals) {
                LOGGER.info("[TeleportUtils] Teleporting leashed animals...");
                for (MobEntity animal : leashedAnimals) {
                    String animalName = animal.getName().getString();
                    LOGGER.info("[TeleportUtils] Teleporting single leashed animal {} (animal of {})", animalName,
                            entityName);
                    MobEntity oldAnimal = animal;
                    animal = (MobEntity) teleportEntityWithItsPassengersAndLeashedAnimalsRecursively(animal, target,
                            targetWorld, particleEffectsOnDeparture, particleEffectsOnArrival, withPassengers,
                            forceRiding, withLeashedAnimals, expandAmount, sendPacketOnDetachLeash,
                            dropItemOnDetachLeash, sendPacketOnAttachLeash);
                    if (animal != null) {
                        LOGGER.info("[TeleportUtils] Animal {} teleported successfully (animal of {})", animalName,
                                entityName);
                        if (detachAndAttachLeashes) {
                            attachLeash(animal, entity, sendPacketOnAttachLeash);
                        }
                    } else {
                        String oldAnimalName = oldAnimal.getName().getString();
                        LOGGER.warn("[TeleportUtils] Failed to teleport an animal {} (animal of {})", oldAnimalName,
                                entityName);
                    }
                }
            }
        } else {
            String oldEntityName = oldEntity.getName().getString();
            LOGGER.warn("[TeleportUtils] Failed to teleport {} {}", entityType, oldEntityName);
        }

        return entity;
    }

    @Nullable
    public static Entity teleportEntityWithItsPassengersLeashedAnimalsAndVehiclesRecursively(Entity entity,
            TeleportTarget target, ServerWorld targetWorld, boolean particleEffectsOnDeparture,
            boolean particleEffectsOnArrival, boolean withPassengers, boolean forceRiding, boolean withLeashedAnimals,
            double expandAmount, boolean sendPacketOnDetachLeash, boolean dropItemOnDetachLeash,
            boolean sendPacketOnAttachLeash, boolean withVehicle, boolean withVehicleRecursively) {
        Entity startingEntity = entity;

        if (entity.hasVehicle() && withVehicle) {
            if (withVehicleRecursively) {
                startingEntity = entity.getRootVehicle();
            } else {
                startingEntity = entity.getVehicle();
            }
        }

        if (startingEntity != entity) {
            String entityName = entity.getName().getString();
            String startingEntityName = startingEntity.getName().getString();
            LOGGER.info("[TeleportUtils] Teleporting {} starting from vehicle {}", entityName, startingEntityName);
        }

        stopRiding(startingEntity);
        startingEntity = teleportEntityWithItsPassengersAndLeashedAnimalsRecursively(startingEntity, target,
                targetWorld, particleEffectsOnDeparture, particleEffectsOnArrival, withPassengers, forceRiding,
                withLeashedAnimals, expandAmount, sendPacketOnDetachLeash, dropItemOnDetachLeash,
                sendPacketOnAttachLeash);

        return entity; // TODO: Find teleported entity (in case of copy) and return that instead
    }

    @Nullable
    public static Entity teleportEntityWithItsPassengersLeashedAnimalsAndVehiclesRecursively(Entity entity,
            TeleportTarget target, ServerWorld targetWorld) {
        return teleportEntityWithItsPassengersLeashedAnimalsAndVehiclesRecursively(entity, target, targetWorld,
                config.particleEffectsOnDeparture, config.particleEffectsOnArrival, config.teleportWithPassengers,
                config.forceRidingWhenRidingBack, config.teleportWithLeashedAnimals,
                config.expandAmountForFindingLeashedAnimals, config.sendPacketOnDetachLeash,
                config.dropItemOnDetachLeash, config.sendPacketOnAttachLeash, config.teleportWithVehicleRecursively,
                config.teleportWithVehicleRecursively);
    }

    @Nullable
    public static Entity teleportEntityWithItsPassengersLeashedAnimalsAndVehiclesRecursively(Entity entity,
            TeleportTarget target) {
        return teleportEntityWithItsPassengersLeashedAnimalsAndVehiclesRecursively(entity, target, null);
    }

    public static void propagateLastNetherPortalPositionRecursively(Entity entity, BlockPos pos) {
        EntityAccessor entityAccessor = (EntityAccessor) entity;
        if (!entity.world.isClient && !pos.equals(entityAccessor.getLastNetherPortalPosition())) {
            entityAccessor.setLastNetherPortalPosition(pos.toImmutable());
        }
        if (config.moveToWorldWithPassengers) {
            List<Entity> passengers = entity.getPassengerList();
            passengers.forEach(e -> propagateLastNetherPortalPositionRecursively(e, pos));
        }
        if (config.moveToWorldWithLeashedAnimals) {
            List<MobEntity> leashedAnimals = findLeashedAnimals(entity, config.expandAmountForFindingLeashedAnimals);
            leashedAnimals.forEach(e -> propagateLastNetherPortalPositionRecursively(e, pos));
        }
    }

    public static String serverWorldToString(ServerWorld serverWorld) {
        return String.format("'%s'/%s", (Object) serverWorld, (Object) serverWorld.getRegistryKey().getValue());
    }

    public static TeleportTarget getTeleportTarget(Entity entity, ServerWorld destination) {
        EntityAccessor entityAccessor = (EntityAccessor) entity;
        return entityAccessor.invokeGetTeleportTarget(destination);
    }

    @Nullable
    public static Entity moveToWorld(Entity entity, ServerWorld destination, boolean resetNetherPortalCooldown) {
        if (resetNetherPortalCooldown) {
            entity.resetNetherPortalCooldown();
        }
        entity = entity.moveToWorld(destination);
        return entity;
    }

    @Nullable
    public static Entity moveToWorld(Entity entity, ServerWorld destination) {
        return moveToWorld(entity, destination, config.resetNetherPortalCooldownOnMoveToWorld);
    }

    @Nullable
    public static Entity entityMoveToWorld(Entity entity, ServerWorld destination, TeleportTarget teleportTarget) {
        if (entity.world instanceof ServerWorld && !entity.isRemoved()) {
            entity.world.getProfiler().push("changeDimension");
            entity.detach();
            entity.world.getProfiler().push("reposition");
            EntityAccessor entityAccessor = (EntityAccessor) entity;
            if (teleportTarget == null) {
                teleportTarget = getTeleportTarget(entity, destination);
            }
            if (teleportTarget == null) {
                return null;
            } else {
                entity.world.getProfiler().swap("reloading");
                Entity newEntity = entity.getType().create(destination);
                if (newEntity != null) {
                    newEntity.copyFrom(entity);
                    newEntity.refreshPositionAndAngles(teleportTarget.position.x, teleportTarget.position.y,
                            teleportTarget.position.z, teleportTarget.yaw, entity.getPitch());
                    newEntity.setVelocity(teleportTarget.velocity);
                    destination.onDimensionChanged(newEntity);
                    if (destination.getRegistryKey() == World.END) {
                        ServerWorld.createEndSpawnPlatform(destination);
                    }
                }
                entityAccessor.invokeRemoveFromDimension();
                entity.world.getProfiler().pop();
                ((ServerWorld) entity.world).resetIdleTimeout();
                destination.resetIdleTimeout();
                entity.world.getProfiler().pop();
                return entity;
            }
        } else {
            return null;
        }
    }

    @Nullable
    public static ServerPlayerEntity serverPlayerEntityMoveToWorld(ServerPlayerEntity player, ServerWorld destination,
            TeleportTarget teleportTarget) {
        ServerPlayerEntityAccessor playerAccessor = (ServerPlayerEntityAccessor) player;
        EntityAccessor entityAccessor = (EntityAccessor) player;
        playerAccessor.setInTeleportationState(true);
        ServerWorld serverWorld = player.getServerWorld();
        RegistryKey<World> registryKey = serverWorld.getRegistryKey();
        if (registryKey == World.END && destination.getRegistryKey() == World.OVERWORLD) {
            player.detach();
            player.getServerWorld().removePlayer(player, Entity.RemovalReason.CHANGED_DIMENSION);
            if (!player.notInAnyWorld) {
                player.notInAnyWorld = true;
                player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_WON,
                        playerAccessor.getSeenCredits() ? GameStateChangeS2CPacket.DEMO_OPEN_SCREEN : 1.0F));
                playerAccessor.setSeenCredits(true);
            }
            return player;
        } else {
            WorldProperties worldProperties = destination.getLevelProperties();
            player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(destination.getDimension(),
                    destination.getRegistryKey(), BiomeAccess.hashSeed(destination.getSeed()),
                    player.interactionManager.getGameMode(), player.interactionManager.getPreviousGameMode(),
                    destination.isDebugWorld(), destination.isFlat(), true));
            player.networkHandler.sendPacket(
                    new DifficultyS2CPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
            PlayerManager playerManager = player.server.getPlayerManager();
            playerManager.sendCommandTree(player);
            serverWorld.removePlayer(player, Entity.RemovalReason.CHANGED_DIMENSION);
            entityAccessor.invokeUnsetRemoved();
            if (teleportTarget == null) {
                teleportTarget = getTeleportTarget(player, destination);
            }
            if (teleportTarget != null) {
                serverWorld.getProfiler().push("moving");
                if (registryKey == World.OVERWORLD && destination.getRegistryKey() == World.NETHER) {
                    playerAccessor.setEnteredNetherPos(player.getPos());
                } else if (destination.getRegistryKey() == World.END) {
                    playerAccessor.invokeCreateEndSpawnPlatform(destination, new BlockPos(teleportTarget.position));
                }
                serverWorld.getProfiler().pop();
                serverWorld.getProfiler().push("placing");
                player.setWorld(destination);
                destination.onPlayerChangeDimension(player);
                entityAccessor.invokeSetRotation(teleportTarget.yaw, teleportTarget.pitch);
                player.refreshPositionAfterTeleport(teleportTarget.position.x, teleportTarget.position.y,
                        teleportTarget.position.z);
                serverWorld.getProfiler().pop();
                playerAccessor.invokeWorldChanged(serverWorld);
                player.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(player.getAbilities()));
                playerManager.sendWorldInfo(player, destination);
                playerManager.sendPlayerStatus(player);
                Iterator<StatusEffectInstance> var7 = player.getStatusEffects().iterator();
                while (var7.hasNext()) {
                    StatusEffectInstance statusEffectInstance = (StatusEffectInstance) var7.next();
                    player.networkHandler
                            .sendPacket(new EntityStatusEffectS2CPacket(player.getId(), statusEffectInstance));
                }
                player.networkHandler.sendPacket(
                        new WorldEventS2CPacket(WorldEvents.TRAVEL_THROUGH_PORTAL, BlockPos.ORIGIN, 0, false));
                playerAccessor.setSyncedExperience(-1);
                playerAccessor.setSyncedHealth(-1.0F);
                playerAccessor.setSyncedFoodLevel(-1);
            }
            return player;
        }
    }

    @Nullable
    public static Entity moveToWorld(Entity entity, ServerWorld destination, TeleportTarget teleportTarget,
            boolean resetNetherPortalCooldown) {
        if (teleportTarget == null) {
            entity = moveToWorld(entity, destination, resetNetherPortalCooldown);
        } else {
            if (resetNetherPortalCooldown) {
                entity.resetNetherPortalCooldown();
            }
            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) entity;
                entity = serverPlayerEntityMoveToWorld(player, destination, teleportTarget);
            } else {
                if (entity instanceof MerchantEntity) {
                    ((MerchantEntity) entity).setCurrentCustomer((PlayerEntity) null);
                }
                entity = entityMoveToWorld(entity, destination, teleportTarget);
            }
        }
        return entity;
    }

    @Nullable
    public static Entity moveToWorld(Entity entity, ServerWorld destination, TeleportTarget teleportTarget) {
        return moveToWorld(entity, destination, teleportTarget, config.resetNetherPortalCooldownOnMoveToWorld);
    }

    @Nullable
    public static Entity getEntityToFollow(Entity entity) {
        // owner
        if (entity instanceof TameableEntity) {
            TameableEntity tameable = (TameableEntity) entity;
            LivingEntity owner = tameable.getOwner();
            if (owner != null) {
                return owner;
            }
        }
        // player vehicle
        if (entity.hasVehicle()) {
            Entity vehicle = entity.getVehicle();
            if (vehicle instanceof PlayerEntity) {
                return vehicle;
            }
        }
        // holder
        if (entity instanceof MobEntity) {
            MobEntity mob = (MobEntity) entity;
            Entity holding = mob.getHoldingEntity();
            if (holding != null) {
                return holding;
            }
        }
        // player passenger
        Optional<Entity> maybePlayerPassenger = entity.getPassengerList().stream().filter(e -> {
            return e instanceof PlayerEntity;
        }).findFirst();
        if (maybePlayerPassenger.isPresent()) {
            return maybePlayerPassenger.get();
        }
        // none
        return null;
    }

    @Nullable
    public static Entity moveToWorldWithItsPassengersAndLeashedAnimalsRecursively(Entity entity,
            ServerWorld destination, TeleportTarget teleportTarget, boolean followTeleportTarget,
            boolean resetNetherPortalCooldown, boolean withPassengers, boolean forceRiding, boolean withLeashedAnimals,
            double expandAmount, boolean sendPacketOnDetachLeash, boolean dropItemOnDetachLeash,
            boolean sendPacketOnAttachLeash) {
        if (teleportTarget == null) {
            if (followTeleportTarget) {
                Entity entityToFollow = getEntityToFollow(entity);
                if (entityToFollow != null) {
                    teleportTarget = getTeleportTarget(entityToFollow, destination);
                }
            }
        }

        List<Entity> passengers = entity.getPassengerList();
        List<MobEntity> leashedAnimals = findLeashedAnimals(entity, expandAmount);

        Function<Entity, TeleportTarget> getTeleportTargetLocal;

        if (followTeleportTarget) {
            Map<Entity, TeleportTarget> targetCache = new ConcurrentHashMap<>();
            Function<Entity, TeleportTarget> getTeleportTargetWithCache = e -> e == null ? null
                    : targetCache.computeIfAbsent(e, f -> getTeleportTarget(f, destination));
            getTeleportTargetLocal = getTeleportTargetWithCache;
        } else {
            getTeleportTargetLocal = e -> null;
        }

        List<Entity> passengerFollowees = passengers.stream().map(e -> getEntityToFollow(e))
                .collect(Collectors.toList());
        List<Entity> leashedAnimalFollowees = leashedAnimals.stream().map(e -> getEntityToFollow(e))
                .collect(Collectors.toList());

        List<TeleportTarget> passengerTargets = passengerFollowees.stream().map(getTeleportTargetLocal)
                .collect(Collectors.toList());
        List<TeleportTarget> leashedAnimalTargets = leashedAnimalFollowees.stream().map(getTeleportTargetLocal)
                .collect(Collectors.toList());

        String entityName = entity.getName().getString();
        String entityType = "entity";

        boolean hadPassengers = entity.hasPassengers();
        boolean hadLeashedAnimals = !leashedAnimals.isEmpty();

        String dimensionName = serverWorldToString(destination);

        if (hadPassengers) {
            LOGGER.info("[TeleportUtils] Removing all passengers before moving {} to world {}", entityName,
                    dimensionName);
            entity.removeAllPassengers();
            entityType = "vehicle";
        }

        if (hadLeashedAnimals) {
            for (int i = 0; i < leashedAnimals.size(); i++) {
                MobEntity animal = leashedAnimals.get(i);
                Entity animalFollowee = leashedAnimalFollowees.get(i);
                boolean withThisLeashedAnimal = animalFollowee.equals(entity);
                boolean dropItem = dropItemOnDetachLeash || !withLeashedAnimals || !withThisLeashedAnimal;
                detachLeash(animal, sendPacketOnDetachLeash, dropItem);
            }
        }

        LOGGER.info("[TeleportUtils] Moving {} {} to world {}", entityType, entityName, dimensionName);
        Entity oldEntity = entity;
        // entity = moveToWorld(entity, destination, teleportTarget,
        // resetNetherPortalCooldown);
        entity = moveToWorld(entity, destination, resetNetherPortalCooldown);

        if (entity != null) {
            LOGGER.info("[TeleportUtils] {} {} moved to world {} successsfully",
                    entityType.substring(0, 1).toUpperCase() + entityType.substring(1), entityName, dimensionName);
            if (hadPassengers && withPassengers) {
                LOGGER.info("[TeleportUtils] Moving passengers to world {}...", dimensionName);
                for (int i = 0; i < passengers.size(); i++) {
                    Entity passenger = passengers.get(i);
                    TeleportTarget target = passengerTargets.get(i);
                    String passengerName = passenger.getName().getString();
                    LOGGER.info("[TeleportUtils] Moving single passenger {} to world {} (passenger of {})",
                            passengerName, dimensionName, entityName);
                    Entity oldPassenger = passenger;

                    passenger = moveToWorldWithItsPassengersAndLeashedAnimalsRecursively(passenger, destination, target,
                            followTeleportTarget, resetNetherPortalCooldown, withPassengers, forceRiding,
                            withLeashedAnimals, expandAmount, sendPacketOnDetachLeash, dropItemOnDetachLeash,
                            sendPacketOnAttachLeash);
                    if (passenger != null) {
                        LOGGER.info("[TeleportUtils] Passenger {} moved to world {} successfully, riding {} back",
                                passengerName, dimensionName, entityName);
                        boolean successfulRide = startRiding(passenger, entity, forceRiding);
                        if (!successfulRide) {
                            LOGGER.warn("[TeleportUtils] Passenger {} failed to ride {} back", passengerName,
                                    entityName);
                        } else {
                            LOGGER.info("[TeleportUtils] Passenger {} rided {} back successfully", passengerName,
                                    entityName);
                        }
                    } else {
                        String oldPassengerName = oldPassenger.getName().getString();
                        LOGGER.warn("[TeleportUtils] Failed to move a passenger {} to world {} (passenger of {})",
                                oldPassengerName, dimensionName, entityName);
                    }
                }
                ;
            }
            if (hadLeashedAnimals && withLeashedAnimals) {
                LOGGER.info("[TeleportUtils] Moving leashed animals to world {}...", dimensionName);
                for (int i = 0; i < leashedAnimals.size(); i++) {
                    MobEntity animal = leashedAnimals.get(i);
                    TeleportTarget target = leashedAnimalTargets.get(i);
                    String animalName = animal.getName().getString();
                    LOGGER.info("[TeleportUtils] Moving single leashed animal {} to world {} (animal of {})",
                            animalName, dimensionName, entityName);
                    MobEntity oldAnimal = animal;
                    animal = (MobEntity) moveToWorldWithItsPassengersAndLeashedAnimalsRecursively(animal, destination,
                            target, followTeleportTarget, resetNetherPortalCooldown, withPassengers, forceRiding,
                            withLeashedAnimals, expandAmount, sendPacketOnDetachLeash, dropItemOnDetachLeash,
                            sendPacketOnAttachLeash);
                    if (animal != null) {
                        LOGGER.info("[TeleportUtils] Animal {} moved to world {} successfully (animal of {})",
                                animalName, dimensionName, entityName);
                        attachLeash(animal, entity, sendPacketOnAttachLeash);
                    } else {
                        String oldAnimalName = oldAnimal.getName().getString();
                        LOGGER.warn("[TeleportUtils] Failed to move an animal {} to world {} (animal of {})",
                                oldAnimalName, dimensionName, entityName);
                    }
                }
            }
        } else {
            String oldEntityName = oldEntity.getName().getString();
            LOGGER.warn("[TeleportUtils] Failed to move {} {} to world {}", entityType, oldEntityName, dimensionName);
        }

        return entity;
    }

    @Nullable
    public static Entity moveToWorldWithItsPassengersLeashedAnimalsAndVehiclesRecursively(Entity entity,
            ServerWorld destination, TeleportTarget teleportTarget, boolean resetNetherPortalCooldown,
            boolean followTeleportTarget, boolean withPassengers, boolean forceRiding, boolean withLeashedAnimals,
            double expandAmount, boolean sendPacketOnDetachLeash, boolean dropItemOnDetachLeash,
            boolean sendPacketOnAttachLeash, boolean withVehicle, boolean withVehicleRecursively) {
        Entity startingEntity = entity;

        if (entity.hasVehicle() && withVehicle) {
            if (withVehicleRecursively) {
                startingEntity = entity.getRootVehicle();
            } else {
                startingEntity = entity.getVehicle();
            }
        }

        if (startingEntity != entity) {
            String entityName = entity.getName().getString();
            String dimensionName = serverWorldToString(destination);
            String startingEntityName = startingEntity.getName().getString();
            LOGGER.info("[TeleportUtils] Moving {} to world {} starting from vehicle {}", entityName, dimensionName,
                    startingEntityName);
        }

        stopRiding(startingEntity);
        startingEntity = moveToWorldWithItsPassengersAndLeashedAnimalsRecursively(startingEntity, destination,
                teleportTarget, followTeleportTarget, resetNetherPortalCooldown, withPassengers, forceRiding,
                withLeashedAnimals, expandAmount, sendPacketOnDetachLeash, dropItemOnDetachLeash,
                sendPacketOnAttachLeash);

        return entity; // TODO: Find moved entity (in case of copy) and return that instead
    }

    @Nullable
    public static Entity moveToWorldWithItsPassengersLeashedAnimalsAndVehiclesRecursively(Entity entity,
            ServerWorld destination, TeleportTarget teleportTarget) {
        return moveToWorldWithItsPassengersLeashedAnimalsAndVehiclesRecursively(entity, destination, teleportTarget,
                config.followTeleportTargetOnMoveToWorld, config.resetNetherPortalCooldownOnMoveToWorld,
                config.moveToWorldWithPassengers, config.forceRidingWhenRidingBack,
                config.moveToWorldWithLeashedAnimals, config.expandAmountForFindingLeashedAnimals,
                config.sendPacketOnDetachLeash, config.dropItemOnDetachLeash, config.sendPacketOnAttachLeash,
                config.moveToWorldWithVehicleRecursively, config.moveToWorldWithVehicleRecursively);
    }

    @Nullable
    public static Entity moveToWorldWithItsPassengersLeashedAnimalsAndVehiclesRecursively(Entity entity,
            ServerWorld destination) {
        return moveToWorldWithItsPassengersLeashedAnimalsAndVehiclesRecursively(entity, destination, null);
    }

}
