package org.xiaoxu.mapper;

import java.util.List;
import org.xiaoxu.domain.ModTab;

public interface ModTabMapper {

    int deleteByPrimaryKey(Long id);

    int insert(ModTab row);

    int insertSelective(ModTab row);

    ModTab selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ModTab row);

    int updateByPrimaryKeyWithBLOBs(ModTab row);

    int updateByPrimaryKey(ModTab row);

    List<ModTab> selectList(List<Integer> idList);
}
