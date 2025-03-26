package com.aih.zpicturebackend.manage.websocket.disruptor;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.aih.zpicturebackend.manage.websocket.disruptor.model.PictureEditEvent;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 创建并配置 Disruptor 实例
 */
@Configuration
public class PictureEditEventDisruptorConfig {

    @Resource
    private PictureEditEventWorkHandler pictureEditEventWorkHandler;

    // 通过@Bean方法创建具体实例，让Spring容器管理，供其他类使用
    @Bean("pictureEditEventDisruptor")
    public Disruptor<PictureEditEvent> messageModelRingBuffer() {
        // ringBuffer 的大小
        int bufferSize = 1024 * 256;
        // 定义 Disruptor 实例。 事件工厂、ringBuffer 大小、线程工厂 => EventFactory<I> eventFactory, int ringBufferSize, ThreadFactory threadFactory
        Disruptor<PictureEditEvent> disruptor = new Disruptor<>(
                PictureEditEvent::new,
                bufferSize, // ringBuffer 环形缓冲区的大小
                ThreadFactoryBuilder.create().setNamePrefix("pictureEditEventDisruptor").build() // 顺便给个前缀(方便排查)
        );
        // 放入自定义的事件处理器【消费者】
        disruptor.handleEventsWithWorkerPool(pictureEditEventWorkHandler);
        disruptor.start(); // 开启 Disruptor
        // 返回 Disruptor 实例
        return disruptor;
    }
}
