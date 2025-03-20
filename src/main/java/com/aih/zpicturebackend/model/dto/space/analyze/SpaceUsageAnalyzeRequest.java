package com.aih.zpicturebackend.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true) // 否则equals和hashcode方法会忽略父类的字段
@Data
public class SpaceUsageAnalyzeRequest extends SpaceAnalyzeRequest{

}
