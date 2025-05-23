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

package no.hasmac.jsonld.lang;

import jakarta.json.JsonValue;
import no.hasmac.jsonld.json.JsonUtils;

import java.util.Optional;

public final class ValueObject {

    private ValueObject() {
    }

    public static boolean isValueObject(JsonValue value) {
        return JsonUtils.isObject(value) && value.asJsonObject().containsKey(Keywords.VALUE);
    }

    public static Optional<JsonValue> getValue(JsonValue value) {
        return isValueObject(value)
                    ? Optional.ofNullable(value.asJsonObject().get(Keywords.VALUE))
                    : Optional.empty();
    }

}
