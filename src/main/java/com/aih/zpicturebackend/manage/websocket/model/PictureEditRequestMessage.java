package com.aih.zpicturebackend.manage.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PictureEditRequestMessage {

    /**
     * 消息类型，对应枚举类 "ENTER_EDIT", "EXIT_EDIT", "EDIT_ACTION"
     */
    private String type;

    /**
     * 执行的编辑动作，对应枚举类 "ZOOM_IN", "ZOOM_OUT", "ROTATE_LEFT", "ROTATE_RIGHT"
     */
    private String editAction;
}
