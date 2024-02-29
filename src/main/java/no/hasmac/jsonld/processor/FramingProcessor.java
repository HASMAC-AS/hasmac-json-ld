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
package no.hasmac.jsonld.processor;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import no.hasmac.jsonld.JsonLdError;
import no.hasmac.jsonld.JsonLdErrorCode;
import no.hasmac.jsonld.JsonLdOptions;
import no.hasmac.jsonld.JsonLdVersion;
import no.hasmac.jsonld.compaction.Compaction;
import no.hasmac.jsonld.context.ActiveContext;
import no.hasmac.jsonld.document.Document;
import no.hasmac.jsonld.flattening.NodeMap;
import no.hasmac.jsonld.flattening.NodeMapBuilder;
import no.hasmac.jsonld.framing.Frame;
import no.hasmac.jsonld.framing.Framing;
import no.hasmac.jsonld.framing.FramingState;
import no.hasmac.jsonld.json.JsonMapBuilder;
import no.hasmac.jsonld.json.JsonProvider;
import no.hasmac.jsonld.json.JsonUtils;
import no.hasmac.jsonld.lang.BlankNode;
import no.hasmac.jsonld.lang.Keywords;
import no.hasmac.jsonld.loader.DocumentLoaderOptions;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @see <a href="https://www.w3.org/TR/json-ld11-framing/#dom-jsonldprocessor-frame">JsonLdProcessor.frame()</a>
 *
 */
public final class FramingProcessor {

    private FramingProcessor() {
    }

    public static JsonObject frame(final URI input, final Document frame, final JsonLdOptions options) throws JsonLdError {
        if (options.getDocumentLoader() == null) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Document loader is null. Cannot fetch [" + input + "].");
        }

        final DocumentLoaderOptions loaderOptions = new DocumentLoaderOptions();
        loaderOptions.setExtractAllScripts(options.isExtractAllScripts());

        final Document remoteDocument = options.getDocumentLoader().loadDocument(input, loaderOptions);

