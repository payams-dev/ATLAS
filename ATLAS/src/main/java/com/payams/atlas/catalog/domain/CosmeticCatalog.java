package com.payams.atlas.catalog.domain;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing and querying cosmetic definitions.
 * 
 * @author Michael Payams (payams-dev)
 */
public interface CosmeticCatalog {

	/**
	 * Adds a cosmetic to the catalog. Throws an exception if the cosmetic is null
	 * or its ID already exists.
	 */
	void add(CosmeticDefinition cosmetic);

	/**
	 * Finds a cosmetic by its unique identifier.
	 */
	Optional<CosmeticDefinition> findById(String id);

	/**
	 * Returns all cosmetics matching the specified type.
	 */
	List<CosmeticDefinition> findByType(CosmeticType type);

	/**
	 * Returns all cosmetics stored in the catalog.
	 */
	List<CosmeticDefinition> findAll();
}