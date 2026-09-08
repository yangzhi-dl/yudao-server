package cn.iocoder.yudao.module.ai.knowledge.grap.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphPermissions {

    private GraphRoute route;

    private GraphWidget widget;

    private List<GraphFile> files;

}
