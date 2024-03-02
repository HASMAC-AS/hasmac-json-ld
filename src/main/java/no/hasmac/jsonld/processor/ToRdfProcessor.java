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
 */

package no.hasmac.jsonld.processor;

import jakarta.json.JsonArray;
import no.hasmac.jsonld.JsonLdError;
import no.hasmac.jsonld.JsonLdErrorCode;
import no.hasmac.jsonld.JsonLdOptions;
import no.hasmac.jsonld.deseralization.JsonLdToRdf;
import no.hasmac.jsonld.document.Document;
import no.hasmac.jsonld.flattening.NodeMap;
import no.hasmac.jsonld.flattening.NodeMapBuilder;
import no.hasmac.jsonld.loader.DocumentLoaderOptions;
import no.hasmac.rdf.Rdf;
import no.hasmac.rdf.RdfConsumer;
import no.hasmac.rdf.RdfDataset;
import no.hasmac.rdf.RdfValueFactory;

import java.net.URI;

/**
 * @see <a href="https://w3c.github.io/json-ld-api/#dom-jsonldprocessor-tordf">JsonLdProcessor.toRdf()</a>
 */
public final class ToRdfProcessor<Triple, Quad, Iri extends Resource, Bnode extends Resource, Resource extends Value, Literal extends Value, Value> {

    private ToRdfProcessor() {
    }

    public static RdfDataset toRdf(final URI input, final JsonLdOptions options) throws JsonLdError {
        RdfDataset dataset = Rdf.createDataset();
        toRdf(input, options, dataset, Rdf.createValueFactory());
        return dataset;
    }

    public static RdfDataset toRdf(Document input, final JsonLdOptions options) throws JsonLdError {
        RdfDataset dataset = Rdf.createDataset();
        toRdf(input, options, dataset, Rdf.createValueFactory());
        return dataset;
    }

    public static <Triple, Quad, Iri extends Resource, Bnode extends Resource, Resource extends Value, Literal extends Value, Value> void toRdf(final URI input, final JsonLdOptions options, RdfConsumer<Triple, Quad> rdfConsumer, RdfValueFactory<Triple, Quad, Iri, Bnode, Resource, Literal, Value> rdfValueFactory) throws JsonLdError {
        if (options.getDocumentLoader() == null) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Document loader is null. Cannot fetch [" + input + "].");
        }

        final DocumentLoaderOptions loaderOptions = new DocumentLoaderOptions();
        loaderOptions.setExtractAllScripts(options.isExtractAllScripts());

        final Document remoteDocument = options.getDocumentLoader().loadDocument(input, loaderOptions);

        if (remoteDocument == null) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED);
        }

        toRdf(remoteDocument, options, rdfConsumer, rdfValueFactory);
    }


    public static <Triple, Quad, Iri extends Resource, Bnode extends Resource, Resource extends Value, Literal extends Value, Value> void toRdf(Document input, final JsonLdOptions options, RdfConsumer<Triple, Quad> rdfConsumer, RdfValueFactory<Triple, Quad, Iri, Bnode, Resource, Literal, Value> rdfValueFactory) throws JsonLdError {

        final JsonLdOptions expansionOptions = new JsonLdOptions(options);

        expansionOptions.setProcessingMode(options.getProcessingMode());
        expansionOptions.setBase(options.getBase());
        expansionOptions.setExpandContext(options.getExpandContext());

        final JsonArray expandedInput = ExpansionProcessor.expand(input, expansionOptions, false);

        JsonLdToRdf
                .with(
                        NodeMapBuilder.with(expandedInput, new NodeMap()).build(),
                        rdfConsumer, rdfValueFactory, expansionOptions
                )
                .rdfDirection(options.getRdfDirection())
                .uriValidation(options.isUriValidation())
                .build();
    }

}
