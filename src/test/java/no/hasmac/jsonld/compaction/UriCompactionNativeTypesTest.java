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

        ActiveContext activeContext = new ActiveContext(null, null, new JsonLdOptions())
                .newContext()
                .create(context, null);

        JsonObject value = Json.createObjectBuilder()
                .add("@value", JsonProvider.instance().createValue(18476.0))
                .build();

        String compacted = activeContext.uriCompaction()
                .value(value)
                .vocab(true)
                .compact("http://example.org/z");

        assertEquals("z", compacted);
    }
}
