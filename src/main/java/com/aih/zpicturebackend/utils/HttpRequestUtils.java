package com.aih.zpicturebackend.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;

import java.util.Locale;

public final class HttpRequestUtils {

    private HttpRequestUtils() {
    }

    public static boolean isJsonContentType(String contentType) {
        if (StrUtil.isBlank(contentType)) {
            return false;
        }
        String mediaType = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        return ContentType.JSON.getValue().equals(mediaType)
                || (mediaType.startsWith("application/") && mediaType.endsWith("+json"));
    }
}
