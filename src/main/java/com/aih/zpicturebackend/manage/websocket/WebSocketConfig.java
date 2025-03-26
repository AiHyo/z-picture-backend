package com.aih.zpicturebackend.manage.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

/**
 * WebSocket配置 (定义连接)
 */
@Configuration
@EnableWebSocket // 启用了WebSocket支持
public class WebSocketConfig implements WebSocketConfigurer {
    // 声明式配置：实现了WebSocketConfigurer接口，通过回调方法配置WebSocket

    @Resource  // 自定义的处理器
    private PictureEditHandler pictureEditHandler;

    @Resource // 自定义的拦截器
    private WsHandshakeInterceptor wsHandshakeInterceptor;

    @Override // 注册了WebSocket处理器和拦截器，
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(pictureEditHandler, "/ws/picture/edit")
                .addInterceptors(wsHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
