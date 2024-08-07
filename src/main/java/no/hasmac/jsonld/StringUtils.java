/*
 * Copyright 2021 APICATALOG and HASMAC.
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

public final class StringUtils {

    private StringUtils() {
        // protected
    }

    public static boolean isBlank(final String string) {
        return string == null || string.isBlank();
    }

    public static boolean isNotBlank(final String string) {
        return string != null && !string.isBlank();
    }

    public static String strip(final String string) {
        return string.strip();
    }

    public static String stripTrailing(final String string) {
        return string.stripTrailing();
    }

}
