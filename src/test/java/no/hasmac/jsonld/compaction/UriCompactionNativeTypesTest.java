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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class UriCompactionNativeTypesTest {

    @Test
    void shouldPreferTermForNativeTypedValue1() throws JsonLdError {
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
	void shouldPreferTermForNativeTypedValue2() throws JsonLdError {
		// Build @context equivalent to the user frame
		JsonObject ctx = Json.createObjectBuilder()
				.add("xsd", "http://www.w3.org/2001/XMLSchema#")
				.add("Notification", Json.createObjectBuilder().add("@id", "Notification"))
				.add("ex", "http://example.org/")
				.add("@vocab", "http://example.org/")
				.add("alert_id", Json.createObjectBuilder().add("@id", "alert_id"))
				.add("x", Json.createObjectBuilder().add("@type", "xsd:double").add("@id", "x"))
				.add("y", Json.createObjectBuilder().add("@type", "xsd:double").add("@id", "y"))
				.add("z", Json.createObjectBuilder().add("@type", "xsd:double").add("@id", "z"))
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
	void shouldPreferTermForNativeTypedValue3() throws JsonLdError {
		// Build @context equivalent to the user frame
		JsonObject ctx = Json.createObjectBuilder()
				.add("xsd", "http://www.w3.org/2001/XMLSchema#")
				.add("Notification", Json.createObjectBuilder().add("@id", "Notification"))
				.add("ex", "http://example.org/")
				.add("@vocab", "http://example.org/")
				.add("alert_id", Json.createObjectBuilder().add("@id", "alert_id"))
				.add("x", Json.createObjectBuilder().add("@type", "xsd:boolean").add("@id", "x"))
				.add("y", Json.createObjectBuilder().add("@type", "xsd:boolean").add("@id", "y"))
				.add("z", Json.createObjectBuilder().add("@type", "xsd:boolean").add("@id", "z"))
				.build();

		ActiveContext activeContext = new ActiveContext(new JsonLdOptions())
				.newContext()
				.create(ctx, null);

		activeContext.getOptions().setUseNativeTypes(true);

		// Value object produced with useNativeTypes=true: only @value present
		JsonObject numericValue = JsonProvider.instance().createObjectBuilder()
				.add(Keywords.VALUE, JsonProvider.instance().createValue("true"))
				.build();

		String compacted = activeContext.uriCompaction()
				.value(numericValue)
				.vocab(true)
				.compact("http://example.org/z");

		assertEquals("z", compacted);
	}

	@Test
	void shouldPreferTermForNativeTypedValue4() throws JsonLdError {
		// Build @context equivalent to the user frame
		JsonObject ctx = Json.createObjectBuilder()
				.add("xsd", "http://www.w3.org/2001/XMLSchema#")
				.add("Notification", Json.createObjectBuilder().add("@id", "Notification"))
				.add("ex", "http://example.org/")
				.add("@vocab", "http://example.org/")
				.add("alert_id", Json.createObjectBuilder().add("@id", "alert_id"))
				.add("x", Json.createObjectBuilder().add("@type", "xsd:boolean").add("@id", "x"))
				.add("y", Json.createObjectBuilder().add("@type", "xsd:boolean").add("@id", "y"))
				.add("z", Json.createObjectBuilder().add("@type", "xsd:boolean").add("@id", "z"))
				.build();

		ActiveContext activeContext = new ActiveContext(new JsonLdOptions())
				.newContext()
				.create(ctx, null);

		activeContext.getOptions().setUseNativeTypes(true);

		// Value object produced with useNativeTypes=true: only @value present
		JsonObject numericValue = JsonProvider.instance().createObjectBuilder()
				.add(Keywords.VALUE, JsonProvider.instance().createValue("not-boolean-value"))
				.build();

		String compacted = activeContext.uriCompaction()
				.value(numericValue)
				.vocab(true)
				.compact("http://example.org/z");

		assertNotEquals("z", compacted);
	}


	@Test
	void shouldPreferTermForNativeTypedValue5() throws JsonLdError {
		// Build @context equivalent to the user frame
		JsonObject ctx = Json.createObjectBuilder()
				.add("xsd", "http://www.w3.org/2001/XMLSchema#")
				.add("Notification", Json.createObjectBuilder().add("@id", "Notification"))
				.add("ex", "http://example.org/")
				.add("@vocab", "http://example.org/")
				.add("alert_id", Json.createObjectBuilder().add("@id", "alert_id"))
				.add("x", Json.createObjectBuilder().add("@type", "xsd:double").add("@id", "x"))
				.add("y", Json.createObjectBuilder().add("@type", "xsd:double").add("@id", "y"))
				.add("z", Json.createObjectBuilder().add("@type", "xsd:double").add("@id", "z"))
				.build();

		ActiveContext activeContext = new ActiveContext(new JsonLdOptions())
				.newContext()
				.create(ctx, null);

		activeContext.getOptions().setUseNativeTypes(true);

		// Value object produced with useNativeTypes=true: only @value present
		JsonObject numericValue = JsonProvider.instance().createObjectBuilder()
				.add(Keywords.VALUE, JsonProvider.instance().createValue("a"))
				.build();

		String compacted = activeContext.uriCompaction()
				.value(numericValue)
				.vocab(true)
				.compact("http://example.org/z");

		assertNotEquals("z", compacted);
	}


	@Test
	void shouldPreferTermForNativeTypedValue6() throws JsonLdError {
		// Build @context equivalent to the user frame
		JsonObject ctx = Json.createObjectBuilder()
				.add("xsd", "http://www.w3.org/2001/XMLSchema#")
				.add("Notification", Json.createObjectBuilder().add("@id", "Notification"))
				.add("ex", "http://example.org/")
				.add("@vocab", "http://example.org/")
				.add("alert_id", Json.createObjectBuilder().add("@id", "alert_id"))
				.add("x", Json.createObjectBuilder().add("@type", "xsd:double").add("@id", "x"))
				.add("y", Json.createObjectBuilder().add("@type", "xsd:double").add("@id", "y"))
				.add("z", Json.createObjectBuilder().add("@type", "xsd:double").add("@id", "z"))
				.build();

		ActiveContext activeContext = new ActiveContext(new JsonLdOptions())
				.newContext()
				.create(ctx, null);

		activeContext.getOptions().setUseNativeTypes(true);

		// Value object produced with useNativeTypes=true: only @value present
		JsonObject numericValue = JsonProvider.instance().createObjectBuilder()
				.add(Keywords.VALUE, JsonProvider.instance().createValue("true"))
				.build();

		String compacted = activeContext.uriCompaction()
				.value(numericValue)
				.vocab(true)
				.compact("http://example.org/z");

		assertNotEquals("z", compacted);
	}



}
