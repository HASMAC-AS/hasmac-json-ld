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

package no.hasmac.jsonld.framing;

import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import no.hasmac.jsonld.JsonLdEmbed;
import no.hasmac.jsonld.JsonLdError;
import no.hasmac.jsonld.json.JsonMapBuilder;
import no.hasmac.jsonld.json.JsonProvider;
import no.hasmac.jsonld.json.JsonUtils;
import no.hasmac.jsonld.lang.Keywords;
import no.hasmac.jsonld.lang.ListObject;
import no.hasmac.jsonld.lang.NodeObject;
import no.hasmac.jsonld.lang.Utils;
import no.hasmac.jsonld.lang.ValueObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @see <a href="https://w3c.github.io/json-ld-framing/#framing-algorithm">Framing Algorithm</a>
 *
 */
public final class Framing {

    // required
    private final FramingState state;
    private final List<String> subjects;
    private final Frame frame;
    private final JsonMapBuilder parent;

    private String activeProperty;

    // optional
    private boolean ordered;

    private Framing(FramingState state, List<String> subjects, Frame frame, JsonMapBuilder parent, String activeProperty) {
        this.state = state;
        this.subjects = subjects;
        this.frame = frame;
        this.parent = parent;
        this.activeProperty = activeProperty;

        // default values
        this.ordered = false;
    }

    public static Framing with(FramingState state, List<String> subjects, Frame frame, JsonMapBuilder parent, String activeProperty) {
        return new Framing(state, subjects, frame, parent, activeProperty);
    }

    public Framing ordered(boolean ordered) {
        this.ordered = ordered;
        return this;
    }

