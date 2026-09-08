package cn.iocoder.yudao.module.ai.knowledge.grap.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphWidget {

    private GraphOperation node;

    public GraphOperation rel;

}
