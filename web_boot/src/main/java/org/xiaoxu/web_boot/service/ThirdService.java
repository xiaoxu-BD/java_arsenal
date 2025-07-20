package org.xiaoxu.web_boot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import org.xiaoxu.web_boot.common.PageResult;
import org.xiaoxu.web_boot.entity.ItoOrderRpaReturnOrderThird;
import org.xiaoxu.web_boot.entity.vo.ThirdVO;

import java.util.List;

/**
 * @className: ThirdService
 * @author: xiaoxu
 * @date: 2025/7/14 21:22
 * @Version: 1.0
 * @description:
 */
public interface ThirdService extends IService<ItoOrderRpaReturnOrderThird> {

    List<ItoOrderRpaReturnOrderThird> getOnCondition();
    List<String> getValue(List<ItoOrderRpaReturnOrderThird> list);

    PageInfo<ThirdVO> getPage(int pageNo, int pageSize);

    PageInfo<?> getPageByCondition(String id, String name, int pageNo, int pageSize);

    PageResult getPageResult(int pageNo, int pageSize);
}
