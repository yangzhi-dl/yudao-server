package cn.iocoder.yudao.module.ai.knowledge.document.model.entity;

import cn.iocoder.yudao.module.ai.search.model.BaseDocument;
import lombok.*;

import java.util.List;

@Data
@Builder /*@Builder可以让你类链式的调用你的代码，来初始化你的实例对象*/
@NoArgsConstructor /*注解在类上；为类提供一个无参的构造方法*/
@AllArgsConstructor /*注解在类上；为类提供一个全参的构造方法*/
@EqualsAndHashCode(callSuper = true)
public class DocumentSearch extends BaseDocument {

    private String title;

    private String summary;

    private List<String> tags;

    private String category;

}
