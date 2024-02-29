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

import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import no.hasmac.jsonld.JsonLdError;
import no.hasmac.jsonld.JsonLdErrorCode;
import no.hasmac.jsonld.JsonLdOptions;
import no.hasmac.jsonld.JsonLdOptions.RdfDirection;
import no.hasmac.jsonld.flattening.NodeMap;
import no.hasmac.jsonld.json.JsonCanonicalizer;
import no.hasmac.jsonld.json.JsonUtils;
import no.hasmac.jsonld.lang.BlankNode;
import no.hasmac.jsonld.lang.Keywords;
import no.hasmac.jsonld.lang.LanguageTag;
import no.hasmac.jsonld.lang.ListObject;
import no.hasmac.jsonld.lang.NodeObject;
import no.hasmac.jsonld.lang.ValueObject;
import no.hasmac.jsonld.uri.UriUtils;
import no.hasmac.rdf.RdfValueFactory;
import no.hasmac.rdf.lang.RdfConstants;
import no.hasmac.rdf.lang.XsdConstants;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @see <a href="https://w3c.github.io/json-ld-api/#deserialize-json-ld-to-rdf-algorithm">Object to RDF Conversion</a>
 */
final class ObjectToRdf<Triple, Quad, Iri extends Resource, Bnode extends Resource, Resource extends Value, Literal extends Value, Value> {

    private static final Logger LOGGER = Logger.getLogger(ObjectToRdf.class.getName());

    private static final DecimalFormat xsdNumberFormat =
            new DecimalFormat("0.0##############E0", new DecimalFormatSymbols(Locale.ENGLISH));

    static {
        xsdNumberFormat.setMinimumFractionDigits(1);
    }

    private final RdfValueFactory<Triple, Quad, Iri, Bnode, Resource, Literal, Value> rdfValueFactory;
    private final JsonLdOptions options;

    // required
    private JsonObject item;
    private List<Triple> triples;
    private NodeMap nodeMap;

    // optional
    private RdfDirection rdfDirection;
    private boolean uriValidation;

    private ObjectToRdf(JsonObject item, List<Triple> triples, NodeMap nodeMap, RdfValueFactory<Triple, Quad, Iri, Bnode, Resource, Literal, Value> rdfValueFactory, JsonLdOptions options) {
        this.item = item;
        this.triples = triples;
        this.nodeMap = nodeMap;
        this.rdfValueFactory = rdfValueFactory;

        // default values
        this.rdfDirection = null;
        this.uriValidation = JsonLdOptions.DEFAULT_URI_VALIDATION;
        this.options = options;
    }

    public static <Triple, Quad, Iri extends Resource, Bnode extends Resource, Resource extends Value, Literal extends Value, Value> ObjectToRdf<Triple, Quad, Iri, Bnode, Resource, Literal, Value> with(JsonObject item, List<Triple> triples, NodeMap nodeMap, RdfValueFactory<Triple, Quad, Iri, Bnode, Resource, Literal, Value> rdfValueFactory, JsonLdOptions options) {
        return new ObjectToRdf<Triple, Quad, Iri, Bnode, Resource, Literal, Value>(item, triples, nodeMap, rdfValueFactory, options);
    }

    public ObjectToRdf<Triple, Quad, Iri, Bnode, Resource, Literal, Value> rdfDirection(RdfDirection rdfDirection) {
        this.rdfDirection = rdfDirection;
        return this;
    }

