package no.hasmac.jsonld.compaction;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import no.hasmac.jsonld.JsonLdError;
import no.hasmac.jsonld.JsonLdOptions;
import no.hasmac.jsonld.context.ActiveContext;
import no.hasmac.jsonld.json.JsonProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UriCompactionNativeTypesTest {

    /*
     * Edge cases covered:
     * 1. Direct native number with explicit type mapping prefers the scoped term.
     * 2. Native number inside an @list container still resolves to the typed term.
     * 3. Native number compaction respects default language and direction settings.
     * 4. Native number with an @index container selects the correct term alias.
     * 5. Native number with an @set container is compacted using the typed term.
     * 6. Native number with combined @set and @index containers keeps the typed alias.
     * 7. Native number lists preserved via @preserve still resolve to the typed alias.
     * 8. Native number lists with multiple entries consistently use the typed alias.
     * 9. Native number that is preserved via @preserve unwraps and keeps the typed term.
     * 10. Native number on a value object that also carries an @index attribute retains the term.
     * 11. Native number when multiple terms share the IRI prefers the typed definition.
     * 12. Native number prefers the typed term even when CURIE compaction is possible.
     */

    private static ActiveContext createActiveContext(JsonObject context) throws JsonLdError {
        return new ActiveContext(null, null, new JsonLdOptions())
                .newContext()
                .create(context, null);
    }

    @Test
    void selectsTermForNativeNumberWithTypeMapping() throws JsonLdError {
        JsonObject context = Json.createObjectBuilder()
                .add("xsd", "http://www.w3.org/2001/XMLSchema#")
                .add("ex", "http://example.org/")
                .add("@vocab", "http://example.org/")
                .add("alert_id", Json.createObjectBuilder().add("@id", "alert_id"))
                .add("x", Json.createObjectBuilder().add("@id", "x").add("@type", "xsd:float"))
                .add("y", Json.createObjectBuilder().add("@id", "y").add("@type", "xsd:float"))
                .add("z", Json.createObjectBuilder().add("@id", "z").add("@type", "xsd:float"))
                .build();

        ActiveContext activeContext = createActiveContext(context);

        JsonObject value = Json.createObjectBuilder()
                .add("@value", JsonProvider.instance().createValue(18476.0))
                .build();

        String compacted = activeContext.uriCompaction()
                .value(value)
                .vocab(true)
                .compact("http://example.org/z");

        assertEquals("z", compacted);
    }

    @Test
    void selectsTermForNativeNumberInListContainer() throws JsonLdError {
        JsonObject context = Json.createObjectBuilder()
                .add("xsd", "http://www.w3.org/2001/XMLSchema#")
                .add("ex", "http://example.org/")
                .add("@vocab", "http://example.org/")
                .add("measurements", Json.createObjectBuilder()
                        .add("@id", "ex:measurement")
                        .add("@type", "xsd:double")
                        .add("@container", Json.createArrayBuilder().add("@list")))
                .build();

        ActiveContext activeContext = createActiveContext(context);

        JsonObject value = Json.createObjectBuilder()
                .add("@list", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("@value", JsonProvider.instance().createValue(98.6))))
                .build();

        String compacted = activeContext.uriCompaction()
                .value(value)
                .vocab(true)
                .compact("http://example.org/measurement");

        assertEquals("measurements", compacted);
    }

    @Test
    void selectsTermForNativeNumberWithDefaultLanguageAndDirection() throws JsonLdError {
        JsonObject context = Json.createObjectBuilder()
                .add("@language", "en")
                .add("@direction", "ltr")
                .add("xsd", "http://www.w3.org/2001/XMLSchema#")
                .add("@vocab", "http://example.org/")
                .add("count", Json.createObjectBuilder()
                        .add("@id", "http://example.org/count")
                        .add("@type", "xsd:integer"))
                .build();

        ActiveContext activeContext = createActiveContext(context);

        JsonObject value = Json.createObjectBuilder()
                .add("@value", JsonProvider.instance().createValue(5))
                .build();

        String compacted = activeContext.uriCompaction()
                .value(value)
                .vocab(true)
                .compact("http://example.org/count");

        assertEquals("count", compacted);
    }

    @Test
    void selectsTermForNativeNumberWithIndexContainer() throws JsonLdError {
        JsonObject context = Json.createObjectBuilder()
                .add("xsd", "http://www.w3.org/2001/XMLSchema#")
                .add("ex", "http://example.org/")
                .add("@vocab", "http://example.org/")
                .add("metric", Json.createObjectBuilder()
                        .add("@id", "ex:metric")
                        .add("@type", "xsd:double")
                        .add("@container", Json.createArrayBuilder().add("@set").add("@index")))
                .build();

        ActiveContext activeContext = createActiveContext(context);

        JsonObject value = Json.createObjectBuilder()
                .add("@value", JsonProvider.instance().createValue(9.81))
                .add("@index", "earth")
                .build();

        String compacted = activeContext.uriCompaction()
                .value(value)
                .vocab(true)
                .compact("http://example.org/metric");

        assertEquals("metric", compacted);
    }

    @Test
    void selectsTermForNativeNumberWithSetContainer() throws JsonLdError {
        JsonObject context = Json.createObjectBuilder()
                .add("xsd", "http://www.w3.org/2001/XMLSchema#")
                .add("ex", "http://example.org/")
                .add("@vocab", "http://example.org/")
                .add("scores", Json.createObjectBuilder()
                        .add("@id", "ex:score")
                        .add("@type", "xsd:integer")
                        .add("@container", "@set"))
                .build();

        ActiveContext activeContext = createActiveContext(context);

        JsonObject value = Json.createObjectBuilder()
                .add("@value", JsonProvider.instance().createValue(42))
                .build();

        String compacted = activeContext.uriCompaction()
                .value(value)
                .vocab(true)
                .compact("http://example.org/score");

        assertEquals("scores", compacted);
    }

    @Test
    void selectsTermForNativeNumberWithSetAndIndexContainers() throws JsonLdError {
        JsonObject context = Json.createObjectBuilder()
                .add("xsd", "http://www.w3.org/2001/XMLSchema#")
                .add("ex", "http://example.org/")
                .add("@vocab", "http://example.org/")
                .add("indexedScores", Json.createObjectBuilder()
                        .add("@id", "ex:score")
                        .add("@type", "xsd:integer")
                        .add("@container", Json.createArrayBuilder().add("@set").add("@index")))
                .build();

        ActiveContext activeContext = createActiveContext(context);

        JsonObject value = Json.createObjectBuilder()
                .add("@value", JsonProvider.instance().createValue(9001))
                .add("@index", "player-1")
                .build();

        String compacted = activeContext.uriCompaction()
                .value(value)
                .vocab(true)
                .compact("http://example.org/score");

        assertEquals("indexedScores", compacted);
    }

    @Test
    void selectsTermForNativeNumberListWithinPreserve() throws JsonLdError {
        JsonObject context = Json.createObjectBuilder()
                .add("xsd", "http://www.w3.org/2001/XMLSchema#")
                .add("ex", "http://example.org/")
                .add("@vocab", "http://example.org/")
                .add("readings", Json.createObjectBuilder()
                        .add("@id", "ex:reading")
                        .add("@type", "xsd:double")
                        .add("@container", Json.createArrayBuilder().add("@list")))
                .build();

        ActiveContext activeContext = createActiveContext(context);

        JsonObject listValue = Json.createObjectBuilder()
                .add("@list", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("@value", JsonProvider.instance().createValue(12.5))))
                .build();

        JsonObject value = Json.createObjectBuilder()
                .add("@preserve", Json.createArrayBuilder().add(listValue))
                .build();

        String compacted = activeContext.uriCompaction()
                .value(value)
                .vocab(true)
                .compact("http://example.org/reading");

        assertEquals("readings", compacted);
    }

    @Test
    void selectsTermForNativeNumberListWithMultipleValues() throws JsonLdError {
        JsonObject context = Json.createObjectBuilder()
                .add("xsd", "http://www.w3.org/2001/XMLSchema#")
                .add("ex", "http://example.org/")
                .add("@vocab", "http://example.org/")
                .add("temperatures", Json.createObjectBuilder()
                        .add("@id", "ex:temperature")
                        .add("@type", "xsd:double")
                        .add("@container", Json.createArrayBuilder().add("@list")))
                .build();

        ActiveContext activeContext = createActiveContext(context);

        JsonObject value = Json.createObjectBuilder()
                .add("@list", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("@value", JsonProvider.instance().createValue(19.5)))
                        .add(Json.createObjectBuilder()
                                .add("@value", JsonProvider.instance().createValue(21.0))))
                .build();

        String compacted = activeContext.uriCompaction()
                .value(value)
                .vocab(true)
                .compact("http://example.org/temperature");

        assertEquals("temperatures", compacted);
    }

    @Test
    void selectsTermForPreservedNativeNumber() throws JsonLdError {
        JsonObject context = Json.createObjectBuilder()
                .add("xsd", "http://www.w3.org/2001/XMLSchema#")
                .add("ex", "http://example.org/")
                .add("@vocab", "http://example.org/")
                .add("count", Json.createObjectBuilder()
                        .add("@id", "ex:count")
                        .add("@type", "xsd:integer"))
                .build();

        ActiveContext activeContext = createActiveContext(context);

        JsonObject value = Json.createObjectBuilder()
                .add("@preserve", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("@value", JsonProvider.instance().createValue(3))))
                .build();

        String compacted = activeContext.uriCompaction()
                .value(value)
                .vocab(true)
                .compact("http://example.org/count");

        assertEquals("count", compacted);
    }

    @Test
    void selectsTermForNativeNumberWithIndexOnValueObject() throws JsonLdError {
        JsonObject context = Json.createObjectBuilder()
                .add("xsd", "http://www.w3.org/2001/XMLSchema#")
                .add("ex", "http://example.org/")
                .add("@vocab", "http://example.org/")
                .add("rating", Json.createObjectBuilder()
                        .add("@id", "ex:rating")
                        .add("@type", "xsd:double")
                        .add("@container", Json.createArrayBuilder().add("@index")))
                .build();

        ActiveContext activeContext = createActiveContext(context);

        JsonObject value = Json.createObjectBuilder()
                .add("@value", JsonProvider.instance().createValue(4.5))
                .add("@index", "expert")
                .build();

        String compacted = activeContext.uriCompaction()
                .value(value)
                .vocab(true)
                .compact("http://example.org/rating");

        assertEquals("rating", compacted);
    }

    @Test
    void selectsTypedTermWhenMultipleDefinitionsShareIri() throws JsonLdError {
        JsonObject context = Json.createObjectBuilder()
                .add("xsd", "http://www.w3.org/2001/XMLSchema#")
                .add("ex", "http://example.org/")
                .add("@vocab", "http://example.org/")
                .add("reading", Json.createObjectBuilder()
                        .add("@id", "ex:reading")
                        .add("@type", "xsd:double"))
                .add("readingLabel", Json.createObjectBuilder()
                        .add("@id", "ex:reading"))
                .build();

        ActiveContext activeContext = createActiveContext(context);

        JsonObject value = Json.createObjectBuilder()
                .add("@value", JsonProvider.instance().createValue(8.5))
                .build();

        String compacted = activeContext.uriCompaction()
                .value(value)
                .vocab(true)
                .compact("http://example.org/reading");

        assertEquals("reading", compacted);
    }

    @Test
    void selectsTypedTermOverCurieCompactionCandidate() throws JsonLdError {
        JsonObject context = Json.createObjectBuilder()
                .add("xsd", "http://www.w3.org/2001/XMLSchema#")
                .add("ex", "http://example.org/")
                .add("@vocab", "http://example.org/vocab/")
                .add("reading", Json.createObjectBuilder()
                        .add("@id", "ex:reading")
                        .add("@type", "xsd:double"))
                .build();

        ActiveContext activeContext = createActiveContext(context);

        JsonObject value = Json.createObjectBuilder()
                .add("@value", JsonProvider.instance().createValue(5.5))
                .build();

        String compacted = activeContext.uriCompaction()
                .value(value)
                .vocab(true)
                .compact("http://example.org/reading");

        assertEquals("reading", compacted);
    }
}
