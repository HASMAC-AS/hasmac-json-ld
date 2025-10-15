/*
 * Copyright 2025 HASMAC.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package no.hasmac.jsonld.framing;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import no.hasmac.jsonld.JsonLd;
import no.hasmac.jsonld.JsonLdError;
import no.hasmac.jsonld.JsonLdOptions;
import no.hasmac.jsonld.document.JsonDocument;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FramingNativeTypesTest {

    private static JsonObject baseContextWithVocabAndXsd() {
        return Json.createObjectBuilder()
                .add("xsd", "http://www.w3.org/2001/XMLSchema#")
                .add("ex", "http://example.org/")
                .add("@vocab", "http://example.org/")
                .build();
    }

    private static JsonObject frameWithContext(JsonObject ctx) {
        return Json.createObjectBuilder()
                .add("@context", ctx)
                .build();
    }

    private static JsonLdOptions nativeTypesOptions() {
        JsonLdOptions options = new JsonLdOptions();
        options.setUseNativeTypes(true);
        return options;
    }

    private static Set<String> keysExcluding(JsonObject o, String... excluded) {
        java.util.Set<String> s = new java.util.HashSet<>(o.keySet());
        for (String e : excluded) s.remove(e);
        return s;
    }

    @Test
    void shouldPreferTermInFramingForBooleanNativeJsonValue() throws JsonLdError {
        JsonObject ctx = Json.createObjectBuilder(baseContextWithVocabAndXsd())
                .add("z", Json.createObjectBuilder().add("@id", "z").add("@type", "xsd:boolean"))
                .build();

        JsonObject input = Json.createObjectBuilder()
                .add("@context", ctx)
                .add("@id", "http://example.org/id1")
                .add("z", JsonValue.TRUE)
                .build();

        JsonObject frame = frameWithContext(ctx);

        JsonObject framed = JsonLd.frame(JsonDocument.of(input), JsonDocument.of(frame))
                .options(nativeTypesOptions())
                .get();

        // Expect property key 'z' selected for boolean native value
        assertTrue(framed.containsKey("z"));
    }

    @Test
    void shouldNotPreferTermInFramingForBooleanIncompatibleString() throws JsonLdError {
        JsonObject ctx = Json.createObjectBuilder(baseContextWithVocabAndXsd())
                .add("z", Json.createObjectBuilder().add("@id", "z").add("@type", "xsd:boolean"))
                .build();

        JsonObject input = Json.createObjectBuilder()
                .add("@context", ctx)
                .add("@id", "http://example.org/id2")
                .add("z", "not-boolean-value")
                .build();

        JsonObject frame = frameWithContext(ctx);

        JsonObject framed = JsonLd.frame(JsonDocument.of(input), JsonDocument.of(frame))
                .options(nativeTypesOptions())
                .get();

        // Expect that 'z' is not chosen as a term for incompatible value
        assertFalse(framed.containsKey("z"));
        // And that the property was still kept (likely compact IRI or IRI form)
        Set<String> otherKeys = keysExcluding(framed, "@context", "@id");
        assertEquals(1, otherKeys.size());
        String onlyKey = otherKeys.iterator().next();
        assertFalse("z".equals(onlyKey));
    }

    @Test
    void shouldPreferTermInFramingForBooleanLikeStringValue() throws JsonLdError {
        JsonObject ctx = Json.createObjectBuilder(baseContextWithVocabAndXsd())
                .add("z", Json.createObjectBuilder().add("@id", "z").add("@type", "xsd:boolean"))
                .build();

        JsonObject input = Json.createObjectBuilder()
                .add("@context", ctx)
                .add("@id", "http://example.org/id5")
                .add("z", "true")
                .build();

        JsonObject frame = frameWithContext(ctx);

        JsonObject framed = JsonLd.frame(JsonDocument.of(input), JsonDocument.of(frame))
                .options(nativeTypesOptions())
                .get();

        assertTrue(framed.containsKey("z"));
    }

    @Test
    void shouldPreferTermInFramingForDoubleNativeJsonNumber() throws JsonLdError {
        JsonObject ctx = Json.createObjectBuilder(baseContextWithVocabAndXsd())
                .add("z", Json.createObjectBuilder().add("@id", "z").add("@type", "xsd:double"))
                .build();

        JsonObject input = Json.createObjectBuilder()
                .add("@context", ctx)
                .add("@id", "http://example.org/id3")
                .add("z", 18476.0)
                .build();

        JsonObject frame = frameWithContext(ctx);

        JsonObject framed = JsonLd.frame(JsonDocument.of(input), JsonDocument.of(frame))
                .options(nativeTypesOptions())
                .get();

        assertTrue(framed.containsKey("z"));
    }

    @Test
    void shouldNotPreferTermInFramingForDoubleIncompatibleString() throws JsonLdError {
        JsonObject ctx = Json.createObjectBuilder(baseContextWithVocabAndXsd())
                .add("z", Json.createObjectBuilder().add("@id", "z").add("@type", "xsd:double"))
                .build();

        JsonObject input = Json.createObjectBuilder()
                .add("@context", ctx)
                .add("@id", "http://example.org/id4")
                .add("z", "a")
                .build();

        JsonObject frame = frameWithContext(ctx);

        JsonObject framed = JsonLd.frame(JsonDocument.of(input), JsonDocument.of(frame))
                .options(nativeTypesOptions())
                .get();

        assertFalse(framed.containsKey("z"));
        Set<String> otherKeys = keysExcluding(framed, "@context", "@id");
        assertEquals(1, otherKeys.size());
        String onlyKey = otherKeys.iterator().next();
        assertFalse("z".equals(onlyKey));
    }
}
