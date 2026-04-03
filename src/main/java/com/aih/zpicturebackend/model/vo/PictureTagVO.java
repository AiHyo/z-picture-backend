package com.aih.zpicturebackend.model.vo;

import com.aih.zpicturebackend.model.entity.PictureTag;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

@Data
public class PictureTagVO implements Serializable {

    private Long id;

    private String tagName;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;

    public static PictureTagVO objToVo(PictureTag pictureTag) {
        if (pictureTag == null) {
            return null;
        }
        PictureTagVO pictureTagVO = new PictureTagVO();
        BeanUtils.copyProperties(pictureTag, pictureTagVO);
        return pictureTagVO;
    }
}