        if (remoteDocument == null) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Returned document is null [" + input + "].");
        }

        return frame(remoteDocument, frame, options);
    }

    public static JsonObject frame(final Document input, final URI frameUri, final JsonLdOptions options) throws JsonLdError {
        if (options.getDocumentLoader() == null) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Document loader is null. Cannot fetch [" + frameUri + "].");
        }

        final Document frameDocument = options.getDocumentLoader().loadDocument(frameUri, new DocumentLoaderOptions());

        if (frameDocument == null) {
            throw new JsonLdError(JsonLdErrorCode.INVALID_REMOTE_CONTEXT, "Returned frame is null [" + frameUri + "] is null.");
        }

        return frame(input, frameDocument, options);
    }

    public static JsonObject frame(final Document input, final Document frame, final JsonLdOptions options) throws JsonLdError {

        if (frame == null) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Frame or Frame.Document is null.");
        }

        final JsonStructure frameStructure =
                                    frame
                                        .getJsonContent()
                                        .orElseThrow(() -> new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Frame is not JSON object but null."));

        if (JsonUtils.isNotObject(frameStructure)) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Frame is not JSON object but [" + frameStructure + "].");
        }

        final JsonObject frameObject = frameStructure.asJsonObject();

        // 4.
        final JsonLdOptions expansionOptions = new JsonLdOptions(options);
        expansionOptions.setOrdered(false);

        JsonArray expandedInput = ExpansionProcessor.expand(input, expansionOptions, false);

        // 7.
        JsonArray expandedFrame = ExpansionProcessor.expand(frame, expansionOptions, true);


        JsonValue context = JsonValue.EMPTY_JSON_OBJECT;

        if (frameObject.containsKey(Keywords.CONTEXT)
                ) {
            context = frameObject.get(Keywords.CONTEXT);
        }

        // 9.
        final URI contextBase = (frame.getContextUrl() != null)
                                ? frame.getDocumentUrl()
                                : options.getBase();

        // 10-11.
        final ActiveContext activeContext =
                                new ActiveContext(input.getDocumentUrl(), input.getDocumentUrl(), options)
                                            .newContext()
                                            .create(context, contextBase);

        // 13.
        String graphKey = activeContext.uriCompaction().vocab(true).compact(Keywords.GRAPH);
        boolean frameDefault = false;
        for (String key : frameObject.keySet()) {
            if(key.equals(graphKey)) {
                frameDefault = true;
                break;
            }
        }

        // 14.
        final FramingState state = new FramingState();

        state.setEmbed(options.getEmbed());     // 14.1.
        state.setEmbedded(false);               // 14.2.
        state.setExplicitInclusion(options.isExplicit());   // 14.3.
        state.setRequireAll(options.isRequiredAll());       // 14.4.
        state.setOmitDefault(options.isOmitDefault());      // 14.5.

        state.setGraphMap(NodeMapBuilder.with(expandedInput, new NodeMap()).build());   // 14.7.

        if (frameDefault) {
            state.setGraphName(Keywords.DEFAULT); // 14.6.

        } else {
            state.setGraphName(Keywords.MERGED);
            state.getGraphMap().merge();
        }

        // 15.
        final JsonMapBuilder resultMap = JsonMapBuilder.create();

        // 16.
        Framing.with(state,
                    new ArrayList<>(state.getGraphMap().subjects(state.getGraphName())),
                    Frame.of(expandedFrame),
                    resultMap,
                    null
                    )
                .ordered(options.isOrdered())
                .frame();

        Stream<JsonValue> result = resultMap.valuesToArray().stream();

        // 17. - remove blank @id
        if (!activeContext.inMode(JsonLdVersion.V1_0)) {

            final List<String> remove = findBlankNodes(resultMap.valuesToArray());

            if (!remove.isEmpty()) {
                result = result.map(v -> FramingProcessor.removeBlankIdKey(v, remove));
            }
        }

        // 18. - remove preserve
        final JsonArrayBuilder filtered = JsonProvider.instance().createArrayBuilder();

        result.map(FramingProcessor::removePreserve).forEach(filtered::add);

        // 19.
        JsonValue compactedResults = Compaction
                                            .with(activeContext)
                                            .compactArrays(options.isCompactArrays())
                                            .ordered(options.isOrdered())
                                            .compact(filtered.build());

        // 19.1.
        if (JsonUtils.isEmptyArray(compactedResults)) {
            compactedResults = JsonValue.EMPTY_JSON_OBJECT;

        // 19.2.
        } else if (JsonUtils.isArray(compactedResults)) {

            compactedResults = JsonProvider.instance().createObjectBuilder()
                                    .add(graphKey, compactedResults).build();

        }

        // 20.
        compactedResults = replaceNull(compactedResults);

        final boolean omitGraph;

        if (options.isOmitGraph() == null) {

            omitGraph = activeContext.inMode(JsonLdVersion.V1_1);

        } else {
            omitGraph = options.isOmitGraph();
        }

        // 21.
        if (!omitGraph && !compactedResults.asJsonObject().containsKey(graphKey)) {

            if (compactedResults.asJsonObject().isEmpty()) {

                compactedResults = JsonProvider.instance().createObjectBuilder().add(graphKey,
                        JsonValue.EMPTY_JSON_ARRAY
                        ).build();

            } else {

                compactedResults = JsonProvider.instance().createObjectBuilder().add(graphKey,
                        JsonProvider.instance().createArrayBuilder().add(compactedResults)
                                        ).build();
            }
        }

        // 19.3.
        if (!JsonUtils.isEmptyArray(context) && !JsonUtils.isEmptyObject(context)) {
            compactedResults = JsonProvider.instance().createObjectBuilder(compactedResults.asJsonObject()).add(Keywords.CONTEXT, context).build();
        }

        return compactedResults.asJsonObject();
    }

    public static JsonObject frame(final URI input, final URI frame, final JsonLdOptions options) throws JsonLdError {
        return frame(getDocument(input, options), getDocument(frame, options), options);
    }

    private static Document getDocument(final URI document, final JsonLdOptions options) throws JsonLdError {

        if (options.getDocumentLoader() == null) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Document loader is null. Cannot fetch [" + document + "].");
        }

        final DocumentLoaderOptions loaderOptions = new DocumentLoaderOptions();
        loaderOptions.setExtractAllScripts(options.isExtractAllScripts());

        final Document remoteDocument = options.getDocumentLoader().loadDocument(document, loaderOptions);

        if (remoteDocument == null) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Cannot load document [" + document + "].");
        }

        return remoteDocument;
    }

    private static JsonValue removePreserve(JsonValue value) {

        if (JsonUtils.isScalar(value)) {
            return value;
        }

        if (JsonUtils.isArray(value)) {

            final JsonArrayBuilder array = JsonProvider.instance().createArrayBuilder();

            value.asJsonArray().forEach(item -> array.add(removePreserve(item)));

            return array.build();
        }

        final JsonObjectBuilder object = JsonProvider.instance().createObjectBuilder();

        for (final Entry<String, JsonValue> entry : value.asJsonObject().entrySet()) {

            if (Keywords.PRESERVE.equals(entry.getKey())) {

                return entry.getValue().asJsonArray().get(0);
            }
            object.add(entry.getKey(), removePreserve(entry.getValue()));
        }

        return object.build();
    }

    private static JsonValue replaceNull(JsonValue value) {

        if (JsonUtils.isString(value) && Keywords.NULL.equals(((JsonString)value).getString())) {
            return JsonValue.NULL;

        } else if (JsonUtils.isScalar(value)) {
            return value;

        } else if (JsonUtils.isArray(value)) {

            final JsonArrayBuilder array = JsonProvider.instance().createArrayBuilder();

            value.asJsonArray().forEach(item -> array.add(replaceNull(item)));

            final JsonArray result = array.build();

            return result.size() != 1 || JsonUtils.isNotNull(result.get(0)) ? result : JsonValue.EMPTY_JSON_ARRAY;
        }

        final JsonObjectBuilder object = JsonProvider.instance().createObjectBuilder();

        value.asJsonObject().entrySet().forEach(entry -> object.add(entry.getKey(), replaceNull(entry.getValue())));

        return object.build();
    }

    private static JsonValue removeBlankIdKey(JsonValue value, List<String> blankNodes) {

        if (JsonUtils.isScalar(value)) {
            return value;
        }

        if (JsonUtils.isArray(value)) {

            final JsonArrayBuilder array = JsonProvider.instance().createArrayBuilder();

            value.asJsonArray().forEach(item -> array.add(removeBlankIdKey(item, blankNodes)));

            return array.build();
        }

        final JsonObjectBuilder object = JsonProvider.instance().createObjectBuilder();

        for (final Entry<String, JsonValue> entry : value.asJsonObject().entrySet()) {

            if (Keywords.ID.equals(entry.getKey())
                    && JsonUtils.isString(entry.getValue())
                    && blankNodes.contains(((JsonString)entry.getValue()).getString())) {

                    continue;
            }

            object.add(entry.getKey(), removeBlankIdKey(entry.getValue(), blankNodes));
        }

        return object.build();
    }

    private static List<String> findBlankNodes(final JsonArray array) {

        if(array.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> candidates = new HashMap<>();

        array.forEach(v -> findBlankNodes(v, candidates));

        if(candidates.isEmpty()) {
            return List.of();
        }

        return candidates.entrySet().stream().filter(e -> e.getValue() == 1).map(Entry::getKey).collect(Collectors.toList());
    }

    private static void findBlankNodes(JsonValue value, final Map<String, Integer> blankNodes) {

        if (JsonUtils.isString(value)) {

            if (BlankNode.isWellFormed(((JsonString)value).getString())) {
                Integer count = blankNodes.computeIfAbsent(((JsonString)value).getString(), x -> 0);
                blankNodes.put(((JsonString)value).getString(), ++count);
            }

            return;
        }

        if (JsonUtils.isScalar(value)) {
            return;
        }

        if (JsonUtils.isArray(value)) {
            for (JsonValue item : value.asJsonArray()) {
                findBlankNodes(item, blankNodes);
            }
            return;
        }

        for (Entry<String, JsonValue> entry : value.asJsonObject().entrySet()) {
            findBlankNodes(entry.getValue(), blankNodes);
        }
    }
}
