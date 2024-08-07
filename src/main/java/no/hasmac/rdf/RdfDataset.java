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

package no.hasmac.rdf;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RdfDataset extends RdfConsumer<RdfTriple, RdfNQuad> {

    RdfGraph getDefaultGraph();

    /**
     * Add <code>N-Quad</code> to the dataset.
     *
     * @param nquad to add
     * @return the same {@link RdfDataset} instance
     */
    RdfDataset add(RdfNQuad nquad);

    /**
     * Add a triple to default graph.
     *
     * @param triple to add
     * @return the same {@link RdfDataset} instance
     */
    RdfDataset add(RdfTriple triple);

    @Override
    default void handleTriple(RdfTriple rdfTriple) {
        add(rdfTriple);
    }

    @Override
    default void handleQuad(RdfNQuad nquad) {
        add(nquad);
    }

    List<RdfNQuad> toList();

    Set<RdfResource> getGraphNames();

    Optional<RdfGraph> getGraph(RdfResource graphName);

    /**
     * Get the size of the dataset.
     *
     * @return total number of <code>N-Quads</code> in the dataset
     */
    int size();
}
