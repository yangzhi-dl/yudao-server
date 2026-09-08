package cn.iocoder.yudao.module.hub.core.market.controller.app.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "用户前台 - Hub 智能体集群分页请求对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class HubClusterPageReqVO extends PageParam {

}
