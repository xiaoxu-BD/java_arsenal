package org.xiaoxu.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xiaoxu.workflow.dto.ApproveLeaveCreateDTO;
import org.xiaoxu.workflow.entity.ApproveLeave;
import org.xiaoxu.workflow.vo.ApproveLeaveVO;

import java.util.List;

/**
 * 请假单服务接口
 */
public interface ApproveLeaveService extends IService<ApproveLeave> {

    ApproveLeaveVO createLeave(ApproveLeaveCreateDTO dto, String username);

    List<ApproveLeaveVO> getMyLeaves(String username);

    ApproveLeaveVO getDetail(Long id);
}
