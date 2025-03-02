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
import no.hasmac.jsonld.JsonLdErrorCode;
import no.hasmac.jsonld.document.Document;
import no.hasmac.jsonld.document.JsonDocument;
import no.hasmac.jsonld.document.RdfDocument;
import no.hasmac.jsonld.http.media.MediaType;
import no.hasmac.rdf.Rdf;

import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class DocumentResolver {

    private static final Logger LOGGER = Logger.getLogger(DocumentResolver.class.getName());

    private MediaType fallbackContentType;

    public DocumentResolver() {
        this.fallbackContentType = null;
    }

    /**
     * Return a reader or throw {@link JsonLdError} if there is no reader nor fallbackContentType.
     *
     * @param contentType content type of the requested reader
     * @return a reader allowing to transform an input into {@link Document}
     * @throws JsonLdError
     */
    public DocumentReader<InputStream> getReader(MediaType contentType) throws JsonLdError {
        return findReader(contentType)
                .or(() -> {

                    if (fallbackContentType != null) {
                        LOGGER.log(Level.WARNING, "Content type [{0}] is not acceptable, trying again with [{1}].", new Object[] { contentType, fallbackContentType});
                        return findReader(fallbackContentType);
                    }

                    return Optional.empty();
                })
                .orElseThrow(() -> new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED,
                    "Unsupported media type '" + contentType
                    + "'. Supported content types are ["
                    + MediaType.JSON_LD + ", "
                    + MediaType.JSON  + ", +json, "
                    + (Rdf.canRead().stream().map(MediaType::toString).collect(Collectors.joining(", ")))
                    + "]"
                    ));
    }

    public void setFallbackContentType(MediaType fallbackContentType) {
        this.fallbackContentType = fallbackContentType;
    }

    private static Optional<DocumentReader<InputStream>> findReader(final MediaType type) {

        if (type == null) {
            return Optional.empty();
        }

        if (JsonDocument.accepts(type)) {
            return Optional.of(is ->  JsonDocument.of(type, is));
        }

        if (RdfDocument.accepts(type)) {
            return Optional.of(is -> RdfDocument.of(type, is));
        }

        return Optional.empty();
    }
}
