package ch.vorburger.minecraft.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.entity.living.animal.Pig;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.EntityUniverse;

/**
 * Helper to simplify spawning entities.
 *
 * @author Michael Vorburger
 */
public class SpawnHelper {
	private final static Logger logger = LoggerFactory.getLogger(SpawnHelper.class);

	private Map<Class<? extends Entity>, EntityType> entityClassToTypeMap;

	protected void populateNewEntityClassToTypeMap(Map<Class<? extends Entity>, EntityType> map) {
		map.put(Human.class, EntityTypes.HUMAN);
		map.put(Pig.class, EntityTypes.PIG);
	}

	// LNE = Log, but Never Exception. Returns Optional instead.
	public <T extends Entity> Optional<T> spawnLNE(Class<T> entityClass, Location<World> startingLocation) {
		try {
			return Optional.of(spawn(entityClass, startingLocation));
		} catch (MinecraftHelperException e) {
			logger.error(e.getMessage(), e);
			return Optional.empty();
		}
	}

	public <T extends Entity> T spawn(Class<T> entityClass, Location<World> location) throws MinecraftHelperException {
		EntityType entityType = getEntityType(entityClass);
		EntityUniverse entityUniverse = location.getExtent();
		Optional<Entity> optionalEntity = entityUniverse.createEntity(entityType, location.getPosition());
		if (optionalEntity.isPresent()) {
			@SuppressWarnings("unchecked") T newEntity = (T) optionalEntity.get();
			boolean isSpawned = entityUniverse.spawnEntity(newEntity, null /* Cause.empty() */);
			if (!isSpawned)
				throw new MinecraftHelperException("Could not spawn new Entity: " + entityType.getName());
			return newEntity;
		} else {
			throw new MinecraftHelperException("Could not create new Entity: " + entityType.getName());
		}
	}

	protected <T extends Entity> EntityType getEntityType(Class<T> entityClass) throws MinecraftHelperException {
		Optional<EntityType> optionalEntityType = getEntityTypeOptional(entityClass);
		if (!optionalEntityType.isPresent()) {
			throw new MinecraftHelperException("EntityType not found for entityClass: " + entityClass.getName());
		}
		EntityType entityType = optionalEntityType.get();
		// TODO isAssignable..
		//		if (!entityType.getEntityClass().equals(entityClass)) {
		//			throw new MinecraftHelperException("EntityType " + entityType.getName() + "'s entityClass " + entityType.getEntityClass() + " != " + entityClass);
		//		}
		return entityType;
	}

	protected <T extends Entity> Optional<EntityType> getEntityTypeOptional(Class<T> entityClass) {
		return Optional.ofNullable(getEntityClassToTypeMap().get(entityClass));
	}

	protected <T extends Entity> Map<Class<? extends Entity>, EntityType> getEntityClassToTypeMap() {
		if (entityClassToTypeMap == null) {
			Map<Class<? extends Entity>, EntityType> newEntityClassToTypeMap = new HashMap<>(70);
			populateNewEntityClassToTypeMap(newEntityClassToTypeMap);
			entityClassToTypeMap = newEntityClassToTypeMap;
		}
		return entityClassToTypeMap;
	}

}
