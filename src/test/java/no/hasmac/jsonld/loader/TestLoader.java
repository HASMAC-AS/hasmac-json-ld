package no.hasmac.jsonld.loader;

import no.hasmac.jsonld.JsonLdError;

import java.net.URI;

public interface TestLoader {

    byte[] fetchBytes(URI create) throws JsonLdError;

}
