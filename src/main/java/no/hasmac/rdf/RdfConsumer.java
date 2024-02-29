package no.hasmac.rdf;

public  interface RdfConsumer<Triple, Quad> {

    void handleTriple(Triple triple);

    void handleQuad(Quad nquad);

}
