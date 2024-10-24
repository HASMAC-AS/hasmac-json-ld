/*
 * Copyright 2020 APICATALOG and HASMAC.
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

package no.hasmac.rdf.io.nquad.writer;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonParser;
import no.hasmac.jsonld.json.JsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.stream.Stream;
import java.util.zip.ZipException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public final class NQuadsWriterTestSuite {

    public static Stream<NQuadsWriterTestCase> load() throws ZipException, IOException, URISyntaxException {

        try (final InputStream is = NQuadsWriterTestSuite.class.getResourceAsStream("/no/hasmac/rdf/io/nquad/manifest.json")) {

            assertNotNull(is);

            final JsonParser parser = Json.createParser(is);

            parser.next();

            return parser
                        .getArray()
                        .stream()
                        .filter(JsonUtils::isObject)
                        .map(JsonObject.class::cast)
                        .map(NQuadsWriterTestCase::of);
        }
    }
}
