package com.aih.zpicturebackend.manage.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.aih.zpicturebackend.manage.websocket.disruptor.PictureEditEventProducer;
import com.aih.zpicturebackend.manage.websocket.model.PictureEditActionEnum;
import com.aih.zpicturebackend.manage.websocket.model.PictureEditMessageTypeEnum;
import com.aih.zpicturebackend.manage.websocket.model.PictureEditRequestMessage;
import com.aih.zpicturebackend.manage.websocket.model.PictureEditResponseMessage;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * websocket处理器。继承TextWebSocketHandler，处理文本消息，用于处理图片编辑相关的消息。
 */
@Component
@Slf4j
public class PictureEditHandler extends TextWebSocketHandler {
    // 可能同时有多个 WebSocket 客户端建立连接和发送消息，集合要使用并发包（JUC）中的 ConcurrentHashMap，来保证线程安全

    // 每张图片的编辑状态，key: pictureId, value: 当前正在编辑的用户 ID
    private final Map<Long, Long> pictureEditingUsers = new ConcurrentHashMap<>();
    // 保存所有连接的会话，key: pictureId, value: 用户会话集合
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();

    @Autowired // 事件生产者
    private PictureEditEventProducer pictureEditEventProducer;

    public PictureEditHandler() {
    }

    @Resource
    private UserService userService;

    /**
     * 连接建立通知 => 直接在WebSocket处理器中同步广播
     * @param session
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        // 从 session 中获取所需信息
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        // 根据pictureId，将当前会话(用户/客户端)加入到会话集合中
        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet()); // 如果不存在，则初始化
        pictureSessions.get(pictureId).add(session); // 以 pictureId 为 key，将当前会话(用户/客户端)加入到会话集合中
        // 封装响应信息
        PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
        responseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("用户 %s 进入图片", user.getUserName());
        responseMessage.setMessage(message);
        responseMessage.setUser(userService.getUserVO(user));
        // 把响应信息广播给全体会话(用户/客户端)集合
        this.broadcastToPicture(pictureId, responseMessage);
    }

    /**
     * 消息处理通知：由Disruptor消费者线程异步处理后广播。
     * 收到前端发送的消息，根据消息类别 => 发布事件【生成消息到 Disruptor 环形队列中】
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        // 从 session 中获取所需信息
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        // 此处 message.getPayload()，获取到的是前端传来的 JSON 字符串 转为=> PictureEditRequestMessage 对象
        PictureEditRequestMessage requestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        // 生产者发布事件 【生成消息到 Disruptor 环形队列中】
        pictureEditEventProducer.publishEvent(requestMessage, session, user, pictureId);
    }

    /**
     * 连接关闭，释放资源
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        // 从 session 中获取所需信息
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        // 移除当前用户的编辑状态
        this.handleExitEditMessage(null, session, user, pictureId);
        // 删除会话
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (sessionSet != null) {
            sessionSet.remove(session);
            if (sessionSet.isEmpty()) {
                pictureSessions.remove(pictureId);
            }
        }
        // 构建响应消息
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("用户 %s 离开图片", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));
        // 广播
        this.broadcastToPicture(pictureId, pictureEditResponseMessage);
    }


    /**
     * 处理编辑操作消息
     */
    // 同步给 除了当前用户之外 的其他客户端，也就是说编辑操作不用再同步给自己
    public void handleEditActionMessage(PictureEditRequestMessage requestMessage, WebSocketSession session, User user, Long pictureId) throws IOException {
        // 正在编辑的用户
        Long editingUserId = pictureEditingUsers.get(pictureId);
        String editAction = requestMessage.getEditAction();
        PictureEditActionEnum actionEnum = PictureEditActionEnum.getEnumByValue(editAction);
        if (actionEnum == null) {
            log.error("无效的编辑操作");
            return;
        }
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
            responseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            responseMessage.setEditAction(editAction);
            responseMessage.setMessage("用户 " + user.getUserName() + " " + actionEnum.getText());
            responseMessage.setUser(userService.getUserVO(user));
            // 广播给除自己之外的所有用户，否则会造成重复编辑
            this.broadcastToPicture(pictureId, responseMessage, session);
        }
    }

    /**
     * 处理退出编辑消息
     */
    public void handleExitEditMessage(PictureEditRequestMessage requestMessage, WebSocketSession session, User
            user, Long pictureId) throws IOException {
        Long editingUserId = pictureEditingUsers.get(pictureId);
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            // 移除当前用户的编辑状态
            pictureEditingUsers.remove(pictureId);
            // 构建响应消息
            PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
            responseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            String message = String.format("用户 %s 退出编辑", user.getUserName());
            responseMessage.setMessage(message);
            responseMessage.setUser(userService.getUserVO(user));
            // 广播给所有用户
            this.broadcastToPicture(pictureId, responseMessage);
        }
    }

    /**
     * 处理进入编辑消息
     *
     * @param requestMessage 进入编辑请求消息
     * @param session        WebSocket 会话
     * @param user           用户
     * @param pictureId      图片 ID
     */
    public void handleEnterEditMessage(PictureEditRequestMessage requestMessage, WebSocketSession session, User
            user, Long pictureId) throws IOException {
        // 没有用户在编辑，才能进入编辑
        if (!pictureEditingUsers.containsKey(pictureId)) {
            // 记录当前用户正在编辑
            pictureEditingUsers.put(pictureId, user.getId());
            PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
            responseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            String message = String.format("用户 %s 进入编辑", user.getUserName());
            responseMessage.setMessage(message);
            responseMessage.setUser(userService.getUserVO(user));
            // 广播给所有用户
            this.broadcastToPicture(pictureId, responseMessage);
        }
    }

    /**
     * 广播消息给所有正在编辑的用户
     *
     * @param pictureId                  图片 ID
     * @param pictureEditResponseMessage 图片编辑响应消息
     * @param excludeSession             排除的会话
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage
            pictureEditResponseMessage, WebSocketSession excludeSession) throws IOException {
        Set<WebSocketSession> webSocketSessions = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(webSocketSessions)) {
            // 创建 ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            // 配置序列化：将 Long 类型转为 String，解决丢失精度问题
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance); // 支持 long 基本类型
            objectMapper.registerModule(module); // 注册模块
            // TextMessage需要String类型的消息当payload => 序列化：将 PictureEditResponseMessage 对象转为 JSON
            String message = objectMapper.writeValueAsString(pictureEditResponseMessage);
            TextMessage textMessage = new TextMessage(message);
            // 找到所有会话，发送消息
            for (WebSocketSession webSocketSession : webSocketSessions) {
                if (excludeSession != null && webSocketSession.getId().equals(excludeSession.getId())) {
                    continue; // 如果有排除的会话，则跳过
                }
                if (webSocketSession.isOpen()) { // 需要打开状态，sendMessage
                    webSocketSession.sendMessage(textMessage);
                }
            }
        }
    }

    /**
     * 广播消息给所有正在编辑的用户
     *
     * @param pictureId                  图片 ID
     * @param pictureEditResponseMessage 图片编辑响应消息
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws
            IOException {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }
}
