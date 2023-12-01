/*
 * Copyright 2020 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import no.hasmac.jsonld.JsonLdError;
import no.hasmac.jsonld.JsonLdOptions;
import no.hasmac.jsonld.JsonLdOptions.RdfDirection;
import no.hasmac.jsonld.flattening.NodeMap;
import no.hasmac.jsonld.json.JsonUtils;
import no.hasmac.rdf.RdfValueFactory;
import no.hasmac.rdf.lang.RdfConstants;

import jakarta.json.JsonArray;
import jakarta.json.JsonValue;

/**
 *
 * @see <a href="https://w3c.github.io/json-ld-api/#list-to-rdf-conversion">List to RDF Conversion</a>
 *
 */
final class ListToRdf<Triple, Quad, Iri extends Resource, Bnode extends Resource, Resource extends Value, Literal extends Value, Value> {

    private final RdfValueFactory<Triple, Quad, Iri, Bnode, Resource, Literal, Value> rdfValueFactory;
    // required
    private final JsonArray list;
    private final List<Triple> triples;
    private final NodeMap nodeMap;

    // optional
    private RdfDirection rdfDirection;
    private boolean uriValidation;

    private ListToRdf(final JsonArray list, final List<Triple> triples, NodeMap nodeMap, RdfValueFactory<Triple, Quad, Iri  , Bnode, Resource, Literal, Value> rdfValueFactory) {
        this.list = list;
        this.triples = triples;
        this.nodeMap = nodeMap;
        this.rdfValueFactory = rdfValueFactory;

        // default values
        this.rdfDirection = null;
        this.uriValidation = JsonLdOptions.DEFAULT_URI_VALIDATION;
    }

    public static <Triple, Quad, Iri extends Resource, Bnode extends Resource, Resource extends Value, Literal extends Value, Value> ListToRdf<Triple, Quad, Iri  , Bnode, Resource, Literal, Value> with(final JsonArray list, final List<Triple> triples, NodeMap nodeMap, RdfValueFactory<Triple, Quad, Iri  , Bnode, Resource, Literal, Value> rdfValueFactory) {
        return new ListToRdf<Triple, Quad, Iri  , Bnode, Resource, Literal, Value>(list, triples, nodeMap, rdfValueFactory);
    }

    public ListToRdf<Triple, Quad, Iri  , Bnode, Resource, Literal, Value> rdfDirection(RdfDirection rdfDirection) {
        this.rdfDirection = rdfDirection;
        return this;
    }

    public Value build() throws JsonLdError {

        // 1.
        if (JsonUtils.isEmptyArray(list)) {
            return rdfValueFactory.createIRI(RdfConstants.NIL);
        }

        // 2.
        final String[] bnodes = new String[list.size()];

        IntStream.range(0,  bnodes.length).forEach(i -> bnodes[i] = nodeMap.createIdentifier());

        // 3.
        int index = 0;
        for (final JsonValue item : list) {

            final String subject = bnodes[index];
            index++;

            // 3.1.
            final List<Triple> embeddedTriples = new ArrayList<>();

            // 3.2.
            Value rdfValue = ObjectToRdf
                    .with(item.asJsonObject(), embeddedTriples, nodeMap, rdfValueFactory)
                    .rdfDirection(rdfDirection)
                    .uriValidation(uriValidation)
                    .build();
            if(rdfValue != null) {
                                triples.add(rdfValueFactory.createTriple(
                                                rdfValueFactory.createBlankNode(subject),
                                                rdfValueFactory.createIRI(RdfConstants.FIRST),
                                                rdfValue));
            }

            // 3.4.
            final Value rest = (index < bnodes.length) ? rdfValueFactory.createBlankNode(bnodes[index])
                                        : rdfValueFactory.createIRI(RdfConstants.NIL)
                                        ;

            triples.add(rdfValueFactory.createTriple(
                                    rdfValueFactory.createBlankNode(subject),
                                    rdfValueFactory.createIRI(RdfConstants.REST),
                                    rest
                                    ));

            // 3.5.
            triples.addAll(embeddedTriples);
        }

        // 4.
        return rdfValueFactory.createBlankNode(bnodes[0]);
    }

    public ListToRdf<Triple, Quad, Iri  , Bnode, Resource, Literal, Value> uriValidation(boolean uriValidation) {
        this.uriValidation = uriValidation;
        return this;
    }
}
