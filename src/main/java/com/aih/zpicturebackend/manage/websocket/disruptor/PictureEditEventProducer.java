package com.aih.zpicturebackend.manage.websocket.disruptor;

import com.aih.zpicturebackend.manage.websocket.disruptor.model.PictureEditEvent;
import com.aih.zpicturebackend.manage.websocket.model.PictureEditRequestMessage;
import com.aih.zpicturebackend.model.entity.User;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * 定义 Disruptor 生产者，只负责事件发布
 */
@Component
@Slf4j
public class PictureEditEventProducer {

    @Resource
    @Lazy
    private Disruptor<PictureEditEvent> pictureEditEventDisruptor;

    // 发布事件到 ringBuffer 环形缓冲区
    public void publishEvent(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) {
        // 获取 ringBuffer 环形缓冲区
        RingBuffer<PictureEditEvent> ringBuffer = pictureEditEventDisruptor.getRingBuffer();
        // 1. 获取下一个可用的序号
        long next = ringBuffer.next(); // 下一个可用的序号
        // 2. 填充数据[构建事件对象]
        PictureEditEvent pictureEditEvent = ringBuffer.get(next); // 获取事件对象
        pictureEditEvent.setSession(session);
        pictureEditEvent.setPictureEditRequestMessage(pictureEditRequestMessage);
        pictureEditEvent.setUser(user);
        pictureEditEvent.setPictureId(pictureId);
        // 3. 发布事件
        ringBuffer.publish(next);
        // 只有在发布后，消费者才能看到并处理这个事件。
    }

    /**
     * 优雅停机
     */
    @PreDestroy
    public void destroy() {
        pictureEditEventDisruptor.shutdown();
    }
}
