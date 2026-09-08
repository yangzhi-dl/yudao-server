package cn.iocoder.yudao.module.ai.knowledge.wiki.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
public enum WikiTypeEnum {

    /* 企业知识库 */
    ENTERPRISE(0),

    /* 部门知识库 */
    DEPARTMENT(1),

    /* 个人知识库 */
    INDIVIDUAL(2);

    @EnumValue
    private final int code;

}
