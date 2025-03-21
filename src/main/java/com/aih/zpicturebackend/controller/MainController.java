package com.aih.zpicturebackend.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.aih.zpicturebackend.common.BaseResponse;
import com.aih.zpicturebackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @PostMapping("login") // 测试登陆
    public String doLogin(String username, String password) {
        if ("user".equals(username) && "12345678".equals(password)) {
            StpUtil.login(10001);
            return "登录成功";
        }
        return "登录失败";
    }

    @GetMapping("isLogin") // 是否登录
    public String isLogin() {
        return "当前会话是否登录：" + StpUtil.isLogin();
    }

    @GetMapping("tokenInfo") // 查询Token信息
    public SaResult tokenInfo() {
        return SaResult.data(StpUtil.getTokenInfo());
    }

    @GetMapping("logout") // 测试注销
    public SaResult logout() {
        StpUtil.logout();
        return SaResult.ok();
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}
