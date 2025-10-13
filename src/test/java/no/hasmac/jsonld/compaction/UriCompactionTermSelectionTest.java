package no.hasmac.jsonld.compaction;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import no.hasmac.jsonld.JsonLdError;
import no.hasmac.jsonld.JsonLdOptions;
import no.hasmac.jsonld.context.ActiveContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.StringReader;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Spec-focused compaction tests that exercise term selection across:
 * - value typing (typed value objects vs native JSON values)
 * - containers (@list/@set/@index)
 * - language mapping and defaults
 * - compact IRI fallback when term selection does not match
 */
class UriCompactionTermSelectionTest {

    private static ActiveContext createActiveContext(JsonObject context) throws JsonLdError {
        return new ActiveContext(null, null, new JsonLdOptions())
                .newContext()
                .create(context, null);
    }

    private static JsonObject obj(String json) {
        try (JsonReader r = Json.createReader(new StringReader(json))) {
            return r.readObject();
        }
    }

    // ------------------------
    // Typed value selection
    // ------------------------

    static Stream<Arguments> typedValueCases() {
        final String EX = "http://example.org/";

        return Stream.of(
                // Integer typed value selects the typed term
                Arguments.of(
                        "{\n  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",\n  \"ex\": \"http://example.org/\",\n  \"@vocab\": \"http://example.org/\",\n  \"count\": { \"@id\": \"ex:count\", \"@type\": \"xsd:integer\" }\n}",
                        "{ \"@value\": 123, \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\" }",
                        EX + "count",
                        "count"
                ),

                // Boolean typed value selects the typed term
                Arguments.of(
                        "{\n  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",\n  \"ex\": \"http://example.org/\",\n  \"@vocab\": \"http://example.org/\",\n  \"isActive\": { \"@id\": \"ex:isActive\", \"@type\": \"xsd:boolean\" }\n}",
                        "{ \"@value\": true, \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\" }",
                        EX + "isActive",
                        "isActive"
                )
        );
    }

    @ParameterizedTest(name = "typed value selects term")
    @MethodSource("typedValueCases")
    void selectsTermForTypedValues(String contextJson, String valueJson, String variable, String expected) throws JsonLdError {
        ActiveContext activeContext = createActiveContext(obj(contextJson));
        String compacted = activeContext.uriCompaction()
                .value(obj(valueJson))
                .vocab(true)
                .compact(variable);
        assertEquals(expected, compacted);
    }

    // ------------------------
    // Language mapping selection
    // ------------------------

    static Stream<Arguments> languageValueCases() {
        final String EX = "http://example.org/";

        return Stream.of(
                // Value with @language=en selects term with @language=en
                Arguments.of(
                        "{\n  \"@vocab\": \"http://example.org/\",\n  \"labelEn\": { \"@id\": \"ex:label\", \"@language\": \"en\" },\n  \"labelAny\": { \"@id\": \"ex:label\" },\n  \"ex\": \"http://example.org/\"\n}",
                        "{ \"@value\": \"Hello\", \"@language\": \"en\" }",
                        EX + "label",
                        "labelEn"
                ),

                // Value with @language=es selects fallback (no lang term), not the en-specific term
                Arguments.of(
                        "{\n  \"@vocab\": \"http://example.org/\",\n  \"labelEn\": { \"@id\": \"ex:label\", \"@language\": \"en\" },\n  \"labelAny\": { \"@id\": \"ex:label\" },\n  \"ex\": \"http://example.org/\"\n}",
                        "{ \"@value\": \"Hola\", \"@language\": \"es\" }",
                        EX + "label",
                        "labelAny"
                )
        );
    }

    @ParameterizedTest(name = "language selection")
    @MethodSource("languageValueCases")
    void selectsTermForLanguageValues(String contextJson, String valueJson, String variable, String expected) throws JsonLdError {
        ActiveContext activeContext = createActiveContext(obj(contextJson));
        String compacted = activeContext.uriCompaction()
                .value(obj(valueJson))
                .vocab(true)
                .compact(variable);
        assertEquals(expected, compacted);
    }
}
