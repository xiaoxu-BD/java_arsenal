package org.xiaoxu.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xiaoxu.entity.CoPolicy;

import java.util.List;
import java.util.Map;

@Mapper
public interface CoPolicyMapper {
    int insert(CoPolicy coPolicy);
    int insertBatch(@Param("list") List<CoPolicy> list);
 
    List<CoPolicy> selectByMasterPolicyNo(@Param("masterPolicyNo") String masterPolicyNo);
    List<CoPolicy> selectActiveByMasterPolicyNo(@Param("masterPolicyNo") String masterPolicyNo);
    List<Map<String, Object>> countByStatus(@Param("masterPolicyNo") String masterPolicyNo);
 
    int updateStatus(CoPolicy coPolicy);
    int cancelAllByMasterPolicyNo(@Param("masterPolicyNo") String masterPolicyNo);
}
 