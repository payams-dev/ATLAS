package com.payams.atlas.catalog.infrastructure;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.payams.atlas.catalog.domain.CosmeticCatalog;
import com.payams.atlas.catalog.domain.CosmeticDefinition;
import com.payams.atlas.catalog.domain.CosmeticType;
import com.payams.atlas.catalog.domain.InvalidCosmeticException;

/**
 * Thread-safe, in-memory implementation of the cosmetic catalog repository.
 */
public class InMemoryCosmeticCatalog implements CosmeticCatalog {

	private final Map<String, CosmeticDefinition> store = new ConcurrentHashMap<>();

	/**
	 * Adds a cosmetic definition to the store if it is valid and unique.
	 */
	@Override
	public void add(CosmeticDefinition cosmetic) {

		if (cosmetic == null) {
			throw new InvalidCosmeticException("Cannot add a null cosmetic definition.");
		}

		// NOTE: implementation changed so that checking for an existing cosmetic
		// and inserting a new one happen, "all-or-nothing".
		// putIfAbsent returns the old value if the key already exists
		CosmeticDefinition existing = store.putIfAbsent(cosmetic.getId(), cosmetic);

		// If the return value is not null, a duplicate was detected
		if (existing != null) {
			throw new InvalidCosmeticException("Duplicate cosmetic ID: " + cosmetic.getId());
		}
	}

	/**
	 * Finds a cosmetic by its unique identifier.
	 */
	@Override
	public Optional<CosmeticDefinition> findById(String id) {
		if (id == null)
			return Optional.empty();
		return Optional.ofNullable(store.get(id));
	}

	/**
	 * Returns all cosmetics matching the specified type.
	 */
	@Override
	public List<CosmeticDefinition> findByType(CosmeticType type) {
		if (type == null)
			return Collections.emptyList();
		return store.values().stream().filter(c -> c.getType() == type).toList();
	}

	/**
	 * Returns an immutable snapshot list of all cosmetics in the store.
	 */
	@Override
	public List<CosmeticDefinition> findAll() {
		return List.copyOf(store.values());
	}
}