package org.xiaoxu.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.entity.CoParty;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface CoPartyMapper {


    CoParty selectByDraftId(String draftId);



    BigDecimal sumRatioByDraftId(String draftId);


    void  insertBatch(List<CoParty> coPartyList);


}
