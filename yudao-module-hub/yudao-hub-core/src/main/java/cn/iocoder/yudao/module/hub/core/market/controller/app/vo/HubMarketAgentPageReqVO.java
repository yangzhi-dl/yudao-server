package cn.iocoder.yudao.module.hub.core.market.controller.app.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hub.core.market.enums.HubMarketAgentTypeEnum;
import cn.iocoder.yudao.module.hub.core.market.enums.HubMarketSortTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "用户前台 - Hub 智能体市场分页请求对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class HubMarketAgentPageReqVO extends PageParam {

    @Schema(description = "搜索关键词，匹配名称、摘要和服务提供方", example = "报告")
    @Size(max = 100, message = "搜索关键词长度不能超过 100 个字符")
    private String keyword;

    @Schema(description = "分类编号", example = "1061148")
    private Long categoryId;

    @Schema(description = "标签编号", example = "1061170")
    private Long tagId;

    @Schema(description = "智能体类型：0 单智能体，1 多智能体", example = "0")
    @InEnum(value = HubMarketAgentTypeEnum.class, message = "智能体类型必须是 {value}")
    private Integer agentType;

    @Schema(description = "是否推荐", example = "true")
    private Boolean recommended;

    @Schema(description = "排序方式：0 综合排序，1 最新发布", example = "0")
    @InEnum(value = HubMarketSortTypeEnum.class, message = "排序方式必须是 {value}")
    private Integer sortType = HubMarketSortTypeEnum.COMPREHENSIVE.getType();

}
