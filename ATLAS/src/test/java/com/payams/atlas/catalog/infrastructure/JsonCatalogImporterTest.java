package com.payams.atlas.catalog.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.payams.atlas.catalog.domain.InvalidCosmeticException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;

import com.payams.atlas.catalog.domain.CosmeticCatalog;
import com.payams.atlas.catalog.domain.CosmeticDefinition;

import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.ObjectMapper;

class JsonCatalogImporterTest {

	private CosmeticCatalog catalog;
	private JsonCatalogImporter importer;

	@BeforeEach
	void setUp() {
		catalog = new InMemoryCosmeticCatalog();
		importer = new JsonCatalogImporter(new ObjectMapper(), catalog);
	}

	@Test
	void shouldImportAllSixCosmetics() throws IOException {
		String json = """
				[
				  {"id":"outfit_001","name":"DEFAULT DAN","type":"SKIN"},
				  {"id":"pickaxe_001","name":"DAN SMASHER","type":"PICKAXE"},
				  {"id":"backbling_001","name":"DAN PACK","type":"BACK_BLING"},
				  {"id":"wrap_001","name":"THE BASIC","type":"WRAP"},
				  {"id":"glider_001","name":"DAN'S DEFAULT CHUTE","type":"GLIDER"},
				  {"id":"emote_001","name":"DEFAULT SHUFFLE","type":"EMOTE"}
				]
				""";

		importer.importCatalog(inputStream(json));

		assertEquals(6, catalog.findAll().size());

		assertTrue(catalog.findById("outfit_001").isPresent());
		assertTrue(catalog.findById("pickaxe_001").isPresent());
		assertTrue(catalog.findById("backbling_001").isPresent());
		assertTrue(catalog.findById("wrap_001").isPresent());
		assertTrue(catalog.findById("glider_001").isPresent());
		assertTrue(catalog.findById("emote_001").isPresent());
	}

	@Test
	void shouldImportCosmeticMetadataAndTags() throws IOException {
		String json = """
				[
				  {
				    "id": "outfit_001",
				    "name": "DEFAULT DAN",
				    "type": "SKIN",
				    "tags": ["default", "common"],
				    "metadata": {
				      "rarity": "Common",
				      "seasonIntroduced": 1
				    }
				  }
				]
				""";

		importer.importCatalog(inputStream(json));

		CosmeticDefinition cosmetic = catalog.findById("outfit_001").orElseThrow();

		assertEquals("DEFAULT DAN", cosmetic.getName());
		assertEquals(2, cosmetic.getTags().size());
		assertEquals("default", cosmetic.getTags().get(0));
		assertEquals("Common", cosmetic.getMetadata().get("rarity"));
		assertEquals(1, cosmetic.getMetadata().get("seasonIntroduced"));
	}

	@Test
	void shouldAcceptAnEmptyJsonArray() throws IOException {
		importer.importCatalog(inputStream("[]"));

		assertTrue(catalog.findAll().isEmpty());
	}

	@Test
	void shouldRejectMalformedJson() {
	    assertThrows(
	            StreamReadException.class,
	            () -> importer.importCatalog(inputStream("[invalid json"))
	    );

	    assertTrue(catalog.findAll().isEmpty());
	}

	@Test
	void shouldRejectJsonNull() {
		assertThrows(IOException.class, () -> importer.importCatalog(inputStream("null")));

		assertTrue(catalog.findAll().isEmpty());
	}

	@Test
	void shouldRejectNullCosmeticEntry() {
		assertThrows(IOException.class, () -> importer.importCatalog(inputStream("[null]")));

		assertTrue(catalog.findAll().isEmpty());
	}

	@Test
	void shouldRejectNullInputStream() {
		assertThrows(NullPointerException.class, () -> importer.importCatalog(null));
	}

	@Test
	void shouldRejectNullObjectMapper() {
		assertThrows(NullPointerException.class, () -> new JsonCatalogImporter(null, catalog));
	}

	@Test
	void shouldRejectNullCatalog() {
		assertThrows(NullPointerException.class, () -> new JsonCatalogImporter(new ObjectMapper(), null));
	}

	@Test
	void shouldRejectDuplicateCosmeticIds() throws IOException {
		String json = """
				[
				  {"id":"outfit_001","name":"DEFAULT DAN","type":"SKIN"},
				  {"id":"outfit_001","name":"ANOTHER DAN","type":"SKIN"}
				]
				""";

		assertThrows(InvalidCosmeticException.class, () -> importer.importCatalog(inputStream(json)));

		
		assertEquals(1, catalog.findAll().size());
		assertEquals("DEFAULT DAN", catalog.findById("outfit_001").orElseThrow().getName());
	}

	private InputStream inputStream(String json) {
		return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
	}
}
