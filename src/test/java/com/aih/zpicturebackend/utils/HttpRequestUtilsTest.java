package com.aih.zpicturebackend.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpRequestUtilsTest {

    @Test
    void isJsonContentTypeAcceptsCharset() {
        assertTrue(HttpRequestUtils.isJsonContentType("application/json"));
        assertTrue(HttpRequestUtils.isJsonContentType("application/json;charset=UTF-8"));
        assertTrue(HttpRequestUtils.isJsonContentType("application/json; charset=utf-8"));
        assertTrue(HttpRequestUtils.isJsonContentType("application/problem+json"));
        assertTrue(HttpRequestUtils.isJsonContentType("application/vnd.z-picture+json; charset=UTF-8"));
    }

    @Test
    void isJsonContentTypeRejectsNonJson() {
        assertFalse(HttpRequestUtils.isJsonContentType(null));
        assertFalse(HttpRequestUtils.isJsonContentType(""));
        assertFalse(HttpRequestUtils.isJsonContentType("multipart/form-data"));
        assertFalse(HttpRequestUtils.isJsonContentType("application/x-www-form-urlencoded"));
    }
}
