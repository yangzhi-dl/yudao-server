package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import cn.iocoder.yudao.module.ai.core.rag.enums.FileModelTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelUseInfo {

    private List<String> context;

    private FileModelTypeEnum type;

}
