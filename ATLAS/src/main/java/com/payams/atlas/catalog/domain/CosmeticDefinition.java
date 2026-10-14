package com.payams.atlas.catalog.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an immutable catalog definition for a cosmetic item.
 * 
 * @author Michael Payams (payams-dev)
 */
public class CosmeticDefinition {

	private final String id;
	private final String name;
	private final CosmeticType type;
	private final List<String> tags;
	private final Map<String, Object> metadata;

	/**
	 * Constructs a new cosmetic definition with validation and defensive copying.
	 */
	public CosmeticDefinition(@JsonProperty("id") String id, @JsonProperty("name") String name,
			@JsonProperty("type") CosmeticType type, @JsonProperty("tags") List<String> tags,
			@JsonProperty("metadata") Map<String, Object> metadata) {

		if (id == null || id.isBlank()) {
			throw new InvalidCosmeticException("Cosmetic ID cannot be null or blank.");
		}
		if (name == null || name.isBlank()) {
			throw new InvalidCosmeticException("Cosmetic name cannot be null or blank.");
		}
		if (type == null) {
			throw new InvalidCosmeticException("Cosmetic type cannot be null.");
		}

		this.id = id;
		this.name = name;
		this.type = type;

		// initizalizing this way for immutability and memory effeciency!
		this.tags = tags != null ? List.copyOf(tags) : Collections.emptyList();
		this.metadata = metadata != null ? Map.copyOf(metadata) : Collections.emptyMap();
	}

	/**
	 * Returns the unique identifier of the cosmetic.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the type of the cosmetic.
	 */
	public CosmeticType getType() {
		return type;
	}

	/**
	 * Returns the name of the cosmetic.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns an immutable list of tags associated with the cosmetic.
	 */
	public List<String> getTags() {
		return tags;
	}

	/**
	 * Returns an immutable map of optional metadata.
	 */
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;

		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		CosmeticDefinition that = (CosmeticDefinition) o;
		return Objects.equals(id, that.id) && Objects.equals(name, that.name) && type == that.type
				&& Objects.equals(tags, that.tags) && Objects.equals(metadata, that.metadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, type, tags, metadata);
	}

	@Override
	public String toString() {
		return "CosmeticDefinition{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", type=" + type + ", tags="
				+ tags + ", metadata=" + metadata + '}';
	}
}