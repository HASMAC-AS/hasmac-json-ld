package no.hasmac.jsonld.compaction;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import no.hasmac.jsonld.JsonLd;
import no.hasmac.jsonld.JsonLdError;
import no.hasmac.jsonld.JsonLdOptions;
import no.hasmac.jsonld.document.JsonDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.StringReader;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Whole-document compaction tests mirroring UriCompactionNativeTypesTest,
 * structured for easy reproduction in the JSON-LD Playground.
 */
class UriCompactionNativeTypesDocumentTest {

    private static JsonObject obj(String json) {
        try (JsonReader r = Json.createReader(new StringReader(json))) {
            return r.readObject();
        }
    }

    private static String lines(String... l) {
        return String.join("\n", l);
    }

    static Stream<Arguments> numberContainerCases() {
        final String EX = "http://example.org/";

        return Stream.of(
                Arguments.of(
                        "list_container_number",
                        // Context
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
                        // Input document
                        lines(
                                "{",
                                "  \"" + EX + "measurement\": { \"@list\": [ { \"@value\": 98.6 } ] }",
                                "}"
                        ),
                        // Expected compacted key
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
                                "  \"" + EX + "score\": 42",
                                "}"
                        ),
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
                                "  \"" + EX + "score\": { \"@value\": 9001, \"@index\": \"player-1\" }",
                                "}"
                        ),
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
                                "  \"" + EX + "rating\": { \"@value\": 4.5, \"@index\": \"expert\" }",
                                "}"
                        ),
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
                                "  \"" + EX + "reading\": { \"@preserve\": [ { \"@list\": [ { \"@value\": 12.5 } ] } ] }",
                                "}"
                        ),
                        "reading"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("numberContainerCases")
    void compactsNativeNumberWithContainers_doc(
            String name,
            String contextJson,
            String inputJson,
            String expectedKey
    ) throws JsonLdError {

        JsonObject compacted = JsonLd
                .compact(JsonDocument.of(new StringReader(inputJson)), JsonDocument.of(new StringReader(contextJson)))
                .get();

        String actualKey = compacted.keySet().stream()
                .filter(k -> !"@context".equals(k))
                .findFirst()
                .orElse(null);

        System.out.println("TEST: " + name);
        System.out.println("Context:\n" + contextJson);
        System.out.println("Input:\n" + inputJson);
        System.out.println("Output:\n" + compacted);

        assertEquals(expectedKey, actualKey);
    }

    @Test
    void nativeNumber_withTypeMapping_doc() throws JsonLdError {
        String contextJson = lines(
                "{",
                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                "  \"ex\": \"http://example.org/\",",
                "  \"@vocab\": \"http://example.org/\",",
                "  \"z\": { \"@id\": \"z\", \"@type\": \"xsd:float\" }",
                "}"
        );

        String inputJson = lines(
                "{",
                "  \"http://example.org/z\": { \"@value\": 18476.0 }",
                "}"
        );

        JsonObject compacted = JsonLd
                .compact(JsonDocument.of(new StringReader(inputJson)), JsonDocument.of(new StringReader(contextJson)))
                .get();

        String actualKey = compacted.keySet().stream()
                .filter(k -> !"@context".equals(k))
                .findFirst()
                .orElse(null);

        assertEquals("ex:z", actualKey);
    }

    @Test
    void nativeNumber_withDefaultLanguage_doc() throws JsonLdError {
        String contextJson = lines(
                "{",
                "  \"@language\": \"en\",",
                "  \"@direction\": \"ltr\",",
                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                "  \"@vocab\": \"http://example.org/\",",
                "  \"count\": { \"@id\": \"http://example.org/count\", \"@type\": \"xsd:integer\" }",
                "}"
        );

        String inputJson = lines(
                "{",
                "  \"http://example.org/count\": { \"@value\": 5 }",
                "}"
        );

        JsonObject compacted = JsonLd
                .compact(JsonDocument.of(new StringReader(inputJson)), JsonDocument.of(new StringReader(contextJson)))
                .get();

        String actualKey = compacted.keySet().stream()
                .filter(k -> !"@context".equals(k))
                .findFirst()
                .orElse(null);

        assertEquals("http://example.org/count", actualKey);
    }

    @Test
    void nativeBoolean_typedAndContainers_doc() throws JsonLdError {
        // typed boolean
        String ctx1 = lines(
                "{",
                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                "  \"ex\": \"http://example.org/\",",
                "  \"@vocab\": \"http://example.org/\",",
                "  \"isActive\": { \"@id\": \"ex:isActive\", \"@type\": \"xsd:boolean\" }",
                "}"
        );
        String in1 = lines(
                "{",
                "  \"http://example.org/isActive\": { \"@value\": true }",
                "}"
        );
        JsonObject out1 = JsonLd.compact(JsonDocument.of(new StringReader(in1)), JsonDocument.of(new StringReader(ctx1))).get();
        String key1 = out1.keySet().stream().filter(k -> !"@context".equals(k)).findFirst().orElse(null);
        assertEquals("ex:isActive", key1);

        // list container
        String ctx2 = lines(
                "{",
                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                "  \"ex\": \"http://example.org/\",",
                "  \"@vocab\": \"http://example.org/\",",
                "  \"flags\": { \"@id\": \"ex:flag\", \"@type\": \"xsd:boolean\", \"@container\": [\"@list\"] }",
                "}"
        );
        String in2 = lines(
                "{",
                "  \"http://example.org/flag\": { \"@list\": [ { \"@value\": false } ] }",
                "}"
        );
        JsonObject out2 = JsonLd.compact(JsonDocument.of(new StringReader(in2)), JsonDocument.of(new StringReader(ctx2))).get();
        String key2 = out2.keySet().stream().filter(k -> !"@context".equals(k)).findFirst().orElse(null);
        assertEquals("flag", key2);

        // index container on term + @index on value
        String ctx3 = lines(
                "{",
                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                "  \"ex\": \"http://example.org/\",",
                "  \"@vocab\": \"http://example.org/\",",
                "  \"flag\": { \"@id\": \"ex:flag\", \"@type\": \"xsd:boolean\", \"@container\": [\"@index\"] }",
                "}"
        );
        String in3 = lines(
                "{",
                "  \"http://example.org/flag\": { \"@value\": true, \"@index\": \"env\" }",
                "}"
        );
        JsonObject out3 = JsonLd.compact(JsonDocument.of(new StringReader(in3)), JsonDocument.of(new StringReader(ctx3))).get();
        String key3 = out3.keySet().stream().filter(k -> !"@context".equals(k)).findFirst().orElse(null);
        assertEquals("ex:flag", key3);
    }

    @Test
    void multipleDefinitions_sameIri_doc() throws JsonLdError {
        String contextJson = lines(
                "{",
                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                "  \"ex\": \"http://example.org/\",",
                "  \"@vocab\": \"http://example.org/\",",
                "  \"reading\": { \"@id\": \"ex:reading\", \"@type\": \"xsd:double\" },",
                "  \"readingLabel\": { \"@id\": \"ex:reading\" }",
                "}"
        );

        String inputJson = lines(
                "{",
                "  \"http://example.org/reading\": { \"@value\": 8.5 }",
                "}"
        );

        JsonObject compacted = JsonLd
                .compact(JsonDocument.of(new StringReader(inputJson)), JsonDocument.of(new StringReader(contextJson)))
                .get();

        String actualKey = compacted.keySet().stream()
                .filter(k -> !"@context".equals(k))
                .findFirst()
                .orElse(null);

        assertEquals("readingLabel", actualKey);
    }

    @Test
    void compactIri_whenTermNotSelected_doc() throws JsonLdError {
        String contextJson = lines(
                "{",
                "  \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",",
                "  \"ex\": \"http://example.org/\",",
                "  \"@vocab\": \"http://example.org/vocab/\",",
                "  \"reading\": { \"@id\": \"ex:reading\", \"@type\": \"xsd:double\" }",
                "}"
        );

        String inputJson = lines(
                "{",
                "  \"http://example.org/reading\": { \"@value\": 5.5 }",
                "}"
        );

        JsonLdOptions opts = new JsonLdOptions();
        opts.setUseNativeTypes(true);
        JsonObject compacted = JsonLd
                .compact(JsonDocument.of(new StringReader(inputJson)), JsonDocument.of(new StringReader(contextJson)))
                .options(opts)
                .get();

        String actualKey = compacted.keySet().stream()
                .filter(k -> !"@context".equals(k))
                .findFirst()
                .orElse(null);

        assertEquals("reading", actualKey);
    }
}
