package com.aih.zpicturebackend.model.vo;

import com.aih.zpicturebackend.model.entity.PictureCategory;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

@Data
public class PictureCategoryVO implements Serializable {

    private Long id;

    private String categoryName;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;

    public static PictureCategoryVO objToVo(PictureCategory pictureCategory) {
        if (pictureCategory == null) {
            return null;
        }
        PictureCategoryVO pictureCategoryVO = new PictureCategoryVO();
        BeanUtils.copyProperties(pictureCategory, pictureCategoryVO);
        return pictureCategoryVO;
    }
}
