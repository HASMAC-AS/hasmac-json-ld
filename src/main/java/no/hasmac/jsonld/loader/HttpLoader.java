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
 */

package no.hasmac.jsonld.loader;

import no.hasmac.jsonld.http.DefaultHttpClient;
import no.hasmac.jsonld.http.HttpClient;

public class HttpLoader extends DefaultHttpLoader {

    private static final HttpLoader INSTANCE = new HttpLoader(DefaultHttpClient.defaultInstance());

    /**
     * @deprecated use <code>HttpLoader(no.hasmac.jsonld.http.HttpClient httpClient)</code>
     *
     * @param httpClient
     */
    @Deprecated(since = "1.0.3")
    public HttpLoader(java.net.http.HttpClient httpClient) {
        this(httpClient, MAX_REDIRECTIONS);
    }

    /**
     * @deprecated use <code>HttpLoader(no.hasmac.jsonld.http.HttpClient httpClient, int maxRedirection)</code>
     *
     * @param httpClient
     * @param maxRedirections
     */
    @Deprecated(since = "1.0.3")
    public HttpLoader(java.net.http.HttpClient httpClient, int maxRedirections) {
        this(new DefaultHttpClient(httpClient), maxRedirections);
    }

    public HttpLoader(HttpClient httpClient) {
        super(httpClient);
    }

    public HttpLoader(HttpClient httpClient, int maxRedirections) {
        super(httpClient, maxRedirections);
    }

    public static DocumentLoader defaultInstance() {
        return INSTANCE;
    }
}
