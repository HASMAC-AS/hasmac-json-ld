package no.hasmac.jsonld.http;

import no.hasmac.jsonld.JsonLdError;

import java.net.URI;

public interface HttpClient {

    HttpResponse send(URI targetUri, String requestProfile) throws JsonLdError;

}
