package cn.iocoder.yudao.module.ai.knowledge.common.convert;

import cn.iocoder.yudao.module.ai.knowledge.document.dal.dataobject.Document;
import cn.iocoder.yudao.module.ai.knowledge.document.model.vo.FindDocumentDetailVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DocumentDetailConvert {
    /**
     * 初始化 convert 实例
     */
    DocumentDetailConvert INSTANCE = Mappers.getMapper(DocumentDetailConvert.class);

    /**
     * 将 DO 转化为 VO
     */
    FindDocumentDetailVO convertDO2VO(Document bean);

}
