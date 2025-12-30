package org.xiaoxu.web_boot.service.entity.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaoxu.web_boot.entity.domain.EmployeeProject;
import org.xiaoxu.web_boot.entity.domain.dto.EmployeeProjectDTO;
import org.xiaoxu.web_boot.mapper.EmployeeProjectMapper;
import org.xiaoxu.web_boot.service.entity.EmployeeProjectService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @className: EmployeeProjectServiceImpl
 * @author: xiaoxu
 * @date: 2025/12/28 11:02
 * @Version: 1.0
 * @description:
 */
@Service
public class EmployeeProjectServiceImpl  extends ServiceImpl<EmployeeProjectMapper, EmployeeProject> implements EmployeeProjectService {

    @Autowired
    private EmployeeProjectMapper employeeProjectMapper;

    @Override
    public List<EmployeeProject> getEmployeeProjects(Long empId) {
        LambdaQueryWrapper<EmployeeProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmployeeProject::getEmpId,empId);
        List<EmployeeProject> result = this.list(wrapper);
        return result;
    }

    @Override
    public List<EmployeeProjectDTO> listByEmpIds(List<Long> ids) {
        return employeeProjectMapper.listByEmpIds(ids);
    }

    @Transactional
    @Override
    public void insertBatchByMp(Long empId, List<Long> projectIds) {

        // 全删全增
        LambdaQueryWrapper<EmployeeProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmployeeProject::getEmpId,empId);
        this.remove(wrapper);
        List<EmployeeProject> employeeProjectList = projectIds.stream().map(
                projectId -> {
                    EmployeeProject employeeProject = new EmployeeProject();
                    employeeProject.setEmpId(empId);
                    employeeProject.setProjectId(projectId);
                    return employeeProject;
                }
        ).toList();
       saveBatch(employeeProjectList);
    }

    @Override
    public void insertBatchByMyBatis(Long empId, List<Long> projectIds) {
        employeeProjectMapper.batchInsert(empId,projectIds);
    }


    @Transactional(rollbackFor = Exception.class)
    public void incrementBatch(Long empId, List<Long> projectIds){
        // 1. 查询现有 projectIds
        List<Long> oldProjectIds = this.list(new LambdaQueryWrapper<EmployeeProject>()
                        .eq(EmployeeProject::getEmpId, empId)
                        .select(EmployeeProject::getProjectId))  // 只查 project_id
                        .stream().map(EmployeeProject::getProjectId).toList();

        // 2. 计算需要删除的 (页面上没有勾选的)
        List<Long> toDelete = oldProjectIds.stream().filter(id -> !projectIds.contains(id)).collect(Collectors.toList());

        // 3. 计算需要新增的 (计算出 已有的情况下 新增的勾选)
        List<Long> toAdd = projectIds.stream().filter(id -> !oldProjectIds.contains(id)).toList();

// 4. 删除
        if (!toDelete.isEmpty()) {
            this.remove(new LambdaQueryWrapper<EmployeeProject>().eq(EmployeeProject::getEmpId, empId).in(EmployeeProject::getProjectId, toDelete));
        }

// 5. 新增
        if (!toAdd.isEmpty()) {
            List<EmployeeProject> addList = toAdd.stream()
                    .map(pid -> {
                        EmployeeProject ep = new EmployeeProject();
                        ep.setEmpId(empId);
                        ep.setProjectId(pid);
                        return ep;
                    }).collect(Collectors.toList());
            this.saveBatch(addList);
        }

    }

}
