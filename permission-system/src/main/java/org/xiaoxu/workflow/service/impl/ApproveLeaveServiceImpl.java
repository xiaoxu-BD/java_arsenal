package org.xiaoxu.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.xiaoxu.pojo.SystemUsers;
import org.xiaoxu.mapper.UserMapper;
import org.xiaoxu.workflow.constant.ApprovalStatus;
import org.xiaoxu.workflow.dto.ApproveLeaveCreateDTO;
import org.xiaoxu.workflow.entity.ApproveLeave;
import org.xiaoxu.workflow.mapper.ApproveLeaveMapper;
import org.xiaoxu.workflow.service.ApproveLeaveService;
import org.xiaoxu.workflow.vo.ApproveLeaveVO;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 请假单服务实现
 */
@Service
@RequiredArgsConstructor
public class ApproveLeaveServiceImpl extends ServiceImpl<ApproveLeaveMapper, ApproveLeave>
        implements ApproveLeaveService {

    private final UserMapper userMapper;

    @Override
    public ApproveLeaveVO createLeave(ApproveLeaveCreateDTO dto, String username) {
        SystemUsers user = userMapper.selectOne(
                new LambdaQueryWrapper<SystemUsers>().eq(SystemUsers::getUsername, username));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        ApproveLeave leave = new ApproveLeave();
        leave.setUserId(user.getId());
        leave.setUserName(username);
        leave.setLeaveReason(dto.getLeaveReason());
        leave.setLeaveDay(dto.getLeaveDay());
        leave.setBeginTime(dto.getBeginTime());
        leave.setEndTime(dto.getEndTime());
        leave.setLeaveType(dto.getLeaveType());
        leave.setStatus(ApprovalStatus.DRAFT.name());
        leave.setIdentifier(UUID.randomUUID().toString().replace("-", ""));
        leave.setCreator(username);

        save(leave);
        return toVO(leave);
    }

    @Override
    public List<ApproveLeaveVO> getMyLeaves(String username) {
        List<ApproveLeave> list = list(
                new LambdaQueryWrapper<ApproveLeave>()
                        .eq(ApproveLeave::getUserName, username)
                        .orderByDesc(ApproveLeave::getCreateTime));
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ApproveLeaveVO getDetail(Long id) {
        ApproveLeave leave = getById(id);
        if (leave == null) {
            throw new RuntimeException("请假单不存在");
        }
        return toVO(leave);
    }

    private ApproveLeaveVO toVO(ApproveLeave leave) {
        ApproveLeaveVO vo = new ApproveLeaveVO();
        vo.setId(leave.getId());
        vo.setUserName(leave.getUserName());
        vo.setStatus(leave.getStatus());
        vo.setLeaveReason(leave.getLeaveReason());
        vo.setLeaveDay(leave.getLeaveDay());
        vo.setBeginTime(leave.getBeginTime());
        vo.setEndTime(leave.getEndTime());
        vo.setLeaveType(leave.getLeaveType());
        vo.setProcessInstanceId(leave.getProcessInstanceId());
        vo.setCreator(leave.getCreator());
        vo.setCreateTime(leave.getCreateTime());
        vo.setUpdater(leave.getUpdater());
        vo.setUpdateTime(leave.getUpdateTime());
        vo.setIdentifier(leave.getIdentifier());
        return vo;
    }
}