    public Value build() throws JsonLdError {

        // 1. - 2.
        if (NodeObject.isNodeObject(item)) {

            JsonValue id = item.get(Keywords.ID);

            if (JsonUtils.isNotString(id) || JsonUtils.isNull(id)) {
                return null;
            }

            String idString = ((JsonString) id).getString();

            if (BlankNode.isWellFormed(idString)) {
                return rdfValueFactory.createBlankNode(idString);

            } else if (UriUtils.isAbsoluteUri(idString, uriValidation)) {
                return rdfValueFactory.createIRI(idString);
            }

            return null;
        }

        // 3.
        if (ListObject.isListObject(item)) {
            return ListToRdf
                    .with(item.get(Keywords.LIST).asJsonArray(), triples, nodeMap, rdfValueFactory, options)
                    .rdfDirection(rdfDirection)
                    .uriValidation(uriValidation)
                    .build();
        }

        // 4.
        if (!ValueObject.isValueObject(item)) {
            return null;
        }

        final JsonValue value = item.get(Keywords.VALUE);

        // 5.
        String datatype = item.containsKey(Keywords.TYPE) && JsonUtils.isString(item.get(Keywords.TYPE))
                ? item.getString(Keywords.TYPE)
                : null;

        // 6.
        if (datatype != null && !Keywords.JSON.equals(datatype)) {
            boolean absoluteUri = UriUtils.isAbsoluteUri(datatype, uriValidation);
            if (!absoluteUri) {
                LOGGER.log(Level.WARNING, "Datatype [{0}] is not an absolute IRI nor @json and value is skipped.", datatype);
                if (options.isExceptionOnWarning()) {
                    throw new JsonLdError(JsonLdErrorCode.UNSPECIFIED, "Datatype '" + datatype + "' is not an absolute IRI nor @json.");
                }
                return null;
            }
        }

        // 7.
        if (item.containsKey(Keywords.LANGUAGE)
                && (JsonUtils.isNotString(item.get(Keywords.LANGUAGE))
                || !LanguageTag.isWellFormed(item.getString(Keywords.LANGUAGE)))
        ) {
            LOGGER.log(Level.WARNING, "Language tag [{0}] is not well formed string and value is skipped.", item.get(Keywords.LANGUAGE));
            if (options.isExceptionOnWarning()) {
                throw new JsonLdError(JsonLdErrorCode.INVALID_LANGUAGE_TAGGED_STRING, "Language tag '" + item.get(Keywords.LANGUAGE) + "' is not well formed.");
            }
            return null;
        }

        String valueString = null;

        // 8.
        if (Keywords.JSON.equals(datatype)) {
            valueString = JsonCanonicalizer.canonicalize(value);
            datatype = RdfConstants.JSON;

            // 9.
        } else if (JsonUtils.isTrue(value)) {

            valueString = "true";

            if (datatype == null) {
                datatype = XsdConstants.BOOLEAN;
            }

        } else if (JsonUtils.isFalse(value)) {

            valueString = "false";

            if (datatype == null) {
                datatype = XsdConstants.BOOLEAN;
            }


            // 10. - 11.
        } else if (JsonUtils.isNumber(value)) {

            JsonNumber number = ((JsonNumber) value);


            // 11.
            if ((!number.isIntegral() && number.doubleValue() % -1 != 0)
                    || XsdConstants.DOUBLE.equals(datatype)
                    || XsdConstants.FLOAT.equals(datatype)
                    || number.bigDecimalValue().compareTo(BigDecimal.ONE.movePointRight(21)) >= 0
            ) {

                valueString = toXsdDouble(number.bigDecimalValue());

                if (datatype == null) {
                    datatype = XsdConstants.DOUBLE;
                }

                // 10.
            } else {

                valueString = number.bigIntegerValue().toString();

                if (datatype == null) {
                    datatype = XsdConstants.INTEGER;
                }

            }

            // 12.
        } else if (datatype == null) {

            datatype = item.containsKey(Keywords.LANGUAGE)
                    ? RdfConstants.LANG_STRING
                    : XsdConstants.STRING
            ;
        }

        if (valueString == null) {

            if (JsonUtils.isNotString(value)) {
                return null;
            }

            valueString = ((JsonString) value).getString();
        }

        Literal rdfLiteral = null;

        // 13.
        if (item.containsKey(Keywords.DIRECTION) && rdfDirection != null) {

            // 13.1.
            final String language = item.containsKey(Keywords.LANGUAGE)
                    ? item.getString(Keywords.LANGUAGE).toLowerCase()
                    : "";
            // 13.2.
            if (RdfDirection.I18N_DATATYPE == rdfDirection) {
                datatype = "https://www.w3.org/ns/i18n#"
                        .concat(language)
                        .concat("_")
                        .concat(item.getString(Keywords.DIRECTION));

                rdfLiteral = rdfValueFactory.createTypedLiteral(valueString, datatype);

                // 13.3.
            } else if (RdfDirection.COMPOUND_LITERAL == rdfDirection) {

                final String blankNodeId = nodeMap.createIdentifier();

                // 13.3.1.
                final Resource subject = rdfValueFactory.createBlankNode(blankNodeId);

                // 13.3.2.
                triples.add(rdfValueFactory.createTriple(
                        subject,
                        rdfValueFactory.createIRI(RdfConstants.VALUE),
                        rdfValueFactory.createString(valueString))
                );

                // 13.3.3.
                if (item.containsKey(Keywords.LANGUAGE) && JsonUtils.isString(item.get(Keywords.LANGUAGE))) {
                    triples.add(rdfValueFactory.createTriple(
                            subject,
                            rdfValueFactory.createIRI(RdfConstants.LANGUAGE),
                            rdfValueFactory.createString(item.getString(Keywords.LANGUAGE).toLowerCase()))
                    );
                }

                // 13.3.4.
                triples.add(rdfValueFactory.createTriple(
                        subject,
                        rdfValueFactory.createIRI(RdfConstants.DIRECTION),
                        rdfValueFactory.createString(item.getString(Keywords.DIRECTION)))
                );

                return rdfValueFactory.createBlankNode(blankNodeId);
            }

            // 14.
        } else {
            if (item.containsKey(Keywords.LANGUAGE) && JsonUtils.isString(item.get(Keywords.LANGUAGE))) {

                rdfLiteral = rdfValueFactory.createLangString(valueString, item.getString(Keywords.LANGUAGE));

            } else {
                rdfLiteral = rdfValueFactory.createTypedLiteral(valueString, datatype);
            }
        }

        // 15.
        return rdfLiteral;
    }

    private static String toXsdDouble(BigDecimal bigDecimal) {
        return xsdNumberFormat.format(bigDecimal);
    }

    public ObjectToRdf<Triple, Quad, Iri, Bnode, Resource, Literal, Value> uriValidation(boolean uriValidation) {
        this.uriValidation = uriValidation;
        return this;
    }
}
