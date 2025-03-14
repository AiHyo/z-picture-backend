package com.aih.zpicturebackend;

import cn.hutool.core.util.URLUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ZPictureBackendApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(URLUtil.getPath("https://xiaotian-1347976582.cos.ap-guangzhou.myqcloud.com//public/1899288499064930305/2025-03-12_BJkJsF6EqHxRcQ4F.jpg"));
    }

}
