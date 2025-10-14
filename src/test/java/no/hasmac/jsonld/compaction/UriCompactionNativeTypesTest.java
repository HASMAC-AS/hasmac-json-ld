/*
 * Copyright 2025 HASMAC.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package no.hasmac.jsonld.compaction;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import no.hasmac.jsonld.JsonLdError;
import no.hasmac.jsonld.JsonLdOptions;
import no.hasmac.jsonld.context.ActiveContext;
import no.hasmac.jsonld.json.JsonProvider;
import no.hasmac.jsonld.lang.Keywords;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UriCompactionNativeTypesTest {

    @Test
    void shouldPreferTermForNativeTypedValue() throws JsonLdError {
        // Build @context equivalent to the user frame
        JsonObject ctx = Json.createObjectBuilder()
                .add("xsd", "http://www.w3.org/2001/XMLSchema#")
                .add("Notification", Json.createObjectBuilder().add("@id", "Notification"))
                .add("ex", "http://example.org/")
                .add("@vocab", "http://example.org/")
                .add("alert_id", Json.createObjectBuilder().add("@id", "alert_id"))
                .add("x", Json.createObjectBuilder().add("@type", "xsd:float").add("@id", "x"))
                .add("y", Json.createObjectBuilder().add("@type", "xsd:float").add("@id", "y"))
                .add("z", Json.createObjectBuilder().add("@type", "xsd:float").add("@id", "z"))
                .build();

        ActiveContext activeContext = new ActiveContext(new JsonLdOptions())
                .newContext()
                .create(ctx, null);

		activeContext.getOptions().setUseNativeTypes(true);

        // Value object produced with useNativeTypes=true: only @value present
        JsonObject numericValue = JsonProvider.instance().createObjectBuilder()
                .add(Keywords.VALUE, JsonProvider.instance().createValue(18476.0))
                .build();

        String compacted = activeContext.uriCompaction()
                .value(numericValue)
                .vocab(true)
                .compact("http://example.org/z");

        assertEquals("z", compacted);
    }

    @Test
    void shouldNotUseIdTypedTermForLiteral() throws JsonLdError {
        // Context where ex:contains expects @id values
        JsonObject ctx = Json.createObjectBuilder()
                .add("@vocab", "http://example.org/")
                .add("ex", "http://example.org/vocab#")
                .add("ex:contains", Json.createObjectBuilder().add("@type", "@id"))
                .build();

        ActiveContext activeContext = new ActiveContext(new JsonLdOptions())
                .newContext()
                .create(ctx, null);

        // Literal string value (not an IRI)
        JsonObject literalValue = JsonProvider.instance().createObjectBuilder()
                .add(Keywords.VALUE, JsonProvider.instance().createValue("this-is-not-an-IRI"))
                .build();

        String compacted = activeContext.uriCompaction()
                .value(literalValue)
                .vocab(true)
                .compact("http://example.org/vocab#contains");

        // Must not pick term alias (ex:contains) for literal; use full IRI
        assertEquals("http://example.org/vocab#contains", compacted);
    }
}
