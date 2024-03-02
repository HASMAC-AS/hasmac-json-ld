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

package no.hasmac.jsonld.loader;

import no.hasmac.jsonld.JsonLdError;
import no.hasmac.jsonld.document.Document;
import no.hasmac.jsonld.http.media.MediaType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileLoaderTest {

    @Test
    void testLoadNQuads() throws URISyntaxException, JsonLdError {

        URL fileUrl = getClass().getResource("document.nq");

        assertNotNull(fileUrl);

        Document document = (new FileLoader()).loadDocument(fileUrl.toURI(), new DocumentLoaderOptions());

        assertNotNull(document);
        assertTrue(MediaType.N_QUADS.match(document.getContentType()));
    }

    @Test
    void testLoadJson() throws URISyntaxException, JsonLdError {

        URL fileUrl = getClass().getResource("document.json");

        assertNotNull(fileUrl);

        Document document = (new FileLoader()).loadDocument(fileUrl.toURI(), new DocumentLoaderOptions());

        assertNotNull(document);
        assertTrue(MediaType.JSON.match(document.getContentType()));
    }

    @Test
    void testLoadJsonLd() throws URISyntaxException, JsonLdError {

        URL fileUrl = getClass().getResource("document.jsonld");

        assertNotNull(fileUrl);

        Document document = (new FileLoader()).loadDocument(fileUrl.toURI(), new DocumentLoaderOptions());

        assertNotNull(document);
        assertTrue(MediaType.JSON_LD.match(document.getContentType()));
    }

    @Test
    void testLoadHtml() throws URISyntaxException {

        URL fileUrl = getClass().getResource("document.html");

        assertNotNull(fileUrl);

        assertThrows(JsonLdError.class, () -> new FileLoader().loadDocument(fileUrl.toURI(), new DocumentLoaderOptions()));
    }

    @Test
    void testUnsupportedScheme() throws URISyntaxException {
        assertThrows(JsonLdError.class, () -> new FileLoader().loadDocument(URI.create("https://github.com/"), new DocumentLoaderOptions()));
    }

}
