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

package no.hasmac.rdf;

public  interface RdfValueFactory<Triple, Quad, Iri extends Resource, Bnode extends Resource, Resource extends Value, Literal extends Value, Value> {

    Triple createTriple(Resource subject, Iri predicate, Value object);

    Quad createQuad(Resource subject, Iri predicate, Value object, Resource graphName);

    Quad createQuad(Triple triple, Resource graphName);


    Iri createIRI( String value) ;

    Bnode createBlankNode(String value) ;

    Literal createTypedLiteral(String value, String datatype);

    Literal createString(String value);

    Literal createLangString(String value, String lang);

}
