package com.payams.atlas.catalog.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.payams.atlas.catalog.domain.CosmeticDefinition;
import com.payams.atlas.catalog.domain.CosmeticType;
import com.payams.atlas.catalog.domain.InvalidCosmeticException;

class InMemoryCosmeticCatalogTest {

    private InMemoryCosmeticCatalog catalog;
    private CosmeticDefinition skinCosmetic;
    private CosmeticDefinition emoteCosmetic;

    @BeforeEach
    void setUp() {
        catalog = new InMemoryCosmeticCatalog();
        skinCosmetic = new CosmeticDefinition("cos_1", "Aura", CosmeticType.SKIN, List.of("rare"), null);
        emoteCosmetic = new CosmeticDefinition("cos_2", "Floss", CosmeticType.EMOTE, List.of("dance"), null);
    }

    @Test
    void shouldAddAndFindCosmeticByIdSuccessfully() {
        catalog.add(skinCosmetic);

        Optional<CosmeticDefinition> found = catalog.findById("cos_1");
        assertTrue(found.isPresent());
        assertEquals(skinCosmetic, found.get());
    }

    @Test
    void shouldReturnEmptyWhenIdNotFoundOrNull() {
        catalog.add(skinCosmetic);

        assertTrue(catalog.findById("non_existent").isEmpty());
        assertTrue(catalog.findById(null).isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenAddingNullCosmetic() {
        assertThrows(InvalidCosmeticException.class, () -> catalog.add(null));
    }

    @Test
    void shouldThrowExceptionWhenAddingDuplicateId() {
        catalog.add(skinCosmetic);
        
        CosmeticDefinition duplicateIdCosmetic = new CosmeticDefinition(
                "cos_1", "Different Name", CosmeticType.EMOTE, null, null
        );

        assertThrows(InvalidCosmeticException.class, () -> catalog.add(duplicateIdCosmetic));
    }

    @Test
    void shouldFindByTypeSuccessfully() {
        CosmeticDefinition anotherSkin = new CosmeticDefinition("cos_3", "Renegade", CosmeticType.SKIN, null, null);
        
        catalog.add(skinCosmetic);
        catalog.add(emoteCosmetic);
        catalog.add(anotherSkin);

        List<CosmeticDefinition> skins = catalog.findByType(CosmeticType.SKIN);
        assertEquals(2, skins.size());
        assertTrue(skins.contains(skinCosmetic));
        assertTrue(skins.contains(anotherSkin));

        List<CosmeticDefinition> emotes = catalog.findByType(CosmeticType.EMOTE);
        assertEquals(1, emotes.size());
        assertEquals(emoteCosmetic, emotes.get(0));

        List<CosmeticDefinition> gliders = catalog.findByType(CosmeticType.GLIDER);
        assertTrue(gliders.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenFindByTypeWithNull() {
        catalog.add(skinCosmetic);
        List<CosmeticDefinition> result = catalog.findByType(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindAllCosmetics() {
        catalog.add(skinCosmetic);
        catalog.add(emoteCosmetic);

        List<CosmeticDefinition> all = catalog.findAll();
        assertEquals(2, all.size());
        assertTrue(all.contains(skinCosmetic));
        assertTrue(all.contains(emoteCosmetic));
    }

    @Test
    void shouldReturnImmutableListFromFindAll() {
        catalog.add(skinCosmetic);
        List<CosmeticDefinition> all = catalog.findAll();

        assertThrows(UnsupportedOperationException.class, () -> 
            all.add(emoteCosmetic)
        );
    }
}