package com.aih.zpicturebackend.model.vo;

import com.aih.zpicturebackend.model.entity.PictureAiTask;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

@Data
public class PictureAiTaskVO implements Serializable {

    private Long id;

    private Long pictureId;

    private Long userId;

    private String taskType;

    private Integer taskStatus;

    private String resultUrl;

    private String errorMessage;

    private Date finishTime;

    private Date createTime;

    private static final long serialVersionUID = 1L;

    public static PictureAiTaskVO objToVo(PictureAiTask pictureAiTask) {
        if (pictureAiTask == null) {
            return null;
        }
        PictureAiTaskVO pictureAiTaskVO = new PictureAiTaskVO();
        BeanUtils.copyProperties(pictureAiTask, pictureAiTaskVO);
        return pictureAiTaskVO;
    }
}
