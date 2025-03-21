/*
 * Copyright 2024 HASMAC.
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

package no.hasmac.jsonld;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

import java.util.AbstractList;
import java.util.List;

/**
 * This class is a workaround for the fact that the default implementation of JsonArray is not modifiable. It would
 * be better to use an internal data structure instead of using the Jakarta JSON API when transforming the data.
 */
public class ModifiableJsonArray extends AbstractList<JsonValue> implements JsonArray {
    private final List<JsonValue> valueList;
    private int hashCode;
    private BloomFilter<Integer> filter;


    public ModifiableJsonArray(List<JsonValue> valueList) {
        this.valueList = valueList;
        if (valueList.size() > 1000) {
            filter = BloomFilter.create(
                    Funnels.integerFunnel(),
                    5000,
                    0.01);
            for (JsonValue value : valueList) {
                filter.put(value.hashCode());
            }
        }

    }

    @Override
    public int size() {
        return valueList.size();
    }

    @Override
    public JsonObject getJsonObject(int index) {
        return (JsonObject) valueList.get(index);
    }

    @Override
    public JsonArray getJsonArray(int index) {
        return (JsonArray) valueList.get(index);
    }

    @Override
    public JsonNumber getJsonNumber(int index) {
        return (JsonNumber) valueList.get(index);
    }

    @Override
    public JsonString getJsonString(int index) {
        return (JsonString) valueList.get(index);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends JsonValue> List<T> getValuesAs(Class<T> clazz) {
        return (List<T>) valueList;
    }

    @Override
    public String getString(int index) {
        return getJsonString(index).getString();
    }

    @Override
    public String getString(int index, String defaultValue) {
        try {
            return getString(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public int getInt(int index) {
        return getJsonNumber(index).intValue();
    }

    @Override
    public int getInt(int index, int defaultValue) {
        try {
            return getInt(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public boolean getBoolean(int index) {
        JsonValue jsonValue = get(index);
        if (jsonValue == JsonValue.TRUE) {
            return true;
        } else if (jsonValue == JsonValue.FALSE) {
            return false;
        } else {
            throw new ClassCastException();
        }
    }

    @Override
    public boolean getBoolean(int index, boolean defaultValue) {
        try {
            return getBoolean(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public boolean isNull(int index) {
        return valueList.get(index).equals(JsonValue.NULL);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.ARRAY;
    }

    @Override
    public JsonValue get(int index) {
        return valueList.get(index);
    }

    public boolean add(JsonValue value) {
        if (filter != null) {
            filter.put(value.hashCode());
        }
        return valueList.add(value);
    }

    @Override
    public boolean contains(Object o) {
        if (valueList.isEmpty()) {
            return false;
        }
        if (o == null) {
            return false;
        }

        if (filter == null && valueList.size() > 100) {
            filter = BloomFilter.create(
                    Funnels.integerFunnel(),
                    100000,
                    0.01);
            for (JsonValue value : valueList) {
                filter.put(value.hashCode());
            }
        }
        if (filter != null) {
            boolean mightContain = filter.mightContain(o.hashCode());
            if (!mightContain) {
                return false;
            }
        }

        int oHashCode = o.hashCode();
        if (valueList.size() == 1) {
            JsonValue jsonValue = valueList.get(0);
            if (oHashCode == jsonValue.hashCode() && jsonValue.equals(o)) {
                return true;
            }
        }

        for (JsonValue value : valueList) {
            if (value.hashCode() == oHashCode && value.equals(o)) {
                return true;
            }
        }
        return false;

    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = super.hashCode();
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return "ModifiableJsonArray{" +
                "valueList=" + valueList +
                '}';
    }

    @Override
    public JsonArray asJsonArray() {
        return this;
    }
}
