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

package no.hasmac.jsonld.deseralization;

import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import no.hasmac.jsonld.JsonLdError;
import no.hasmac.jsonld.JsonLdErrorCode;
import no.hasmac.jsonld.JsonLdOptions;
import no.hasmac.jsonld.JsonLdOptions.RdfDirection;
import no.hasmac.jsonld.flattening.NodeMap;
import no.hasmac.jsonld.json.JsonUtils;
import no.hasmac.jsonld.lang.BlankNode;
import no.hasmac.jsonld.lang.Keywords;
import no.hasmac.jsonld.lang.Utils;
import no.hasmac.jsonld.uri.UriUtils;
import no.hasmac.rdf.RdfConsumer;
import no.hasmac.rdf.RdfValueFactory;
import no.hasmac.rdf.lang.RdfConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class JsonLdToRdf<Triple, Quad, Iri extends Resource, Bnode extends Resource, Resource extends Value, Literal extends Value, Value> {

    private static final Logger LOGGER = Logger.getLogger(JsonLdToRdf.class.getName());

    // required
    private final NodeMap nodeMap;
    private final RdfConsumer<Triple, Quad> dataset;
    private final RdfValueFactory<Triple, Quad, Iri, Bnode, Resource, Literal, Value> rdfValueFactory;
    private final JsonLdOptions options;

    // optional
    private RdfDirection rdfDirection;
    private boolean uriValidation;

    private JsonLdToRdf(NodeMap nodeMap, RdfConsumer<Triple, Quad> dataset, RdfValueFactory<Triple, Quad, Iri, Bnode, Resource, Literal, Value> rdfValueFactory, JsonLdOptions options) {
        this.nodeMap = nodeMap;
        this.dataset = dataset;
        this.rdfValueFactory = rdfValueFactory;

        this.rdfDirection = null;
        this.uriValidation = JsonLdOptions.DEFAULT_URI_VALIDATION;
        this.options = options;
    }

    public static <Triple, Quad, Iri extends Resource, Bnode extends Resource, Resource extends Value, Literal extends Value, Value> JsonLdToRdf<Triple, Quad, Iri, Bnode, Resource, Literal, Value> with(NodeMap nodeMap, RdfConsumer<Triple, Quad> dataset, RdfValueFactory<Triple, Quad, Iri, Bnode, Resource, Literal, Value> rdfValueFactory, JsonLdOptions options) {
        return new JsonLdToRdf<Triple, Quad, Iri, Bnode, Resource, Literal, Value>(nodeMap, dataset, rdfValueFactory, options);
    }


    public JsonLdToRdf<Triple, Quad, Iri, Bnode, Resource, Literal, Value> rdfDirection(RdfDirection rdfDirection) {
        this.rdfDirection = rdfDirection;
        return this;
    }


    public void build() throws JsonLdError {

        // 1.
        for (final String graphName : Utils.index(nodeMap.graphs(), true)) {

            // 1.2.
            final Resource rdfGraphName;

            if (Keywords.DEFAULT.equals(graphName)) {
                rdfGraphName = null;

            } else {

                // 1.1.
                if (BlankNode.isWellFormed(graphName)) {

                    rdfGraphName = rdfValueFactory.createBlankNode(graphName);

                } else if (UriUtils.isAbsoluteUri(graphName, uriValidation)) {
                    rdfGraphName = rdfValueFactory.createIRI(graphName);
                } else {
                    continue;
                }
            }

            // 1.3.
            for (final String subject : Utils.index(nodeMap.subjects(graphName), true)) {

                final Resource rdfSubject;

                // 1.3.1.
                if (BlankNode.isWellFormed(subject)) {
                    rdfSubject = rdfValueFactory.createBlankNode(subject);
                } else if (UriUtils.isAbsoluteUri(subject, uriValidation)) {
                    rdfSubject = rdfValueFactory.createIRI(subject);
                } else {
                    LOGGER.log(Level.WARNING, "Non well-formed subject [{0}].", subject);
                    if (options.isExceptionOnWarning()) {
                        throw new JsonLdError(JsonLdErrorCode.UNSPECIFIED, "Non well-formed subject ["+subject+"].");
                    }
                    continue;
                }

                // 1.3.2.
                for (final String property : Utils.index(nodeMap.properties(graphName, subject), true)) {

                    // 1.3.2.1.
                    if (Keywords.TYPE.equals(property)) {

                        for (JsonValue type : nodeMap.get(graphName, subject, property).asJsonArray()) {

                            if (JsonUtils.isNotString(type)) {
                                continue;
                            }

                            final String typeString = ((JsonString) type).getString();

                            final Value rdfObject;

                            if (BlankNode.isWellFormed(typeString)) {
                                rdfObject = rdfValueFactory.createBlankNode(typeString);

                            } else if (UriUtils.isAbsoluteUri(typeString, uriValidation)) {
                                rdfObject = rdfValueFactory.createIRI(typeString);
                            } else {
                                continue;
                            }

                            dataset.handleQuad(rdfValueFactory.createQuad(
                                    rdfSubject,
                                    rdfValueFactory.createIRI(RdfConstants.TYPE),
                                    rdfObject,
                                    rdfGraphName
                            ));
                        }

                        // 1.3.2.2.
                    } else if (!Keywords.contains(property)) {

                        final Iri rdfProperty;

                        if (UriUtils.isAbsoluteUri(property, uriValidation)) {
                            rdfProperty = rdfValueFactory.createIRI(property);
                        } else {
                            rdfProperty = null;
                        }

                        if (rdfProperty != null) {

                            // 1.3.2.5.
                            for (JsonValue item : nodeMap.get(graphName, subject, property).asJsonArray()) {

                                // 1.3.2.5.1.
                                final List<Triple> listTriples = new ArrayList<>();

                                // 1.3.2.5.2.
                                Value rdfValue = ObjectToRdf
                                        .with(item.asJsonObject(), listTriples, nodeMap, rdfValueFactory, options)
                                        .rdfDirection(rdfDirection)
                                        .uriValidation(uriValidation)
                                        .build();

                                if (rdfValue != null) {
                                    dataset.handleQuad(rdfValueFactory.createQuad(
                                            rdfSubject,
                                            rdfProperty,
                                            rdfValue,
                                            rdfGraphName
                                    ));
                                }

                                // 1.3.2.5.3.
                                listTriples.stream()
                                        .map(triple -> rdfValueFactory.createQuad(triple, rdfGraphName))
                                        .forEach(dataset::handleQuad);
                            }
                        }
                    }
                }
            }
        }
    }

    public JsonLdToRdf<Triple, Quad, Iri, Bnode, Resource, Literal, Value> uriValidation(boolean uriValidation) {
        this.uriValidation = uriValidation;
        return this;
    }
}
