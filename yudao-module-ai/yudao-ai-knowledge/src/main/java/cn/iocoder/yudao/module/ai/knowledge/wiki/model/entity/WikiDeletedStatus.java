package cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder /*@Builder可以让你类链式的调用你的代码，来初始化你的实例对象*/
@NoArgsConstructor /*注解在类上；为类提供一个无参的构造方法*/
@AllArgsConstructor /*注解在类上；为类提供一个全参的构造方法*/
public class WikiDeletedStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long wikiId;

    private List<Long> ids;

    private Integer deleted;

    private String updater;

    private LocalDateTime updateTime;


}