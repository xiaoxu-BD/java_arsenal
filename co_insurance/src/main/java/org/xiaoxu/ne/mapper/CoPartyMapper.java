package org.xiaoxu.ne.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.xiaoxu.ne.entity.CoParty;

import java.util.List;

@Mapper
public interface CoPartyMapper {

    @Select("""
        SELECT insurer_code, share_ratio, sort_order
        FROM insurance_co_party
        WHERE draft_id = #{draftId}
        ORDER BY sort_order
    """)
    List<CoParty> findByDraftId(String draftId);
}