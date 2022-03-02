package com.img.imgbackend;

import org.springframework.mock.web.MockMultipartFile;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MockPart extends MockMultipartFile implements Part {

    private Map<String, String> headers;

    public MockPart(String name, byte[] content) {
        super(name, content);
        init();
    }

    public MockPart(String name, InputStream contentStream) throws IOException {
        super(name, contentStream);
        init();
    }

    public MockPart(String name, String originalFilename, String contentType, byte[] content) {
        super(name, originalFilename, contentType, content);
        init();
    }

    public MockPart(String name, String originalFilename, String contentType, InputStream contentStream) throws IOException {
        super(name, originalFilename, contentType, contentStream);
        init();
    }

    public void init() {
        this.headers = new HashMap<String, String>();
        if (getOriginalFilename() != null) {
            this.headers.put("Content-Disposition".toLowerCase(), "form-data; name=\"" + getName() + "\"; filename=\"" + getOriginalFilename() + "\"");
        } else {
            this.headers.put("Content-Disposition".toLowerCase(), "form-data; name=\"" + getName() + "\"");
        }
        if (getContentType() != null) {
            this.headers.put("Content-Type".toLowerCase(), getContentType());
        }
    }

    @Override
    public String getSubmittedFileName() {
        return this.getName();
    }

    @Override
    public void write(String fileName) throws IOException {
    }

    @Override
    public void delete() throws IOException {
    }

    @Override
    public String getHeader(String name) {
        return this.headers.get(name.toLowerCase());
    }

    @Override
    public Collection<String> getHeaders(String name) {
        List<String> res = new ArrayList<String>();
        if (getHeader(name) != null) {
            res.add(getHeader(name));
        }
        return res;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return this.headers.keySet();
    }
}
