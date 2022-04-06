package com.apicatalog.jsonld.processor;

import java.net.URI;

import org.w3c.dom.stylesheets.DocumentStyle;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdErrorCode;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.context.ActiveContext;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;

import jakarta.json.JsonObject;

/**
 * Experimental low-level API. The processor provides low-level API to transform JsonLd.
 * 
 * @since 1.3.0
 */
public class JsonLdProcessor {
    
    public enum DocumentState {
        COMPACTED,
        EXPANDED,
        FLATTENED,
        FRAMED,
        UNKNOWN,
    }
    
    public enum ResourceType {
        EXPANSION_CONTEXT,
        COMPACTION_CONTEXT,
        FRAME,
    }

    protected final JsonLdOptions options;
    
    protected Document document;
    
    protected DocumentState documentState;
    
    public JsonLdProcessor() {
        this.options = new JsonLdOptions();
    }
    
    public JsonLdProcessor document(URI documentUri) {
        //TODO
//        final DocumentLoaderOptions loaderOptions = new DocumentLoaderOptions();
//        loaderOptions.setExtractAllScripts(options.isExtractAllScripts());

//        document(load(documentUri));
        return this;
    }
    
    public JsonLdProcessor document(Document document) {
        //TODO
        this.document = document;
        return this;
    }
    
    public JsonLdProcessor register(String key, Document document, ResourceType resourceType) {
        //TODO
        switch (resourceType) {
        case EXPANSION_CONTEXT:
            break;
        case COMPACTION_CONTEXT:
            break;
        case FRAME:
            break;
        }        
        return this;
    }

    public JsonLdProcessor register(String key, URI documentUri, ResourceType resourceType) {
        //TODO
        return this;
    }

    public JsonLdProcessor expand() {
        //TODO
        return this;
        
    }

    public JsonLdProcessor expand(String contextKey) {
        //TODO
        return this;
        
    }

    public JsonLdProcessor expand(ActiveContext context) {
        //TODO
        return this;
    }

    public JsonLdOptions options() {
        return options;
    }

    protected static final Document load(final URI documentUri, final DocumentLoader loader, final DocumentLoaderOptions options) throws JsonLdError {

        if (loader == null) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Document loader is null. Cannot fetch [" + documentUri + "].");
        }

        final Document remoteDocument = loader.loadDocument(documentUri, options);

        if (remoteDocument == null) { 
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Remote document[" + documentUri + "] returned null.");
        }

        return remoteDocument;
    }
}