    public void frame() throws JsonLdError {

        // 2.
        final JsonLdEmbed embed = frame.getEmbed(state.getEmbed());

        final boolean explicit = frame.getExplicit(state.isExplicitInclusion());

        final boolean requireAll = frame.getRequireAll(state.isRequireAll());

        // 3.
        final List<String> matchedSubjects =
                                FrameMatcher
                                    .with(state, frame, requireAll)
                                    .match(subjects);

        // 4.
        for (final String id : Utils.index(matchedSubjects, ordered)) {

            final Map<String, JsonValue> node = state.getGraphMap().get(state.getGraphName(), id);

            final String nodeId = JsonUtils.isString(node.get(Keywords.ID))
                                ? ((JsonString)node.get(Keywords.ID)).getString()
                                : null;

            // 4.1.
            final JsonMapBuilder output = JsonMapBuilder.create();
            output.put(Keywords.ID, JsonProvider.instance().createValue(id));


            if (activeProperty == null) {
                state.clearDone();
            }

            // 4.2.
            if (!state.isEmbedded() && state.isDone(id))  {
                continue;
            }

            // 4.3.
            if (state.isEmbedded()
                    && (JsonLdEmbed.NEVER == embed
                            || state.isParent(nodeId)
                            )

                    ) {
                addToResult(parent, activeProperty, output.build());
                continue;
            }

            // 4.4.
            if (state.isEmbedded()
                    && JsonLdEmbed.ONCE == embed
                    && state.isDone(id)

                    ) {
                addToResult(parent, activeProperty, output.build());
                continue;
            }

            state.markDone(id);
            state.addParent(nodeId);

            // 4.5.
            if (state.getGraphMap().contains(id)) {

                // 4.5.1.
                boolean recurse;
                Frame subframe;

                if (!frame.contains(Keywords.GRAPH)) {
                    recurse = !Keywords.MERGED.equals(state.getGraphName());
                    subframe = Frame.EMPTY;

                // 4.5.2.
                } else {
                    recurse = !Keywords.MERGED.equals(id) && !Keywords.DEFAULT.equals(id);

                    if (JsonUtils.isObject(frame.get(Keywords.GRAPH))
                            || JsonUtils.isArray(frame.get(Keywords.GRAPH))
                            ) {
                        subframe = Frame.of((JsonStructure)frame.get(Keywords.GRAPH));

                    } else {
                        subframe = Frame.EMPTY;
                    }
                }

                // 4.5.3.
                if (recurse) {

                    final FramingState graphState = new FramingState(state);

                    graphState.setGraphName(id);
                    graphState.setEmbedded(false);

                    Framing.with(
                                graphState,
                                new ArrayList<>(state.getGraphMap().get(id).map(Map::keySet).orElseGet(() -> Collections.emptySet())),
                                subframe,
                                output,
                                Keywords.GRAPH
                                )
                            .ordered(ordered)
                            .frame();
                }
            }

            // 4.6.
            if (frame.contains(Keywords.INCLUDED)) {

                FramingState includedState = new FramingState(state);
                includedState.setEmbedded(false);

                Framing.with(
                            includedState,
                            subjects,
                            Frame.of((JsonStructure)frame.get(Keywords.INCLUDED)),
                            output,
                            Keywords.INCLUDED
                            )
                        .ordered(ordered)
                        .frame();
            }

            // 4.7.
            for (final String property : Utils.index(state.getGraphMap().properties(state.getGraphName(), id), ordered)) {
                final JsonValue objects = state.getGraphMap().get(state.getGraphName(), id, property);

                // 4.7.1.
                if (Keywords.contains(property)) {
                    output.put(property, objects);
                    continue;
                }

                // 4.7.2.
                if (explicit && !frame.contains(property)) {
                    continue;
                }

                // 4.7.3.
                for (final JsonValue item : JsonUtils.toCollection(objects)) {

                    JsonValue subframe = frame.get(property);

                    if (subframe == null) {
                        subframe = JsonProvider.instance().createObjectBuilder()
                                        .add(Keywords.EMBED, "@".concat(embed.name().toLowerCase()))
                                        .add(Keywords.EXPLICIT, explicit)
                                        .add(Keywords.REQUIRE_ALL, requireAll)
                                        .build();
                    }

                    // 4.7.3.1.
                    if (ListObject.isListObject(item)) {

                            JsonValue listFrameValue = null;

                            if (frame.contains(property)
                                    && !JsonUtils.isEmptyArray(frame.get(property))
                                    && JsonUtils.isObject(frame.get(property).asJsonArray().get(0))
                                ) {
                                listFrameValue = frame.get(property).asJsonArray().get(0).asJsonObject().get(Keywords.LIST);
                            }

                            if (listFrameValue == null) {
                                listFrameValue = JsonProvider.instance().createObjectBuilder()
                                        .add(Keywords.EMBED, "@".concat(embed.name().toLowerCase()))
                                        .add(Keywords.EXPLICIT, explicit)
                                        .add(Keywords.REQUIRE_ALL, requireAll)
                                        .build();
                            }

                            final Frame listFrame = Frame.of((JsonStructure)listFrameValue);

                            final JsonArrayBuilder list = JsonProvider.instance().createArrayBuilder();

                            for (final JsonValue listItem : JsonUtils.toCollection(item.asJsonObject().get(Keywords.LIST))) {

                                // 4.7.3.1.1.
                                if (NodeObject.isNodeReference(listItem)) {

                                    FramingState listState = new FramingState(state);
                                    listState.setEmbedded(true);


                                    final JsonMapBuilder listResult = JsonMapBuilder.create();

                                    Framing.with(
                                                listState,
                                                Arrays.asList(listItem.asJsonObject().getString(Keywords.ID)),
                                                listFrame,
                                                listResult,
                                                Keywords.LIST)
                                            .ordered(ordered)
                                            .frame();

                                    if (listResult.containsKey(Keywords.LIST)) {
                                        listResult.get(Keywords.LIST).ifPresent(list::add);
                                    }

                                // 4.7.3.1.2.
                                } else {
                                    list.add(listItem);
                                }
                            }

                            output.add(property, JsonProvider.instance().createObjectBuilder().add(Keywords.LIST, list));

                    } else if (NodeObject.isNodeReference(item)) {

                        FramingState clonedState = new FramingState(state);
                        clonedState.setEmbedded(true);

                        Framing.with(
                                    clonedState,
                                    Arrays.asList(item.asJsonObject().getString(Keywords.ID)),
                                    Frame.of((JsonStructure)subframe),
                                    output,
                                    property)
                                .ordered(ordered)
                                .frame();

                    } else if (ValueObject.isValueObject(item)) {
                        if (Frame.of((JsonStructure)subframe).matchValue(item)) {
                            output.add(property, item);
                        }

                    } else {
                        output.add(property, item);
                    }
                }
            }

            // 4.7.4. - default values
            for (String property : frame.keys()) {
                if (output.containsKey(property)
                        || !Keywords.TYPE.equals(property) && Keywords.matchForm(property)
                        || Keywords.TYPE.equals(property) && !frame.isDefaultObject(property)
                        ) {
                    continue;
                }

                // 4.7.4.2.
                final JsonObject propertyFrame;

                if (JsonUtils.isNonEmptyArray(frame.get(property))) {
                    propertyFrame = frame.get(property).asJsonArray().getJsonObject(0);

                } else {
                    propertyFrame = JsonValue.EMPTY_JSON_OBJECT;

                }

                // 4.7.4.3.
                if (Frame.getBoolean(propertyFrame, Keywords.OMIT_DEFAULT, state.isOmitDefault())) {
                    continue;
                }

                // 4.7.4.4.
                JsonValue defaultValue = propertyFrame.get(Keywords.DEFAULT);

                if (JsonUtils.isNull(defaultValue)) {
                    defaultValue = JsonProvider.instance().createValue(Keywords.NULL);
                }

                output.add(property, JsonProvider.instance().createObjectBuilder()
                                                    .add(Keywords.PRESERVE,
                                                            JsonProvider.instance().createArrayBuilder().add(
                                                            defaultValue)));
            }

            // 4.7.5. - reverse properties
            if (frame.contains(Keywords.REVERSE)) {

                final JsonValue reverseObject = frame.get(Keywords.REVERSE);

                if (JsonUtils.isObject(reverseObject)) {

                    for (final String reverseProperty :  reverseObject.asJsonObject().keySet()) {

                        final Frame subframe = Frame.of((JsonStructure)reverseObject.asJsonObject().get(reverseProperty));

                        for (final String subjectProperty : state.getGraphMap().get(state.getGraphName()).map(Map::keySet).orElseGet(() -> Collections.emptySet())) {

                            final JsonValue nodeValues = state.getGraphMap().get(state.getGraphName(), subjectProperty, reverseProperty);

                            if (nodeValues != null
                                    && JsonUtils.toStream(nodeValues)
                                                .filter(JsonUtils::isObject)
                                                .map(JsonObject.class::cast)
                                                .filter(v -> v.containsKey(Keywords.ID))
                                                .map(v -> v.getString(Keywords.ID))
                                                .anyMatch(vid -> Objects.equals(vid, id))
                                    ) {

                                final JsonMapBuilder reverseResult = JsonMapBuilder.create();

                                final FramingState reverseState = new FramingState(state);
                                reverseState.setEmbedded(true);

                                Framing.with(
                                            reverseState,
                                            Arrays.asList(subjectProperty),
                                            subframe,
                                            reverseResult,
                                            null)
                                        .ordered(ordered)
                                        .frame();

                                output
                                    .getMapBuilder(Keywords.REVERSE)
                                    .add(reverseProperty, reverseResult.valuesToArray());

                            }
                        }
                    }
                }
            }

            state.removeLastParent();

            // 4.8.
            addToResult(parent, activeProperty, output.build());
        }
    }

    private static void addToResult(JsonMapBuilder result, String property, JsonValue value) {
        if (property == null) {
            result.put(Integer.toHexString(result.size()), value);

        } else {
            result.add(property, value);
        }
    }
}
