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

package no.hasmac.rdf.impl;

import no.hasmac.rdf.RdfResource;

import java.util.Objects;

final class RdfResourceImpl implements RdfResource {

    private final String value;
    private final boolean blankNode;

    protected RdfResourceImpl(final String value, boolean isBlankNode) {
        this.value = value;
        this.blankNode = isBlankNode;
    }

    @Override
    public boolean isBlankNode() {
        return blankNode;
    }

    @Override
    public boolean isIRI() {
        return !blankNode;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if(obj instanceof RdfResource){
            RdfResource other = (RdfResource) obj;
            return Objects.equals(value, other.getValue());
        }

        return false;

    }

    @Override
    public String toString() {
        return Objects.toString(value);
    }
}
