package com.payams.atlas.catalog.infrastructure;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import com.payams.atlas.catalog.domain.CosmeticCatalog;
import com.payams.atlas.catalog.domain.CosmeticDefinition;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Imports cosmetic definitions from a JSON source into a catalog.
 * 
 * @author Michael Payams (payams-dev)
 */
public final class JsonCatalogImporter {

    private static final TypeReference<List<CosmeticDefinition>> CATALOG_TYPE =
            new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final CosmeticCatalog catalog;

    /**
     * Creates an importer with the required dependencies.
     */
    public JsonCatalogImporter(
            ObjectMapper objectMapper,
            CosmeticCatalog catalog) {

        this.objectMapper = Objects.requireNonNull(
                objectMapper, "ObjectMapper must not be null.");

        this.catalog = Objects.requireNonNull(
                catalog, "CosmeticCatalog must not be null.");
    }

    /**
     * Imports cosmetic definitions from a JSON array.
     */
    public void importCatalog(InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "Input stream must not be null.");

        List<CosmeticDefinition> cosmetics =
                objectMapper.readValue(inputStream, CATALOG_TYPE);

        if (cosmetics == null) {
            throw new IOException(
                    "Invalid catalog JSON: expected a JSON array.");
        }

        for (CosmeticDefinition cosmetic : cosmetics) {
            if (cosmetic == null) {
                throw new IOException(
                        "Invalid catalog JSON: cosmetic entries must not be null.");
            }

            catalog.add(cosmetic);
        }
    }
}