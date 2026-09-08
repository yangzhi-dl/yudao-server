package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import cn.iocoder.yudao.module.ai.core.chat.model.entity.ModelConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveModelSettingDTO {

    /**
     * 用户编号；为空时表示当前登录用户
     */
    private Long userId;

    private String creator;

    /**
     * 合并后的完整配置列表（由 Service 层组装后传入 Mapper）
     */
    private List<ModelConfig> configs;

}
