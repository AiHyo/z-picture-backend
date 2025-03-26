package com.aih.zpicturebackend.manage.websocket.disruptor;

import cn.hutool.json.JSONUtil;
import com.aih.zpicturebackend.manage.websocket.PictureEditHandler;
import com.aih.zpicturebackend.manage.websocket.disruptor.model.PictureEditEvent;
import com.aih.zpicturebackend.manage.websocket.model.PictureEditMessageTypeEnum;
import com.aih.zpicturebackend.manage.websocket.model.PictureEditRequestMessage;
import com.aih.zpicturebackend.manage.websocket.model.PictureEditResponseMessage;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.service.UserService;
import com.lmax.disruptor.WorkHandler;
import groovy.lang.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

/**
 * 定义消费事件的的处理逻辑
 */
@Slf4j
@Component
public class PictureEditEventWorkHandler implements WorkHandler<PictureEditEvent> {

    @Resource
    @Lazy
    private PictureEditHandler pictureEditHandler;

    @Resource
    private UserService userService;

    @Override // 定义消费者对事件的的处理逻辑     // 实际调用的是自定义的pictureEditHandler里的方法
    public void onEvent(PictureEditEvent event) throws Exception {
        // 从event中获取数据：消息、session、用户、图片id
        PictureEditRequestMessage pictureEditRequestMessage = event.getPictureEditRequestMessage();
        WebSocketSession session = event.getSession();
        User user = event.getUser();
        Long pictureId = event.getPictureId();
        // 获取消息类型
        String type = pictureEditRequestMessage.getType();
        PictureEditMessageTypeEnum pictureEditMessageTypeEnum = PictureEditMessageTypeEnum.valueOf(type);
        // 根据消息类型处理消息, 通知其他客户端(用户)进行同步
        switch (pictureEditMessageTypeEnum) {
            case ENTER_EDIT:
                pictureEditHandler.handleEnterEditMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            case EDIT_ACTION:
                pictureEditHandler.handleEditActionMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            case EXIT_EDIT:
                pictureEditHandler.handleExitEditMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            default: // 错误消息，返回错误消息
                PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
                pictureEditResponseMessage.setMessage("消息类型错误");
                pictureEditResponseMessage.setUser(userService.getUserVO(user));
                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(pictureEditResponseMessage)));
        }
    }
}
