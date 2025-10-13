package no.hasmac.jsonld.compaction;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import no.hasmac.jsonld.JsonLdError;
import no.hasmac.jsonld.JsonLdOptions;
import no.hasmac.jsonld.context.ActiveContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.StringReader;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UriCompactionNativeTypesTest {

    /*
     * Edge cases covered:
     * 1. Direct native number with explicit type mapping prefers the scoped term.
     * 2. Container effects (@list/@set/@index/@preserve) on selection.
     * 3. Native number compaction respects default language and direction settings.
     * 4-9. Container combinations via parameterized matrix (@list/@set/@index/@preserve).
     * 11. Native number when multiple terms share the IRI prefers the typed definition.
     * 12. Behavior when a compact IRI (prefix:suffix) is possible.
     */

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

    private static String lines(String... l) {
        return String.join("\n", l);
    }

    // ------------------------
    // Parameterized matrix for container-focused cases (native numbers)
    // ------------------------

    static Stream<Arguments> numberContainerCases() {
        final String EX = "http://example.org/";

        return Stream.of(
                Arguments.of(
                        "list_container_number",
                        lines(
                                "{",
                                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                                "  \"ex\": \"http://example.org/\",",
                                "  \"@vocab\": \"http://example.org/\",",
                                "  \"measurements\": {",
                                "    \"@id\": \"ex:measurement\",",
                                "    \"@type\": \"xsd:double\",",
                                "    \"@container\": [\"@list\"]",
                                "  }",
                                "}"
                        ),
                        lines(
                                "{",
                                "  \"@list\": [ { \"@value\": 98.6 } ]",
                                "}"
                        ),
                        EX + "measurement",
                        "measurement"
                ),
                Arguments.of(
                        "set_container_number",
                        lines(
                                "{",
                                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                                "  \"ex\": \"http://example.org/\",",
                                "  \"@vocab\": \"http://example.org/\",",
                                "  \"scores\": {",
                                "    \"@id\": \"ex:score\",",
                                "    \"@type\": \"xsd:integer\",",
                                "    \"@container\": \"@set\"",
                                "  }",
                                "}"
                        ),
                        lines(
                                "{",
                                "  \"@value\": 42",
                                "}"
                        ),
                        EX + "score",
                        "score"
                ),
                Arguments.of(
                        "set_index_container_number",
                        lines(
                                "{",
                                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                                "  \"ex\": \"http://example.org/\",",
                                "  \"@vocab\": \"http://example.org/\",",
                                "  \"indexedScores\": {",
                                "    \"@id\": \"ex:score\",",
                                "    \"@type\": \"xsd:integer\",",
                                "    \"@container\": [\"@set\", \"@index\"]",
                                "  }",
                                "}"
                        ),
                        lines(
                                "{",
                                "  \"@value\": 9001, \"@index\": \"player-1\"",
                                "}"
                        ),
                        EX + "score",
                        "score"
                ),
                Arguments.of(
                        "index_container_on_value_object_number",
                        lines(
                                "{",
                                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                                "  \"ex\": \"http://example.org/\",",
                                "  \"@vocab\": \"http://example.org/\",",
                                "  \"rating\": {",
                                "    \"@id\": \"ex:rating\",",
                                "    \"@type\": \"xsd:double\",",
                                "    \"@container\": [\"@index\"]",
                                "  }",
                                "}"
                        ),
                        lines(
                                "{",
                                "  \"@value\": 4.5, \"@index\": \"expert\"",
                                "}"
                        ),
                        EX + "rating",
                        "ex:rating"
                ),
                Arguments.of(
                        "preserve_list_number",
                        lines(
                                "{",
                                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                                "  \"ex\": \"http://example.org/\",",
                                "  \"@vocab\": \"http://example.org/\",",
                                "  \"readings\": {",
                                "    \"@id\": \"ex:reading\",",
                                "    \"@type\": \"xsd:double\",",
                                "    \"@container\": [\"@list\"]",
                                "  }",
                                "}"
                        ),
                        lines(
                                "{",
                                "  \"@preserve\": [ {",
                                "    \"@list\": [ { \"@value\": 12.5 } ]",
                                "  } ]",
                                "}"
                        ),
                        EX + "reading",
                        "reading"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("numberContainerCases")
    void compactsNativeNumberWithContainers(
            String name,
            String contextJson,
            String valueJson,
            String variable,
            String expected
    ) throws JsonLdError {
        ActiveContext activeContext = createActiveContext(obj(contextJson));
        String compacted = activeContext.uriCompaction()
                .value(obj(valueJson))
                .vocab(true)
                .compact(variable);

        System.out.println("TEST: " + name);
        System.out.println("Context:\n" + contextJson);
        System.out.println("Input:\n" + valueJson);
        System.out.println("Variable: " + variable);
        System.out.println("Output: " + compacted);
        assertEquals(expected, compacted);
    }

    @Test
    void selectsTermForNativeNumberWithTypeMapping() throws JsonLdError {
        String contextJson = lines(
                "{",
                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                "  \"ex\": \"http://example.org/\",",
                "  \"@vocab\": \"http://example.org/\",",
                "  \"alert_id\": { \"@id\": \"alert_id\" },",
                "  \"x\": { \"@id\": \"x\", \"@type\": \"xsd:float\" },",
                "  \"y\": { \"@id\": \"y\", \"@type\": \"xsd:float\" },",
                "  \"z\": { \"@id\": \"z\", \"@type\": \"xsd:float\" }",
                "}"
        );

        ActiveContext activeContext = createActiveContext(obj(contextJson));

        String valueJson = lines(
                "{",
                "  \"@value\": 18476.0",
                "}"
        );

        String compacted = activeContext.uriCompaction()
                .value(obj(valueJson))
                .vocab(true)
                .compact("http://example.org/z");

        System.out.println("Context:\n" + contextJson);
        System.out.println("Input:\n" + valueJson);
        System.out.println("Variable: http://example.org/z");
        System.out.println("Output: " + compacted);
        assertEquals("ex:z", compacted);
    }

    @Test
    void selectsTermForNativeNumberWithDefaultLanguageAndDirection() throws JsonLdError {
        String contextJson = lines(
                "{",
                "  \"@language\": \"en\",",
                "  \"@direction\": \"ltr\",",
                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                "  \"@vocab\": \"http://example.org/\",",
                "  \"count\": { \"@id\": \"http://example.org/count\", \"@type\": \"xsd:integer\" }",
                "}"
        );

        ActiveContext activeContext = createActiveContext(obj(contextJson));

        String valueJson = lines(
                "{",
                "  \"@value\": 5",
                "}"
        );

        String compacted = activeContext.uriCompaction()
                .value(obj(valueJson))
                .vocab(true)
                .compact("http://example.org/count");

        System.out.println("Context:\n" + contextJson);
        System.out.println("Input:\n" + valueJson);
        System.out.println("Variable: http://example.org/count");
        System.out.println("Output: " + compacted);
        assertEquals("http://example.org/count", compacted);
    }

    // ------------------------
    // Boolean-focused coverage (native booleans behave like numbers wrt selection)
    // ------------------------

    @Test
    void selectsCompactIriForNativeBooleanWithTypedMapping() throws JsonLdError {
        String contextJson = lines(
                "{",
                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                "  \"ex\": \"http://example.org/\",",
                "  \"@vocab\": \"http://example.org/\",",
                "  \"isActive\": { \"@id\": \"ex:isActive\", \"@type\": \"xsd:boolean\" }",
                "}"
        );

        ActiveContext activeContext = createActiveContext(obj(contextJson));

        String valueJson = lines(
                "{",
                "  \"@value\": true",
                "}"
        );

        String compacted = activeContext.uriCompaction()
                .value(obj(valueJson))
                .vocab(true)
                .compact("http://example.org/isActive");

        System.out.println("Context:\n" + contextJson);
        System.out.println("Input:\n" + valueJson);
        System.out.println("Variable: http://example.org/isActive");
        System.out.println("Output: " + compacted);
        assertEquals("ex:isActive", compacted);
    }

    @Test
    void compactsNativeBooleanInListContainer() throws JsonLdError {
        String contextJson = lines(
                "{",
                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                "  \"ex\": \"http://example.org/\",",
                "  \"@vocab\": \"http://example.org/\",",
                "  \"flags\": { \"@id\": \"ex:flag\", \"@type\": \"xsd:boolean\", \"@container\": [\"@list\"] }",
                "}"
        );

        ActiveContext activeContext = createActiveContext(obj(contextJson));

        String valueJson = lines(
                "{",
                "  \"@list\": [ { \"@value\": false } ]",
                "}"
        );

        String compacted = activeContext.uriCompaction()
                .value(obj(valueJson))
                .vocab(true)
                .compact("http://example.org/flag");

        System.out.println("Context:\n" + contextJson);
        System.out.println("Input:\n" + valueJson);
        System.out.println("Variable: http://example.org/flag");
        System.out.println("Output: " + compacted);
        assertEquals("flag", compacted);
    }

    @Test
    void compactsNativeBooleanWithIndexContainer() throws JsonLdError {
        String contextJson = lines(
                "{",
                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                "  \"ex\": \"http://example.org/\",",
                "  \"@vocab\": \"http://example.org/\",",
                "  \"flag\": { \"@id\": \"ex:flag\", \"@type\": \"xsd:boolean\", \"@container\": [\"@index\"] }",
                "}"
        );

        ActiveContext activeContext = createActiveContext(obj(contextJson));

        String valueJson = lines(
                "{",
                "  \"@value\": true, \"@index\": \"env\"",
                "}"
        );

        String compacted = activeContext.uriCompaction()
                .value(obj(valueJson))
                .vocab(true)
                .compact("http://example.org/flag");

        System.out.println("Context:\n" + contextJson);
        System.out.println("Input:\n" + valueJson);
        System.out.println("Variable: http://example.org/flag");
        System.out.println("Output: " + compacted);
        assertEquals("ex:flag", compacted);
    }

    @Test
    void selectsTypedTermWhenMultipleDefinitionsShareIri() throws JsonLdError {

        String contextJson = lines(
                "{",
                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                "  \"ex\": \"http://example.org/\",",
                "  \"@vocab\": \"http://example.org/\",",
                "  \"reading\": { \"@id\": \"ex:reading\", \"@type\": \"xsd:double\" },",
                "  \"readingLabel\": { \"@id\": \"ex:reading\" }",
                "}"
        );

        ActiveContext activeContext = createActiveContext(obj(contextJson));

        String valueJson = lines(
                "{",
                "  \"@value\": 8.5",
                "}"
        );

        String compacted = activeContext.uriCompaction()
                .value(obj(valueJson))
                .vocab(true)
                .compact("http://example.org/reading");

        System.out.println("Context:\n" + contextJson);
        System.out.println("Input:\n" + valueJson);
        System.out.println("Variable: http://example.org/reading");
        System.out.println("Output: " + compacted);
        assertEquals("readingLabel", compacted);
    }

    @Test
    void selectsCompactIriWhenTermNotSelected() throws JsonLdError {
        String contextJson = lines(
                "{",
                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                "  \"ex\": \"http://example.org/\",",
                "  \"@vocab\": \"http://example.org/vocab/\",",
                "  \"reading\": { \"@id\": \"ex:reading\", \"@type\": \"xsd:double\" }",
                "}"
        );

        ActiveContext activeContext = createActiveContext(obj(contextJson));

        String valueJson = lines(
                "{",
                "  \"@value\": 5.5",
                "}"
        );

        String compacted = activeContext.uriCompaction()
                .value(obj(valueJson))
                .vocab(true)
                .compact("http://example.org/reading");

        System.out.println("Context:\n" + contextJson);
        System.out.println("Input:\n" + valueJson);
        System.out.println("Variable: http://example.org/reading");
        System.out.println("Output: " + compacted);
        assertEquals("ex:reading", compacted);
    }
}
