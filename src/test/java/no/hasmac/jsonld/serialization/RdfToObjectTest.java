/*
 * Copyright 2025 HASMAC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package no.hasmac.jsonld.serialization;

import jakarta.json.JsonObject;
import no.hasmac.jsonld.JsonLdError;
import no.hasmac.jsonld.JsonLdOptions;
import no.hasmac.rdf.RdfValue;
import no.hasmac.rdf.impl.DefaultRdfProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RdfToObjectTest {

    @Test
    public void testRdfToObjectLangStringWithNativeTypes() throws JsonLdError {
        RdfValue langStringObject = DefaultRdfProvider.INSTANCE.createLangString("text", "en");
        JsonObject result = RdfToObject.with(langStringObject, JsonLdOptions.RdfDirection.COMPOUND_LITERAL, true).build();

        assertNotNull(result);
        assertTrue(result.containsKey("@value"));
        assertTrue(result.containsKey("@language"));
    }

}