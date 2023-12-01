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
