package com.aih.zpicturebackend.model.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PictureTest {

    @Test
    void getOriginalUrlPrefersStoredPictureUrl() {
        Picture picture = new Picture();
        picture.setUrl("https://example.com/space/1/2026-05-19_demo.webp");
        picture.setThumbnailUrl("https://example.com/space/1/2026-05-19_demo_thumbnail.jpg");

        assertEquals("https://example.com/space/1/2026-05-19_demo.webp", picture.getOriginalUrl());
    }
}
