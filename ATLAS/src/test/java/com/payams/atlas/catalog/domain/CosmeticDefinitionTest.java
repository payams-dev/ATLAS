package com.payams.atlas.catalog.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CosmeticDefinitionTest {

	@Test
	void shouldCreateCosmeticDefinitionSuccessfullyWithValidInputs() {
		List<String> tags = List.of("legendary", "season-1");
		Map<String, Object> metadata = Map.of("rarity", "Legendary", "animated", true);

		CosmeticDefinition cosmetic = new CosmeticDefinition("cos_123", "Aura Skin", CosmeticType.SKIN, tags, metadata);

		assertEquals("cos_123", cosmetic.getId());
		assertEquals("Aura Skin", cosmetic.getName());
		assertEquals(CosmeticType.SKIN, cosmetic.getType());
		assertEquals(tags, cosmetic.getTags());
		assertEquals(metadata, cosmetic.getMetadata());
	}

	@Test
	void shouldHandleNullTagsAndMetadataGracefully() {
		CosmeticDefinition cosmetic = new CosmeticDefinition("cos_123", "Default Glider", CosmeticType.GLIDER, null,
				null);

		assertNotNull(cosmetic.getTags());
		assertTrue(cosmetic.getTags().isEmpty());
		assertNotNull(cosmetic.getMetadata());
		assertTrue(cosmetic.getMetadata().isEmpty());
	}

	@Test
	void shouldEnforceDefensiveCopyingForTagsAndMetadata() {
		List<String> mutableTags = new ArrayList<>();
		mutableTags.add("epic");

		Map<String, Object> mutableMetadata = new HashMap<>();
		mutableMetadata.put("key", "value");

		CosmeticDefinition cosmetic = new CosmeticDefinition("cos_123", "Star Wand", CosmeticType.PICKAXE, mutableTags,
				mutableMetadata);

		// Mutating original collections should not affect the instance
		mutableTags.add("exclusive");
		mutableMetadata.put("key", "changed");

		assertEquals(1, cosmetic.getTags().size());
		assertEquals("value", cosmetic.getMetadata().get("key"));

		// Returned collections should be immutable
		assertThrows(UnsupportedOperationException.class, () -> cosmetic.getTags().add("new-tag"));
		assertThrows(UnsupportedOperationException.class, () -> cosmetic.getMetadata().put("new-key", "new-value"));
	}

	@Test
	void shouldThrowExceptionWhenIdIsNullOrBlank() {
		assertThrows(InvalidCosmeticException.class,
				() -> new CosmeticDefinition(null, "Aura", CosmeticType.SKIN, null, null));
		assertThrows(InvalidCosmeticException.class,
				() -> new CosmeticDefinition("   ", "Aura", CosmeticType.SKIN, null, null));
	}

	@Test
	void shouldThrowExceptionWhenNameIsNullOrBlank() {
		assertThrows(InvalidCosmeticException.class,
				() -> new CosmeticDefinition("cos_1", null, CosmeticType.SKIN, null, null));
		assertThrows(InvalidCosmeticException.class,
				() -> new CosmeticDefinition("cos_1", "", CosmeticType.SKIN, null, null));
	}

	@Test
	void shouldThrowExceptionWhenTypeIsNull() {
		assertThrows(InvalidCosmeticException.class, () -> new CosmeticDefinition("cos_1", "Aura", null, null, null));
	}

	@Test
	void shouldVerifyEqualsAndHashCode() {
		CosmeticDefinition cosmetic1 = new CosmeticDefinition("cos_1", "Floss", CosmeticType.EMOTE, List.of("dance"),
				Map.of("interactive", true));
		CosmeticDefinition cosmetic2 = new CosmeticDefinition("cos_1", "Floss", CosmeticType.EMOTE, List.of("dance"),
				Map.of("interactive", true));
		CosmeticDefinition cosmetic3 = new CosmeticDefinition("cos_2", "Magma", CosmeticType.WRAP, List.of("animated"),
				Map.of("interactive", false));

		assertEquals(cosmetic1, cosmetic2);
		assertEquals(cosmetic1.hashCode(), cosmetic2.hashCode());
		assertNotEquals(cosmetic1, cosmetic3);
	}

	@Test
	void shouldProvideMeaningfulToString() {
		CosmeticDefinition cosmetic = new CosmeticDefinition("cos_1", "Frozen Shroud", CosmeticType.BACK_BLING,
				List.of("reactive"), Map.of());

		String toStringResult = cosmetic.toString();
		assertTrue(toStringResult.contains("cos_1"));
		assertTrue(toStringResult.contains("Frozen Shroud"));
		assertTrue(toStringResult.contains("BACK_BLING"));
	}
}