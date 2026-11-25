package com.pfe.management.dto.response;

import org.springframework.core.io.Resource;

public class DeliverableFileResource {

    private final Resource resource;
    private final String originalFilename;
    private final String contentType;
    private final long contentLength;

    public DeliverableFileResource(Resource resource, String originalFilename, String contentType, long contentLength) {
        this.resource = resource;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.contentLength = contentLength;
    }

    public Resource getResource() {
        return resource;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getContentLength() {
        return contentLength;
    }
}
